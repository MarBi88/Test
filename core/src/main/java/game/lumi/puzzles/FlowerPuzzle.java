package game.lumi.puzzles;

import game.lumi.entities.Collectible;
import game.lumi.entities.PuzzleProp;
import game.lumi.world.GameWorld;

/**
 * Rätsel 1: Drei leuchtende Blumen müssen in der Farbreihenfolge rosa → gelb → blau berührt werden.
 * Die Reihenfolge zeigt der Hinweisstein (drei Farbperlen von links nach rechts).
 * Falsche Blume: sie wackelt, alle Blumen dimmen sanft – nichts wird bestraft, man darf sofort neu beginnen.
 */
public final class FlowerPuzzle extends Puzzle {
    public final PuzzleProp[] flowers = new PuzzleProp[3];
    public final PuzzleProp hint = new PuzzleProp(PuzzleProp.Kind.HINT, 0);
    public Collectible reward;
    private int progress;
    private final boolean[] touching = new boolean[3];
    private float lockout;

    @Override public void update(GameWorld w, float dt) {
        lockout = Math.max(0f, lockout - dt);
        for (PuzzleProp f : flowers) {
            f.wiggle = Math.max(0f, f.wiggle - dt);
            f.litAmt += ((f.lit ? 1f : 0f) - f.litAmt) * Math.min(1f, 5f * dt);
        }
        hint.progress = solved ? 3 : progress;
        if (solved) return;
        for (int i = 0; i < 3; i++) {
            PuzzleProp f = flowers[i];
            float d = dist(f, w);
            if (d > 1.5f) touching[i] = false;
            else if (d < 0.85f && !touching[i]) { touching[i] = true; onTouch(w, f, i); }
        }
    }

    private void onTouch(GameWorld w, PuzzleProp f, int i) {
        if (f.lit || lockout > 0f) return;
        if (f.idx == progress) { // richtige nächste Blume
            f.lit = true; progress++;
            w.particles.burst(f.screenX(), f.screenY() + 50f, 12, colorR(f.idx), colorG(f.idx), colorB(f.idx));
            w.lumi.cheer();
            if (progress >= 3) solve(w);
        } else { // falsche Blume: sanft zurücksetzen
            f.wiggle = 0.6f; lockout = 0.8f; progress = 0;
            for (PuzzleProp o : flowers) o.lit = false;
        }
    }

    private void solve(GameWorld w) {
        solved = true;
        if (reward != null) {
            reward.active = true;
            w.particles.burst(reward.screenX(), reward.screenY() + 50f, 34, 0.6f, 1f, 0.92f);
        }
        for (PuzzleProp f : flowers) w.particles.burst(f.screenX(), f.screenY() + 50f, 16, colorR(f.idx), colorG(f.idx), colorB(f.idx));
        w.progressChanged = true;
    }

    public void loadSolved(boolean s) {
        solved = s; progress = s ? 3 : 0;
        for (PuzzleProp f : flowers) { f.lit = s; f.litAmt = s ? 1f : 0f; }
        if (reward != null && s) reward.active = true;
    }

    @Override public PuzzleProp hintTarget(GameWorld w) {
        if (solved) return null;
        // Wer die Reihenfolge noch nicht kennt, wird zuerst zum Hinweisstein geführt.
        return progress == 0 ? hint : nextFlower();
    }

    /** Nächste richtige Blume (für Hinweise und Entwickler-Teleport). */
    public PuzzleProp nextFlower() {
        for (PuzzleProp f : flowers) if (f.idx == progress) return f;
        return hint;
    }

    private static final float[][] RGB = { { 1f, 0.45f, 0.66f }, { 1f, 0.85f, 0.3f }, { 0.43f, 0.68f, 1f } };
    public static float colorR(int i) { return RGB[i][0]; }
    public static float colorG(int i) { return RGB[i][1]; }
    public static float colorB(int i) { return RGB[i][2]; }
}
