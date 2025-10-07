"""
Pygame rendering module for the 2D platform coin-collection game.
Handles all visual rendering and input processing.
"""

import pygame
from typing import Optional
from core import State, Config, NOOP, LEFT, RIGHT, JUMP, _platform_tops


# Color constants
BLACK = (0, 0, 0)
WHITE = (255, 255, 255)
RED = (255, 0, 0)
YELLOW = (255, 255, 0)
GREEN = (0, 255, 0)


def get_actions_from_keys(st: State) -> list[int]:
    """
    Map keyboard input to game actions.
    Returns a list of simultaneous actions.
    
    Args:
        st: Current game state
        
    Returns:
        List of action constants (can be empty, or contain LEFT/RIGHT/JUMP)
    """
    keys = pygame.key.get_pressed()
    actions = []
    
    # Horizontal movement
    if keys[pygame.K_LEFT] or keys[pygame.K_a]:
        actions.append(LEFT)
    if keys[pygame.K_RIGHT] or keys[pygame.K_d]:
        actions.append(RIGHT)
    
    # Jump (only if grounded)
    if (keys[pygame.K_SPACE] or keys[pygame.K_w] or keys[pygame.K_UP]) and st.agent.grounded:
        actions.append(JUMP)
    
    return actions


def get_action_from_keys(st: State) -> int:
    """
    Legacy function for single action input.
    Returns the first action from get_actions_from_keys.
    
    Args:
        st: Current game state
        
    Returns:
        Action constant (NOOP, LEFT, RIGHT, JUMP)
    """
    actions = get_actions_from_keys(st)
    return actions[0] if actions else NOOP


def draw_platforms(screen: pygame.Surface, cfg: Config) -> None:
    """
    Draw all platforms in the arena.
    
    Args:
        screen: Pygame surface to draw on
        cfg: Game configuration
    """
    platforms = _platform_tops(cfg)
    
    for x, y, w, h in platforms:
        pygame.draw.rect(screen, WHITE, (x, y, w, h))


def draw_agent(screen: pygame.Surface, cfg: Config, st: State) -> None:
    """
    Draw the agent as a red rectangle.
    
    Args:
        screen: Pygame surface to draw on
        cfg: Game configuration
        st: Current game state
    """
    agent = st.agent
    pygame.draw.rect(screen, RED, (agent.x, agent.y, cfg.agent_size_w, cfg.agent_size_h))


def draw_coin(screen: pygame.Surface, st: State) -> None:
    """
    Draw the coin as a yellow circle.
    
    Args:
        screen: Pygame surface to draw on
        st: Current game state
    """
    coin = st.coin
    coin_radius = 8
    coin_center = (int(coin.x + coin_radius), int(coin.y + coin_radius))
    pygame.draw.circle(screen, YELLOW, coin_center, coin_radius)


def draw_hud(screen: pygame.Surface, cfg: Config, st: State, message: Optional[str] = None) -> None:
    """
    Draw the heads-up display with game information.
    
    Args:
        screen: Pygame surface to draw on
        cfg: Game configuration
        st: Current game state
        message: Optional message to display
    """
    font = pygame.font.SysFont(None, 22)
    
    # Time information
    time_text = f"Time: {st.time_left:.1f}s"
    time_surface = font.render(time_text, True, GREEN)
    screen.blit(time_surface, (10, 10))
    
    # Coins collected
    coins_text = f"Coins: {st.coins_collected} / {cfg.coins_to_win}"
    coins_surface = font.render(coins_text, True, GREEN)
    screen.blit(coins_surface, (10, 35))
    
    # Instructions or game message
    if message:
        message_surface = font.render(message, True, GREEN)
        # Center the message horizontally
        message_rect = message_surface.get_rect(center=(cfg.W // 2, 30))
        screen.blit(message_surface, message_rect)


def draw(screen: pygame.Surface, cfg: Config, st: State, message: Optional[str] = None) -> None:
    """
    Main drawing function that renders the entire game scene.
    
    Args:
        screen: Pygame surface to draw on
        cfg: Game configuration
        st: Current game state
        message: Optional message to display (win/fail banner)
    """
    # Clear screen with black background
    screen.fill(BLACK)
    
    # Draw game elements
    draw_platforms(screen, cfg)
    draw_agent(screen, cfg, st)
    draw_coin(screen, st)
    draw_hud(screen, cfg, st, message)
