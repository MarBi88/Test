package game.lumi.systems;

import game.lumi.entities.Animal;
import game.lumi.input.InputManager;
import game.lumi.world.GameWorld;

/** Findet das nächste interaktive Objekt und löst die Interaktion bei E / Tipp auf die Pfoten-Taste aus. */
public final class InteractionSystem {
    private final AnimalSystem animals;

    public InteractionSystem(AnimalSystem animals) { this.animals = animals; }

    public void update(GameWorld w, InputManager in) {
        Animal best = null; float bd = 1e9f;
        for (Animal a : w.animals) {
            if (!a.canInteract) continue;
            float dx = a.x - w.player.x, dy = a.y - w.player.y, d = dx * dx + dy * dy;
            if (d < bd) { bd = d; best = a; }
        }
        w.interactTarget = best;
        if (best == null || !in.interact) return;
        float sdx = (best.x - best.y) - (w.player.x - w.player.y);
        if (Math.abs(sdx) > 0.2f) w.player.facingX = sdx > 0 ? 1 : -1;
        w.player.facingBack = false;
        w.lumi.interact();
        switch (best.type) {
            case RABBIT:
                best.done = true; best.happy = 3f; best.mode = Animal.Mode.SIT; best.modeTime = 0f;
                w.particles.burst(best.screenX(), best.screenY() + 80f, 22, 1f, 0.5f, 0.65f);
                w.progressChanged = true;
                break;
            case SQUIRREL:
                best.mode = Animal.Mode.DIG; best.modeTime = 0f;
                break;
            default:
                animals.interactBird(w, best);
        }
    }
}
