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

## Phase 3, Teil 1: Lichtkristalle

| Test | Ergebnis |
|---|---|
| 15 Kristalle werden erzeugt (Log mit Positionen) | ✔ |
| Berühren sammelt ein: Perle in der Anzeige füllt sich, Lumi zeigt Freude (Screenshot) | ✔ |
| Speicherstand `crystals` im LocalStorage (z. B. `100000000000000`) | ✔ |
| Erreichbarkeit: Flutfüllung beim Platzieren; Test mit einem versteckten Kristall | ✔ (nur stichprobenartig, nicht alle 15 einzeln abgelaufen) |
| Wiederladen mit Kristall-Zustand in der Anzeige | nicht separat geprüft |

## Phase 3, Teil 2: Tiere und Rätsel

| Test | Ergebnis |
|---|---|
| Kaninchen füttern (E): Flag im LocalStorage `10…`, Herzchen-Effekt, Screenshot | ✔ |
| Eichhörnchen graben: Flag gesetzt, Geschenk-Kristall erscheint und wird eingesammelt (Perle in der Anzeige gefüllt) | ✔ |
| Vogel fliegt zu verstecktem Kristall, Noten-Blase, Kristall eingesammelt | ✔ |
| Blumenrätsel (Teleport zur jeweils richtigen Blume per Dev-Taste F5), Lösung gespeichert, Belohnungskristall | ✔ (richtige Reihenfolge; **Fehlversuch-Reset nicht automatisiert getestet**) |
| Steinrätsel: 3 Steine aktiviert → Flags `001111` | ✔ |
| Tor sperrt bei geschlossenem Zustand (Lumi bleibt 4,2 Kacheln vor der Lichtungsmitte), bei offenem Zustand läuft Lumi hinein (2,7) | ✔ |
| Hinweis nach Inaktivität (Lumi blickt zum Hinweisstein, Signal leuchtet) | ✔ Screenshot |
| Weltgenerierung deterministisch (zwei Ladevorgänge liefern identische Kristall-/Tierpositionen) | ✔ |

## Gefundene und behobene Fehler

1. GWT: `PreloaderBundle`-Fehler → Konfiguration `gdx.assetpath` ergänzt.
2. Preloader hängt bei 0 % → `assets.txt` im 6-Felder-Format erzeugen.
3. Dunkler Rand rechts/unten → `padHorizontal/padVertical = 0`.
4. Klicks kamen nicht an → eigener `#embed-html`-Container lag über dem Canvas.
5. Antippen verpasst (Polling) → ereignisbasierte Zeigerverarbeitung.
6. Dunkle „Halbmonde“ im Gras → Stammfuß ragte aus der Sprite-Zelle in Nachbarzellen.
7. Vignette zeigte Streifen → pro Spalte/Zeile direkte Alpha-Werte statt Aufsummieren.
8. **Nichtdeterministische Platzierung** der versteckten Kristalle (ungeseedetes `shuffle`): Speicherstände hätten nach dem Neuladen auf falsche Kristalle gezeigt → seeded.
9. Baumkronen verdeckten Lumi trotz Transparenz-Logik (Tiefenlimit zu klein) → Limit erhöht.
10. Lumi sah wie ein Hase aus (lange Ohren) → Fuchsohren, größere Figur, Schwanz neu platziert.

## Nicht getestet / offene Punkte (ehrlich)

* **Performance/FPS auf echter Hardware (Laptop, Tablet) nicht gemessen.** Software-Rendering ist dafür nicht aussagekräftig. Start-Erzeugung der Grafik: ~170 ms im Browser (Chromium headless).
* Kein Test auf echtem Touchgerät, in Safari/Firefox, im Hochformat oder mit Fenstergrößenänderung zur Laufzeit (Viewport ist `ExtendViewport`, Größenänderung sollte funktionieren, ist aber nicht geprüft).
* Kollisionen wurden nur indirekt geprüft (Lumi bleibt vor Büschen stehen); keine automatisierten Tests.
* Es gibt noch keine automatisierten Unit-Tests; Phase 5 soll Logik (Rätsel, Speichern) testen.
* Audio: nicht implementiert. Die headless-Tests laufen mit niedriger Bildrate (Software-Rendering), Spielzeit läuft dann langsamer; Zeitabläufe (Buddeln, Hinweise) wurden entsprechend lange abgewartet.
* Karte/Minimap und Einstellungsmenü fehlen.
