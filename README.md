# LUMI – Das Geheimnis des Zauberwaldes

Ein liebevoll gestaltetes 2.5D-Browsergame für ein fünfjähriges Kind. Lumi, ein kleiner orangefarbener Fuchs,
erkundet einen magischen Wald. Java + libGDX, im Browser lauffähig (kompiliert nach JavaScript).

**Stand: Meilenstein 1 (Phase 0 + 1 + Teile von Phase 2) – spielbar im Browser.** Details in [docs/](docs/).

## Starten

Voraussetzungen: JDK 17+ (getestet mit 21), Internet für den ersten Gradle-Abruf. Gradle-Wrapper liegt bei.

```bash
# Browser-Version bauen (erzeugt html/build/dist)
./gradlew :html:dist

# lokal ausliefern und im Browser öffnen: http://localhost:8080
cd html/build/dist && python3 -m http.server 8080

# Desktop-Testversion (LWJGL3)
./gradlew :lwjgl3:run
```

Der Ordner `html/build/dist` ist ein komplett statisches Verzeichnis und kann auf jedem Webserver liegen.
Tipp: Der GWT-Build dauert ca. 30–60 s; `-PgwtPretty` erzeugt lesbares JavaScript zum Debuggen.

## Steuerung

| | Desktop | Tablet |
|---|---|---|
| Bewegen | WASD / Pfeiltasten | Finger links aufsetzen und ziehen (schwebender Joystick) |
| Rennen | Shift | Joystick ganz ausschlagen |
| Springen | Leertaste | große Taste unten rechts |
| Pause | Esc / P | Knopf oben rechts |
| Spiel starten | Klick auf Play / Enter | Tippen |

Entwickler-Hilfen im Spiel: `F9` Atlas anzeigen, `F1`–`F4` Ebenen ausblenden (Kacheln, Bodenflecken, Objekte, Schatten), `F8` Partikel an/aus.

## Projektstruktur

```
core/    plattformunabhängiger Spielcode (game.lumi.*)
lwjgl3/  Desktop-Launcher (nur zum Testen)
html/    Browser-Launcher, GWT-Modul, webapp/index.html, Gradle-Tasks gwtCompile/dist
docs/    Design, Architektur, Asset-Leitfaden, Testbericht
```
