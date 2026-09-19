"""Bake the native-size tag panel PNG used in-game (panel/background.png)."""

from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

sys.stdout.reconfigure(encoding="utf-8")

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "common/src/main/resources/assets/cobblemonpcsortplus/textures/gui/sprites/panel"
PREVIEW = ROOT / "build/gui-preview"
BUTTON = ROOT / "common/src/main/resources/assets/cobblemonpcsortplus/textures/gui/sprites/widget/button.png"
BUTTON_SEL = ROOT / "common/src/main/resources/assets/cobblemonpcsortplus/textures/gui/sprites/widget/button_selected.png"
OUT.mkdir(parents=True, exist_ok=True)
PREVIEW.mkdir(parents=True, exist_ok=True)

TRANS = (0, 0, 0, 0)
OUTL = (0x2F, 0x2F, 0x2F, 255)
DEEP = (0x3D, 0x3D, 0x3D, 255)
WELL = (0x4B, 0x4B, 0x4B, 255)
RARE = (0x59, 0x59, 0x59, 255)
FILL = (0x67, 0x67, 0x67, 255)
MID = (0x8D, 0x8D, 0x8D, 255)
HI = (0xC6, 0xC6, 0xC6, 255)
GRASS = (0x47, 0x70, 0x4C, 255)

TAG_WIDTH = 74
CHIP_HEIGHT = 16
CHIP_GAP_Y = 3
TAG_ROWS = 13
TAG_INSET = 10
IV_PAD = 6
IV_WIDTH = 74
BUTTON_HEIGHT = 20
STAIR = 8
WELL_PAD = 4
WELL_Y0 = IV_PAD + BUTTON_HEIGHT + 2
PAGER_SIZE = 16
PAGER_GAP = 4
TAG_START_Y = WELL_Y0 + WELL_PAD + PAGER_SIZE + PAGER_GAP
TAG_BOTTOM_PAD = 6
ORDER_GAP = 4
FOOTER_HEIGHT = ORDER_GAP + BUTTON_HEIGHT
TAG_COL_GAP = 6
W = TAG_INSET * 2 + TAG_WIDTH * 2 + TAG_COL_GAP
H = TAG_START_Y + TAG_ROWS * (CHIP_HEIGHT + CHIP_GAP_Y) - CHIP_GAP_Y + TAG_BOTTOM_PAD + FOOTER_HEIGHT

# Vanilla default-font glyph widths. 1px is added between characters.
NARROW = {
    " ": 3, "!": 1, "'": 1, ",": 1, ".": 1, ":": 1, ";": 1, "I": 3, "i": 1, "l": 1,
    "t": 3, "f": 4, "k": 4, "|": 1, "î": 3, "ï": 3,
}


def latin_width(text: str) -> int:
    if not text:
        return 0
    return sum(NARROW.get(ch, 5) for ch in text) + (len(text) - 1)


def cyr_width(text: str) -> int:
    if not text:
        return 0
    return 6 * len(text) + (len(text) - 1)


def px(im, x, y, c):
    if 0 <= x < im.width and 0 <= y < im.height:
        im.putpixel((x, y), c)


def in_round_rect(x, y, w, h, rtl, rtr, rbl, rbr, stair_tl=0, stair_tr=0, stair_bl=0, stair_br=0):
    if x < 0 or y < 0 or x >= w or y >= h:
        return False
    if stair_tl > 0 and y < stair_tl and x < (stair_tl - y):
        return False
    if stair_tr > 0 and y < stair_tr and x > (w - 1 - (stair_tr - y)):
        return False
    db = h - 1 - y
    if stair_bl > 0 and db < stair_bl and x < (stair_bl - db):
        return False
    if stair_br > 0 and db < stair_br and x > (w - 1 - (stair_br - db)):
        return False

    def cut(px_, py_, r):
        if r <= 1:
            return False
        return (r - 1 - px_) ** 2 + (r - 1 - py_) ** 2 > (r - 0.45) ** 2

    if x < rtl and y < rtl and cut(x, y, rtl):
        return False
    if x >= w - rtr and y < rtr and cut(w - 1 - x, y, rtr):
        return False
    if x < rbl and y >= h - rbl and cut(x, h - 1 - y, rbl):
        return False
    if x >= w - rbr and y >= h - rbr and cut(w - 1 - x, h - 1 - y, rbr):
        return False
    return True


