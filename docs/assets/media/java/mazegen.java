// <applet code=mazegen.class width=500 height=500>
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
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;

/**
 * mazegen - Generates mazes.
 * Last updated: June 27, 2000
 *
 * @author Po Shan Cheah
 */
public class mazegen extends Applet {

    /**
     * Window containing the applet.
     */
    Frame mainwin;

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
	MyDialog(Frame f, String title, String message) {
	    super(f, title, true);

	    // Need this so the user can close the window.
	    addWindowListener(
		new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			removeDialog();
		    }
		}
	    );

	    // Another way to close the window is to click on the Ok button.
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
     * Canvas on which the maze will be drawn. Holds the maze image in
     * an offscreen buffer so that it can be redrawn and refreshed.
     */
    class MazeCanvas extends Canvas {
	/**
	 * Maze encoding scheme.
	 */
	final int UP = 1;
	final int LEFT = 2;
	final int RIGHT = 4;
	final int DOWN = 8;

	/**
	 * The maze array. Will be reallocated every time generate() is
	 * called.
	 */
	int maze[][];

	/**
	 * Number of rows in the maze.
	 */
	int rows;
	/**
	 * Number of columns in the maze.
	 */
	int cols;
	/**
	 * Number of cells remaining to be filled.
	 */
	int unused;

	Image offscreen;
	Graphics offscreeng;

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
		return;
    	    g.drawImage(offscreen, 0, 0, this);
	}

	/**
	 * Allocate and initialize the maze array.
	 */
	void initmaze() {
	    maze = new int[cols][rows];

	    for (int i = 0; i < cols; ++i)
		for (int j = 0; j < rows; ++j)
		    maze[i][j] = 0;

	    // Corners
	    maze[0][0] = DOWN | RIGHT;
	    maze[cols-1][0] = DOWN | LEFT;
	    maze[0][rows-1] = UP | RIGHT;
	    maze[cols-1][rows-1] = UP | LEFT;

	    // Sides
	    for (int i = 1; i < rows - 1; ++i) {
		maze[0][i] = DOWN | UP;
		maze[cols-1][i] = DOWN | UP;
	    }
	    for (int i = 1; i < cols - 1; ++i) {
		maze[i][0] = LEFT | RIGHT;
		maze[i][rows-1] = LEFT | RIGHT;
	    }

	    // Openings
	    maze[cols/2][0] = UP | LEFT;
	    maze[cols/2 + 1][0] = UP | RIGHT;
	    maze[cols/2][rows-1] = DOWN | LEFT;
	    maze[cols/2 + 1][rows-1] = DOWN | RIGHT;

	    unused = (cols - 2) * (rows - 2);
	} // initmaze

	/**
	 * Draw a vertical line.
	 */
	void vline(int col, int row1, int row2) {
	    // System.out.println("Line: (" + col + "," + row1 + ") to (" +
			       // col + "," + row2 + ")");
	    offscreeng.drawLine(col, row1, col, row2);
	}

	/**
	 * Draw a horizontal line.
	 */
	void hline(int col1, int col2, int row) {
	    // System.out.println("Line: (" + col1 + "," + row + ") to (" +
			       // col2 + "," + row + ")");
	    offscreeng.drawLine(col1, row, col2, row);
	}

	/**
	 * Draw the maze on the offscreen image buffer.
	 */
	void displaymaze() {
	    final int CELLSIZE = 8;

	    int xsize = cols * CELLSIZE;
	    int ysize = rows * CELLSIZE;

	    // We want the image buffer to be at least as large as the
	    // canvas so that it will clear out the previous maze. The
	    // image buffer can be larger than the canvas though and in
	    // that case, the user can resize the window (in non-applet
	    // mode, of course) to see the rest of the maze.

	    if (getSize().width > xsize) 
		xsize = getSize().width;
	    if (getSize().height > ysize) 
		ysize = getSize().height;

	    offscreen = createImage(xsize, ysize);
	    offscreeng = offscreen.getGraphics();
	    offscreeng.setColor(Color.white);
	    offscreeng.fillRect(0, 0, xsize, ysize);
	    offscreeng.setColor(Color.black);

	    for (int i = 0; i < cols; ++i)
		for (int j = 0; j < rows; ++j) {
		    int room = maze[i][j];
		    if ((room & UP) != 0)
			vline(i * CELLSIZE + CELLSIZE/2,
			      j * CELLSIZE,
			      j * CELLSIZE + CELLSIZE/2 - 1);
		    if ((room & DOWN) != 0)
			vline(i * CELLSIZE + CELLSIZE/2,
			      j * CELLSIZE + CELLSIZE/2,
			      j * CELLSIZE + CELLSIZE - 1);
		    if ((room & RIGHT) != 0)
			hline(i * CELLSIZE + CELLSIZE/2,
			      i * CELLSIZE + CELLSIZE - 1,
			      j * CELLSIZE + CELLSIZE/2);
		    if ((room & LEFT) != 0)
			hline(i * CELLSIZE,
			      i * CELLSIZE + CELLSIZE/2 - 1,
			      j * CELLSIZE + CELLSIZE/2);
		}
	    repaint();
	} // displaymaze

	/**
	 * Generate a random maze.
	 *
	 * We start at an empty cell in the maze. We create a wall by doing
	 * a random walk through the maze. When we hit an existing wall, we
	 * stop. 
	 *
	 * Theory: Since the walk started on an empty cell, that path can
	 * never bisect the maze and so there should still be a solution to
	 * the maze after we add each wall.
	 */
	void generatemaze() {
	    /**
	     * Maximum number of steps to save so that the maze crawler
	     * does not close back on itself. Low values may affect the
	     * quality of the maze.
	     */
	    final int nsave = 20;

	    /**
	     * Array keeping track of the directions the random walk took
	     * in the past nsave steps.
	     */
	    int prevndir[] = new int[nsave];

	    /**
	     * Table stating what we should add to the current row
	     * and column to move in that direction.
	     */
	    int movetable[][] =
		{{0, -1}, {-1, 0}, {1, 0}, {0, 1}};
	    
	    /**
	     * The location of the random walker.
	     */
	    int curx, cury;

	    while (unused > 0) {
		// System.out.println("Unused cells: " + unused);

		// Find an empty cell.
		do {
		    curx = (int) (Math.random() * cols);
		    cury = (int) (Math.random() * rows);
		} while (maze[curx][cury] != 0);

		for (int i = 0; i < nsave; ++i)
		    prevndir[i] = 0;
		--unused;

		for (;;) {
		    // 1 << dir will be UP, DOWN, LEFT, RIGHT.
		    // Those direction constants are chosen so that
		    // the dir values for UP and DOWN and the xor of
		    // each other and so are the dir values for LEFT
		    // and RIGHT.
		    int dir;

		    for (int i = 0; i < nsave - 1; ++i)
			prevndir[i] = prevndir[i+1];

		    boolean gotdir[] = new boolean[4];

		    do {
			// Choose a direction that doesn't go back on the
			// previous step.
			do {
			    dir = (int) (Math.random() * 4);
			} while (dir == (prevndir[nsave - 2] ^ 3));
			prevndir[nsave - 1] = dir;

			for (int i = 0; i < 4; ++i)
			    gotdir[i] = false;
			for (int i = 0; i < nsave; ++i)
			    gotdir[prevndir[i]] = true;

			// Avoid loops within the past nsave steps by
			// making sure not all four directions are used.
			// This is more restrictive than necessary but 
			// it works okay.
		    } while (gotdir[0] && gotdir[1] && gotdir[2] && gotdir[3]);

		    // Add the wall.
		    maze[curx][cury] |= (1 << dir);

		    // Go to the next cell.
		    curx += movetable[dir][0];
		    cury += movetable[dir][1];

		    // Reached another wall?
		    boolean exitcond = (maze[curx][cury] != 0);

		    // Add wall segment needed to join with the previous
		    // cell.
		    maze[curx][cury] |= (1 << (dir ^ 3));

		    if (exitcond)
			break;

		    --unused;
		}
	    }
	} // generatemaze

	/**
	 * Generate the maze. Call this function to do everything.
	 */
	void generate(int rows, int cols) {
	    this.rows = rows;
	    this.cols = cols;
	    initmaze();
	    generatemaze();
	    displaymaze();
	}
    } // MazeCanvas

    /**
     * Initialize the applet.
     */
    public void init() {
	// Find the frame containing this applet.
	Component c = getParent();
	while (c != null && !(c instanceof Frame))
	    c = c.getParent();
	mainwin = (Frame) c;
	    
	setLayout(new BorderLayout());

	Panel p1 = new Panel();

	Label l1 = new Label("Rows:");
	final TextField rowsfield = new TextField("54", 4);

	p1.add(l1);
	p1.add(rowsfield);

	Label l2 = new Label("Columns:");
	final TextField colsfield = new TextField("61", 4);

	p1.add(l2);
	p1.add(colsfield);

	Button gobutton = new Button("Go");
	p1.add(gobutton);

	add("North", p1);

	final MazeCanvas mazecanvas = new MazeCanvas();
	add("Center", mazecanvas);

	gobutton.addActionListener(
	    new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			int rows = 
			    Integer.valueOf(rowsfield.getText()).intValue();
			int cols = 
			    Integer.valueOf(colsfield.getText()).intValue();

			// The maze breaks down for really small
			// dimensions. The upper limit is to put a cap on
			// the run time.

			final int lowerbound = 10;
			final int upperbound = 300;
			
			if (rows < lowerbound || cols < lowerbound)
			    new MyDialog(mainwin, "Out of range",
					 "Number of rows and columns must " +
					 "be at least " + lowerbound);
			else if (rows > upperbound || cols > upperbound)
			    new MyDialog(mainwin, "Out of range",
					 "Number of rows and columns must " +
					 "be below " + upperbound);
			else
			    mazecanvas.generate(rows, cols);

			// new MyDialog(mainwin, "Go",
				     // "Rows: " + rows + "  Cols: " + cols);
		    }
		    catch (NumberFormatException exc) {
			new MyDialog(mainwin, "Invalid numbers",
				     "Invalid numeric value(s) entered.");
		    }
		}
	    }
	);

    } // init

    /**
     * Return information about the applet.
     */
    public String getAppletInfo() {
	return "mazegen - Maze generator.";
    }

    public static void main(String args[]) {
	final mazegen mg = new mazegen();
	final Frame f = new Frame("Maze Generator");

	// Need this so the user can close the window.
	f.addWindowListener(
	    new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    mg.stop();
		    f.setVisible(false);
		    f.dispose();
		    System.exit(0);
		}
	    }
	);
	
	f.add("Center", mg);
	mg.init();
	mg.start();
	f.setSize(500, 500);
	f.setVisible(true);
    }
} // mazegen

// The End
