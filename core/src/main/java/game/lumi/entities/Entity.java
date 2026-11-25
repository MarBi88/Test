package game.lumi.entities;

import game.lumi.world.Iso;

/** Basis für alles, was in der Welt steht und tiefensortiert gezeichnet wird. */
public abstract class Entity {
    /** Fußpunkt in Weltkoordinaten (Kacheln). */
    public float x, y;

    public float depth() { return Iso.depth(x, y); }
    public float screenX() { return Iso.sx(x, y); }
    public float screenY() { return Iso.sy(x, y); }
}
