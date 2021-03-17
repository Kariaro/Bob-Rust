package com.bobrust.analyser;

import java.awt.*;
import java.awt.event.InputEvent;

import com.bobrust.analyser.RustGUIAnalyser.RColor;

public class RustController {
	private final Robot robot;
	private final RustGUIAnalyser anr;
	
	public RustController(Robot robot, RustGUIAnalyser anr) {
		this.robot = robot;
		this.anr = anr;
	}
	
	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch(Exception e) {
			
		}
	}
	
	public void giveFocus() {
		click(anr.getFocusPoint());
	}
	
	public void setBrushOpacity(int level) {
		click(anr.getOpacityButtons(), level);
	}
	
	public void setBrushSize(int level) {
		click(anr.getSizeButtons(), level);
	}
	
	public void setBrushShape(Shape shape) {
		click(anr.getShapeButtons(), shape.ordinal());
	}
	
	public void setBrushColor(int rgb) {
		setBrushColor(new Color(rgb));
	}
	
	public void setBrushColor(Color color) {
		setBrushColor(getClosest(color));
	}
	
	public void setBrushColor(RColor color) {
		if(color == null) return;
		click(color.getPoint());
	}
	
	private void click(Point[] array, int index) {
		if(index < 0) index = 0;
		if(index >= array.length) index = array.length - 1;
		
		click(array[index]);
	}
	
	private void click(Point p) {
		robot.mouseMove(p.x, p.y);
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		sleep(10);
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		
		Point next = MouseInfo.getPointerInfo().getLocation();
		if(next.x != p.x || next.y != p.y) {
			throw new IllegalStateException("Mouse was moved by the user. Stopping...");
		}
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
