package game.lumi.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Array;
import game.lumi.art.ArtSheet;
import game.lumi.characters.LumiCharacter;
import game.lumi.entities.Collectible;
import game.lumi.entities.Decor;
import game.lumi.entities.Player;
import game.lumi.input.InputManager;
import game.lumi.systems.CollectionSystem;
import game.lumi.systems.MovementSystem;
import game.lumi.systems.ParticleSystem;

/** Aggregiert den Spielzustand der Welt (Karte, Lumi, Sammelobjekte, Systeme). Kein Rendering, keine Eingabe. */
public final class GameWorld {
    public static final int CRYSTALS = 15;

    public final WorldMap map;
    public final Player player = new Player();
    public final LumiCharacter lumi;
    public final ParticleSystem particles;
    public final Array<Collectible> crystals = new Array<>();
    public float time;
    /** Wird gesetzt, wenn sich der Sammelstand ändert (Speichern). */
    public boolean collectionChanged;
    private final MovementSystem movement = new MovementSystem();
    private final CollectionSystem collection = new CollectionSystem();

    public GameWorld(ArtSheet art) {
        map = new WorldMap(art);
        lumi = new LumiCharacter(art);
        particles = new ParticleSystem(art);
        placeCrystals();
        for (Collectible c : crystals) com.badlogic.gdx.Gdx.app.log("Lumi", "Kristall " + c.id + " bei " + c.x + "," + c.y + (c.hidden ? " (versteckt)" : ""));
        resetPlayer(map.spawnX, map.spawnY);
    }

    /** 9 Kristalle in offenem Gelände, 6 versteckt hinter Bäumen (Neugier wird belohnt). */
    private static final float STEP = 0.4f;
    private boolean[] reach;
    private int rn;

    /** Flutfüllung ab dem Startpunkt: Kristalle müssen für Lumi erreichbar sein. */
    private void computeReach() {
        rn = (int) (WorldMap.N / STEP);
        reach = new boolean[rn * rn];
        int[] queue = new int[rn * rn];
        int head = 0, tail = 0;
        int s = (int) (map.spawnY / STEP) * rn + (int) (map.spawnX / STEP);
        reach[s] = true; queue[tail++] = s;
        int[] dx = { 1, -1, 0, 0 }, dy = { 0, 0, 1, -1 };
        while (head < tail) {
            int c = queue[head++], cx = c % rn, cy = c / rn;
            for (int k = 0; k < 4; k++) {
                int nx = cx + dx[k], ny = cy + dy[k];
                if (nx < 0 || ny < 0 || nx >= rn || ny >= rn || reach[ny * rn + nx]) continue;
                if (!map.free(nx * STEP, ny * STEP, 0.26f)) continue;
                reach[ny * rn + nx] = true; queue[tail++] = ny * rn + nx;
            }
        }
    }

    private boolean reachable(float x, float y) {
        int cx = (int) (x / STEP), cy = (int) (y / STEP);
        for (int j = -1; j <= 1; j++) for (int i = -1; i <= 1; i++) {
            int nx = cx + i, ny = cy + j;
            if (nx >= 0 && ny >= 0 && nx < rn && ny < rn && reach[ny * rn + nx]) return true;
        }
        return false;
    }

    private void placeCrystals() {
        computeReach();
        RandomXS128 r = new RandomXS128(77);
        int tries = 0;
        while (crystals.size < 9 && tries++ < 5000) {
            float x = map.spawnX + r.nextFloat() * 36f - 18f, y = map.spawnY + r.nextFloat() * 36f - 18f;
            float dx = x - map.spawnX, dy = y - map.spawnY;
            if (dx * dx + dy * dy < 16f || map.field(x, y) > 0.8f || !map.free(x, y, 0.5f) || !reachable(x, y) || !farFromOthers(x, y, 5f)) continue;
            add(x, y, false);
        }
        // hinter Bäumen: "oben" auf dem Bildschirm = kleinere Tiefe → x, y verkleinern
        Array<Decor> trees = new Array<>();
        for (Decor d : map.decor) if (d.occluder && d.solid > 0.2f && map.field(d.x, d.y) < 0.75f) trees.add(d);
        trees.shuffle();
        for (Decor t : trees) {
            if (crystals.size >= CRYSTALS) break;
            float x = t.x - 0.95f, y = t.y - 0.95f;
            if (!map.free(x, y, 0.45f) || !reachable(x, y) || !farFromOthers(x, y, 5f)) continue;
            add(x, y, true);
        }
        // Falls zu wenig versteckte Plätze: Rest offen
        tries = 0;
        while (crystals.size < CRYSTALS && tries++ < 5000) {
            float x = map.spawnX + r.nextFloat() * 36f - 18f, y = map.spawnY + r.nextFloat() * 36f - 18f;
            if (map.field(x, y) > 0.8f || !map.free(x, y, 0.5f) || !reachable(x, y) || !farFromOthers(x, y, 3f)) continue;
            add(x, y, false);
        }
        crystals.sort((a, b) -> Float.compare(a.depth(), b.depth()));
        for (int i = 0; i < crystals.size; i++) crystals.get(i).id = i;
    }

    private boolean farFromOthers(float x, float y, float d) {
        for (Collectible c : crystals) { float dx = c.x - x, dy = c.y - y; if (dx * dx + dy * dy < d * d) return false; }
        return true;
    }

    private void add(float x, float y, boolean hidden) {
        Collectible c = new Collectible();
        c.x = x; c.y = y; c.hidden = hidden; c.phase = MathUtils.random(MathUtils.PI2);
        crystals.add(c);
    }

    public int collectedCount() {
        int n = 0;
        for (Collectible c : crystals) if (c.collected) n++;
        return n;
    }

    public String collectedBits() {
        StringBuilder sb = new StringBuilder();
        for (Collectible c : crystals) sb.append(c.collected ? '1' : '0');
        return sb.toString();
    }

    public void loadCollected(String bits) {
        for (Collectible c : crystals) {
            c.collected = c.id < bits.length() && bits.charAt(c.id) == '1';
            c.vanish = c.collected ? 1f : 0f;
        }
    }

    public void resetPlayer(float x, float y) {
        if (!map.walkable(x, y)) { x = map.spawnX; y = map.spawnY; }
        player.x = x; player.y = y; player.vx = player.vy = 0f; player.z = 0f; player.vz = 0f;
        player.grounded = true; player.facingX = 1; player.facingBack = false; player.idleTime = 0f;
        player.state = Player.State.IDLE;
    }

    public void update(float dt, InputManager in) {
        time += dt;
        movement.update(player, in, map, dt);
        if (collection.update(this, dt)) collectionChanged = true;
        lumi.update(dt, player);
    }
}
