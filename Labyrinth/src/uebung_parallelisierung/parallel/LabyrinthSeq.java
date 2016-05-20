package uebung_parallelisierung.parallel;

import java.util.ArrayDeque;

public class LabyrinthSeq extends Labyrinth {

	private static final long serialVersionUID = 1L;
	
	// For each cell in the labyrinth: Has solve() visited it yet?
	protected final boolean[][] visited;
	
	public LabyrinthSeq(Grid grid) {
		super(grid);
		this.strategyName = "Sequential";
		visited = new boolean[grid.width][grid.height]; // initially all false
	}
	
	protected boolean visitedBefore(Point p) {
		boolean result = visited[p.getX()][p.getY()];
		// DEBUG
		// if (result)
		// System.out.println("Node " + p + " already visited.");
		return result;
	}

	protected void visit(Point p) {
		// DEBUG System.out.println("Visiting " + p);
		visited[p.getX()][p.getY()] = true;
	}
	
	/**
	 * @return Returns a path through the labyrinth from start to end as an
	 *         array, or null if no solution exists
	 */
	public Point[] solve() {

		Point current = grid.start;
		ArrayDeque<Point> pathSoFar = new ArrayDeque<Point>(); // Path from
																// start to just
																// before
																// current

		ArrayDeque<PointAndDirection> backtrackStack = new ArrayDeque<PointAndDirection>();
		// Used as a stack: Branches not yet taken; solver will backtrack to
		// these branching points later
		// Is it faster to allocate backtrackStack with width*height elements
		// right away?

		while (!current.equals(grid.end)) {
			Point next = null;
			visit(current);

			// Use first random unvisited neighbor as next cell, push others on
			// the backtrack stack:
			Direction[] dirs = Direction.values();
			for (Direction directionToNeighbor : dirs) {
				Point neighbor = current.getNeighbor(directionToNeighbor);
				if (hasPassage(current, neighbor) && !visitedBefore(neighbor)) {
					if (next == null) // 1st unvisited neighbor
						next = neighbor;
					else // 2nd or higher unvisited neighbor: Save neighbor as
							// starting cell for a later backtracking
						backtrackStack.push(new PointAndDirection(neighbor, directionToNeighbor.opposite));
				}
			}
			// Advance to next cell, if any:
			if (next != null) {
				// DEBUG System.out.println("Advancing from " + current + " to "
				// + next);
				pathSoFar.addLast(current);
				current = next;
			} else {
				// current has no unvisited neighbor: Backtrack, if possible
				if (backtrackStack.isEmpty())
					return null; // No more backtracking avaible: No solution
									// exists

				// Backtrack: Continue with cell saved at latest branching
				// point:
				PointAndDirection pd = backtrackStack.pop();
				current = pd.getPoint();
				Point branchingPoint = current.getNeighbor(pd.getDirectionToBranchingPoint());
				// DEBUG System.out.println("Backtracking to " +
				// branchingPoint);
				// Remove the dead end from the top of pathSoFar, i.e. all cells
				// after branchingPoint:
				while (!pathSoFar.peekLast().equals(branchingPoint)) {
					// DEBUG System.out.println(" Going back before " +
					// pathSoFar.peekLast());
					pathSoFar.removeLast();
				}
			}
		}
		pathSoFar.addLast(current);
		// Point[0] is only for making the return value have type Point[] (and
		// not Object[]):
		return pathSoFar.toArray(new Point[0]);
	}
}
