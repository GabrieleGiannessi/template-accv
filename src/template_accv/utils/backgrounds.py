"""
Background image manager and processor.
Handles loading specific background images, selecting random emotion-based backgrounds,
falling back to default assets/backgrounds/ images, resizing to canvas dimensions,
and applying contrast filters.
"""

import math
import os
import random
import unicodedata
from pathlib import Path
from typing import Optional, Tuple
from PIL import Image, ImageEnhance, ImageOps

from template_accv.config import BASE_DIR, BACKGROUNDS_DIR, Colors


def find_emotion_directory(emotion_name: str) -> Optional[Path]:
    """Find matching category folder in backgrounds."""
    if not emotion_name or not BACKGROUNDS_DIR.exists():
        return None

    def normalize(value: str) -> str:
        return "".join(
            char for char in unicodedata.normalize("NFKD", value.strip().lower())
            if not unicodedata.combining(char)
        ).replace("_", " ").replace("-", " ")

    clean_target = normalize(emotion_name)
    if clean_target in {"foto squadra", "foto di gruppo", "gruppo"}:
        group_dir = BACKGROUNDS_DIR / "foto_di_gruppo"
        if group_dir.is_dir():
            return group_dir

    themes_dir = BACKGROUNDS_DIR / "temi"
    search_roots = [themes_dir, BACKGROUNDS_DIR]
    for root in search_roots:
        if not root.is_dir():
            continue
        for entry in sorted(root.iterdir()):
            if entry.is_dir() and normalize(entry.name.removeprefix("tema ")) == clean_target:
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
    standard_dir = BACKGROUNDS_DIR / "standard"
    standard_image = get_random_image_from_dir(standard_dir)
    if standard_image:
        return standard_image

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
    dark_overlay_alpha: int = 150,
    bg_zoom: float = 1.0,
    bg_x: float = 0.5,
    bg_y: float = 0.5,
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
        elif (BACKGROUNDS_DIR / bg_path).exists() and (BACKGROUNDS_DIR / bg_path).is_file():
            selected_path = BACKGROUNDS_DIR / bg_path
        elif (BASE_DIR / bg_path).exists() and (BASE_DIR / bg_path).is_file():
            selected_path = BASE_DIR / bg_path

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
            # Lift genuinely dark covers gently, preserving contrast and detail.
            luminance = ImageOps.grayscale(img).resize((256, 256)).histogram()
            mean_luma = sum(i * count for i, count in enumerate(luminance)) / (256 * 256)
            if mean_luma < 82:
                img = ImageEnhance.Brightness(img).enhance(min(1.22, 1.0 + (82 - mean_luma) / 420))
            zoom = max(1.0, min(float(bg_zoom), 2.5))
            img = ImageOps.fit(img, target_size, method=Image.Resampling.LANCZOS)
            if zoom > 1:
                img = img.resize((int(target_size[0] * zoom), int(target_size[1] * zoom)), Image.Resampling.LANCZOS)
            x_pos, y_pos = max(0, min(1, float(bg_x))), max(0, min(1, float(bg_y)))
            left = int((img.width - target_size[0]) * x_pos)
            top = int((img.height - target_size[1]) * y_pos)
            img = img.crop((left, top, left + target_size[0], top + target_size[1]))
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
