import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

class MazeVisualizer extends JPanel {
    private MazeGraph maze;
    private List<Cell> explorationSteps;
    private int currentStep;
    private Timer timer;
    private String currentAlgorithm;
    private int animationSpeed = 5;

    private Map<Cell, Integer> pathColors;
    private List<List<Cell>> allPaths;
    private int[] pathCosts;
    private boolean explorationComplete = false;
    private int pathAnimationStep = 0;
    private List<Cell> currentPathAnimation;

    private static final Color PATH_COLOR_1 = new Color(255, 105, 180);
    private static final Color PATH_COLOR_2 = new Color(64, 156, 255);
    private static final Color PATH_COLOR_3 = new Color(46, 213, 115);

    private static final int CELL_SIZE = 38;
    private static final int MARGIN = 15;
    private static final int WALL_THICKNESS = 3;
    private static final int INFO_PANEL_HEIGHT = 100;

    public MazeVisualizer(MazeGraph maze) {
        this.maze = maze;
        this.currentStep = 0;
        this.currentAlgorithm = "";
        this.pathColors = new HashMap<>();
        this.allPaths = new ArrayList<>();
        this.pathCosts = new int[3];
        setPreferredSize(new Dimension(
                maze.getCols() * CELL_SIZE + 2 * MARGIN,
                maze.getRows() * CELL_SIZE + 2 * MARGIN + INFO_PANEL_HEIGHT
        ));
        setBackground(new Color(245, 245, 250));
    }

    public void setAnimationSpeed(int speed) {
        this.animationSpeed = speed;
        if (timer != null && timer.isRunning()) {
            timer.setDelay(getDelayForSpeed(speed));
        }
    }

    private int getDelayForSpeed(int speed) {
        return Math.max(5, 210 - speed * 20);
    }

    public void animateSolution(List<Cell> steps, String algorithm) {
        this.explorationSteps = steps;
        this.currentStep = 0;
        this.currentAlgorithm = algorithm;
        this.pathColors.clear();
        this.allPaths.clear();
        this.explorationComplete = false;
        this.pathAnimationStep = 0;
        this.currentPathAnimation = new ArrayList<>();
        Arrays.fill(pathCosts, 0);

        if (timer != null) {
            timer.stop();
        }

        timer = new Timer(getDelayForSpeed(animationSpeed), e -> {
            if (currentStep < explorationSteps.size()) {
                currentStep++;
                repaint();
            } else {
                timer.stop();
                explorationComplete = true;
                reconstructAllPathsPostExploration();
                animatePaths();
            }
        });
        timer.start();
    }

    private void reconstructAllPathsPostExploration() {
        List<Cell> finishCells = maze.getFinishCells();

        for (int i = 0; i < finishCells.size(); i++) {
            Cell finish = finishCells.get(i);
            List<Cell> path = findShortestPathToGoal(finish);
            allPaths.add(path);

            int cost = 0;
            for (Cell cell : path) {
                cost += cell.getCost();
            }
            pathCosts[i] = cost;
        }
    }

