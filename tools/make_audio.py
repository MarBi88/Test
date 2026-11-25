#!/usr/bin/env python3
"""Erzeugt alle Klänge des Spiels selbst (keine Fremdaudios, keine Lizenzfragen).

Aufruf:  python3 tools/make_audio.py   → schreibt core/assets/audio/*.mp3  (benötigt numpy + ffmpeg)
Alle Zufallsanteile sind geseedet, das Ergebnis ist reproduzierbar.
"""
import os, subprocess, tempfile, wave
import numpy as np

SR = 32000
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'core', 'assets', 'audio')
os.makedirs(OUT, exist_ok=True)
rng = np.random.default_rng(7)

def t_(dur): return np.arange(int(dur * SR)) / SR
def env_ad(n, a=0.005, d=0.3, curve=4.0):
    t = np.arange(n) / SR
    e = np.exp(-t * curve / max(d, 1e-3)); att = np.minimum(1, t / max(a, 1e-4)); return e * att
def midi(m): return 440.0 * 2 ** ((m - 69) / 12)

def bell(freq, dur, amp=1.0, decay=None, bright=1.0):
    """Marimba/Spieluhr-artig: Grundton + inharmonische Obertöne, schneller Anschlag, weiches Ausklingen."""
    n = int(dur * SR); t = np.arange(n) / SR
    decay = decay or dur
    y = (np.sin(2 * np.pi * freq * t) * 1.0 + np.sin(2 * np.pi * freq * 2.76 * t) * 0.28 * bright * np.exp(-t * 14)
         + np.sin(2 * np.pi * freq * 5.4 * t) * 0.10 * bright * np.exp(-t * 30) + np.sin(2 * np.pi * freq * 4.0 * t) * 0.12 * np.exp(-t * 9))
    return y * env_ad(n, 0.003, decay, 5.0) * amp

def pad(freq, dur, amp=1.0):
    n = int(dur * SR); t = np.arange(n) / SR
    y = np.sin(2 * np.pi * freq * t) + 0.35 * np.sin(2 * np.pi * freq * 2 * t + 0.4) + 0.12 * np.sin(2 * np.pi * freq * 3 * t)
    a = np.minimum(1, t / 0.6) * np.minimum(1, (dur - t) / 0.8)
    return y * a * amp * (1 + 0.04 * np.sin(2 * np.pi * 4.5 * t))

def add(buf, sig, at):
    i = int(at * SR); j = min(len(buf), i + len(sig))
    if i < len(buf): buf[i:j] += sig[:j - i]

def lowpass(x, k):
    k = max(1, int(k)); c = np.cumsum(np.insert(x, 0, 0)); y = (c[k:] - c[:-k]) / k
    return np.concatenate([np.full(k - 1, y[0]), y])

def highpass(x, k): return x - lowpass(x, k)

def loopify(x, xf):
    """Nahtlos loopen: letzte xf Samples werden mit dem Anfang überblendet."""
    n = len(x) - xf; out = x[:n].copy(); fade = np.linspace(0, 1, xf)
    out[:xf] = out[:xf] * fade + x[n:] * (1 - fade)
    return out

def norm(x, peak=0.85):
    m = np.max(np.abs(x)) or 1.0
    y = x / m * peak
    return np.tanh(y * 1.2) / np.tanh(1.2)  # sanfter Limiter: keine harten Spitzen

def fadeio(x, a=0.004, b=0.01):
    x = x.copy(); na, nb = int(a * SR), int(b * SR)
    x[:na] *= np.linspace(0, 1, na); x[-nb:] *= np.linspace(1, 0, nb); return x

def save(name, x, br='56k'):
    x = np.clip(x, -1, 1)
    with tempfile.TemporaryDirectory() as d:
        w = os.path.join(d, 'a.wav')
        with wave.open(w, 'wb') as f:
            f.setnchannels(1); f.setsampwidth(2); f.setframerate(SR); f.writeframes((x * 32000).astype('<i2').tobytes())
        subprocess.run(['ffmpeg', '-y', '-loglevel', 'error', '-i', w, '-ac', '1', '-ar', str(SR), '-b:a', br, os.path.join(OUT, name + '.mp3')], check=True)
    print(f'{name:16s} {len(x) / SR:5.1f}s  {os.path.getsize(os.path.join(OUT, name + ".mp3")) // 1024:4d} KB')

