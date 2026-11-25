package game.lumi.characters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import game.lumi.art.ArtSheet;
import game.lumi.entities.Animal;

/** Zeichnet die Waldtiere samt Animation (Atmen, Hüpfen, Buddeln, Flügelschlag) und Sprechblasen-Symbolen. */
public final class AnimalCharacter {
    private final TextureRegion rabbit, squirrel, bird, wing, shadow, bubble, carrot, acorn, note, paw;

    public AnimalCharacter(ArtSheet art) {
        rabbit = art.get("animal_rabbit"); squirrel = art.get("animal_squirrel"); bird = art.get("animal_bird"); wing = art.get("animal_wing");
        shadow = art.get("shadow"); bubble = art.get("ui_bubble");
        carrot = art.get("icon_carrot"); acorn = art.get("icon_acorn"); note = art.get("icon_note"); paw = art.get("icon_paw");
    }

    private static float height(Animal a) {
        switch (a.type) { case RABBIT: return 56f; case SQUIRREL: return 66f; default: return 62f; }
    }

    public void draw(SpriteBatch b, Animal a) {
        float sx = a.screenX(), sy = a.screenY();
        // Schatten (schrumpft in der Luft)
        float k = 1f - MathUtils.clamp(a.z / 120f, 0f, 0.6f);
        float sw = (a.type == Animal.Type.BIRD ? 38f : 62f) * k, sh = sw * 0.42f;
        b.setColor(1f, 1f, 1f, 0.8f * k);
        b.draw(shadow, sx - sw / 2f + 3f, sy - sh / 2f, sw, sh);
        b.setColor(Color.WHITE);

        TextureRegion r; float scale, anchor, w0, h0;
        switch (a.type) {
            case RABBIT: r = rabbit; scale = 0.85f; anchor = 8f; break;
            case SQUIRREL: r = squirrel; scale = 0.8f; anchor = 12f; break;
            default: r = bird; scale = 0.9f; anchor = 4f;
        }
        w0 = r.getRegionWidth() * scale; h0 = r.getRegionHeight() * scale;
        float breathe = MathUtils.sin(a.anim * 2.6f);
        float scaleX = 1f, scaleY = 1f + breathe * 0.015f, rot = 0f, offX = 0f;
        if (a.mode == Animal.Mode.HOP) {
            float t = MathUtils.clamp(a.modeTime / 0.46f, 0f, 1f);
            float st = MathUtils.sin(t * MathUtils.PI);
            float soft = a.type == Animal.Type.RABBIT ? 0.3f : 1f; // Schildkröte watschelt nur
            scaleY = 1f + 0.14f * st * soft; scaleX = 1f - 0.07f * st * soft;
            if (a.type != Animal.Type.RABBIT && (t < 0.12f || t > 0.88f)) { scaleY = 0.88f; scaleX = 1.08f; } // Landung/Absprung
            rot = -a.facing * 10f * (t - 0.5f);
        } else if (a.mode == Animal.Mode.DIG) {
            offX = MathUtils.sin(a.anim * 38f) * 3f; scaleY = 0.9f + MathUtils.sin(a.anim * 19f) * 0.04f; rot = a.facing * (8f + MathUtils.sin(a.anim * 19f) * 4f);
        } else if (a.type == Animal.Type.SQUIRREL) {
            rot = MathUtils.sin(a.anim * 2.1f) * 1.6f;
        }
        float cx = sx + offX, by = sy + a.z - anchor * scale;
        b.draw(r, cx - w0 / 2f, by, w0 / 2f, anchor * scale, w0, h0, scaleX * a.facing, scaleY, rot);

        if (a.type == Animal.Type.BIRD) {
            boolean fly = a.mode == Animal.Mode.FLY;
            float flap = fly ? MathUtils.sin(a.anim * 32f) * 55f : -8f + MathUtils.sin(a.anim * 3f) * 2f;
            float ww = wing.getRegionWidth() * scale * 0.9f, wh = wing.getRegionHeight() * scale * 0.9f;
            float wx = cx - a.facing * 4f, wy = sy + a.z + 28f * scale;
            b.draw(wing, wx - ww * 0.2f * 1f, wy - wh / 2f, ww * 0.2f, wh / 2f, ww, wh, a.facing, 1f, a.facing * flap);
        }
    }

    public void drawBubble(SpriteBatch b, Animal a, float time) {
        if (a.bubble < 0.02f) return;
        TextureRegion icon = a.type == Animal.Type.RABBIT ? carrot : a.type == Animal.Type.SQUIRREL ? acorn : note;
        float pop = a.bubble;
        float s = (0.85f + 0.07f * MathUtils.sin(time * 3f)) * pop;
        float bw = 112f * s, bh = 128f * s;
        float x = a.screenX(), y = a.screenY() + a.z + height(a) + 8f + MathUtils.sin(time * 2.4f + a.x) * 3f;
        b.setColor(1f, 1f, 1f, Math.min(1f, pop * 1.4f));
        b.draw(bubble, x - bw / 2f, y, bw, bh);
        float is = 54f * s;
        b.draw(icon, x - is / 2f, y + bh * 0.31f, is, is);
        if (a.canInteract) { // Pfoten-Hinweis: "jetzt antippen/E drücken"
            float pp = 26f * pop * (1f + 0.12f * MathUtils.sin(time * 6f));
            b.setColor(0.96f, 0.5f, 0.15f, Math.min(1f, pop * 1.4f));
            b.draw(paw, x + bw * 0.28f, y + bh * 0.74f, pp, pp);
        }
        b.setColor(Color.WHITE);
    }
}
