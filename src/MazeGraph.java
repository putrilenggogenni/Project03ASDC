import java.util.*;

class MazeGraph {
    private Cell[][] grid;
    private int rows, cols;
    private Random random;

    public MazeGraph(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Cell[rows][cols];
        this.random = new Random();
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

        // Create entrance and exit
        grid[0][0].topWall = false;
        grid[rows-1][cols-1].bottomWall = false;

        // Assign random terrain types
        assignRandomTerrain();

        // Reset visited for solving
        resetVisited();
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
        // Keep start and end as default
        grid[0][0].terrain = Cell.TerrainType.DEFAULT;
        grid[rows-1][cols-1].terrain = Cell.TerrainType.DEFAULT;
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