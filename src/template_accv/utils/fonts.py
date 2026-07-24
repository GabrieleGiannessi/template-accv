"""
Font manager and automatic loader from Google Fonts.
"""

import os
import urllib.request
from pathlib import Path
from PIL import ImageFont

from template_accv.config import FONTS, FONTS_DIR


FONT_URLS = {
    "BebasNeue-Regular.ttf": "https://raw.githubusercontent.com/google/fonts/main/ofl/bebasneue/BebasNeue-Regular.ttf",
    "Montserrat-Bold.ttf": "https://raw.githubusercontent.com/google/fonts/main/ofl/montserrat/Montserrat-Bold.ttf",
    "Montserrat-Regular.ttf": "https://raw.githubusercontent.com/google/fonts/main/ofl/montserrat/Montserrat-Regular.ttf",
    "Montserrat-Light.ttf": "https://raw.githubusercontent.com/google/fonts/main/ofl/montserrat/Montserrat-Light.ttf",
}

_fonts_checked = False

def ensure_fonts_downloaded():
    """Ensure all required fonts exist locally in assets/fonts."""
    global _fonts_checked
    if _fonts_checked:
        return
    _fonts_checked = True

    for font_file, url in FONT_URLS.items():
        font_path = FONTS_DIR / font_file
        if not font_path.exists():
            try:
                urllib.request.urlretrieve(url, font_path)
            except Exception:
                pass


def get_font(font_name_key: str, size: int) -> ImageFont.FreeTypeFont:
    """
    Get a loaded PIL ImageFont by key ('HEADER', 'BODY', 'REGULAR', 'LIGHT') or filename.
    """
    ensure_fonts_downloaded()
    
    font_file = FONTS.get(font_name_key, font_name_key)
    font_path = FONTS_DIR / font_file
    
    if font_path.exists():
        try:
            return ImageFont.truetype(str(font_path), size)
        except Exception as e:
            print(f"Error loading {font_path}: {e}")
            
    # Fallback to default font
    return ImageFont.load_default()
