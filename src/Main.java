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
        button.setPreferredSize(new Dimension(130, 40));
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

        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(28, 28));
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 180), 2),
                BorderFactory.createLineBorder(color.darker(), 1)
        ));

        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));

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

        // Stats panel for score system
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        statsPanel.setBackground(new Color(52, 73, 94));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        final JLabel cellsExploredLabel = createStatLabel("Cells Explored: 0");
        final JLabel timeElapsedLabel = createStatLabel("Time: 0.0s");
        final JLabel efficiencyLabel = createStatLabel("Efficiency: 0%");
        final JLabel bestPathLabel = createStatLabel("Best Cost: ---");

        statsPanel.add(cellsExploredLabel);
        statsPanel.add(timeElapsedLabel);
        statsPanel.add(efficiencyLabel);
        statsPanel.add(bestPathLabel);

        // Make mazeSize final for lambda
        final int finalMazeSize = mazeSize;

        // Add stats updater to visualizer
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

        // Create modern control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(52, 73, 94));
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Create styled buttons
        JButton bfsButton = createStyledButton("BFS", new Color(108, 92, 231));
        JButton dfsButton = createStyledButton("DFS", new Color(162, 155, 254));
        JButton dijkstraButton = createStyledButton("Dijkstra", new Color(46, 213, 115));
        JButton astarButton = createStyledButton("A* Algorithm", new Color(72, 219, 251));
        JButton resetButton = createStyledButton("Reset", new Color(255, 152, 0));
        JButton regenerateButton = createStyledButton("New Maze", new Color(255, 71, 87));

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

        controlPanel.add(bfsButton);
        controlPanel.add(dfsButton);
        controlPanel.add(dijkstraButton);
        controlPanel.add(astarButton);
        controlPanel.add(resetButton);
        controlPanel.add(regenerateButton);

        // Playback control panel (NEW!)
        JPanel playbackPanel = new JPanel();
        playbackPanel.setBackground(new Color(52, 73, 94));
        playbackPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        playbackPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        JButton pauseButton = createSmallButton("⏸ Pause", new Color(255, 193, 7));
        JButton playButton = createSmallButton("▶ Play", new Color(76, 175, 80));
        JButton stepButton = createSmallButton("⏭ Step", new Color(3, 169, 244));

        pauseButton.addActionListener(e -> visualizer.pause());
        playButton.addActionListener(e -> visualizer.play());
        stepButton.addActionListener(e -> visualizer.step());

        playbackPanel.add(pauseButton);
        playbackPanel.add(playButton);
        playbackPanel.add(stepButton);

        // Speed control panel
        JPanel speedPanel = new JPanel();
        speedPanel.setBackground(new Color(52, 73, 94));
        speedPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        speedPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        JLabel speedLabel = new JLabel("⚡ Animation Speed:");
        speedLabel.setForeground(new Color(255, 235, 59));
        speedLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 5);
        speedSlider.setBackground(new Color(52, 73, 94));
        speedSlider.setForeground(Color.WHITE);
        speedSlider.setMajorTickSpacing(3);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(false);
        speedSlider.setPreferredSize(new Dimension(200, 40));

        JLabel speedValueLabel = new JLabel("Normal");
        speedValueLabel.setForeground(Color.WHITE);
        speedValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        speedValueLabel.setPreferredSize(new Dimension(60, 20));

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

        speedPanel.add(speedLabel);
        speedPanel.add(speedSlider);
        speedPanel.add(speedValueLabel);

        // Theme selector (NEW!)
        JPanel themePanel = new JPanel();
        themePanel.setBackground(new Color(52, 73, 94));
        themePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        themePanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        JLabel themeLabel = new JLabel("🎨 Theme:");
        themeLabel.setForeground(new Color(255, 235, 59));
        themeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JButton neonTheme = createSmallButton("Neon", new Color(147, 51, 234));
        JButton oceanTheme = createSmallButton("Ocean", new Color(14, 165, 233));
        JButton forestTheme = createSmallButton("Forest", new Color(34, 197, 94));
        JButton sunsetTheme = createSmallButton("Sunset", new Color(249, 115, 22));

        neonTheme.addActionListener(e -> visualizer.setTheme("neon"));
        oceanTheme.addActionListener(e -> visualizer.setTheme("ocean"));
        forestTheme.addActionListener(e -> visualizer.setTheme("forest"));
        sunsetTheme.addActionListener(e -> visualizer.setTheme("sunset"));

        themePanel.add(themeLabel);
        themePanel.add(neonTheme);
        themePanel.add(oceanTheme);
        themePanel.add(forestTheme);
        themePanel.add(sunsetTheme);

        // Create elegant legend panel
        JPanel legendPanel = new JPanel();
        legendPanel.setBackground(new Color(52, 73, 94));
        legendPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        legendPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 20));

        JLabel legendTitle = new JLabel("⚡ Terrain Cost:");
        legendTitle.setForeground(new Color(255, 235, 59));
        legendTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        legendPanel.add(legendTitle);

        legendPanel.add(createLegendItem("Default (0)", Cell.TerrainType.DEFAULT.color));
        legendPanel.add(createLegendItem("Grass (1)", Cell.TerrainType.GRASS.color));
        legendPanel.add(createLegendItem("Mud (5)", Cell.TerrainType.MUD.color));
        legendPanel.add(createLegendItem("Water (10)", Cell.TerrainType.WATER.color));

        // Add algorithm info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(52, 73, 94));
        infoPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 8));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));

        JLabel infoLabel = new JLabel("<html><center>💡 <b>Tip:</b> Dijkstra & A* minimize path cost | 🎯 Reach any of the 3 finish points to win!</center></html>");
        infoLabel.setForeground(new Color(200, 200, 200));
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoPanel.add(infoLabel);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(statsPanel, BorderLayout.NORTH);

        JPanel controlsContainer = new JPanel(new BorderLayout());
        controlsContainer.add(controlPanel, BorderLayout.NORTH);
        controlsContainer.add(playbackPanel, BorderLayout.CENTER);

        JPanel speedThemeContainer = new JPanel(new BorderLayout());
        speedThemeContainer.add(speedPanel, BorderLayout.NORTH);
        speedThemeContainer.add(themePanel, BorderLayout.CENTER);

        southPanel.add(controlsContainer, BorderLayout.CENTER);
        southPanel.add(speedThemeContainer, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(southPanel, BorderLayout.NORTH);
        bottomPanel.add(legendPanel, BorderLayout.CENTER);
        bottomPanel.add(infoPanel, BorderLayout.SOUTH);

        // Wrap visualizer in scroll pane to handle overflow
        JScrollPane scrollPane = new JScrollPane(visualizer);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.pack();
        // Ensure window isn't too large for screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxWidth = Math.min(frame.getWidth(), (int)(screenSize.width * 0.9));
        int maxHeight = Math.min(frame.getHeight(), (int)(screenSize.height * 0.9));
        frame.setSize(maxWidth, maxHeight);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(129, 212, 250));
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private static JButton createSmallButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 10));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(90, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }
}