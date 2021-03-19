package com.bobrust.sorter;

import java.util.*;

public class FastBlobSorterExperimentalBck {
	private static final int MIN_SIZE = 8;
	// Average time for the sorting https://www.desmos.com/calculator/rhrjpnsvji
	
	private static class Piece {
		final Blob blob;
		final int index;
		
		public Piece(Blob blob, int index) {
			this.blob = blob;
			this.index = index;
		}
	}
	
	private static class QTree {
		private final QTree[] nodes = new QTree[4];
		
		private final IntList list = new IntList();
		private final int x;
		private final int y;
		private final int s;
		private final int hs;
		
		public QTree(int width, int height) {
			this(0, 0, Math.max(width, height));
		}
		
		private QTree(int x, int y, int s) {
			this.x = x;
			this.y = y;
			this.s = s;
			this.hs = s / 2;
		}
		
		public void add_piece(Piece piece) {
			int x = piece.blob.x - this.x;
			int y = piece.blob.y - this.y;
			int r = piece.blob.size;
			
			// TODO: Look at the memory usage and see if this is inefficient
			if(hs < MIN_SIZE) {// || (r >= hs && r <= s)) {
				list.add(piece.index);
				return;
			}
			
			if(x - r <= hs && y - r <= hs) add_piece(piece, 0);
			if(x + r >= hs && y - r <= hs) add_piece(piece, 1);
			if(x - r <= hs && y + r >= hs) add_piece(piece, 2);
			if(x + r >= hs && y + r >= hs) add_piece(piece, 3);
		}
		
		private void add_piece(Piece piece, int index) {
			QTree node = nodes[index];
			if(node == null) {
				nodes[index] = (node = new QTree(x + (index & 1) * hs, y + (index / 2) * hs, hs + (s & 1)));
			}
			
			node.add_piece(piece);
		}
		
		public List<Integer> get_pieces(Piece piece) {
			Set<Integer> set = new HashSet<>();
			get_pieces(piece, set);
			
			List<Integer> list = new ArrayList<>(set);
			list.sort(null);
			return list;
		}
		
		private void get_pieces(Piece piece, Set<Integer> set) {
			int x = piece.blob.x - this.x;
			int y = piece.blob.y - this.y;
			int r = piece.blob.size;
			
			if(hs < MIN_SIZE) {// || (r >= hs && r <= s)) {
				for(int i = 0; i < list.size(); i++) {
					set.add(list.get(i));
				}
			}
			
			if(x - r <= hs && y - r <= hs) get_pieces(piece, 0, set);
			if(x + r >= hs && y - r <= hs) get_pieces(piece, 1, set);
			if(x - r <= hs && y + r >= hs) get_pieces(piece, 2, set);
			if(x + r >= hs && y + r >= hs) get_pieces(piece, 3, set);
		}
		
