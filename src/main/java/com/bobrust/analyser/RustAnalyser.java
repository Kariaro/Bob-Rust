package com.bobrust.analyser;

import java.awt.*;

import com.bobrust.analyser.RustGUIAnalyser.RColor;

public class RustAnalyser {
	private static final long TIME_MS_MAX = 100;
	private static final long TIME_MS_MIN = 0;
	
	
	private final Robot robot;
	private final RustGUIAnalyser info;
	private final RustController rust_old;
	private final RustAdvancedController rust;
	
	public RustAnalyser(Robot robot, RustGUIAnalyser info, RustController rust) {
		this.robot = robot;
		this.info = info;
		this.rust_old = rust;
		this.rust = new RustAdvancedController(robot, info);
	}
	
	public void analyse(Rectangle screen) {
		// Make sure we do not have any auto delay
		robot.setAutoDelay(0);
		
		
		color_analyse(screen);
	}
	
	private int delay;
	
	private void initCorrectColors() {
		System.out.println(robot.getAutoDelay());
		
		RColor[] colors = info.getColorButtons();
		Color last = getPreviewColor();
		for(int i = 0; i < colors.length; i++) {
			RColor color = colors[i];
			rust.setBrushColor(color.index(), 100);
			rust.sleep(delay);
			Color next = getPreviewColor();
			
			System.out.println("Check: " + last + " != " + getPreviewColor());
			if(last.equals(next)) {
				long start = System.nanoTime();
				int timeout = 0;
				while(last.equals(next = getPreviewColor())) {
					rust.sleep(1);
					if(timeout++ > 100) break;
				}
				if(timeout > 100) {
					// Retry the loop
					i--;
					continue;
				}
				
				int new_delay = (int)((System.nanoTime() - start) / 1000000) + 10;
				if(new_delay > this.delay) {
					this.delay = new_delay;
				}
				
				last = next;
			} else {
				last = next;
			}
			
			System.out.println("Update: " + next + ", " + this.delay);
			info.colors.set(i, new RColor(next, color.getPoint(), i));
		}
		
		System.out.println("Delay: " + delay + " ms");
	}
	
	private void color_analyse(Rectangle screen) {
		for(int i = 0; i < 10; i++) rust.giveFocus(100);
		rust.setBrushColor_test(Color.black, 1000);
		
		initCorrectColors();
		RColor[] colors = info.getColorButtons();
		
		int times = 100;
		int count = colors.length;
		long[][] tests = new long[times][count];
		long[] maxs = new long[count];
		long[] mins = new long[count];
		for(int i = 0; i < count; i++) {
			maxs[i] = TIME_MS_MAX;
			mins[i] = TIME_MS_MIN;
		}
		
		for(int i = 0; i < times; i++) {
			System.out.println("Performing checks");
			
			for(int j = 0; j < count; j++) {
				RColor color = colors[j];
				
				long delay = (maxs[j] + mins[j]) / 2;
				long nano = System.nanoTime();
				rust.setBrushColor(color.index(), delay);
				long end = System.nanoTime();
				
				// Sleep atleast 1 frame
				rust.sleep(this.delay);
				// Should we add delay for 
				RColor view = rust.getClosest(getPreviewColor());
				if(view != color) { // Failed to update
					System.out.println("Not all passed increasing delay");
					this.delay += 10;
					mins[j] = delay;
				} else {
					maxs[j] = delay;
				}
				
				tests[i][j] = (end - nano);
				System.out.printf("(%s)\t[%s]\t[delay: %s ms] time: %.2f ms\n", color, view, delay, (end - nano) / 1000000.0);
			}
			
			long max = TIME_MS_MIN;
			long min = TIME_MS_MAX;
			for(int j = 0; j < count; j++) {
				if(maxs[j] > max) max = maxs[j];
				if(mins[j] < min) min = mins[j];
			}
			
			// Recalibrating group
			for(int j = 0; j < count; j++) {
				maxs[j] = max;
				mins[j] = min;
			}
			
			System.out.printf("New delay: [%s, %s]\t redraw[%s]\n", min, max, this.delay);
			
			if(min == max) break;
		}
		
		{
			long max = TIME_MS_MIN;
			long min = TIME_MS_MAX;
			for(int j = 0; j < count; j++) {
				if(maxs[j] > max) max = maxs[j];
				if(mins[j] < min) min = mins[j];
			}
			
			calibrate(this.delay, (max + min) / 2);
		}
	}
	
	private void calibrate(long redraw_delay, long mouse_delay) {
		rust.setBrushColor_test(Color.black, redraw_delay + mouse_delay);
		rust.sleep(redraw_delay);
		
		long MAX = redraw_delay * 3;
		long MIN = 1;
		
		RColor[] colors = info.getColorButtons();
		
		int times = 100;
		int count = colors.length;
		long[][] tests = new long[times][count];
		long[] maxs = new long[count];
		long[] mins = new long[count];
		for(int i = 0; i < count; i++) {
			maxs[i] = MAX;
			mins[i] = MIN;
		}
		
		long old_delay = (maxs[0] + mins[0]) / 2;
		for(int i = 0; i < times; i++) {
			System.out.println("Performing checks");
			
			boolean passed = true;
			for(int j = 0; j < count; j++) {
				RColor color = colors[j];
				
				long delay = (maxs[j] + mins[j]) / 2;
				long nano = System.nanoTime();
				rust.setBrushColor(color.index(), mouse_delay);
				long end = System.nanoTime();
				
				rust.sleep(delay);
				RColor view = rust.getClosest(getPreviewColor());
				if(view != color) { // Failed to update
					mins[j] = Math.max(delay - 10, MIN);
					maxs[j] = Math.min(maxs[j] + 10, MAX);
					passed = false;
				} else {
					maxs[j] = Math.min(delay + 10, MAX);
				}
				
				tests[i][j] = (end - nano);
				//System.out.printf("(%s)\t[%s]\t[delay: %s ms] time: %.2f ms\n", color, view, delay, (end - nano) / 1000000.0);
			}
			
			long nanos = 0;
			for(int j = 0; j < count; j++) {
				nanos += tests[i][j];
			}
			
			long max = 0;
			long min = 0;
			for(int j = 0; j < count; j++) {
				max += maxs[j];
				min += mins[j];
			}
			
			max /= count;
			min /= count;
			
			if(!passed) {
				min += 10;
				max += 10;
			}
			
			// Recalibrating group
			for(int j = 0; j < count; j++) {
				maxs[j] = max;
				mins[j] = min;
			}
			
			this.delay = (int)((min + max) / 2);
			System.out.printf("New delay: [%s, %s]\t redraw[%s]\t[%.2f ms]\t[%s]\n",
				min, max, old_delay, (nanos / (count * 1000000.0)), passed ? "PASSED":"BAD"
			);
			
			if(min == max) break;
		}
		
		{
			long max = MIN;
			long min = MAX;
			for(int j = 0; j < count; j++) {
				if(maxs[j] > max) max = maxs[j];
				if(mins[j] < min) min = mins[j];
			}
			
			this.delay = (int)((min + max) / 2);
			
			System.out.printf("New redraw: [%s]\n", this.delay);
		}
	}
	
	private Color getPreviewColor() {
		Point p = info.getColorPreview();
		return robot.getPixelColor(p.x, p.y);
	}
}
