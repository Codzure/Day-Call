# Play Store asset generator

This folder contains a simple generator to create Google Play listing images.

What you can generate here
- Feature graphic (1024x500 PNG)
- Processed phone screenshots (1080x1920 PNG) with optional caption overlays

Requirements
- Python 3.8+
- Pillow library: pip install Pillow
- Your app icon at app/src/main/res/drawable/app_icon.png (already present)
- Optional: background image at app/src/main/res/drawable/bg.png (already present)

Usage
1) Create and activate a virtualenv (optional but recommended)
   - python3 -m venv .venv && source .venv/bin/activate
   - pip install Pillow

2) Option A: Generate only the feature graphic
   - python generate_assets.py --feature-graphic
   -> Outputs to output/feature_graphic.png (1024x500)

3) Option B: Process phone screenshots with captions
   - Put raw portrait screenshots (PNG/JPG) into input/ (e.g., input/01.png, 02.png, ...)
   - Edit captions.json to set overlay text per image (keys should match filenames without extension)
   - python generate_assets.py --screenshots
   -> Outputs to output/screenshots/phone_01.png, phone_02.png, ... (1080x1920)

Notes and tips
- Google Play requires at least 2 phone screenshots; recommended size is 1080x1920 (portrait).
- Feature graphic must be 1024x500 (JPG or PNG), under 1MB. This script produces PNG.
- If the generator cannot find a TTF font on your system, it will fall back to a default PIL bitmap font.
- Brand colors are configurable via CLI flags; default primary is #4F46E5 to match the app.

CLI help
- python generate_assets.py -h


