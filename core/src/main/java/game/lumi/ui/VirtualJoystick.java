package game.lumi.ui;

/**
 * Wisch-Steuerung: Der Finger setzt irgendwo auf dem Bildschirm auf, die Bewegungsrichtung ist die Richtung
 * vom Aufsetzpunkt zum Finger. Entfernt sich der Finger weiter als {@link #MAX_RADIUS}, wandert der Ankerpunkt
 * hinterher – man kann also endlos in eine neue Richtung "umwischen", ohne den Finger abzusetzen.
 * (Klassenname historisch; es wird kein sichtbarer Joystick mehr gezeichnet.)
 */
public final class VirtualJoystick {
    public static final float MAX_RADIUS = 70f;
    private static final float DEAD = 0.10f;

    public boolean active;
    public int pointer = -1;
    /** Ankerpunkt und aktuelle Fingerposition (UI-Koordinaten). */
    public float baseX, baseY, knobX, knobY;
    public float outX, outY, magnitude;

    public void begin(int pointer, float x, float y) {
        active = true; this.pointer = pointer;
        baseX = knobX = x; baseY = knobY = y;
        outX = outY = magnitude = 0f;
    }

    public void drag(float x, float y) {
        float dx = x - baseX, dy = y - baseY;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > MAX_RADIUS) { // Anker zieht nach
            float over = (len - MAX_RADIUS) / len;
            baseX += dx * over; baseY += dy * over;
            dx = x - baseX; dy = y - baseY; len = MAX_RADIUS;
        }
        knobX = x; knobY = y;
        float m = len / MAX_RADIUS;
        if (m < DEAD) { outX = outY = magnitude = 0f; return; }
        float scaled = (m - DEAD) / (1f - DEAD);
        outX = dx / len * scaled; outY = dy / len * scaled; magnitude = scaled;
    }

    public void end() { active = false; pointer = -1; outX = outY = magnitude = 0f; }
}
