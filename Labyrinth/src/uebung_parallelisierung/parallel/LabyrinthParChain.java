package uebung_parallelisierung.parallel;

import java.util.ArrayDeque;
import java.util.Iterator;

public class LabyrinthParChain extends Labyrinth {

	class PointChain {
		public int point;
		public byte solver;
		public int prev_point;

		public PointChain(Point _point, byte _solver) {
			point = (_point.x << 16) + _point.y;
			solver = _solver;
			prev_point = -1;
		}

		public PointChain(Point _point, byte _solver, PointChain _prev) {
			point = (_point.x << 16) + _point.y;
			solver = _solver;
			prev_point = _prev.point;
		}

		public Point getPoint() {
			return new Point(point >> 16, point & 0xFFFF);
		}
		
		public Point getPrevPoint() {
			return new Point(prev_point >> 16, prev_point & 0xFFFF);
		}
	}

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
	protected final PointChain[][] visited;

	protected volatile Point meetingPoint = null;

	protected Thread t;

	public LabyrinthParChain(Grid grid) {
		super(grid);
		this.strategyName = "Parallel with linked list";

		// initially all null
		visited = new PointChain[grid.width][grid.height];
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

		PointChain pcStart = new PointChain(grid.start, FROM_START);
		PointChain pcEnd = new PointChain(grid.end, FROM_END);
		visited[grid.start.getX()][grid.start.getY()] = pcStart;
		visited[grid.end.getX()][grid.end.getY()] = pcEnd;

		t = new Thread(() -> subsolve(pcEnd, grid.start, FROM_END, resEnd));
		t.start();

		subsolve(pcStart, grid.end, FROM_START, resStart);

		try {
			t.join();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (!resStart.isEmpty()) {
			return resStart.toArray(new Point[0]);
		} else if (!resEnd.isEmpty()) {
			return resEnd.toArray(new Point[0]);
		}
		System.out.println(meetingPoint);
		return new Point[0];
	}

	public ArrayDeque<Point> subsolve(final PointChain searchStart, final Point searchEnd, final byte subSolveNumber,
			ArrayDeque<Point> pathSoFar) {
		// Used as a stack: Branches not yet taken; solver will backtrack to
		// these branching points later
		ArrayDeque<PointChain> openPointList = new ArrayDeque<PointChain>();

		PointChain current = searchStart;

		Direction lastDirection = null;

		// Continue the search until a meeting point is found
		while (meetingPoint == null) {
			Point currentPoint = current.getPoint();
			PointChain next = null;
			Direction[] dirs = getBiasedDirections(currentPoint, lastDirection, searchEnd);
			for (Direction directionToNeighbor : dirs) {
				Point neighbor = currentPoint.getNeighbor(directionToNeighbor);
				if (hasPassage(currentPoint, neighbor)) {
					synchronized (visited[neighbor.getX()]) {
						PointChain pc = visited[neighbor.getX()][neighbor.getY()];
						// I can visit this cell
						if (pc == null) {
							pc = new PointChain(neighbor, subSolveNumber, current);
							visited[neighbor.getX()][neighbor.getY()] = pc;
							if (next == null) {
								next = pc;
								lastDirection = directionToNeighbor;
							} else {
								openPointList.push(pc);
							}
							// We met, search over!
						} else if (pc.solver != subSolveNumber) {
							synchronized (visited) {
								if (meetingPoint == null) {
									meetingPoint = neighbor;

									if (subSolveNumber == FROM_START) {
										PointChain tmp = pc;
										pc = current;
										current = tmp;
									}

									// path from meetingPoint to the start
									ArrayDeque<Point> pathSoFar2 = new ArrayDeque<Point>();
									Point p = pc.getPoint();
									pathSoFar2.addLast(p);
									do {
										p = pc.getPrevPoint();
										pc = visited[p.x][p.y];
										pathSoFar2.addLast(p);
									} while (pc.prev_point != -1);

									Iterator<Point> it = pathSoFar2.descendingIterator();
									while (it.hasNext()) {
										pathSoFar.addLast(it.next());
									}

									// path from meetingPoint to the end
									Point p2 = current.getPoint();
									pathSoFar.addLast(p2);
									do {
										p2 = current.getPrevPoint();
										current = visited[p2.x][p2.y];
										pathSoFar.addLast(p2);
									} while (current.prev_point != -1);

									return pathSoFar;
								} else {
									return null;
								}
							}
						}
					}
				}
			}
			// Advance to next cell, if any:
			if (next != null) {
				current = next;
				// current has no unvisited neighbor: Backtrack, if possible
			} else {
				// No more backtracking available: No solution exists
				if (openPointList.isEmpty())
					return null;
				current = openPointList.pop();
				lastDirection = null;
			}
		}
		// meetingPoint != null, search over!
		return null;
	}
}