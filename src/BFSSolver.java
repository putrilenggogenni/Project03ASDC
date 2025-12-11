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

        Cell start = maze.getCell(0, 0);
        Cell end = maze.getCell(maze.getRows()-1, maze.getCols()-1);

        queue.offer(start);
        start.visited = true;
        pathSteps.add(start);
        parentMap.put(start, null);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();

            if (current == end) {
                reconstructPath(parentMap, end);
                return true;
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

        return false;
    }
}