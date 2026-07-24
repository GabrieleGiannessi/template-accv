"""
Base Graphic Generator canvas builder.
Handles background rendering, layout scaling for multiple aspect ratios (9:16, 4:3, 16:9, 1:1, 4:5),
standard headers, and brand footers.
"""

from typing import Optional, Tuple
from PIL import Image, ImageDraw

from template_accv.config import AspectRatio, DIMENSIONS, Colors
from template_accv.utils.fonts import get_font
from template_accv.utils.image_fx import (
    draw_rounded_card,
    draw_text_centered,
    get_text_dimensions,
)
from template_accv.utils.backgrounds import load_and_process_background


class BaseGraphicGenerator:
    def __init__(
        self,
        aspect_ratio: AspectRatio = AspectRatio.RATIO_9_16,
        bg_path: Optional[str] = None,
        emotion: Optional[str] = None,
        contrast_factor: float = 1.0,
        remove_contrast: bool = False,
        dark_overlay_alpha: int = 150
    ):
        self.aspect_ratio = aspect_ratio
        self.width, self.height = DIMENSIONS.get(aspect_ratio, DIMENSIONS[AspectRatio.RATIO_9_16])
        
        # Load background with emotion logic, fallback, and contrast filter
        self.image = load_and_process_background(
            target_size=(self.width, self.height),
            bg_path=bg_path,
            emotion=emotion,
            contrast_factor=contrast_factor,
            remove_contrast=remove_contrast,
            dark_overlay_alpha=dark_overlay_alpha
        )
        self.draw = ImageDraw.Draw(self.image)

    @property
    def is_vertical(self) -> bool:
        """True for 9:16 or 4:5 vertical formats."""
        return self.height > self.width

    @property
    def is_wide_landscape(self) -> bool:
        """True for 16:9 or 4:3 landscape formats."""
        return self.width > self.height

    def draw_top_header(self, tournament: str, matchday: str):
        """Draw top tournament tag and matchday title."""
        if self.is_vertical:
            y_top = 80 if self.aspect_ratio == AspectRatio.RATIO_9_16 else 55
        elif self.is_wide_landscape:
            y_top = 40
        else:  # Square 1:1
            y_top = 50

        # Tournament category subtitle
        font_size_tourn = 26 if self.is_vertical else 22
        font_tourn = get_font("REGULAR", font_size_tourn)
        tw, th = get_text_dimensions(tournament.upper(), font_tourn)
        self.draw.text(
            ((self.width - tw) // 2, y_top),
            tournament.upper(),
            font=font_tourn,
            fill=Colors.ACCENT_CYAN
        )
        
        # Matchday title below
        font_size_md = 56 if self.is_vertical else 46
        font_md = get_font("HEADER", font_size_md)
        tw_md, th_md = get_text_dimensions(matchday.upper(), font_md)
        self.draw.text(
            ((self.width - tw_md) // 2, y_top + 32),
            matchday.upper(),
            font=font_md,
            fill=Colors.TEXT_WHITE
        )

    def draw_footer_brand(self, location_date_info: str = ""):
        """Draw footer with team tagline and match location/date."""
        if self.is_vertical:
            y_foot = self.height - (100 if self.aspect_ratio == AspectRatio.RATIO_9_16 else 75)
        else:
            y_foot = self.height - 60
        
        if location_date_info:
            font_loc = get_font("REGULAR", 22 if self.is_vertical else 19)
            tw, th = get_text_dimensions(location_date_info, font_loc)
            self.draw.text(
                ((self.width - tw) // 2, y_foot - 32),
                location_date_info,
                font=font_loc,
                fill=Colors.TEXT_MUTED
            )
            
        font_brand = get_font("BODY", 20 if self.is_vertical else 17)
        brand_text = "A.C.C.V.  •  OFFICIAL MATCHDAY"
        tw, th = get_text_dimensions(brand_text, font_brand)
        self.draw.text(
            ((self.width - tw) // 2, y_foot),
            brand_text,
            font=font_brand,
            fill=Colors.ACCENT_CYAN
        )

    def save(self, filepath: str) -> str:
        """Render and save generated graphic to disk."""
        rendered = self.render()
        rendered.save(filepath, "PNG", quality=95)
        return filepath
