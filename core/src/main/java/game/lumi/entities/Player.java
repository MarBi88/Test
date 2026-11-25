package game.lumi.entities;

/** Lumi als Spielfigur: reine Zustandsdaten, Logik in den Systemen, Darstellung in LumiCharacter. */
public final class Player extends Entity {
    public enum State { IDLE, WALK, RUN, JUMP, LAND, SLEEP }

    public static final float RADIUS = 0.2f;

    public float vx, vy;
    /** Höhe über dem Boden in Pixeln. */
    public float z, vz;
    public boolean grounded = true;

    public State state = State.IDLE;
    public float stateTime;
    public float idleTime;
    /** Bildschirmrichtung der Blickrichtung: facingX -1/+1, facingBack = von hinten sichtbar. */
    public int facingX = 1;
    public boolean facingBack;
    /** Aktuelle Geschwindigkeit relativ zur Laufgeschwindigkeit (0..1+). */
    public float speed01;
    /** Anzahl Landungen etc. für Partikel/Sound später. */
    public boolean justLanded;
    public boolean justJumped;

    public void setState(State s) {
        if (state != s) { state = s; stateTime = 0f; }
    }
}
