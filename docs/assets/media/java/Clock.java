// <applet code=clock.class height=500 width=500>
// <param name=foreground value="0xFFFFFF">
// <param name=background value="0x000000">
// </applet>

import java.applet.Applet;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Image;
import java.util.Calendar;

/**
 * clock - Applet that displays an analog clock.
 * Last updated: January 10, 2000
 *
 * @author Po Shan Cheah
 * @version 0.1
 */

public class clock extends Applet implements Runnable {

    int xsize;
    int ysize;
    int xmid;
    int ymid;
    int clockradius;
    Thread runner;

    /**
     * The offscreen buffer.
     */
    Image offscreen;

    /**
     * Graphics context for the offscreen buffer.
     */
    Graphics offscreeng;

    /**
     * The last time the clock hands were drawn.
     */
    int oldhour;
    int oldmin;
    int oldsec;

    /**
     * Background color of clock. Can be set in applet parameters.
     */
    Color background = Color.blue;
    /**
     * Foreground color of clock. Can be set in applet parameters.
     */
    Color foreground = Color.yellow;

    public String getAppletInfo() {
	return "clock - Displays an analog clock. By Po Shan Cheah (c)2000";
    }

    public String[][] getParameterInfo() {
	String pinfo[][] = {
	    { "foreground", "0x000000-0xFFFFFF", "foreground color RGB" },
	    { "background", "0x000000-0xFFFFFF", "background color RGB" }
	};
	return pinfo;
    }

    /** 
     * Recalculate some length values.
     * Called if the window has been resized.
     */
    void recalcLengths() {
	xsize = getSize().width;
	ysize = getSize().height;

	xmid = xsize / 2;
	ymid = ysize / 2;

	clockradius = (int) (Math.min(xmid, ymid) * 0.95);
    }

    /**
     * Polar to Rectangular conversion.
     * @return The X coordinate.
     */
    int polarx(double dist, double angle) {
	return xmid + (int) Math.round(dist * Math.sin(angle));
    }

    /**
     * Polar to Rectangular conversion.
     * @return The Y coordinate.
     */
    int polary(double dist, double angle) {
	return ymid - (int) Math.round(dist * Math.cos(angle));
    }

