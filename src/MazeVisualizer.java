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

    // Particle system for visual effects
    private List<Particle> particles = new ArrayList<>();
    private Random random = new Random();
    private Timer particleTimer;

    // Glow effects
    private Map<Cell, Float> cellGlow = new HashMap<>();
    private long animationStartTime;

    // NEW: Playback control
    private boolean isPaused = false;

    // NEW: Trail effects
    private List<TrailPoint> trails = new ArrayList<>();

    // NEW: Theme colors
    private String currentTheme = "neon";
    private Color themeColor1, themeColor2, themeColor3, themeBg;

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
        setBackground(new Color(20, 20, 35));

        // Initialize theme
        updateThemeColors();

        // Start particle animation timer
        particleTimer = new Timer(30, e -> {
            updateParticles();
            updateTrails();
            repaint();
        });
        particleTimer.start();
    }

    // NEW: Theme system
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
            default: // neon
                themeColor1 = new Color(147, 51, 234);
                themeColor2 = new Color(168, 85, 247);
                themeColor3 = new Color(192, 132, 252);
                themeBg = new Color(20, 20, 35);
                break;
        }
        setBackground(themeBg);
    }

    // NEW: Playback controls
    public void pause() {
        isPaused = true;
        if (timer != null) {
            timer.stop();
        }
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
            addExplosionParticles(cell, new Color(103, 58, 183));
            addTrail(cell);
            currentStep++;
            repaint();
        }
    }

    // NEW: Stats getters
    public boolean isAnimating() {
        return explorationSteps != null && currentStep > 0;
    }

    public boolean isComplete() {
        return explorationComplete && pathAnimationStep >= currentPathAnimation.size();
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public long getElapsedTime() {
        if (explorationSteps == null) return 0;
        return System.currentTimeMillis() - animationStartTime;
    }

    public int getBestPathCost() {
        if (pathCosts.length == 0) return 0;
        int min = Integer.MAX_VALUE;
        for (int cost : pathCosts) {
            if (cost > 0 && cost < min) min = cost;
        }
        return min == Integer.MAX_VALUE ? 0 : min;
    }

    public void setAnimationSpeed(int speed) {
        this.animationSpeed = speed;
        if (timer != null && timer.isRunning()) {
            timer.setDelay(getDelayForSpeed(speed));
        }
        // Adjust particle animation speed too
        if (particleTimer != null) {
            int particleDelay = Math.max(10, 50 - speed * 4);
            particleTimer.setDelay(particleDelay);
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
        this.cellGlow.clear();
        this.particles.clear();
        Arrays.fill(pathCosts, 0);
        this.animationStartTime = System.currentTimeMillis();

        if (timer != null) {
            timer.stop();
        }

        timer = new Timer(getDelayForSpeed(animationSpeed), e -> {
            if (!isPaused && currentStep < explorationSteps.size()) {
                Cell cell = explorationSteps.get(currentStep);
                cellGlow.put(cell, 1.0f);
                // Add particles when exploring cells
                addExplosionParticles(cell, themeColor2);
                // Add trail effect
                addTrail(cell);
                currentStep++;
                repaint();
            } else if (currentStep >= explorationSteps.size()) {
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
                Cell cell = currentPathAnimation.get(pathAnimationStep);
                int pathIndex = pathColors.get(cell);
                Color pathColor = getPathColor(pathIndex);
                addExplosionParticles(cell, pathColor);
                pathAnimationStep++;
                repaint();
            } else {
                timer.stop();
                // Add celebration particles at finish points
                for (Cell finish : maze.getFinishCells()) {
                    addCelebrationParticles(finish);
                }
            }
        });
        timer.start();
    }

    private Color getPathColor(int pathIndex) {
        switch (pathIndex) {
            case 0: return PATH_COLOR_1;
            case 1: return PATH_COLOR_2;
            case 2: return PATH_COLOR_3;
            default: return new Color(255, 193, 7);
        }
    }

    // Particle system methods
    private void addExplosionParticles(Cell cell, Color color) {
        int x = MARGIN + cell.col * CELL_SIZE + CELL_SIZE / 2;
        int y = MARGIN + INFO_PANEL_HEIGHT + cell.row * CELL_SIZE + CELL_SIZE / 2;

        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2 * i) / 8;
            double speed = 1 + random.nextDouble() * 2;
            particles.add(new Particle(x, y, angle, speed, color));
        }
    }

    // NEW: Trail system
    private void addTrail(Cell cell) {
        int x = MARGIN + cell.col * CELL_SIZE + CELL_SIZE / 2;
        int y = MARGIN + INFO_PANEL_HEIGHT + cell.row * CELL_SIZE + CELL_SIZE / 2;
        trails.add(new TrailPoint(x, y, themeColor1));
    }

    private void updateTrails() {
        Iterator<TrailPoint> iterator = trails.iterator();
        while (iterator.hasNext()) {
            TrailPoint trail = iterator.next();
            trail.update();
            if (trail.isDead()) {
                iterator.remove();
            }
        }
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
        // Update glow effects
        for (Map.Entry<Cell, Float> entry : new HashMap<>(cellGlow).entrySet()) {
            float glow = entry.getValue() - 0.05f;
            if (glow <= 0) {
                cellGlow.remove(entry.getKey());
            } else {
                cellGlow.put(entry.getKey(), glow);
            }
        }

        // Update particles
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle p = iterator.next();
            p.update();
            if (p.isDead()) {
                iterator.remove();
            }
        }
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
        cellGlow.clear();
        particles.clear();
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
        drawMaze(g2d);
        drawTrails(g2d);
        drawParticles(g2d);
        drawMarkers(g2d);
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
                    Color pathColor = getPathColor(cellPathIndex);

                    // Animated gradient for path
                    float progress = (float)((currentTime / 1000.0) % 2.0) / 2.0f;
                    Color color1 = pathColor;
                    Color color2 = new Color(
                            Math.min(255, pathColor.getRed() + 40),
                            Math.min(255, pathColor.getGreen() + 40),
                            Math.min(255, pathColor.getBlue() + 40)
                    );

                    GradientPaint gradient = new GradientPaint(
                            x + CELL_SIZE * progress, y, color1,
                            x + CELL_SIZE * (1 - progress), y + CELL_SIZE, color2
                    );
                    g2d.setPaint(gradient);
                    g2d.fill(cellRect);

                    // Sparkle effect
                    int sparkleCount = 3;
                    for (int s = 0; s < sparkleCount; s++) {
                        float sparklePhase = (float)((currentTime + s * 300) % 1500) / 1500.0f;
                        int alpha = (int)(255 * Math.sin(sparklePhase * Math.PI));
                        g2d.setColor(new Color(255, 255, 255, alpha));
                        int sx = x + 10 + (s * 10);
                        int sy = y + 10 + (int)(Math.sin(sparklePhase * Math.PI * 2) * 5);
                        g2d.fillOval(sx, sy, 4, 4);
                    }

                    // Glowing border
                    g2d.setColor(new Color(255, 255, 255, 100));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.draw(cellRect);

                } else if (explorationSteps != null && currentStep > 0 && !explorationComplete) {
                    int index = explorationSteps.indexOf(cell);
                    if (index >= 0 && index < currentStep) {
                        float progress = (float) index / currentStep;
                        int baseAlpha = (int)(150 + 105 * progress);

                        // Pulse effect
                        float pulse = (float)(Math.sin((currentTime - animationStartTime) / 200.0 + index * 0.1) * 0.3 + 0.7);
                        int alpha = (int)(baseAlpha * pulse);

                        Color exploredColor = new Color(103, 58, 183, Math.min(255, alpha));
                        g2d.setColor(exploredColor);
                        g2d.fill(cellRect);

                        // Glow effect for recently explored cells
                        if (cellGlow.containsKey(cell)) {
                            float glow = cellGlow.get(cell);
                            int glowAlpha = (int)(glow * 180);
                            g2d.setColor(new Color(200, 150, 255, glowAlpha));
                            g2d.setStroke(new BasicStroke(3));
                            g2d.draw(new RoundRectangle2D.Double(
                                    x, y, CELL_SIZE, CELL_SIZE, 12, 12
                            ));
                        }

                        // Wave ripple for recent exploration
                        if (currentStep - index < 8) {
                            float rippleProgress = (currentStep - index) / 8.0f;
                            int rippleAlpha = (int)(80 * (1 - rippleProgress));
                            g2d.setColor(new Color(103, 58, 183, rippleAlpha));
                            int rippleSize = (int)(CELL_SIZE * (1 + rippleProgress * 0.3));
                            int offset = (rippleSize - CELL_SIZE) / 2;
                            g2d.setStroke(new BasicStroke(2));
                            g2d.draw(new RoundRectangle2D.Double(
                                    x - offset, y - offset, rippleSize, rippleSize, 12, 12
                            ));
                        }
                    } else {
                        // Animated terrain background
                        drawAnimatedTerrain(g2d, cell, x, y, cellRect, currentTime);
                    }
                } else {
                    // Animated terrain background
                    drawAnimatedTerrain(g2d, cell, x, y, cellRect, currentTime);
                }

                // Cost display with glow
                if (cell.terrain != Cell.TerrainType.DEFAULT && !isInAnimatedPath) {
                    // Glow effect on cost number
                    g2d.setColor(new Color(255, 255, 255, 40));
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    String costStr = String.valueOf(cell.getCost());
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(costStr);
                    int textX = x + (CELL_SIZE - textWidth) / 2;
                    int textY = y + CELL_SIZE / 2 + 5;

                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            g2d.drawString(costStr, textX + dx, textY + dy);
                        }
                    }

                    g2d.setColor(new Color(0, 0, 0, 180));
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    g2d.drawString(costStr, textX, textY);
                }

                // Subtle cell border
                if (!isInAnimatedPath) {
                    g2d.setColor(new Color(255, 255, 255, 15));
                    g2d.drawRoundRect(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4, 10, 10);
                }

                // Draw walls with glow
                drawWalls(g2d, cell, x, y);
            }
        }
    }

    private void drawAnimatedTerrain(Graphics2D g2d, Cell cell, int x, int y, RoundRectangle2D cellRect, long currentTime) {
        Color baseColor = cell.terrain.color;

        // Subtle color animation
        float colorPulse = (float)(Math.sin(currentTime / 1000.0 + (cell.row + cell.col) * 0.3) * 0.1 + 0.9);
        Color animatedColor = new Color(
                (int)(baseColor.getRed() * colorPulse),
                (int)(baseColor.getGreen() * colorPulse),
                (int)(baseColor.getBlue() * colorPulse)
        );

        g2d.setColor(animatedColor);
        g2d.fill(cellRect);

        // Add subtle texture overlay
        if (cell.terrain != Cell.TerrainType.DEFAULT) {
            g2d.setColor(new Color(255, 255, 255, 10));
            for (int i = 0; i < 3; i++) {
                int tx = x + 5 + i * 10 + (int)(Math.sin(currentTime / 500.0 + i) * 2);
                int ty = y + 5 + i * 10;
                g2d.fillOval(tx, ty, 3, 3);
            }
        }
    }

    private void drawWalls(Graphics2D g2d, Cell cell, int x, int y) {
        // Glowing walls
        g2d.setColor(new Color(100, 200, 255, 40));
        g2d.setStroke(new BasicStroke(WALL_THICKNESS + 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (cell.topWall) g2d.drawLine(x, y, x + CELL_SIZE, y);
        if (cell.rightWall) g2d.drawLine(x + CELL_SIZE, y, x + CELL_SIZE, y + CELL_SIZE);
        if (cell.bottomWall) g2d.drawLine(x, y + CELL_SIZE, x + CELL_SIZE, y + CELL_SIZE);
        if (cell.leftWall) g2d.drawLine(x, y, x, y + CELL_SIZE);

        // Main wall color
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
        // Animated gradient background
        long time = System.currentTimeMillis();
        float gradientShift = (float)((time / 50.0) % 100) / 100.0f;

        GradientPaint bgGradient = new GradientPaint(
                gradientShift * getWidth(), 0, new Color(67, 97, 238),
                getWidth() * (1 - gradientShift), INFO_PANEL_HEIGHT, new Color(103, 58, 183)
        );
        g2d.setPaint(bgGradient);
        g2d.fillRoundRect(10, 10, getWidth() - 20, INFO_PANEL_HEIGHT - 20, 15, 15);

        // Glowing border
        g2d.setColor(new Color(150, 150, 255, 100));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(10, 10, getWidth() - 20, INFO_PANEL_HEIGHT - 20, 15, 15);

        g2d.setColor(Color.WHITE);

        if (!currentAlgorithm.isEmpty()) {
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
            // Add text shadow
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.drawString("Algorithm: " + currentAlgorithm, 31, 41);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Algorithm: " + currentAlgorithm, 30, 40);
        }

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
        int yPos = 65;

        if (explorationComplete && pathAnimationStep > 0) {
            drawCostDisplay(g2d, PATH_COLOR_1, "G1: " + pathCosts[0], 30, yPos);
            drawCostDisplay(g2d, PATH_COLOR_2, "G2: " + pathCosts[1], 120, yPos);
            drawCostDisplay(g2d, PATH_COLOR_3, "G3: " + pathCosts[2], 210, yPos);
        } else if (!explorationComplete && explorationSteps != null) {
            g2d.setColor(new Color(129, 212, 250));
            g2d.drawString("Exploring: " + currentStep + " cells", 30, yPos);
        } else {
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawString("Path Costs: ---", 30, yPos);
        }

        // Status indicator
        drawStatusIndicator(g2d);
    }

    private void drawCostDisplay(Graphics2D g2d, Color color, String text, int x, int y) {
        // Glowing background
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
        g2d.fillRoundRect(x - 5, y - 15, 80, 22, 8, 8);

        // Text
        g2d.setColor(color.brighter());
        g2d.drawString(text, x, y);
    }

    private void drawStatusIndicator(Graphics2D g2d) {
        int x = getWidth() - 100;
        int y = 25;

        if (explorationSteps != null) {
            if (pathAnimationStep >= currentPathAnimation.size() && explorationComplete) {
                // Complete - animated checkmark
                g2d.setColor(new Color(76, 175, 80));
                g2d.fillOval(x, y, 15, 15);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(x + 4, y + 8, x + 7, y + 11);
                g2d.drawLine(x + 7, y + 11, x + 11, y + 5);

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2d.drawString("Complete", x + 20, y + 12);
            } else if (explorationComplete) {
                // Drawing paths - animated
                long time = System.currentTimeMillis();
                float pulse = (float)(Math.sin(time / 200.0) * 0.5 + 0.5);
                g2d.setColor(new Color(255, 193, 7, (int)(150 + 105 * pulse)));
                g2d.fillOval(x, y, 15, 15);

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.drawString("Drawing Paths", x + 20, y + 12);
            } else {
                // Exploring - rotating spinner
                long time = System.currentTimeMillis();
                double angle = (time % 1000) / 1000.0 * Math.PI * 2;

                g2d.setColor(new Color(255, 193, 7));
                g2d.fillOval(x, y, 15, 15);

                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = x + 7;
                int cy = y + 7;
                int r = 5;
                g2d.drawLine(cx, cy,
                        cx + (int)(Math.cos(angle) * r),
                        cy + (int)(Math.sin(angle) * r));

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.drawString("Exploring", x + 20, y + 12);
            }
        }
    }

    private void drawMarker(Graphics2D g2d, int x, int y, Color color, String label) {
        long time = System.currentTimeMillis();
        float pulse = (float)(Math.sin(time / 300.0) * 0.2 + 1.0);

        // Outer glow rings
        for (int i = 5; i > 0; i--) {
            int alpha = (int)(20 * i * pulse);
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            int size = 30 + i * 5;
            g2d.fillOval(x - size/2, y - size/2, size, size);
        }

        // Main marker with animated gradient
        int mainSize = (int)(32 * pulse);
        float gradientAngle = (float)((time / 1000.0) % (Math.PI * 2));
        int gx1 = x + (int)(Math.cos(gradientAngle) * mainSize / 2);
        int gy1 = y + (int)(Math.sin(gradientAngle) * mainSize / 2);
        int gx2 = x - (int)(Math.cos(gradientAngle) * mainSize / 2);
        int gy2 = y - (int)(Math.sin(gradientAngle) * mainSize / 2);

        GradientPaint gradient = new GradientPaint(
                gx1, gy1, color.brighter(),
                gx2, gy2, color
        );
        g2d.setPaint(gradient);
        g2d.fillOval(x - mainSize/2, y - mainSize/2, mainSize, mainSize);

        // Glowing border
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.drawOval(x - mainSize/2, y - mainSize/2, mainSize, mainSize);

        // Inner sparkle
        g2d.setColor(new Color(255, 255, 255, (int)(150 * pulse)));
        g2d.fillOval(x - 3, y - 3, 6, 6);

        // Label with shadow
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
        FontMetrics fm = g2d.getFontMetrics();
        int labelWidth = fm.stringWidth(label);

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.drawString(label, x - labelWidth/2 + 1, y + 5);

        g2d.setColor(Color.WHITE);
        g2d.drawString(label, x - labelWidth/2, y + 4);
    }

    // Particle class for visual effects
    private class Particle {
        float x, y;
        float vx, vy;
        Color color;
        float life;
        float size;

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
            vy += 0.1f; // Gravity
            life -= 0.02f;
            vx *= 0.98f; // Friction
            vy *= 0.98f;
        }

        boolean isDead() {
            return life <= 0;
        }

        void draw(Graphics2D g2d) {
            int alpha = (int)(life * 255);
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));

            // Glow effect
            int glowSize = (int)(size * 2);
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha / 3));
            g2d.fillOval((int)x - glowSize/2, (int)y - glowSize/2, glowSize, glowSize);

            // Main particle
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            g2d.fillOval((int)x - (int)size/2, (int)y - (int)size/2, (int)size, (int)size);
        }
    }

    // NEW: Trail effect class
    private class TrailPoint {
        float x, y;
        Color color;
        float life;
        float size;

        TrailPoint(float x, float y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.life = 1.0f;
            this.size = 15 + random.nextFloat() * 10;
        }

        void update() {
            life -= 0.015f;
            size *= 1.02f; // Expand slowly
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