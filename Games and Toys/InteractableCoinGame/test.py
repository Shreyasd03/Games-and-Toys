from game.core import Config, reset, step, LEFT, RIGHT, JUMP, NOOP
import numpy as np

cfg = Config()
st, rng = reset(cfg, seed=0)

# random roll for 300 steps
for _ in range(10000):
    a = np.random.randint(0, 4)
    st, ev = step(cfg, st, int(a), rng)
    assert np.isfinite([st.agent.x, st.agent.y, st.agent.vx, st.agent.vy]).all()
    if ev["pickup"]:
        print("pickup; time_left reset:", st.time_left)
    if ev["win"] or ev["fail"]:
        print("end:", ev)
        break