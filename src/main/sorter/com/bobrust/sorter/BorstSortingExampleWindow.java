package com.bobrust.sorter;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
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
	
	private BufferStrategy bs;
	private boolean do_render = true;
	private Thread render_thread = new Thread(new Runnable() {
		boolean hasThread = false;
		
		public void run() {
			while(true) {
				try {
					Thread.sleep(10);
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				if(bs != null) {
					bs.show();
				}
				
				if(do_render && !hasThread) {
					hasThread = true;
					Thread thread = new Thread(() -> {
						render();
						hasThread = false;
					});
					thread.setDaemon(true);
					thread.start();
					do_render = false;
				}
			}
		}
	});
	
	public BorstSortingExampleWindow() {
		setSize(512 * 2 + 1, 512 + 29);
		setResizable(false);
		setTitle("Example painting");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_U) {
					refreshBorst();
				}
				
				do_render = true;
			}
		});
		setIgnoreRepaint(true);
		
		refreshBorst();
		repaint();
		
		render_thread.setDaemon(true);
		render_thread.start();
	}
	
	private void refreshBorst() {
		try {
			BorstData borst = BorstReader.readFile(new File("res/borst/monalisarust8000.borst"));
			
			list = new BlobList();
			
			for(BorstShape shape : borst.instructions) {
				list.add(Blob.get(
					shape.x, shape.y,
					BlobList.SIZES[shape.size],
					shape.color
				));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private BlobList list;
	private BlobPainter render = new BlobPainter();
	private static final Color BACKGROUND = new Color(0xebeace);
	
	public String timeStr(int millis) {
		int minutes = (int)(millis / 60000);
		float seconds = (int)(millis % 60000) / 1000.0f;
		
		if(minutes > 0) {
			return String.format("%sm %5.2fs", minutes, seconds);
		}
		
		return String.format("%5.2fs", seconds);
	}
	
	@Override
	public void paint(Graphics g) {
		if(bs == null) {
			createBufferStrategy(1);
			bs = getBufferStrategy();
			return;
		}
	}
	
	@Override
	public void paintComponents(Graphics g) {
		
	}
	
	@Override
	public void paintAll(Graphics g) {
		
	}
	
	protected void render() {
		Graphics2D g = (Graphics2D)bs.getDrawGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		//g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		//g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		g.translate(0, 28);
		BlobList data = list;
		if(data == null) return;
		
		int shapes = 6000;
		render.draw(g, shapes, BACKGROUND, data);
		g.translate(512, 0);
		
		long start0 = System.nanoTime();
		BlobList sorted0 = FastBlobSorterV2.sort(data);
		long time0 = System.nanoTime() - start0;
		
		long start1 = System.nanoTime();
		BlobList sorted1 = FastBlobSorter.sort(data);
		long time1 = System.nanoTime() - start1;
		
		BlobList sorted = sorted1;
		render.draw(g, shapes, BACKGROUND, sorted);
		
		{
			int a0 = score(data, shapes);
			int a1 = score(sorted, shapes);
			int a2 = score(sorted0, shapes);
			int ms = 50;
			
			System.out.printf("Timings: Basic: %.2f ms, Fast: %.2f ms\n", time0 / 1000000.0, time1 / 1000000.0);
			System.out.printf("Shapes: %s\n", shapes);
			System.out.printf("Original: score=%s,\t~(%s)\n", a0, timeStr((a0 + shapes) * ms));
			System.out.printf("Sorted  : score=%s,\t~(%s)\n", a1, timeStr((a1 + shapes) * ms));
			System.out.printf("Old     : score=%s,\t~(%s)\n", a2, timeStr((a2 + shapes) * ms));
			System.out.printf("Saved   : ~(%s)\n", timeStr(Math.abs(a1 - a0) * ms));
		}
	}
	
	public int score(BlobList data) {
		return score(data.list(), data.size());
	}
	
	public int score(BlobList data, int shapes) {
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
	
	public void repaint() {
		
	}
}
