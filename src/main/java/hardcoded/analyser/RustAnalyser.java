package hardcoded.analyser;

import java.awt.*;

import hardcoded.analyser.RustGUIAnalyser.RColor;

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
		
		int times = 30;
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
					if(i == 0) {
						System.out.println("Not all passed increasing delay");
						this.delay += 10;
						j--;
						continue;
					}
					
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
			
			System.out.printf("New delay: [%s, %s]\n", min, max);
		}
	}
	
	private Color getPreviewColor() {
		Point p = info.getColorPreview();
		return robot.getPixelColor(p.x, p.y);
	}
}
