package Pong;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

/**
 * Main Pong game class that creates and manages the game window.
 * This class extends JFrame to provide the main game window.
 */
public class Pong extends JFrame {
    private GamePanel gamePanel;
    
    /**
     * Constructor for the Pong game window.
     * Sets up the window properties and creates the game panel.
     */
    public Pong() {
        setTitle("Pong Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        gamePanel = new GamePanel();
        add(gamePanel);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    /**
     * Main entry point for the Pong game.
     * Uses SwingUtilities.invokeLater to ensure thread safety.
     * 
     * @param args Command line arguments (unused)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Pong());
    }
}

/**
 * Main game panel that handles all game logic, rendering, and user input.
 * This class manages the game state, collision detection, AI behavior, and drawing.
 */
class GamePanel extends JPanel implements KeyListener, ActionListener {
    // Game window dimensions
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    
    // Game object dimensions
    private static final int PADDLE_WIDTH = 15;
    private static final int PADDLE_HEIGHT = 100;
    private static final int BALL_SIZE = 20;
    
    // Movement speeds (pixels per frame)
    private static final int PADDLE_SPEED = 5;
    private static final int BALL_SPEED = 4;
    
    // Game objects
    private Paddle playerPaddle;    // Left paddle controlled by player
    private Paddle aiPaddle;        // Right paddle controlled by AI
    private Ball ball;              // The game ball
    
    // Game systems
    private Timer gameTimer;        // Main game loop timer (60 FPS)
    private Random random;          // Random number generator for AI and ball physics
    
    // Game state
    private int playerScore;        // Player's current score
    private int aiScore;            // AI's current score
    private boolean gameRunning;    // Whether the game is currently active
    private boolean gameStarted;    // Whether the ball is in motion
    private boolean upPressed;      // Whether up movement key is currently held
    private boolean downPressed;    // Whether down movement key is currently held
    private double ballSpeedMultiplier; // Multiplier for ball speed (increases on hits)
    
    /**
     * Constructor for the GamePanel.
     * Initializes the game panel, sets up input handling, and starts the game loop.
     */
    public GamePanel() {
        // Set up the panel properties
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        // Initialize game systems
        random = new Random();
        initializeGame();
        
        // Start the main game loop timer (60 FPS)
        gameTimer = new Timer(16, this); // 16ms = ~60 FPS
        gameTimer.start();
    }
    
    /**
     * Initializes or resets the game to its starting state.
     * Creates all game objects and resets scores and game state.
     */
    private void initializeGame() {
        // Create paddles at their starting positions
        playerPaddle = new Paddle(50, WINDOW_HEIGHT / 2 - PADDLE_HEIGHT / 2, PADDLE_WIDTH, PADDLE_HEIGHT);
        aiPaddle = new Paddle(WINDOW_WIDTH - 50 - PADDLE_WIDTH, WINDOW_HEIGHT / 2 - PADDLE_HEIGHT / 2, PADDLE_WIDTH, PADDLE_HEIGHT);
        
        // Create ball at center of screen
        ball = new Ball(WINDOW_WIDTH / 2 - BALL_SIZE / 2, WINDOW_HEIGHT / 2 - BALL_SIZE / 2, BALL_SIZE, BALL_SIZE);
        
        // Reset game state
        playerScore = 0;
        aiScore = 0;
        gameRunning = true;
        gameStarted = false;
        ballSpeedMultiplier = 1.0;
    }
    
    /**
     * Renders the game graphics to the screen.
     * Called automatically by Swing whenever the panel needs to be redrawn.
     * 
     * @param g The Graphics object used for drawing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable antialiasing for smoother graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw paddles in white
        g2d.setColor(Color.WHITE);
        g2d.fillRect(playerPaddle.getX(), playerPaddle.getY(), playerPaddle.getWidth(), playerPaddle.getHeight());
        g2d.fillRect(aiPaddle.getX(), aiPaddle.getY(), aiPaddle.getWidth(), aiPaddle.getHeight());
        
        // Draw ball in white
        g2d.fillOval(ball.getX(), ball.getY(), ball.getWidth(), ball.getHeight());
        
        // Draw dashed center line
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < WINDOW_HEIGHT; i += 20) {
            g2d.drawLine(WINDOW_WIDTH / 2, i, WINDOW_WIDTH / 2, i + 10);
        }
        
        // Draw scores
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        g2d.drawString(String.valueOf(playerScore), WINDOW_WIDTH / 4, 60);
        g2d.drawString(String.valueOf(aiScore), 3 * WINDOW_WIDTH / 4, 60);
        
        // Draw start message when game hasn't started
        if (!gameStarted) {
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString("Press SPACE to start", WINDOW_WIDTH / 2 - 120, WINDOW_HEIGHT / 2 + 50);
        }
        
        // Draw game over message when game ends
        if (!gameRunning) {
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            String winner = playerScore >= 10 ? "Player Wins!" : "AI Wins!";
            g2d.drawString(winner, WINDOW_WIDTH / 2 - 120, WINDOW_HEIGHT / 2);
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            g2d.drawString("Press R to restart", WINDOW_WIDTH / 2 - 80, WINDOW_HEIGHT / 2 + 40);
        }
    }
    
    /**
     * Called by the game timer every 16ms (60 FPS).
     * Updates game logic and triggers screen redraw.
     * 
     * @param e The ActionEvent from the timer
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Only update game logic if game is running and started
        if (gameRunning && gameStarted) {
            updateGame();
        }
        // Always redraw the screen to show current state
        repaint();
    }
    
    /**
     * Updates all game logic for one frame.
     * Called every 16ms when the game is running and started.
     */
    private void updateGame() {
        // Update ball position
        ball.update();
        
        // Handle player paddle movement based on key presses
        updatePlayerPaddle();
        
        // Handle AI paddle movement
        updateAIPaddle();
        
        // Check for collisions between ball and other objects
        checkCollisions();
        
        // Check if anyone has scored
        checkScoring();
    }
    
    /**
     * Updates the player paddle position based on currently pressed keys.
     * Moves the paddle up or down if the corresponding keys are held.
     */
    private void updatePlayerPaddle() {
        if (upPressed) {
            playerPaddle.moveUp(PADDLE_SPEED);
        }
        if (downPressed) {
            playerPaddle.moveDown(PADDLE_SPEED);
        }
    }
    
    /**
     * Updates the AI paddle position using simple AI logic.
     * AI tries to follow the ball with some randomness and reaction delay to make it more human-like.
     */
    private void updateAIPaddle() {
        int aiCenterY = aiPaddle.getY() + aiPaddle.getHeight() / 2;
        int ballCenterY = ball.getY() + ball.getHeight() / 2;
        
        // Add randomness to AI targeting (not always perfect aim)
        int targetOffset = random.nextInt(40) - 20; // Random offset between -20 and +20 pixels
        int targetY = ballCenterY + targetOffset;
        
        // Add some reaction delay - AI doesn't always move (more human-like)
        if (random.nextDouble() < 1) {
            if (aiCenterY < targetY - 15) {
                aiPaddle.moveDown(PADDLE_SPEED);
            } else if (aiCenterY > targetY + 15) {
                aiPaddle.moveUp(PADDLE_SPEED);
            }
        }
    }
    
    /**
     * Checks for collisions between the ball and other game objects.
     * Handles ball bouncing off walls and paddles with angle-based physics.
     */
    private void checkCollisions() {
        // Ball bouncing off top and bottom walls
        if (ball.getY() <= 0 || ball.getY() >= WINDOW_HEIGHT - ball.getHeight()) {
            ball.reverseY();
        }
        
        // Ball hitting player paddle
        if (playerPaddle.intersects(ball)) {
            ball.reverseX();
            increaseBallSpeed();
            calculateBallAngle(playerPaddle, ball);
        }
        
        // Ball hitting AI paddle
        if (aiPaddle.intersects(ball)) {
            ball.reverseX();
            increaseBallSpeed();
            calculateBallAngle(aiPaddle, ball);
        }
    }
    
    /**
     * Calculates the new ball angle based on which section of the paddle was hit.
     * Divides the paddle into 5 sections, each producing a different bounce angle.
     * 
     * @param paddle The paddle that was hit
     * @param ball The ball that hit the paddle
     */
    private void calculateBallAngle(Paddle paddle, Ball ball) {
        // Calculate which section of the paddle the ball hit (0-4)
        int paddleCenterY = paddle.getY() + paddle.getHeight() / 2;
        int ballCenterY = ball.getY() + ball.getHeight() / 2;
        int relativeY = ballCenterY - paddleCenterY;
        
        // Divide paddle into 5 sections
        int sectionHeight = paddle.getHeight() / 5;
        int section = (relativeY + paddle.getHeight() / 2) / sectionHeight;
        
        // Clamp section to valid range (0-4)
        section = Math.max(0, Math.min(4, section));
        
        // Define bounce angles for each section (in degrees)
        // Section 0: -60°, Section 1: -30°, Section 2: 0°, Section 3: 30°, Section 4: 60°
        double[] angles = {-60, -30, 0, 30, 60};
        double angle = angles[section];
        
        // Convert angle to radians for trigonometry
        double angleRad = Math.toRadians(angle);
        
        // Calculate new velocity using current speed multiplier
        double speed = BALL_SPEED * ballSpeedMultiplier;
        int newVelocityX = (int) (speed * Math.cos(angleRad));
        int newVelocityY = (int) (speed * Math.sin(angleRad));
        
        // Ensure ball moves away from the paddle it hit
        if (paddle == playerPaddle) {
            newVelocityX = Math.abs(newVelocityX); // Move right (away from player)
        } else {
            newVelocityX = -Math.abs(newVelocityX); // Move left (away from AI)
        }
        
        ball.setVelocity(newVelocityX, newVelocityY);
    }
    
    /**
     * Checks if either player has scored and handles scoring logic.
     * Resets the ball position when someone scores and ends the game at 10 points.
     */
    private void checkScoring() {
        // AI scores (ball went past player paddle)
        if (ball.getX() < 0) {
            aiScore++;
            resetBall();
        } 
        // Player scores (ball went past AI paddle)
        else if (ball.getX() > WINDOW_WIDTH) {
            playerScore++;
            resetBall();
        }
        
        // End game when someone reaches 10 points
        if (playerScore >= 10 || aiScore >= 10) {
            gameRunning = false;
        }
    }
    
    /**
     * Increases the ball speed by 10% each time it hits a paddle.
     * Caps the maximum speed at 3x the original speed to keep the game playable.
     */
    private void increaseBallSpeed() {
        ballSpeedMultiplier += 0.1; // Increase speed by 10% each hit
        // Cap the maximum speed to prevent it from becoming too fast
        ballSpeedMultiplier = Math.min(ballSpeedMultiplier, 3.0);
    }
    
    /**
     * Resets the ball to the center of the screen and resets game state.
     * Called when someone scores a point.
     */
    private void resetBall() {
        ball.reset(WINDOW_WIDTH / 2 - BALL_SIZE / 2, WINDOW_HEIGHT / 2 - BALL_SIZE / 2);
        gameStarted = false;
        ballSpeedMultiplier = 1.0; // Reset speed multiplier
    }
    
    /**
     * Handles key press events for game controls.
     * Manages game start, restart, and player paddle movement.
     * 
     * @param e The KeyEvent containing information about the pressed key
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        // Start the game when SPACE is pressed
        if (key == KeyEvent.VK_SPACE && !gameStarted && gameRunning) {
            gameStarted = true;
            ball.setVelocity(BALL_SPEED, random.nextInt(3) - 1);
        }
        
        // Restart the game when R is pressed (only when game is over)
        if (key == KeyEvent.VK_R && !gameRunning) {
            initializeGame();
        }
        
        // Handle player paddle movement (only during active gameplay)
        if (gameRunning && gameStarted) {
            if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
                upPressed = true;
            }
            if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
                downPressed = true;
            }
        }
    }
    
    /**
     * Handles key release events for smooth paddle movement.
     * Stops paddle movement when movement keys are released.
     * 
     * @param e The KeyEvent containing information about the released key
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
            upPressed = false;
        }
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
            downPressed = false;
        }
    }
    
    /**
     * Handles key typed events (unused in this implementation).
     * Required by KeyListener interface but not needed for this game.
     * 
     * @param e The KeyEvent (unused)
     */
    @Override
    public void keyTyped(KeyEvent e) {
        // Not needed for this implementation
    }
}

/**
 * Represents a paddle in the Pong game.
 * Handles paddle movement and collision detection with the ball.
 */
class Paddle {
    private int x, y, width, height;
    
    /**
     * Creates a new paddle at the specified position and size.
     * 
     * @param x The x-coordinate of the paddle's top-left corner
     * @param y The y-coordinate of the paddle's top-left corner
     * @param width The width of the paddle
     * @param height The height of the paddle
     */
    public Paddle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    /**
     * Moves the paddle up by the specified speed.
     * Prevents the paddle from moving above the top of the screen.
     * 
     * @param speed The number of pixels to move up
     */
    public void moveUp(int speed) {
        if (y > 0) {
            y -= speed;
        }
    }
    
    /**
     * Moves the paddle down by the specified speed.
     * Prevents the paddle from moving below the bottom of the screen.
     * 
     * @param speed The number of pixels to move down
     */
    public void moveDown(int speed) {
        if (y < 600 - height) {
            y += speed;
        }
    }
    
    /**
     * Checks if this paddle intersects with the given ball.
     * Uses Axis-Aligned Bounding Box (AABB) collision detection.
     * 
     * @param ball The ball to check collision with
     * @return true if the paddle and ball intersect, false otherwise
     */
    public boolean intersects(Ball ball) {
        return x < ball.getX() + ball.getWidth() &&
               x + width > ball.getX() &&
               y < ball.getY() + ball.getHeight() &&
               y + height > ball.getY();
    }
    
    // Getters for paddle properties
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}

/**
 * Represents the ball in the Pong game.
 * Handles ball movement, physics, and collision responses.
 */
class Ball {
    private int x, y, width, height;
    private int velocityX, velocityY;
    
    /**
     * Creates a new ball at the specified position and size.
     * Ball starts with zero velocity (stationary).
     * 
     * @param x The x-coordinate of the ball's top-left corner
     * @param y The y-coordinate of the ball's top-left corner
     * @param width The width of the ball
     * @param height The height of the ball
     */
    public Ball(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.velocityX = 0;
        this.velocityY = 0;
    }
    
    /**
     * Updates the ball's position based on its current velocity.
     * Called every frame to move the ball.
     */
    public void update() {
        x += velocityX;
        y += velocityY;
    }
    
    /**
     * Sets the ball's velocity in both X and Y directions.
     * 
     * @param velocityX The horizontal velocity (pixels per frame)
     * @param velocityY The vertical velocity (pixels per frame)
     */
    public void setVelocity(int velocityX, int velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }
    
    /**
     * Reverses the ball's horizontal direction (bounces off vertical surfaces).
     */
    public void reverseX() {
        velocityX = -velocityX;
    }
    
    /**
     * Reverses the ball's vertical direction (bounces off horizontal surfaces).
     */
    public void reverseY() {
        velocityY = -velocityY;
    }
    
    /**
     * Sets only the vertical velocity of the ball.
     * Used for adding randomness to ball direction after paddle hits.
     * 
     * @param velocityY The new vertical velocity
     */
    public void setVelocityY(int velocityY) {
        this.velocityY = velocityY;
    }
    
    /**
     * Resets the ball to a specific position with zero velocity.
     * Called when someone scores a point.
     * 
     * @param x The new x-coordinate
     * @param y The new y-coordinate
     */
    public void reset(int x, int y) {
        this.x = x;
        this.y = y;
        this.velocityX = 0;
        this.velocityY = 0;
    }
    
    // Getters for ball properties
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getVelocityX() { return velocityX; }
    public int getVelocityY() { return velocityY; }
}
