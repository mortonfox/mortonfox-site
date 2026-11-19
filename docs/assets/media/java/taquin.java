// <applet code=taquin.class width=410 height=450>
// </applet>

import java.applet.Applet;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;

/**
 * taquin - 4x4 Moving tiles puzzle.
 * Last updated: February 2, 2000
 *
 * @author Po Shan Cheah
 */
public class taquin extends Applet {

    /**
     * Number of rows in the puzzle.
     */
    final int rows = 4;
    /**
     * Number of columns in the puzzle.
     */
    final int columns = 4;

    /**
     * Size of each tile.
     */
    final int tilesize = 100;

    final int tileouterborder = 5;
    final int tileinnerborder = 15;

    /**
     * Subclass of a Canvas that displays and manages the tiles.
     */
    class TaquinCanvas extends Canvas {
	private Image offscreen;
	private Graphics offscreeng;
	private int xsize;
	private int ysize;
	private int blankrow;
	private int blankcol;
	private int board[][] = new int[rows][columns];

	public void init() {
	    addMouseListener(
		new MouseAdapter() {
		    public void mousePressed(MouseEvent e) {
			doClick(e.getX(), e.getY());
		    }
		}
	    );
	    enableEvents(AWTEvent.KEY_EVENT_MASK);
	    addKeyListener(new ArrowKeys(this));
	}

	/**
	 * Reset the tiles to normal (in order) configuration.
	 */
	public void normalreset() {
	    for (int i = 1; i < rows * columns; ++i) {
		board[(i - 1) / rows][(i - 1) % columns] = i;
	    }
	    board[rows - 1][columns - 1] = 0;
	    blankrow = rows - 1;
	    blankcol = columns - 1;
	}
	
	/**
	 * Reset the tiles to the normal configuration except with the
	 * last two tiles swapped.
	 */
	public void invertreset() {
	    normalreset();
	    board[rows - 1][columns - 3] = rows * columns - 1;
	    board[rows - 1][columns - 2] = rows * columns - 2;
	}

	/**
	 * Move a tile up, down, left or right. This class is meant to
	 * separate the moving logic from the code that prevents tiles
	 * from moving over the edge of the board. The moving logic itself
	 * could be with or without animation and is chosen via the
	 * subclasses.
	 */
        abstract class MoveBlankBase {
    
	    /**
	     * Move a tile at the specified row and column into the blank
	     * spot without animation.
	     * @param row The row at which the blank spot will end up.
	     * @param col The column at which the blank spot will end up.
	     */
	    void moveBlank(int row, int col) {
		board[blankrow][blankcol] = board[row][col];
		board[row][col] = 0;
		blankrow = row;
		blankcol = col;
	    }

	    /**
	     * Move a tile at the specified row and column into the blank
	     * spot with animation.
	     * @param row The row at which the blank spot will end up.
	     * @param col The column at which the blank spot will end up.
	     */
	    void moveBlankAnimate(int row, int col) {
		Thread thread = new Thread(
		    new AnimateTile(
			row, col, blankrow, blankcol, 
			String.valueOf(board[row][col])
		    )
		);
		thread.start();
		try {
		    thread.join();
		}
		catch (InterruptedException e) { }
		moveBlank(row, col);
	    }

	    /**
	     * Push a tile up into the blank spot.
	     */
	    public void pushUp() {
		if (blankrow < rows - 1)
		    move(blankrow + 1, blankcol);
	    }

	    /**
	     * Push a tile down into the blank spot.
	     */
	    public void pushDown() {
		if (blankrow > 0)
		    move(blankrow - 1, blankcol);
	    }

	    /**
	     * Push a tile left into the blank spot.
	     */
	    public void pushLeft() {
		if (blankcol < columns - 1)
		    move(blankrow, blankcol + 1);
	    }

	    /**
	     * Push a tile right into the blank spot.
	     */
	    public void pushRight() {
		if (blankcol > 0) 
		    move(blankrow, blankcol - 1);
	    }

	    abstract public void move(int row, int col);
	}

	/**
	 * Move tile without animation.
	 */
	class MoveBlank extends MoveBlankBase {
	    public void move(int row, int col) {
		moveBlank(row, col);
	    }
	}

	/**
	 * Move tile with animation.
	 */
	class MoveBlankAnimate extends MoveBlankBase {
	    public void move(int row, int col) {
		moveBlankAnimate(row, col);
	    }
	}

