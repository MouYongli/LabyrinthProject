package uebung_parallelisierung.parallel;

import java.io.Serializable;

public class Grid implements Serializable{
	private static final long serialVersionUID = 1L;

	/**
	 * Serialized state of a labyrinth with size, passages, start and end
	 * (without search state).
	 * This is only a separate class in order to easily (de)serialize
	 * the state of the labyrinth. In all other respects, it should be
	 * considered a part of class Labyrinth (which is also why its 
	 * attributes are not private).
	 */
	
	final public int width;  // total number of cells in x direction
	final public int height;  // total number of cells in y direction
	final public Point start;
	final public Point end;
	
	final public byte[][] passages;
	/*		
	 *  Each array element represents a cell in the labyrinth with the passages possible from 
	 *  this cell. Its four least significant bits are interpreted as one flag for each direction
	 *  (see enum Direction for which bit means which direction) indicating whether 
	 *  there is a passage from this cell in that direction (note that passages
	 *  and walls are not cells, but represented indirectly by these flags).
	 *  Initially all cells are 0, i.e. have no passage from them (i.e. surrounded
	 *  by walls on all their four sides). Note that two-way passages appear as opposite
	 *  bits in both the source and destination cell; thus, this data structure supports
	 *  one-way passages, too, by setting a bit in the source cell only.
	 */	
	
	public Grid(int width, int height, Point start, Point end) {
		this.width = width;
		this.height = height;
		this.start = start;
		this.end = end;
		
		passages = new byte[width][height]; // initially all 0 (see comment at declaration of passages)
	}

	public boolean contains(Point p) {
		return 0 <= p.getX() && p.getX() < this.width && 
			   0 <= p.getY() && p.getY() < this.height;
	}
}