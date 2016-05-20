package uebung_parallelisierung.parallel;

import java.util.ArrayDeque;
import java.util.Iterator;

public class LabyrinthPar extends Labyrinth {

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

	private static final byte FROM_START = (byte) 1;
	private static final byte FROM_END = (byte) 2;

	// For each cell in the labyrinth: Has solve() visited it yet?
	protected final byte[][] visited;

	protected volatile Point meetingPoint = null;

	protected Thread t;

	public LabyrinthPar(Grid grid) {
		super(grid);
		this.strategyName = "Parallel with 2 threads";

		// initially all 0
		visited = new byte[grid.width][grid.height];
	}

	protected Direction[] getBiasedDirections(Point cur, Direction lastDir, Point goal) {
		int diffX = cur.x - goal.x;
		int diffY = cur.y - goal.y;
		if (lastDir == null) {
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
		switch (lastDir) {
		case S:
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
		case N:
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
		case W:
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
		case E:
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
		final ArrayDeque<Point> resStart = new ArrayDeque<Point>();
		final ArrayDeque<Point> resEnd = new ArrayDeque<Point>();

		visited[grid.start.getX()][grid.start.getY()] = FROM_START;
		visited[grid.end.getX()][grid.end.getY()] = FROM_END;

		t = new Thread(() -> subsolve(grid.end, grid.start, FROM_END, resEnd));
		t.start();

		subsolve(grid.start, grid.end, FROM_START, resStart);

		try {
			t.join();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Point[] res = new Point[resStart.size() + resEnd.size()];
		int i = resStart.size();
		resStart.toArray(res);
		Iterator<Point> it = resEnd.descendingIterator();
		while (it.hasNext()) {
			res[i++] = it.next();
		}
		return res;
	}

	public ArrayDeque<Point> subsolve(final Point searchStart, final Point searchEnd, final byte subSolveNumber,
			final ArrayDeque<Point> pathSoFar) {
		// Used as a stack: Branches not yet taken; solver will backtrack to
		// these branching points later
		ArrayDeque<PointAndDirection> backtrackStack = new ArrayDeque<PointAndDirection>();

		Point current = searchStart;

		Direction lastDirection = null;

		// Continue the search until a meeting point is found
		while (meetingPoint == null) {
			Point next = null;
			Direction[] dirs = getBiasedDirections(current, lastDirection, searchEnd);
			for (Direction directionToNeighbor : dirs) {
				Point neighbor = current.getNeighbor(directionToNeighbor);
				if (hasPassage(current, neighbor)) {
					boolean gotIt = false;
					synchronized (visited[neighbor.getX()]) {
						// I can visit this cell
						if (visited[neighbor.getX()][neighbor.getY()] == 0) {
							visited[neighbor.getX()][neighbor.getY()] = subSolveNumber;
							gotIt = true;
							// We met, search over!
						} else if (visited[neighbor.getX()][neighbor.getY()] != subSolveNumber) {
							synchronized (visited) {
								if (meetingPoint == null) {
									meetingPoint = neighbor;
									pathSoFar.addLast(current);
									return pathSoFar;
								} else {
									pathSoFar.addLast(current);
									if (current.equals(meetingPoint)) {
										return pathSoFar;
									}
									while (!hasPassage(pathSoFar.peekLast(), meetingPoint)) {
										pathSoFar.removeLast();
									}
									pathSoFar.addLast(meetingPoint);
									return pathSoFar;
								}
							}
						}
					}
					if (gotIt) {
						// 1st unvisited neighbor
						if (next == null) {
							next = neighbor;
							lastDirection = directionToNeighbor;
						} else {
							backtrackStack.push(new PointAndDirection(current, directionToNeighbor));
						}
					}
				}
			}
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
				Point branchingPoint = pd.getPoint();
				// Remove the dead end from the top of pathSoFar, i.e. all cells
				// after branchingPoint:
				while (!pathSoFar.peekLast().equals(branchingPoint)) {
					pathSoFar.removeLast();
				}
				Direction dir = pd.getDirectionToBranchingPoint();
				current = branchingPoint.getNeighbor(dir);
				lastDirection = dir;
			}
		}
		// meetingPoint != null, search over!
		pathSoFar.addLast(current);
		if (current.equals(meetingPoint)) {
			return pathSoFar;
		}
		while (!hasPassage(pathSoFar.peekLast(), meetingPoint)) {
			pathSoFar.removeLast();
		}
		pathSoFar.addLast(meetingPoint);
		return pathSoFar;
	}
}