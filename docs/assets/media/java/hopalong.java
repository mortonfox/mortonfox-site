// <applet code=hopalong.class width=500 height=700>
// </applet>

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.text.DecimalFormat;
import java.util.Random;

/**
 * hopalong - Produces graphics using the hopalong attractor.
 * Last updated: January 21, 2000
 *
 * @author Po Shan Cheah
 */

public class hopalong extends Applet {
    double p;
    double q;
    double r;
    final HopalongCanvas hcanvas = new HopalongCanvas();
    Frame mainwin;
    Random rand = new Random();

    /**
    * Common code for all our warning and message boxes.
    */
    class MyDialog extends Dialog {

	Button okbutton;

	/**
	 * Initialize the dialog.
	 * @param f Parent frame.
	 * @param title Dialog title.
	 * @param message Dialog message text.
	 */
	public MyDialog(Frame f, String title, String message) {
	    super(f, title, true);

	    // Need this so the user can close the window.
	    addWindowListener(
		new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			removeDialog();
		    }
		}
	    );

	    okbutton = new Button("Ok");
	    okbutton.addActionListener(
		new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			removeDialog();
		    }
		}
	    );

	    Panel p = new Panel();
	    Panel p2 = new Panel();

	    p.add(new Label(message));
	    p2.add(okbutton);

	    add("Center", p);
	    add("South", p2);
	    setSize(400, 100);
	    this.show();
	}

	void removeDialog() {
	    setVisible(false);
	    dispose();
	}
    } // MyDialog

    /**
     * This is our special canvas for the graphics area of the window.
     */
    class HopalongCanvas extends Canvas implements Runnable {

	Image offscreen;
	Graphics offscreeng;
	int xsize;
	int ysize;
	int xmid;
	int ymid;
	Thread runner;
	final double multiplier = 10 / Math.sqrt(2);

	public HopalongCanvas() {
	}

	/**
	 * Initialize the offscreen buffer based on the canvas size.
	 */
	void initOffscreen() {
	    xsize = getSize().width;
	    ysize = getSize().height;
	    xmid = xsize / 2;
	    ymid = ysize / 2;
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
	 * Generate a random color.
	 * @return The color.
	 */
	Color randomColor() {
	    return new Color(rand.nextFloat() * 0.5f + 0.5f,
			     rand.nextFloat() * 0.5f + 0.5f,
			     rand.nextFloat() * 0.5f + 0.5f);
	}

	/**
	 * Generate the graphics.
	 */
	public void run() {
	    if (offscreen == null)
		initOffscreen();
	    int count = 0;
	    double x = 0;
	    double y = 0;
	    offscreeng.setColor(Color.white);
	    while (true) {
		if (count % 500 == 0) {
		    repaint();
		    offscreeng.setColor(randomColor());
		    // This is needed for non-preemptive scheduling.
		    // Always a good idea to sleep occasionally anyway.
		    // Also, the sleep() method is better than yield() for
		    // single-processor non-preemptive because it actually
		    // lets the other threads run for a non-trivial
		    // amount of time.
		    try {
			Thread.sleep(10);
		    }
		    catch (InterruptedException e) { }
		}
		int sign = x < 0 ? -1 : 1;
		double x1 = y - sign * Math.sqrt(Math.abs(q * x - r));
		y = p - x;
		x = x1;

		offscreeng.fillRect(
		    (int) (Math.round((x + y) * multiplier) + xmid), 
		    (int) (Math.round((y - x) * multiplier) + ymid), 
		    1, 1);

		++count;
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
    } // HopalongCanvas

    public void init() {
	// Find the frame containing this applet.
	Component c = getParent();
	while (c != null && !(c instanceof Frame))
	    c = c.getParent();
	mainwin = (Frame) c;
	    
	setLayout(new BorderLayout());

	Panel p1 = new Panel();
	
	Label l1 = new Label("Try values between 0 and 2. P:");
	l1.setAlignment(Label.RIGHT);
	p1.add(l1);
	final TextField pfield = new TextField("0.5", 8);
	p1.add(pfield);

	Label l2 = new Label("Q:");
	l2.setAlignment(Label.RIGHT);
	p1.add(l2);
	final TextField qfield = new TextField("0.5", 8);
	p1.add(qfield);

	Label l3 = new Label("R:");
	l3.setAlignment(Label.RIGHT);
	p1.add(l3);
	final TextField rfield = new TextField("0.5", 8);
	p1.add(rfield);

	Panel p2 = new Panel();
	Button gobutton = new Button("Go");
	p2.add(gobutton);

	// The Go button gets the values from the textfields and
	// starts the hopalong.
	gobutton.addActionListener(
	    new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			p = Double.valueOf(pfield.getText()).doubleValue();
			q = Double.valueOf(qfield.getText()).doubleValue();
			r = Double.valueOf(rfield.getText()).doubleValue();
			hcanvas.stop();
			hcanvas.start();
		    }
		    catch (NumberFormatException exc) {
			new MyDialog(mainwin, "Invalid numbers",
				     "Invalid numeric value(s) entered.");
		    }
		}
	    }
	);

	Button randombutton = new Button("Random");
	p2.add(randombutton);

	// The Random button puts random values into the textfields.
	randombutton.addActionListener(
	    new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    DecimalFormat df = new DecimalFormat("#.####");
		    pfield.setText(df.format(rand.nextDouble() + 0.3));
		    qfield.setText(df.format(rand.nextDouble() + 0.3));
		    rfield.setText(df.format(rand.nextDouble() + 0.3));
		}
	    }
	);

	Panel p = new Panel();
	p.setLayout(new GridLayout(2, 1, 0, 0));
	p.add(p1);
	p.add(p2);

	add("North", p);
	add("Center", hcanvas);
    }

    public void start() {
    }

    public void stop() {
	hcanvas.stop();
    }

    public String getAppletInfo() {
	return "hopalong - Produces graphics using the hopalong attractor.";
    }

    public static void main(String args[]) {
	final hopalong h = new hopalong();
	final Frame f = new Frame("hopalong");

	// Need this so the user can close the window.
	f.addWindowListener(
	    new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    h.hcanvas.stop();
		    f.setVisible(false);
		    f.dispose();
		    System.exit(0);
		}
	    }
	);
	
	f.add("Center", h);
	h.init();
	h.start();
	f.setSize(600, 500);
	f.show();
    }
} // hopalong

// The End
