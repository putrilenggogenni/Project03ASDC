import java.util.*;

class BFSSolver extends MazeSolver {

    public BFSSolver(MazeGraph maze) {
        super(maze);
    }

    @Override
    public boolean solve() {
        maze.resetVisited();
        pathSteps.clear();

        Queue<Cell> queue = new LinkedList<>();
        Map<Cell, Cell> parentMap = new HashMap<>();
        Set<Cell> goalsFound = new HashSet<>();
        List<Cell> allFinishCells = maze.getFinishCells();

        Cell start = maze.getCell(0, 0);

        queue.offer(start);
        start.visited = true;
        pathSteps.add(start);
        parentMap.put(start, null);

        // Continue until all goals are found or queue is empty
        while (!queue.isEmpty() && goalsFound.size() < allFinishCells.size()) {
            Cell current = queue.poll();

            // Check if this is a goal
            if (isGoalReached(current)) {
                goalsFound.add(current);
                // Don't stop - continue to find all goals
            }

            for (Cell neighbor : maze.getNeighbors(current)) {
                if (!neighbor.visited) {
                    neighbor.visited = true;
                    queue.offer(neighbor);
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