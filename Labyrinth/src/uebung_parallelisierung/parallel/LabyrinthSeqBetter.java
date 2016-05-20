package uebung_parallelisierung.parallel;

import java.util.ArrayDeque;
import java.util.Iterator;

public class LabyrinthSeqBetter extends Labyrinth {

	private static final long serialVersionUID = 1L;

	private static final Direction[] NEWS = { Direction.N, Direction.E, Direction.W, Direction.S };
	private static final Direction[] NWES = { Direction.N, Direction.W, Direction.E, Direction.S };
	private static final Direction[] SEWN = { Direction.S, Direction.E, Direction.W, Direction.N };
	private static final Direction[] SWEN = { Direction.S, Direction.W, Direction.E, Direction.N };
	private static final Direction[] ENSW = { Direction.E, Direction.N, Direction.S, Direction.W };
	private static final Direction[] ESNW = { Direction.E, Direction.S, Direction.N, Direction.W };
	private static final Direction[] WNSE = { Direction.W, Direction.N, Direction.S, Direction.E };
	private static final Direction[] WSNE = { Direction.W, Direction.S, Direction.N, Direction.E };
	private static final Direction[] NSW = { Direction.N, Direction.S, Direction.W };
	private static final Direction[] NSE = { Direction.N, Direction.S, Direction.E };
	private static final Direction[] NEW = { Direction.N, Direction.E, Direction.W };
	private static final Direction[] NES = { Direction.N, Direction.E, Direction.S };
	private static final Direction[] NWE = { Direction.N, Direction.W, Direction.E };
	private static final Direction[] NWS = { Direction.N, Direction.W, Direction.S };
	private static final Direction[] SNE = { Direction.S, Direction.N, Direction.E };
	private static final Direction[] SNW = { Direction.S, Direction.N, Direction.W };
	private static final Direction[] SEN = { Direction.S, Direction.E, Direction.N };
	private static final Direction[] SEW = { Direction.S, Direction.E, Direction.W };
	private static final Direction[] SWN = { Direction.S, Direction.W, Direction.N };
	private static final Direction[] SWE = { Direction.S, Direction.W, Direction.E };
	private static final Direction[] ENS = { Direction.E, Direction.N, Direction.S };
	private static final Direction[] ENW = { Direction.E, Direction.N, Direction.W };
	private static final Direction[] ESN = { Direction.E, Direction.S, Direction.N };
	private static final Direction[] ESW = { Direction.E, Direction.S, Direction.W };
	private static final Direction[] EWN = { Direction.E, Direction.W, Direction.N };
	private static final Direction[] EWS = { Direction.E, Direction.W, Direction.S };
	private static final Direction[] WNS = { Direction.W, Direction.N, Direction.S };
	private static final Direction[] WNE = { Direction.W, Direction.N, Direction.E };
	private static final Direction[] WSN = { Direction.W, Direction.S, Direction.N };
	private static final Direction[] WSE = { Direction.W, Direction.S, Direction.E };
	private static final Direction[] WEN = { Direction.W, Direction.E, Direction.N };
	private static final Direction[] WES = { Direction.W, Direction.E, Direction.S };

	// For each cell in the labyrinth: Has solve() visited it yet?
	protected final byte[][] visited;

	protected Point meetingPoint = null;

	// private int searchDepth = 0;

	public LabyrinthSeqBetter(Grid grid) {
		super(grid);
		this.strategyName = "Sequential better";

		// initially all 0
		visited = new byte[grid.width][grid.height];
	}

