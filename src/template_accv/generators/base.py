"""
Base Graphic Generator canvas builder.
Handles canvas creation, standard header/footer layout elements, and ratio handling.
"""

from typing import Tuple
from PIL import Image, ImageDraw

from template_accv.config import AspectRatio, DIMENSIONS, Colors
from template_accv.utils.fonts import get_font
from template_accv.utils.image_fx import (
    create_gradient_background,
    draw_rounded_card,
    draw_text_centered,
    get_text_dimensions,
)


class BaseGraphicGenerator:
    def __init__(self, aspect_ratio: AspectRatio = AspectRatio.POST):
        self.aspect_ratio = aspect_ratio
        self.width, self.height = DIMENSIONS[aspect_ratio]
        
        # Base canvas setup with rich dark gradient
        self.image = create_gradient_background(
            self.width,
            self.height,
            top_color=Colors.BG_DARK,
            bottom_color=Colors.BG_GRADIENT_END,
            radial_spotlight=True
        )
        self.draw = ImageDraw.Draw(self.image)

    def draw_top_header(self, tournament: str, matchday: str):
        """Draw top tournament tag and matchday pill badge."""
        y_top = 50 if self.aspect_ratio == AspectRatio.POST else 120
        
        # Tournament category subtitle
        font_tourn = get_font("REGULAR", 22 if self.aspect_ratio == AspectRatio.POST else 26)
        tw, th = get_text_dimensions(tournament.upper(), font_tourn)
        self.draw.text(
            ((self.width - tw) // 2, y_top),
            tournament.upper(),
            font=font_tourn,
            fill=Colors.ACCENT_CYAN
        )
        
        # Matchday title below
        font_md = get_font("HEADER", 48 if self.aspect_ratio == AspectRatio.POST else 56)
        tw_md, th_md = get_text_dimensions(matchday.upper(), font_md)
        self.draw.text(
            ((self.width - tw_md) // 2, y_top + 34),
            matchday.upper(),
            font=font_md,
            fill=Colors.TEXT_WHITE
        )

    def draw_footer_brand(self, location_date_info: str = ""):
        """Draw footer with team tagline 'A.C. C.V. • OFFICIAL MATCH GRAPHIC' and match location/date."""
        y_foot = self.height - (70 if self.aspect_ratio == AspectRatio.POST else 120)
        
        if location_date_info:
            font_loc = get_font("REGULAR", 20 if self.aspect_ratio == AspectRatio.POST else 24)
            tw, th = get_text_dimensions(location_date_info, font_loc)
            self.draw.text(
                ((self.width - tw) // 2, y_foot - 34),
                location_date_info,
                font=font_loc,
                fill=Colors.TEXT_MUTED
            )
            
        font_brand = get_font("BODY", 18 if self.aspect_ratio == AspectRatio.POST else 22)
        brand_text = "A.C.C.V.  •  OFFICIAL MATCHDAY"
        tw, th = get_text_dimensions(brand_text, font_brand)
        self.draw.text(
            ((self.width - tw) // 2, y_foot),
            brand_text,
            font=font_brand,
            fill=Colors.ACCENT_CYAN
        )

    def save(self, filepath: str) -> str:
        """Save generated graphic to disk."""
        self.image.save(filepath, "PNG", quality=95)
        return filepath
