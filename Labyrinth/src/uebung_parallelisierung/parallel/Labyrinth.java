package uebung_parallelisierung.parallel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public abstract class Labyrinth extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final int CELL_PX = 10; // width and length of the labyrinth
											// cells in pixels
	private static final int HALF_WALL_PX = 2; // thickness/2 of the labyrinth
												// walls in pixels
	// labyrinths with more pixels than this (in one or both directions) will
	// not be graphically displayed:
	private static final int MAX_PX_TO_DISPLAY = 1000;

	// The default size of the labyrinth (i.e. unless program is invoked with
	// size arguments):
	// private static final int DEFAULT_WIDTH_IN_CELLS = 5000;
	// private static final int DEFAULT_HEIGHT_IN_CELLS = 5000;

	protected final Grid grid;

	private Point[] solution = null; // set to solution path once that has
										// been
										// computed
	private long solvingTime;

	protected String strategyName;

	public Labyrinth(Grid grid) {
		this.grid = grid;
	}

	protected boolean hasPassage(Point from, Point to) {
		if (!grid.contains(from) || !grid.contains(to)) {
			return false;
		}
		if (from.getNeighbor(Direction.N).equals(to))
			return (grid.passages[from.getX()][from.getY()] & Direction.N.bit) != 0;
		if (from.getNeighbor(Direction.S).equals(to))
			return (grid.passages[from.getX()][from.getY()] & Direction.S.bit) != 0;
		if (from.getNeighbor(Direction.E).equals(to))
			return (grid.passages[from.getX()][from.getY()] & Direction.E.bit) != 0;
		if (from.getNeighbor(Direction.W).equals(to))
			return (grid.passages[from.getX()][from.getY()] & Direction.W.bit) != 0;
		return false; // To suppress warning about undefined return value
	}

	protected abstract Point[] solve();

	protected Point getMeetingPoint() {
		return null;
	}

	public boolean checkSolution() {
		Point from = solution[0];
		if (!from.equals(grid.start)) {
			System.out.println("checkSolution fails because the first cell is" + from + ", but not  " + grid.start);
			return false;
		}

		for (int i = 1; i < solution.length; ++i) {
			Point to = solution[i];
			if (!hasPassage(from, to)) {
				System.out.println("checkSolution fails because there is no passage from " + from + " to " + to);
				return false;
			}
			from = to;
		}
		if (!from.equals(grid.end)) {
			System.out.println("checkSolution fails because the last cell is" + from + ", but not  " + grid.end);
			return false;
		}
		return true;
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		display(graphics);
	}

	public void print() {
		for (int i = 0; i < grid.height; i++) {
			// draw the north edges
			for (int j = 0; j < grid.width; j++) {
				System.out.print((grid.passages[j][i] & Direction.N.bit) == 0 ? "+---" : "+   ");
			}
			System.out.println("+");
			// draw the west edges
			for (int j = 0; j < grid.width; j++) {
				System.out.print((grid.passages[j][i] & Direction.W.bit) == 0 ? "|   " : "    ");
			}
			// draw the far east edge
			System.out.println("|");
		}
		// draw the bottom line
		for (int j = 0; j < grid.width; j++) {
			System.out.print("+---");
		}
		System.out.println("+");
	}

	private boolean smallEnoughToDisplay() {
		return grid.width * CELL_PX <= MAX_PX_TO_DISPLAY && grid.height * CELL_PX <= MAX_PX_TO_DISPLAY;
	}

	public void display(Graphics graphics) {
		// draw white background
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, grid.width * CELL_PX, grid.height * CELL_PX);

		// draw solution path, if available
		if (solution != null) {
			graphics.setColor(Color.YELLOW);
			for (Point p : solution)
				/*
				 * // fill only white area between the walls instead of whole
				 * cell: graphics.fillRect(p.getX()*CELL_PX+HALF_WALL_PX,
				 * p.getY()*CELL_PX+HALF_WALL_PX, CELL_PX-2*HALF_WALL_PX,
				 * CELL_PX-2*HALF_WALL_PX);
				 */
				graphics.fillRect(p.getX() * CELL_PX, p.getY() * CELL_PX, CELL_PX, CELL_PX);
			Point m = getMeetingPoint();
			if (m != null) {
				graphics.setColor(Color.BLUE);
				graphics.fillRect(m.getX() * CELL_PX, m.getY() * CELL_PX, CELL_PX, CELL_PX);
			}
		}

		// draw start and end cell in special colors (covering start and end
		// cell of the solution path)
		graphics.setColor(Color.RED);
		graphics.fillRect(grid.start.getX() * CELL_PX, grid.start.getY() * CELL_PX, CELL_PX, CELL_PX);
		graphics.setColor(Color.GREEN);
		graphics.fillRect(grid.end.getX() * CELL_PX, grid.end.getY() * CELL_PX, CELL_PX, CELL_PX);

		// draw black walls (covering part of the solution path)
		graphics.setColor(Color.BLACK);
		for (int x = 0; x < grid.width; ++x) {
			for (int y = 0; y < grid.height; ++y) {
				// draw north edge of each cell (together with south edge of
				// cell above)
				if ((grid.passages[x][y] & Direction.N.bit) == 0)
					// y-HALF_WALL_PX will be half out of labyrinth for x==0
					// row,
					// but that does not hurt the picture thanks to automatic
					// cropping
					graphics.fillRect(x * CELL_PX, y * CELL_PX - HALF_WALL_PX, CELL_PX, 2 * HALF_WALL_PX);
				// draw west edge of each cell (together with east edge of cell
				// to the left)
				if ((grid.passages[x][y] & Direction.W.bit) == 0)
					// x-HALF_WALL_PX will be half out of labyrinth for y==0
					// column,
					// but that does not hurt the picture thanks to automatic
					// cropping
					graphics.fillRect(x * CELL_PX - HALF_WALL_PX, y * CELL_PX, 2 * HALF_WALL_PX, CELL_PX);
			}
		}
		// draw east edge of labyrinth
		graphics.fillRect(grid.width * CELL_PX, 0, HALF_WALL_PX, grid.height * CELL_PX);
		// draw south edge of labyrinth
		graphics.fillRect(0, grid.height * CELL_PX - HALF_WALL_PX, grid.width * CELL_PX, HALF_WALL_PX);
	}

	public void printSolution() {
		System.out.print("Solution: ");
		for (Point p : solution)
			System.out.print(p);
		System.out.println();
	}

	public void displaySolution(JFrame frame) {
		repaint();
	}
	
	protected void afterSolve() {
		return;
	}

	public long solveAndMeasure() {
		long startTime = System.currentTimeMillis();
		this.solution = this.solve();
		long endTime = System.currentTimeMillis();

		afterSolve();
		
		this.solvingTime = endTime - startTime;
		return this.solvingTime;
	}

	public void showSolutionDetails() {
		JFrame frame = null;

		if (this.smallEnoughToDisplay()) {
			frame = new JFrame("Sequential labyrinth solver");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			// TODO: Window is initially displayed somewhat smaller than
			// the indicated frame size, therefore use width+5 and height+5:
			frame.setSize((this.grid.width + 5) * CELL_PX, (this.grid.height + 5) * CELL_PX);

			// Put a scroll pane around the labyrinth frame if the latter is too
			// large
			// (by Joern Lenselink)
			Dimension displayDimens = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
					.getMaximumWindowBounds().getSize();
			Dimension labyrinthDimens = frame.getSize();
			if (labyrinthDimens.height > displayDimens.height) {
				JScrollPane scroll = new JScrollPane();
				this.setBackground(Color.LIGHT_GRAY);
				frame.getContentPane().add(scroll);
				JPanel borderlayoutpanel = new JPanel();
				borderlayoutpanel.setBackground(Color.darkGray);
				scroll.setViewportView(borderlayoutpanel);
				borderlayoutpanel.setLayout(new BorderLayout(0, 0));

				JPanel columnpanel = new JPanel();
				borderlayoutpanel.add(columnpanel, BorderLayout.NORTH);
				columnpanel.setLayout(new GridLayout(0, 1, 0, 1));
				columnpanel.setOpaque(false);
				columnpanel.setBackground(Color.darkGray);

				columnpanel.setSize(labyrinthDimens.getSize());
				columnpanel.setPreferredSize(labyrinthDimens.getSize());
				columnpanel.add(this);
			} else {
				// No scroll pane needed:
				frame.getContentPane().add(this);
			}

			frame.setVisible(true); // will draw the labyrinth (without
									// solution)
			this.print();
		}

		System.out.println(this.strategyName + ": Computed solution of length " + this.solution.length
				+ " to labyrinth of size " + this.grid.width + "x" + this.grid.height + " in " + solvingTime + "ms.");

		if (this.smallEnoughToDisplay()) {
			this.displaySolution(frame);
			this.printSolution();
		}
	}
}