	protected Direction[] getBiasedDirections(Point cur, Direction ignoreDir, Point goal) {
		int diffX = cur.x - goal.x;
		int diffY = cur.y - goal.y;
		if (ignoreDir == null) {
			if (Math.abs(diffX) > Math.abs(diffY)) {
				if (diffX > 0) {
					if (diffY > 0) {
						return WNSE;
					} else {
						return WSNE;
					}
				} else {
					if (diffY > 0) {
						return ENSW;
					} else {
						return ESNW;
					}
				}
			} else {
				if (diffY > 0) {
					if (diffX > 0) {
						return NWES;
					} else {
						return NEWS;
					}
				} else {
					if (diffX > 0) {
						return SWEN;
					} else {
						return SEWN;
					}
				}
			}
		}
		switch (ignoreDir) {
		case N:
			if (Math.abs(diffX) > Math.abs(diffY)) {
				if (diffX > 0) {
					return WSE;
				} else {
					return ESW;
				}
			} else {
				if (diffY > 0) {
					if (diffX > 0) {
						return WES;
					} else {
						return EWS;
					}
				} else {
					if (diffX > 0) {
						return SWE;
					} else {
						return SEW;
					}
				}
			}
			// break;
		case S:
			if (Math.abs(diffX) > Math.abs(diffY)) {
				if (diffX > 0) {
					return WNE;
				} else {
					return ENW;
				}
			} else {
				if (diffY > 0) {
					if (diffX > 0) {
						return NWE;
					} else {
						return NEW;
					}
				} else {
					if (diffX > 0) {
						return WEN;
					} else {
						return EWN;
					}
				}
			}
			// break;
		case E:
			if (Math.abs(diffX) > Math.abs(diffY)) {
				if (diffX > 0) {
					if (diffY > 0) {
						return WNS;
					} else {
						return WSN;
					}
				} else {
					if (diffY > 0) {
						return NSW;
					} else {
						return SNW;
					}
				}
			} else {
				if (diffY > 0) {
					return NWS;
				} else {
					return SWN;
				}
			}
			// break;
		case W:
			if (Math.abs(diffX) > Math.abs(diffY)) {
				if (diffX > 0) {
					if (diffY > 0) {
						return NSE;
					} else {
						return SNE;
					}
				} else {
					if (diffY > 0) {
						return ENS;
					} else {
						return ESN;
					}
				}
			} else {
				if (diffY > 0) {
					return NES;
				} else {
					return SEN;
				}
			}
			// break;
		default:
			return NEWS;
		}
	}

	protected Point getMeetingPoint() {
		return meetingPoint;
	}

	public Point[] solve() {
		return subsolve(grid.start, grid.end);
	}

	protected Point[] finalize(ArrayDeque<Point> res1, ArrayDeque<Point> res2) {
		Point[] res = new Point[res1.size() + res2.size()];
		int i = res1.size();
		res1.toArray(res);
		Iterator<Point> it = res2.descendingIterator();
		while (it.hasNext()) {
			res[i++] = it.next();
		}
		return res;
	}