    public void init() {
	addComponentListener(
	    new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
		    // Invalidate offscreen buffer so that the next
		    // update will create a new one of the correct size.
		    if (offscreen != null)
			offscreen.flush();
		    offscreen = null;
		    if (offscreeng != null)
			offscreeng.dispose();
		    offscreeng = null;
		}
	    }
	);
	try {
	    Color fgcolor = Color.decode(getParameter("foreground"));
	    Color bgcolor = Color.decode(getParameter("background"));
	    foreground = fgcolor;
	    background = bgcolor;
	}
	catch (Exception e) {
	    // Catch NumberFormatException or possibly 
	    // NullPointerException.
	}
    }

    public void start() {
	if (runner == null) {
	    runner = new Thread(this);
	    runner.start();
	}
    }

    public void stop() {
	if (runner != null) {
	    runner.stop();
	    runner = null;
	}
    }

    public void run() {
	while (true) {
	    repaint();
	    // Sleep for one second.
	    try { 
		Thread.sleep(1000); 
	    }
	    catch (InterruptedException e) { }
	}
    }

    public void update(Graphics g) {
	// Define our own update method so that the AWT won't clear the
	// window between updates. This way, we can prevent flicker.
	paint(g);
    }

    /**
     * Displays text centered on a specific coordinate. Takes into
     * account the text width, ascent and height.
     *
     * @param g Graphics context.
     * @param text Text to display.
     * @param x X-coordinate at which the text should be centered.
     * @param y Y-coordinate at which the text should be centered.
     */
    void centerText(Graphics g, String text, int x, int y) {
	FontMetrics fm = g.getFontMetrics();

	int locy = y + fm.getAscent() - fm.getHeight() / 2;
	int locx = x - fm.stringWidth(text) / 2;

	g.drawString(text, locx, locy);
    }

    /**
     * Draw a clock hand.
     *
     * @param g
     * Graphics context.
     *
     * @param length
     * Length of the hand from the clock center.
     *
     * @param width
     * Width of the clock hand at the center.
     *
     * @param bkwidth
     * Length of the clock hand on the other side of the center.
     *
     * @param angle
     * Angle at which the clock hand is pointing.
     */
    void drawhand(Graphics g,
			 int length, int width, int bkwidth, double angle) {
	Polygon p = new Polygon();
	p.addPoint(polarx(-bkwidth, angle), polary(-bkwidth, angle));
	p.addPoint(polarx(width, angle + Math.PI / 2),
		   polary(width, angle + Math.PI / 2));
	p.addPoint(polarx(length, angle), polary(length, angle));
	p.addPoint(polarx(width, angle - Math.PI / 2),
		   polary(width, angle - Math.PI / 2));
	g.drawPolygon(p);
    }

    /**
     * Draw the three clock hands.
     *
     * @param g
     * Graphics context.
     *
     * @param hour
     * The hours part of the time.
     *
     * @param min
     * The minutes part of the time.
     *
     * @param sec
     * The seconds part of the time.
     */
    void drawhands(Graphics g, int hour, int min, int sec) {
	drawhand(g, 
		 (int) (0.6 * clockradius), 
		 (int) (0.075 * clockradius), 
		 (int) (0.1 * clockradius), 
		 (hour + min / 60.0) * 2 * Math.PI / 12);
	drawhand(g, 
		 (int) (0.9 * clockradius), 
		 (int) (0.075 * clockradius), 
		 (int) (0.15 * clockradius), 
		 (min + sec / 60.0) * 2 * Math.PI / 60);
	drawhand(g, 
		 (int) (0.9 * clockradius), 
		 (int) (0.025 * clockradius), 
		 (int) (0.15 * clockradius), 
		 sec * 2 * Math.PI / 60);
    }

    public void paint(Graphics g) {
	// double-buffering
	if (offscreen == null) {
	    // No offscreen buffer or offscreen buffer has been
	    // invalidated. Just create a new one and repaint
	    // the whole clock face.
	    recalcLengths();
	    offscreen = createImage(xsize, ysize);
	    offscreeng = offscreen.getGraphics();
	    paintClock(offscreeng);
	}
	else {
	    // Once the offscreen buffer has been established, we
	    // can just remove the old clock hands and paint the new
	    // clock hands, leaving the rest of the clock alone.
	    updateClock(offscreeng);
	}
	g.drawImage(offscreen, 0, 0, this);
    }

    /**
     * Paint the face of the clock.
     *
     * @param g Graphics context.
     */
    void paintClock(Graphics g) {
	g.setColor(background);
	g.fillRect(0, 0, xsize, ysize);
	g.setColor(foreground);
	
	int fontsize = clockradius / 7;
	if (fontsize < 1)
	    fontsize = 1;
	g.setFont(new Font("SansSerif", Font.PLAIN, fontsize));

	for (int i = 0; i < 360; i += 6) {
	    double rad = i * Math.PI / 180;

	    if (i % 30 == 0) {
		g.drawLine(polarx(clockradius, rad), 
			   polary(clockradius, rad),
			   polarx(0.925 * clockradius, rad), 
			   polary(0.925 * clockradius, rad));
		
		int hour = i / 30;
		if (i == 0) {
		    hour = 12;
		}

		centerText(g, Integer.toString(hour),
			   polarx(0.8 * clockradius, rad),
			   polary(0.8 * clockradius, rad));
	    }
	    else {
		g.drawLine(polarx(clockradius, rad), 
			   polary(clockradius, rad),
			   polarx(0.975 * clockradius, rad), 
			   polary(0.975 * clockradius, rad));
	    }
	}

	// This XOR color will flip background and foreground pixels.
	// g.setXORMode(new Color(background.getRGB() ^ foreground.getRGB()));
	g.setXORMode(foreground);
	g.setColor(background);

	Calendar cal = Calendar.getInstance();
	oldhour = cal.get(Calendar.HOUR_OF_DAY);
	oldmin = cal.get(Calendar.MINUTE);
	oldsec = cal.get(Calendar.SECOND);
	// Add new hands in XOR mode.
	drawhands(g, oldhour % 12, oldmin, oldsec);
    }

    /**
     * Update only the hands of the clock. This method can be used if
     * the face of the clock has already been drawn. To save time, it
     * does not clear the graphics context before drawing. Instead it
     * draws over the old hands and then draws the new hands in XOR mode.
     *
     * @param g Graphics context.
     */
    void updateClock(Graphics g) {
	// Remove old hands in XOR mode.
	drawhands(g, oldhour % 12, oldmin, oldsec);
	Calendar cal = Calendar.getInstance();
	oldhour = cal.get(Calendar.HOUR_OF_DAY);
	oldmin = cal.get(Calendar.MINUTE);
	oldsec = cal.get(Calendar.SECOND);
	// Add new hands in XOR mode.
	drawhands(g, oldhour % 12, oldmin, oldsec);
    }

    public static void main(String args[]) {
	clock clock = new clock();
	clock.init();
	clock.start();

	final Frame f = new Frame("Clock");

	f.addWindowListener(
	    new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    f.setVisible(false);
		    f.dispose();
		    System.exit(0);
		}
	    }
	);
	
	f.add("Center", clock);
	f.setSize(600, 500);
	f.show();
    }
}

// The End
