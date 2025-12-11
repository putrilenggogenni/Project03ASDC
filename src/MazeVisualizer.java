import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

class MazeVisualizer extends JPanel {
    private MazeGraph maze;
    private List<Cell> animationSteps;
    private int currentStep;
    private Timer timer;
    private int totalCost;
    private int cellsExplored;
    private String currentAlgorithm;
    private static final int CELL_SIZE = 40;
    private static final int MARGIN = 30;
    private static final int WALL_THICKNESS = 3;
    private static final int INFO_PANEL_HEIGHT = 100;

    public MazeVisualizer(MazeGraph maze) {
        this.maze = maze;
        this.currentStep = 0;
        this.totalCost = 0;
        this.cellsExplored = 0;
        this.currentAlgorithm = "";
        setPreferredSize(new Dimension(
                maze.getCols() * CELL_SIZE + 2 * MARGIN,
                maze.getRows() * CELL_SIZE + 2 * MARGIN + INFO_PANEL_HEIGHT
        ));
        setBackground(new Color(245, 245, 250));
    }

    public void animateSolution(List<Cell> steps, String algorithm) {
        this.animationSteps = steps;
        this.currentStep = 0;
        this.currentAlgorithm = algorithm;
        this.cellsExplored = 0;

        if (timer != null) {
            timer.stop();
        }

        timer = new Timer(40, e -> {
            if (currentStep < animationSteps.size()) {
                currentStep++;
                cellsExplored = currentStep;
                repaint();
            } else {
                // Calculate total cost when animation completes
                calculateTotalCost();
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
        animationSteps = null;
        currentStep = 0;
        totalCost = 0;
        cellsExplored = 0;
        currentAlgorithm = "";
        repaint();
    }

    private void calculateTotalCost() {
        totalCost = 0;
        for (int i = 0; i < maze.getRows(); i++) {
            for (int j = 0; j < maze.getCols(); j++) {
                Cell cell = maze.getCell(i, j);
                if (cell.inPath) {
                    totalCost += cell.getCost();
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw info panel
        drawInfoPanel(g2d);

        // Draw cells
        for (int i = 0; i < maze.getRows(); i++) {
            for (int j = 0; j < maze.getCols(); j++) {
                Cell cell = maze.getCell(i, j);
                int x = MARGIN + j * CELL_SIZE;
                int y = MARGIN + INFO_PANEL_HEIGHT + i * CELL_SIZE;

                // Draw cell with rounded corners
                RoundRectangle2D cellRect = new RoundRectangle2D.Double(
                        x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4, 10, 10
                );

                // Fill cell based on state
                if (cell.inPath) {
                    // Vibrant gradient for solution path
                    GradientPaint gradient = new GradientPaint(
                            x, y, new Color(255, 193, 7),
                            x + CELL_SIZE, y + CELL_SIZE, new Color(255, 87, 34)
                    );
                    g2d.setPaint(gradient);
                    g2d.fill(cellRect);

                    // Add sparkle effect
                    g2d.setColor(new Color(255, 255, 255, 100));
                    g2d.fillOval(x + 8, y + 8, 6, 6);
                } else if (animationSteps != null && currentStep > 0) {
                    int index = animationSteps.indexOf(cell);
                    if (index >= 0 && index < currentStep) {
                        // Animated exploration gradient
                        float progress = (float) index / currentStep;
                        int alpha = (int)(150 + 105 * progress);
                        Color exploredColor = new Color(103, 58, 183, alpha);
                        g2d.setColor(exploredColor);
                        g2d.fill(cellRect);

                        // Ripple effect for recent cells
                        if (currentStep - index < 5) {
                            int rippleAlpha = (int)(50 * (5 - (currentStep - index)) / 5.0);
                            g2d.setColor(new Color(103, 58, 183, rippleAlpha));
                            g2d.setStroke(new BasicStroke(2));
                            g2d.draw(new RoundRectangle2D.Double(
                                    x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2, 10, 10
                            ));
                        }
                    } else {
                        // Terrain color with enhanced saturation
                        g2d.setColor(cell.terrain.color);
                        g2d.fill(cellRect);
                    }
                } else {
                    // Terrain color
                    g2d.setColor(cell.terrain.color);
                    g2d.fill(cellRect);
                }

                // Draw cost number in center for non-default terrain
                if (cell.terrain != Cell.TerrainType.DEFAULT && !cell.inPath) {
                    g2d.setColor(new Color(0, 0, 0, 80));
                    g2d.setFont(new Font("Arial", Font.BOLD, 11));
                    String costStr = String.valueOf(cell.getCost());
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(costStr);
                    g2d.drawString(costStr, x + (CELL_SIZE - textWidth) / 2, y + CELL_SIZE / 2 + 4);
                }

                // Draw subtle inner shadow for depth
                if (!cell.inPath) {
                    g2d.setColor(new Color(0, 0, 0, 20));
                    g2d.drawRoundRect(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4, 10, 10);
                }

                // Draw walls with rounded caps
                g2d.setColor(new Color(52, 73, 94));
                g2d.setStroke(new BasicStroke(WALL_THICKNESS, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                if (cell.topWall) {
                    g2d.drawLine(x, y, x + CELL_SIZE, y);
                }
                if (cell.rightWall) {
                    g2d.drawLine(x + CELL_SIZE, y, x + CELL_SIZE, y + CELL_SIZE);
                }
                if (cell.bottomWall) {
                    g2d.drawLine(x, y + CELL_SIZE, x + CELL_SIZE, y + CELL_SIZE);
                }
                if (cell.leftWall) {
                    g2d.drawLine(x, y, x, y + CELL_SIZE);
                }
            }
        }

        // Draw start marker with glow effect
        drawMarker(g2d, MARGIN + CELL_SIZE/2, MARGIN + INFO_PANEL_HEIGHT + CELL_SIZE/2,
                new Color(76, 175, 80), "START");

        // Draw end marker with glow effect
        int endX = MARGIN + (maze.getCols()-1) * CELL_SIZE + CELL_SIZE/2;
        int endY = MARGIN + INFO_PANEL_HEIGHT + (maze.getRows()-1) * CELL_SIZE + CELL_SIZE/2;
        drawMarker(g2d, endX, endY, new Color(244, 67, 54), "GOAL");
    }

    private void drawInfoPanel(Graphics2D g2d) {
        // Background gradient
        GradientPaint bgGradient = new GradientPaint(
                0, 0, new Color(67, 97, 238),
                getWidth(), INFO_PANEL_HEIGHT, new Color(103, 58, 183)
        );
        g2d.setPaint(bgGradient);
        g2d.fillRoundRect(10, 10, getWidth() - 20, INFO_PANEL_HEIGHT - 20, 15, 15);

        // Draw stats
        g2d.setColor(Color.WHITE);

        // Algorithm name
        if (!currentAlgorithm.isEmpty()) {
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            g2d.drawString("Algorithm: " + currentAlgorithm, 30, 40);
        }

        // Stats in columns
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        int yPos = 65;

        // Path Cost
        String costText = "Path Cost: ";
        if (totalCost > 0) {
            g2d.setColor(new Color(255, 235, 59));
            costText += totalCost;
        } else {
            g2d.setColor(new Color(200, 200, 200));
            costText += "---";
        }
        g2d.drawString(costText, 30, yPos);

        // Cells Explored
        g2d.setColor(new Color(129, 212, 250));
        String exploredText = "Cells Explored: " + cellsExplored;
        g2d.drawString(exploredText, 220, yPos);

        // Status indicator
        if (animationSteps != null) {
            if (currentStep >= animationSteps.size()) {
                g2d.setColor(new Color(76, 175, 80));
                g2d.fillOval(getWidth() - 100, 25, 15, 15);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                g2d.drawString("Complete", getWidth() - 80, 37);
            } else {
                g2d.setColor(new Color(255, 193, 7));
                g2d.fillOval(getWidth() - 100, 25, 15, 15);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                g2d.drawString("Solving...", getWidth() - 80, 37);
            }
        }
    }

    private void drawMarker(Graphics2D g2d, int x, int y, Color color, String label) {
        // Animated pulse effect
        long time = System.currentTimeMillis();
        float pulse = (float)(Math.sin(time / 300.0) * 0.2 + 1.0);

        // Outer glow with pulse
        for (int i = 4; i > 0; i--) {
            int alpha = (int)(25 * i * pulse);
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            int size = 30 + i * 4;
            g2d.fillOval(x - size/2, y - size/2, size, size);
        }

        // Main circle with gradient
        int mainSize = (int)(28 * pulse);
        GradientPaint gradient = new GradientPaint(
                x - mainSize/2, y - mainSize/2, color.brighter(),
                x + mainSize/2, y + mainSize/2, color
        );
        g2d.setPaint(gradient);
        g2d.fillOval(x - mainSize/2, y - mainSize/2, mainSize, mainSize);

        // Border
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawOval(x - mainSize/2, y - mainSize/2, mainSize, mainSize);

        // Label with shadow
        g2d.setFont(new Font("Arial", Font.BOLD, 9));
        FontMetrics fm = g2d.getFontMetrics();
        int labelWidth = fm.stringWidth(label);

        // Shadow
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(label, x - labelWidth/2 + 1, y + 4);

        // Text
        g2d.setColor(Color.WHITE);
        g2d.drawString(label, x - labelWidth/2, y + 3);

        // Schedule repaint for animation
        Timer pulseTimer = new Timer(50, e -> repaint());
        pulseTimer.setRepeats(false);
        pulseTimer.start();
    }
}