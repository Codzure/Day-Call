#!/usr/bin/env python3
import argparse
import json
import os
from pathlib import Path
from typing import Tuple

from PIL import Image, ImageDraw, ImageFont, ImageColor

ROOT = Path(__file__).resolve().parent
RES = ROOT.parent.parent / "app" / "src" / "main" / "res"

OUT_DIR = ROOT / "output"
IN_DIR = ROOT / "input"
CAPTIONS = ROOT / "captions.json"

PRIMARY_DEFAULT = "#4F46E5"
ACCENT_DEFAULT = "#22D3EE"  # teal accent
TEXT_LIGHT = (255, 255, 255)
TEXT_DARK = (20, 20, 20)

FONT_CANDIDATES = [
    "/System/Library/Fonts/Supplemental/Arial Bold.ttf",
    "/System/Library/Fonts/Supplemental/Arial.ttf",
    "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
    "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
]


def load_font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    for path in FONT_CANDIDATES:
        if os.path.exists(path):
            try:
                return ImageFont.truetype(path, size=size)
            except Exception:
                continue
    # Fallback to PIL default bitmap font (not ideal, but works)
    return ImageFont.load_default()


def save_png(img: Image.Image, path: Path):
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path, format="PNG")


def make_gradient(size: Tuple[int, int], start_hex: str, end_hex: str) -> Image.Image:
    w, h = size
    start = ImageColor.getrgb(start_hex)
    end = ImageColor.getrgb(end_hex)
    base = Image.new("RGB", size, start)
    top = Image.new("RGB", size, end)
    mask = Image.linear_gradient("L").resize(size)
    return Image.composite(top, base, mask)


def feature_graphic(primary: str, accent: str):
    W, H = 1024, 500
    img = Image.new("RGB", (W, H), PRIMARY_DEFAULT)
    draw = ImageDraw.Draw(img)

    # Background: diagonal gradient bands
    for i in range(6):
        band = Image.new("RGBA", (int(W*1.2), int(H*0.5)), (0,0,0,0))
        band_draw = ImageDraw.Draw(band)
        alpha = int(32 + i*16)
        band_draw.rounded_rectangle([0,0,band.width,band.height], radius=48, fill=(*ImageColor.getrgb(accent), alpha))
        band = band.rotate(-15 - i*5, expand=True)
        img.alpha_composite(band, (int(-0.1*W + i*120), int(40 + i*40))) if img.mode == "RGBA" else img.paste(band, (int(-0.1*W + i*120), int(40 + i*40)), band)

    # App icon
    app_icon_path_png = RES / "drawable" / "app_icon.png"
    if app_icon_path_png.exists():
        icon = Image.open(app_icon_path_png).convert("RGBA")
        # Fit into a nice size box
        ICON_H = int(H * 0.5)
        ratio = ICON_H / icon.height
        icon = icon.resize((int(icon.width*ratio), ICON_H), Image.LANCZOS)
        # Drop shadow
        shadow = Image.new("RGBA", icon.size, (0, 0, 0, 0))
        ImageDraw.Draw(shadow).rounded_rectangle([0,0,icon.width,icon.height], radius=48, fill=(0,0,0,64))
        img.paste(shadow, (int(W*0.08)+6, int(H*0.25)+6), shadow)
        img.paste(icon, (int(W*0.08), int(H*0.25)), icon)

    # Title and tagline
    title_font = load_font(64, bold=True)
    subtitle_font = load_font(30)
    title = "Day Call"
    tagline = "Wake with vibes. Live with intention."
    draw.text((int(W*0.45), int(H*0.28)), title, font=title_font, fill=TEXT_LIGHT)
    draw.text((int(W*0.45), int(H*0.28)+80), tagline, font=subtitle_font, fill=(235,235,245))

    save_png(img, OUT_DIR / "feature_graphic.png")


