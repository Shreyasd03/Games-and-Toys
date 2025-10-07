Timing: dt = 1/60, timer_budget = 10.0s, win_coin = 10

Arena: size W=640, H=360, gravity g = 1500 px/s², death line y≥410

Platforms: floor (0,320,640,40), mid-left (80,240,160,15), mid-right (400,200,160,15)

Agent: size 24×32, ground accel 3000, air accel 1500, max speed 250, jump impulse -500

Actions: {0:noop, 1:left, 2:right, 3:jump_if_grounded}

Coin spawns: one point centered on each platform top; no immediate repeats

Win/Fail: win if elapsed ≥ win_time; fail on time_left ≤ 0 or falling below death_y