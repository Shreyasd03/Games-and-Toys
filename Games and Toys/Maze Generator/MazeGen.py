"""
Maze Generator using Recursive Backtracking Algorithm

This module implements a maze generator using the recursive backtracking algorithm
with a graphical user interface built with tkinter. The generator creates perfect
mazes (exactly one path between any two points) with customizable dimensions.

Features:
- Interactive GUI with real-time visualization
- Adjustable maze dimensions (11x11 to 51x51)
- Animated generation process
- Recursive backtracking algorithm implementation

"""

import tkinter as tk
from tkinter import Canvas, Button, Label, Scale, Frame
import random
import time

class MazeGenerator:
    """
    A maze generator that uses the recursive backtracking algorithm to create perfect mazes.
    
    The class provides both instant generation and animated generation capabilities
    with a graphical user interface for interactive maze creation.
    """
    
    def __init__(self, width=21, height=21, cell_size=20):
        """
        Initialize the maze generator with specified dimensions.
        
        Args:
            width (int): Width of the maze in cells (must be odd for proper structure)
            height (int): Height of the maze in cells (must be odd for proper structure)
            cell_size (int): Size of each cell in pixels for display
        """
        # Store maze dimensions and properties
        self.width = width
        self.height = height
        self.cell_size = cell_size
        self.maze = []          # 2D array representing the maze (0=wall, 1=path)
        self.visited = []       # 2D array for tracking visited cells during animation
        
        # Initialize GUI components
        self.root = tk.Tk()
        self.root.title("Maze Generator - Recursive Backtracking")
        self.root.resizable(True, True)
        
        # Calculate initial window size based on maze dimensions
        self.update_window_size()
        
        # Create control panel with buttons and sliders
        self.setup_controls()
        
        # Create canvas for maze display
        self.canvas = Canvas(
            self.root,
            width=width * cell_size,
            height=height * cell_size,
            bg='white',
            highlightthickness=2,
            highlightbackground='black'
        )
        self.canvas.pack(pady=10)
        
        # Initialize the maze grid
        self.initialize_maze()
        
    def setup_controls(self):
        """
        Create the control panel with sliders for maze dimensions and action buttons.
        
        The control panel includes:
        - Width and height sliders (11-51, odd numbers only)
        - Generate Maze button for instant generation
        - Clear button to reset the maze
        - Animate Generation button for step-by-step visualization
        """
        control_frame = Frame(self.root, bg='lightgray', height=120)
        control_frame.pack(fill='x', padx=10, pady=5)
        control_frame.pack_propagate(False)
        
        # Size controls
        size_frame = Frame(control_frame, bg='lightgray')
        size_frame.pack(side='left', padx=10, pady=10)
        
        Label(size_frame, text="Width:", bg='lightgray', font=('Arial', 9, 'bold')).pack(side='left')
        self.width_scale = Scale(size_frame, from_=11, to=51, orient='horizontal', 
                                resolution=2, length=120, command=self.update_width)
        self.width_scale.set(self.width)
        self.width_scale.pack(side='left', padx=5)
        
        Label(size_frame, text="Height:", bg='lightgray', font=('Arial', 9, 'bold')).pack(side='left', padx=(15,0))
        self.height_scale = Scale(size_frame, from_=11, to=51, orient='horizontal', 
                                 resolution=2, length=120, command=self.update_height)
        self.height_scale.set(self.height)
        self.height_scale.pack(side='left', padx=5)
        
        # Buttons - arranged in two rows to prevent text cutoff
        button_frame = Frame(control_frame, bg='lightgray')
        button_frame.pack(side='right', padx=10, pady=10)
        
        # First row of buttons
        button_row1 = Frame(button_frame, bg='lightgray')
        button_row1.pack(side='top', pady=2)
        
        Button(button_row1, text="Generate Maze", command=self.generate_maze, 
               bg='lightblue', font=('Arial', 9, 'bold'), width=12, height=1).pack(side='left', padx=3)
        
        Button(button_row1, text="Clear", command=self.clear_maze, 
               bg='lightcoral', font=('Arial', 9, 'bold'), width=12, height=1).pack(side='left', padx=3)
        
        # Second row of buttons
        button_row2 = Frame(button_frame, bg='lightgray')
        button_row2.pack(side='top', pady=2)
        
        Button(button_row2, text="Animate Generation", command=self.animate_generation, 
               bg='lightgreen', font=('Arial', 9, 'bold'), width=16, height=1).pack(side='left', padx=3)
        
    def update_window_size(self):
        """
        Calculate and set the optimal window size based on current maze dimensions.
        
        The window size is dynamically calculated to accommodate:
        - The maze canvas (width * cell_size)
        - Control panel height (200px)
        - Minimum width of 600px for proper control visibility
        - Automatic centering on the screen
        """
        # Calculate canvas size
        canvas_width = self.width * self.cell_size
        canvas_height = self.height * self.cell_size
        
        # Add padding for controls and margins
        window_width = max(canvas_width + 40, 600)  # Minimum width of 600
        window_height = canvas_height + 200  # Extra space for controls
        
        # Set window size
        self.root.geometry(f"{window_width}x{window_height}")
        
        # Center the window on screen
        self.root.update_idletasks()
        x = (self.root.winfo_screenwidth() // 2) - (window_width // 2)
        y = (self.root.winfo_screenheight() // 2) - (window_height // 2)
        self.root.geometry(f"{window_width}x{window_height}+{x}+{y}")
        
    def update_width(self, value):
        """
        Update the maze width when the width slider is moved.
        
        Args:
            value (str): The new width value from the slider
            
        Ensures the width is odd for proper maze structure and triggers
        a complete maze resize and redraw.
        """
        self.width = int(value)
        if self.width % 2 == 0:
            self.width += 1
            self.width_scale.set(self.width)
        self.resize_maze()
        
    def update_height(self, value):
        """
        Update the maze height when the height slider is moved.
        
        Args:
            value (str): The new height value from the slider
            
        Ensures the height is odd for proper maze structure and triggers
        a complete maze resize and redraw.
        """
        self.height = int(value)
        if self.height % 2 == 0:
            self.height += 1
            self.height_scale.set(self.height)
        self.resize_maze()
        
    def resize_maze(self):
        """
        Resize the maze grid and canvas when dimensions change.
        
        This method:
        1. Updates the canvas size to match new dimensions
        2. Recalculates the window size
        3. Reinitializes the maze grid
        4. Redraws the empty maze
        """
        # Update canvas size
        self.canvas.config(width=self.width * self.cell_size, 
                          height=self.height * self.cell_size)
        
        # Update window size to fit new maze
        self.update_window_size()
        
        # Reinitialize maze
        self.initialize_maze()
        self.draw_maze()
        
    def initialize_maze(self):
        """
        Initialize the maze grid and visited tracking array.
        
        Creates two 2D arrays:
        - maze: Grid where 0 represents walls and 1 represents paths
        - visited: Boolean array for tracking visited cells during animation
        """
        # Create maze grid (0 = wall, 1 = path)
        self.maze = [[0 for _ in range(self.width)] for _ in range(self.height)]
        
        # Create visited array for tracking
        self.visited = [[False for _ in range(self.width)] for _ in range(self.height)]
        
    def generate_maze(self):
        """
        Generate a complete maze using the recursive backtracking algorithm.
        
        This method:
        1. Ensures maze dimensions are odd for proper structure
        2. Selects a random starting position
        3. Calls the recursive backtracking algorithm
        4. Draws the final maze on the canvas
        
        The algorithm creates a perfect maze with exactly one path between any two points.
        """
        self.initialize_maze()
        
        # Ensure dimensions are odd for proper maze structure
        if self.width % 2 == 0:
            self.width += 1
        if self.height % 2 == 0:
            self.height += 1
            
        # Start from a random cell (must be odd coordinates)
        start_x = random.randint(1, self.width - 2)
        start_y = random.randint(1, self.height - 2)
        
        if start_x % 2 == 0:
            start_x = (start_x + 1) % (self.width - 1)
        if start_y % 2 == 0:
            start_y = (start_y + 1) % (self.height - 1)
            
        # Generate the maze
        self._recursive_backtrack(start_x, start_y)
        
        # Draw the final maze
        self.draw_maze()
        
    def animate_generation(self):
        """
        Generate a maze with step-by-step animation showing the algorithm's progress.
        
        This method provides visual feedback by:
        1. Drawing the initial maze with all black walls
        2. Showing the current cell being explored in light blue
        3. Displaying carved paths in white
        4. Using small delays to make the process visible
        
        Perfect for understanding how the recursive backtracking algorithm works.
        """
        self.initialize_maze()
        
        # Ensure dimensions are odd
        if self.width % 2 == 0:
            self.width += 1
        if self.height % 2 == 0:
            self.height += 1
            
        # Start from a random cell
        start_x = random.randint(1, self.width - 2)
        start_y = random.randint(1, self.height - 2)
        
        if start_x % 2 == 0:
            start_x = (start_x + 1) % (self.width - 1)
        if start_y % 2 == 0:
            start_y = (start_y + 1) % (self.height - 1)
            
        # Clear canvas and draw initial maze (all walls black)
        self.canvas.delete("all")
        self.draw_maze()  # Draw all walls as black first
        
        # Animate the generation
        self._recursive_backtrack_animated(start_x, start_y)
        
    def _recursive_backtrack(self, x, y):
        """
        Core recursive backtracking algorithm for maze generation.
        
        This is the heart of the maze generation algorithm. It works by:
        1. Marking the current cell as a path
        2. Randomly trying all four directions (right, down, left, up)
        3. For each valid direction, carving through the wall to the next cell
        4. Recursively exploring the new cell
        5. Automatically backtracking when no more directions are available
        
        Args:
            x (int): Current cell's x-coordinate
            y (int): Current cell's y-coordinate
            
        The algorithm ensures every cell is visited exactly once, creating
        a perfect maze with no cycles and exactly one path between any two points.
        """
        # Mark current cell as path
        self.maze[y][x] = 1
        
        # Define possible directions (dx, dy) - moving 2 cells at a time
        directions = [(0, 2), (2, 0), (0, -2), (-2, 0)]  # right, down, left, up
        random.shuffle(directions)
        
        # Try each direction
        for dx, dy in directions:
            new_x, new_y = x + dx, y + dy
            
            # Check if new position is valid and unvisited
            if (0 <= new_x < self.width and 
                0 <= new_y < self.height and 
                self.maze[new_y][new_x] == 0):
                
                # Carve path through the wall between current and new cell
                wall_x, wall_y = x + dx // 2, y + dy // 2
                self.maze[wall_y][wall_x] = 1
                
                # Recursively visit the new cell
                self._recursive_backtrack(new_x, new_y)
                
    def _recursive_backtrack_animated(self, x, y):
        """
        Animated version of the recursive backtracking algorithm.
        
        This method provides the same functionality as _recursive_backtrack but
        with visual feedback during the generation process:
        - Current cell being explored: light blue
        - Carved paths: white
        - Unvisited walls: black
        
        Args:
            x (int): Current cell's x-coordinate
            y (int): Current cell's y-coordinate
            
        The animation helps visualize how the algorithm explores and backtracks
        through the maze, making it easier to understand the process.
        """
        # Mark current cell as path
        self.maze[y][x] = 1
        self.visited[y][x] = True
        
        # Draw current cell
        self.draw_cell(x, y, 'lightblue')
        self.root.update()
        time.sleep(0.01)  # Small delay for animation
        
        # Define possible directions
        directions = [(0, 2), (2, 0), (0, -2), (-2, 0)]
        random.shuffle(directions)
        
        for dx, dy in directions:
            new_x, new_y = x + dx, y + dy
            
            if (0 <= new_x < self.width and 
                0 <= new_y < self.height and 
                self.maze[new_y][new_x] == 0):
                
                # Carve path through wall
                wall_x, wall_y = x + dx // 2, y + dy // 2
                self.maze[wall_y][wall_x] = 1
                
                # Draw the wall being carved
                self.draw_cell(wall_x, wall_y, 'white')
                self.root.update()
                time.sleep(0.005)
                
                # Recursively visit new cell
                self._recursive_backtrack_animated(new_x, new_y)
                
        # Mark as visited and draw final path
        self.draw_cell(x, y, 'white')
        self.root.update()
        
    def draw_maze(self):
        """
        Draw the complete maze on the canvas.
        
        This method renders the current state of the maze grid:
        - Walls (value 0): drawn in black
        - Paths (value 1): drawn in white with gray outlines
        
        Clears the canvas first, then draws each cell according to its value.
        """
        self.canvas.delete("all")
        
        for y in range(self.height):
            for x in range(self.width):
                if self.maze[y][x] == 0:  # Wall
                    self.draw_cell(x, y, 'black')
                else:  # Path
                    self.draw_cell(x, y, 'white')
                    
    def draw_cell(self, x, y, color):
        """
        Draw a single cell on the canvas at the specified coordinates.
        
        Args:
            x (int): X-coordinate of the cell
            y (int): Y-coordinate of the cell
            color (str): Color to fill the cell with
            
        Special handling for black cells (walls) to ensure they have
        solid black fill and outline for better visibility.
        """
        x1 = x * self.cell_size
        y1 = y * self.cell_size
        x2 = x1 + self.cell_size
        y2 = y1 + self.cell_size
        
        # Keep walls black, paths white
        if color == 'black':
            self.canvas.create_rectangle(x1, y1, x2, y2, fill='black', outline='black')
        else:
            self.canvas.create_rectangle(x1, y1, x2, y2, fill=color, outline='gray')
        
    def clear_maze(self):
        """
        Clear the current maze and reset to initial state.
        
        This method:
        1. Reinitializes the maze grid (all walls)
        2. Redraws the empty maze on the canvas
        3. Resets all internal state variables
        """
        self.initialize_maze()
        self.draw_maze()
        
    def print_maze(self):
        """
        Print the current maze to the console for debugging purposes.
        
        Uses '#' to represent walls and spaces to represent paths,
        providing a text-based view of the maze structure.
        """
        print("Maze:")
        for row in self.maze:
            print(''.join(['#' if cell == 0 else ' ' for cell in row]))
            
    def run(self):
        """
        Start the maze generator application.
        
        This method begins the tkinter main event loop, which handles
        all user interactions and keeps the application running until
        the user closes the window.
        """
        self.root.mainloop()

def main():
    """
    Main function to run the maze generator application.
    
    This function:
    1. Displays information about the maze generator
    2. Creates a MazeGenerator instance with default dimensions (21x21)
    3. Starts the application's main event loop
    
    The default maze size is 21x21 cells, which provides a good balance
    between complexity and performance for demonstration purposes.
    """
    print("Maze Generator - Recursive Backtracking Algorithm")
    print("=" * 50)
    print("Features:")
    print("- Interactive GUI with tkinter")
    print("- Adjustable maze size")
    print("- Animated generation")
    print("- Real-time visualization")
    print("=" * 50)
    
    # Create and run the maze generator
    generator = MazeGenerator(width=21, height=21, cell_size=20)
    generator.run()

if __name__ == "__main__":
    main()
