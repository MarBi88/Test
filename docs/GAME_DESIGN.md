# Game Design

**Zielgruppe:** fünfjähriges Kind. Keine Gewalt, kein Zeitdruck, keine Bestrafung, keine Werbung, keine Käufe, keine externen Links, kein Lesen nötig (Symbole statt Text).

**Setting:** Eine tropische Trauminsel im türkisen Meer: Sandstrand mit Brandung, Palmen, Bananenstauden, Flammenbäume, Lagune, Bach, Wasserfall mit Höhle, Muscheln und Seesterne am Strand.

**Story:** Die Trauminsel hat ihre Leuchtkraft verloren, Lichtkristalle sind verschwunden. Lumi erkundet die Insel, findet sie wieder und hilft den Inselbewohnern. Erzählt wird nur über kleine visuelle Ereignisse.

**Lumi:** ein Katta (Ringelschwanz-Maki): graues Fell, weißes Gesicht und Bauch, schwarze Augenmasken, große bernsteinfarbene Augen, schwarze Nase, runde Ohren, langer schwarz-weiß geringelter Schwanz, leuchtender türkiser Anhänger.

**Inselbewohner:** Baumfrosch (hüpft, mit Mango zu füttern), Krabbe (huscht seitlich, buddelt auf Wunsch und enthüllt ihren Kristall), Papagei (fliegt zum versteckten Kristall und ruft). Im Code heißen die Typen aus Altgründen weiter RABBIT/SQUIRREL/BIRD.

**Weltgröße:** 96×96 Kacheln, bewusst licht bewachsen: Palmen stehen vereinzelt, viel offene Tropenwiese mit Blumen, sandigen Wegen, Lagune, Bach. Das Meer umgibt die Insel; ins Wasser kann man nicht laufen.

## Umsetzungsstand

| Bereich | Stand |
|---|---|
| Startmenü (Titel, Play, Ton) | ✔ |
| Zusammenhängendes Waldgebiet (Lichtung, Wege, Teich, Büsche, Blumenwiesen, Pilze, Steine, Baumstämme) | ✔ erste Fassung; Bach (flach, begehbar, Spritzer), Felswand mit **Höhle und Wasserfall** (Kristall hinter dem Vorhang, nur durch das Wasser erreichbar), magischer Platz (geheime Lichtung) ✔ |
| Lumi: Idle, Idle-Special (Umschauen, Blinzeln, Ohrzucken), Walk, Run, Jump, Land, Sleep (nach 18 s) | ✔ |
| Lumi: Happy (beim Einsammeln), Curious (Kristall in der Nähe) | ✔ |
| Lumi: Interact | Animation noch **nicht ausgelöst** (Tiere/Rätsel) |
| Steuerung Desktop + Tablet, Pause, Speichern der Position | ✔ |
| Animierte Vegetation, Wasser, Partikel (Glühwürmchen, Staub, Blätter), Lichtstrahlen, Vignette | ✔ |
| 15 Lichtkristalle (9 offen, 6 hinter Bäumen versteckt, per Flutfüllung als erreichbar geprüft), Sammeln mit Funkeln + Freude-Animation + Neugier in der Nähe, Sammelanzeige (Perlen-Leiste), Speicherung | ✔ |
| 3 Tiere mit eigenem Verhalten, Reaktion auf Lumi und Interaktion (Taste E / Pfoten-Taste, Sprechblasen-Symbole statt Text) | ✔ |
| 2 Rätsel (Blumenfolge, magische Steine + Ranken-Tor) mit Hinweisen bei Inaktivität | ✔ |
| Musik (45-s-Loop, Spieluhr/Marimba, C-Dur-Pentatonik), Ambiente (Wind, Teich-Plätschern nach Abstand, zufälliges Vogelgezwitscher), Schritte (Gras/Weg), Sprung/Landung, Kristall, Tierlaute, Blumentöne (C/E/G), sanfter Fehlklang, Steine/Tor, Klick; leiser im Pausenmenü | ✔ (nur gemessen/geprüft, nicht abgehört – siehe TEST_REPORT) |
| Einstellungen (Musik, Effekte, Fortschritt zurücksetzen per langem Drücken) | ✔ |

## Gameplay (umgesetzt, Phase 3)

* **Lichtkristalle** (≥15): pulsierend, teils hinter Bäumen/Wasserfall versteckt; Einsammeln = Sammelanimation + Funkeln + Zähler; Zustand wird gespeichert.
* **Kaninchen** (füttern), **Eichhörnchen** (hat etwas versteckt), **Vogel** (zeigt versteckten Ort) – je mit eigenem Verhalten und Reaktion auf Lumi.
* **Tiere:** Das *Kaninchen* hoppelt in Heimatnähe, schaut zu Lumi und weicht höflich aus, wenn Lumi herangerannt kommt; mit der Karotten-Blase kann man es füttern (Herzchen, danach Freudenhüpfer). Das *Eichhörnchen* huscht kurz umher; mit der Eichel-Blase buddelt es – dabei erscheint sein Geschenk, ein bis dahin unsichtbarer Kristall. Der *Vogel* fliegt, sobald Lumi nah ist, zu einem noch nicht gefundenen versteckten Kristall, landet daneben und zwitschert (Noten-Blase); Antippen ruft ihn erneut.
* **Kristalle (15):** 6 offen, 4 hinter Bäumen versteckt (davon 1 Eichhörnchen-Geschenk), 1 hinter dem Wasserfall in der Höhle, 1 Belohnung des Blumenrätsels, 3 in der geheimen Lichtung.
* **Hinweise:** Nach ca. 11 s Stillstand schaut Lumi zum nächsten ungelösten Rätselteil, dort leuchtet ein sanftes Signal.
* **Rätsel 1:** drei leuchtende Blumen in der Farbreihenfolge rosa → gelb → blau berühren; die Reihenfolge zeigt der Hinweisstein (drei Farbperlen). Falsche Blume: sie wackelt, alles dimmt sanft, man beginnt neu – ohne Strafe. Lösung: Blumen blühen auf, ein Kristall erscheint.
* **Rätsel 2:** drei magische Steine im Wald durch Berühren aktivieren. Drei Orbs über dem Ranken-Tor zeigen den Fortschritt. Sind alle an, lösen sich die Ranken, und der Weg zur geheimen Lichtung (magischer Platz, 3 Kristalle) ist frei. Das Tor sperrt vorher wirklich (per Test geprüft).
