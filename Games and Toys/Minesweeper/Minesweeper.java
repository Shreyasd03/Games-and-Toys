package Minesweeper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

/**
 * Minesweeper Game Implementation
 * 
 * A complete Minesweeper game with customizable board dimensions and mine count.
 * Features include:
 * - Customizable board size (rows, columns, mine count)
 * - Options menu with preset difficulty levels
 * - Dynamic window resizing based on board dimensions
 * - Left-click to reveal cells, right-click to flag mines
 * - Win/lose detection with proper game state management
 * 
 */
public class Minesweeper extends JFrame {
    private JButton[][] buttons;
    private boolean[][] mines;
    private boolean[][] revealed;
    private boolean[][] flagged;
    private int rows, cols, mineCount;
    private int revealedCount;
    private boolean gameOver;
    private JLabel statusLabel;
    private JLabel mineLabel;
    private JMenuBar menuBar;
    private JMenu gameMenu;
    private JMenu optionsMenu;
    
    // Default game values
    private static final int DEFAULT_ROWS = 9;
    private static final int DEFAULT_COLS = 9;
    private static final int DEFAULT_MINES = 10;
    
    /**
     * Default constructor - creates a Minesweeper game with default settings (9x9, 10 mines)
     */
    public Minesweeper() {
        this.rows = DEFAULT_ROWS;
        this.cols = DEFAULT_COLS;
        this.mineCount = DEFAULT_MINES;
        initializeGame();
    }
    
    /**
     * Custom constructor - creates a Minesweeper game with specified dimensions and mine count
     * @param rows Number of rows in the game board
     * @param cols Number of columns in the game board
     * @param mineCount Number of mines to place on the board
     */
    public Minesweeper(int rows, int cols, int mineCount) {
        this.rows = rows;
        this.cols = cols;
        this.mineCount = mineCount;
        initializeGame();
    }
    
    /**
     * Initializes the game by setting up the GUI components, game arrays, and window properties
     */
    private void initializeGame() {
        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create menu bar
        createMenuBar();
        setJMenuBar(menuBar);
        
        // Create status panel
        JPanel statusPanel = new JPanel(new FlowLayout());
        statusLabel = new JLabel("Click a cell to start!");
        mineLabel = new JLabel("Mines: " + mineCount);
        statusPanel.add(statusLabel);
        statusPanel.add(mineLabel);
        add(statusPanel, BorderLayout.NORTH);
        
        // Initialize game arrays
        buttons = new JButton[rows][cols];
        mines = new boolean[rows][cols];
        revealed = new boolean[rows][cols];
        flagged = new boolean[rows][cols];
        revealedCount = 0;
        gameOver = false;
        
        // Create game board
        createGameBoard();
        
        // Set window size based on board dimensions
        int cellSize = 35;
        int windowWidth = cols * cellSize + 20;
        int windowHeight = rows * cellSize + 100;
        setSize(windowWidth, windowHeight);
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    /**
     * Creates the menu bar with Game and Options menus
     * Game menu: New Game, Exit
     * Options menu: Custom Game, Beginner, Intermediate, Expert difficulty levels
     */
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // Game menu
        gameMenu = new JMenu("Game");
        JMenuItem newGameItem = new JMenuItem("New Game");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        newGameItem.addActionListener(e -> newGame());
        exitItem.addActionListener(e -> System.exit(0));
        
        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);
        
        // Options menu
        optionsMenu = new JMenu("Options");
        JMenuItem customGameItem = new JMenuItem("Custom Game...");
        JMenuItem beginnerItem = new JMenuItem("Beginner (9x9, 10 mines)");
        JMenuItem intermediateItem = new JMenuItem("Intermediate (16x16, 40 mines)");
        JMenuItem expertItem = new JMenuItem("Expert (16x30, 99 mines)");
        
        customGameItem.addActionListener(e -> showCustomGameDialog());
        beginnerItem.addActionListener(e -> setGameSettings(9, 9, 10));
        intermediateItem.addActionListener(e -> setGameSettings(16, 16, 40));
        expertItem.addActionListener(e -> setGameSettings(16, 30, 99));
        
        optionsMenu.add(customGameItem);
        optionsMenu.addSeparator();
        optionsMenu.add(beginnerItem);
        optionsMenu.add(intermediateItem);
        optionsMenu.add(expertItem);
        
