"""
Helper to populate assets/logos with sample league team logos.
"""

from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
from template_accv.config import LOGOS_DIR


def generate_sample_team_logos():
    """Create clean team logo graphics for standard league teams."""
    LOGOS_DIR.mkdir(parents=True, exist_ok=True)
    
    teams = [
        ("Real Matrid", "real_matrid.png", (220, 50, 50), (255, 255, 255), "RM"),
        ("FC Barcelona", "fc_barcelona.png", (0, 75, 160), (200, 30, 60), "FCB"),
        ("Inter Calcetto", "inter_calcetto.png", (0, 102, 204), (0, 0, 0), "INT"),
        ("Milan Calcetto", "milan_calcetto.png", (220, 20, 20), (0, 0, 0), "ACM"),
        ("Juventus Calcetto", "juventus_calcetto.png", (30, 30, 30), (255, 255, 255), "JUV"),
        ("Roma Calcetto", "roma_calcetto.png", (180, 20, 40), (240, 180, 20), "ASR"),
        ("Napoli Calcetto", "napoli_calcetto.png", (70, 170, 240), (255, 255, 255), "NAP"),
        ("Totutti", "totutti.png", (250, 250, 250), (20, 20, 20), "TOT"),
    ]

    size = (300, 300)
    for name, filename, bg_color, sec_color, initials in teams:
        out_path = LOGOS_DIR / filename
        if out_path.exists():
            continue

        img = Image.new("RGBA", size, (0, 0, 0, 0))
        draw = ImageDraw.Draw(img)
        
        # Draw shield/crest
        cx, cy = size[0] // 2, size[1] // 2
        r = 135
        
        # Shield background
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=bg_color, outline=sec_color, width=8)
        draw.ellipse([cx - r + 15, cy - r + 15, cx + r - 15, cy + r - 15], outline=(255, 255, 255, 200), width=4)
        
        # Initials text
        try:
            from template_accv.utils.fonts import get_font
            font = get_font("HEADER", 110)
        except Exception:
            font = ImageFont.load_default()
            
        bbox = font.getbbox(initials)
        tw = bbox[2] - bbox[0]
        th = bbox[3] - bbox[1]
        text_fill = sec_color if bg_color[0] > 200 else (255, 255, 255, 255)
        draw.text((cx - tw // 2, cy - th // 2 - 10), initials, font=font, fill=text_fill)
        
        img.save(out_path, "PNG")


if __name__ == "__main__":
    generate_sample_team_logos()
