import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Maze Solver - Pathfinding Algorithms Visualization");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Create maze with adjusted size for screen fit
            MazeGraph maze = new MazeGraph(12, 12);
            maze.generateMazeWithPrim();

            // Create visualizer
            MazeVisualizer visualizer = new MazeVisualizer(maze);

            // Create right side control panel
            JPanel rightPanel = new JPanel();
            rightPanel.setBackground(new Color(52, 73, 94));
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.setPreferredSize(new Dimension(280, 0));
            rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Title panel
            JPanel titlePanel = new JPanel();
            titlePanel.setBackground(new Color(52, 73, 94));
            titlePanel.setMaximumSize(new Dimension(280, 60));
            JLabel titleLabel = new JLabel("<html><center>⚙️ Control Panel</center></html>");
            titleLabel.setForeground(new Color(255, 235, 59));
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            titlePanel.add(titleLabel);

            // Algorithm selection panel
            JPanel algoPanel = new JPanel();
            algoPanel.setBackground(new Color(52, 73, 94));
            algoPanel.setLayout(new BoxLayout(algoPanel, BoxLayout.Y_AXIS));
            algoPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
                    "Algorithm Selection",
                    0,
                    0,
                    new Font("Arial", Font.BOLD, 14),
                    Color.WHITE
            ));
            algoPanel.setMaximumSize(new Dimension(280, 240));

            JButton bfsButton = createControlButton("BFS", new Color(108, 92, 231));
            JButton dfsButton = createControlButton("DFS", new Color(162, 155, 254));
            JButton dijkstraButton = createControlButton("Dijkstra", new Color(46, 213, 115));
            JButton astarButton = createControlButton("A* Algorithm", new Color(72, 219, 251));

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

            algoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            algoPanel.add(bfsButton);
            algoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            algoPanel.add(dfsButton);
            algoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            algoPanel.add(dijkstraButton);
            algoPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            algoPanel.add(astarButton);
            algoPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            // Speed control panel
            JPanel speedPanel = new JPanel();
            speedPanel.setBackground(new Color(52, 73, 94));
            speedPanel.setLayout(new BoxLayout(speedPanel, BoxLayout.Y_AXIS));
            speedPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
                    "Animation Speed",
                    0,
                    0,
                    new Font("Arial", Font.BOLD, 14),
                    Color.WHITE
            ));
            speedPanel.setMaximumSize(new Dimension(280, 120));

            JLabel speedLabel = new JLabel("Speed: Normal");
            speedLabel.setForeground(Color.WHITE);
            speedLabel.setFont(new Font("Arial", Font.BOLD, 13));
            speedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 5);
            speedSlider.setBackground(new Color(52, 73, 94));
            speedSlider.setForeground(Color.WHITE);
            speedSlider.setMajorTickSpacing(3);
            speedSlider.setMinorTickSpacing(1);
            speedSlider.setPaintTicks(true);
            speedSlider.setPaintLabels(false);
            speedSlider.setMaximumSize(new Dimension(240, 50));

            speedSlider.addChangeListener(e -> {
                int value = speedSlider.getValue();
                visualizer.setAnimationSpeed(value);
                if (value <= 3) {
                    speedLabel.setText("Speed: Slow");
                } else if (value <= 7) {
                    speedLabel.setText("Speed: Normal");
                } else {
                    speedLabel.setText("Speed: Fast");
                }
            });

            speedPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            speedPanel.add(speedLabel);
            speedPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            speedPanel.add(speedSlider);
            speedPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            // Action buttons panel
            JPanel actionPanel = new JPanel();
            actionPanel.setBackground(new Color(52, 73, 94));
            actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
            actionPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
                    "Actions",
                    0,
                    0,
                    new Font("Arial", Font.BOLD, 14),
                    Color.WHITE
            ));
            actionPanel.setMaximumSize(new Dimension(280, 140));

            JButton resetButton = createControlButton("Reset", new Color(255, 152, 0));
            JButton regenerateButton = createControlButton("New Maze", new Color(255, 71, 87));

            resetButton.addActionListener(e -> {
                visualizer.reset();
            });

            regenerateButton.addActionListener(e -> {
                maze.generateMazeWithPrim();
                visualizer.reset();
            });

            actionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            actionPanel.add(resetButton);
            actionPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            actionPanel.add(regenerateButton);
            actionPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            // Legend panel
            JPanel legendPanel = new JPanel();
            legendPanel.setBackground(new Color(52, 73, 94));
            legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
            legendPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
                    "Terrain Cost",
                    0,
                    0,
                    new Font("Arial", Font.BOLD, 14),
                    Color.WHITE
            ));
            legendPanel.setMaximumSize(new Dimension(280, 200));

            legendPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            legendPanel.add(createLegendItem("Default (0)", Cell.TerrainType.DEFAULT.color));
            legendPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            legendPanel.add(createLegendItem("Grass (1)", Cell.TerrainType.GRASS.color));
            legendPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            legendPanel.add(createLegendItem("Mud (5)", Cell.TerrainType.MUD.color));
            legendPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            legendPanel.add(createLegendItem("Water (10)", Cell.TerrainType.WATER.color));
            legendPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            // Info panel
            JPanel infoPanel = new JPanel();
            infoPanel.setBackground(new Color(52, 73, 94));
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setMaximumSize(new Dimension(280, 100));

            JLabel infoLabel = new JLabel("<html><center>💡 Routes to all 3 goals<br>are shown in different colors</center></html>");
            infoLabel.setForeground(new Color(200, 200, 200));
            infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            infoPanel.add(infoLabel);

            // Assemble right panel
            rightPanel.add(titlePanel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            rightPanel.add(algoPanel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            rightPanel.add(speedPanel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            rightPanel.add(actionPanel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            rightPanel.add(legendPanel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            rightPanel.add(infoPanel);
            rightPanel.add(Box.createVerticalGlue());

            // Main layout: maze on left, controls on right
            frame.setLayout(new BorderLayout());
            frame.add(visualizer, BorderLayout.CENTER);
            frame.add(rightPanel, BorderLayout.EAST);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static JButton createControlButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMaximumSize(new Dimension(240, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

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

    private static JPanel createLegendItem(String text, Color color) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setBackground(new Color(52, 73, 94));
        panel.setMaximumSize(new Dimension(240, 30));

        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(24, 24));
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 180), 2),
                BorderFactory.createLineBorder(color.darker(), 1)
        ));

        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 11));

        panel.add(colorBox);
        panel.add(label);
        return panel;
    }
}