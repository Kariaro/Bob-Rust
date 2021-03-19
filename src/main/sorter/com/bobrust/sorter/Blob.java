package com.bobrust.sorter;

import java.awt.Color;

public class Blob {
	public final int x;
	public final int y;
	public final int size;
	public final int color;
	private final int hash;
	
	private final int color_index;
	private final int size_index;
	
	protected Blob(int x, int y, int size, int color) {
		this.x = x;
		this.y = y;
		this.size = size;
		this.color = color;
		this.hash = (x) | (y << 14) | (((size + 1) ^ (color + 1)) << 28);
		
		int color_idx = 0;
		for(int i = 0; i < BlobList.COLORS.length; i++) {
			if(color == BlobList.COLORS[i]) {
				color_idx = i;
				break;
			}
		}
		
		int size_idx = 0;
		for(int i = 0; i < BlobList.SIZES.length; i++) {
			if(size == BlobList.SIZES[i]) {
				size_idx = i;
				break;
			}
		}
		
		color_index = color_idx;
		size_index = size_idx;
	}
	
	private Color _to_color;
	public Color toColor() {
		if(_to_color == null) {
			_to_color = new Color(color | (45 << 24), true);
		}
		
		return _to_color;
	}
	
	public int getSizeIndex() {
		return size_index;
	}
	
	public int getColorIndex() {
		return color_index;
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	public static Blob get(int x, int y, int size, int color) {
		return new Blob(x, y, size, color);
	}
	
	@Override
	public String toString() {
		return String.format("{ x: %d, y: %d, size: %d, color: #%06x }", x, y, size, color);
	}
}
