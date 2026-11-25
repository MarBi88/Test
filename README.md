# LUMI – Das Geheimnis der Trauminsel

Ein liebevoll gestaltetes 2.5D-Browsergame für ein fünfjähriges Kind. Lumi, ein kleiner orangefarbener Fuchs,
erkundet einen magischen Wald. Java + libGDX, im Browser lauffähig (kompiliert nach JavaScript).

**Stand: Phase 0–5 umgesetzt – spielbar im Browser (Welt mit Wasserfall/Höhle/Bach, Lumi, 15 Kristalle, 3 Tiere, 2 Rätsel, Ton, Einstellungen, automatisierte Tests). Offen: Leistungsmessung auf echter Hardware, Karte, Hörprüfung des Tons.** Details in [docs/](docs/).

## Starten

Voraussetzungen: JDK 17+ (getestet mit 21), Internet für den ersten Gradle-Abruf. Gradle-Wrapper liegt bei.

```bash
# Browser-Version bauen (erzeugt html/build/dist)
./gradlew :html:dist

# lokal ausliefern und im Browser öffnen: http://localhost:8080
cd html/build/dist && python3 -m http.server 8080

# Automatisierte Tests (headless, 16 Tests: Rätsel, Tor, Kollision, Speicherstand, Determinismus)
./gradlew :core:test

# Desktop-Testversion (LWJGL3)
./gradlew :lwjgl3:run
```

Der Ordner `html/build/dist` ist ein komplett statisches Verzeichnis und kann auf jedem Webserver liegen.
Tipp: Der GWT-Build dauert ca. 30–60 s; `-PgwtPretty` erzeugt lesbares JavaScript zum Debuggen.

## Steuerung

| | Desktop | Tablet |
|---|---|---|
| Bewegen | WASD / Pfeiltasten | Wischen: Finger irgendwo aufsetzen und in die gewünschte Richtung ziehen (kein Joystick) |
| Rennen | Shift | weit wischen |
| Springen | Leertaste | große Taste unten rechts |
| Interagieren (Tiere) | E | Pfoten-Taste (erscheint nur in Reichweite) |
| Rätsel (Blumen, Steine) | Berühren, kein Knopf nötig | Berühren |
| Pause | Esc / P | Knopf oben rechts |
| Spiel starten | Klick auf Play / Enter | Tippen |

Ton: Musik, Vogelgezwitscher, Wind, Teich-Plätschern, Schritte (Gras/Weg), Sammel-, Tier- und Rätselklänge. Zahnrad im Start-/Pausenmenü: Musik an/aus, Effekte an/aus, Fortschritt zurücksetzen (Knopf 2 s halten). Die Klänge entstehen per `python3 tools/make_audio.py` (numpy + ffmpeg) in `core/assets/audio/`.

Entwickler-Hilfen im Spiel: `F5` zum nächsten Rätselteil teleportieren, `F7` zur nächsten Beute, `F6` Übersichts-Zoom, `F9` Atlas anzeigen, `F1`–`F4` Ebenen ausblenden (Kacheln, Bodenflecken, Objekte, Schatten), `F8` Partikel an/aus.

## Projektstruktur

```
core/    plattformunabhängiger Spielcode (game.lumi.*)
lwjgl3/  Desktop-Launcher (nur zum Testen)
html/    Browser-Launcher, GWT-Modul, webapp/index.html, Gradle-Tasks gwtCompile/dist
docs/    Design, Architektur, Asset-Leitfaden, Testbericht
```
