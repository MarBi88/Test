package game.lumi.systems;

import game.lumi.entities.PuzzleProp;
import game.lumi.puzzles.Puzzle;
import game.lumi.world.GameWorld;

/**
 * Sanfte Hinweise bei längerer Inaktivität: Lumi schaut zum nächsten ungelösten Rätselteil,
 * und dort erscheint ein leuchtendes Signal. Kein Text, kein Druck.
 */
public final class HintSystem {
    private static final float IDLE_BEFORE_HINT = 11f, COOLDOWN = 16f, MAX_DIST = 26f;
    private float cooldown = 6f;

    public void update(GameWorld w, float dt) {
        w.hintTime = Math.max(0f, w.hintTime - dt);
        if (w.hintTime <= 0f) w.hintTarget = null;
        cooldown -= dt;
        if (cooldown > 0f || w.player.idleTime < IDLE_BEFORE_HINT || w.hintTime > 0f) return;
        PuzzleProp best = null; float bd = MAX_DIST * MAX_DIST;
        for (Puzzle pz : w.puzzles) {
            PuzzleProp t = pz.hintTarget(w);
            if (t == null) continue;
            float dx = t.x - w.player.x, dy = t.y - w.player.y, d = dx * dx + dy * dy;
            if (d < bd) { bd = d; best = t; }
        }
        if (best == null) { cooldown = COOLDOWN; return; }
        w.hintTarget = best; w.hintTime = 5f; cooldown = COOLDOWN;
        float sdx = (best.x - best.y) - (w.player.x - w.player.y);
        w.lumi.glance(sdx >= 0f ? 1f : -1f, 3f);
    }
}
