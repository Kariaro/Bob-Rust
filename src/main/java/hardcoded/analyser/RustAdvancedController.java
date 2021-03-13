package hardcoded.analyser;

import java.awt.*;
import java.awt.event.InputEvent;

import hardcoded.analyser.RustGUIAnalyser.RColor;

public class RustAdvancedController {
	private final Robot robot;
	private final RustGUIAnalyser anr;
	
	public RustAdvancedController(Robot robot, RustGUIAnalyser anr) {
		this.robot = robot;
		this.anr = anr;
	}
	
	public void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch(Exception e) {
			
		}
	}
	
	public void giveFocus(long millis) {
		click(anr.getFocusPoint(), millis);
	}
	
	public void setBrushOpacity(int index, long millis) {
		click(anr.getOpacityButtons(), index, millis);
	}
	
	public void setBrushSize(int index, long millis) {
		click(anr.getSizeButtons(), index, millis);
	}
	
	public void setBrushShape(Shape shape, long millis) {
		click(anr.getShapeButtons(), shape.ordinal(), millis);
	}
	
	public void setBrushColor_test(Color color, long millis) {
		click(getClosest(color).getPoint(), millis);
	}
	
	public void setBrushColor(int index, long millis) {
		click(anr.getColorButtons()[index].getPoint(), millis);
	}
	
	private void click(Point[] array, int index, long millis) {
		if(index < 0) index = 0;
		if(index >= array.length) index = array.length - 1;
		click(array[index], millis);
	}
	
	private void click(Point p, long millis) {
		robot.mouseMove(p.x, p.y);
		sleep(millis);
		mousePress(p);
		
		Point next = MouseInfo.getPointerInfo().getLocation();
		if(next.x != p.x || next.y != p.y) {
			throw new IllegalStateException("Mouse was moved by the user. Stopping...");
		}
	}
	
	private void mousePress(Point p) {
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}
	
	public RColor getClosest(Color color) {
		RColor[] array = anr.getColorButtons();
		
		RColor best = null;
		float score = Float.MAX_VALUE;
		for(RColor rc : array) {
			Color cc = rc.getColor();
			float rd = (color.getRed() - cc.getRed()) / 255.0f;
			float gd = (color.getGreen() - cc.getGreen()) / 255.0f;
			float bd = (color.getBlue() - cc.getBlue()) / 255.0f;
			float cs = (rd * rd + gd * gd + bd * bd);
			
			if(cs < score) {
				score = cs;
				best = rc;
			}
		}
		
		return best;
	}
	
	public static enum Shape {
		SOFT_HALO,
		CIRCLE,
		STRONG_HALO,
		SQUARE
	}
}
