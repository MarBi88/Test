package game.lumi.ui;

/** Schwebender Joystick: erscheint dort, wo der Finger aufsetzt. Reine Logik, ohne Rendering. */
public final class VirtualJoystick {
    public static final float MAX_RADIUS = 100f;
    private static final float DEAD = 0.10f;

    public boolean active;
    public int pointer = -1;
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
        if (len > MAX_RADIUS) { dx *= MAX_RADIUS / len; dy *= MAX_RADIUS / len; len = MAX_RADIUS; }
        knobX = baseX + dx; knobY = baseY + dy;
        float m = len / MAX_RADIUS;
        if (m < DEAD) { outX = outY = magnitude = 0f; return; }
        // Totzone herausrechnen, damit die Bewegung sanft einsetzt
        float scaled = (m - DEAD) / (1f - DEAD);
        outX = dx / len * scaled; outY = dy / len * scaled; magnitude = scaled;
    }

    public void end() { active = false; pointer = -1; outX = outY = magnitude = 0f; }
}