	MoveBlank moveblank = new MoveBlank();
	MoveBlankAnimate moveblankanimate = new MoveBlankAnimate();

	/**
	 * Process a mouse click on the canvas.
	 * @param x Horizontal pixel location where mouse was clicked.
	 * @param y Vertical pixel location where mouse was clicked.
	 */
	private void doClick(int x, int y) {
	    int clickrow = y / tilesize;
	    int clickcol = x / tilesize;

	    if (clickrow == blankrow && clickcol == blankcol - 1) {
		moveblankanimate.pushRight();
		showBoard();
	    }
	    if (clickrow == blankrow && clickcol == blankcol + 1) {
		moveblankanimate.pushLeft();
		showBoard();
	    }
	    if (clickrow == blankrow - 1 && clickcol == blankcol) {
		moveblankanimate.pushDown();
		showBoard();
	    }
	    if (clickrow == blankrow + 1 && clickcol == blankcol) {
		moveblankanimate.pushUp();
		showBoard();
	    }
	}

	/**
	 * Scramble the puzzle.
	 */
	public void scramble() {
	    for (int i = 0; i < 200; ++i) {
		switch ((int) (Math.random() * 4)) {
		case 0:
		    moveblank.pushUp();
		    break;
		case 1:
		    moveblank.pushDown();
		    break;
		case 2:
		    moveblank.pushLeft();
		    break;
		case 3:
		    moveblank.pushRight();
		    break;
		}
	    }
	}
			

