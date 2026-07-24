"""
Background image manager and processor.
Handles loading specific background images, selecting random emotion-based backgrounds,
falling back to default assets/backgrounds/ images, resizing to canvas dimensions,
and applying contrast filters.
"""

import math
import os
import random
from pathlib import Path
from typing import Optional, Tuple
from PIL import Image, ImageEnhance, ImageOps

from template_accv.config import BACKGROUNDS_DIR, Colors


def find_emotion_directory(emotion_name: str) -> Optional[Path]:
    """Find matching category folder in backgrounds."""
    if not emotion_name or not BACKGROUNDS_DIR.exists():
        return None

    clean_target = emotion_name.strip().lower()
    
    # Direct match or normalized match
    for entry in BACKGROUNDS_DIR.iterdir():
        if entry.is_dir():
            dir_name = entry.name.lower()
            if (clean_target in dir_name) or (dir_name in clean_target):
                return entry
            # Handling accented/normalized Italian emotion names e.g. felicita -> felicità
            if clean_target.replace("a", "à") in dir_name or clean_target.replace("e", "è") in dir_name:
                return entry

    return None


def get_random_image_from_dir(directory: Path) -> Optional[Path]:
    """Return a random image file path from directory."""
    if not directory or not directory.exists():
        return None
    valid_exts = {".jpg", ".jpeg", ".png", ".webp"}
    images = [p for p in directory.iterdir() if p.suffix.lower() in valid_exts]
    if images:
        return random.choice(images)
    return None


def get_default_background_path() -> Optional[Path]:
    """
    Returns default background path from assets/backgrounds/.
    Defaults to std.JPG or any image inside assets/backgrounds/.
    """
    std_path = BACKGROUNDS_DIR / "std.JPG"
    if std_path.exists():
        return std_path

    # Fallback to any image file in BACKGROUNDS_DIR
    random_bg = get_random_image_from_dir(BACKGROUNDS_DIR)
    if random_bg:
        return random_bg
    return None


def load_and_process_background(
    target_size: Tuple[int, int],
    bg_path: Optional[str] = None,
    emotion: Optional[str] = None,
    contrast_factor: float = 1.0,
    remove_contrast: bool = False,
    dark_overlay_alpha: int = 150
) -> Image.Image:
    """
    Load background image based on user rules, crop/scale to target_size,
    apply contrast filters, and composite a dark gradient overlay.
    """
    target_w, target_h = target_size
    selected_path: Optional[Path] = None

    # 1. Explicit path specified
    if bg_path:
        p = Path(bg_path)
        if p.exists() and p.is_file():
            selected_path = p

    # 2. Emotion category specified
    if not selected_path and emotion:
        emo_dir = find_emotion_directory(emotion)
        if emo_dir:
            selected_path = get_random_image_from_dir(emo_dir)

    # 3. Default behaviour fallback to assets/backgrounds/
    if not selected_path:
        selected_path = get_default_background_path()

    if selected_path and selected_path.exists():
        try:
            img = Image.open(selected_path).convert("RGBA")
            # Fit and crop image precisely to target canvas dimensions
            img = ImageOps.fit(img, target_size, method=Image.Resampling.LANCZOS)
        except Exception as e:
            print(f"Warning: Failed to load background image {selected_path}: {e}")
            img = Image.new("RGBA", target_size, Colors.BG_DARK)
    else:
        # Fallback dark canvas
        img = Image.new("RGBA", target_size, Colors.BG_DARK)

    # 4. Apply contrast filters
    if remove_contrast:
        contrast_factor = 0.4
    if contrast_factor != 1.0:
        # ImageEnhance works best on RGB
        rgb_img = img.convert("RGB")
        enhancer = ImageEnhance.Contrast(rgb_img)
        rgb_img = enhancer.enhance(contrast_factor)
        img = rgb_img.convert("RGBA")

    # 5. Composite Dark Overlay Mask for optimal text legibility
    dark_mask = Image.new("RGBA", target_size, (12, 16, 26, dark_overlay_alpha))
    img = Image.alpha_composite(img, dark_mask)

    return img
