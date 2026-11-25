package game.lumi.audio;

/**
 * Platzhalter-Schnittstelle für Phase 4 (Musik, Ambiente, SFX). Aktuell wird nur der
 * Ein/Aus-Zustand verwaltet; es werden noch keine Klänge erzeugt.
 */
public final class AudioManager {
    private boolean enabled = true;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean on) { enabled = on; }
}
