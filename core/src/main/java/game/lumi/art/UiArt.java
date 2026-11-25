package game.lumi.art;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/** Kindgerechte UI-Grafiken: weiß (wird per Tint eingefärbt), große runde Formen, ohne Text. */
public final class UiArt {
    private UiArt() {}

    static void build(ArtSheet s) {
        Painter btn = s.alloc("ui_circle", 192, 192);
        btn.disc(96, 96, 92, Color.WHITE, 1f);
        Painter ring = s.alloc("ui_ring", 192, 192);
        // Ring aus Kreisen gelegt (kein Blending.None: im Canvas-Backend würde "copy" die ganze Fläche löschen).
        for (float a = 0; a < MathUtils.PI2; a += 0.02f)
            ring.disc(96 + MathUtils.cos(a) * 86, 96 + MathUtils.sin(a) * 86, 6f, Color.WHITE, 1f);
        Painter pill = s.alloc("ui_pill", 256, 128);
        pill.disc(64, 64, 60, Color.WHITE, 1f);
        pill.disc(192, 64, 60, Color.WHITE, 1f);
        pill.rect(64, 4, 128, 120, Color.WHITE, 1f);

        Painter play = s.alloc("icon_play", 128, 128);
        play.tri(38, 18, 38, 110, 108, 64, Color.WHITE, 1f);
        play.disc(38, 28, 10, Color.WHITE, 1f); play.disc(38, 100, 10, Color.WHITE, 1f); play.disc(100, 64, 10, Color.WHITE, 1f);

        Painter pause = s.alloc("icon_pause", 128, 128);
        pause.rect(32, 24, 26, 80, Color.WHITE, 1f); pause.rect(70, 24, 26, 80, Color.WHITE, 1f);
        pause.disc(45, 24, 13, Color.WHITE, 1f); pause.disc(45, 104, 13, Color.WHITE, 1f);
        pause.disc(83, 24, 13, Color.WHITE, 1f); pause.disc(83, 104, 13, Color.WHITE, 1f);

        for (int on = 0; on < 2; on++) {
            Painter sp = s.alloc(on == 1 ? "icon_sound_on" : "icon_sound_off", 128, 128);
            sp.rect(18, 46, 22, 36, Color.WHITE, 1f);
            sp.tri(36, 46, 36, 82, 64, 108, Color.WHITE, 1f);
            sp.tri(36, 46, 36, 82, 64, 20, Color.WHITE, 1f);
            sp.tri(40, 46, 64, 20, 64, 108, Color.WHITE, 1f);
            if (on == 1) {
                for (int i = 0; i < 2; i++) {
                    float r = 22 + i * 18;
                    for (float a = -0.9f; a <= 0.9f; a += 0.05f)
                        sp.disc(70 + MathUtils.cos(a) * r, 64 + MathUtils.sin(a) * r, 4f, Color.WHITE, 1f);
                }
            } else {
                sp.stroke(78, 44, 112, 84, 9, Color.WHITE, 1f);
                sp.stroke(112, 44, 78, 84, 9, Color.WHITE, 1f);
            }
        }

        Painter home = s.alloc("icon_home", 128, 128);
        home.tri(14, 62, 64, 18, 114, 62, Color.WHITE, 1f);
        home.rect(30, 58, 24, 50, Color.WHITE, 1f); home.rect(74, 58, 24, 50, Color.WHITE, 1f); home.rect(30, 58, 68, 20, Color.WHITE, 1f);
        home.stroke(14, 62, 64, 18, 10, Color.WHITE, 1f); home.stroke(114, 62, 64, 18, 10, Color.WHITE, 1f);

        Painter jump = s.alloc("icon_jump", 128, 128);
        jump.stroke(64, 100, 64, 34, 16, Color.WHITE, 1f);
        jump.stroke(64, 24, 28, 62, 16, Color.WHITE, 1f);
        jump.stroke(64, 24, 100, 62, 16, Color.WHITE, 1f);

        Painter paw = s.alloc("icon_paw", 128, 128);
        paw.ellipse(64, 80, 28, 23, Color.WHITE);
        paw.ellipse(26, 54, 11, 15, Color.WHITE); paw.ellipse(50, 32, 11, 16, Color.WHITE);
        paw.ellipse(78, 32, 11, 16, Color.WHITE); paw.ellipse(102, 54, 11, 15, Color.WHITE);

        Painter gem = s.alloc("icon_crystal", 96, 96);
        gem.tri(48, 6, 14, 38, 82, 38, Painter.rgb(0xB5FFF2), 1f);
        gem.tri(14, 38, 82, 38, 48, 92, Painter.rgb(0x58E6D6), 1f);
        gem.tri(48, 6, 48, 38, 82, 38, Painter.rgb(0xDFFFFA), 0.7f);
        gem.tri(48, 38, 82, 38, 48, 92, Painter.rgb(0x2FB8AA), 0.8f);

        Painter knob = s.alloc("ui_knob", 128, 128);
        knob.sphere(64, 64, 60, Color.WHITE);

        settingsIcons(s);
        title(s);
    }

