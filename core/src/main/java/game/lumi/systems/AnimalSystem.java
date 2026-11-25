package game.lumi.systems;

import com.badlogic.gdx.math.MathUtils;
import game.lumi.entities.Animal;
import game.lumi.entities.Collectible;
import game.lumi.entities.Player;
import game.lumi.world.GameWorld;
import game.lumi.world.Iso;

/**
 * Verhalten der Waldtiere. Jedes Tier hat ein eigenes Bewegungsmuster und reagiert auf Lumi:
 * Kaninchen hoppeln in Heimatnähe und weichen schnellem Rennen höflich aus,
 * das Eichhörnchen huscht kurz umher und buddelt auf Wunsch,
 * der Vogel fliegt zu einem noch nicht gefundenen versteckten Kristall und zwitschert dort.
 */
public final class AnimalSystem {
    private static final float HOP_T = 0.46f;
    private final float[] out = new float[2];

    public void update(GameWorld w, float dt) {
        Player p = w.player;
        for (Animal a : w.animals) {
            a.anim += dt;
            a.modeTime += dt;
            a.happy = Math.max(0f, a.happy - dt);
            float dx = p.x - a.x, dy = p.y - a.y;
            float d = (float) Math.sqrt(dx * dx + dy * dy);
            a.nearLumi = d < 5.5f;
            float screenDx = (p.x - p.y) - (a.x - a.y);
            boolean wantsInteraction;
            switch (a.type) {
                case RABBIT: updateRabbit(w, a, p, d, screenDx, dt); wantsInteraction = !a.done; break;
                case SQUIRREL: updateSquirrel(w, a, p, d, screenDx, dt); wantsInteraction = !a.done; break;
                default: updateBird(w, a, p, d, screenDx, dt); wantsInteraction = true; break;
            }
            a.canInteract = wantsInteraction && d < 1.9f && a.mode != Animal.Mode.DIG;
            boolean showBubble = a.type == Animal.Type.BIRD ? (a.happy > 0f || (a.nearLumi && a.mode == Animal.Mode.SIT && a.crystalId >= 0))
                    : (!a.done && a.nearLumi);
            a.bubble += ((showBubble ? 1f : 0f) - a.bubble) * Math.min(1f, 6f * dt);
        }
    }

    // ---------------------------------------------------------------- Hilfen

    private void startHop(GameWorld w, Animal a, float dirX, float dirY, int hops) {
        float l = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (l < 0.001f) { dirX = 1f; dirY = 0f; l = 1f; }
        a.tx = dirX / l; a.ty = dirY / l;
        a.mode = Animal.Mode.HOP; a.modeTime = 0f; a.hopsLeft = hops;
        a.facing = (a.tx - a.ty) >= 0f ? 1 : -1;
    }

    private void doHop(GameWorld w, Animal a, float speed, float dt) {
        w.map.move(a.x, a.y, a.tx * speed * dt, a.ty * speed * dt, 0.25f, out);
        a.x = out[0]; a.y = out[1];
        float t = Math.min(1f, a.modeTime / HOP_T);
        a.z = MathUtils.sin(t * MathUtils.PI) * 26f;
    }

    /** Richtung: zurück zur Heimat, wenn zu weit weg, sonst zufällig. */
    private void pickWander(GameWorld w, Animal a, float radius, int maxHops) {
        float hx = a.homeX - a.x, hy = a.homeY - a.y;
        float h = (float) Math.sqrt(hx * hx + hy * hy);
        float ang = MathUtils.random(MathUtils.PI2);
        float dx = MathUtils.cos(ang), dy = MathUtils.sin(ang);
        if (h > radius) { dx = hx / h; dy = hy / h; }
        startHop(w, a, dx, dy, 1 + MathUtils.random(maxHops - 1));
    }

    private void faceLumi(Animal a, float screenDx) {
        if (Math.abs(screenDx) > 0.3f) a.facing = screenDx > 0 ? 1 : -1;
    }

    // ---------------------------------------------------------------- Kaninchen

    private void updateRabbit(GameWorld w, Animal a, Player p, float d, float screenDx, float dt) {
        if (a.mode == Animal.Mode.HOP) {
            doHop(w, a, 1.9f, dt);
            if (a.modeTime >= HOP_T) {
                a.z = 0f;
                if (--a.hopsLeft > 0) { a.modeTime = 0f; } else { a.mode = Animal.Mode.SIT; a.modeTime = 0f; a.timer = MathUtils.random(1.5f, 4f); }
            }
            return;
        }
        a.z = a.happy > 0f ? Math.abs(MathUtils.sin(a.anim * 9f)) * 16f : 0f;
        if (a.nearLumi) faceLumi(a, screenDx);
        // höflich ausweichen, wenn Lumi schnell heranrennt
        if (d < 1.4f && p.speed01 > 0.7f) { startHop(w, a, a.x - p.x, a.y - p.y, 2); return; }
        a.timer -= dt;
        if (a.timer <= 0f && a.happy <= 0f) pickWander(w, a, 3.2f, 3);
        if (a.done && a.happy <= 0f && MathUtils.randomBoolean(0.004f)) a.happy = 0.9f; // zufriedene Freudenhüpfer
    }

