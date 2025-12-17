import java.util.*;

class DFSSolver extends MazeSolver {

    public DFSSolver(MazeGraph maze) {
        super(maze);
    }

    @Override
    public boolean solve() {
        maze.resetVisited();
        pathSteps.clear();

        Stack<Cell> stack = new Stack<>();
        Map<Cell, Cell> parentMap = new HashMap<>();
        Set<Cell> goalsFound = new HashSet<>();
        List<Cell> allFinishCells = maze.getFinishCells();

        Cell start = maze.getCell(0, 0);

        stack.push(start);
        start.visited = true;
        pathSteps.add(start);
        parentMap.put(start, null);

        // Continue until all goals are found or stack is empty
        while (!stack.isEmpty() && goalsFound.size() < allFinishCells.size()) {
            Cell current = stack.pop();

            // Check if this is a goal
            if (isGoalReached(current)) {
                goalsFound.add(current);
                // Don't stop - continue to find all goals
            }

            for (Cell neighbor : maze.getNeighbors(current)) {
                if (!neighbor.visited) {
                    neighbor.visited = true;
                    stack.push(neighbor);
                    parentMap.put(neighbor, current);
                    pathSteps.add(neighbor);
                }
            }
        }

        // Reconstruct paths to all goals found
        if (!goalsFound.isEmpty()) {
            reconstructAllPaths(parentMap);
            return true;
        }

        return false;
    }
}