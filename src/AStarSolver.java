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
        Map<Cell, Cell> parentMap = new HashMap<>();
        Map<Cell, Integer> gScore = new HashMap<>();

        Cell start = maze.getCell(0, 0);

        gScore.put(start, 0);
        // NEW: Calculate heuristic to nearest finish point
        pq.offer(new CellNode(start, 0, heuristicToNearestGoal(start)));
        parentMap.put(start, null);

        while (!pq.isEmpty()) {
            CellNode current = pq.poll();
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
                    int tentativeG = gScore.get(cell) + neighbor.getCost();

                    if (!gScore.containsKey(neighbor) || tentativeG < gScore.get(neighbor)) {
                        gScore.put(neighbor, tentativeG);
                        parentMap.put(neighbor, cell);
                        // NEW: Calculate heuristic to nearest finish point
                        int h = heuristicToNearestGoal(neighbor);
                        pq.offer(new CellNode(neighbor, tentativeG, tentativeG + h));
                    }
                }
            }
        }

        return false;
    }

    // NEW: Calculate Manhattan distance to nearest goal
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