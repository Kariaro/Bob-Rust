package com.bobrust.sorter;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

import javax.swing.JFrame;

import com.bobrust.reader.BorstData;
import com.bobrust.reader.BorstReader;
import com.bobrust.reader.BorstShape;

/**
 * Example of how to sort a borst file.
 * 
 * @author HardCoded
 */
public class BorstSortingExampleWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		new BorstSortingExampleWindow();
	}
	
	public BorstSortingExampleWindow() {
		setSize(512 * 2 + 1, 512 + 29);
		setResizable(false);
		setTitle("Example sorting");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				repaint();
			}
			
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_U) {
					refreshBorst();
				}
				repaint();
			}
		});
		setIgnoreRepaint(true);
		
		refreshBorst();
		repaint();
	}
	
	private int shapes = 8000;
	
	private void refreshBorst() {
		try {
			BorstData borst = BorstReader.readFile(new File("res/borst/aidan8000.borst"));
			
			list = new BlobList();
			
			int max = shapes;
			for(BorstShape shape : borst.instructions) {
				if(--max < 0) break;
				list.add(Blob.get(
					shape.x, shape.y,
					BlobList.SIZES[shape.size],
					shape.color
				));
			}
			
			list = BlobList.populate(20000);
			shapes = list.size();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		Thread thread = new Thread(() -> {
		try {
			long start0 = System.nanoTime();
			BlobList sorted0 = FastBlobSorterOld.sort(list);
			long time0 = System.nanoTime() - start0;
			
			long start1 = System.nanoTime();
			BlobList sorted1 = FastBlobSorter.sort(list);
			long time1 = System.nanoTime() - start1;
			
			this.sorted0 = sorted0;
			this.sorted1 = sorted1;
			
			{
				int a0 = score(list, shapes);
				int a1 = score(sorted1, shapes);
				int a2 = score(sorted0, shapes);
				int ms = 50;
				
				System.out.println("=============================================================================");
				System.out.printf("Timings: Basic: %.2f ms, Fast: %.2f ms\n", time0 / 1000000.0, time1 / 1000000.0);
				System.out.printf("Shapes: %s\n", shapes);
				System.out.printf("Original: score=%s,\t~(%s)\n", a0, timeStr((a0 + shapes) * ms));
				System.out.printf("Sorted  : score=%s,\t~(%s)\n", a1, timeStr((a1 + shapes) * ms));
				System.out.printf("Old     : score=%s,\t~(%s)\n", a2, timeStr((a2 + shapes) * ms));
				System.out.printf("Saved   : ~(%s)\n", timeStr(Math.abs(a1 - a0) * ms));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		});
		thread.setDaemon(true);
		thread.start();
		
		try {
			thread.join();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static final Color BACKGROUND = new Color(0xebeace);
	private BlobPainter render = new BlobPainter();
	private BlobList sorted0;
	private BlobList sorted1;
	private BlobList list;
	
	public String timeStr(int millis) {
		int minutes = (int)(millis / 60000);
		float seconds = (int)(millis % 60000) / 1000.0f;
		
		if(minutes > 0) {
			return String.format("%sm %5.2fs", minutes, seconds);
		}
		
		return String.format("%5.2fs", seconds);
	}
	
	@Override
	public void paint(Graphics gr) {
		Graphics2D g = (Graphics2D)gr;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		//g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		//g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		shapes = 8000;
		g.translate(0, 28);
		BlobList data = list;
		if(data == null) return;
		
		BlobList list0 = sorted0;
		if(list0 != null) render.draw(g, shapes, BACKGROUND, list0);
		g.translate(512, 0);
		
		BlobList list1 = sorted1;
		if(list1 != null) render.draw(g, shapes, BACKGROUND, list1);
	}
	
	public int score(BlobList data) {
		if(data == null) return 0;
		return score(data.list(), data.size());
	}
	
	public int score(BlobList data, int shapes) {
		if(data == null) return 0;
		return score(data.list(), shapes);
	}
	
	public int score(List<Blob> list, int shapes) {
		Blob last = null;
		
		int index = 0;
		int changes = 2;
		for(Blob blob : list) {
			if(index++ > shapes) break;
			if(last != null) {
				if(last.size != blob.size) changes++;
				if(last.color != blob.color) changes++;
			}
			
			last = blob;
		}
		
		return changes;
	}
}
