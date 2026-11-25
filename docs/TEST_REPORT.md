# Testbericht (Meilenstein 1)

Alle Angaben stammen aus tatsächlich ausgeführten Läufen in dieser Entwicklungsumgebung (Linux-Container, kein GPU, Software-Rendering).

## Builds

| Test | Ergebnis |
|---|---|
| `./gradlew :core:compileJava` | ✔ |
| `./gradlew :html:dist` (GWT-Compile, ca. 25 s Compile + Link) | ✔ |
| `./gradlew :lwjgl3:build` | ✔ |
| Desktop-Start unter Xvfb (`:lwjgl3:run`) | ✔ Spiel initialisiert („Grafik erzeugt in 360 ms“, „Start abgeschlossen“); nur ALSA-Warnungen (keine Soundkarte) |

## Browser (headless Chromium, Playwright, SwiftShader-WebGL)

| Test | Ergebnis |
|---|---|
| Seite lädt, libGDX-Preloader, Menü erscheint | ✔ (nach Behebung: fehlende `assets.txt`/falsches Format) |
| Start per Maus-Klick, per Enter | ✔ |
| Start per Touch-Tap (emuliertes Tablet, 1180×820, CDP-Touch) | ✔ (nach Umstellung auf ereignisbasierte Eingabe `PointerTracker`) |
| Bewegung WASD, Kamera folgt, Rückansicht, Sprung | ✔ (Screenshots geprüft) |
| Schwebender Joystick + Rennen, Sprungtaste (Touch) | ✔ (Screenshots geprüft) |
| Pause per Esc, Pausenmenü sichtbar | ✔ |
| Speichern: `localStorage` enthält Position nach Pause; nach `reload` + Play steht Lumi am gespeicherten Ort | ✔ |
| Verdeckungs-Transparenz hinter Bäumen | ✔ sichtbar |
| Browser-Konsole | Keine Fehler des Spiels. Einziger Eintrag: „Permissions policy violation: unload“ – kommt vom libGDX-Backend (Unload-Handler) in der Playwright-Umgebung, harmlos. |

## Gefundene und behobene Fehler

1. GWT: `PreloaderBundle`-Fehler → Konfiguration `gdx.assetpath` ergänzt.
2. Preloader hängt bei 0 % → `assets.txt` im 6-Felder-Format erzeugen.
3. Dunkler Rand rechts/unten → `padHorizontal/padVertical = 0`.
4. Klicks kamen nicht an → eigener `#embed-html`-Container lag über dem Canvas.
5. Antippen verpasst (Polling) → ereignisbasierte Zeigerverarbeitung.
6. Dunkle „Halbmonde“ im Gras → Stammfuß ragte aus der Sprite-Zelle in Nachbarzellen.
7. Vignette zeigte Streifen → pro Spalte/Zeile direkte Alpha-Werte statt Aufsummieren.
8. Lumi sah wie ein Hase aus (lange Ohren) → Fuchsohren, größere Figur, Schwanz neu platziert.

## Nicht getestet / offene Punkte (ehrlich)

* **Performance/FPS auf echter Hardware (Laptop, Tablet) nicht gemessen.** Software-Rendering ist dafür nicht aussagekräftig. Start-Erzeugung der Grafik: ~170 ms im Browser (Chromium headless).
* Kein Test auf echtem Touchgerät, in Safari/Firefox, im Hochformat oder mit Fenstergrößenänderung zur Laufzeit (Viewport ist `ExtendViewport`, Größenänderung sollte funktionieren, ist aber nicht geprüft).
* Kollisionen wurden nur indirekt geprüft (Lumi bleibt vor Büschen stehen); keine automatisierten Tests.
* Es gibt noch keine automatisierten Unit-Tests; Phase 5 soll Logik (Rätsel, Speichern) testen.
* Audio, Tiere, Kristalle, Rätsel: nicht implementiert (siehe GAME_DESIGN).
