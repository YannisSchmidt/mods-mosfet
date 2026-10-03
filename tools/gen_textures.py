#!/usr/bin/env python3
"""Genere les textures 16x16 des deux MOSFET (et l'icone du mod).

Les textures sont ecrites en PNG RGBA sans dependance externe (zlib + struct).
Usage : python3 tools/gen_textures.py
"""

import os
import struct
import zlib

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "assets", "mosfet")

BODY = (0x1A, 0x1A, 0x20)      # epoxy
BODY_DARK = (0x13, 0x13, 0x18)
BEVEL = (0x35, 0x35, 0x40)     # bord du boitier
METAL = (0xC9, 0xC9, 0xD4)     # pattes / plages metalliques
METAL_DARK = (0x8B, 0x8B, 0x99)
WHITE = (0xEE, 0xF0, 0xF6)

N_ACCENT = (0x4A, 0xA3, 0xFF)  # canal N : bleu
N_ACCENT_DARK = (0x1E, 0x5F, 0xA8)
P_ACCENT = (0xFF, 0x8A, 0x3C)  # canal P : orange
P_ACCENT_DARK = (0xA5, 0x44, 0x1A)


# ---------------------------------------------------------------- png
def write_png(path, width, height, pixels):
    raw = bytearray()
    for row in pixels:
        raw.append(0)
        for r, g, b, a in row:
            raw += struct.pack("BBBB", r, g, b, a)

    def chunk(tag, data):
        out = struct.pack(">I", len(data)) + tag + data
        return out + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")
    with open(path, "wb") as handle:
        handle.write(png)


# ---------------------------------------------------------------- canvas
def rgba(color):
    """Accepte des couleurs RGB ou RGBA."""
    return tuple(color) + (255,) * (4 - len(color))


class Canvas:
    def __init__(self, size, color):
        self.size = size
        self.px = [[rgba(color) for _ in range(size)] for _ in range(size)]

    def set(self, x, y, color):
        if 0 <= x < self.size and 0 <= y < self.size:
            self.px[y][x] = rgba(color)

    def rect(self, x0, y0, x1, y1, color):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, rgba(color))

    def save(self, path):
        write_png(path, self.size, self.size, self.px)


def chip_body(canvas):
    """Boitier noir avec un leger biseau clair sur le pourtour."""
    canvas.rect(0, 0, 15, 15, BODY)
    canvas.rect(0, 0, 15, 0, BEVEL)
    canvas.rect(0, 15, 15, 15, BEVEL)
    canvas.rect(0, 1, 0, 14, BEVEL)
    canvas.rect(15, 1, 15, 14, BEVEL)
    canvas.rect(2, 2, 13, 13, BODY_DARK)
    canvas.rect(3, 3, 12, 12, BODY)


def top_texture(accent, accent_dark, bubble):
    canvas = Canvas(16, BODY)
    chip_body(canvas)

    # pattes : drain (haut / nord), source (bas / sud), grille (gauche + droite)
    canvas.rect(6, 0, 9, 1, METAL)
    canvas.rect(6, 2, 9, 2, METAL_DARK)
    canvas.rect(6, 13, 9, 13, METAL_DARK)
    canvas.rect(6, 14, 9, 15, METAL)
    canvas.rect(0, 6, 2, 9, METAL)
    canvas.rect(13, 6, 15, 9, METAL)
    canvas.rect(2, 6, 2, 9, METAL_DARK)
    canvas.rect(13, 6, 13, 9, METAL_DARK)

    # fleche blanche vers le nord : c'est le sens du drain (sortie)
    canvas.rect(7, 3, 8, 3, WHITE)
    canvas.rect(6, 4, 9, 4, WHITE)
    canvas.rect(5, 5, 10, 5, WHITE)
    canvas.rect(7, 6, 8, 7, WHITE)

    # barre de canal
    canvas.rect(4, 9, 11, 9, accent_dark)
    canvas.rect(4, 10, 11, 11, accent)
    canvas.rect(4, 12, 11, 12, accent_dark)

    if bubble:
        # bulle d'inversion : signature du canal P
        canvas.rect(2, 10, 3, 11, WHITE)

    return canvas


def side_texture(accent, kind):
    canvas = Canvas(16, BODY)
    chip_body(canvas)

    # patte metallique descendant vers le bas du boitier
    canvas.rect(7, 12, 8, 15, METAL)
    canvas.rect(7, 11, 8, 11, METAL_DARK)

    if kind == "drain":
        canvas.rect(5, 6, 10, 6, accent)
        canvas.rect(5, 7, 10, 8, accent)
        canvas.rect(5, 9, 10, 9, accent)
    elif kind == "source":
        canvas.rect(5, 6, 10, 8, METAL)
        canvas.rect(5, 9, 10, 9, METAL_DARK)
    else:  # gate
        canvas.rect(3, 7, 5, 8, METAL)
        canvas.rect(10, 7, 12, 8, METAL)
        canvas.rect(7, 5, 8, 9, accent)

    return canvas


def bottom_texture():
    canvas = Canvas(16, BODY)
    chip_body(canvas)
    for x0, y0 in ((3, 3), (11, 3), (3, 11), (11, 11)):
        canvas.rect(x0, y0, x0 + 1, y0 + 1, METAL_DARK)
    return canvas


def icon(accent, accent_dark):
    """Icone 64x64 : un boitier vu de dessus avec ses trois pattes."""
    canvas = Canvas(64, (0x22, 0x22, 0x2B))
    canvas.rect(4, 4, 59, 59, (0x11, 0x11, 0x16))
    canvas.rect(8, 8, 55, 55, (0x1C, 0x1C, 0x24))

    # pattes
    canvas.rect(27, 8, 36, 18, METAL)      # drain (haut)
    canvas.rect(27, 45, 36, 55, METAL)     # source (bas)
    canvas.rect(8, 27, 18, 36, METAL)      # grille (gauche)
    canvas.rect(45, 27, 55, 36, METAL)     # grille (droite)

    # symbole : canal vertical + barre de grille
    canvas.rect(29, 22, 34, 41, accent)
    canvas.rect(22, 29, 41, 34, accent_dark)
    canvas.rect(29, 29, 34, 34, accent)
    canvas.rect(28, 18, 35, 18, WHITE)
    canvas.rect(26, 19, 37, 20, WHITE)
    return canvas


def main():
    blocks = {
        "nmos_mosfet": (N_ACCENT, N_ACCENT_DARK, False),
        "cmos_mosfet": (P_ACCENT, P_ACCENT_DARK, True),
    }

    tex_dir = os.path.join(OUT, "textures", "block")
    os.makedirs(tex_dir, exist_ok=True)

    for name, (accent, accent_dark, bubble) in blocks.items():
        top_texture(accent, accent_dark, bubble).save(os.path.join(tex_dir, name + "_top.png"))
        side_texture(accent, "drain").save(os.path.join(tex_dir, name + "_drain.png"))
        side_texture(accent, "source").save(os.path.join(tex_dir, name + "_source.png"))
        side_texture(accent, "gate").save(os.path.join(tex_dir, name + "_gate.png"))
        bottom_texture().save(os.path.join(tex_dir, name + "_bottom.png"))

    icon(N_ACCENT, N_ACCENT_DARK).save(os.path.join(OUT, "icon.png"))
    print("textures generees dans", os.path.normpath(tex_dir))


if __name__ == "__main__":
    main()
