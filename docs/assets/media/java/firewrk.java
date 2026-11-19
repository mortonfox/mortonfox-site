// <applet code=firewrk.class width=700 height=500>
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
 * firewrk - Fireworks graphics.
 * Last updated: January 21, 2000
 *
 * @author Po Shan Cheah
 */
public class firewrk extends Applet implements Runnable {

    /**
     * Maximum number of fireworks in the air at a time.
     */
    final int maxfw = 10;
    /**
     * Maximum number of rays per firework.
     */
    final int maxrays = 10;

    /**
     * Number of active fireworks.
     */
    int nactive = 0;

    Image offscreen;
    Graphics offscreeng;
    Thread runner;
    int xsize;
    int ysize;
    Random rand = new Random();
    FireWorkRec[] fw;

    /**
     * Class for keeping track of each of the fireworks.
     */
    class FireWorkRec {
	/**
	 * start, stop, len, and curpos are four variables that control
	 * the progress of the fireworks. It begins at start and ends at
	 * stop and the fireworks ray is kept at a maximum length of len
	 * units. curpos is the current position in the iteration.
	 */
	double start;
	double stop;
	double len;
	double curpos;

	Color color;

	/**
	 * Number of rays in this fireworks.
	 */
	int nrays;

	/**
	 * cx and cy are the center of the fireworks.
	 */
	double cx;
	double cy;

	/**
	 * Cached values of the sin and cos of the angle of each ray.
	 */
	double[] sintab;
	double[] costab;

	/**
	 * This determines how much faster each fireworks ray falls
	 * because of gravity. It is generated at random for each
	 * of the fireworks.
	 */
	double descent;

	/**
	 * Is this fireworks active?
	 */
	boolean active;

	boolean isactive() {
	    return active;
	}

	public FireWorkRec() {
	    sintab = new double[maxrays];
	    costab = new double[maxrays];
	}

	/**
	 * Step to the next pixel in the fireworks.
	 * We increment by 0.5 instead of 1 to make it smoother.
	 */
	public void update() {
	    curpos += 0.5;
	    if (curpos > stop + len)
		makeinactive();
	    else
		advance();
	}

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
	 * Advance the fireworks graphics by one step.
	 * Draw a new pixel unless it is beyond the stop value.
	 * Erase a pixel if it is beyond the start value minus the length.
	 * This keeps up to len units on the screen.
	 */
	void advance() {
	    for (int i = 0; i < nrays; ++i) {
		if (curpos <= stop) {
		    offscreeng.setColor(color);
		    double quad = descent * curpos;
		    // The quad squared component simulates some
		    // additional downward movement due to gravity.
		    offscreeng.fillRect((int) (cx + curpos * costab[i]),
					(int) (cy + curpos * sintab[i] +
					       quad * quad),
					1, 1);
		}

		if (curpos >= start + len) {
		    offscreeng.setColor(Color.black);
		    double quad = descent * (curpos - len);
		    offscreeng.fillRect((int) (cx + 
					       (curpos - len) * costab[i]),
					(int) (cy + 
					       (curpos - len) * sintab[i] +
					       quad * quad),
					1, 1);
		}
	    }
	}

	/**
	 * Deactivate this fireworks record.
	 */
	void makeinactive() {
	    active = false;
	    --nactive;
	}

	/**
	 * Initialize this fireworks record and mark it active.
	 */
	public void init() {
	    active = true;
	    ++nactive;

	    cx = Math.floor(rand.nextDouble() * xsize);
	    cy = Math.floor(rand.nextDouble() * ysize);
	    descent = rand.nextDouble() * 0.1 + 0.05;
	    
	    start = Math.floor(rand.nextDouble() * 10) + 5;
	    stop = Math.floor(rand.nextDouble() * 50) + 50;
	    len = Math.floor(rand.nextDouble() * 50) + 25;
	    curpos = start;
	    color = randomColor();
	    nrays = (int) (rand.nextDouble() * 6 + 5);

	    double angleinc = 2 * Math.PI / nrays;
	    double angle = rand.nextDouble() * angleinc;

	    for (int i = 0; i < maxrays; ++i, angle += angleinc) {
		costab[i] = Math.cos(angle);
		sintab[i] = Math.sin(angle);
	    }
	}
    }
    
    public String getAppletInfo() {
	return "Fireworks graphics";
    }

    /**
     * Initialize the fireworks.
     */
    public void init() {
	xsize = getSize().width;
	ysize = getSize().height;
	fw = new FireWorkRec[maxfw];
	for (int i = 0; i < maxfw; ++i)
	    fw[i] = new FireWorkRec();
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
     * Find an inactive fireworks and initialize it.
     */
    void makenew() {
	for (int i = 0; i < maxfw; ++i)
	    if (! fw[i].isactive()) {
		fw[i].init();
		break;
	    }
    }

    /**
     * Update all the active fireworks.
     * Start a new fireworks if we have less than the maximum 
     * one in ten times.
     */
    void cycle() {
	for (int i = 0; i < maxfw; ++i) {
	    if (fw[i].isactive()) 
		fw[i].update();
	}
	if (nactive < maxfw && rand.nextDouble() < 0.1)
	    makenew();
    }

    /**
     * Generate the graphics.
     */
    public void run() {
	if (offscreen == null)
	    initOffscreen();

	while (true) {
	    cycle();
	    repaint();

	    // Sleep for a split-second.
	    try { 
		Thread.sleep(25); 
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

    public static void main(String args[]) {
	final firewrk h = new firewrk();
	final Frame f = new Frame("Fireworks");

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
