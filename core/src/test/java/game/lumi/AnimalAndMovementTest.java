package game.lumi;

import static org.junit.Assert.*;

import com.badlogic.gdx.math.MathUtils;
import game.lumi.entities.Animal;
import game.lumi.entities.Collectible;
import game.lumi.entities.Player;
import game.lumi.input.InputManager;
import game.lumi.input.PointerTracker;
import game.lumi.world.GameWorld;
import game.lumi.world.Iso;
import org.junit.BeforeClass;
import org.junit.Test;

public class AnimalAndMovementTest {
    @BeforeClass public static void setUp() { TestWorlds.init(); }

    private static Animal animal(GameWorld w, Animal.Type t) { for (Animal a : w.animals) if (a.type == t) return a; throw new AssertionError(); }

    @Test public void rabbitCanBeFedOnce() {
        GameWorld w = TestWorlds.newWorld(); InputManager in = TestWorlds.noInput();
        Animal r = animal(w, Animal.Type.RABBIT);
        w.player.x = r.x + 0.8f; w.player.y = r.y + 0.8f;
        w.update(0.05f, in);
        assertNotNull("Interaktionsziel in Reichweite", w.interactTarget);
        in.interact = true; w.update(0.05f, in); in.interact = false;
        assertTrue(r.done);
        assertTrue(w.sfx.contains("feed", false));
        w.update(0.05f, in);
        assertNull("nach dem Füttern keine weitere Interaktion", w.interactTarget);
    }

    @Test public void squirrelDigsAndRevealsItsCrystal() {
        GameWorld w = TestWorlds.newWorld(); InputManager in = TestWorlds.noInput();
        Animal s = animal(w, Animal.Type.SQUIRREL);
        Collectible gift = w.crystals.get(s.crystalId);
        assertFalse("Geschenk ist zunächst unsichtbar", gift.active);
        w.player.x = s.x + 0.8f; w.player.y = s.y + 0.8f;
        w.update(0.05f, in);
        in.interact = true; w.update(0.05f, in); in.interact = false;
        for (int i = 0; i < 50; i++) { s.x = w.player.x - 0.8f; s.y = w.player.y - 0.8f; w.update(0.05f, in); }
        assertTrue(s.done); assertTrue(gift.active);
        assertEquals('1', w.flagsBits().charAt(1));
    }

    @Test public void birdFliesToAnUncollectedHiddenCrystal() {
        GameWorld w = TestWorlds.newWorld(); InputManager in = TestWorlds.noInput();
        Animal b = animal(w, Animal.Type.BIRD);
        w.player.x = b.x + 1f; w.player.y = b.y + 1f;
        for (int i = 0; i < 400; i++) { w.player.x = b.homeX + 1f; w.player.y = b.homeY + 1f; w.update(0.05f, in); }
        assertTrue("Vogel hat ein Ziel", b.crystalId >= 0);
        Collectible c = w.crystals.get(b.crystalId);
        assertTrue(c.hidden && c.active && !c.collected);
        assertTrue("Vogel in der Nähe des Kristalls", Math.hypot(b.x - c.x, b.y - c.y) < 2.5);
    }

    @Test public void lumiNeverEndsInsideAnObstacleAndKeepsMoving() {
        GameWorld w = TestWorlds.newWorld(); InputManager in = TestWorlds.noInput();
        MathUtils.random.setSeed(5);
        float travelled = 0f, lx = w.player.x, ly = w.player.y;
        for (int i = 0; i < 4000; i++) {
            if (i % 40 == 0) { float a = MathUtils.random(MathUtils.PI2); in.moveX = MathUtils.cos(a); in.moveY = MathUtils.sin(a); in.run = MathUtils.randomBoolean(); }
            w.update(1f / 60f, in);
            travelled += (float) Math.hypot(w.player.x - lx, w.player.y - ly); lx = w.player.x; ly = w.player.y;
            assertTrue("Schritt " + i + ": Position begehbar", w.map.walkable(w.player.x, w.player.y));
            assertTrue("Schritt " + i + ": nicht in Hindernis", w.map.free(w.player.x, w.player.y, Player.RADIUS - 0.03f));
        }
        assertTrue("Lumi bewegt sich tatsächlich (" + travelled + " Kacheln)", travelled > 40f);
    }

    @Test public void lumiSlidesAlongAnObstacleInsteadOfStopping() {
        GameWorld w = TestWorlds.newWorld();
        float[] out = new float[2];
        // erstes massives Hindernis (Baum/Busch) suchen und frontal schräg dagegen laufen
        game.lumi.entities.Decor d = null;
        for (game.lumi.entities.Decor x : w.map.decor) if (x.solid > 0.25f && !x.gate && w.map.field(x.x, x.y) < 0.6f) { d = x; break; }
        assertNotNull(d);
        float sx = d.x - 1.2f, sy = d.y - 0.9f; // schräg versetzt
        if (!w.map.free(sx, sy, 0.2f)) { sx = d.x - 1.5f; sy = d.y - 1.5f; }
        float x = sx, y = sy;
        for (int i = 0; i < 60; i++) {
            w.map.move(x, y, 0.08f, 0.08f, 0.2f, out); x = out[0]; y = out[1];
            assertTrue("Schritt " + i + ": nie im Hindernis", w.map.free(x, y, 0.19f));
        }
        assertTrue("kam an dem Objekt vorbei oder glitt zumindest weiter", Math.hypot(x - sx, y - sy) > 2.0);
    }

    @Test public void isoProjectionRoundTrip() {
        for (float[] p : new float[][] { { 3f, 4f }, { 10f, 2f }, { 0f, 0f }, { 30.5f, 12.25f } }) {
            float sx = Iso.sx(p[0], p[1]), sy = Iso.sy(p[0], p[1]);
            float wxMinusWy = sx / Iso.HW, wxPlusWy = -sy / Iso.HH;
            assertEquals(p[0], (wxPlusWy + wxMinusWy) / 2f, 1e-4f);
            assertEquals(p[1], (wxPlusWy - wxMinusWy) / 2f, 1e-4f);
        }
        // Bildschirm "rechts" zeigt weltweit nach (+,−): 2:1-korrigiert
        assertTrue(Iso.dirX(1, 0) > 0 && Iso.dirY(1, 0) < 0);
        assertTrue(Iso.dirX(0, 1) < 0 && Iso.dirY(0, 1) < 0);
    }

    @Test public void joystickHasDeadzoneAndClamps() {
        game.lumi.ui.VirtualJoystick j = new game.lumi.ui.VirtualJoystick();
        j.begin(0, 100, 100);
        j.drag(105, 100);
        assertEquals("Totzone", 0f, j.magnitude, 0f);
        j.drag(100 + 500, 100);
        assertEquals("begrenzt auf 1", 1f, j.magnitude, 1e-4f);
        assertEquals(1f, j.outX, 1e-4f);
        j.end();
        assertFalse(j.active);
    }
}
