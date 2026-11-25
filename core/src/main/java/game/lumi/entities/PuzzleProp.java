package game.lumi.entities;

/** Spielelement der Rätsel: leuchtende Blume, magischer Stein oder Hinweisstein. */
public final class PuzzleProp extends Entity {
    public enum Kind { FLOWER, STONE, HINT }

    public final Kind kind;
    /** Blume: Farbindex (0 rosa, 1 gelb, 2 blau). Stein: laufende Nummer. */
    public final int idx;
    public boolean lit;
    /** Weich geglätteter Leuchtwert 0..1. */
    public float litAmt;
    public float wiggle;
    public float phase;
    /** Hinweisstein: wie viele Perlen leuchten (Fortschritt). */
    public int progress;

    public PuzzleProp(Kind kind, int idx) { this.kind = kind; this.idx = idx; }
}