        menuBar.add(gameMenu);
        menuBar.add(optionsMenu);
    }
    
    /**
     * Creates the game board by initializing buttons in a grid layout
     * Each button has mouse listeners for left-click (reveal) and right-click (flag) actions
     */
    private void createGameBoard() {
        JPanel gamePanel = new JPanel(new GridLayout(rows, cols));
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setPreferredSize(new Dimension(35, 35));
                buttons[i][j].setFont(new Font("Arial", Font.BOLD, 14));
                buttons[i][j].setMargin(new Insets(0, 0, 0, 0));
                
                final int row = i;
                final int col = j;
                
                buttons[i][j].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (gameOver) return;
                        
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            leftClick(row, col);
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            rightClick(row, col);
                        }
                    }
                });
                
                gamePanel.add(buttons[i][j]);
            }
        }
        
        add(gamePanel, BorderLayout.CENTER);
    }
    
    /**
     * Handles left-click events on game cells
     * Places mines on first click, then reveals the clicked cell
     * @param row Row index of the clicked cell
     * @param col Column index of the clicked cell
     */
    private void leftClick(int row, int col) {
        if (flagged[row][col] || revealed[row][col]) return;
        
        // First click - place mines
        if (revealedCount == 0) {
            placeMines(row, col);
        }
        
        revealCell(row, col);
    }
    
    /**
     * Handles right-click events on game cells for flagging/unflagging mines
     * @param row Row index of the clicked cell
     * @param col Column index of the clicked cell
     */
    private void rightClick(int row, int col) {
        if (revealed[row][col] || gameOver) return;
        
        if (flagged[row][col]) {
            flagged[row][col] = false;
            buttons[row][col].setText("");
            buttons[row][col].setBackground(null);
        } else {
            flagged[row][col] = true;
            buttons[row][col].setText("F");
            buttons[row][col].setBackground(Color.YELLOW);
        }
        
        updateMineLabel();
    }
    
    /**
     * Randomly places mines on the board, ensuring the first clicked cell is safe
     * @param firstRow Row index of the first clicked cell (will not contain a mine)
     * @param firstCol Column index of the first clicked cell (will not contain a mine)
     */
    private void placeMines(int firstRow, int firstCol) {
        Random random = new Random();
        int minesPlaced = 0;
        
        while (minesPlaced < mineCount) {
            int row = random.nextInt(rows);
            int col = random.nextInt(cols);
            
            if (!mines[row][col] && !(row == firstRow && col == firstCol)) {
                mines[row][col] = true;
                minesPlaced++;
            }
        }
    }
    
    /**
     * Reveals a cell and handles game logic (mine detection, number display, auto-reveal)
     * @param row Row index of the cell to reveal
     * @param col Column index of the cell to reveal
     */
    private void revealCell(int row, int col) {
        if (revealed[row][col] || flagged[row][col]) return;
        
        revealed[row][col] = true;
        revealedCount++;
        
        if (mines[row][col]) {
            // Game over - hit a mine
            gameOver = true;
            buttons[row][col].setText("X");
            buttons[row][col].setBackground(Color.RED);
            revealAllMines();
            statusLabel.setText("Game Over! You hit a mine!");
            return;
        }
        
        int adjacentMines = countAdjacentMines(row, col);
        if (adjacentMines > 0) {
            buttons[row][col].setText(String.valueOf(adjacentMines));
        } else {
            buttons[row][col].setText("");
        }
        buttons[row][col].setBackground(Color.LIGHT_GRAY);
        buttons[row][col].setEnabled(false);
        buttons[row][col].repaint();
        
        // If no adjacent mines, reveal adjacent cells
        if (adjacentMines == 0) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int newRow = row + i;
                    int newCol = col + j;
                    if (isValidCell(newRow, newCol)) {
                        revealCell(newRow, newCol);
                    }
                }
            }
        }
        
        // Check for win condition
        if (revealedCount == (rows * cols - mineCount)) {
            gameOver = true;
            statusLabel.setText("Congratulations! You won!");
            JOptionPane.showMessageDialog(this, "Congratulations! You won!", "Victory!", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Counts the number of mines adjacent to a given cell
     * @param row Row index of the cell to check
     * @param col Column index of the cell to check
     * @return Number of adjacent mines (0-8)
     */
    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = row + i;
                int newCol = col + j;
                if (isValidCell(newRow, newCol) && mines[newRow][newCol]) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Checks if the given coordinates are within the bounds of the game board
     * @param row Row index to validate
     * @param col Column index to validate
     * @return true if coordinates are valid, false otherwise
     */
    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }
    
    /**
     * Reveals all mines on the board when the game ends (player hits a mine)
     * Shows "M" on all mine cells that weren't flagged
     */
    private void revealAllMines() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mines[i][j] && !flagged[i][j]) {
                    buttons[i][j].setText("M");
                    buttons[i][j].setBackground(Color.RED);
                    buttons[i][j].repaint();
                }
            }
        }
    }
    
    /**
     * Updates the mine counter label to show remaining unflagged mines
     * Calculates: total mines - flagged cells = remaining mines
     */
    private void updateMineLabel() {
        int flaggedCount = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (flagged[i][j]) {
                    flaggedCount++;
                }
            }
        }
        mineLabel.setText("Mines: " + (mineCount - flaggedCount));
    }
    
    /**
     * Shows a dialog for customizing game settings (rows, columns, mine count)
     * Validates input and creates a new game with the specified settings
     */
    private void showCustomGameDialog() {
        JDialog dialog = new JDialog(this, "Custom Game Settings", true);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        
        JLabel rowsLabel = new JLabel("Rows (1-30):");
        JTextField rowsField = new JTextField(String.valueOf(rows));
        
        JLabel colsLabel = new JLabel("Columns (1-30):");
        JTextField colsField = new JTextField(String.valueOf(cols));
        
        JLabel minesLabel = new JLabel("Mines:");
        JTextField minesField = new JTextField(String.valueOf(mineCount));
        
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        
        okButton.addActionListener(e -> {
            try {
                int newRows = Integer.parseInt(rowsField.getText());
                int newCols = Integer.parseInt(colsField.getText());
                int newMines = Integer.parseInt(minesField.getText());
                
                if (newRows < 1 || newRows > 30 || newCols < 1 || newCols > 30) {
                    JOptionPane.showMessageDialog(dialog, "Rows and columns must be between 1 and 30!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (newMines < 1 || newMines >= newRows * newCols) {
                    JOptionPane.showMessageDialog(dialog, "Mines must be between 1 and " + (newRows * newCols - 1) + "!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                setGameSettings(newRows, newCols, newMines);
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        dialog.add(rowsLabel);
        dialog.add(rowsField);
        dialog.add(colsLabel);
        dialog.add(colsField);
        dialog.add(minesLabel);
        dialog.add(minesField);
        dialog.add(okButton);
        dialog.add(cancelButton);
        
        dialog.setVisible(true);
    }
    
    /**
     * Updates game settings and starts a new game with the specified parameters
     * @param newRows Number of rows for the new game
     * @param newCols Number of columns for the new game
     * @param newMines Number of mines for the new game
     */
    private void setGameSettings(int newRows, int newCols, int newMines) {
        this.rows = newRows;
        this.cols = newCols;
        this.mineCount = newMines;
        newGame();
    }
    
    /**
     * Resets the game by clearing the current board and creating a new one
     * Maintains current game settings (rows, cols, mineCount) but resets game state
     */
    private void newGame() {
        // Remove current game board
        getContentPane().removeAll();
        
        // Recreate status panel
        JPanel statusPanel = new JPanel(new FlowLayout());
        statusLabel = new JLabel("Click a cell to start!");
        mineLabel = new JLabel("Mines: " + mineCount);
        statusPanel.add(statusLabel);
        statusPanel.add(mineLabel);
        add(statusPanel, BorderLayout.NORTH);
        
        // Recreate menu bar
        setJMenuBar(menuBar);
        
        // Initialize game arrays
        buttons = new JButton[rows][cols];
        mines = new boolean[rows][cols];
        revealed = new boolean[rows][cols];
        flagged = new boolean[rows][cols];
        revealedCount = 0;
        gameOver = false;
        
        // Create game board
        createGameBoard();
        
        // Resize window
        int cellSize = 35;
        int windowWidth = cols * cellSize + 20;
        int windowHeight = rows * cellSize + 100;
        setSize(windowWidth, windowHeight);
        setLocationRelativeTo(null);
        
        revalidate();
        repaint();
    }
    
    /**
     * Main method - entry point for the Minesweeper application
     * Creates and displays the game window on the Event Dispatch Thread
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Minesweeper().setVisible(true);
        });
    }
}
