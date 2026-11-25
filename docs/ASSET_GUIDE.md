# Asset-Leitfaden

## Aktueller Ansatz: eigene, prozedural erzeugte Grafik

Es werden **keine** Fremdassets, Downloads oder generierten Bilddateien verwendet; damit gibt es keine Lizenzfragen.
Alle Grafiken entstehen beim Start per Code (`core/.../art`) in einem Atlas:

* `Painter` – Zeichenwerkzeug (Kugel-Shading mit Licht oben links, kühler Schatten, warmes Rücklicht, weiche Ellipsen, dicke runde Linien).
* `ArtSheet` – Shelf-Packer für den 2048²-Atlas; Zugriff nur über `get(name)` / `family(prefix)`.
* `ArtFactory` – Boden, Bäume, Büsche, Blumen, Gras, Pilze, Steine, Baumstamm, Blatt; Palette.
* `LumiArt` – Lumis Körperteile (einzeln animierbar).
* `UiArt` – Symbole, Knöpfe, Titel „LUMI“ (Strichbuchstaben, kein Font nötig).

Diagnose: im Spiel `F9` zeigt den ganzen Atlas.

## Asset-Plan (Namen im Atlas)

| Gruppe | Namen | Status |
|---|---|---|
| Boden | `tile0..3`, `blob`, `shadow` | fertig |
| Bäume | `tree0` Eiche, `tree1` Blütenbaum, `tree2` Fantasie-Tanne, `tree3` alte Eiche | fertig |
| Pflanzen | `bush0..2`, `flower0..7`, `tuft0..2`, `mushroom0..1` | fertig |
| Objekte | `rock0..2`, `log0` | fertig |
| Lumi | `lumi_body`, `_bodyBack`, `_head`, `_headBack`, `_muzzle`, `_eye`, `_eyeHappy`, `_blush`, `_ear`, `_earBack`, `_tail`, `_foot`, `_pendant` | fertig |
| UI | `ui_circle/ring/pill/knob`, `icon_play/pause/sound_on/sound_off/home/jump/paw/crystal`, `title` | fertig |
| Effekte | `glow`, `ray`, `vignette`, `leaf`, `white` | fertig |
| Tiere | `animal_rabbit`, `animal_squirrel`, `animal_bird`, `animal_wing` (Animation per Transformation) | fertig |
| Sammelobjekte | `crystal`, Funkeln über Partikel | fertig |
| Rätsel | `flower_glow0..2`, `hintstone`, `stone_magic`, `vine` (Tor), Sprechblasen `ui_bubble`, `icon_carrot/acorn/note/heart` | fertig |
| Wasser | Bach, Wasserfall, Höhle | **offen (Phase 2)**; Teich ist fertig |

## Später gemalte Assets einsetzen

1. Eigene PNGs (bevorzugt als Atlas) nach `core/assets/` legen.
2. Einen `ArtFactory`-Ersatz schreiben, der denselben Namen → Region-Zuordnung liefert (über `AssetManager` oder `TextureAtlas`).
3. Konventionen beibehalten: Bäume/Büsche haben den Fußpunkt `ArtFactory.TREE_ANCHOR`/Decor-Anker über der Unterkante, Lumi-Teile sind in 2-facher Auflösung (`LumiArt.K`).
4. Lizenzen der Quellen in dieser Datei dokumentieren.

## Hinweise zum Zeichnen (Fallen)

* Nie über die Zellgrenze hinaus zeichnen (Nachbarzellen werden überschrieben – so entstanden anfangs dunkle Halbmonde unter Bäumen).
* Kein `Pixmap.Blending.None` (Canvas-Backend: löscht die gesamte Fläche).
* Vor dem Zeichnen alpha-weicher Formen bedenken, dass Canvas-Pixel vormultipliziert sind: sehr niedrige Alpha-Werte können leicht dunkel wirken.