def draw_plate(
    im,
    x0,
    y0,
    w,
    h,
    fill,
    rtl=2,
    rtr=2,
    rbl=3,
    rbr=3,
    raised=True,
    recessed=False,
    stair_tl=0,
    stair_tr=0,
    stair_bl=0,
    stair_br=0,
):
    def inside(x, y):
        return in_round_rect(
            x, y, w, h, rtl, rtr, rbl, rbr, stair_tl, stair_tr, stair_bl, stair_br
        )

    for y in range(h):
        for x in range(w):
            if not inside(x, y):
                continue
            n = sum(
                1
                for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1))
                if inside(x + dx, y + dy)
            )
            gx, gy = x0 + x, y0 + y
            if n < 4:
                px(im, gx, gy, OUTL)
            elif recessed:
                if y == 1:
                    px(im, gx, gy, OUTL)
                elif x == 1:
                    px(im, gx, gy, DEEP)
                elif x == w - 2:
                    px(im, gx, gy, RARE)
                elif y == h - 2:
                    px(im, gx, gy, RARE)
                else:
                    px(im, gx, gy, fill)
            elif raised:
                if y == 1 and 2 <= x <= w - 3:
                    px(im, gx, gy, HI)
                elif y in (2, 3) and 2 <= x <= w - 3:
                    px(im, gx, gy, MID)
                elif x == 1:
                    px(im, gx, gy, MID)
                elif x == w - 2:
                    px(im, gx, gy, MID)
                elif y >= h - 2:
                    px(im, gx, gy, DEEP if y == h - 1 else WELL)
                else:
                    px(im, gx, gy, fill)
            else:
                if x == w - 2:
                    px(im, gx, gy, MID)
                else:
                    px(im, gx, gy, fill)


def chamfer_well(im, x0, y0, x1, y1, stair=6):
    def inside(x, y):
        if x < x0 or x > x1 or y < y0 or y > y1:
            return False
        if (y - y0) < stair and x > x1 - (stair - (y - y0)):
            return False
        db = y1 - y
        if db < stair and x < x0 + (stair - db):
            return False
        return True

    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if not inside(x, y):
                continue
            n = sum(
                1
                for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1))
                if inside(x + dx, y + dy)
            )
            if n < 4:
                px(im, x, y, OUTL)
            elif x == x0 + 1 or y == y0 + 1:
                px(im, x, y, DEEP)
            elif x == x1 - 1 or y == y1 - 1:
                px(im, x, y, RARE)
            else:
                px(im, x, y, DEEP)


def bracket(im, x, y, dx, dy):
    for i in range(9):
        px(im, x + i * dx, y, OUTL)
        px(im, x + i * dx, y + dy, HI if i else MID)
    for j in range(1, 9):
        px(im, x, y + j * dy, OUTL)
        px(im, x + dx, y + j * dy, HI if j else MID)


def staggered_pips(im, x, y, cols, rows, top_row_right):
    size, stride, stagger = 2, 5, 3
    for row in range(rows):
        shift = (row % 2 == 0) if top_row_right else (row % 2 == 1)
        ox = stagger if shift else 0
        for col in range(cols):
            px0 = x + ox + col * stride
            py0 = y + row * stride
            px(im, px0, py0, DEEP)
            px(im, px0 + 1, py0, DEEP)
            px(im, px0, py0 + 1, DEEP)
            px(im, px0 + 1, py0 + 1, DEEP)