    // ---------------------------------------------------------------- Eichhörnchen

    private void updateSquirrel(GameWorld w, Animal a, Player p, float d, float screenDx, float dt) {
        if (a.mode == Animal.Mode.DIG) {
            a.z = 0f;
            if (a.modeTime > 1.5f) {
                a.mode = Animal.Mode.SIT; a.modeTime = 0f; a.done = true; a.happy = 2.5f;
                Collectible c = a.crystalId >= 0 ? w.crystals.get(a.crystalId) : null;
                if (c != null) {
                    c.active = true;
                    w.particles.burst(c.screenX(), c.screenY() + 50f, 34, 0.6f, 1f, 0.92f);
                }
                w.progressChanged = true;
            }
            return;
        }
        if (a.mode == Animal.Mode.HOP) {
            doHop(w, a, 2.8f, dt);
            if (a.modeTime >= HOP_T * 0.7f) {
                a.z = 0f;
                if (--a.hopsLeft > 0) a.modeTime = 0f; else { a.mode = Animal.Mode.SIT; a.modeTime = 0f; a.timer = MathUtils.random(2.5f, 5f); }
            }
            return;
        }
        a.z = a.happy > 0f ? Math.abs(MathUtils.sin(a.anim * 11f)) * 12f : 0f;
        if (a.nearLumi) faceLumi(a, screenDx);
        a.timer -= dt;
        if (a.timer <= 0f && a.happy <= 0f) pickWander(w, a, 1.6f, 2);
    }

    // ---------------------------------------------------------------- Vogel

    private Collectible pickTarget(GameWorld w, Player p) {
        Collectible best = null; float bd = 1e9f;
        for (Collectible c : w.crystals) {
            if (c.collected || !c.active || !c.hidden) continue;
            float dx = c.x - p.x, dy = c.y - p.y, dd = dx * dx + dy * dy;
            if (dd < bd) { bd = dd; best = c; }
        }
        return best;
    }

    private boolean valid(GameWorld w, int id) {
        if (id < 0) return false;
        Collectible c = w.crystals.get(id);
        return !c.collected && c.active;
    }

    /** Vogel ist (fast) am Landeplatz neben seinem Ziel-Kristall. */
    private void sendBird(GameWorld w, Animal a, Collectible c) {
        a.crystalId = c.id;
        a.tx = c.x + 1.1f; a.ty = c.y + 0.1f;
        if (!w.map.free(a.tx, a.ty, 0.2f)) { a.tx = c.x + 0.1f; a.ty = c.y + 1.1f; }
        a.mode = Animal.Mode.FLY; a.modeTime = 0f;
    }

    private void updateBird(GameWorld w, Animal a, Player p, float d, float screenDx, float dt) {
        if (a.crystalId >= 0 && !valid(w, a.crystalId)) a.crystalId = -1;
        if (a.mode == Animal.Mode.FLY) {
            float dx = a.tx - a.x, dy = a.ty - a.y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            float step = Math.min(dist, 4.6f * dt);
            if (dist > 0.01f) { a.x += dx / dist * step; a.y += dy / dist * step; a.facing = (dx - dy) >= 0f ? 1 : -1; }
            float up = Math.min(a.modeTime * 90f, 70f), down = dist * 22f;
            a.z = Math.min(up, down + 0f) + MathUtils.sin(a.anim * 9f) * 3f;
            if (dist < 0.05f) { a.mode = Animal.Mode.SIT; a.modeTime = 0f; a.z = 0f; a.happy = 1.8f; a.timer = 4f; }
            return;
        }
        // am Boden: kleine Hüpfer
        a.z = a.happy > 0f ? Math.abs(MathUtils.sin(a.anim * 10f)) * 10f : 0f;
        if (a.nearLumi) faceLumi(a, screenDx);
        a.timer -= dt;
        if (a.nearLumi && a.crystalId < 0) {
            Collectible c = pickTarget(w, p);
            if (c != null) sendBird(w, a, c);
        } else if (a.nearLumi && a.timer <= 0f && a.crystalId >= 0) {
            a.happy = 1.4f; a.timer = 5f; // zwitschert am Landeplatz und lockt Lumi
        }
    }

    /** Antippen/E beim Vogel: sofort zwitschern und (wenn nötig) zum versteckten Ort fliegen. */
    public void interactBird(GameWorld w, Animal a) {
        a.happy = 1.6f;
        Collectible c = pickTarget(w, w.player);
        if (c != null && (a.crystalId != c.id || a.mode != Animal.Mode.FLY)) sendBird(w, a, c);
    }
}