	/**
	 * Initialize the offscreen buffer based on the canvas size.
	 */
	private void initOffscreen() {
	    xsize = getSize().width;
	    ysize = getSize().height;
	    offscreen = createImage(xsize, ysize);
	    offscreeng = offscreen.getGraphics();
	    offscreeng.setColor(Color.black);
	    offscreeng.fillRect(0, 0, xsize, ysize);
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
	private void centerText(Graphics g, String text, int x, int y) {
	    FontMetrics fm = g.getFontMetrics();

	    int locy = y + fm.getAscent() - fm.getHeight() / 2;
	    int locx = x - fm.stringWidth(text) / 2;

	    g.drawString(text, locx, locy);
	}

	/**
	 * Draw a tile. The reason why the x and y coordinates are
	 * specified in pixels instead of tile positions is to be general
	 * enough to support the animation routine that needs to draw a
	 * tile at any arbitrary location, even between tile spaces.
	 * @param x Horizontal pixel coordinate where tile should be drawn.
	 * @param y Vertical pixel coordinate where tile should be drawn.
	 * @param tilestr Tile number string.
	 */
	private void drawTile(int x, int y, String tilestr) {
	    drawBlank(x, y);
	    offscreeng.setColor(Color.white);
	    offscreeng.fillRect(x + tileouterborder, 
				y + tileouterborder, 
				tilesize - tileouterborder * 2, 
				tilesize - tileouterborder * 2);
	    offscreeng.setColor(Color.black);
	    offscreeng.fillRect(x + tileinnerborder, 
				y + tileinnerborder, 
				tilesize - tileinnerborder * 2, 
				tilesize - tileinnerborder * 2);
	    offscreeng.setColor(Color.white);
	    centerText(offscreeng, tilestr,
		       x + tilesize / 2, 
		       y + tilesize / 2);
	}

	/**
	 * Draw a blank spot.
	 */
	private void drawBlank(int x, int y) {
	    offscreeng.setColor(Color.black);
	    offscreeng.fillRect(x, y, tilesize, tilesize);
	}

	/**
	 * Animation routine for moving a tile. This is run in a thread of
	 * its own.
	 */
	class AnimateTile implements Runnable {

	    private int oldx;
	    private int oldy;
	    private int newx;
	    private int newy;
	    private String tilestr;

	    /**
	     * Number of steps to animate the tile movement.
	     */
	    final int steps = 5;

	    /**
	     * Delay in milliseconds between each animation frame.
	     */
	    final int delay = 10;

	    public AnimateTile(int oldrow, int oldcol,
			       int newrow, int newcol,
			       String tilestr) {
		oldx = oldcol * tilesize;
		oldy = oldrow * tilesize;
		newx = newcol * tilesize;
		newy = newrow * tilesize;
		this.tilestr = tilestr;
	    }

	    /**
	     * Run the animation.
	     */
	    public void run() {
		int deltax = (newx - oldx) / steps;
		int deltay = (newy - oldy) / steps;
		int x = oldx;
		int y = oldy;
		Graphics g = getGraphics();

		// Set the clip area to a 2 by 2 tile square to
		// reduce the amount of repainting required.
		g.setClip(Math.min(oldx, newx), Math.min(oldy, newy), 
			  2 * tilesize, 2 * tilesize);

		for (int i = 0; i < steps; ++i) {

		    drawBlank(oldx, oldy);
		    drawTile(x, y, tilestr);

		    // For some reason, repaint() does not work here.
		    paint(g);

		    // Sleep until time for next frame.
		    try { 
			Thread.sleep(delay); 
		    }
		    catch (InterruptedException e) { }

		    x += deltax;
		    y += deltay;
		}

		// Reset the clip area.
		g.setClip(0, 0, xsize, ysize);
	    }
	}

	/**
	 * Display the tile puzzle.
	 */
	public void showBoard() {
	    if (offscreen == null)
		initOffscreen();
	    offscreeng.setFont(new Font("SansSerif", Font.BOLD, 24));

	    for (int i = 0; i < rows; ++i) {
		for (int j = 0; j < columns; ++j) {
		    if (board[i][j] == 0) {
			drawBlank(j * tilesize, i * tilesize);
		    }
		    else {
			drawTile(j * tilesize, i * tilesize, 
				 String.valueOf(board[i][j]));
		    }
		}
	    }
	    repaint();
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
    }

    /**
     * Key listener class that handles the arrow keys.
     */
    class ArrowKeys extends KeyAdapter {
	TaquinCanvas tcnvs;
	
	public ArrowKeys(TaquinCanvas tcanvas) {
	    tcnvs = tcanvas;
	}
	
	public void keyPressed(KeyEvent e) {
	    switch (e.getKeyCode()) {
	    case KeyEvent.VK_UP:
		tcnvs.moveblankanimate.pushUp();
		tcnvs.showBoard();
		break;
	    case KeyEvent.VK_DOWN:
		tcnvs.moveblankanimate.pushDown();
		tcnvs.showBoard();
		break;
	    case KeyEvent.VK_LEFT:
		tcnvs.moveblankanimate.pushLeft();
		tcnvs.showBoard();
		break;
	    case KeyEvent.VK_RIGHT:
		tcnvs.moveblankanimate.pushRight();
		tcnvs.showBoard();
		break;
	    }
	}
    }

    /**
     * Subclass of Button that processes arrow keys. We need to do this
     * because the input focus is on the buttons and not the canvas.
     */
    class KeyButton extends Button {
	public KeyButton(TaquinCanvas tcanvas, String title) {
	    super(title);
	    enableEvents(AWTEvent.KEY_EVENT_MASK);
	    addKeyListener(new ArrowKeys(tcanvas));
	}
    }

    public void init() {
	// Find the frame containing this applet.
	Component c = getParent();
	while (c != null && !(c instanceof Frame))
	    c = c.getParent();
	Frame mainwin = (Frame) c;

	final TaquinCanvas tcanvas = new TaquinCanvas();

	setLayout(new BorderLayout());

	Panel p1 = new Panel();

	KeyButton normalButton = new KeyButton(tcanvas, "Normal Reset");
	p1.add(normalButton);
	KeyButton invertButton = new KeyButton(tcanvas, "Inverted Reset");
	p1.add(invertButton);
	KeyButton scrambleButton = new KeyButton(tcanvas, "Scramble");
	p1.add(scrambleButton);

	add("North", p1);

	add("Center", tcanvas);

	normalButton.addActionListener(
	    new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    tcanvas.normalreset();
		    tcanvas.showBoard();
	       	}
	    }
	);
	invertButton.addActionListener(
	    new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    tcanvas.invertreset();
		    tcanvas.showBoard();
	       	}
	    }
	);
	scrambleButton.addActionListener(
	    new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    tcanvas.scramble();
		    tcanvas.showBoard();
	       	}
	    }
	);

	doLayout();
	p1.doLayout();

	tcanvas.normalreset();
	tcanvas.showBoard();
	tcanvas.init();

	// Move the input focus into the first button. When run in applet
	// form, the focus does not begin on the first button by default.
	normalButton.requestFocus();
    }

    public static void main(String args[]) {
	final taquin h = new taquin();
	final Frame f = new Frame("Taquin");

	// Need this so the user can close the window.
	f.addWindowListener(
	    new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
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
	f.setSize(420, 480);
	f.show();
    }
}

// The End
