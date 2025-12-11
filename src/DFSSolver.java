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

        Cell start = maze.getCell(0, 0);
        Cell end = maze.getCell(maze.getRows()-1, maze.getCols()-1);

        stack.push(start);
        start.visited = true;
        pathSteps.add(start);
        parentMap.put(start, null);

        while (!stack.isEmpty()) {
            Cell current = stack.pop();

            if (current == end) {
                reconstructPath(parentMap, end);
                return true;
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

        return false;
    }
}