		private void get_pieces(Piece piece, int index, Set<Integer> set) {
			QTree node = nodes[index];
			if(node != null) {
				node.get_pieces(piece, set);
			}
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
		
		// TODO: Give size information
		QTree tree = new QTree(512, 512);
		/* Calculate the intersections */ {
			// Add all the pieces to a quad tree
			for(int i = 1; i < array.length; i++) {
				tree.add_piece(array[i]);
			}
			
			// Use the quad tree to efficiently calculate the collisions
			for(int i = 1; i < array.length; i++) {
				// Worst case senario O(N^2) if every single circle is in the same position
				map[i] = get_intersections(array[i], array, tree);
			}
		}
		
		// 6 sizes * 20 colors
		IntList[] list_all = new IntList[6 * 20];
		IntList[] list_either = new IntList[6 * 20];
		IntList[][] cache = { list_all, list_either };
		
		/* Calculate the lists */ {
			for(Piece piece : array) {
				if(piece == null) continue;
				
				int color = piece.blob.getColorIndex();
				int size = piece.blob.getSizeIndex();
				
				/* all */ {
					IntList list = list_all[size + color * 6];
					if(list == null) {
						list_all[size + color * 6] = (list = new IntList());
					}
					
					list.add(piece.index);
				}
				
				/* either */ {
					for(int i = 0; i < BlobList.COLORS.length; i++) {
						// Colors is the X axis and has the upper value
						// Sizes is the Y axis and has the lowest value
						
						if(i == color) {
							// Add all the colors
							for(int j = 0; j < BlobList.SIZES.length; j++) {
								IntList list = list_either[j + color * 6];
								if(list == null) {
									list_either[j + color * 6] = (list = new IntList());
								}
								
								list.add(piece.index);
							}
						} else {
							IntList list = list_either[size + i * 6];
							if(list == null) {
								list_either[size + i * 6] = (list = new IntList());
							}
							
							list.add(piece.index);
						}
					}
				}
			}
			
			// Make sure we do not have any null values
			for(int i = 0; i < list_all.length; i++) {
				if(list_all[i] == null) list_all[i] = IntList.emptyList();
				if(list_either[i] == null) list_either[i] = IntList.emptyList();
			}
		}
		
		int start = 1;
		int i = 0;
		// TODO: Try make this O(N)
		// This is O(N^2)
		while(++i < array.length) {
			Blob last = out[i - 1];
			int index = find_best_fast_cache(last.getSizeIndex(), last.getColorIndex(), start, cache, array);
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
	
	private static int find_best_fast_cache(int size, int color, int start, IntList[][] cache, Piece[] array) {
		// If we precalculate this for each piece we could make the sorter O(N)
		
		// Precalculate arrays that sorts the array based on size and color.
		// And a map containing all sizes with the index (size + (color * 6)) to
		// allow for instantly finding shapes with the exact same combination and
		// to allow finding values with the same size or color.
		
		/* all */ {
			IntList list = cache[0][size + color * 6];
			for(int i = 0; i < list.size(); i++) {
				Piece p = array[list.get(i)];
				if(p == null) {
					// Remove elements from the list to ensure we remove memory
					list.remove(i--);
					continue;
				}
				
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
				
				// If we didn't have any collisions we return
				list.remove(i);
				return p.index;
			}
		}
		
		/* either */ {
			IntList list = cache[1][size + color * 6];
			for(int i = 0; i < list.size(); i++) {
				Piece p = array[list.get(i)];
				if(p == null) {
					// Remove elements from the list to ensure we remove memory
					list.remove(i--);
					continue;
				}
				
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
				
				// If we didn't have any collisions we return
				list.remove(i);
				return p.index;
			}
		}
		
		// If we didn't find any valid value we return the start because
		// that is the first non null value in the list.
		return start;
	}
	
	/*
	private static int find_best_fast(int size, int color, int start, Piece[] array) {
		int one_match_index = -1;
		
		// If we precalculate this for each piece we could make the sorter O(N)
		
		// Precalculate arrays that sorts the array based on size and color.
		// And a map containing all sizes with the index (size + (color * 6)) to
		// allow for instantly finding shapes with the exact same combination and
		// to allow finding values with the same size or color.
		for(int i = start; i < array.length; i++) {
			Piece p = array[i];
			if(p == null) continue;
			
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
			return start;
		}
		
		return one_match_index;
	}
	*/
	private static IntList get_intersections(Piece piece, Piece[] array, QTree tree) {
		List<Integer> list = tree.get_pieces(piece);
		
		IntList result = null;
		Blob blob = piece.blob;
		int s2 = blob.size;
		for(int i : list) {
			if(i > piece.index) continue;
			
			Blob s = array[i].blob;
			int s1 = s.size;
			
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

//		IntList old = get_intersections_old(piece.blob, array, piece.index);
//		result = result == null ? IntList.emptyList():result;
//		if(old.size() != result.size()) {
//			System.out.println("BAD_SIZE: " + piece.blob + ", " + piece.index);
//			System.out.println(result);
//			System.out.println(old);
//		} else {
//			for(int i = 0; i < result.size(); i++) {
//				if(result.get(i) != old.get(i)) {
//					System.out.println("BAD_VALUE: " + piece.blob + ", " + piece.index);
//					System.out.println(result);
//					System.out.println(old);
//					break;
//				}
//			}
//		}
		
		return result == null ? IntList.emptyList():result;
	}
	
	/*
	private static IntList get_intersections_old(Blob blob, Piece[] array, int length) {
		IntList result = null;
		int s2 = blob.size;
		
		for(int i = 1; i < length; i++) {
			Blob s = array[i].blob;
			int s1 = s.size;
			
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
	*/
}