    private static void settingsIcons(ArtSheet s) {
        Color w = Color.WHITE;
        Painter g = s.alloc("icon_gear", 128, 128);
        for (float a = 0; a < MathUtils.PI2; a += 0.03f) g.disc(64 + MathUtils.cos(a) * 30, 64 + MathUtils.sin(a) * 30, 8f, w, 1f);
        for (int i = 0; i < 8; i++) { float a = i * MathUtils.PI2 / 8; g.disc(64 + MathUtils.cos(a) * 42, 64 + MathUtils.sin(a) * 42, 12f, w, 1f); }
        Painter b = s.alloc("icon_back", 128, 128);
        b.stroke(100, 64, 30, 64, 16, w, 1f); b.stroke(30, 64, 64, 30, 16, w, 1f); b.stroke(30, 64, 64, 98, 16, w, 1f);
        for (int off = 0; off < 2; off++) {
            Painter m = s.alloc(off == 0 ? "icon_music" : "icon_music_off", 128, 128);
            m.stroke(78, 24, 78, 88, 11, w, 1f); m.ellipse(60, 94, 22, 16, w); m.stroke(78, 24, 106, 44, 11, w, 1f);
            if (off == 1) m.stroke(22, 22, 106, 106, 11, w, 1f);
            Painter f = s.alloc(off == 0 ? "icon_fx" : "icon_fx_off", 128, 128);
            f.tri(64, 10, 76, 52, 52, 52, w, 1f); f.tri(64, 118, 76, 76, 52, 76, w, 1f);
            f.tri(10, 64, 52, 76, 52, 52, w, 1f); f.tri(118, 64, 76, 76, 76, 52, w, 1f);
            f.disc(64, 64, 20, w, 1f);
            if (off == 1) f.stroke(22, 22, 106, 106, 11, w, 1f);
        }
        Painter r = s.alloc("icon_reset", 128, 128);
        for (float a = 0.5f; a < MathUtils.PI2 - 0.2f; a += 0.04f) r.disc(64 + MathUtils.cos(a) * 36, 64 + MathUtils.sin(a) * 36, 8f, w, 1f);
        r.tri(112, 54, 85, 60, 103, 78, w, 1f);
    }

    /** "LUMI" als dicke, runde Strichbuchstaben – kein Font nötig, passt zum Stil. */
    private static void title(ArtSheet s) {
        Painter p = s.alloc("title", 700, 230);
        Color outline = Painter.rgb(0xB5521C), body = Painter.rgb(0xFFC24D), hi = Painter.rgb(0xFFEFB0);
        float[][][] letters = {
            // L
            { { 0, 0, 0, 1 }, { 0, 1, 0.85f, 1 } },
            // U (Bogen unten)
            { { 0, 0, 0, 0.62f }, { 0.85f, 0, 0.85f, 0.62f }, { -1, 0.62f, 0.85f, 0.62f } },
            // M
            { { 0, 1, 0, 0 }, { 0, 0, 0.5f, 0.62f }, { 0.5f, 0.62f, 1f, 0 }, { 1f, 0, 1f, 1 } },
            // I
            { { 0, 0, 0, 1 } },
        };
        float[] xs = { 20, 190, 360, 590 };
        float[] ws = { 130, 130, 170, 10 };
        float top = 30, hgt = 160;
        for (int pass = 0; pass < 3; pass++) {
            float th = pass == 0 ? 58 : pass == 1 ? 42 : 12;
            Color c = pass == 0 ? outline : pass == 1 ? body : hi;
            float off = pass == 2 ? -9 : 0;
            for (int li = 0; li < letters.length; li++) {
                for (float[] seg : letters[li]) {
                    if (seg[0] < 0) { // Bogen für U
                        float cx = xs[li] + ws[li] * 0.5f, r = ws[li] * 0.5f;
                        for (float a = 0; a <= MathUtils.PI; a += 0.04f)
                            p.disc(cx + MathUtils.cos(a) * r + (pass == 2 ? -4 : 0), top + hgt * 0.62f + MathUtils.sin(a) * (hgt * 0.38f) + off,
                                    th * 0.5f, c, 1f);
                        continue;
                    }
                    p.stroke(xs[li] + seg[0] * ws[li] + (pass == 2 ? -6 : 0), top + seg[1] * hgt + off,
                            xs[li] + seg[2] * ws[li] + (pass == 2 ? -6 : 0), top + seg[3] * hgt + off, th, c, 1f);
                }
            }
        }
        // Funkelstern über dem I
        for (int i = 0; i < 4; i++) {
            float a = i * MathUtils.HALF_PI;
            p.stroke(620, 14, 620 + MathUtils.cos(a) * 26, 14 + MathUtils.sin(a) * 26, 6, Painter.rgb(0xFFF8D0), 1f);
        }
        p.disc(620, 14, 7, Color.WHITE, 1f);
    }
}