    private List<Cell> findShortestPathToGoal(Cell goal) {
        Queue<Cell> queue = new LinkedList<>();
        Map<Cell, Cell> parent = new HashMap<>();
        Set<Cell> visited = new HashSet<>();

        Cell start = maze.getCell(0, 0);
        queue.offer(start);
        visited.add(start);
        parent.put(start, null);

        boolean found = false;
        while (!queue.isEmpty() && !found) {
            Cell current = queue.poll();

            if (current == goal) {
                found = true;
                break;
            }

            for (Cell neighbor : maze.getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }

        List<Cell> path = new ArrayList<>();
        if (found) {
            Cell current = goal;
            while (current != null) {
                path.add(0, current);
                current = parent.get(current);
            }
        }

        return path;
    }

    private void animatePaths() {
        currentPathAnimation.clear();

        int maxPathLength = 0;
        for (List<Cell> path : allPaths) {
            maxPathLength = Math.max(maxPathLength, path.size());
        }

        for (int step = 0; step < maxPathLength; step++) {
            for (int pathIndex = 0; pathIndex < allPaths.size(); pathIndex++) {
                List<Cell> path = allPaths.get(pathIndex);
                if (step < path.size()) {
                    Cell cell = path.get(step);
                    if (!pathColors.containsKey(cell)) {
                        pathColors.put(cell, pathIndex);
                        currentPathAnimation.add(cell);
                    }
                }
            }
        }

        pathAnimationStep = 0;

        timer = new Timer(getDelayForSpeed(animationSpeed) * 2, e -> {
            if (pathAnimationStep < currentPathAnimation.size()) {
                pathAnimationStep++;
                repaint();
            } else {
                timer.stop();
            }
        });
        timer.start();
    }

    public void reset() {
        if (timer != null) {
            timer.stop();
        }
        maze.resetVisited();
        explorationSteps = null;
        currentStep = 0;
        currentAlgorithm = "";
        pathColors.clear();
        allPaths.clear();
        Arrays.fill(pathCosts, 0);
        explorationComplete = false;
        pathAnimationStep = 0;
        currentPathAnimation.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawInfoPanel(g2d);

        for (int i = 0; i < maze.getRows(); i++) {
            for (int j = 0; j < maze.getCols(); j++) {
                Cell cell = maze.getCell(i, j);
                int x = MARGIN + j * CELL_SIZE;
                int y = MARGIN + INFO_PANEL_HEIGHT + i * CELL_SIZE;

                RoundRectangle2D cellRect = new RoundRectangle2D.Double(
                        x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4, 10, 10
                );

                boolean isInAnimatedPath = false;
                int cellPathIndex = -1;

                if (explorationComplete && pathColors.containsKey(cell)) {
                    int animIndex = currentPathAnimation.indexOf(cell);
                    if (animIndex >= 0 && animIndex < pathAnimationStep) {
                        isInAnimatedPath = true;
                        cellPathIndex = pathColors.get(cell);
                    }
                }

                if (isInAnimatedPath) {
                    Color pathColor;
                    switch (cellPathIndex) {
                        case 0: pathColor = PATH_COLOR_1; break;
                        case 1: pathColor = PATH_COLOR_2; break;
                        case 2: pathColor = PATH_COLOR_3; break;
                        default: pathColor = new Color(255, 193, 7); break;
                    }

                    GradientPaint gradient = new GradientPaint(
                            x, y, pathColor,
                            x + CELL_SIZE, y + CELL_SIZE, pathColor.darker()
                    );
                    g2d.setPaint(gradient);
                    g2d.fill(cellRect);

                    g2d.setColor(new Color(255, 255, 255, 120));
                    g2d.fillOval(x + 8, y + 8, 6, 6);
                } else if (explorationSteps != null && currentStep > 0 && !explorationComplete) {
                    int index = explorationSteps.indexOf(cell);
                    if (index >= 0 && index < currentStep) {
                        float progress = (float) index / currentStep;
                        int alpha = (int)(150 + 105 * progress);
                        Color exploredColor = new Color(103, 58, 183, alpha);
                        g2d.setColor(exploredColor);
                        g2d.fill(cellRect);

                        if (currentStep - index < 5) {
                            int rippleAlpha = (int)(50 * (5 - (currentStep - index)) / 5.0);
                            g2d.setColor(new Color(103, 58, 183, rippleAlpha));
                            g2d.setStroke(new BasicStroke(2));
                            g2d.draw(new RoundRectangle2D.Double(
                                    x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2, 10, 10
                            ));
                        }
                    } else {
                        g2d.setColor(cell.terrain.color);
                        g2d.fill(cellRect);
                    }
                } else {
                    g2d.setColor(cell.terrain.color);
                    g2d.fill(cellRect);
                }

                if (cell.terrain != Cell.TerrainType.DEFAULT && !isInAnimatedPath) {
                    g2d.setColor(new Color(0, 0, 0, 80));
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    String costStr = String.valueOf(cell.getCost());
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(costStr);
                    g2d.drawString(costStr, x + (CELL_SIZE - textWidth) / 2, y + CELL_SIZE / 2 + 4);
                }

                if (!isInAnimatedPath) {
                    g2d.setColor(new Color(0, 0, 0, 20));
                    g2d.drawRoundRect(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4, 10, 10);
                }

                g2d.setColor(new Color(52, 73, 94));
                g2d.setStroke(new BasicStroke(WALL_THICKNESS, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                if (cell.topWall) g2d.drawLine(x, y, x + CELL_SIZE, y);
                if (cell.rightWall) g2d.drawLine(x + CELL_SIZE, y, x + CELL_SIZE, y + CELL_SIZE);
                if (cell.bottomWall) g2d.drawLine(x, y + CELL_SIZE, x + CELL_SIZE, y + CELL_SIZE);
                if (cell.leftWall) g2d.drawLine(x, y, x, y + CELL_SIZE);
            }
        }

        drawMarker(g2d, MARGIN + CELL_SIZE/2, MARGIN + INFO_PANEL_HEIGHT + CELL_SIZE/2,
                new Color(76, 175, 80), "START");

        List<Cell> finishCells = maze.getFinishCells();
        Color[] finishColors = {PATH_COLOR_1, PATH_COLOR_2, PATH_COLOR_3};

        for (int i = 0; i < finishCells.size(); i++) {
            Cell finish = finishCells.get(i);
            int endX = MARGIN + finish.col * CELL_SIZE + CELL_SIZE/2;
            int endY = MARGIN + INFO_PANEL_HEIGHT + finish.row * CELL_SIZE + CELL_SIZE/2;
            drawMarker(g2d, endX, endY, finishColors[i], "G" + (i + 1));
        }
    }

    private void drawInfoPanel(Graphics2D g2d) {
        GradientPaint bgGradient = new GradientPaint(
                0, 0, new Color(67, 97, 238),
                getWidth(), INFO_PANEL_HEIGHT, new Color(103, 58, 183)
        );
        g2d.setPaint(bgGradient);
        g2d.fillRoundRect(10, 10, getWidth() - 20, INFO_PANEL_HEIGHT - 20, 15, 15);

        g2d.setColor(Color.WHITE);

        if (!currentAlgorithm.isEmpty()) {
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2d.drawString("Algorithm: " + currentAlgorithm, 30, 40);
        }

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
        int yPos = 65;

        if (explorationComplete && pathAnimationStep > 0) {
            g2d.setColor(PATH_COLOR_1);
            g2d.drawString("G1: " + pathCosts[0], 30, yPos);

            g2d.setColor(PATH_COLOR_2);
            g2d.drawString("G2: " + pathCosts[1], 120, yPos);

            g2d.setColor(PATH_COLOR_3);
            g2d.drawString("G3: " + pathCosts[2], 210, yPos);
        } else if (!explorationComplete && explorationSteps != null) {
            g2d.setColor(new Color(129, 212, 250));
            g2d.drawString("Exploring: " + currentStep + " cells", 30, yPos);
        } else {
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawString("Path Costs: ---", 30, yPos);
        }

        if (explorationSteps != null) {
            if (pathAnimationStep >= currentPathAnimation.size() && explorationComplete) {
                g2d.setColor(new Color(76, 175, 80));
                g2d.fillOval(getWidth() - 100, 25, 15, 15);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.drawString("Complete", getWidth() - 80, 37);
            } else if (explorationComplete) {
                g2d.setColor(new Color(255, 193, 7));
                g2d.fillOval(getWidth() - 100, 25, 15, 15);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.drawString("Drawing Paths", getWidth() - 80, 37);
            } else {
                g2d.setColor(new Color(255, 193, 7));
                g2d.fillOval(getWidth() - 100, 25, 15, 15);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.drawString("Exploring", getWidth() - 80, 37);
            }
        }
    }

    private void drawMarker(Graphics2D g2d, int x, int y, Color color, String label) {
        long time = System.currentTimeMillis();
        float pulse = (float)(Math.sin(time / 300.0) * 0.2 + 1.0);

        for (int i = 4; i > 0; i--) {
            int alpha = (int)(25 * i * pulse);
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            int size = 30 + i * 4;
            g2d.fillOval(x - size/2, y - size/2, size, size);
        }

        int mainSize = (int)(28 * pulse);
        GradientPaint gradient = new GradientPaint(
                x - mainSize/2, y - mainSize/2, color.brighter(),
                x + mainSize/2, y + mainSize/2, color
        );
        g2d.setPaint(gradient);
        g2d.fillOval(x - mainSize/2, y - mainSize/2, mainSize, mainSize);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawOval(x - mainSize/2, y - mainSize/2, mainSize, mainSize);

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 9));
        FontMetrics fm = g2d.getFontMetrics();
        int labelWidth = fm.stringWidth(label);

        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(label, x - labelWidth/2 + 1, y + 4);

        g2d.setColor(Color.WHITE);
        g2d.drawString(label, x - labelWidth/2, y + 3);

        Timer pulseTimer = new Timer(50, e -> repaint());
        pulseTimer.setRepeats(false);
        pulseTimer.start();
    }
}