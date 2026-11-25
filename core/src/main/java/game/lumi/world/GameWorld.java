package game.lumi.world;

import game.lumi.art.ArtSheet;
import game.lumi.characters.LumiCharacter;
import game.lumi.entities.Player;
import game.lumi.input.InputManager;
import game.lumi.systems.MovementSystem;
import game.lumi.systems.ParticleSystem;

/** Aggregiert den Spielzustand der Welt (Karte, Lumi, Systeme). Kein Rendering, keine Eingabe. */
public final class GameWorld {
    public final WorldMap map;
    public final Player player = new Player();
    public final LumiCharacter lumi;
    public final ParticleSystem particles;
    public float time;
    private final MovementSystem movement = new MovementSystem();

    public GameWorld(ArtSheet art) {
        map = new WorldMap(art);
        lumi = new LumiCharacter(art);
        particles = new ParticleSystem(art);
        resetPlayer(map.spawnX, map.spawnY);
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
        lumi.update(dt, player);
    }
}
