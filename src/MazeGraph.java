import java.util.*;

class MazeGraph {
    private Cell[][] grid;
    private int rows, cols;
    private Random random;
    private List<Cell> finishCells;

    public MazeGraph(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Cell[rows][cols];
        this.random = new Random();
        this.finishCells = new ArrayList<>();
        initializeGrid();
    }

    private void initializeGrid() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new Cell(i, j);
            }
        }
    }

    public void generateMazeWithPrim() {
        // Start from top-left corner
        Cell start = grid[0][0];
        start.visited = true;

        List<Wall> walls = new ArrayList<>();
        addWallsToList(start, walls);

        while (!walls.isEmpty()) {
            // Pick random wall
            int index = random.nextInt(walls.size());
            Wall wall = walls.remove(index);

            Cell cell1 = grid[wall.row1][wall.col1];
            Cell cell2 = grid[wall.row2][wall.col2];

            // If only one cell is visited, remove wall
            if (cell1.visited != cell2.visited) {
                removeWall(wall);

                Cell unvisited = cell1.visited ? cell2 : cell1;
                unvisited.visited = true;
                addWallsToList(unvisited, walls);
            }
        }

        // Create entrance at start
        grid[0][0].topWall = false;

        // Setup three random finish points
        setupRandomFinishPoints();

        // Assign random terrain types
        assignRandomTerrain();

        // Reset visited for solving
        resetVisited();
    }

    // NEW: Randomly place three finish points at different locations
    private void setupRandomFinishPoints() {
        finishCells.clear();
        Set<String> usedPositions = new HashSet<>();

        // Start position is reserved
        usedPositions.add("0,0");

        // Find all reachable cells (cells that have at least one open wall)
        List<Cell> candidates = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = grid[i][j];
                // Exclude start position
                if (i == 0 && j == 0) continue;

                // Cell must have at least one open wall to be reachable
                if (!cell.topWall || !cell.bottomWall || !cell.leftWall || !cell.rightWall) {
                    candidates.add(cell);
                }
            }
        }

        // Shuffle candidates for randomness
        Collections.shuffle(candidates, random);

        // Prefer edge cells for finish points (easier to visualize)
        List<Cell> edgeCandidates = new ArrayList<>();
        List<Cell> innerCandidates = new ArrayList<>();

        for (Cell candidate : candidates) {
            if (candidate.row == 0 || candidate.row == rows - 1 ||
                    candidate.col == 0 || candidate.col == cols - 1) {
                edgeCandidates.add(candidate);
            } else {
                innerCandidates.add(candidate);
            }
        }

        // Try to select from edge cells first, then inner cells if needed
        List<Cell> selectionPool = new ArrayList<>(edgeCandidates);
        selectionPool.addAll(innerCandidates);

        // Select three distinct finish points
        int finishCount = 0;
        for (Cell candidate : selectionPool) {
            if (finishCount >= 3) break;

            String pos = candidate.row + "," + candidate.col;
            if (!usedPositions.contains(pos)) {
                finishCells.add(candidate);
                usedPositions.add(pos);
                finishCount++;

                // Create exit opening for finish nodes at edges
                if (candidate.row == 0) {
                    candidate.topWall = false;
                } else if (candidate.row == rows - 1) {
                    candidate.bottomWall = false;
                }
                if (candidate.col == 0) {
                    candidate.leftWall = false;
                } else if (candidate.col == cols - 1) {
                    candidate.rightWall = false;
                }
            }
        }

        // Fallback: if we couldn't find 3 distinct positions, force some positions
        while (finishCells.size() < 3) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);
            String pos = row + "," + col;

            if (!usedPositions.contains(pos)) {
                Cell cell = grid[row][col];
                finishCells.add(cell);
                usedPositions.add(pos);

                // Create exit
                if (row == rows - 1) {
                    cell.bottomWall = false;
                } else if (col == cols - 1) {
                    cell.rightWall = false;
                }
            }
        }
    }

    public boolean isFinishPoint(Cell cell) {
        return finishCells.contains(cell);
    }

    public List<Cell> getFinishCells() {
        return finishCells;
    }

    private void assignRandomTerrain() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // 40% default, 30% grass, 20% mud, 10% water
                int rand = random.nextInt(100);
                if (rand < 40) {
                    grid[i][j].terrain = Cell.TerrainType.DEFAULT;
                } else if (rand < 70) {
                    grid[i][j].terrain = Cell.TerrainType.GRASS;
                } else if (rand < 90) {
                    grid[i][j].terrain = Cell.TerrainType.MUD;
                } else {
                    grid[i][j].terrain = Cell.TerrainType.WATER;
                }
            }
        }
        // Keep start and all finish points as default
        grid[0][0].terrain = Cell.TerrainType.DEFAULT;
        for (Cell finish : finishCells) {
            finish.terrain = Cell.TerrainType.DEFAULT;
        }
    }

    private void addWallsToList(Cell cell, List<Wall> walls) {
        int r = cell.row;
        int c = cell.col;

        if (r > 0) walls.add(new Wall(r, c, r-1, c, "top"));
        if (r < rows-1) walls.add(new Wall(r, c, r+1, c, "bottom"));
        if (c > 0) walls.add(new Wall(r, c, r, c-1, "left"));
        if (c < cols-1) walls.add(new Wall(r, c, r, c+1, "right"));
    }

    private void removeWall(Wall wall) {
        Cell cell1 = grid[wall.row1][wall.col1];
        Cell cell2 = grid[wall.row2][wall.col2];

        if (wall.direction.equals("top")) {
            cell1.topWall = false;
            cell2.bottomWall = false;
        } else if (wall.direction.equals("bottom")) {
            cell1.bottomWall = false;
            cell2.topWall = false;
        } else if (wall.direction.equals("left")) {
            cell1.leftWall = false;
            cell2.rightWall = false;
        } else if (wall.direction.equals("right")) {
            cell1.rightWall = false;
            cell2.leftWall = false;
        }
    }

    public List<Cell> getNeighbors(Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        int r = cell.row;
        int c = cell.col;

        if (r > 0 && !cell.topWall) neighbors.add(grid[r-1][c]);
        if (r < rows-1 && !cell.bottomWall) neighbors.add(grid[r+1][c]);
        if (c > 0 && !cell.leftWall) neighbors.add(grid[r][c-1]);
        if (c < cols-1 && !cell.rightWall) neighbors.add(grid[r][c+1]);

        return neighbors;
    }

    public void resetVisited() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j].visited = false;
                grid[i][j].inPath = false;
            }
        }
    }

    public Cell getCell(int row, int col) {
        return grid[row][col];
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    private class Wall {
        int row1, col1, row2, col2;
        String direction;

        Wall(int r1, int c1, int r2, int c2, String dir) {
            this.row1 = r1;
            this.col1 = c1;
            this.row2 = r2;
            this.col2 = c2;
            this.direction = dir;
        }
    }
}