package game.lumi;

import static org.junit.Assert.*;

import game.lumi.entities.Collectible;
import game.lumi.entities.PuzzleProp;
import game.lumi.input.InputManager;
import game.lumi.world.GameWorld;
import org.junit.BeforeClass;
import org.junit.Test;

public class PuzzleTest {
    @BeforeClass public static void setUp() { TestWorlds.init(); }

    private static void stand(GameWorld w, float x, float y) { w.player.x = x + 0.2f; w.player.y = y + 0.2f; }

    private static PuzzleProp flowerWithColor(GameWorld w, int color) {
        for (PuzzleProp f : w.flowers.flowers) if (f.idx == color) return f;
        throw new AssertionError("Blume fehlt");
    }

    private static void touch(GameWorld w, InputManager in, PuzzleProp p) {
        stand(w, p.x, p.y);
        TestWorlds.run(w, in, 3, 0.05f);
        stand(w, p.x + 5f, p.y + 5f); // wieder weggehen, damit die nächste Berührung neu zählt
        w.player.x = Math.min(w.player.x, 50f);
        TestWorlds.run(w, in, 3, 0.05f);
    }

    @Test public void flowersInCorrectOrderSolveAndRevealCrystal() {
        GameWorld w = TestWorlds.newWorld(); InputManager in = TestWorlds.noInput();
        assertFalse(w.flowers.reward.active);
        for (int c = 0; c < 3; c++) touch(w, in, flowerWithColor(w, c));
        assertTrue("gelöst", w.flowers.solved);
        assertTrue("Belohnungskristall erscheint", w.flowers.reward.active);
        assertEquals('1', w.flagsBits().charAt(2));
    }

    @Test public void wrongOrderResetsGentlyAndCanBeRetried() {
        GameWorld w = TestWorlds.newWorld(); InputManager in = TestWorlds.noInput();
        touch(w, in, flowerWithColor(w, 0));           // richtig (rosa)
        touch(w, in, flowerWithColor(w, 2));           // falsch (blau statt gelb)
        assertFalse(w.flowers.solved);
        for (PuzzleProp f : w.flowers.flowers) assertFalse("Fehlversuch dimmt alle Blumen", f.lit);
        // ohne Strafe: sofort neu versuchen
        w.update(1.0f, in);                             // Sperrzeit nach Fehlversuch abwarten
        for (int c = 0; c < 3; c++) touch(w, in, flowerWithColor(w, c));
        assertTrue(w.flowers.solved);
    }

    @Test public void touchingSameFlowerTwiceDoesNotCountAsMistake() {
        GameWorld w = TestWorlds.newWorld(); InputManager in = TestWorlds.noInput();
        PuzzleProp pink = flowerWithColor(w, 0);
        touch(w, in, pink); touch(w, in, pink);
        assertTrue(pink.lit);
    }

    @Test public void stonesOpenGateAndGateBlocksBefore() {
        GameWorld w = TestWorlds.newWorld(); InputManager in = TestWorlds.noInput();
        float gx = w.map.secretX + (float) Math.cos(w.map.gateAngle) * 4.6f, gy = w.map.secretY + (float) Math.sin(w.map.gateAngle) * 4.6f;
        float[] out = new float[2];
        // geschlossen: Schritt Richtung Lichtungsmitte durch das Tor bleibt draußen
        float x = gx, y = gy;
        for (int i = 0; i < 60; i++) {
            float dx = w.map.secretX - x, dy = w.map.secretY - y, l = (float) Math.hypot(dx, dy);
            w.map.move(x, y, dx / l * 0.1f, dy / l * 0.1f, 0.2f, out); x = out[0]; y = out[1];
        }
        assertTrue("bleibt vor dem Tor (Abstand " + Math.hypot(x - w.map.secretX, y - w.map.secretY) + ")", Math.hypot(x - w.map.secretX, y - w.map.secretY) > 3.0);
        assertFalse(w.map.gateOpen);
        for (PuzzleProp s : w.stones.stones) { stand(w, s.x, s.y); TestWorlds.run(w, in, 3, 0.05f); }
        assertTrue("alle Steine leuchten", w.stones.litCount() == 3);
        assertTrue(w.map.gateOpen);
        for (int i = 0; i < 90; i++) {
            float dx = w.map.secretX - x, dy = w.map.secretY - y, l = (float) Math.hypot(dx, dy);
            w.map.move(x, y, dx / l * 0.1f, dy / l * 0.1f, 0.2f, out); x = out[0]; y = out[1];
        }
        assertTrue("offen: Lumi gelangt hinein", Math.hypot(x - w.map.secretX, y - w.map.secretY) < 2.0);
    }

    @Test public void progressRoundTripsThroughSaveFlags() {
        GameWorld a = TestWorlds.newWorld(); InputManager in = TestWorlds.noInput();
        for (int c = 0; c < 3; c++) touch(a, in, flowerWithColor(a, c));
        stand(a, a.stones.stones[1].x, a.stones.stones[1].y); TestWorlds.run(a, in, 3, 0.05f);
        a.animals.get(0).done = true;
        String bits = a.flagsBits();
        GameWorld b = TestWorlds.newWorld();
        b.loadFlags(bits);
        assertEquals(bits, b.flagsBits());
        assertTrue(b.flowers.solved && b.flowers.reward.active);
        assertTrue(b.stones.stones[1].lit); assertFalse(b.stones.stones[0].lit);
        assertFalse("Tor nur offen, wenn alle drei Steine leuchten", b.map.gateOpen);
        // alle Steine geladen → Tor offen
        b.loadFlags("1" + "1" + "1" + "111");
        assertTrue(b.map.gateOpen);
    }

    @Test public void crystalCollectionPersistsAsBits() {
        GameWorld a = TestWorlds.newWorld(); InputManager in = TestWorlds.noInput();
        Collectible c = null;
        for (Collectible k : a.crystals) if (k.active && !a.map.inSecret(k.x, k.y, 0f)) { c = k; break; }
        stand(a, c.x - 0.2f, c.y - 0.2f); a.player.x = c.x; a.player.y = c.y;
        TestWorlds.run(a, in, 3, 0.05f);
        assertTrue(c.collected);
        assertEquals(1, a.collectedCount());
        GameWorld b = TestWorlds.newWorld(); b.loadCollected(a.collectedBits());
        assertTrue(b.crystals.get(c.id).collected);
        assertEquals(1, b.collectedCount());
        assertEquals(15, b.collectedBits().length());
    }
}
