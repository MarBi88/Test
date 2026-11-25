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

## Phase 4: Audio und Einstellungen

| Test | Ergebnis |
|---|---|
| 28 MP3-Dateien erzeugt (Pegel per `volumedetect`: Spitzen ≤ −1,9 dB, Musik Ø −16,7 dB) | ✔ |
| Browser lädt alle Audiodateien (HTTP 200), kein Dekodierfehler, `decodeAudioData` funktioniert in Chromium (Stichprobe) | ✔ |
| Nach erstem Klick: Musik-, Wind- und Wasser-Schleife starten (Log „Audio gestartet“, gültige Sound-IDs) | ✔ |
| Einstellungsmenü (Musik/Effekte umschalten, Symbolwechsel), Pausen-Overlay | ✔ Screenshots |
| Zurücksetzen per 2-s-Halten | Code vorhanden, **nicht automatisiert getestet** |
| **Tatsächliche Hörbarkeit/Klangqualität** | **nicht geprüft** – die Testumgebung hat keine Audioausgabe und ich kann nicht hören. Lautstärken sind nur rechnerisch gewählt (Musik 0,34, Wind 0,55, Effekte 0,3–0,8). |

## Phase 5: Automatisierte Tests (`./gradlew :core:test`, 16 Tests, alle bestanden)

Headless-libGDX mit gemocktem GL; die **echte** Welt wird gebaut (Grafik, Karte, Tiere, Rätsel).

| Bereich | Geprüft |
|---|---|
| Weltgenerierung | genau 15 Kristalle, eindeutige Positionen, alle innerhalb der Karte; **Determinismus** (zwei Welten identisch) |
| Erreichbarkeit | Mit offenem Tor sind alle Kristalle, Tiere und Rätselteile erreichbar (Flutfüllung mit Lumis Radius); mit geschlossenem Tor ist die Lichtung samt ihrer 3 Kristalle unerreichbar, alle anderen Kristalle bleiben erreichbar |
| Blumenrätsel | richtige Reihenfolge löst und enthüllt Kristall; Fehlversuch dimmt alle Blumen ohne Strafe und lässt sofort neu versuchen; erneutes Berühren derselben Blume ist kein Fehler |
| Steinrätsel | Tor sperrt vorher, öffnet nach drei Steinen, Lumi gelangt hinein |
| Speicherstand | Flags (Tiere, Blumen, Steine) und Kristall-Bits laufen als Rundreise korrekt zurück; Tor nur bei 3 Steinen offen |
| Tiere | Kaninchen füttern (einmalig), Eichhörnchen graben → Kristall erscheint, Vogel fliegt zu verstecktem Kristall |
| Kollision | 4000 zufällige Schritte: Lumi ist nie in einem Hindernis, immer begehbar und kommt weit herum; gleitet an Objekten entlang |
| Sonstiges | Iso-Projektion (Rundreise), Joystick (Totzone, Begrenzung) |

**Mutationsprüfung** (um „Fake-Tests“ auszuschließen): Tor-Öffnen entfernt → `stonesOpenGateAndGateBlocksBefore` schlägt fehl; Hindernis-Herausschieben entfernt → `lumiNeverEndsInsideAnObstacle…` schlägt fehl. Danach wurde der Gleiten-Test verschärft, da er die zweite Mutation zunächst nicht bemerkte.

### Leistung
| Messung | Ergebnis |
|---|---|
| Canvas erscheint nach | ca. 0,5 s (lokal) |
| Bildrate in headless Chromium mit **Software-Rendering** (SwiftShader, 1280×720) | Ø 130 ms/Frame (≈ 8 FPS), p95 167 ms – **nicht repräsentativ für echte GPUs**, nur ein Worst-Case-Anhaltspunkt |
| Echte Hardware (Laptop, Tablet) | **nicht gemessen** |
| Gegenmaßnahme | `GameScreen.guardPerformance`: Bei dauerhaft > 28 ms pro Frame wird zuerst die Partikelmenge schrittweise reduziert (bis 15 %), bei gutem Lauf wieder erhöht |

## Thema-Umbau: Tropeninsel + Katta (Ringelschwanz-Maki)

| Test | Ergebnis |
|---|---|
| 16 automatisierte Tests nach dem Umbau (Erreichbarkeit, Rätsel, Tor, Kollision, Speicherstand) | ✔ alle bestanden |
| Browser-Start, Screenshots: Maki-Lumi, Strand/Sand/Meer mit Schaum und Glitzern, Palmen, Frosch/Krabbe-Blasen, Übersicht der Insel | ✔ geprüft |
| Neue Klänge (Tropen-Musik, Meeresrauschen nach Küstennähe, Frosch) | erzeugt und Pegel plausibel, **nicht abgehört** |
| Speicherstand-Schlüssel auf `lumi_trauminsel_v1` umgestellt (alter Waldstand wird nicht übernommen) | ✔ |

## Wisch-Steuerung und Beutetiere

| Test | Ergebnis |
|---|---|
| 23 automatisierte Tests (neu: Beutetiere vorhanden/auf der Insel, Fressen + Zähler, Respawn, Ausweichen langsamer als Lumi, kein Verlassen der Insel in 3000 Schritten, Zählerstand-Rundreise, Wisch-Anker folgt dem Finger) | ✔ alle bestanden |
| Emuliertes Tablet: Wischen auf der **rechten** Bildschirmhälfte steuert Lumi, Richtungswechsel ohne Absetzen, nur dezenter Finger-Ring, kein Joystick | ✔ Screenshots |
| Fressen im Browser (Dev-Taste F7 teleportiert zur Beute): Zähler steigt, `eaten` im LocalStorage (`0,0,1`) | ✔ |
| Kauen-Animation, Mampf-Klang | Animation per Screenshot-Stichprobe; **Klang nicht abgehört** |

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
10. Felswand-Grafik wurde am Zellrand abgeschnitten (Brocken zu breit) → schmaler.
11. Lumi sah wie ein Hase aus (lange Ohren) → Fuchsohren, größere Figur, Schwanz neu platziert.

## Nicht getestet / offene Punkte (ehrlich)

* **Performance/FPS auf echter Hardware (Laptop, Tablet) nicht gemessen.** Software-Rendering ist dafür nicht aussagekräftig. Start-Erzeugung der Grafik: ~170 ms im Browser (Chromium headless).
* Kein Test auf echtem Touchgerät, in Safari/Firefox, im Hochformat oder mit Fenstergrößenänderung zur Laufzeit (Viewport ist `ExtendViewport`, Größenänderung sollte funktionieren, ist aber nicht geprüft).
* Rendering (Optik, Verdeckung, Animationen) wird nur per Screenshot geprüft, nicht automatisiert.
* Audio: nicht implementiert. Die headless-Tests laufen mit niedriger Bildrate (Software-Rendering), Spielzeit läuft dann langsamer; Zeitabläufe (Buddeln, Hinweise) wurden entsprechend lange abgewartet.
* Karte/Minimap und Einstellungsmenü fehlen.
