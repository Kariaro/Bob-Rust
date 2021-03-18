package com.bobrust.sorter;

import java.util.Arrays;

public class IntList {
	private int[] array;
	private int size;
	
	public IntList() {
		this.array = new int[10];
	}
	
	public int size() {
		return size;
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	public int get(int index) {
		return array[index];
	}
	
	private int[] grow(int length) {
		int old_length = array.length;
		if(old_length > 0) {
			return array = Arrays.copyOf(array, Math.max(length - old_length, old_length >> 1) + old_length);
		} else {
			return array = new int[Math.max(10, length)];
		}
	}
	
	public void add(int value) {
		if(size == array.length) array = grow(size + 1);
		array[size++] = value;
	}
	
	public void remove(int index) {
		final int[] arr = array;
		
		final int new_size = size - 1;
		if(new_size > index) {
			System.arraycopy(arr, index + 1, arr, index, new_size - index);
		}
		
		size = new_size;
	}
	
	private static final IntList EMPTY_LIST = new IntList() {
		public int get(int index) { return 0; }
		public void add(int value) {}
		public void remove(int index) {}
		public int size() { return 0; }
		public boolean isEmpty() { return true; }
	};
	public static final IntList emptyList() {
		return EMPTY_LIST;
	}
}
