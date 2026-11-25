package game.lumi.entities;

/** Beutetier (Insekt, Frosch, Spinne), das Lumi am liebsten frisst. Reine Daten; Verhalten in CritterSystem. */
public final class Critter extends Entity {
    public static final int INSECT = 0, FROG = 1, SPIDER = 2;
    /** Insekten-Varianten: 0 Marienkäfer, 1 Schmetterling (fliegt), 2 Heuschrecke (hüpft). */
    public final int kind, variant;
    public boolean alive = true;
    public float z, anim, timer, respawn, dirX, dirY, homeX, homeY, hopT;
    public boolean moving;
    public int facing = 1;

    public Critter(int kind, int variant) { this.kind = kind; this.variant = variant; }

    public boolean flies() { return kind == INSECT && variant == 1; }
    public boolean hops() { return kind == FROG || (kind == INSECT && variant == 2); }
}
