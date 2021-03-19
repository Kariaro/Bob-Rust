package com.bobrust.sorter;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.bobrust.reader.BorstData;
import com.bobrust.reader.BorstReader;
import com.bobrust.reader.BorstShape;

/**
 * Example of how to sort a borst file.
 * 
 * @author HardCoded
 */
public class BorstSortingExampleWindow extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		BorstSortingExampleWindow window = new BorstSortingExampleWindow();
		
		JFrame frame = new JFrame("Example sorting");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setIgnoreRepaint(true);
		frame.add(window);
		frame.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				frame.repaint();
			}
			
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_U) {
					window.refreshBorst();
				}
				frame.repaint();
			}
		});
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		window.start();
	}
	
	public BorstSortingExampleWindow() {
		Dimension dim = new Dimension(512 * 2 + 1, 512);
		setPreferredSize(dim);
		setMaximumSize(dim);
		setMinimumSize(dim);
	}
	
	public void start() {
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
		
		/*
		{
			int start = 1;
			int end = 50 + start;
			double[] speed = new double[end - start];
			for(int i = start; i < end; i++) {
				BlobList list = BlobList.populate(i * 100);
				System.out.println(i);
				
				long start0 = System.nanoTime();
				FastBlobSorterV2.sort(list);
				long time0 = System.nanoTime() - start0;
				
				speed[i - start] = time0 / 1000000.0;
			}
			
			String str = Arrays.toString(speed);
			str = str.replace("[", "\\left[");
			str = str.replace("]", "\\right]");
			System.out.println(str);
			
			long start0 = System.nanoTime();
			FastBlobSorterV2.sort(BlobList.populate(10000));
			long time0 = System.nanoTime() - start0;
			
			System.out.printf("Time 0: %.2f ms\n", time0 / 1000000.0);
			System.exit(0);
		}
		*/
		
		
		/*
		 * This Thread is solely used to make debugging with visualvm easer.
		 * 
		 * The default call stack within AWT is usually 20 or 30 calls deep.
		 * By creating a new thread the call stack will only be 5 calls deep.
		 */
		Thread thread = new Thread(() -> {
			try {
				long start0 = System.nanoTime();
				BlobList sorted0 = FastBlobSorterExperimental.sort(list);
				long time0 = System.nanoTime() - start0;
				
				long start1 = System.nanoTime();
				BlobList sorted1 = FastBlobSorterExperimental.sort(list);
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
	protected BlobList sorted0;
	protected BlobList sorted1;
	protected BlobList list;
	
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
		
		shapes = 100000;
		g.translate(0, 0);
		g.setColor(Color.black);
		g.fillRect(-10, -10, 512 * 2 + 3 + 100, 514 + 100);
		
		BlobList data = list;
		if(data == null) return;
		
		BlobList list0 = data;
		if(list0 != null) render.draw(g, shapes, BACKGROUND, list0);
		g.translate(513, 0);
		
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
