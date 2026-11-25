package game.lumi.systems;

import com.badlogic.gdx.math.MathUtils;
import game.lumi.config.GameConfig;
import game.lumi.entities.Player;
import game.lumi.input.InputManager;
import game.lumi.world.Iso;
import game.lumi.world.WorldMap;

/** Bewegung, Sprung-Physik und Zustandswechsel von Lumi. */
public final class MovementSystem {
    private static final float GRAVITY = 1900f, JUMP_V = 560f;
    private static final float SLEEP_AFTER = 18f;
    private final float[] out = new float[2];

    public void update(Player p, InputManager in, WorldMap map, float dt) {
        p.justLanded = false; p.justJumped = false;
        float mag = Math.min(1f, (float) Math.sqrt(in.moveX * in.moveX + in.moveY * in.moveY));
        boolean moving = mag > 0.05f;
        float top = in.run ? GameConfig.RUN_SPEED : GameConfig.WALK_SPEED;
        // Joystick liefert stufenlos: halber Ausschlag = halbe Geschwindigkeit
        float target = moving ? top * Math.max(0.35f, mag) : 0f;
        float tvx = 0f, tvy = 0f;
        if (moving) {
            float wx = Iso.dirX(in.moveX, in.moveY), wy = Iso.dirY(in.moveX, in.moveY);
            float wl = (float) Math.sqrt(wx * wx + wy * wy);
            tvx = wx / wl * target; tvy = wy / wl * target;
        }
        // weiche Beschleunigung/Abbremsung; in der Luft etwas träger
        float k = (p.grounded ? 13f : 6f) * dt;
        p.vx += (tvx - p.vx) * Math.min(1f, k);
        p.vy += (tvy - p.vy) * Math.min(1f, k);

        map.move(p.x, p.y, p.vx * dt, p.vy * dt, Player.RADIUS, out);
        p.x = out[0]; p.y = out[1];

        float sp = (float) Math.sqrt(p.vx * p.vx + p.vy * p.vy);
        p.speed01 = sp / GameConfig.RUN_SPEED;

        if (moving) {
            if (in.moveX > 0.2f) p.facingX = 1; else if (in.moveX < -0.2f) p.facingX = -1;
            p.facingBack = in.moveY > 0.4f && Math.abs(in.moveY) > Math.abs(in.moveX) * 0.55f;
            if (in.moveY < -0.2f) p.facingBack = false;
        }

        // Sprung
        if (in.jump && p.grounded && p.state != Player.State.LAND) {
            p.vz = JUMP_V; p.grounded = false; p.justJumped = true;
            p.setState(Player.State.JUMP);
        }
        if (!p.grounded) {
            p.vz -= GRAVITY * dt;
            p.z += p.vz * dt;
            if (p.z <= 0f) {
                p.z = 0f; p.vz = 0f; p.grounded = true; p.justLanded = true;
                p.setState(Player.State.LAND);
            }
        }

        p.stateTime += dt;
        switch (p.state) {
            case JUMP: break;
            case LAND:
                if (p.stateTime > 0.24f) p.setState(moving ? Player.State.WALK : Player.State.IDLE);
                break;
            default:
                if (moving) {
                    p.idleTime = 0f;
                    p.setState(sp > GameConfig.WALK_SPEED * 1.18f ? Player.State.RUN : Player.State.WALK);
                } else if (sp < 0.4f) {
                    p.idleTime += dt;
                    p.setState(p.idleTime > SLEEP_AFTER ? Player.State.SLEEP : Player.State.IDLE);
                }
        }
        if (moving || in.jump) p.idleTime = 0f;
        p.speed01 = MathUtils.clamp(p.speed01, 0f, 1.3f);
    }
}
