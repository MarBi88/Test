package game.lumi.entities;

/** Waldtier mit eigenem Verhalten (siehe AnimalSystem). Reine Daten. */
public final class Animal extends Entity {
    public enum Type { RABBIT, SQUIRREL, BIRD }
    public enum Mode { SIT, HOP, FLY, DIG }

    public final Type type;
    public Mode mode = Mode.SIT;
    public float homeX, homeY, tx, ty;
    public float z;
    public float timer;
    public float anim;          // Zeitgeber für Animation
    public float happy;         // >0: freut sich
    public float modeTime;
    public int facing = 1;      // Bildschirmrichtung: +1 rechts, -1 links
    public int hopsLeft;
    /** Hat die Interaktion bereits erlebt (Kaninchen gefüttert / Eichhörnchen hat gegraben). */
    public boolean done;
    /** Ziel-Kristall (Vogel) oder Geschenk-Kristall (Eichhörnchen); -1 = keins. */
    public int crystalId = -1;
    /** Zustand für die Sprechblase (0..1 weich ein-/ausblenden). */
    public float bubble;
    public boolean nearLumi;
    public boolean canInteract;

    public Animal(Type type) { this.type = type; }
}
