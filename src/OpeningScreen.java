import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class OpeningScreen extends JFrame {
    private Timer animationTimer;
    private float titleAlpha = 0f;
    private float buttonAlpha = 0f;
    private float particleOffset = 0f;
    private JButton startButton, exitButton;
    private boolean animationComplete = false;

    public OpeningScreen() {
        setTitle("PathQuest - Algorithmic Maze Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        OpeningPanel panel = new OpeningPanel();
        add(panel);

        startFadeInAnimation();
        // Di constructor OpeningScreen setelah komponen dibuat
        SoundManager.getInstance().playOpeningMusic();
    }

    private void startFadeInAnimation() {
        animationTimer = new Timer(30, e -> {
            if (titleAlpha < 1f) {
                titleAlpha = Math.min(1f, titleAlpha + 0.02f);
            } else if (buttonAlpha < 1f) {
                buttonAlpha = Math.min(1f, buttonAlpha + 0.03f);
            } else {
                animationComplete = true;
                animationTimer.stop();
            }
            particleOffset += 0.5f;
            repaint();
        });
        animationTimer.start();
    }

    class OpeningPanel extends JPanel {
        private Point mousePos = new Point(450, 350);

        public OpeningPanel() {
            setLayout(null);
            setBackground(new Color(15, 23, 42));

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mousePos = e.getPoint();
                    repaint();
                }
            });

            createButtons();
        }

        private void createButtons() {
            startButton = new JButton("START ADVENTURE");
            styleButton(startButton, new Color(99, 102, 241), 300, 450);
            startButton.addActionListener(e -> {
                if (animationTimer != null) {
                    animationTimer.stop();
                }
                dispose();
                Main.createGameWindow();
            });
            startButton.addActionListener(e -> {
                // Play click sound
                SoundManager.getInstance().playClick();

                // Fade out opening music
                SoundManager.getInstance().fadeOut(500);

                // Stop animation
                if (animationTimer != null) {
                    animationTimer.stop();
                }

                // Delay untuk fade out, lalu main gameplay music
                Timer transitionTimer = new Timer(600, event -> {
                    SoundManager.getInstance().playGameplayMusic();
                });
                transitionTimer.setRepeats(false);
                transitionTimer.start();

                dispose();
                Main.createGameWindow();
            });

            exitButton = new JButton("EXIT");
            styleButton(exitButton, new Color(239, 68, 68), 300, 520);
            exitButton.addActionListener(e -> System.exit(0));

            exitButton.addActionListener(e -> {
                SoundManager.getInstance().playClick();
                SoundManager.getInstance().fadeOut(300);

                Timer exitTimer = new Timer(400, event -> {
                    System.exit(0);
                });
                exitTimer.setRepeats(false);
                exitTimer.start();
            });

            add(startButton);
            add(exitButton);
        }

        private void styleButton(JButton button, Color baseColor, int x, int y) {
            button.setBounds(x, y, 300, 55);
            button.setFont(new Font("SansSerif", Font.BOLD, 18));
            button.setForeground(Color.WHITE);
            button.setBackground(baseColor);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setBorder(BorderFactory.createEmptyBorder());

            button.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(baseColor.brighter());
                    button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
                }
                public void mouseExited(MouseEvent e) {
                    button.setBackground(baseColor);
                    button.setBorder(BorderFactory.createEmptyBorder());
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            drawAnimatedBackground(g2d);
            drawMazePattern(g2d);
            drawParticles(g2d);
            drawTitle(g2d);
            drawSubtitle(g2d);
            drawAlgorithmIcons(g2d);

            float buttonVis = Math.max(0f, Math.min(1f, buttonAlpha));
            startButton.setEnabled(buttonVis > 0.8f);
            exitButton.setEnabled(buttonVis > 0.8f);
        }

        private void drawAnimatedBackground(Graphics2D g2d) {
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(15, 23, 42),
                    getWidth(), getHeight(), new Color(30, 41, 59)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(new Color(99, 102, 241, 15));
            g2d.setStroke(new BasicStroke(1f));

            int gridSize = 40;
            int offset = (int)(particleOffset % gridSize);

            for (int i = -gridSize; i < getWidth() + gridSize; i += gridSize) {
                g2d.drawLine(i + offset, 0, i + offset, getHeight());
            }
            for (int i = -gridSize; i < getHeight() + gridSize; i += gridSize) {
                g2d.drawLine(0, i + offset, getWidth(), i + offset);
            }
        }

        private void drawMazePattern(Graphics2D g2d) {
            g2d.setColor(new Color(99, 102, 241, 30));
            g2d.setStroke(new BasicStroke(3f));

            int cellSize = 60;
            int startX = 50;
            int startY = 100;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    int x = startX + j * cellSize;
                    int y = startY + i * cellSize;

                    if ((i + j) % 2 == 0) {
                        g2d.drawLine(x, y, x + cellSize, y);
                        g2d.drawLine(x + cellSize, y, x + cellSize, y + cellSize);
                    } else {
                        g2d.drawLine(x, y, x, y + cellSize);
                        g2d.drawLine(x, y + cellSize, x + cellSize, y + cellSize);
                    }
                }
            }

            startX = getWidth() - 230;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    int x = startX + j * cellSize;
                    int y = startY + i * cellSize;

                    if ((i + j) % 2 == 1) {
                        g2d.drawLine(x, y, x + cellSize, y);
                        g2d.drawLine(x, y, x, y + cellSize);
                    } else {
                        g2d.drawLine(x + cellSize, y, x + cellSize, y + cellSize);
                        g2d.drawLine(x, y + cellSize, x + cellSize, y + cellSize);
                    }
                }
            }
        }

        private void drawParticles(Graphics2D g2d) {
            g2d.setColor(new Color(168, 85, 247, 80));

            for (int i = 0; i < 30; i++) {
                float x = (i * 137.5f + particleOffset * 0.5f) % getWidth();
                float y = (i * 71.3f + particleOffset * 0.3f) % getHeight();
                float size = 2 + (i % 4);

                g2d.fill(new Ellipse2D.Float(x, y, size, size));
            }
        }

        private void drawTitle(Graphics2D g2d) {
            float safeAlpha = Math.max(0f, Math.min(1f, titleAlpha));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, safeAlpha));

            for (int i = 5; i > 0; i--) {
                int alpha = Math.max(0, Math.min(255, (int)(30 * safeAlpha)));
                g2d.setColor(new Color(99, 102, 241, alpha));
                Font glowFont = new Font("SansSerif", Font.BOLD, 72 + i * 2);
                g2d.setFont(glowFont);
                drawCenteredString(g2d, "PATHQUEST", 200);
            }

            g2d.setColor(new Color(255, 255, 255));
            Font titleFont = new Font("SansSerif", Font.BOLD, 72);
            g2d.setFont(titleFont);
            drawCenteredString(g2d, "PATHQUEST", 200);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        private void drawSubtitle(Graphics2D g2d) {
            float safeAlpha = Math.max(0f, Math.min(1f, titleAlpha));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, safeAlpha));

            g2d.setColor(new Color(168, 85, 247));
            Font subtitleFont = new Font("SansSerif", Font.BOLD, 24);
            g2d.setFont(subtitleFont);
            drawCenteredString(g2d, "THE ALGORITHMIC MAZE", 260);

            g2d.setColor(new Color(203, 213, 225));
            Font descFont = new Font("SansSerif", Font.PLAIN, 16);
            g2d.setFont(descFont);
            drawCenteredString(g2d, "Master the art of pathfinding algorithms", 310);
            drawCenteredString(g2d, "Navigate through dynamic mazes using BFS, DFS, Dijkstra & A*", 340);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        private void drawAlgorithmIcons(Graphics2D g2d) {
            float safeAlpha = Math.max(0f, Math.min(1f, titleAlpha));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, safeAlpha));

            String[] algorithms = {"BFS", "DFS", "Dijkstra", "A*"};
            Color[] colors = {
                    new Color(108, 92, 231),
                    new Color(162, 155, 254),
                    new Color(46, 213, 115),
                    new Color(72, 219, 251)
            };

            int totalWidth = algorithms.length * 100 + (algorithms.length - 1) * 20;
            int startX = (getWidth() - totalWidth) / 2;
            int y = 380;

            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));

            for (int i = 0; i < algorithms.length; i++) {
                int x = startX + i * 120;

                g2d.setColor(colors[i]);
                g2d.fillRoundRect(x, y, 100, 35, 10, 10);

                g2d.setColor(colors[i].brighter());
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawRoundRect(x, y, 100, 35, 10, 10);

                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(algorithms[i]);
                g2d.drawString(algorithms[i], x + (100 - textWidth) / 2, y + 22);
            }

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        private void drawCenteredString(Graphics2D g2d, String text, int y) {
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            g2d.drawString(text, x, y);
        }
    }
}