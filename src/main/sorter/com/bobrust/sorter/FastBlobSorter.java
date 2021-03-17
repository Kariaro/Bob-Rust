package com.bobrust.sorter;

import java.util.*;

public class FastBlobSorter {
	
	public static BlobList sort(BlobList data) {
		return new BlobList(sort0(data.list()));
	}

	private static Map<Blob, List<Blob>> map = new HashMap<>();
	private static List<Blob> sort0(List<Blob> original) {
		List<Blob> list = new ArrayList<>(original);
		List<Blob> out = new ArrayList<>();
		
		{
			out.add(list.get(0));
			list.remove(0);
		}
		
		/* Recalculate the intersections */ {
			map.clear();
			
			// Takes 200 ms for 8000 shapes
			for(int i = 0; i < list.size(); i++) {
				Blob blob = list.get(i);
				map.put(blob, get_intersections(blob, list, i));
			}
		}
		
		// Takes 800 ms for 8000 shapes
		while(!list.isEmpty()) {
			Blob last = out.get(out.size() - 1);
			int index = find_best_fast(last.size, last.color, list);
			out.add(list.remove(index));
		}
		
		map.clear();
		return out;
	}
	
	// TODO: If we used a linked hash set we could make it even faster
	private static int find_best_fast(int size, int color, List<Blob> list) {
		int one_match_index = 0;
		
		for(int i = 0; i < list.size(); i++) {
			Blob s = list.get(i);
			boolean sm = s.size == size;
			boolean cm = s.color == color;
			
			// If neither color nor size matches skip.
			if(!cm && !sm) continue;
			
			{
				// If this code can be optimized we can make this sorter even faster
				List<Blob> cols = map.get(s);
				for(int j = 0; j < cols.size(); j++) {
					// Takes 400 ms for 8000 shapes
					if(list.contains(cols.get(j))) {
						break;
					}
					
					// Remove elements to help speed
					cols.remove(j--);
				}
				
				if(!cols.isEmpty()) {
					// Found collision
					continue;
				}
			}
			
			// There is no noticable performance gain changing this
			if(sm) {
				if(cm) {
					return i;
				} else {
					if(one_match_index == 0) {
						one_match_index = i;
					}
				}
			} else if(cm) {
				if(one_match_index == 0) {
					one_match_index = i;
				}
			}
		}
		
		return one_match_index;
	}
	
	private static List<Blob> get_intersections(Blob blob, List<Blob> list, int length) {
		// Do not create new instances if not needed
		List<Blob> result = null;
		int s2 = blob.size * 2;
		
		for(int i = 0; i < length; i++) {
			Blob s = list.get(i);
			int s1 = s.size * 2;
			
			// Experimental
			if(s1 == s2 && s.color == blob.color) continue;
			
			float x = s.x - blob.x;
			float y = s.y - blob.y;
			int sum = s1 + s2;
			if(x * x + y * y < sum * sum) {
				if(result == null) {
					result = new ArrayList<>();
				}
				
				result.add(s);
			}
		}
		
		return result == null ? Collections.emptyList():result;
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
