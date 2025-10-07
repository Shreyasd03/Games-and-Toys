"""
Main game loop for the 2D platform coin-collection game.
Handles pygame initialization, event processing, and game state management.
"""

import sys
import pygame
from core import Config, reset, step, NOOP
from render import draw, get_actions_from_keys


def main():
    """
    Main game loop that initializes pygame and runs the game.
    """
    # Initialize pygame
    pygame.init()
    
    # Create game configuration
    cfg = Config()
    
    # Create window and clock
    screen = pygame.display.set_mode((cfg.W, cfg.H))
    pygame.display.set_caption("Coin Timer Game")
    clock = pygame.time.Clock()
    
    # Initialize game state
    state, rng = reset(cfg, seed=0)
    game_message = None
    game_over = False
    
    # Main game loop
    running = True
    while running:
        # Handle events
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False
            
            # Handle restart on R key press
            elif event.type == pygame.KEYDOWN:
                if event.key == pygame.K_r and game_over:
                    # Reset game
                    state, rng = reset(cfg, seed=0)
                    game_message = None
                    game_over = False
        
        # Game logic (only if not game over)
        if not game_over:
            # Get actions from keyboard input
            actions = get_actions_from_keys(state)
            
            # Always run physics step (even with NOOP if no actions)
            if not actions:
                actions = [NOOP]  # Ensure physics always runs
            
            # Execute game step with all actions
            state, events = step(cfg, state, actions, rng)
            
            # Handle game events
            if events["win"]:
                game_message = "WIN! Press R to restart."
                game_over = True
            elif events["fail"]:
                if events["fail"] == "timeout":
                    game_message = "FAIL (timeout). Press R to restart."
                elif events["fail"] == "fall":
                    game_message = "FAIL (fell). Press R to restart."
                game_over = True
        
        # Render the game
        draw(screen, cfg, state, game_message)
        pygame.display.flip()
        
        # Maintain ~60 FPS
        clock.tick(60)
    
    # Clean up and exit
    pygame.quit()
    sys.exit()


if __name__ == "__main__":
    main()
