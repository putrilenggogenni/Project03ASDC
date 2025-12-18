import java.util.*;

class AStarSolver extends MazeSolver {

    public AStarSolver(MazeGraph maze) {
        super(maze);
    }

    @Override
    public boolean solve() {
        maze.resetVisited();
        pathSteps.clear();

        PriorityQueue<CellNode> pq = new PriorityQueue<>(Comparator.comparingInt(cn -> cn.fScore));
        Map<Cell, Integer> gScore = new HashMap<>();
        Map<Cell, Cell> parentMap = new HashMap<>();
        Set<Cell> goalsFound = new HashSet<>();
        List<Cell> allFinishCells = maze.getFinishCells();

        Cell start = maze.getCell(0, 0);

        gScore.put(start, 0);
        parentMap.put(start, null);
        pq.offer(new CellNode(start, 0, heuristicToNearestGoal(start)));

        // Continue until all goals are found or queue is empty
        while (!pq.isEmpty() && goalsFound.size() < allFinishCells.size()) {
            CellNode current = pq.poll();
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
                    int tentativeG = gScore.get(cell) + neighbor.getCost();

                    if (!gScore.containsKey(neighbor) || tentativeG < gScore.get(neighbor)) {
                        gScore.put(neighbor, tentativeG);
                        parentMap.put(neighbor, cell);
                        int h = heuristicToNearestGoal(neighbor);
                        pq.offer(new CellNode(neighbor, tentativeG, tentativeG + h));
                    }
                }
            }
        }

        // Reconstruct paths to all goals found
        if (!goalsFound.isEmpty()) {
            for (Cell goal : goalsFound) {
                reconstructPath(parentMap, goal);
            }
            return true;
        }

        return false;
    }

    private int heuristicToNearestGoal(Cell current) {
        int minDistance = Integer.MAX_VALUE;
        for (Cell goal : maze.getFinishCells()) {
            int distance = Math.abs(current.row - goal.row) + Math.abs(current.col - goal.col);
            minDistance = Math.min(minDistance, distance);
        }
        return minDistance;
    }

    private static class CellNode {
        Cell cell;
        int gScore;
        int fScore;

        CellNode(Cell cell, int gScore, int fScore) {
            this.cell = cell;
            this.gScore = gScore;
            this.fScore = fScore;
        }
    }
}