def nine_slice(src: Image.Image, dw: int, dh: int, border: int = 4) -> Image.Image:
    sw, sh = src.size
    out = Image.new("RGBA", (dw, dh), TRANS)
    b = border
    tiles = [
        ((0, 0, b, b), (0, 0, b, b)),
        ((b, 0, sw - b, b), (b, 0, dw - b, b)),
        ((sw - b, 0, sw, b), (dw - b, 0, dw, b)),
        ((0, b, b, sh - b), (0, b, b, dh - b)),
        ((b, b, sw - b, sh - b), (b, b, dw - b, dh - b)),
        ((sw - b, b, sw, sh - b), (dw - b, b, dw, dh - b)),
        ((0, sh - b, b, sh), (0, dh - b, b, dh)),
        ((b, sh - b, sw - b, sh), (b, dh - b, dw - b, dh)),
        ((sw - b, sh - b, sw, sh), (dw - b, dh - b, dw, dh)),
    ]
    for src_box, dst_box in tiles:
        piece = src.crop(src_box)
        dw_, dh_ = dst_box[2] - dst_box[0], dst_box[3] - dst_box[1]
        if dw_ <= 0 or dh_ <= 0:
            continue
        if piece.size != (dw_, dh_):
            piece = piece.resize((dw_, dh_), Image.NEAREST)
        out.paste(piece, (dst_box[0], dst_box[1]))
    return out


def report_labels():
    usable_plain = TAG_WIDTH - 10
    usable_icon = TAG_WIDTH - 22
    cases = [
        ("EN", False, "Source", latin_width("Source"), usable_plain),
        ("EN", False, "Fallback", latin_width("Fallback"), usable_plain),
        ("EN", False, "Legendary", latin_width("Legendary"), usable_plain),
        ("EN", False, "Mythical", latin_width("Mythical"), usable_plain),
        ("EN", True, "Water", latin_width("Water"), usable_icon),
        ("EN", True, "Psychic", latin_width("Psychic"), usable_icon),
        ("EN", True, "Flying", latin_width("Flying"), usable_icon),
        ("EN", True, "Electric", latin_width("Electric"), usable_icon),
        ("FR", False, "Source", latin_width("Source"), usable_plain),
        ("FR", False, "Repli", latin_width("Repli"), usable_plain),
        ("FR", False, "Légendaire", latin_width("Légendaire"), usable_plain),
        ("FR", False, "Fabuleux", latin_width("Fabuleux"), usable_plain),
        ("FR", True, "Eau", latin_width("Eau"), usable_icon),
        ("FR", True, "Psy", latin_width("Psy"), usable_icon),
        ("FR", True, "Vol", latin_width("Vol"), usable_icon),
        ("FR", True, "Électrik", latin_width("Électrik"), usable_icon),
        ("RU", False, "Источник", cyr_width("Источник"), usable_plain),
        ("RU", False, "Резерв", cyr_width("Резерв"), usable_plain),
        ("RU", False, "Легендарный", cyr_width("Легендарный"), usable_plain),
        ("RU", False, "Легенда", cyr_width("Легенда"), usable_plain),
        ("RU", False, "Мифический", cyr_width("Мифический"), usable_plain),
        ("RU", False, "Мифич.", cyr_width("Мифич."), usable_plain),
        ("RU", True, "Вода", cyr_width("Вода"), usable_icon),
        ("RU", True, "Пси", cyr_width("Пси"), usable_icon),
        ("RU", True, "Полёт", cyr_width("Полёт"), usable_icon),
        ("RU", True, "Электро", cyr_width("Электро"), usable_icon),
        ("RU", True, "Электрический", cyr_width("Электрический"), usable_icon),
        ("RU", True, "Психический", cyr_width("Психический"), usable_icon),
        ("RU", True, "Летающий", cyr_width("Летающий"), usable_icon),
    ]
    lines = [
        f"panel {W}x{H}  chip {TAG_WIDTH}x{CHIP_HEIGHT}  plain={usable_plain}px  icon={usable_icon}px",
        f"{'lang':<4} {'icon':<4} {'label':<16} {'px':>4} {'room':>4} result",
    ]
    for lang, icon, label, width, room in cases:
        ok = "PASS" if width <= room else "FAIL"
        lines.append(f"{lang:<4} {'yes' if icon else 'no':<4} {label:<16} {width:>4} {room:>4} {ok}")
    report = "\n".join(lines) + "\n"
    (PREVIEW / "label_fit.txt").write_text(report, encoding="utf-8")
    print(report)


