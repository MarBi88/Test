# Technische Architektur

## Technologieentscheidung (Phase 0)

| Frage | Ergebnis |
|---|---|
| Umgebung | JDK 21, Gradle 8.14.3, Node 22, Chromium (Playwright) vorhanden; Maven Central erreichbar (gelegentlich HTTP 429 → Wiederholung hilft). |
| Version | libGDX **1.14.2** (aktuellste stabile Version auf Maven Central zum Zeitpunkt des Baus). |
| HTML5-Backend | **Offizielles libGDX-GWT-Backend** (`gdx-backend-gwt:1.14.2`, gebaut gegen GWT 2.11.0). |
| Alternative gdx-teavm | Geprüft (Maven: `backend-teavm` 1.4.0 vorhanden), aber **nicht verwendet**: Das GWT-Backend deckt alle benötigten Funktionen ab, und der Build-Prototyp lief auf Anhieb. Keine Vermischung beider Backends. |
| Begründung | Der GWT-Build wurde tatsächlich ausgeführt und im Browser getestet (siehe TEST_REPORT). Ein Wechsel wäre nur bei GWT-Inkompatibilitäten sinnvoll. |

## Build-Besonderheiten

* Kein Gradle-GWT-Plugin: Die Tasks `:html:gwtCompile` (JavaExec auf `com.google.gwt.dev.Compiler`) und `:html:dist` sind in `html/build.gradle` selbst definiert. Weniger Abhängigkeiten, volle Kontrolle.
* Der Java-Quellcode von `core` wird als GWT-Quelle über `Lumi.gwt.xml` bereitgestellt (`core/build.gradle` fügt `src/main/java` als Ressource hinzu).
* Der libGDX-Preloader braucht `assets/assets.txt` im Format `Typ:Pfad:URL-Pfad:Größe:MIME:Preload`; `dist` erzeugt sie aus `core/assets/`.
* Das Backend hängt das Canvas an `<body>` an. Keine eigenen Container-Elemente mit `position: fixed` darüberlegen (sonst gehen Klicks verloren – hier gefunden und behoben).
* GWT-Einschränkungen im Code beachtet: kein `String.format`, keine Reflection, kein `java.nio.file`, keine Pixmap-Blending-Mode `None` (im Canvas-Backend löscht `copy` die ganze Fläche).

## Schichten

```
LumiGame ──┬─ screens/   MenuScreen, GameScreen          (Ablauf)
           ├─ world/     GameWorld, WorldMap, WorldCamera, WorldRenderer, Iso   (Zustand + Darstellung der Welt)
           ├─ entities/  Entity, Player, Decor           (reine Daten)
           ├─ characters/LumiCharacter                   (Animation + Darstellung der Figur)
           ├─ systems/   MovementSystem, ParticleSystem  (Logik)
           ├─ input/     PointerTracker, DesktopInput, TouchInput, InputManager
           ├─ ui/        UiButton, UiLayer, MainMenu, PauseMenu, GameHUD, VirtualJoystick
           ├─ art/       ArtSheet, Painter, ArtFactory, LumiArt, UiArt   (Grafikerzeugung)
           ├─ audio/     AudioManager (Phase 4)
           ├─ save/      SaveManager (Preferences → LocalStorage)
           └─ config/    GameConfig
```

Trennung: Spiellogik (`systems`, `entities`) kennt kein Rendering; `WorldRenderer` liest nur Zustand; Eingabe wird in `InputManager` zu einem plattformneutralen Zustand zusammengeführt.

## Rendering (2.5D)

* Isometrische 2:1-Projektion (`Iso`): `sx = (x − y)·64`, `sy = −(x + y)·32`; Tiefe = `x + y`.
* Reihenfolge: Boden-Kacheln → Bodenflecken (Weg, Moos, Ufer) → Wasser → Schatten → **tiefensortierte Objekte inkl. Lumi** (Decor-Liste ist einmal vorsortiert, Lumi wird per Tiefe eingefügt – kein Sortieren pro Frame) → additive Leuchteffekte → Lichtstrahlen → Blätter → Vignette (UI-Ebene).
* Verdeckung: Hohe Objekte (Bäume) vor Lumi werden weich transparent (`Decor.alpha`), damit Lumi nie verschwindet.
* Wind: Zwei-Streifen-Scherung der Sprites (Stamm starr, Krone wiegt sich) über selbst gebaute Vertices (`float[40]`, wiederverwendet).
* Culling: Bildschirm-Rechtecke pro Objekt/Kachel; Kamera per `ExtendViewport(1280×720)`.
* Keine Allokationen im Render-Loop (Vertex-Puffer, Vektoren und Farben wiederverwendet).

## Grafikpipeline (prozedural)

Alle Sprites werden beim Start in **einen** 2048²-Atlas gezeichnet (`ArtSheet`) → ein Texturwechsel-freier SpriteBatch. Siehe `docs/ASSET_GUIDE.md`.

## Speichern

`SaveManager` nutzt `Gdx.app.getPreferences`, im Browser ist das `localStorage` (Schlüssel `lumi_zauberwald_v1:*`). Alle Zugriffe sind in try/catch; ohne Speicher läuft das Spiel normal weiter. Gespeichert wird Position (alle ≤4 s, bei Pause, beim Verlassen) und Toneinstellung.

## Bekannte Risiken

| Risiko | Stand |
|---|---|
| Browser-Autoplay blockiert Audio | Phase 4: Audio erst nach erster Interaktion starten (libGDX-GWT entsperrt AudioContext bei erster Eingabe, im Log bestätigt). |
| Echte Audiodateien/Synthese im GWT-Backend | Offen (Phase 4). libGDX-GWT unterstützt keine Echtzeit-Synthese über `AudioDevice`; Plan: kleine, selbst erzeugte OGG/WAV-Dateien oder Web-Audio per JSNI. |
| Performance auf Tablets | Nicht auf echter Hardware gemessen. Gegenmaßnahme vorhanden: `ParticleSystem.quality` (dekorative Effekte zuerst reduzieren). |
| Prozedurale Grafik erreicht nicht Studio-Qualität gemalter Assets | Austauschbare Struktur vorhanden (ASSET_GUIDE). |
