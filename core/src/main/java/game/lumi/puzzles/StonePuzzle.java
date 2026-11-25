package game.lumi.puzzles;

import game.lumi.entities.PuzzleProp;
import game.lumi.world.GameWorld;

/**
 * Rätsel 2: Drei magische Steine im Wald werden durch Berühren aktiviert. Sind alle leuchtend,
 * lösen sich die Ranken des Tors und der Weg zur geheimen Lichtung ist frei.
 */
public final class StonePuzzle extends Puzzle {
    public final PuzzleProp[] stones = new PuzzleProp[3];

    public int litCount() {
        int n = 0;
        for (PuzzleProp s : stones) if (s.lit) n++;
        return n;
    }

    @Override public void update(GameWorld w, float dt) {
        for (PuzzleProp s : stones) {
            s.litAmt += ((s.lit ? 1f : 0f) - s.litAmt) * Math.min(1f, 4f * dt);
            if (!s.lit && dist(s, w) < 1.15f) {
                s.lit = true;
                w.particles.burst(s.screenX(), s.screenY() + 70f, 20, 0.75f, 0.55f, 1f);
                w.lumi.cheer();
                w.progressChanged = true;
                if (litCount() == 3) open(w);
            }
        }
    }

    private void open(GameWorld w) {
        solved = true;
        w.map.gateOpen = true;
        w.particles.burst(w.map.gateX(), w.map.gateY() + 60f, 40, 0.8f, 0.6f, 1f);
    }

    public void load(boolean[] lit) {
        for (int i = 0; i < 3; i++) { stones[i].lit = lit[i]; stones[i].litAmt = lit[i] ? 1f : 0f; }
        solved = litCount() == 3;
        w_open(solved);
    }

    private GameWorld ref;
    public void bind(GameWorld w) { ref = w; }
    private void w_open(boolean open) { if (ref != null) { ref.map.gateOpen = open; ref.map.gateAlphaReset(open); } }

    @Override public PuzzleProp hintTarget(GameWorld w) {
        PuzzleProp best = null; float bd = 1e9f;
        for (PuzzleProp s : stones) {
            if (s.lit) continue;
            float d = dist(s, w);
            if (d < bd) { bd = d; best = s; }
        }
        return best;
    }
}
