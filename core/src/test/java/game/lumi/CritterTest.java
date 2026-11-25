package game.lumi;

import static org.junit.Assert.*;

import game.lumi.entities.Critter;
import game.lumi.input.InputManager;
import game.lumi.ui.VirtualJoystick;
import game.lumi.world.GameWorld;
import org.junit.BeforeClass;
import org.junit.Test;

public class CritterTest {
    @BeforeClass public static void setUp() { TestWorlds.init(); }

    private static int count(GameWorld w, int kind) { int n = 0; for (Critter c : w.critters) if (c.kind == kind) n++; return n; }

    @Test public void islandHasPlentyOfInsectsFrogsAndSpiders() {
        GameWorld w = TestWorlds.newWorld();
        assertEquals(16, count(w, Critter.INSECT)); assertEquals(12, count(w, Critter.FROG)); assertEquals(12, count(w, Critter.SPIDER));
        for (Critter c : w.critters) {
            assertTrue("auf der Insel, nicht im Meer", w.map.field(c.x, c.y) <= 0.84f);
            assertFalse("nie in der verschlossenen Lichtung", w.map.inSecret(c.x, c.y, 0f));
        }
    }

    @Test public void lumiEatsWhatHeTouchesAndCountsIt() {
        GameWorld w = TestWorlds.newWorld(); InputManager in = TestWorlds.noInput();
        for (int kind = 0; kind < 3; kind++) {
            Critter c = null; for (Critter x : w.critters) if (x.kind == kind) { c = x; break; }
            w.player.x = c.x; w.player.y = c.y;
            TestWorlds.run(w, in, 2, 0.02f);
            assertFalse("gefressen", c.alive);
            assertEquals(1, w.eaten[kind]);
        }
        assertEquals(3, w.totalEaten());
        assertTrue(w.sfx.contains("munch", false));
        assertTrue("Speicherung wird angestoßen", w.eatChanged);
    }

    @Test public void eatenCritterRespawnsElsewhere() {
        GameWorld w = TestWorlds.newWorld(); InputManager in = TestWorlds.noInput();
        Critter c = w.critters.get(0);
        w.player.x = c.x; w.player.y = c.y;
        TestWorlds.run(w, in, 2, 0.02f);
        assertFalse(c.alive);
        float ox = c.x, oy = c.y;
        w.player.x = w.map.spawnX; w.player.y = w.map.spawnY;
        TestWorlds.run(w, in, 1300, 0.05f); // > 65 s
        assertTrue("taucht wieder auf", c.alive);
    }

    @Test public void critterFleesButIsSlowerThanLumi() {
        GameWorld w = TestWorlds.newWorld(); InputManager in = TestWorlds.noInput();
        Critter c = null; for (Critter x : w.critters) if (x.kind == Critter.SPIDER) { c = x; break; }
        w.player.x = c.x - 1.8f; w.player.y = c.y - 1.8f;
        float d0 = (float) Math.hypot(c.x - w.player.x, c.y - w.player.y);
        TestWorlds.run(w, in, 10, 0.05f);
        assertTrue("weicht aus", Math.hypot(c.x - w.player.x, c.y - w.player.y) > d0 || !c.alive);
        assertTrue(game.lumi.systems.CritterSystem.speedOf(c) < game.lumi.config.GameConfig.WALK_SPEED);
    }

    @Test public void critterWalksNeverLeaveTheIsland() {
        GameWorld w = TestWorlds.newWorld(); InputManager in = TestWorlds.noInput();
        for (int i = 0; i < 3000; i++) {
            w.update(0.05f, in);
            for (Critter c : w.critters) if (c.alive) assertTrue("Schritt " + i, w.map.field(c.x, c.y) <= 0.9f && !w.map.inSecret(c.x, c.y, 0f));
        }
    }

    @Test public void eatenCountsRoundTrip() {
        GameWorld a = TestWorlds.newWorld(); a.eaten[0] = 7; a.eaten[1] = 12; a.eaten[2] = 103;
        GameWorld b = TestWorlds.newWorld(); b.loadEaten(a.eatenString());
        assertArrayEquals(a.eaten, b.eaten);
        b.loadEaten("garbage,1"); assertEquals(0, b.eaten[0]);
        b.loadEaten(""); assertEquals(0, b.totalEaten());
    }

    @Test public void swipeAnchorFollowsTheFinger() {
        VirtualJoystick j = new VirtualJoystick();
        j.begin(0, 100, 100);
        j.drag(100 + 300, 100);              // weit wischen: Anker zieht nach
        assertEquals(1f, j.magnitude, 1e-3f);
        assertEquals("Anker folgt bis Radius", 100 + 300 - VirtualJoystick.MAX_RADIUS, j.baseX, 1e-3f);
        j.drag(400 - 20, 100 + 70);          // Richtungswechsel ohne Absetzen
        assertTrue(j.outY > 0.4f);
    }
}
