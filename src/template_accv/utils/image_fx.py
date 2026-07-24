"""
Image processing utilities for Pillow graphics rendering.
Provides rounded glass cards, gradients, glow effects, badge drawing, and text layout helpers.
"""

from typing import Tuple, Optional
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter, ImageFont, ImageOps
import math


def create_gradient_background(
    width: int,
    height: int,
    top_color: Tuple[int, int, int, int],
    bottom_color: Tuple[int, int, int, int],
    radial_spotlight: bool = True
) -> Image.Image:
    """Create a high quality vertical gradient background with optional radial spotlight center."""
    base = Image.new("RGBA", (width, height), top_color)
    gradient = Image.new("RGBA", (width, height))
    
    # Vertical linear gradient
    for y in range(height):
        ratio = y / height
        r = int(top_color[0] + (bottom_color[0] - top_color[0]) * ratio)
        g = int(top_color[1] + (bottom_color[1] - top_color[1]) * ratio)
        b = int(top_color[2] + (bottom_color[2] - top_color[2]) * ratio)
        a = int(top_color[3] + (bottom_color[3] - top_color[3]) * ratio)
        
        # Line drawing
        for x in range(width):
            gradient.putpixel((x, y), (r, g, b, a))
            
    if radial_spotlight:
        # Add a subtle radial cyan spotlight at upper-center
        spotlight = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        spot_draw = ImageDraw.Draw(spotlight)
        center_x, center_y = width // 2, int(height * 0.35)
        max_r = int(min(width, height) * 0.6)
        
        for r in range(max_r, 0, -5):
            alpha = int(35 * (1.0 - (r / max_r) ** 1.5))
            spot_draw.ellipse(
                [center_x - r, center_y - r, center_x + r, center_y + r],
                fill=(0, 229, 255, alpha)
            )
        gradient = Image.alpha_composite(gradient, spotlight)

    return gradient


def draw_rounded_card(
    draw: ImageDraw.ImageDraw,
    bbox: Tuple[int, int, int, int],
    radius: int = 20,
    fill: Tuple[int, int, int, int] = (30, 36, 54, 220),
    border: Optional[Tuple[int, int, int, int]] = (60, 70, 100, 180),
    border_width: int = 2
):
    """Draw a rounded glassmorphic rectangle with smooth corners and optional border."""
    draw.rounded_rectangle(bbox, radius=radius, fill=fill, outline=border, width=border_width)


def get_text_dimensions(text: str, font: ImageFont.FreeTypeFont) -> Tuple[int, int]:
    """Calculate text width and height accurately."""
    bbox = font.getbbox(text)
    width = bbox[2] - bbox[0]
    height = bbox[3] - bbox[1]
    return width, height


def draw_text_centered(
    draw: ImageDraw.ImageDraw,
    text: str,
    font: ImageFont.FreeTypeFont,
    box: Tuple[int, int, int, int],
    fill: Tuple[int, int, int, int] = (255, 255, 255, 255),
    shadow: bool = False
):
    """Draw text centered vertically and horizontally inside a bounding box (x1, y1, x2, y2)."""
    box_w = box[2] - box[0]
    box_h = box[3] - box[1]
    
    tw, th = get_text_dimensions(text, font)
    
    x = box[0] + (box_w - tw) // 2
    y = box[1] + (box_h - th) // 2
    
    if shadow:
        draw.text((x + 2, y + 2), text, font=font, fill=(0, 0, 0, 180))
        
    draw.text((x, y), text, font=font, fill=fill)


def get_fitted_font(
    text: str,
    font_name_key: str,
    max_width: int,
    initial_size: int,
    min_size: int = 14
) -> ImageFont.FreeTypeFont:
    """
    Dynamically calculate and return font scaled down so text fits within max_width.
    """
    from template_accv.utils.fonts import get_font
    current_size = initial_size
    font = get_font(font_name_key, current_size)
    while current_size > min_size:
        tw, _ = get_text_dimensions(text, font)
        if tw <= max_width:
            return font
        current_size -= 2
        font = get_font(font_name_key, current_size)
    return font


