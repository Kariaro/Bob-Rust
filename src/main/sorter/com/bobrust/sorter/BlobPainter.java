package com.bobrust.sorter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;

public class BlobPainter {
	public void draw(Graphics2D g, int shapes, Color background, BlobList list) {
		if(list == null) return;
		
		Color old_color = g.getColor();
		Shape old_clip = g.getClip();
		g.setClip(0, 0, 512, 512);
		
		g.setColor(background);
		g.fillRect(0, 0, 512, 512);
		
		for(Blob blob : list.list()) {
			if(shapes-- <= 0) break;
			
			int size = blob.size;
			Color rgb = blob.toColor();
			int x = (int)(blob.x - size);
			int y = (int)(blob.y - size);
			
			g.setColor(rgb);
			g.fillOval(x, y, size * 2, size * 2);
		}
		
		g.setColor(old_color);
		g.setClip(old_clip);
	}
}
