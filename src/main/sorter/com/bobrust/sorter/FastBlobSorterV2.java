package com.bobrust.sorter;

import java.awt.Point;
import java.util.*;

public class FastBlobSorterV2 {
	
	public static BlobList sort(BlobList data) {
		/*
		{
			System.out.println("ORIGINAL");
			debug(data.getList());
			System.out.println("NEW");
			debug(list);
			System.out.println("=".repeat(100));
		}
		*/
		
		return sort0(data.list());
	}
	
	private static BlobList sort0(List<Blob> original) {
		List<Blob> list = new ArrayList<>(original);
		BlobList result = new BlobList();
		List<Blob> out = result.list();
		
		{
			out.add(list.get(0));
			list.remove(0);
		}
		
		//long start1 = System.nanoTime();
		// 600 ms
		recalculateMap(list);
		//long time1 = System.nanoTime() - start1;
		
		//long start2 = System.nanoTime();
		while(!list.isEmpty()) {
			//System.out.println(list.size());
			add_next(out, list);
		}
		//long time2 = System.nanoTime() - start2;
		
		//System.out.printf("Time 1: %.2f ms\n", time1 / 1000000.0);
		//System.out.printf("Time 2: %.2f ms\n", time2 / 1000000.0);
		
		map.clear();
		return result;
	}
	
	private static void add_next(List<Blob> out, List<Blob> list) {
		Blob last = out.get(out.size() - 1);
		
		int index = find_best_fast(last, list);
		if(index != -1) {
			Blob best = list.get(index);
			list.remove(index);
			out.add(best);
		}
	}
	
	private static int find_best_fast(Blob match, List<Blob> list) {
		int s2 = match.size * 2;
		
		int one_match_index = 0;
		for(int i = 0; i < list.size(); i++) {
			Blob s = list.get(i);
			int s1 = s.size * 2;
			boolean cm = s.color == match.color;
			boolean sm = s1 == s2;
			
			// If neither color nor size matches skip.
			if(!cm && !sm) continue;
			
			{
				List<Blob> cols = map.get(s);
				for(int j = 0; j < cols.size(); j++) {
					if(!list.contains(cols.get(j))) {
						// Remove element to help speed
						cols.remove(j--);
					} else {
						break;
					}
				}
				
				if(!cols.isEmpty()) {
					// Found collision
					continue;
				}
			}
			
			if(sm) {
				if(cm) {
					return i;
				} else {
					one_match_index = i;
				}
			} else if(cm) {
				one_match_index = i;
			}
		}
		
		return one_match_index;
	}
	
	private static Map<Blob, List<Blob>> map = new HashMap<>();
	private static void recalculateMap(List<Blob> list) {
		map.clear();
		
		for(int i = 0; i < list.size(); i++) {
			Blob blob = list.get(i);
			map.put(blob, getIntersections(blob, list, i));
		}
	}
	
	private static List<Blob> getIntersections(Blob blob, List<Blob> list, int length) {
		List<Blob> result = new ArrayList<>();
		
		int s2 = blob.size * 2;
		
		for(int i = 0; i < length; i++) {
			Blob s = list.get(i);
			int s1 = s.size * 2;
			
			// Experimental
			if(s1 == s2 && s.color == blob.color) continue;
			
			
			if(Point.distance(s.x, s.y, blob.x, blob.y) < s1 + s2) {
				result.add(s);
			}
		}
		
		return result;
	}
	
	static void debug(List<Blob> list) {
		Blob last = null;
		
		System.out.println("[DEBUG]: Showing the changes of a list");
		int changes = 2;
		for(Blob blob : list) {
			if(last != null) {
				if(last.size != blob.size) changes++;
				if(last.color != blob.color) changes++;
			}
			last = blob;
			
			System.out.printf("  [%4d]: s=%d,\tc=%d\n", changes, blob.size, blob.color);
		}
	}
}