	public Point[] subsolve(Point searchStart, Point searchEnd) {
		visited[searchStart.getX()][searchStart.getY()] = 1;
		Point current = searchStart;
		// Path from start to just before current
		ArrayDeque<Point> pathSoFar = new ArrayDeque<Point>();
		// Used as a stack: Branches not yet taken; solver will backtrack to
		// these branching points later
		ArrayDeque<PointAndDirection> backtrackStack = new ArrayDeque<PointAndDirection>();

		Direction lastOppositeDirection = null;

		visited[searchEnd.getX()][searchEnd.getY()] = 2;
		Point current2 = searchEnd;
		// Path from start to just before current
		ArrayDeque<Point> pathSoFar2 = new ArrayDeque<Point>();
		// Used as a stack: Branches not yet taken; solver will backtrack to
		// these branching points later
		ArrayDeque<PointAndDirection> backtrackStack2 = new ArrayDeque<PointAndDirection>();

		Direction lastOppositeDirection2 = null;

		// Let's go!
		while (true) {
			// You met me, search over!
			if (meetingPoint != null) {
				pathSoFar.addLast(current);
				if (hasPassage(pathSoFar.peekLast(), meetingPoint)) {
					pathSoFar.addLast(meetingPoint);
					return finalize(pathSoFar, pathSoFar2);
				} else if (!pathSoFar.peekLast().equals(meetingPoint)) {
					PointAndDirection pd;
					Point cur;
					do {
						pd = backtrackStack.pop();
						cur = pd.getPoint();
					} while (!cur.equals(meetingPoint));
					Point branchingPoint = cur.getNeighbor(pd.getDirectionToBranchingPoint());
					while (!pathSoFar.peekLast().equals(branchingPoint)) {
						pathSoFar.removeLast();
					}
					pathSoFar.addLast(meetingPoint);
				}
				return finalize(pathSoFar, pathSoFar2);
			}
			Point next = null;
			Direction[] dirs = getBiasedDirections(current, lastOppositeDirection, searchEnd);
			for (Direction directionToNeighbor : dirs) {
				Point neighbor = current.getNeighbor(directionToNeighbor);
				if (hasPassage(current, neighbor)) {
					boolean gotIt = false;
					// I can visit this cell
					if (visited[neighbor.getX()][neighbor.getY()] == 0) {
						visited[neighbor.getX()][neighbor.getY()] = 1;
						gotIt = true;
						// I met you, search over!
					} else if (visited[neighbor.getX()][neighbor.getY()] != 1) {
						meetingPoint = neighbor;
						pathSoFar.addLast(current);
						break;
					}
					if (gotIt) {
						// 1st unvisited neighbor
						if (next == null) {
							next = neighbor;
							lastOppositeDirection = directionToNeighbor.opposite;
						} else {
							backtrackStack.push(new PointAndDirection(neighbor, directionToNeighbor.opposite));
						}
					}
				}
			}
			if (meetingPoint == null) {
				// Advance to next cell, if any:
				if (next != null) {
					pathSoFar.addLast(current);
					current = next;
					// current has no unvisited neighbor: Backtrack, if possible
				} else {
					// No more backtracking available: No solution exists
					if (backtrackStack.isEmpty())
						return null;
					// Backtrack: Continue with cell saved at latest branching
					// point:
					PointAndDirection pd = backtrackStack.pop();
					current = pd.getPoint();
					Direction dir = pd.getDirectionToBranchingPoint();
					Point branchingPoint = current.getNeighbor(dir);
					lastOppositeDirection = dir;
					// Remove the dead end from the top of pathSoFar, i.e. all
					// cells
					// after branchingPoint:
					while (!pathSoFar.peekLast().equals(branchingPoint)) {
						pathSoFar.removeLast();
					}
				}
			}

			// You met me, search over!
			if (meetingPoint != null) {
				pathSoFar2.addLast(current2);
				if (hasPassage(pathSoFar2.peekLast(), meetingPoint)) {
					pathSoFar2.addLast(meetingPoint);
					return finalize(pathSoFar, pathSoFar2);
				} else if (!pathSoFar2.peekLast().equals(meetingPoint)) {
					PointAndDirection pd;
					Point cur;
					do {
						pd = backtrackStack2.pop();
						cur = pd.getPoint();
					} while (!cur.equals(meetingPoint));
					Point branchingPoint = cur.getNeighbor(pd.getDirectionToBranchingPoint());
					while (!pathSoFar2.peekLast().equals(branchingPoint)) {
						pathSoFar2.removeLast();
					}
					pathSoFar2.addLast(meetingPoint);
				}
				return finalize(pathSoFar, pathSoFar2);
			}
			Point next2 = null;
			Direction[] dirs2 = getBiasedDirections(current2, lastOppositeDirection2, searchStart);
			for (Direction directionToNeighbor : dirs2) {
				Point neighbor = current2.getNeighbor(directionToNeighbor);
				if (hasPassage(current2, neighbor)) {
					boolean gotIt = false;
					// I can visit this cell
					if (visited[neighbor.getX()][neighbor.getY()] == 0) {
						visited[neighbor.getX()][neighbor.getY()] = 2;
						gotIt = true;
						// I met you, search over!
					} else if (visited[neighbor.getX()][neighbor.getY()] != 2) {
						meetingPoint = neighbor;
						pathSoFar2.addLast(current2);
						break;
					}
					if (gotIt) {
						// 1st unvisited neighbor
						if (next2 == null) {
							next2 = neighbor;
							lastOppositeDirection2 = directionToNeighbor.opposite;
						} else {
							backtrackStack2.push(new PointAndDirection(neighbor, directionToNeighbor.opposite));
						}
					}
				}
			}
			if (meetingPoint == null) {
				// Advance to next cell, if any:
				if (next2 != null) {
					pathSoFar2.addLast(current2);
					current2 = next2;
					// current has no unvisited neighbor: Backtrack, if
					// possible
				} else {
					// No more backtracking available: No solution exists
					if (backtrackStack2.isEmpty())
						return null;
					// Backtrack: Continue with cell saved at latest
					// branching
					// point:
					PointAndDirection pd = backtrackStack2.pop();
					current2 = pd.getPoint();
					Direction dir = pd.getDirectionToBranchingPoint();
					Point branchingPoint = current2.getNeighbor(dir);
					lastOppositeDirection2 = dir;
					// Remove the dead end from the top of pathSoFar, i.e.
					// all
					// cells
					// after branchingPoint:
					while (!pathSoFar2.peekLast().equals(branchingPoint)) {
						pathSoFar2.removeLast();
					}
				}
			}
		}
	}
}