# ------------------------------------------------------------------ Musik
def music():
    bpm = 84; beat = 60 / bpm; bars = 16; total = bars * 4 * beat
    buf = np.zeros(int((total + 3) * SR))
    chords = [(48, [60, 64, 67]), (45, [57, 60, 64]), (41, [57, 60, 65]), (43, [55, 59, 62])]  # C Am F G
    penta = [60, 62, 64, 67, 69, 72, 74, 76]
    r = np.random.default_rng(21)
    idx = 3
    for bar in range(bars):
        root, notes = chords[bar % 4]
        t0 = bar * 4 * beat
        for k, nn in enumerate(notes): add(buf, pad(midi(nn - 12 + 12), 4 * beat + 0.5, 0.10), t0)
        for b8 in range(8):  # weiche Arpeggio-Basslinie
            add(buf, bell(midi(root + [0, 7, 12, 7][b8 % 4]), beat, 0.16, beat * 1.2, 0.2), t0 + b8 * beat * 0.5)
        pos = 0.0
        pattern = [1, 0.5, 0.5, 1, 1] if bar % 2 == 0 else [1.5, 0.5, 1, 1]
        for dur in pattern:
            if pos >= 4: break
            if r.random() < 0.82:
                idx = int(np.clip(idx + r.choice([-2, -1, 1, 1, 2]), 0, len(penta) - 1))
                add(buf, bell(midi(penta[idx]), 1.4, 0.32, 1.1), t0 + pos * beat)
            pos += dur
    n = int(total * SR)
    tail = buf[n:]; buf = buf[:n]; buf[:len(tail)] += tail[:len(buf)]  # Ausklang über den Loop-Anfang legen
    save('music_loop', norm(buf, 0.55), '64k')

# ------------------------------------------------------------------ Ambiente
def wind():
    n = int(14 * SR) + int(2 * SR); t = np.arange(n) / SR
    x = lowpass(rng.standard_normal(n), 60) * 2.0 + lowpass(rng.standard_normal(n), 15) * 0.3
    mod = 0.55 + 0.30 * np.sin(2 * np.pi * t / 7.0) + 0.15 * np.sin(2 * np.pi * t / 3.5 + 1)
    save('wind_loop', norm(loopify(x * mod, int(2 * SR)), 0.35), '40k')

def water():
    n = int(8 * SR) + int(1 * SR)
    x = highpass(lowpass(rng.standard_normal(n), 5), 60) * 0.8
    for _ in range(46):  # Blubbern
        f = rng.uniform(500, 1400); d = rng.uniform(0.04, 0.09); tt = t_(d)
        blip = np.sin(2 * np.pi * (f * tt + f * 6 * tt ** 2)) * np.exp(-tt * 45) * rng.uniform(0.2, 0.5)
        add(x, blip, rng.uniform(0, 8))
    save('water_loop', norm(loopify(x, SR), 0.4), '48k')

def chirp(f0, f1, dur, amp=1.0, vib=0.0):
    t = t_(dur); f = f0 + (f1 - f0) * (t / dur)
    ph = 2 * np.pi * np.cumsum(f + vib * np.sin(2 * np.pi * 28 * t)) / SR
    return np.sin(ph) * np.sin(np.pi * np.clip(t / dur, 0, 1)) ** 0.7 * amp

def bird(seed, name, notes=3, base=3200, amp=0.8):
    r = np.random.default_rng(seed); buf = np.zeros(int(1.2 * SR)); at = 0.02
    for _ in range(notes):
        f0 = base * r.uniform(0.8, 1.15); f1 = f0 * r.choice([0.7, 1.3, 1.5])
        d = r.uniform(0.06, 0.14); add(buf, chirp(f0, f1, d, amp, vib=r.uniform(0, 250)), at); at += d + r.uniform(0.03, 0.08)
    save(name, norm(buf[:int(at * SR) + 400], 0.6) * 0.8, '40k')

