import java.util.*;

class DijkstraSolver extends MazeSolver {

    public DijkstraSolver(MazeGraph maze) {
        super(maze);
    }

    @Override
    public boolean solve() {
        maze.resetVisited();
        pathSteps.clear();

        PriorityQueue<CellDistance> pq = new PriorityQueue<>(Comparator.comparingInt(cd -> cd.distance));
        Map<Cell, Cell> parentMap = new HashMap<>();
        Map<Cell, Integer> distanceMap = new HashMap<>();

        Cell start = maze.getCell(0, 0);

        pq.offer(new CellDistance(start, 0));
        distanceMap.put(start, 0);
        parentMap.put(start, null);

        while (!pq.isEmpty()) {
            CellDistance current = pq.poll();
            Cell cell = current.cell;

            if (cell.visited) continue;

            cell.visited = true;
            pathSteps.add(cell);

            // NEW: Check if any finish point is reached
            if (isGoalReached(cell)) {
                reconstructPath(parentMap, cell);
                return true;
            }

            for (Cell neighbor : maze.getNeighbors(cell)) {
                if (!neighbor.visited) {
                    int newDist = distanceMap.get(cell) + neighbor.getCost();

                    if (!distanceMap.containsKey(neighbor) || newDist < distanceMap.get(neighbor)) {
                        distanceMap.put(neighbor, newDist);
                        parentMap.put(neighbor, cell);
                        pq.offer(new CellDistance(neighbor, newDist));
                    }
                }
            }
        }

        return false;
    }

    private static class CellDistance {
        Cell cell;
        int distance;

        CellDistance(Cell cell, int distance) {
            this.cell = cell;
            this.distance = distance;
        }
    }
}