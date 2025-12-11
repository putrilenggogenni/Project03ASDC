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
        Cell end = maze.getCell(maze.getRows()-1, maze.getCols()-1);

        gScore.put(start, 0);
        pq.offer(new CellNode(start, 0, heuristic(start, end)));
        parentMap.put(start, null);

        while (!pq.isEmpty()) {
            CellNode current = pq.poll();
            Cell cell = current.cell;

            if (cell.visited) continue;

            cell.visited = true;
            pathSteps.add(cell);

            if (cell == end) {
                reconstructPath(parentMap, end);
                return true;
            }

            for (Cell neighbor : maze.getNeighbors(cell)) {
                if (!neighbor.visited) {
                    int tentativeG = gScore.get(cell) + neighbor.getCost();

                    if (!gScore.containsKey(neighbor) || tentativeG < gScore.get(neighbor)) {
                        gScore.put(neighbor, tentativeG);
                        parentMap.put(neighbor, cell);
                        int h = heuristic(neighbor, end);
                        pq.offer(new CellNode(neighbor, tentativeG, tentativeG + h));
                    }
                }
            }
        }

        return false;
    }

    private int heuristic(Cell current, Cell goal) {
        // Manhattan distance
        return Math.abs(current.row - goal.row) + Math.abs(current.col - goal.col);
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