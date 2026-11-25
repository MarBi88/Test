package game.lumi.characters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import game.lumi.art.ArtSheet;
import game.lumi.entities.Critter;

/** Zeichnet die Beutetiere mit kleinen Animationen (Krabbeln, Hüpfen, Flügelschlag). */
public final class CritterCharacter {
    private final TextureRegion ladybug, butterfly, hopper, frog0, frog1, spider0, spider1, shadow;

    public CritterCharacter(ArtSheet art) {
        ladybug = art.get("crit_ladybug"); butterfly = art.get("crit_butterfly"); hopper = art.get("crit_hopper");
        frog0 = art.get("crit_frog0"); frog1 = art.get("crit_frog1"); spider0 = art.get("crit_spider0"); spider1 = art.get("crit_spider1");
        shadow = art.get("shadow");
    }

    public TextureRegion region(int kind, int variant) {
        if (kind == Critter.INSECT) return variant == 0 ? ladybug : variant == 1 ? butterfly : hopper;
        if (kind == Critter.FROG) return variant == 0 ? frog0 : frog1;
        return variant == 0 ? spider0 : spider1;
    }

    public void draw(SpriteBatch b, Critter c) {
        float sx = c.screenX(), sy = c.screenY();
        float k = 1f - MathUtils.clamp(c.z / 90f, 0f, 0.55f);
        b.setColor(1f, 1f, 1f, 0.7f * k);
        b.draw(shadow, sx - 14f * k, sy - 5f * k, 28f * k, 12f * k);
        TextureRegion r = region(c.kind, c.variant);
        float scale = c.kind == Critter.SPIDER ? 0.95f : 1.05f;
        float w = r.getRegionWidth() * scale, h = r.getRegionHeight() * scale;
        float scaleX = c.facing, scaleY = 1f, rot = 0f;
        if (c.flies()) scaleX *= 0.55f + 0.45f * Math.abs(MathUtils.sin(c.anim * 22f)); // Flügelschlag
        else if (c.hops()) {
            float t = MathUtils.clamp(c.hopT / 0.42f, 0f, 1f);
            if (c.z > 0.5f) scaleY = 1f + 0.12f * MathUtils.sin(t * MathUtils.PI); else scaleY = 0.92f + 0.03f * MathUtils.sin(c.anim * 3f);
        } else if (c.moving) rot = MathUtils.sin(c.anim * 16f) * 4f;       // Krabbeln: leichtes Wackeln
        else scaleY = 1f + 0.03f * MathUtils.sin(c.anim * 3f);
        b.setColor(Color.WHITE);
        b.draw(r, sx - w / 2f, sy + c.z - 4f, w / 2f, 4f, w, h, scaleX, scaleY, rot);
    }
}
