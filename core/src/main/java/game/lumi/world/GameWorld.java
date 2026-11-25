package game.lumi.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Array;
import game.lumi.art.ArtSheet;
import game.lumi.characters.LumiCharacter;
import game.lumi.characters.AnimalCharacter;
import game.lumi.entities.Animal;
import game.lumi.entities.Collectible;
import game.lumi.entities.Decor;
import game.lumi.entities.Player;
import game.lumi.entities.PuzzleProp;
import game.lumi.puzzles.FlowerPuzzle;
import game.lumi.puzzles.Puzzle;
import game.lumi.puzzles.StonePuzzle;
import game.lumi.systems.HintSystem;
import game.lumi.input.InputManager;
import game.lumi.systems.AnimalSystem;
import game.lumi.systems.CollectionSystem;
import game.lumi.systems.InteractionSystem;
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
    public final Array<Animal> animals = new Array<>();
    public final Array<PuzzleProp> props = new Array<>();
    public final FlowerPuzzle flowers = new FlowerPuzzle();
    public final StonePuzzle stones = new StonePuzzle();
    public final Array<Puzzle> puzzles = new Array<>();
    /** Aktueller Hinweis (leuchtendes Signal an einem ungelösten Rätselteil). */
    public PuzzleProp hintTarget;
    public float hintTime;
    private final HintSystem hints = new HintSystem();
    public final AnimalCharacter animalArt;
    /** Nächstes interaktives Tier in Reichweite (oder null) – steuert Hinweis-Taste. */
    public Animal interactTarget;
    public float time;
    /** Klang-Ereignisse; werden pro Frame vom Bildschirm an den AudioManager übergeben (Spiellogik kennt kein Audio). */
    public final Array<String> sfx = new Array<>();
    public final com.badlogic.gdx.utils.FloatArray sfxVol = new com.badlogic.gdx.utils.FloatArray();
    private float stepAccum;
    /** Wird gesetzt, wenn sich der Sammelstand ändert (Speichern). */
    public boolean collectionChanged;
    /** Tier-Fortschritt hat sich geändert (Speichern). */
    public boolean progressChanged;
    private final MovementSystem movement = new MovementSystem();
    private final CollectionSystem collection = new CollectionSystem();
    private final AnimalSystem animalSystem = new AnimalSystem();
    private final InteractionSystem interaction = new InteractionSystem(animalSystem);

    public GameWorld(ArtSheet art) {
        map = new WorldMap(art);
        lumi = new LumiCharacter(art);
        particles = new ParticleSystem(art);
        placePuzzleProps();
        placeCrystals();
        animalArt = new AnimalCharacter(art);
        placeAnimals();
        for (Animal a : animals) com.badlogic.gdx.Gdx.app.log("Lumi", "Tier " + a.type + " bei " + a.x + "," + a.y);
        for (Collectible c : crystals) com.badlogic.gdx.Gdx.app.log("Lumi", "Kristall " + c.id + " bei " + c.x + "," + c.y + (c.hidden ? " (versteckt)" : ""));
        for (PuzzleProp pr : props) com.badlogic.gdx.Gdx.app.log("Lumi", "Prop " + pr.kind + " " + pr.idx + " bei " + pr.x + "," + pr.y);
        stones.bind(this);
        puzzles.add(flowers); puzzles.add(stones);
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
                if (!map.free(nx * STEP, ny * STEP, 0.22f)) continue;
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
        // feste Kristalle: Belohnung des Blumenrätsels (erscheint erst nach dem Lösen) + 3 in der geheimen Lichtung
        flowers.reward = add(flowerCx, flowerCy, false);
        flowers.reward.active = false;
        float[][] inner = { { -1.2f, 0.4f }, { 1.1f, 0.7f }, { 0.1f, -1.3f } };
        for (float[] o : inner) add(map.secretX + o[0], map.secretY + o[1], false);
        int tries = 0;
        while (crystals.size < 10 && tries++ < 5000) {
            float x = map.spawnX + r.nextFloat() * 36f - 18f, y = map.spawnY + r.nextFloat() * 36f - 18f;
            float dx = x - map.spawnX, dy = y - map.spawnY;
            if (dx * dx + dy * dy < 16f || map.inSecret(x, y, 1.5f) || nearProps(x, y, 2.4f) || map.field(x, y) > 0.8f || !map.free(x, y, 0.5f) || !reachable(x, y) || !farFromOthers(x, y, 5f)) continue;
            add(x, y, false);
        }
        // hinter Bäumen: "oben" auf dem Bildschirm = kleinere Tiefe → x, y verkleinern
        Array<Decor> trees = new Array<>();
        for (Decor d : map.decor) if (d.occluder && d.solid > 0.2f && map.field(d.x, d.y) < 0.75f) trees.add(d);
        // deterministisch mischen (Speicherstände referenzieren Kristalle über ihren Index!)
        for (int i = trees.size - 1; i > 0; i--) trees.swap(i, r.nextInt(i + 1));
        for (Decor t : trees) {
            if (crystals.size >= CRYSTALS) break;
            float x = t.x - 0.95f, y = t.y - 0.95f;
            if (map.inSecret(x, y, 1.5f) || nearProps(x, y, 2.4f)) continue;
            if (!map.free(x, y, 0.45f) || !reachable(x, y) || !farFromOthers(x, y, 5f)) continue;
            add(x, y, true);
        }
        // Falls zu wenig versteckte Plätze: Rest offen
        tries = 0;
        while (crystals.size < CRYSTALS && tries++ < 5000) {
            float x = map.spawnX + r.nextFloat() * 36f - 18f, y = map.spawnY + r.nextFloat() * 36f - 18f;
            if (map.inSecret(x, y, 1.5f) || nearProps(x, y, 2.4f) || map.field(x, y) > 0.8f || !map.free(x, y, 0.5f) || !reachable(x, y) || !farFromOthers(x, y, 3f)) continue;
            add(x, y, false);
        }
        crystals.sort((a, b) -> Float.compare(a.depth(), b.depth()));
        for (int i = 0; i < crystals.size; i++) crystals.get(i).id = i;
    }

    private void findFree(Animal a, float x, float y) {
        for (float r = 0f; r < 4f; r += 0.35f) {
            for (int k = 0; k < 10; k++) {
                float ang = k * 0.628f + r;
                float px = x + MathUtils.cos(ang) * r, py = y + MathUtils.sin(ang) * r;
                if (map.walkable(px, py) && map.free(px, py, 0.4f) && map.distToPath(px, py) > 0f) { a.x = px; a.y = py; a.homeX = px; a.homeY = py; return; }
            }
        }
        a.x = a.homeX = map.spawnX; a.y = a.homeY = map.spawnY;
    }

    /** Kaninchen und Vogel nahe der Lichtung; das Eichhörnchen wohnt neben einem versteckten Kristall (sein Geschenk). */
    private void placeAnimals() {
        Animal rabbit = new Animal(Animal.Type.RABBIT);
        findFree(rabbit, map.spawnX - 6f, map.spawnY + 3.5f);
        Animal bird = new Animal(Animal.Type.BIRD);
        findFree(bird, map.spawnX + 4.5f, map.spawnY - 4.5f);
        Animal squirrel = new Animal(Animal.Type.SQUIRREL);
        Collectible gift = null;
        for (Collectible c : crystals) if (c.hidden) { gift = c; break; }
        if (gift == null) gift = crystals.get(crystals.size - 1);
        gift.active = false;
        squirrel.crystalId = gift.id;
        findFree(squirrel, gift.x + 0.9f, gift.y - 0.9f);
        for (Animal a : new Animal[] { rabbit, squirrel, bird }) {
            a.timer = MathUtils.random(0.5f, 3f);
            animals.add(a);
        }
    }

    /** Zeichen 0/1: Kaninchen gefüttert, Eichhörnchen gegraben, Blumenrätsel gelöst, Stein 1–3 aktiviert. */
    public String flagsBits() {
        StringBuilder sb = new StringBuilder();
        for (Animal a : animals) if (a.type != Animal.Type.BIRD) sb.append(a.done ? '1' : '0');
        sb.append(flowers.solved ? '1' : '0');
        for (PuzzleProp st : stones.stones) sb.append(st.lit ? '1' : '0');
        return sb.toString();
    }

    public void loadFlags(String bits) {
        int i = 0;
        for (Animal a : animals) {
            if (a.type == Animal.Type.BIRD) continue;
            a.done = i < bits.length() && bits.charAt(i) == '1';
            i++;
            a.happy = 0f; a.bubble = 0f;
            if (a.type == Animal.Type.SQUIRREL && a.crystalId >= 0) {
                Collectible c = crystals.get(a.crystalId);
                c.active = a.done || c.collected;
                if (c.collected) a.done = true;
            }
        }
        boolean fs = i < bits.length() && bits.charAt(i++) == '1';
        if (flowers.reward != null && flowers.reward.collected) fs = true;
        flowers.loadSolved(fs);
        boolean[] lit = new boolean[3];
        for (int k = 0; k < 3; k++) lit[k] = i < bits.length() && bits.charAt(i++) == '1';
        stones.load(lit);
        hintTarget = null; hintTime = 0f;
    }

    private float flowerCx, flowerCy;

    private boolean nearProps(float x, float y, float d) {
        for (PuzzleProp p : props) { float dx = p.x - x, dy = p.y - y; if (dx * dx + dy * dy < d * d) return true; }
        return false;
    }

    private final float[] spotOut = new float[2];

    /** Sucht spiralförmig einen freien, begehbaren Platz in Wunschnähe (nicht auf dem Weg, nicht in der geheimen Lichtung). */
    private float[] spot(float x, float y, float clear) {
        for (float r = 0f; r < 6f; r += 0.4f) {
            for (int k = 0; k < 12; k++) {
                float ang = k * 0.5236f + r * 1.3f;
                float px = x + MathUtils.cos(ang) * r, py = y + MathUtils.sin(ang) * r;
                if (map.free(px, py, clear) && map.field(px, py) < 0.8f && !map.inSecret(px, py, 2f) && map.distToPath(px, py) > 0.9f) {
                    spotOut[0] = px; spotOut[1] = py; return spotOut;
                }
            }
        }
        spotOut[0] = x; spotOut[1] = y; return spotOut;
    }

    private PuzzleProp prop(PuzzleProp.Kind k, int idx, float x, float y) {
        PuzzleProp p = new PuzzleProp(k, idx);
        p.x = x; p.y = y; p.phase = MathUtils.random(MathUtils.PI2);
        props.add(p);
        return p;
    }

    /** Blumenrätsel nahe der Lichtung, drei magische Steine weit verteilt. */
    private void placePuzzleProps() {
        float[] f = spot(map.spawnX - 2f, map.spawnY - 7.5f, 1.8f);
        flowerCx = f[0]; flowerCy = f[1];
        int[] colorAtPos = { 2, 0, 1 }; // räumliche Reihenfolge ≠ Lösungsreihenfolge → man muss den Hinweisstein lesen
        for (int i = 0; i < 3; i++) {
            float a = i * 2.094f + 0.6f;
            float[] q = spot(flowerCx + MathUtils.cos(a) * 1.6f, flowerCy + MathUtils.sin(a) * 1.6f, 0.45f);
            flowers.flowers[i] = prop(PuzzleProp.Kind.FLOWER, colorAtPos[i], q[0], q[1]);
        }
        float[] h = spot(flowerCx + 2.6f, flowerCy + 2.6f, 0.9f);
        flowers.hint.x = h[0]; flowers.hint.y = h[1];
        props.add(flowers.hint);
        map.addObstacle(h[0], h[1], 0.5f);
        float[][] want = { { -10f, -3f }, { 3f, 10f }, { 11f, 1f } };
        for (int i = 0; i < 3; i++) {
            float[] q = spot(map.spawnX + want[i][0], map.spawnY + want[i][1], 0.8f);
            stones.stones[i] = prop(PuzzleProp.Kind.STONE, i, q[0], q[1]);
            map.addObstacle(q[0], q[1], 0.4f);
        }
    }

    private boolean farFromOthers(float x, float y, float d) {
        for (Collectible c : crystals) { float dx = c.x - x, dy = c.y - y; if (dx * dx + dy * dy < d * d) return false; }
        return true;
    }

    private Collectible add(float x, float y, boolean hidden) {
        Collectible c = new Collectible();
        c.x = x; c.y = y; c.hidden = hidden; c.phase = MathUtils.random(MathUtils.PI2);
        crystals.add(c);
        return c;
    }

    public void sfx(String name, float vol) { if (sfx.size < 24) { sfx.add(name); sfxVol.add(vol); } }

    /** Klang an einem Ort: mit dem Abstand zu Lumi leiser. */
    public void sfxAt(String name, float vol, float x, float y) {
        float dx = x - player.x, dy = y - player.y, d = (float) Math.sqrt(dx * dx + dy * dy);
        float k = MathUtils.clamp(1f - d / 14f, 0f, 1f);
        sfx(name, vol * k * k);
    }

    private void footsteps(float dt) {
        float sp = (float) Math.sqrt(player.vx * player.vx + player.vy * player.vy);
        if (player.justJumped) sfx("jump", 0.5f);
        if (player.justLanded) sfx("land", 0.55f);
        if (!player.grounded || sp < 0.6f) { stepAccum = Math.min(stepAccum, 0.4f); return; }
        stepAccum += sp * dt;
        float stride = sp > 4.2f ? 1.15f : 0.85f;
        if (stepAccum >= stride) {
            stepAccum = 0f;
            boolean path = map.distToPath(player.x, player.y) < 0.75f;
            sfx((path ? "step_path" : "step_grass") + MathUtils.random(2), 0.5f + MathUtils.random(0.15f));
        }
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
        footsteps(dt);
        for (Puzzle pz : puzzles) pz.update(this, dt);
        hints.update(this, dt);
        animalSystem.update(this, dt);
        interaction.update(this, in);
        if (collection.update(this, dt)) collectionChanged = true;
        lumi.update(dt, player);
    }
}
