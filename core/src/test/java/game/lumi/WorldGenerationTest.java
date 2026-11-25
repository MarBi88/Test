package game.lumi;

import static org.junit.Assert.*;

import com.badlogic.gdx.utils.Array;
import game.lumi.entities.Animal;
import game.lumi.entities.Collectible;
import game.lumi.entities.Player;
import game.lumi.entities.PuzzleProp;
import game.lumi.world.GameWorld;
import game.lumi.world.WorldMap;
import org.junit.BeforeClass;
import org.junit.Test;

public class WorldGenerationTest {
    @BeforeClass public static void setUp() { TestWorlds.init(); }

    @Test public void fifteenUniqueCrystalsInsideTheWorld() {
        GameWorld w = TestWorlds.newWorld();
        assertEquals(15, w.crystals.size);
        for (int i = 0; i < w.crystals.size; i++) {
            Collectible c = w.crystals.get(i);
            assertEquals("Index stabil", i, c.id);
            assertTrue("Kristall " + i + " innerhalb der Karte", w.map.field(c.x, c.y) < 0.93f);
            for (int j = i + 1; j < w.crystals.size; j++) {
                Collectible o = w.crystals.get(j);
                assertTrue("Kristalle überlappen nicht", Math.hypot(c.x - o.x, c.y - o.y) > 0.8);
            }
        }
    }

    @Test public void worldIsDeterministic() {
        GameWorld a = TestWorlds.newWorld(), b = TestWorlds.newWorld();
        for (int i = 0; i < a.crystals.size; i++) {
            assertEquals(a.crystals.get(i).x, b.crystals.get(i).x, 1e-5f);
            assertEquals(a.crystals.get(i).y, b.crystals.get(i).y, 1e-5f);
            assertEquals(a.crystals.get(i).hidden, b.crystals.get(i).hidden);
        }
        for (int i = 0; i < a.animals.size; i++) assertEquals(a.animals.get(i).x, b.animals.get(i).x, 1e-5f);
        for (int i = 0; i < a.props.size; i++) assertEquals(a.props.get(i).x, b.props.get(i).x, 1e-5f);
        assertEquals(a.map.decor.size, b.map.decor.size);
    }

    /** Alles Spielrelevante ist erreichbar, sobald das Tor offen ist; ohne offenes Tor ist die geheime Lichtung unerreichbar. */
    @Test public void reachabilityWithAndWithoutGate() {
        GameWorld w = TestWorlds.newWorld();
        w.map.gateOpen = true;
        boolean[] open = flood(w.map);
        for (Collectible c : w.crystals) assertTrue("Kristall " + c.id + " erreichbar (Tor offen)", near(open, w.map, c.x, c.y, 0.7f));
        for (Animal a : w.animals) assertTrue(a.type + " erreichbar", near(open, w.map, a.x, a.y, 1.0f));
        for (PuzzleProp p : w.props) assertTrue(p.kind + " erreichbar", near(open, w.map, p.x, p.y, 1.0f));

        w.map.gateOpen = false;
        boolean[] closed = flood(w.map);
        assertFalse("Lichtung bei geschlossenem Tor unerreichbar", near(closed, w.map, w.map.secretX, w.map.secretY, 1.0f));
        int inside = 0;
        for (Collectible c : w.crystals) if (w.map.inSecret(c.x, c.y, 0f)) { inside++; assertFalse(near(closed, w.map, c.x, c.y, 0.7f)); }
        assertEquals("3 Kristalle in der Lichtung", 3, inside);
        // alle übrigen Kristalle bleiben ohne Tor erreichbar
        for (Collectible c : w.crystals) if (!w.map.inSecret(c.x, c.y, 0f)) assertTrue("Kristall " + c.id + " ohne Tor erreichbar", near(closed, w.map, c.x, c.y, 0.7f));
    }

    private static final float STEP = 0.25f;

    static boolean[] flood(WorldMap m) {
        int n = (int) (WorldMap.N / STEP);
        boolean[] seen = new boolean[n * n];
        int[] q = new int[n * n]; int h = 0, t = 0;
        int s = (int) (m.spawnY / STEP) * n + (int) (m.spawnX / STEP);
        seen[s] = true; q[t++] = s;
        int[] dx = { 1, -1, 0, 0 }, dy = { 0, 0, 1, -1 };
        while (h < t) {
            int c = q[h++], cx = c % n, cy = c / n;
            for (int k = 0; k < 4; k++) {
                int nx = cx + dx[k], ny = cy + dy[k];
                if (nx < 0 || ny < 0 || nx >= n || ny >= n || seen[ny * n + nx]) continue;
                if (!m.free(nx * STEP, ny * STEP, Player.RADIUS + 0.02f)) continue;
                seen[ny * n + nx] = true; q[t++] = ny * n + nx;
            }
        }
        return seen;
    }

    static boolean near(boolean[] seen, WorldMap m, float x, float y, float r) {
        int n = (int) (WorldMap.N / STEP), rr = (int) Math.ceil(r / STEP);
        int cx = (int) (x / STEP), cy = (int) (y / STEP);
        for (int j = -rr; j <= rr; j++) for (int i = -rr; i <= rr; i++) {
            int nx = cx + i, ny = cy + j;
            if (nx < 0 || ny < 0 || nx >= n || ny >= n || !seen[ny * n + nx]) continue;
            if (Math.hypot(i * STEP, j * STEP) <= r) return true;
        }
        return false;
    }
}