# ------------------------------------------------------------------ Effekte
def sfx():
    for i in range(3):
        n = int(0.09 * SR); x = lowpass(rng.standard_normal(n), 6 + i) * env_ad(n, 0.004, 0.06, 5)
        save(f'step_grass{i}', norm(fadeio(x), 0.30), '32k')
        x = highpass(lowpass(rng.standard_normal(n), 3), 30) * env_ad(n, 0.002, 0.045, 5)
        save(f'step_path{i}', norm(fadeio(x), 0.32), '32k')
    t = t_(0.14); save('jump', norm(fadeio(np.sin(2 * np.pi * (300 * t + 1800 * t ** 2)) * np.sin(np.pi * t / 0.14)), 0.5), '32k')
    t = t_(0.14); thump = np.sin(2 * np.pi * (140 * t - 400 * t ** 2)) * np.exp(-t * 28) + lowpass(rng.standard_normal(len(t)), 8) * np.exp(-t * 40) * 0.5
    save('land', norm(fadeio(thump), 0.5), '32k')
    buf = np.zeros(int(1.3 * SR))
    for k, m in enumerate([84, 88, 91, 96]): add(buf, bell(midi(m), 0.9, 0.7, 0.8, 1.4), k * 0.07)
    add(buf, highpass(lowpass(rng.standard_normal(len(buf)), 3), 12) * env_ad(len(buf), 0.02, 0.4, 5) * 0.05, 0)
    save('collect', norm(fadeio(buf, 0.002, 0.05), 0.7), '48k')
    buf = np.zeros(int(0.5 * SR)); add(buf, chirp(520, 780, 0.12, 0.8), 0); add(buf, chirp(640, 1040, 0.16, 0.8), 0.14)
    save('lumi_happy', norm(fadeio(buf), 0.55), '40k')
    buf = np.zeros(int(0.3 * SR)); add(buf, chirp(900, 1500, 0.07, 0.9), 0); add(buf, chirp(1500, 1000, 0.1, 0.9), 0.07)
    save('rabbit', norm(fadeio(buf), 0.5), '40k')
    buf = np.zeros(int(0.7 * SR))
    for k in range(5): add(buf, chirp(1800 + 200 * (k % 2), 2600, 0.04, 0.8, vib=150) + highpass(lowpass(rng.standard_normal(int(0.04 * SR)), 2), 8) * 0.3, k * 0.1)
    save('squirrel', norm(fadeio(buf), 0.5), '40k')
    buf = np.zeros(int(0.9 * SR))
    for k in range(6): add(buf, lowpass(rng.standard_normal(int(0.07 * SR)), 3) * env_ad(int(0.07 * SR), 0.004, 0.05, 5), k * 0.13)
    save('dig', norm(fadeio(buf), 0.4), '32k')
    for k, m in enumerate([72, 76, 79]):  # rosa, gelb, blau = C5, E5, G5 (eine Oktave höher als MIDI 60er)
        save(f'flower{k}', norm(fadeio(bell(midi(m + 12), 1.1, 1.0, 0.9)), 0.6), '40k')
    t = t_(0.28); save('wrong', norm(fadeio(np.sin(2 * np.pi * (330 * t - 160 * t ** 2)) * np.sin(np.pi * t / 0.28) ** 1.5), 0.35), '32k')
    buf = np.zeros(int(2.2 * SR)); t = t_(2.2)
    swell = np.sin(np.pi * np.clip(t / 2.2, 0, 1)) ** 1.5
    for f in (196, 294, 392, 588): buf += np.sin(2 * np.pi * f * t) * 0.3 * swell
    for k, m in enumerate([72, 76, 79, 84, 88]): add(buf, bell(midi(m), 0.7, 0.5, 0.7, 1.4), 0.3 + k * 0.12)
    save('stone_on', norm(fadeio(buf, 0.005, 0.1), 0.6), '48k')
    buf = np.zeros(int(3.0 * SR)); t = t_(3.0)
    nz = highpass(lowpass(rng.standard_normal(len(t)), 4), 20) * np.sin(np.pi * np.clip(t / 3, 0, 1)) ** 2 * 0.25
    buf += nz
    for k, m in enumerate([67, 72, 76, 79, 84, 88, 91, 96]): add(buf, bell(midi(m), 1.2, 0.55, 1.0, 1.6), 0.1 + k * 0.17)
    save('gate_open', norm(fadeio(buf, 0.005, 0.15), 0.65), '48k')
    n = int(0.06 * SR); t = np.arange(n) / SR
    save('click', norm(fadeio(np.sin(2 * np.pi * 620 * t) * np.exp(-t * 60)), 0.45), '32k')
    buf = np.zeros(int(1.0 * SR)); add(buf, bell(midi(79), 0.8, 0.5, 0.7), 0); add(buf, bell(midi(84), 0.9, 0.5, 0.8), 0.12)
    save('feed', norm(fadeio(buf), 0.55), '40k')

if __name__ == '__main__':
    music(); wind(); water()
    bird(1, 'bird_a', 3); bird(2, 'bird_b', 4, 3600); bird(3, 'bird_c', 2, 2800); bird(4, 'bird_call', 4, 3300, 1.0)
    sfx()
    print('fertig →', os.path.abspath(OUT))
