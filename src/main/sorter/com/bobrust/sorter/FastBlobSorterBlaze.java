package com.bobrust.sorter;

import java.util.*;
import java.util.stream.Collectors;

public class FastBlobSorterBlaze {
	
	private static class Piece {
		final Blob blob;
		final int index;
		
		public Piece(Blob blob, int index) {
			this.blob = blob;
			this.index = index;
		}
	}

	private static Piece[] pieces;
	private static List<Integer>[] map;
	
	@SuppressWarnings("unchecked")
	public static BlobList sort(BlobList data) {
		// Init arrays
		pieces = new Piece[data.size()];
		map = new List[data.size()];
		
		for(int i = 0; i < data.size(); i++) {
			pieces[i] = new Piece(data.get(i), i);
		}
		
		List<Piece> list = sort0(pieces.clone());
		
		// Remove references
		map = null;
		pieces = null;
		
		return new BlobList(list.stream().map((x) -> x.blob).collect(Collectors.toList()));
	}
	
	private static List<Piece> sort0(Piece[] array) {
		List<Piece> out = new ArrayList<>();
		out.add(array[0]);
		array[0] = null;
		
		/* Recalculate the intersections */ {
			// Takes 200 ms for 8000 shapes
			for(int i = 1; i < array.length; i++) {
				map[i] = get_intersections(array[i].blob, array, i);
			}
		}
		
		int idx = 0;
		// Takes ? ms for 8000 shapes
		while(++idx < array.length) {
			Blob last = out.get(out.size() - 1).blob;
			int index = find_best_fast(last.size, last.color, array);
			Piece next = array[index];
			out.add(next);
			array[index] = null;
		}
		
		return out;
	}
	
	private static int find_best_fast(int size, int color, Piece[] array) {
		int one_match_index = -1;
		int first_non_null = -1;
		
		for(int i = 0; i < array.length; i++) {
			Piece p = array[i];
			if(p == null) continue;
			
			if(first_non_null == -1) {
				first_non_null = i;
			}
			
			boolean sm = p.blob.size == size;
			boolean cm = p.blob.color == color;
			
			// If neither color nor size matches skip.
			if(!cm && !sm) continue;
			
			{
				// If this code can be optimized we can make this sorter even faster
				List<Integer> cols = map[p.index];
				for(int j = 0; j < cols.size(); j++) {
					// Takes ? ms for 8000 shapes
					if(array[cols.get(j)] != null) {
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
					if(one_match_index == -1) {
						one_match_index = i;
					}
				}
			} else if(cm) {
				if(one_match_index == -1) {
					one_match_index = i;
				}
			}
		}
		
		if(one_match_index == -1) {
			return first_non_null;
		}
		
		return one_match_index;
	}
	
	private static List<Integer> get_intersections(Blob blob, Piece[] array, int length) {
		// Do not create new instances if not needed
		List<Integer> result = null;
		int s2 = blob.size * 2;
		
		for(int i = 1; i < length; i++) {
			Blob s = array[i].blob;
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
				
				result.add(i);
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
