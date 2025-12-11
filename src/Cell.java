import java.awt.Color;

class Cell {
    int row, col;
    boolean topWall, rightWall, bottomWall, leftWall;
    boolean visited;
    boolean inPath;
    TerrainType terrain;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.topWall = true;
        this.rightWall = true;
        this.bottomWall = true;
        this.leftWall = true;
        this.visited = false;
        this.inPath = false;
        this.terrain = TerrainType.DEFAULT;
    }

    public int getCost() {
        return terrain.cost;
    }

    public enum TerrainType {
        DEFAULT(0, new Color(248, 249, 250)),
        GRASS(1, new Color(163, 228, 134)),
        MUD(5, new Color(181, 131, 90)),
        WATER(10, new Color(129, 199, 232));

        final int cost;
        final Color color;

        TerrainType(int cost, Color color) {
            this.cost = cost;
            this.color = color;
        }
    }
}