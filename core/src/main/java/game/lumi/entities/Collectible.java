package game.lumi.entities;

/** Lichtkristall. Der Index ist stabil (Speicherstand!), nicht ändern ohne Savegame-Version zu erhöhen. */
public final class Collectible extends Entity {
    public int id;
    public boolean collected;
    public float phase;
    /** 0..1: Sammel-Animation (schrumpft und steigt auf), danach unsichtbar. */
    public float vanish;
    public boolean hidden;
}
