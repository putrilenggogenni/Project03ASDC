import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Maze Solver - Pathfinding Algorithms Visualization");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Create maze
            MazeGraph maze = new MazeGraph(15, 15);
            maze.generateMazeWithPrim();

            // Create visualizer
            MazeVisualizer visualizer = new MazeVisualizer(maze);

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
            });

            regenerateButton.addActionListener(e -> {
                maze.generateMazeWithPrim();
                visualizer.reset();
            });

            controlPanel.add(bfsButton);
            controlPanel.add(dfsButton);
            controlPanel.add(dijkstraButton);
            controlPanel.add(astarButton);
            controlPanel.add(resetButton);
            controlPanel.add(regenerateButton);

            // Create elegant legend panel
            JPanel legendPanel = new JPanel();
            legendPanel.setBackground(new Color(52, 73, 94));
            legendPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
            legendPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 20));

            JLabel legendTitle = new JLabel("⚡ Terrain Cost:");
            legendTitle.setForeground(new Color(255, 235, 59));
            legendTitle.setFont(new Font("Arial", Font.BOLD, 14));
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

            JLabel infoLabel = new JLabel("<html><center>💡 <b>Tip:</b> Dijkstra & A* minimize path cost by avoiding expensive terrain</center></html>");
            infoLabel.setForeground(new Color(200, 200, 200));
            infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            infoPanel.add(infoLabel);

            JPanel southPanel = new JPanel(new BorderLayout());
            southPanel.add(controlPanel, BorderLayout.NORTH);
            southPanel.add(legendPanel, BorderLayout.CENTER);
            southPanel.add(infoPanel, BorderLayout.SOUTH);

            frame.setLayout(new BorderLayout());
            frame.add(visualizer, BorderLayout.CENTER);
            frame.add(southPanel, BorderLayout.SOUTH);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(130, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Enhanced hover effect with scale
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
        label.setFont(new Font("Arial", Font.BOLD, 12));

        panel.add(colorBox);
        panel.add(label);
        return panel;
    }
}