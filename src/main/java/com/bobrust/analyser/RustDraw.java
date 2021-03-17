package com.bobrust.analyser;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;

import com.bobrust.analyser.RustController.Shape;
import com.bobrust.reader.BorstData;
import com.bobrust.reader.BorstReader;
import com.bobrust.reader.BorstShape;

public class RustDraw {
	protected RustGUIAnalyser info;
	protected RustController rust;
	protected RustAnalyser advanced;
	protected Robot robot;
	
	public RustDraw() throws AWTException {
		robot = new Robot();
		info = new RustGUIAnalyser(robot);
		rust = new RustController(robot, info);
		advanced = new RustAnalyser(robot, info, rust);
	}
	
	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch(Exception e) {
			
		}
	}
	
	public void test(Rectangle screen, Rectangle user, JLabel counter, BufferedImage raster) {
		if(raster == null) return;
		
		try {
			drawImageOnScreen(screen, user, counter);
		} catch(Exception e) {
			// Panic break out
			e.printStackTrace();
		}
	}
	
	private void drawImageOnScreen(Rectangle screen, Rectangle area, JLabel counter) {
		info.analyse(screen);
		advanced.analyse(screen);
		
		if(true) return;
		
		BorstData data = null;
		try {
			data = BorstReader.readFile(new File("C:\\Users\\Admin\\Desktop\\TextureRust\\wolf8000.borst"));
		} catch(IOException e) {
			e.printStackTrace();
			return;
		}
		
		if(data.instructions.length == 0) return;
		
		{
			BorstShape first = data.instructions[0];
			robot.setAutoDelay(20);
			
			System.out.println("RustDraw: start borst");
			
			rust.giveFocus();
			rust.giveFocus();
			rust.giveFocus();
			rust.giveFocus();
			
			sleep(50);
			
			rust.setBrushShape(Shape.CIRCLE);
			rust.setBrushSize(first.size);
			rust.setBrushOpacity(first.opacity);
			
			sleep(200);
		}
		
		int width = Math.max(data.width, data.height);
		int height = Math.max(data.width, data.height);
		int xo = (width - data.width) / 2;
		int yo = (height - data.height) / 2;
		
		int last_size = -1;
		int last_color = -1;
		
		for(int i = 0; i < data.instructions.length; i++) {
			counter.setText((i + 1) + "/" + (data.instructions.length));
			BorstShape inst = data.instructions[i];
			
			if(last_size != inst.size) {
				rust.setBrushSize(inst.size);
				last_size = inst.size;
			}
			
			if(last_color != inst.color) {
				rust.setBrushColor(inst.color);
				last_color = inst.color;
			}
			
			float rx = (inst.x + xo) / (width + 0.0f);
			float ry = (inst.y + yo) / (height + 0.0f);
			
			float tx = area.x + rx * area.width;
			float ty = area.y + ry * area.height;
			click((int)tx, (int)ty);
		}
	}
	
	private void click(int x, int y) {
		robot.mouseMove(x, y);
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		sleep(10);
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		
		Point next = MouseInfo.getPointerInfo().getLocation();
		if(next.x != x || next.y != y) {
			throw new IllegalStateException("Mouse was moved during the execution of the program.. Pause?");
		}
	}
}
