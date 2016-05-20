package uebung_parallelisierung.parallel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LabyrinthPar4 extends Labyrinth {

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

	protected List<Thread> threads;
	protected List<ArrayDeque<Point>> searchStartList;
	protected List<ArrayDeque<Point>> searchEndList;
	
	public LabyrinthPar4(Grid grid) {
		super(grid);
		this.strategyName = "Parallel with more threads";

		// initially all 0
		visited = new byte[grid.width][grid.height];
		
		threads = new ArrayList<Thread>(8);
		searchStartList = new ArrayList<ArrayDeque<Point>>(4);
		searchEndList = new ArrayList<ArrayDeque<Point>>(4);
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
		final ArrayDeque<Point> adStart = new ArrayDeque<Point>();
		final ArrayDeque<Point> adEnd = new ArrayDeque<Point>();

		visited[grid.start.getX()][grid.start.getY()] = FROM_START;
		visited[grid.end.getX()][grid.end.getY()] = FROM_END;

		Point currentEnd = grid.end;
		do {
			List<Point> points = new ArrayList<Point>(4);
			Direction[] dirs = getBiasedDirections(currentEnd, null, grid.start);
			for (Direction directionToNeighbor : dirs) {
				Point neighbor = currentEnd.getNeighbor(directionToNeighbor);
				if (hasPassage(currentEnd, neighbor)) {
					points.add(neighbor);
				}
			}
			adEnd.addLast(currentEnd);
			if (points.size() == 1) {
				Point neighbor = points.get(0);
				visited[neighbor.getX()][neighbor.getY()] = FROM_END;
				currentEnd = neighbor;
			} else {
				for(Point neighbor : points) {
					visited[neighbor.getX()][neighbor.getY()] = FROM_END;
					final ArrayDeque<Point> resEndThis = new ArrayDeque<Point>();
					resEndThis.addAll(adEnd);
					searchEndList.add(resEndThis);
					Thread t = new Thread(() -> subsolve(neighbor, grid.start, FROM_END, resEndThis));
					threads.add(t);
				}
				break;
			}
		} while (true);
		
		Point currentStart = grid.start;
		do {
			List<Point> points = new ArrayList<Point>(4);
			Direction[] dirs = getBiasedDirections(currentStart, null, grid.end);
			for (Direction directionToNeighbor : dirs) {
				Point neighbor = currentStart.getNeighbor(directionToNeighbor);
				if (hasPassage(currentStart, neighbor)) {
					points.add(neighbor);
				}
			}
			adStart.addLast(currentStart);
			if (points.size() == 1) {
				Point neighbor = points.get(0);
				visited[neighbor.getX()][neighbor.getY()] = FROM_START;
				currentStart = neighbor;
			} else {
				for(Point neighbor : points) {
					visited[neighbor.getX()][neighbor.getY()] = FROM_START;
					final ArrayDeque<Point> resStartThis = new ArrayDeque<Point>();
					resStartThis.addAll(adStart);
					searchStartList.add(resStartThis);
					Thread t = new Thread(() -> subsolve(neighbor, grid.end, FROM_START, resStartThis));
					threads.add(t);
				}
				break;
			}
		} while (true);

		for(Thread t : threads) {
			t.start();
		}
		
		try {
			for(Thread t : threads) {
				t.join();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		ArrayDeque<Point> resStart = null;
		ArrayDeque<Point> resEnd = null;
		
		for(ArrayDeque<Point> a : searchStartList) {
			if(!a.peekLast().equals(new Point(-1, -1))) {
				resStart = a;
				break;
			}
		}
		for(ArrayDeque<Point> a : searchEndList) {
			if(!a.peekLast().equals(new Point(-1, -1))) {
				resEnd = a;
				break;
			}
		}
		/*
		System.out.println("Meeting point was " + meetingPoint);
		
		System.out.println("resStart.size() = " + resStart.size());
		System.out.println("resEnd.size() = " + resEnd.size());
		System.out.println("resEnd1.size() = " + resEnd1.size());
		System.out.println("resEnd2.size() = " + resEnd2.size());
		System.out.println("resEnd1.peekLast() = " + resEnd1.peekLast());
		System.out.println("resEnd2.peekLast() = " + resEnd2.peekLast());

		System.out.println(Thread.activeCount() + " threads running");
		*/
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
									//System.out.println(subSolveNumber + " met someone at the meetingPoint");
									return pathSoFar;
								} /*
									 * else if (visited[meetingPoint.getX()][
									 * meetingPoint.getY()] == subSolveNumber) {
									 * pathSoFar.addLast(current); if
									 * (current.equals(meetingPoint)) { return
									 * pathSoFar; } while (!pathSoFar.isEmpty()
									 * && !hasPassage(pathSoFar.peekLast(),
									 * meetingPoint)) { pathSoFar.removeLast();
									 * } if (hasPassage(pathSoFar.peekLast(),
									 * meetingPoint)) {
									 * pathSoFar.addLast(meetingPoint); return
									 * pathSoFar; } else { pathSoFar.addLast(new
									 * Point(-1, -1)); return null; } } else {
									 * pathSoFar.addLast(new Point(-1, -1));
									 * return null; }
									 */
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
				if (backtrackStack.isEmpty()) {
					pathSoFar.addLast(new Point(-1, -1));
					return null;
				}
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
		if (visited[meetingPoint.getX()][meetingPoint.getY()] == subSolveNumber) {
			//System.out.println("Meine pathSoFar.size() = " + pathSoFar.size());
			pathSoFar.addLast(current);
			if (current.equals(meetingPoint)) {
				return pathSoFar;
			}
			while (!hasPassage(pathSoFar.peekLast(), meetingPoint)) {
				pathSoFar.removeLast();
				if (pathSoFar.isEmpty()) {
					pathSoFar.addLast(new Point(-1, -1));
					return null;
				}
			}
			pathSoFar.addLast(meetingPoint);
			return pathSoFar;
		} else {
			pathSoFar.addLast(new Point(-1, -1));
			return null;
		}
	}
}