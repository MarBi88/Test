package game.lumi.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Musik, Ambiente und Effekte. Alle Klänge liegen als kleine MP3-Dateien in assets/audio
 * (selbst erzeugt, siehe tools/make_audio.py). Alles läuft über libGDX-{@link Sound}
 * (Web Audio im Browser): Schleifen sind lückenlos, Lautstärken lassen sich live ändern.
 *
 * Browser-Autoplay: Es wird erst nach der ersten Benutzerinteraktion gestartet
 * ({@link #onUserInteraction()}). Fehlende oder nicht ladbare Dateien führen nie zum Absturz.
 */
public final class AudioManager {
    public static final String[] NAMES = {
        "music_loop", "wind_loop", "water_loop", "waves_loop", "bird_a", "bird_b", "bird_c", "bird_call",
        "step_grass0", "step_grass1", "step_grass2", "step_path0", "step_path1", "step_path2",
        "jump", "land", "collect", "lumi_happy", "rabbit", "squirrel", "dig", "feed",
        "flower0", "flower1", "flower2", "munch", "wrong", "stone_on", "gate_open", "click",
    };
    private static final float MUSIC_VOL = 0.34f, WIND_VOL = 0.55f, WATER_VOL = 0.5f, WAVES_VOL = 0.55f;

    private final ObjectMap<String, Sound> sounds = new ObjectMap<>();
    private boolean enabled = true, music = true, fx = true, started;
    private long musicId = -1, windId = -1, waterId = -1, wavesId = -1;
    private float birdTimer = 5f, duck = 1f, duckTarget = 1f, waterVol, wavesVol;

    public void load() {
        for (String n : NAMES) {
            try { sounds.put(n, Gdx.audio.newSound(Gdx.files.internal("audio/" + n + ".mp3"))); }
            catch (Throwable t) { Gdx.app.error("Audio", "nicht geladen: " + n + " (" + t + ")"); }
        }
    }

    public boolean isEnabled() { return enabled; }
    public boolean isMusic() { return music; }
    public boolean isFx() { return fx; }

    public void setEnabled(boolean on) { enabled = on; applyLoops(); }
    public void setMusic(boolean on) { music = on; applyLoops(); }
    public void setFx(boolean on) { fx = on; applyLoops(); }

    /** Muss aus einer echten Benutzeraktion (Tippen/Taste) heraus aufgerufen werden. */
    public void onUserInteraction() {
        if (started) return;
        started = true;
        applyLoops();
        Gdx.app.log("Audio", "gestartet: Musik-Loop " + musicId + ", Wind " + windId + ", Wasser " + waterId + ", Sounds geladen: " + sounds.size);
    }

    private long startLoop(String name, float vol) {
        Sound s = sounds.get(name);
        if (s == null) return -1;
        try { return s.loop(vol); } catch (Throwable t) { Gdx.app.error("Audio", "loop " + name + ": " + t); return -1; }
    }

    private void stopLoop(String name, long id) {
        Sound s = sounds.get(name);
        if (s != null && id >= 0) { try { s.stop(id); } catch (Throwable ignored) { } }
    }

    private void applyLoops() {
        if (!started) return;
        boolean m = enabled && music, a = enabled && fx;
        if (m && musicId < 0) musicId = startLoop("music_loop", MUSIC_VOL * duck);
        if (!m && musicId >= 0) { stopLoop("music_loop", musicId); musicId = -1; }
        if (a && windId < 0) windId = startLoop("wind_loop", WIND_VOL);
        if (!a && windId >= 0) { stopLoop("wind_loop", windId); windId = -1; }
        if (a && waterId < 0) waterId = startLoop("water_loop", 0f);
        if (!a && waterId >= 0) { stopLoop("water_loop", waterId); waterId = -1; }
        if (a && wavesId < 0) wavesId = startLoop("waves_loop", 0f);
        if (!a && wavesId >= 0) { stopLoop("waves_loop", wavesId); wavesId = -1; }
    }

    private void setVol(String name, long id, float v) {
        Sound s = sounds.get(name);
        if (s != null && id >= 0) { try { s.setVolume(id, v); } catch (Throwable ignored) { } }
    }

    /**
     * Pro Frame aufrufen.
     * @param pondDist Abstand zum Teich in Kacheln (Wasserplätschern wird leiser)
     * @param coast    0..1: Nähe zur Küste (Meeresrauschen wird lauter)
     * @param tweets   zufälliges Vogelgezwitscher erlauben (nur im laufenden Spiel)
     * @param quiet    leiser (Pausenmenü)
     */
    public void update(float dt, float pondDist, float coast, boolean tweets, boolean quiet) {
        if (!started || !enabled) return;
        duckTarget = quiet ? 0.45f : 1f;
        duck += (duckTarget - duck) * Math.min(1f, 4f * dt);
        if (musicId >= 0) setVol("music_loop", musicId, MUSIC_VOL * duck);
        if (windId >= 0) setVol("wind_loop", windId, WIND_VOL * (quiet ? 0.5f : 1f));
        if (waterId >= 0) {
            float k = MathUtils.clamp(1f - pondDist / 11f, 0f, 1f);
            float target = k * k * WATER_VOL * (quiet ? 0.5f : 1f);
            waterVol += (target - waterVol) * Math.min(1f, 3f * dt);
            setVol("water_loop", waterId, waterVol);
        }
        if (wavesId >= 0) {
            float target = coast * coast * WAVES_VOL * (quiet ? 0.5f : 1f) + 0.06f;
            wavesVol += (target - wavesVol) * Math.min(1f, 3f * dt);
            setVol("waves_loop", wavesId, wavesVol);
        }
        if (tweets && fx) {
            birdTimer -= dt;
            if (birdTimer <= 0f) {
                birdTimer = MathUtils.random(5f, 11f);
                String[] b = { "bird_a", "bird_b", "bird_c" }; // Papageien und andere Tropenvögel
                play(b[MathUtils.random(2)], MathUtils.random(0.12f, 0.24f));
            }
        }
    }

    public void play(String name, float vol) { play(name, vol, 1f); }

    public void play(String name, float vol, float pitch) {
        if (!started || !enabled || !fx || vol <= 0.01f) return;
        Sound s = sounds.get(name);
        if (s == null) return;
        try { s.play(Math.min(1f, vol), pitch, 0f); } catch (Throwable t) { Gdx.app.error("Audio", name + ": " + t); }
    }

    public void dispose() {
        for (Sound s : sounds.values()) { try { s.dispose(); } catch (Throwable ignored) { } }
        sounds.clear();
    }
}
