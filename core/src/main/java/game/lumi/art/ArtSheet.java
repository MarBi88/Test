package game.lumi.art;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Ein einzelner Textur-Atlas (2048²), der zur Laufzeit prozedural befüllt wird.
 * Alle Sprites des Spiels liegen darin, damit der SpriteBatch nie die Textur wechseln muss.
 *
 * Austauschbarkeit: Wer später gemalte PNGs verwenden will, ersetzt {@link ArtFactory}
 * durch einen Loader, der dieselben Namen in einen Atlas legt – der Rest des Spiels
 * greift nur über {@link #get(String)} auf Grafiken zu. (Siehe docs/ASSET_GUIDE.md)
 */
public final class ArtSheet implements Disposable {
    public static final int SIZE = 2048;
    private static final int PAD = 3;

    private final Pixmap pm = new Pixmap(SIZE, SIZE, Pixmap.Format.RGBA8888);
    private final ObjectMap<String, int[]> rects = new ObjectMap<>();
    private final ObjectMap<String, TextureRegion> regions = new ObjectMap<>();
    private Texture texture;
    private int shelfX = PAD, shelfY = PAD, shelfH = 0;
    private boolean finished;

    public ArtSheet() {
        pm.setBlending(Pixmap.Blending.SourceOver);
    }

    /** Reserviert eine Zelle und liefert den Painter dafür. */
    public Painter alloc(String name, int w, int h) {
        if (finished) throw new IllegalStateException("Sheet bereits fertig");
        if (shelfX + w + PAD > SIZE) { shelfX = PAD; shelfY += shelfH + PAD; shelfH = 0; }
        if (shelfY + h + PAD > SIZE) throw new IllegalStateException("ArtSheet voll bei " + name);
        int x = shelfX, y = shelfY;
        shelfX += w + PAD;
        shelfH = Math.max(shelfH, h);
        rects.put(name, new int[] { x, y, w, h });
        return new Painter(pm, x, y, w, h);
    }

    public void finish() {
        texture = new Texture(pm);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        for (ObjectMap.Entry<String, int[]> e : rects) {
            int[] r = e.value;
            regions.put(e.key, new TextureRegion(texture, r[0], r[1], r[2], r[3]));
        }
        pm.dispose();
        finished = true;
    }

    public TextureRegion get(String name) {
        TextureRegion r = regions.get(name);
        if (r == null) throw new IllegalArgumentException("Unbekannte Grafik: " + name);
        return r;
    }

    public Texture texture() { return texture; }

    /** Alle Namen mit Präfix, z. B. "tree" → tree0, tree1 … */
    public Array<TextureRegion> family(String prefix) {
        Array<TextureRegion> out = new Array<>();
        for (int i = 0; regions.containsKey(prefix + i); i++) out.add(regions.get(prefix + i));
        return out;
    }

    @Override public void dispose() { if (texture != null) texture.dispose(); }
}
