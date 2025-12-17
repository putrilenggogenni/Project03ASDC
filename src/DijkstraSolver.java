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
        Set<Cell> goalsFound = new HashSet<>();
        List<Cell> allFinishCells = maze.getFinishCells();

        Cell start = maze.getCell(0, 0);

        pq.offer(new CellDistance(start, 0));
        distanceMap.put(start, 0);
        parentMap.put(start, null);

        // Continue until all goals are found or queue is empty
        while (!pq.isEmpty() && goalsFound.size() < allFinishCells.size()) {
            CellDistance current = pq.poll();
            Cell cell = current.cell;

            if (cell.visited) continue;

            cell.visited = true;
            pathSteps.add(cell);

            // Check if this is a goal
            if (isGoalReached(cell)) {
                goalsFound.add(cell);
                // Don't stop - continue to find all goals
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

        // Reconstruct paths to all goals found
        if (!goalsFound.isEmpty()) {
            reconstructAllPaths(parentMap);
            return true;
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