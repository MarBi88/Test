package game.lumi.puzzles;

import game.lumi.entities.PuzzleProp;
import game.lumi.world.GameWorld;

/** Basis der Rätsel. Rätsel haben keinen Zeitdruck und keine Strafe; Fehlversuche setzen nur sanft zurück. */
public abstract class Puzzle {
    public boolean solved;

    public abstract void update(GameWorld w, float dt);

    /** Nächstes Element, auf das ein Hinweis zeigen soll (oder null, wenn gelöst). */
    public abstract PuzzleProp hintTarget(GameWorld w);

    protected static float dist(PuzzleProp p, GameWorld w) {
        float dx = p.x - w.player.x, dy = p.y - w.player.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
