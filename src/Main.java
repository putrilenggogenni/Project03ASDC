import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {
    private static String currentTheme = "neon"; // neon, ocean, forest, sunset

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Initialize sound manager
            SoundManager soundManager = SoundManager.getInstance();
            soundManager.loadSounds();

            // Show opening screen first
            OpeningScreen openingScreen = new OpeningScreen();
            openingScreen.setVisible(true);
        });
    }

    private static JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(200, 40));
        button.setMaximumSize(new Dimension(200, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.WHITE, 2),
                        BorderFactory.createEmptyBorder(8, 18, 8, 18)
                ));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
                button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            }
        });

        return button;
    }

    private static JPanel createLegendItem(String text, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setBackground(new Color(52, 73, 94));
        panel.setMaximumSize(new Dimension(250, 35));

        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(24, 24));
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 180), 2),
                BorderFactory.createLineBorder(color.darker(), 1)
        ));

        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));

        panel.add(colorBox);
        panel.add(label);
        return panel;
    }

    public static void createGameWindow() {
        JFrame frame = new JFrame("PathQuest - Algorithmic Maze Solver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Difficulty selector dialog
        String[] difficulties = {"Small (10x10)", "Medium (15x15)", "Large (20x20)", "Extreme (25x25)"};
        String selected = (String) JOptionPane.showInputDialog(
                null,
                "Select Maze Difficulty:",
                "PathQuest - Difficulty Selection",
                JOptionPane.QUESTION_MESSAGE,
                null,
                difficulties,
                difficulties[1]
        );

        int mazeSize = 15; // default
        if (selected != null) {
            if (selected.contains("10x10")) mazeSize = 10;
            else if (selected.contains("15x15")) mazeSize = 15;
            else if (selected.contains("20x20")) mazeSize = 20;
            else if (selected.contains("25x25")) mazeSize = 25;
        }

        // Create maze
        final MazeGraph maze = new MazeGraph(mazeSize, mazeSize);
        maze.generateMazeWithPrim();

        // Create visualizer
        final MazeVisualizer visualizer = new MazeVisualizer(maze);

        // Create RIGHT SIDE CONTROL PANEL
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(new Color(52, 73, 94));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Make mazeSize final for lambda
        final int finalMazeSize = mazeSize;

        // === SECTION 1: STATS PANEL ===
        JPanel statsSection = createSection("📊 STATISTICS");

        final JLabel cellsExploredLabel = createStatLabel("Cells Explored: 0");
        final JLabel timeElapsedLabel = createStatLabel("Time: 0.0s");
        final JLabel efficiencyLabel = createStatLabel("Efficiency: 0%");
        final JLabel bestPathLabel = createStatLabel("Best Cost: ---");

        statsSection.add(cellsExploredLabel);
        statsSection.add(Box.createVerticalStrut(8));
        statsSection.add(timeElapsedLabel);
        statsSection.add(Box.createVerticalStrut(8));
        statsSection.add(efficiencyLabel);
        statsSection.add(Box.createVerticalStrut(8));
        statsSection.add(bestPathLabel);

        rightPanel.add(statsSection);
        rightPanel.add(Box.createVerticalStrut(15));

        // Stats updater timer
        Timer statsTimer = new Timer(100, e -> {
            if (visualizer.isAnimating()) {
                int explored = visualizer.getCurrentStep();
                cellsExploredLabel.setText("Cells Explored: " + explored);

                double timeElapsed = visualizer.getElapsedTime() / 1000.0;
                timeElapsedLabel.setText(String.format("Time: %.1fs", timeElapsed));

                if (visualizer.isComplete()) {
                    int totalCells = finalMazeSize * finalMazeSize;
                    int efficiency = (int)((1.0 - (double)explored / totalCells) * 100);
                    efficiencyLabel.setText("Efficiency: " + Math.max(0, efficiency) + "%");

                    int bestCost = visualizer.getBestPathCost();
                    bestPathLabel.setText("Best Cost: " + bestCost);
                }
            }
        });
        statsTimer.start();

        // === SECTION 2: ALGORITHM SELECTION ===
        JPanel algorithmSection = createSection("🧠 ALGORITHMS");

        JButton bfsButton = createStyledButton("BFS", new Color(108, 92, 231));
        JButton dfsButton = createStyledButton("DFS", new Color(162, 155, 254));
        JButton dijkstraButton = createStyledButton("Dijkstra", new Color(46, 213, 115));
        JButton astarButton = createStyledButton("A* Algorithm", new Color(72, 219, 251));

        bfsButton.addActionListener(e -> {
            BFSSolver solver = new BFSSolver(maze);
            if (solver.solve()) {
                visualizer.animateSolution(solver.getPathSteps(), "BFS (Breadth-First Search)");
            }
        });

        dfsButton.addActionListener(e -> {
            DFSSolver solver = new DFSSolver(maze);
            if (solver.solve()) {
                visualizer.animateSolution(solver.getPathSteps(), "DFS (Depth-First Search)");
            }
        });

        dijkstraButton.addActionListener(e -> {
            DijkstraSolver solver = new DijkstraSolver(maze);
            if (solver.solve()) {
                visualizer.animateSolution(solver.getPathSteps(), "Dijkstra's Algorithm");
            }
        });

        astarButton.addActionListener(e -> {
            AStarSolver solver = new AStarSolver(maze);
            if (solver.solve()) {
                visualizer.animateSolution(solver.getPathSteps(), "A* Algorithm");
            }
        });

        algorithmSection.add(bfsButton);
        algorithmSection.add(Box.createVerticalStrut(10));
        algorithmSection.add(dfsButton);
        algorithmSection.add(Box.createVerticalStrut(10));
        algorithmSection.add(dijkstraButton);
        algorithmSection.add(Box.createVerticalStrut(10));
        algorithmSection.add(astarButton);

        rightPanel.add(algorithmSection);
        rightPanel.add(Box.createVerticalStrut(15));

        // === SECTION 3: PLAYBACK CONTROLS ===
        JPanel playbackSection = createSection("⏯️ PLAYBACK");

        JButton pauseButton = createStyledButton("⏸ Pause", new Color(255, 193, 7));
        JButton playButton = createStyledButton("▶ Play", new Color(76, 175, 80));
        JButton stepButton = createStyledButton("⏭ Step", new Color(3, 169, 244));

        pauseButton.addActionListener(e -> visualizer.pause());
        playButton.addActionListener(e -> visualizer.play());
        stepButton.addActionListener(e -> visualizer.step());

        playbackSection.add(pauseButton);
        playbackSection.add(Box.createVerticalStrut(10));
        playbackSection.add(playButton);
        playbackSection.add(Box.createVerticalStrut(10));
        playbackSection.add(stepButton);

        rightPanel.add(playbackSection);
        rightPanel.add(Box.createVerticalStrut(15));

        // === SECTION 4: ANIMATION SPEED ===
        JPanel speedSection = createSection("⚡ ANIMATION SPEED");

        JLabel speedLabel = new JLabel("Speed:");
        speedLabel.setForeground(Color.WHITE);
        speedLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        speedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 5);
        speedSlider.setBackground(new Color(52, 73, 94));
        speedSlider.setForeground(Color.WHITE);
        speedSlider.setMajorTickSpacing(3);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(false);
        speedSlider.setMaximumSize(new Dimension(200, 40));
        speedSlider.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel speedValueLabel = new JLabel("Normal");
        speedValueLabel.setForeground(new Color(129, 212, 250));
        speedValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        speedValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        speedSlider.addChangeListener(e -> {
            int value = speedSlider.getValue();
            visualizer.setAnimationSpeed(value);
            if (value <= 3) {
                speedValueLabel.setText("Slow");
            } else if (value <= 7) {
                speedValueLabel.setText("Normal");
            } else {
                speedValueLabel.setText("Fast");
            }
        });

        speedSection.add(speedLabel);
        speedSection.add(Box.createVerticalStrut(8));
        speedSection.add(speedSlider);
        speedSection.add(Box.createVerticalStrut(8));
        speedSection.add(speedValueLabel);

        rightPanel.add(speedSection);
        rightPanel.add(Box.createVerticalStrut(15));

        // === SECTION 5: THEMES ===
        JPanel themeSection = createSection("🎨 THEMES");

        JButton neonTheme = createStyledButton("Neon", new Color(147, 51, 234));
        JButton oceanTheme = createStyledButton("Ocean", new Color(14, 165, 233));
        JButton forestTheme = createStyledButton("Forest", new Color(34, 197, 94));
        JButton sunsetTheme = createStyledButton("Sunset", new Color(249, 115, 22));

        neonTheme.addActionListener(e -> visualizer.setTheme("neon"));
        oceanTheme.addActionListener(e -> visualizer.setTheme("ocean"));
        forestTheme.addActionListener(e -> visualizer.setTheme("forest"));
        sunsetTheme.addActionListener(e -> visualizer.setTheme("sunset"));

        themeSection.add(neonTheme);
        themeSection.add(Box.createVerticalStrut(10));
        themeSection.add(oceanTheme);
        themeSection.add(Box.createVerticalStrut(10));
        themeSection.add(forestTheme);
        themeSection.add(Box.createVerticalStrut(10));
        themeSection.add(sunsetTheme);

        rightPanel.add(themeSection);
        rightPanel.add(Box.createVerticalStrut(15));

        // === SECTION 6: MAZE ACTIONS ===
        JPanel mazeSection = createSection("🎲 MAZE ACTIONS");

        JButton resetButton = createStyledButton("Reset", new Color(255, 152, 0));
        JButton regenerateButton = createStyledButton("New Maze", new Color(255, 71, 87));

        resetButton.addActionListener(e -> {
            visualizer.reset();
            cellsExploredLabel.setText("Cells Explored: 0");
            timeElapsedLabel.setText("Time: 0.0s");
            efficiencyLabel.setText("Efficiency: 0%");
            bestPathLabel.setText("Best Cost: ---");
        });

        regenerateButton.addActionListener(e -> {
            maze.generateMazeWithPrim();
            visualizer.reset();
            cellsExploredLabel.setText("Cells Explored: 0");
            timeElapsedLabel.setText("Time: 0.0s");
            efficiencyLabel.setText("Efficiency: 0%");
            bestPathLabel.setText("Best Cost: ---");
        });

        mazeSection.add(resetButton);
        mazeSection.add(Box.createVerticalStrut(10));
        mazeSection.add(regenerateButton);

        rightPanel.add(mazeSection);
        rightPanel.add(Box.createVerticalStrut(15));

        // === SECTION 7: LEGEND ===
        JPanel legendSection = createSection("⚡ TERRAIN COSTS");

        legendSection.add(createLegendItem("Default (0)", Cell.TerrainType.DEFAULT.color));
        legendSection.add(Box.createVerticalStrut(8));
        legendSection.add(createLegendItem("Grass (1)", Cell.TerrainType.GRASS.color));
        legendSection.add(Box.createVerticalStrut(8));
        legendSection.add(createLegendItem("Mud (5)", Cell.TerrainType.MUD.color));
        legendSection.add(Box.createVerticalStrut(8));
        legendSection.add(createLegendItem("Water (10)", Cell.TerrainType.WATER.color));

        rightPanel.add(legendSection);
        rightPanel.add(Box.createVerticalStrut(15));

        // === SECTION 8: INFO ===
        JPanel infoSection = createSection("💡 INFO");

        JTextArea infoText = new JTextArea(
                "• Dijkstra & A* minimize path cost\n" +
                        "• BFS & DFS explore systematically\n" +
                        "• Reach any G1/G2/G3 to win!\n" +
                        "• Adjust speed for better viewing"
        );
        infoText.setEditable(false);
        infoText.setBackground(new Color(52, 73, 94));
        infoText.setForeground(new Color(200, 200, 200));
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoText.setLineWrap(true);
        infoText.setWrapStyleWord(true);
        infoText.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoText.setMaximumSize(new Dimension(250, 100));

        infoSection.add(infoText);

        rightPanel.add(infoSection);
        rightPanel.add(Box.createVerticalGlue());

        // Wrap right panel in scroll pane
        JScrollPane rightScrollPane = new JScrollPane(rightPanel);
        rightScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        rightScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rightScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        rightScrollPane.setBorder(BorderFactory.createEmptyBorder());
        rightScrollPane.setPreferredSize(new Dimension(280, 600));

        // Wrap visualizer in scroll pane (for large mazes)
        JScrollPane mazeScrollPane = new JScrollPane(visualizer);
        mazeScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mazeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mazeScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mazeScrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        // Main layout: maze on left, controls on right
        frame.setLayout(new BorderLayout());
        frame.add(mazeScrollPane, BorderLayout.CENTER);
        frame.add(rightScrollPane, BorderLayout.EAST);

        frame.pack();

        // Ensure window fits screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxWidth = Math.min(frame.getWidth(), (int)(screenSize.width * 0.95));
        int maxHeight = Math.min(frame.getHeight(), (int)(screenSize.height * 0.95));
        frame.setSize(maxWidth, maxHeight);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JPanel createSection(String title) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(44, 62, 80));
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(99, 102, 241, 100), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(280, Integer.MAX_VALUE));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(255, 235, 59));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        section.add(titleLabel);
        section.add(Box.createVerticalStrut(12));

        return section;
    }

    private static JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(129, 212, 250));
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}