// <applet code=worms.class width=700 height=500>
// </applet>

import java.applet.Applet;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Random;

/**
 * worms - Moving worms animation.
 * Last updated: January 18, 2000
 *
 * @author Po Shan Cheah
 */
public class worms extends Applet implements Runnable {

    final int maxwormlen = 25;
    final int nworms = 10;
    final int wormradius = 4;
    final int wormshift = 6;
    final double dirchange = 0.2;

    Image offscreen;
    Graphics offscreeng;
    Thread runner;
    int xsize;
    int ysize;
    Worm[] worms;
    Random rand = new Random();

    /**
     * Class for keeping track of a worm.
     */
    class Worm {
	Color color;
	double dir;
	int[] x;
	int[] y;

	/**
	 * Generate a random color.
	 * @return The color.
	 */
	Color randomColor() {
	    return new Color(rand.nextFloat() * 0.5f + 0.5f,
			     rand.nextFloat() * 0.5f + 0.5f,
			     rand.nextFloat() * 0.5f + 0.5f);
	}

	/**
	 * Initialize the worm.
	 * Start it at a random location on the screen.
	 */
	public Worm() {
	    color = randomColor();
	    dir = 0;
	    x = new int[maxwormlen];
	    y = new int[maxwormlen];
	    x[0] = Math.round(rand.nextFloat() * xsize);
	    y[0] = Math.round(rand.nextFloat() * ysize);
	}

	/**
	 * Erase one segment of the worm by drawing it in black.
	 * @param g Graphics context
	 * @param tail Segment to erase
	 */
	public void eraseSegment(Graphics g, int tail) {
	    g.setColor(Color.black);
	    g.drawOval(x[tail] - wormradius,
		       y[tail] - wormradius,
		       2 * wormradius,
		       2 * wormradius);
	}

	/**
	 * Draw one segment of the worm.
	 * @param g Graphics context
	 * @param tail Segment to draw
	 */
	public void drawSegment(Graphics g, int tail) {
	    g.setColor(color);
	    g.drawOval(x[tail] - wormradius,
		       y[tail] - wormradius,
		       2 * wormradius,
		       2 * wormradius);
	}

	/**
	 * Generate a new worm segment. The direction in which the worm
	 * moves will be dir plus or minus dirchange. And the worm will
	 * wrap around the screen if necessary.
	 */
	public void newSegment(int tail, int prevtail) {
	    dir += rand.nextDouble() > 0.5 ? dirchange : -dirchange;
	    x[tail] = wrap(x[prevtail] + 
			   Math.round(wormshift * (float) Math.cos(dir)),
			   xsize);
	    y[tail] = wrap(y[prevtail] + 
			   Math.round(wormshift * (float) Math.sin(dir)), 
			   ysize);
	}

	/**
	 * Adjust x so that it is between 0 and upper.
	 * If x is greater than upper, it will wrap to 0.
	 * If x is less than zero, it will wrap to upper.
	 * @param x value to wrap
	 * @param upper upper bound of the range
	 * @return wrapped value
	 */
	int wrap(int x, int upper) {
	    return x < 0 ? upper : x > upper ? 0 : x;
	}
    }

    /**
     * Initialize the worms.
     */
    public void init() {
	xsize = getSize().width;
	ysize = getSize().height;
	worms = new Worm[nworms];
	for (int i = 0; i < nworms; ++i) 
	    worms[i] = new Worm();
    }

    /**
     * Allocate and reset the offscreen buffer.
     */
    void initOffscreen() {
	offscreen = createImage(xsize, ysize);
	offscreeng = offscreen.getGraphics();
	offscreeng.setColor(Color.black);
	offscreeng.fillRect(0, 0, xsize, ysize);
    }

    /**
     * Start a thread for generating the graphics.
     */
    public void start() {
	if (runner == null) {
	    runner = new Thread(this);
	    runner.start();
	}
    }

    /**
     * Stop the thread and reset the graphic.
     */
    public void stop() {
	if (runner != null) {
	    runner.stop();
	    runner = null;
	}
	offscreen = null;
	offscreeng = null;
    }

    /**
     * Generate the graphics.
     */
    public void run() {
	if (offscreen == null)
	    initOffscreen();

	int tail = 0;
	while (true) {
	    int prevtail = tail;
	    tail = (tail + 1) % maxwormlen;

	    for (int i = 0; i < nworms; ++i) {
		worms[i].eraseSegment(offscreeng, tail);
		worms[i].newSegment(tail, prevtail);
		worms[i].drawSegment(offscreeng, tail);
	    }

	    repaint();

	    // Sleep for a split-second.
	    try { 
		Thread.sleep(200); 
	    }
	    catch (InterruptedException e) { }
	}
    }

    /**
     * Define our own update method so that the AWT won't clear the
     * window between updates. This way, we can prevent flicker.
     */
    public void update(Graphics g) {
	paint(g);
    }

    /**
     * Paint the canvas from our offscreen buffer.
     */
    public void paint(Graphics g) {
	if (offscreen == null)
	    initOffscreen();
	g.drawImage(offscreen, 0, 0, this);
    }

    public String getAppletInfo() {
	return "worms - Moving worms animation.";
    }

    public static void main(String args[]) {
	final worms h = new worms();
	final Frame f = new Frame("worms");

	// Need this so the user can close the window.
	f.addWindowListener(
	    new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    h.stop();
		    f.setVisible(false);
		    f.dispose();
		    System.exit(0);
		}
		// Need this so that our offscreen buffer can be
		// allocated with the correct width and height that
		// is only known after the window has been opened.
		public void windowOpened(WindowEvent e) {
		    h.init();
		    h.start();
		}
	    }
	);
	
	f.add("Center", h);
	f.setSize(700, 500);
	f.show();
    }
}

// The End