def build_panel() -> Image.Image:
    im = Image.new("RGBA", (W, H), TRANS)
    well_y0 = WELL_Y0
    well_y1 = TAG_START_Y + TAG_ROWS * (CHIP_HEIGHT + CHIP_GAP_Y) - CHIP_GAP_Y + TAG_BOTTOM_PAD - 2
    plate_h = well_y1 + 5 - 1

    draw_plate(
        im,
        2,
        1,
        W - 4,
        plate_h,
        FILL,
        rtl=0,
        rtr=0,
        rbl=0,
        rbr=0,
        raised=True,
        stair_tl=0,
        stair_tr=STAIR,
        stair_bl=STAIR,
        stair_br=0,
    )
    chamfer_well(im, TAG_INSET - 3, well_y0, W - TAG_INSET + 3, well_y1, STAIR)
    return im


def build_preview(panel: Image.Image) -> Image.Image:
    scale = 2
    bg = Image.new("RGBA", (W, H), GRASS)
    bg.alpha_composite(panel)
    idle = nine_slice(Image.open(BUTTON).convert("RGBA"), TAG_WIDTH, CHIP_HEIGHT)
    selected = nine_slice(Image.open(BUTTON_SEL).convert("RGBA"), TAG_WIDTH, CHIP_HEIGHT)
    order_w = TAG_WIDTH * 2 + TAG_COL_GAP - 18
    order = nine_slice(Image.open(BUTTON).convert("RGBA"), order_w, BUTTON_HEIGHT)
    iv = nine_slice(Image.open(BUTTON).convert("RGBA"), IV_WIDTH, BUTTON_HEIGHT)
    labels = [
        "Source", "Protect",
        "Fallback", "IV",
        "Shiny", "Legendary",
        "Mythical", "Water",
        "Fire", "Grass",
        "Electric", "Steel",
        "Ground", "Rock",
        "Fight", "Psychic",
        "Flying", "Bug",
        "Poison", "Dark",
        "Dragon", "Ice",
        "Fairy", "Normal",
        "Ghost",
    ]
    draw = ImageDraw.Draw(bg)
    font = ImageFont.load_default()
    selected_tags = {0, 7, 9}
    for index, label in enumerate(labels):
        col = index % 2
        row = index // 2
        x = TAG_INSET + col * (TAG_WIDTH + TAG_COL_GAP)
        y = TAG_START_Y + row * (CHIP_HEIGHT + CHIP_GAP_Y)
        chip = selected if index in selected_tags else idle
        bg.alpha_composite(chip, (x, y))
        tw = latin_width(label)
        tx = x + (TAG_WIDTH - tw) // 2
        if index >= 7:
            tx = x + 16 + (TAG_WIDTH - 20 - tw) // 2
        draw.text((tx, y + 4), label, fill=(255, 255, 255, 255), font=font)
    iv_x = W - IV_PAD - IV_WIDTH
    bg.alpha_composite(iv, (iv_x, IV_PAD))
    draw.text((iv_x + 18, IV_PAD + 6), "IV >= 4/6", fill=(255, 255, 255, 255), font=font)
    tw = latin_width("Box 1")
    cluster_w = 3 + 3 * 5 + 2
    header_left, header_right = 8, W - IV_PAD - IV_WIDTH - 6
    total = cluster_w * 2 + 12 + tw
    start = header_left + max(0, (header_right - header_left - total) // 2)
    pip_y = IV_PAD + (BUTTON_HEIGHT - 7) // 2
    staggered_pips(bg, start, pip_y, 4, 2, True)
    draw.text((start + cluster_w + 6, IV_PAD + 6), "Box 1", fill=(255, 255, 255, 255), font=font)
    staggered_pips(bg, start + cluster_w + 6 + tw + 6, pip_y, 4, 2, False)
    order_x = (W - order_w) // 2
    bg.alpha_composite(order, (order_x, H - BUTTON_HEIGHT))
    tw = latin_width("Order: Level")
    draw.text((order_x + (order_w - tw) // 2, H - BUTTON_HEIGHT + 6), "Order: Level", fill=(255, 255, 255, 255), font=font)
    return bg.resize((W * scale, H * scale), Image.NEAREST)


def main():
    report_labels()
    panel = build_panel()
    panel.save(OUT / "background.png")
    mcmeta = OUT / "background.png.mcmeta"
    if mcmeta.exists():
        mcmeta.unlink()
    preview = build_preview(panel)
    preview.save(PREVIEW / "panel_organic_x2.png")
    print("wrote", OUT / "background.png", panel.size)


if __name__ == "__main__":
    main()
