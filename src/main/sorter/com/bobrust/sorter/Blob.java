package com.bobrust.sorter;

import java.awt.Color;

public class Blob {
	public final int x;
	public final int y;
	public final int size;
	public final int color;
	private final int hash;
	
	protected Blob(int x, int y, int size, int color) {
		this.x = x;
		this.y = y;
		this.size = size;
		this.color = color;
		this.hash = (x) | (y << 14) | (((size + 1) ^ (color + 1)) << 28);
	}
	
	private transient Color _to_color;
	public Color toColor() {
		if(_to_color == null) {
			_to_color = new Color(color | (45 << 24), true);
		}
		
		return _to_color;
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
		return String.format("{ x: %.2f, y: %.2f, size: %d, color: #%06x }", x, y, size, color);
	}
}
