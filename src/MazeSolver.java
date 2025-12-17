import java.util.*;

abstract class MazeSolver {
    protected MazeGraph maze;
    protected List<Cell> pathSteps;

    public MazeSolver(MazeGraph maze) {
        this.maze = maze;
        this.pathSteps = new ArrayList<>();
    }

    public abstract boolean solve();

    public List<Cell> getPathSteps() {
        return pathSteps;
    }

    // Modified to find paths to ALL finish points
    protected void reconstructAllPaths(Map<Cell, Cell> parentMap) {
        List<Cell> allFinishCells = maze.getFinishCells();

        for (Cell finish : allFinishCells) {
            reconstructPath(parentMap, finish);
        }
    }

    protected void reconstructPath(Map<Cell, Cell> parentMap, Cell end) {
        if (!parentMap.containsKey(end)) {
            return; // Path not found to this goal
        }

        List<Cell> path = new ArrayList<>();
        Cell current = end;

        while (current != null) {
            path.add(0, current);
            current = parentMap.get(current);
        }

        for (Cell cell : path) {
            cell.inPath = true;
        }
    }

    protected boolean isGoalReached(Cell cell) {
        return maze.isFinishPoint(cell);
    }
}