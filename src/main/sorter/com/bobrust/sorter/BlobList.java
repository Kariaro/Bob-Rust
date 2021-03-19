package com.bobrust.sorter;

import java.util.*;

public class BlobList {
	static final int[] SIZES = new int[] { 1, 2, 4, 6, 10, 13 };
	static final int[] COLORS = new int[] {
		0x2ECC70, 0x16A184, 0x3499DA, 0xF1C310,
		0x8F45AD, 0x99A3A2, 0x34495D, 0x2E9E87,
		0x1EE018, 0xB07AC3, 0xE77F21, 0xECF0F1,
		0x26AE60, 0x21CBF1, 0x7E4D29, 0xEF4431,
		0x4AD4BC, 0x453021, 0x313131, 0x010201,
	};
	
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
	
	public static BlobList populate(int length) {
		Random random = new Random();
		BlobList list = new BlobList();
		for(int i = 0; i < length; i++) {
			list.add(Blob.get(
				random.nextInt(512),
				random.nextInt(512),
				BlobList.SIZES[random.nextInt(BlobList.SIZES.length)],
				BlobList.COLORS[random.nextInt(BlobList.COLORS.length)]
			));
		}
		
		return list;
	}
}
