package uebung_parallelisierung.parallel;
 
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
 
public class LabyrinthCreator {
    // When generating the labyrinth and considering whether to create a passage
    // to some neighbor cell, create a
    // passage to a cell already that is accessible on another path (i.e. create
    // a cycle) with this probability:
    private static final double CYCLE_CREATION_PROBABILITY = 0;//0.01;
 
    /**
     * Generate a labyrinth (with or without cycles, depending on
     * CYCLE_CREATION_PROBABILITY) using the depth-first algorithm
     * (www.astrolog.org/labyrnth/algrithm.htm (sic!))
     */
    private static Grid generate(Grid grid) {
        ArrayDeque<Point> pointsToDo = new ArrayDeque<Point>();
        Point current;
        pointsToDo.push(grid.start);
        while (!pointsToDo.isEmpty()) {
            current = pointsToDo.pop();
            int cx = current.getX();
            int cy = current.getY();
            Direction[] dirs = Direction.values();
            Collections.shuffle(Arrays.asList(dirs));
            // For all unvisited neighboring cells in random order:
            // Make a passage from the current cell to that neighbor
            for (Direction dir : dirs) {
                // Pick random neighbor of current cell as new cell (nx, ny)
                Point neighbor = current.getNeighbor(dir);
                int nx = neighbor.getX();
                int ny = neighbor.getY();
 
                if (grid.contains(neighbor) // If neighbor is still in the labyrinth
                                        // ...
                        && (grid.passages[nx][ny] == 0 // ... and has no passage
                                                        // yet, i.e. has not
                                                        // been visited yet
                                                        // during generation
                                || Math.random() < CYCLE_CREATION_PROBABILITY)) { // ...
                                                                                    // or
                                                                                    // creating
                                                                                    // a
                                                                                    // cycle
                                                                                    // is
                                                                                    // OK
 
                    // Make a two-way passage, i.e. from current to neighbor and
                    // from neighbor to current:
                    grid.passages[cx][cy] |= dir.bit;
                    grid.passages[nx][ny] |= dir.opposite.bit;
 
                    // Remember to continue from this neighbor later on
                    pointsToDo.push(neighbor);
                }
            }
        }
        return grid;
    }
 
    public static Grid createGrid(int width, int height) {
        return createGrid(width, height, "grid");
    }
 
    public static Grid createGrid(int width, int height, String name) {
        // Create new, random labyrinth:
        Point start = new Point(width / 2, height / 2);
 
        // Randomly pick one of the four corners as the end point:
        int zeroToThree = (int) (4.0 * Math.random());
        Point end = new Point(zeroToThree / 2 == 0 ? 0 : width - 1, zeroToThree % 2 == 0 ? 0 : height - 1);
 
        Grid grid = new Grid(width, height, start, end);
        grid = generate(grid);
        // Save to file (may be reused in future program executions):
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(name + ".ser"));
            oos.writeObject(grid);
            oos.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return grid;
    }
 
    public static Grid loadGrid(String name) {
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(new FileInputStream(name + ".ser"));
            Grid grid = (Grid) ois.readObject();
            ois.close();
            return grid;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}