package com.bobrust.sorter;

import java.util.*;

public class FastBlobSorter {
	// Average time for the sorting https://www.desmos.com/calculator/tjwldcg72h
	
	private static class Piece {
		final Blob blob;
		final int index;
		
		public Piece(Blob blob, int index) {
			this.blob = blob;
			this.index = index;
		}
	}

	private static IntList[] map;
	public static BlobList sort(BlobList data) {
		Piece[] pieces = new Piece[data.size()];
		map = new IntList[data.size()];
		
		for(int i = 0; i < data.size(); i++) {
			pieces[i] = new Piece(data.get(i), i);
		}
		
		BlobList list = new BlobList(Arrays.asList(sort0(pieces)));
		map = null;
		
		return list;
	}
	
	private static Blob[] sort0(Piece[] array) {
		Blob[] out = new Blob[array.length];
		out[0] = array[0].blob;
		array[0] = null;
		
		/* Recalculate the intersections */ {
			// Takes 200 ms for 8000 shapes
			for(int i = 1; i < array.length; i++) {
				map[i] = get_intersections(array[i].blob, array, i);
			}
		}
		
		int start = 1;
		int i = 0;
		while(++i < array.length) {
			Blob last = out[i - 1];
			int index = find_best_fast(last.size, last.color, start, array);
			out[i] = array[index].blob;
			array[index] = null;
			
			// Make the starting point shift place.. Will most of the time half the calculations
			if(index == start) {
				for(; start < array.length; start++) {
					if(array[start] != null) break;
				}
			}
		}
		
		return out;
	}
	
	private static int find_best_fast(int size, int color, int start, Piece[] array) {
		int one_match_index = -1;
		int first_non_null = -1;
		
		for(int i = start; i < array.length; i++) {
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
				IntList cols = map[p.index];
				while(!cols.isEmpty()) {
					if(array[cols.get(0)] != null) {
						break;
					}
					
					cols.remove(0);
				}
				
				if(!cols.isEmpty()) { 
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
	
	private static IntList get_intersections(Blob blob, Piece[] array, int length) {
		IntList result = null;
		int s2 = blob.size * 2;
		
		for(int i = 1; i < length; i++) {
			Blob s = array[i].blob;
			int s1 = s.size * 2;
			
			// Experimental
			if(s1 == s2 && s.color == blob.color) continue;
			
			int x = s.x - blob.x;
			int y = s.y - blob.y;
			int sum = s1 + s2;
			if(x * x + y * y < sum * sum) {
				if(result == null) {
					result = new IntList();
				}
				
				result.add(i);
			}
		}
		
		return result == null ? IntList.emptyList():result;
	}
}
