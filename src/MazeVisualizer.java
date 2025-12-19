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

    // Post-exploration path reconstruction
    private List<List<Cell>> allGoalPaths;
    private int pathAnimationStep = 0;
    private boolean explorationComplete = false;

    private List<Particle> particles = new ArrayList<>();
    private Random random = new Random();
    private Timer particleTimer;
    private Map<Cell, Float> cellGlow = new HashMap<>();
    private long animationStartTime;
    private boolean isPaused = false;
    private List<TrailPoint> trails = new ArrayList<>();
    private String currentTheme = "neon";
    private Color themeColor1, themeColor2, themeColor3, themeBg;

    private static final Color PATH_COLOR_1 = new Color(255, 105, 180);
    private static final Color PATH_COLOR_2 = new Color(64, 156, 255);
    private static final Color PATH_COLOR_3 = new Color(46, 213, 115);

    private static final int CELL_SIZE = 38;
    private static final int MARGIN = 15;
    private static final int WALL_THICKNESS = 3;
    private static final int INFO_PANEL_HEIGHT = 90;

    public MazeVisualizer(MazeGraph maze) {
        this.maze = maze;
        this.currentStep = 0;
        this.currentAlgorithm = "";
        this.allGoalPaths = new ArrayList<>();
        setPreferredSize(new Dimension(
                maze.getCols() * CELL_SIZE + 2 * MARGIN,
                maze.getRows() * CELL_SIZE + 2 * MARGIN + INFO_PANEL_HEIGHT
        ));
        updateThemeColors();

        particleTimer = new Timer(30, e -> {
            updateParticles();
            updateTrails();
            repaint();
        });
        particleTimer.start();
    }

    public void setTheme(String theme) {
        this.currentTheme = theme;
        updateThemeColors();
        repaint();
    }

    private void updateThemeColors() {
        switch (currentTheme) {
            case "ocean":
                themeColor1 = new Color(14, 165, 233);
                themeColor2 = new Color(34, 211, 238);
                themeColor3 = new Color(6, 182, 212);
                themeBg = new Color(15, 23, 42);
                break;
            case "forest":
                themeColor1 = new Color(34, 197, 94);
                themeColor2 = new Color(74, 222, 128);
                themeColor3 = new Color(134, 239, 172);
                themeBg = new Color(20, 30, 25);
                break;
            case "sunset":
                themeColor1 = new Color(249, 115, 22);
                themeColor2 = new Color(251, 146, 60);
                themeColor3 = new Color(253, 186, 116);
                themeBg = new Color(30, 20, 25);
                break;
            default:
                themeColor1 = new Color(147, 51, 234);
                themeColor2 = new Color(168, 85, 247);
                themeColor3 = new Color(192, 132, 252);
                themeBg = new Color(20, 20, 35);
                break;
        }
        setBackground(themeBg);
    }

    public void pause() {
        isPaused = true;
        if (timer != null) timer.stop();
    }

    public void play() {
        isPaused = false;
        if (timer != null && !timer.isRunning() && currentStep < explorationSteps.size()) {
            timer.start();
        }
    }

    public void step() {
        if (explorationSteps != null && currentStep < explorationSteps.size()) {
            Cell cell = explorationSteps.get(currentStep);
            cellGlow.put(cell, 1.0f);
            addExplosionParticles(cell, themeColor2);
            addTrail(cell);
            currentStep++;
            repaint();
        }
    }

    public boolean isAnimating() {
        return explorationSteps != null && currentStep > 0;
    }

    public boolean isComplete() {
        return explorationComplete && pathAnimationStep >= getTotalPathSteps();
    }

    private int getTotalPathSteps() {
        int total = 0;
        for (List<Cell> path : allGoalPaths) {
            total += path.size();
        }
        return total;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public long getElapsedTime() {
        if (explorationSteps == null) return 0;
        return System.currentTimeMillis() - animationStartTime;
    }

    public int getBestPathCost() {
        int minCost = Integer.MAX_VALUE;
        for (List<Cell> path : allGoalPaths) {
            int cost = 0;
            for (Cell cell : path) {
                cost += cell.getCost();
            }
            if (cost > 0 && cost < minCost) minCost = cost;
        }
        return minCost == Integer.MAX_VALUE ? 0 : minCost;
    }

    public void setAnimationSpeed(int speed) {
        this.animationSpeed = speed;
        if (timer != null && timer.isRunning()) {
            timer.setDelay(getDelayForSpeed(speed));
        }
        if (particleTimer != null) {
            particleTimer.setDelay(Math.max(10, 50 - speed * 4));
        }
    }

    private int getDelayForSpeed(int speed) {
        return Math.max(5, 210 - speed * 20);
    }

    public void animateSolution(List<Cell> steps, String algorithm) {
        this.explorationSteps = steps;
        this.currentStep = 0;
        this.currentAlgorithm = algorithm;
        this.allGoalPaths.clear();
        this.explorationComplete = false;
        this.pathAnimationStep = 0;
        this.cellGlow.clear();
        this.particles.clear();
        this.trails.clear();
        this.animationStartTime = System.currentTimeMillis();

        if (timer != null) timer.stop();

        // Phase 1: Exploration animation
        timer = new Timer(getDelayForSpeed(animationSpeed), e -> {
            if (!isPaused && currentStep < explorationSteps.size()) {
                Cell cell = explorationSteps.get(currentStep);
                cellGlow.put(cell, 1.0f);
                addExplosionParticles(cell, themeColor2);
                addTrail(cell);
                currentStep++;
                repaint();
            } else if (currentStep >= explorationSteps.size()) {
                timer.stop();
                explorationComplete = true;
                // Phase 2: Post-exploration shortest path determination
                reconstructShortestPathsToAllGoals();
                animateAllPaths();
            }
        });
        timer.start();
    }

    // NEW: Post-exploration shortest path reconstruction
    private void reconstructShortestPathsToAllGoals() {
        allGoalPaths.clear();
        List<Cell> finishCells = maze.getFinishCells();

        for (Cell goal : finishCells) {
            List<Cell> path = findShortestPathBFS(goal);
            if (path != null && !path.isEmpty()) {
                allGoalPaths.add(path);
            }
        }
    }

    private List<Cell> findShortestPathBFS(Cell goal) {
        Queue<Cell> queue = new LinkedList<>();
        Map<Cell, Cell> parent = new HashMap<>();
        Set<Cell> visited = new HashSet<>();

        Cell start = maze.getCell(0, 0);
        queue.offer(start);
        visited.add(start);
        parent.put(start, null);

        while (!queue.isEmpty()) {
            Cell current = queue.poll();

            if (current == goal) {
                // Reconstruct path
                List<Cell> path = new ArrayList<>();
                Cell c = goal;
                while (c != null) {
                    path.add(0, c);
                    c = parent.get(c);
                }
                return path;
            }

            for (Cell neighbor : maze.getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }

        return new ArrayList<>();
    }

    // Phase 3: Animate all paths step-by-step
    private void animateAllPaths() {
        pathAnimationStep = 0;

        timer = new Timer(getDelayForSpeed(animationSpeed) * 2, e -> {
            if (pathAnimationStep < getTotalPathSteps()) {
                // Animate one step across all paths
                int stepCount = 0;
                for (int i = 0; i < allGoalPaths.size(); i++) {
                    List<Cell> path = allGoalPaths.get(i);
                    int localStep = pathAnimationStep - stepCount;

                    if (localStep >= 0 && localStep < path.size()) {
                        Cell cell = path.get(localStep);
                        Color pathColor = getPathColor(i);
                        addExplosionParticles(cell, pathColor);
                    }
                    stepCount += path.size();
                }

                pathAnimationStep++;
                repaint();
            } else {
                timer.stop();
                // Celebration
                for (Cell finish : maze.getFinishCells()) {
                    addCelebrationParticles(finish);
                }
                SoundManager.getInstance().playSuccess();
            }
        });
        timer.start();
    }

    private Color getPathColor(int pathIndex) {
        Color[] colors = {PATH_COLOR_1, PATH_COLOR_2, PATH_COLOR_3};
        return colors[pathIndex % 3];
    }

    private void addExplosionParticles(Cell cell, Color color) {
        int x = MARGIN + cell.col * CELL_SIZE + CELL_SIZE / 2;
        int y = MARGIN + INFO_PANEL_HEIGHT + cell.row * CELL_SIZE + CELL_SIZE / 2;

        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2 * i) / 8;
            double speed = 1 + random.nextDouble() * 2;
            particles.add(new Particle(x, y, angle, speed, color));
        }
    }

    private void addTrail(Cell cell) {
        int x = MARGIN + cell.col * CELL_SIZE + CELL_SIZE / 2;
        int y = MARGIN + INFO_PANEL_HEIGHT + cell.row * CELL_SIZE + CELL_SIZE / 2;
        trails.add(new TrailPoint(x, y, themeColor1));
    }

    private void updateTrails() {
        trails.removeIf(t -> { t.update(); return t.isDead(); });
    }

    private void addCelebrationParticles(Cell cell) {
        int x = MARGIN + cell.col * CELL_SIZE + CELL_SIZE / 2;
        int y = MARGIN + INFO_PANEL_HEIGHT + cell.row * CELL_SIZE + CELL_SIZE / 2;

        for (int i = 0; i < 30; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 2 + random.nextDouble() * 4;
            Color color = new Color(
                    random.nextInt(100) + 155,
                    random.nextInt(100) + 155,
                    random.nextInt(100) + 155
            );
            particles.add(new Particle(x, y, angle, speed, color));
        }
    }

    private void updateParticles() {
        cellGlow.entrySet().removeIf(entry -> {
            float glow = entry.getValue() - 0.05f;
            if (glow <= 0) return true;
            entry.setValue(glow);
            return false;
        });

        particles.removeIf(p -> { p.update(); return p.isDead(); });
    }

    public void reset() {
        if (timer != null) timer.stop();
        maze.resetVisited();
        explorationSteps = null;
        currentStep = 0;
        currentAlgorithm = "";
        allGoalPaths.clear();
        explorationComplete = false;
        pathAnimationStep = 0;
        cellGlow.clear();
        particles.clear();
        trails.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawInfoPanel(g2d);
        drawMaze(g2d);
        drawPaths(g2d);
        drawTrails(g2d);
        drawParticles(g2d);
        drawMarkers(g2d);
    }

    private void drawPaths(Graphics2D g2d) {
        if (!explorationComplete || allGoalPaths.isEmpty()) return;

        int cumulativeStep = 0;
        for (int pathIndex = 0; pathIndex < allGoalPaths.size(); pathIndex++) {
            List<Cell> path = allGoalPaths.get(pathIndex);
            Color pathColor = getPathColor(pathIndex);

            int visibleSteps = Math.min(path.size(), Math.max(0, pathAnimationStep - cumulativeStep));

            // Draw path line
            g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < visibleSteps - 1; i++) {
                Cell c1 = path.get(i);
                Cell c2 = path.get(i + 1);
                int x1 = MARGIN + c1.col * CELL_SIZE + CELL_SIZE / 2;
                int y1 = MARGIN + INFO_PANEL_HEIGHT + c1.row * CELL_SIZE + CELL_SIZE / 2;
                int x2 = MARGIN + c2.col * CELL_SIZE + CELL_SIZE / 2;
                int y2 = MARGIN + INFO_PANEL_HEIGHT + c2.row * CELL_SIZE + CELL_SIZE / 2;

                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.drawLine(x1 + 2, y1 + 2, x2 + 2, y2 + 2);
                g2d.setColor(pathColor);
                g2d.drawLine(x1, y1, x2, y2);
            }

            // Draw node markers
            for (int i = 1; i < visibleSteps; i++) {
                Cell cell = path.get(i);
                int x = MARGIN + cell.col * CELL_SIZE + CELL_SIZE / 2;
                int y = MARGIN + INFO_PANEL_HEIGHT + cell.row * CELL_SIZE + CELL_SIZE / 2;

                g2d.setColor(pathColor);
                g2d.fillOval(x - 6, y - 6, 12, 12);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(x - 6, y - 6, 12, 12);

                g2d.setFont(new Font("Segoe UI", Font.BOLD, 8));
                String num = String.valueOf(i);
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(num, x - fm.stringWidth(num)/2, y + 3);
            }

            cumulativeStep += path.size();
        }
    }

    private void drawTrails(Graphics2D g2d) {
        for (TrailPoint trail : trails) {
            trail.draw(g2d);
        }
    }

    private void drawMaze(Graphics2D g2d) {
        long currentTime = System.currentTimeMillis();

        for (int i = 0; i < maze.getRows(); i++) {
            for (int j = 0; j < maze.getCols(); j++) {
                Cell cell = maze.getCell(i, j);
                int x = MARGIN + j * CELL_SIZE;
                int y = MARGIN + INFO_PANEL_HEIGHT + i * CELL_SIZE;

                RoundRectangle2D cellRect = new RoundRectangle2D.Double(
                        x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4, 10, 10
                );

                // Check if in any animated path
                boolean inPath = false;
                if (explorationComplete) {
                    int stepCount = 0;
                    for (List<Cell> path : allGoalPaths) {
                        int localStep = pathAnimationStep - stepCount;
                        if (localStep > 0 && localStep < path.size() && path.subList(0, localStep).contains(cell)) {
                            inPath = true;
                            break;
                        }
                        stepCount += path.size();
                    }
                }

                if (!inPath && explorationSteps != null && currentStep > 0 && !explorationComplete) {
                    int index = explorationSteps.indexOf(cell);
                    if (index >= 0 && index < currentStep) {
                        float progress = (float) index / currentStep;
                        int baseAlpha = (int)(150 + 105 * progress);
                        float pulse = (float)(Math.sin((currentTime - animationStartTime) / 200.0 + index * 0.1) * 0.3 + 0.7);
                        int alpha = (int)(baseAlpha * pulse);

                        g2d.setColor(new Color(103, 58, 183, Math.min(255, alpha)));
                        g2d.fill(cellRect);

                        if (cellGlow.containsKey(cell)) {
                            float glow = cellGlow.get(cell);
                            g2d.setColor(new Color(200, 150, 255, (int)(glow * 180)));
                            g2d.setStroke(new BasicStroke(3));
                            g2d.draw(new RoundRectangle2D.Double(x, y, CELL_SIZE, CELL_SIZE, 12, 12));
                        }
                    } else {
                        drawTerrainCell(g2d, cell, x, y, cellRect, currentTime);
                    }
                } else {
                    drawTerrainCell(g2d, cell, x, y, cellRect, currentTime);
                }

                // Cost display
                if (cell.terrain != Cell.TerrainType.DEFAULT && !inPath) {
                    g2d.setColor(new Color(0, 0, 0, 180));
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    String costStr = String.valueOf(cell.getCost());
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString(costStr, x + (CELL_SIZE - fm.stringWidth(costStr)) / 2, y + CELL_SIZE / 2 + 3);
                }

                drawWalls(g2d, cell, x, y);
            }
        }
    }

    private void drawTerrainCell(Graphics2D g2d, Cell cell, int x, int y, RoundRectangle2D cellRect, long time) {
        Color baseColor = cell.terrain.color;
        float pulse = (float)(Math.sin(time / 1000.0 + (cell.row + cell.col) * 0.3) * 0.1 + 0.9);
        Color animated = new Color(
                (int)(baseColor.getRed() * pulse),
                (int)(baseColor.getGreen() * pulse),
                (int)(baseColor.getBlue() * pulse)
        );
        g2d.setColor(animated);
        g2d.fill(cellRect);
    }

    private void drawWalls(Graphics2D g2d, Cell cell, int x, int y) {
        g2d.setColor(new Color(100, 200, 255, 40));
        g2d.setStroke(new BasicStroke(WALL_THICKNESS + 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (cell.topWall) g2d.drawLine(x, y, x + CELL_SIZE, y);
        if (cell.rightWall) g2d.drawLine(x + CELL_SIZE, y, x + CELL_SIZE, y + CELL_SIZE);
        if (cell.bottomWall) g2d.drawLine(x, y + CELL_SIZE, x + CELL_SIZE, y + CELL_SIZE);
        if (cell.leftWall) g2d.drawLine(x, y, x, y + CELL_SIZE);

        g2d.setColor(new Color(120, 180, 255));
        g2d.setStroke(new BasicStroke(WALL_THICKNESS, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (cell.topWall) g2d.drawLine(x, y, x + CELL_SIZE, y);
        if (cell.rightWall) g2d.drawLine(x + CELL_SIZE, y, x + CELL_SIZE, y + CELL_SIZE);
        if (cell.bottomWall) g2d.drawLine(x, y + CELL_SIZE, x + CELL_SIZE, y + CELL_SIZE);
        if (cell.leftWall) g2d.drawLine(x, y, x, y + CELL_SIZE);
    }

    private void drawParticles(Graphics2D g2d) {
        for (Particle p : particles) {
            p.draw(g2d);
        }
    }

    private void drawMarkers(Graphics2D g2d) {
        drawMarker(g2d, MARGIN + CELL_SIZE/2, MARGIN + INFO_PANEL_HEIGHT + CELL_SIZE/2,
                new Color(76, 175, 80), "S");

        List<Cell> finishCells = maze.getFinishCells();
        Color[] colors = {PATH_COLOR_1, PATH_COLOR_2, PATH_COLOR_3};

        for (int i = 0; i < finishCells.size(); i++) {
            Cell finish = finishCells.get(i);
            int x = MARGIN + finish.col * CELL_SIZE + CELL_SIZE/2;
            int y = MARGIN + INFO_PANEL_HEIGHT + finish.row * CELL_SIZE + CELL_SIZE/2;
            drawMarker(g2d, x, y, colors[i], "G" + (i + 1));
        }
    }

    private void drawInfoPanel(Graphics2D g2d) {
        long time = System.currentTimeMillis();
        float shift = (float)((time / 50.0) % 100) / 100.0f;

        GradientPaint bg = new GradientPaint(
                shift * getWidth(), 0, new Color(67, 97, 238),
                getWidth() * (1 - shift), INFO_PANEL_HEIGHT, new Color(103, 58, 183)
        );
        g2d.setPaint(bg);
        g2d.fillRoundRect(10, 10, getWidth() - 20, INFO_PANEL_HEIGHT - 20, 15, 15);

        g2d.setColor(new Color(150, 150, 255, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(10, 10, getWidth() - 20, INFO_PANEL_HEIGHT - 20, 15, 15);

        g2d.setColor(Color.WHITE);
        if (!currentAlgorithm.isEmpty()) {
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2d.drawString("Algorithm: " + currentAlgorithm, 25, 35);
        }

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
        if (explorationComplete && !allGoalPaths.isEmpty()) {
            for (int i = 0; i < allGoalPaths.size(); i++) {
                int cost = 0;
                for (Cell c : allGoalPaths.get(i)) cost += c.getCost();
                Color pc = getPathColor(i);
                g2d.setColor(pc.brighter());
                g2d.drawString("G" + (i+1) + ": " + cost, 25 + i * 70, 60);
            }
        } else if (!explorationComplete && explorationSteps != null) {
            g2d.setColor(new Color(129, 212, 250));
            g2d.drawString("Exploring: " + currentStep, 25, 60);
        }

        drawStatusIndicator(g2d);
    }

    private void drawStatusIndicator(Graphics2D g2d) {
        int x = getWidth() - 90;
        int y = 20;

        if (explorationSteps != null) {
            if (isComplete()) {
                g2d.setColor(new Color(76, 175, 80));
                g2d.fillOval(x, y, 12, 12);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2d.drawString("Done", x + 18, y + 10);
            } else if (explorationComplete) {
                g2d.setColor(new Color(255, 193, 7));
                g2d.fillOval(x, y, 12, 12);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2d.drawString("Paths", x + 18, y + 10);
            } else {
                long time = System.currentTimeMillis();
                double angle = (time % 1000) / 1000.0 * Math.PI * 2;
                g2d.setColor(new Color(255, 193, 7));
                g2d.fillOval(x, y, 12, 12);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(x + 6, y + 6, x + 6 + (int)(Math.cos(angle) * 4), y + 6 + (int)(Math.sin(angle) * 4));
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2d.drawString("Search", x + 18, y + 10);
            }
        }
    }

    private void drawMarker(Graphics2D g2d, int x, int y, Color color, String label) {
        long time = System.currentTimeMillis();
        float pulse = (float)(Math.sin(time / 300.0) * 0.2 + 1.0);

        for (int i = 5; i > 0; i--) {
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(20 * i * pulse)));
            int size = 30 + i * 5;
            g2d.fillOval(x - size/2, y - size/2, size, size);
        }

        int mainSize = (int)(28 * pulse);
        GradientPaint gradient = new GradientPaint(
                x - mainSize/2, y - mainSize/2, color.brighter(),
                x + mainSize/2, y + mainSize/2, color
        );
        g2d.setPaint(gradient);
        g2d.fillOval(x - mainSize/2, y - mainSize/2, mainSize, mainSize);

        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawOval(x - mainSize/2, y - mainSize/2, mainSize, mainSize);

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 9));
        FontMetrics fm = g2d.getFontMetrics();
        int lw = fm.stringWidth(label);
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.drawString(label, x - lw/2 + 1, y + 4);
        g2d.setColor(Color.WHITE);
        g2d.drawString(label, x - lw/2, y + 3);
    }

    private class Particle {
        float x, y, vx, vy, life, size;
        Color color;

        Particle(float x, float y, double angle, double speed, Color color) {
            this.x = x;
            this.y = y;
            this.vx = (float)(Math.cos(angle) * speed);
            this.vy = (float)(Math.sin(angle) * speed);
            this.color = color;
            this.life = 1.0f;
            this.size = 2 + random.nextFloat() * 3;
        }

        void update() {
            x += vx;
            y += vy;
            vy += 0.1f;
            life -= 0.02f;
            vx *= 0.98f;
            vy *= 0.98f;
        }

        boolean isDead() {
            return life <= 0;
        }

        void draw(Graphics2D g2d) {
            int alpha = (int)(life * 255);
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha / 3));
            g2d.fillOval((int)x - (int)size, (int)y - (int)size, (int)size * 2, (int)size * 2);
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            g2d.fillOval((int)x - (int)size/2, (int)y - (int)size/2, (int)size, (int)size);
        }
    }

    private class TrailPoint {
        float x, y, life, size;
        Color color;

        TrailPoint(float x, float y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.life = 1.0f;
            this.size = 15 + random.nextFloat() * 10;
        }

        void update() {
            life -= 0.015f;
            size *= 1.02f;
        }

        boolean isDead() {
            return life <= 0;
        }

        void draw(Graphics2D g2d) {
            int alpha = (int)(life * 80);
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            g2d.fillOval((int)(x - size/2), (int)(y - size/2), (int)size, (int)size);
        }
    }
}