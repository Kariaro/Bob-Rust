package com.bobrust.sorter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlobList {
	static final int[] SIZES = new int[] { 1, 2, 4, 6, 10, 13 };
	
	private final List<Blob> list;
	
	public BlobList() {
		list = new ArrayList<>();
	}
	
	public BlobList(Collection<Blob> collection) {
		list = new ArrayList<>(collection);
	}
	
	public int size() {
		return list.size();
	}
	
	public Blob get(int index) {
		return list.get(index);
	}
	
	public void add(Blob blob) {
		list.add(blob);
	}
	
	public List<Blob> list() {
		return list;
	}
	
	public String toString() {
		return list.toString();
	}
}