def fit_portrait(img: Image.Image, target=(1080, 1920)) -> Image.Image:
    # letterbox to fit 1080x1920
    TW, TH = target
    src = img.convert("RGBA")
    ratio = min(TW/src.width, TH/src.height)
    new_size = (int(src.width*ratio), int(src.height*ratio))
    resized = src.resize(new_size, Image.LANCZOS)
    bg = Image.new("RGBA", target, (10, 10, 20, 255))
    pos = ((TW - new_size[0])//2, (TH - new_size[1])//2)
    bg.paste(resized, pos, resized)
    return bg.convert("RGB")


def overlay_caption(img: Image.Image, text: str, primary: str):
    draw = ImageDraw.Draw(img)
    W, H = img.size
    pad = 36
    # Translucent top bar
    bar_h = 220
    overlay = Image.new("RGBA", (W, bar_h), (*ImageColor.getrgb(primary), 180))
    img.paste(overlay, (0, 0), overlay)

    title_font = load_font(64, bold=True)
    draw.text((pad, pad), text, font=title_font, fill=TEXT_LIGHT)


def process_screenshots(primary: str):
    if not IN_DIR.exists():
        print(f"No input dir: {IN_DIR}. Place raw screenshots there.")
        return
    captions = {}
    if CAPTIONS.exists():
        captions = json.loads(CAPTIONS.read_text())

    for fp in sorted(IN_DIR.glob("*")):
        if fp.suffix.lower() not in [".png", ".jpg", ".jpeg", ".webp"]:
            continue
        base = fp.stem
        img = Image.open(fp)
        fitted = fit_portrait(img)
        caption = captions.get(base)
        if caption:
            overlay_caption(fitted, caption, primary)
        save_png(fitted, OUT_DIR / "screenshots" / f"phone_{base}.png")


def generate_mock_screens(primary: str):
    # Create synthetic screenshots for major app screens if no real screenshots provided
    TW, TH = 1080, 1920
    screens = [
        ("01_home", "Alarms", "Your alarms at a glance"),
        ("02_add_alarm", "Add Alarm", "Label, days, tones, challenges"),
        ("03_edit_alarm", "Edit Alarm", "Fine-tune time, repeat and vibe"),
        ("04_ring", "Wake Up Challenge", "Engage your brain to dismiss"),
        ("05_vibes", "Vibes", "Pick a vibe to match your morning"),
        ("06_todo", "Todos", "Plan your day with reminders"),
        ("07_add_todo", "Add Task", "Due date, reminders, recurrence"),
        ("08_completed", "Completed", "Celebrate what you've done"),
        ("09_settings", "Settings", "Reliability, sounds, preferences"),
        ("10_login", "Welcome", "Sign in to sync your experience"),
        ("11_splash", "Day Call", "Wake with vibes. Live with intention."),
    ]
    bg_path = RES / "drawable" / "bg.png"
    app_icon_path_png = RES / "drawable" / "app_icon.png"

    for key, title, subtitle in screens:
        # Background
        if bg_path.exists():
            bg = Image.open(bg_path).convert("RGB").resize((TW, TH), Image.LANCZOS)
        else:
            # simple gradient background
            top = ImageColor.getrgb(primary)
            base = Image.new("RGB", (TW, TH), top)
            overlay = Image.new("RGBA", (TW, TH), (*ImageColor.getrgb("#000000"), 80))
            base.paste(overlay, (0, 0), overlay)
            bg = base

        img = bg.copy()
        draw = ImageDraw.Draw(img)

        # Top translucent bar
        bar_h = 280
        overlay = Image.new("RGBA", (TW, bar_h), (*ImageColor.getrgb(primary), 170))
        img.paste(overlay, (0, 0), overlay)

        # App icon on bar
        if app_icon_path_png.exists():
            icon = Image.open(app_icon_path_png).convert("RGBA")
            ICON_H = 140
            ratio = ICON_H / icon.height
            icon = icon.resize((int(icon.width*ratio), ICON_H), Image.LANCZOS)
            img.paste(icon, (48, 48), icon)

        # Title/subtitle text
        title_font = load_font(72, bold=True)
        subtitle_font = load_font(36)
        draw.text((220, 64), title, font=title_font, fill=TEXT_LIGHT)
        draw.text((220, 64+96), subtitle, font=subtitle_font, fill=(235,235,245))

        save_png(img, OUT_DIR / "screenshots" / f"phone_{key}.png")


def main():
    parser = argparse.ArgumentParser(description="Generate Play Store assets")
    parser.add_argument("--feature-graphic", action="store_true", help="Generate 1024x500 feature graphic")
    parser.add_argument("--screenshots", action="store_true", help="Process phone screenshots to 1080x1920 with optional captions")
    parser.add_argument("--all-screens", action="store_true", help="Generate synthetic screenshots for every major app screen")
    parser.add_argument("--primary", default=PRIMARY_DEFAULT, help="Primary brand color (hex)")
    parser.add_argument("--accent", default=ACCENT_DEFAULT, help="Accent brand color (hex)")
    args = parser.parse_args()

    os.makedirs(OUT_DIR, exist_ok=True)
    os.makedirs(IN_DIR, exist_ok=True)

    if args.feature_graphic:
        feature_graphic(args.primary, args.accent)
        print(f"Wrote {OUT_DIR / 'feature_graphic.png'}")
    if args.screenshots:
        process_screenshots(args.primary)
        print(f"Wrote screenshots to {OUT_DIR / 'screenshots'}")
    if args.all_screens:
        generate_mock_screens(args.primary)
        print(f"Wrote mock screenshots to {OUT_DIR / 'screenshots'}")
    if not any([args.feature_graphic, args.screenshots, args.all_screens]):
        parser.print_help()


if __name__ == "__main__":
    main()

