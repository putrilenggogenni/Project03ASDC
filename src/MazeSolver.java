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

    protected void reconstructPath(Map<Cell, Cell> parentMap, Cell end) {
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

    // NEW: Check if current cell is any finish point
    protected boolean isGoalReached(Cell cell) {
        return maze.isFinishPoint(cell);
    }
}