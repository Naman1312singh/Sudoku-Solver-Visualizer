import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class SudokuSolverVisualizer extends JFrame {
    private static final long serialVersionUID = 1L;
    private int gridSize = 9;
    private int solvingSpeed = 500; // milliseconds
    private JTextField[][] cells;
    private boolean[][] fixedValues;
    private JPanel gridPanel;
    private JTextField sizeField;
    private JTextField speedField;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public SudokuSolverVisualizer() {
        setTitle("Sudoku Solver Visualizer");
        setSize(900, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);
        getContentPane().setBackground(new Color(30, 30, 30));

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        JPanel startPanel = createStartPanel();
        JPanel gamePanel = createGamePanel();
        JPanel endPanel = createEndPanel();

        mainPanel.add(startPanel, "start");
        mainPanel.add(gamePanel, "game");
        mainPanel.add(endPanel, "end");

        add(mainPanel, BorderLayout.CENTER);
        cardLayout.show(mainPanel, "start");
    }

    private JPanel createStartPanel() {
        JPanel startPanel = new JPanel();
        startPanel.setLayout(new GridBagLayout());
        startPanel.setBackground(new Color(50, 50, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel titleLabel = new JLabel("Sudoku Solver Visualizer");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        startPanel.add(titleLabel, gbc);

        JButton startButton = new ShadowButton("Start Game", new Color(76, 175, 80));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.addActionListener(e -> cardLayout.show(mainPanel, "game"));
        gbc.gridy = 1;
        startPanel.add(startButton, gbc);

        return startPanel;
    }

    private JPanel createGamePanel() {
        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(30, 30, 30));

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setBackground(new Color(50, 50, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel sizeLabel = new JLabel("Grid Size: ");
        sizeLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        inputPanel.add(sizeLabel, gbc);

        sizeField = new JTextField(2);
        sizeField.setText(String.valueOf(gridSize));
        gbc.gridx = 1;
        gbc.gridy = 0;
        inputPanel.add(sizeField, gbc);

        JLabel speedLabel = new JLabel("Solving Speed (ms): ");
        speedLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(speedLabel, gbc);

        speedField = new JTextField(4);
        speedField.setText(String.valueOf(solvingSpeed));
        gbc.gridx = 1;
        gbc.gridy = 1;
        inputPanel.add(speedField, gbc);

        JButton updateButton = new ShadowButton("Update", new Color(76, 175, 80));
        updateButton.setForeground(Color.WHITE);
        updateButton.setFocusPainted(false);
        updateButton.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        inputPanel.add(updateButton, gbc);

        JButton solveButton = new ShadowButton("Solve", new Color(33, 150, 243));
        solveButton.setForeground(Color.WHITE);
        solveButton.setFocusPainted(false);
        solveButton.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy = 3;
        inputPanel.add(solveButton, gbc);

        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    gridSize = Integer.parseInt(sizeField.getText());
                    solvingSpeed = Integer.parseInt(speedField.getText());
                    createGrid();
                    fillRandomValues();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid input. Please enter valid numbers.");
                }
            }
        });

        solveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    if (solveSudoku()) {
                        showEndMessage("You Win!");
                    } else {
                        showEndMessage("No Solution Found.");
                    }
                }).start();
            }
        });

        gamePanel.add(inputPanel, BorderLayout.NORTH);

        // Sudoku grid panel
        gridPanel = new JPanel();
        gridPanel.setBackground(new Color(58, 65, 73));
        createGrid();
        fillRandomValues();
        gamePanel.add(gridPanel, BorderLayout.CENTER);

        return gamePanel;
    }

    private JPanel createEndPanel() {
        JPanel endPanel = new JPanel();
        endPanel.setLayout(new GridBagLayout());
        endPanel.setBackground(new Color(50, 50, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel endMessageLabel = new JLabel("");
        endMessageLabel.setForeground(Color.WHITE);
        endMessageLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        endPanel.add(endMessageLabel, gbc);

        JButton restartButton = new ShadowButton("Restart Game", new Color(76, 175, 80));
        restartButton.setForeground(Color.WHITE);
        restartButton.setFocusPainted(false);
        restartButton.setFont(new Font("Arial", Font.BOLD, 14));
        restartButton.addActionListener(e -> cardLayout.show(mainPanel, "start"));
        gbc.gridy = 1;
        endPanel.add(restartButton, gbc);

        return endPanel;
    }

    private void showEndMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            Component[] components = mainPanel.getComponents();
            for (Component component : components) {
                if (component instanceof JPanel) {
                    JPanel panel = (JPanel) component;
                    if (panel.getLayout() instanceof GridBagLayout) {
                        Component[] panelComponents = panel.getComponents();
                        for (Component panelComponent : panelComponents) {
                            if (panelComponent instanceof JLabel) {
                                ((JLabel) panelComponent).setText(message);
                            }
                        }
                    }
                }
            }
            cardLayout.show(mainPanel, "end");
        });
    }

    private void createGrid() {
        gridPanel.removeAll();
        gridPanel.setLayout(new GridLayout(gridSize, gridSize));
        cells = new JTextField[gridSize][gridSize];
        fixedValues = new boolean[gridSize][gridSize];

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                cells[i][j] = new JTextField(2);
                cells[i][j].setHorizontalAlignment(JTextField.CENTER);
                cells[i][j].setFont(new Font("Arial", Font.BOLD, 26));
                cells[i][j].setBackground(new Color(255, 255, 255));
                cells[i][j].setForeground(new Color(0, 0, 0));
                cells[i][j].setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 1));

                // Set border with bold margins every 3 rows and columns
                int top = (i % 3 == 0) ? 3 : 1;
                int left = (j % 3 == 0) ? 3 : 1;
                int bottom = (i == gridSize - 1) ? 3 : 1;
                int right = (j == gridSize - 1) ? 3 : 1;
                cells[i][j].setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, new Color(0, 0, 0)));

                cells[i][j].addActionListener(e -> validateCellInput((JTextField) e.getSource()));

                gridPanel.add(cells[i][j]);
            }
        }

        gridPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0), 5));
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void validateCellInput(JTextField cell) {
        String text = cell.getText();
        if (!text.matches("\\d+") || text.length() > 1) {
            cell.setText("");
            cell.setBackground(Color.RED);
        } else {
            cell.setBackground(Color.WHITE);
        }
    }

    private void fillRandomValues() {
        Random random = new Random();
        int cellsToFill = gridSize * gridSize / 9;

        for (int i = 0; i < cellsToFill; i++) {
            int row = random.nextInt(gridSize);
            int col = random.nextInt(gridSize);
            int value = random.nextInt(gridSize) + 1;

            if (cells[row][col].getText().isEmpty()) {
                cells[row][col].setText(String.valueOf(value));
                cells[row][col].setEditable(false);
                cells[row][col].setBackground(new Color(200, 200, 200));
                fixedValues[row][col] = true;
            } else {
                i--; // Retry if cell is already filled
            }
        }
    }

    private boolean solveSudoku() {
        return solve(0, 0);
    }

    private boolean solve(int row, int col) {
        if (row == gridSize) {
            return true;
        }

        int nextRow = (col == gridSize - 1) ? row + 1 : row;
        int nextCol = (col == gridSize - 1) ? 0 : col + 1;

        if (fixedValues[row][col]) {
            return solve(nextRow, nextCol);
        }

        for (int num = 1; num <= gridSize; num++) {
            if (isValidMove(row, col, num)) {
                cells[row][col].setText(String.valueOf(num));
                highlightCell(cells[row][col], true); // Highlight with green color
                delay(solvingSpeed);

                if (solve(nextRow, nextCol)) {
                    return true;
                }

                cells[row][col].setText("");
                highlightCell(cells[row][col], false); // Highlight with red color
                delay(solvingSpeed);
            }
        }

        return false;
    }

    private boolean isValidMove(int row, int col, int num) {
        for (int i = 0; i < gridSize; i++) {
            if (cells[row][i].getText().equals(String.valueOf(num)) ||
                cells[i][col].getText().equals(String.valueOf(num))) {
                return false;
            }
        }

        int sqrt = (int) Math.sqrt(gridSize);
        int boxRowStart = row - row % sqrt;
        int boxColStart = col - col % sqrt;

        for (int r = boxRowStart; r < boxRowStart + sqrt; r++) {
            for (int d = boxColStart; d < boxColStart + sqrt; d++) {
                if (cells[r][d].getText().equals(String.valueOf(num))) {
                    return false;
                }
            }
        }

        return true;
    }

    private void highlightCell(JTextField cell, boolean isCorrect) {
        SwingUtilities.invokeLater(() -> cell.setBackground(isCorrect ? Color.GREEN : Color.RED));
    }

    private void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SudokuSolverVisualizer visualizer = new SudokuSolverVisualizer();
            visualizer.setVisible(true);
        });
    }

    private class ShadowButton extends JButton {
        private static final long serialVersionUID = 1L;
        private Color hoverBackgroundColor;
        private Color pressedBackgroundColor;

        public ShadowButton(String text, Color color) {
            super(text);
            super.setContentAreaFilled(false);
            this.hoverBackgroundColor = color.darker();
            this.pressedBackgroundColor = color.brighter();
            this.setBackground(color);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (getModel().isPressed()) {
                g.setColor(pressedBackgroundColor);
            } else if (getModel().isRollover()) {
                g.setColor(hoverBackgroundColor);
            } else {
                g.setColor(getBackground());
            }
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            super.paintComponent(g);
        }

        @Override
        public void setContentAreaFilled(boolean b) {
        }
    }
}