def format_team_name_vertical(
    name: str,
    font_name_key: str,
    max_width: int,
    initial_size: int,
    min_size: int = 18
) -> Tuple[list, ImageFont.FreeTypeFont]:
    """
    Format team name for drawing above team logo.
    If single line exceeds max_width and name has multiple words,
    split into 2 balanced vertical lines.
    """
    from template_accv.utils.fonts import get_font
    words = name.strip().upper().split()
    font = get_font(font_name_key, initial_size)
    tw, _ = get_text_dimensions(name.upper(), font)

    if (tw > max_width or len(name) > 14) and len(words) > 1:
        mid = len(words) // 2
        line1 = " ".join(words[:mid])
        line2 = " ".join(words[mid:])
        
        cur_sz = initial_size
        font = get_font(font_name_key, cur_sz)
        while cur_sz > min_size:
            w1, _ = get_text_dimensions(line1, font)
            w2, _ = get_text_dimensions(line2, font)
            if max(w1, w2) <= max_width:
                break
            cur_sz -= 2
            font = get_font(font_name_key, cur_sz)
            
        return [line1, line2], font

    font = get_fitted_font(name.upper(), font_name_key, max_width, initial_size, min_size)
    return [name.upper()], font


def find_logo_in_logos_dir(team_identifier: str) -> Optional[Path]:
    """Search LOGOS_DIR for matching logo filename."""
    from template_accv.config import LOGOS_DIR
    if not team_identifier or not LOGOS_DIR.exists():
        return None

    clean_id = team_identifier.strip().lower().replace(" ", "_").replace(".", "")
    for p in LOGOS_DIR.iterdir():
        if p.is_file() and p.suffix.lower() in {".png", ".jpg", ".jpeg", ".webp"}:
            stem = p.stem.lower().replace(" ", "_").replace(".", "")
            if clean_id in stem or stem in clean_id:
                return p
    return None


def load_team_logo(
    logo_path: Optional[str] = None,
    team_name: Optional[str] = None,
    size: Tuple[int, int] = (140, 140),
    fallback_text: str = "FC",
    primary_color: Tuple[int, int, int] = (0, 229, 255),
    padding: int = 10
) -> Image.Image:
    """
    Load a team logo image from path or logos directory, or generate a modern circular badge as fallback.
    Trims transparent padding and applies safe inner margins so no edges are clipped.
    """
    resolved_path: Optional[Path] = None

    if logo_path and Path(logo_path).exists():
        resolved_path = Path(logo_path)
    else:
        # Try finding in assets/logos
        if team_name:
            resolved_path = find_logo_in_logos_dir(team_name)
        if not resolved_path and fallback_text:
            resolved_path = find_logo_in_logos_dir(fallback_text)

    if resolved_path and resolved_path.exists():
        try:
            img = Image.open(resolved_path).convert("RGBA")
            
            # Trim transparent margins to obtain true content bounding box
            bbox = img.getbbox()
            if bbox:
                img = img.crop(bbox)

            # Fit proportionally inside inner safe area (size minus padding)
            inner_w = max(10, size[0] - padding * 2)
            inner_h = max(10, size[1] - padding * 2)
            img.thumbnail((inner_w, inner_h), Image.Resampling.LANCZOS)
            
            # Center thumbnail on target size canvas
            canvas = Image.new("RGBA", size, (0, 0, 0, 0))
            offset = ((size[0] - img.width) // 2, (size[1] - img.height) // 2)
            canvas.paste(img, offset, img)
            return canvas
        except Exception as e:
            print(f"Error loading logo {resolved_path}: {e}")

    # Generate modern circular badge as fallback
    canvas = Image.new("RGBA", size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(canvas)
    
    # Outer circle ring
    r = min(size) // 2 - 4
    cx, cy = size[0] // 2, size[1] // 2
    
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(22, 27, 40, 240), outline=primary_color, width=4)
    
    # Fallback text inside badge
    try:
        from template_accv.utils.fonts import get_font
        font = get_font("HEADER", int(r * 0.9))
    except Exception:
        font = ImageFont.load_default()
        
    tw, th = get_text_dimensions(fallback_text[:3].upper(), font)
    draw.text((cx - tw // 2, cy - th // 2 - 4), fallback_text[:3].upper(), font=font, fill=(255, 255, 255, 255))
    
    return canvas


def create_glowing_badge(
    text: str,
    font: ImageFont.FreeTypeFont,
    bg_color: Tuple[int, int, int, int],
    text_color: Tuple[int, int, int, int],
    padding: Tuple[int, int] = (24, 12)
) -> Image.Image:
    """Create a pill-style badge with text."""
    tw, th = get_text_dimensions(text, font)
    w = tw + padding[0] * 2
    h = th + padding[1] * 2
    
    badge = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(badge)
    
    draw.rounded_rectangle([0, 0, w, h], radius=h // 2, fill=bg_color)
    draw.text((padding[0], padding[1] - 2), text, font=font, fill=text_color)
    
    return badge
