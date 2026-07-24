"""
Next Match graphic generator (Prossima Partita / Matchday promo).
Renders promo graphics across social media aspect ratios (9:16, 4:3, 16:9, 1:1, 4:5).
"""

from typing import Optional
from PIL import Image

from template_accv.config import AspectRatio, Colors
from template_accv.models import NextMatch
from template_accv.generators.base import BaseGraphicGenerator
from template_accv.utils.fonts import get_font
from template_accv.utils.image_fx import (
    draw_rounded_card,
    draw_text_centered,
    get_text_dimensions,
    load_team_logo,
)


class NextMatchGenerator(BaseGraphicGenerator):
    def __init__(
        self,
        next_match: NextMatch,
        aspect_ratio: AspectRatio = AspectRatio.RATIO_9_16,
        bg_path: Optional[str] = None,
        emotion: Optional[str] = None,
        contrast_factor: float = 1.0,
        remove_contrast: bool = False
    ):
        super().__init__(
            aspect_ratio=aspect_ratio,
            bg_path=bg_path,
            emotion=emotion,
            contrast_factor=contrast_factor,
            remove_contrast=remove_contrast
        )
        self.data = next_match

    def render(self) -> Image.Image:
        """Render full Next Match promo graphic."""
        # 1. Header
        self.draw_top_header(self.data.tournament, self.data.matchday)

        # 2. Main Title Banner ("PROSSIMA PARTITA")
        if self.aspect_ratio == AspectRatio.RATIO_9_16:
            y_title = 220
            font_sz = 80
        elif self.is_wide_landscape:
            y_title = 110
            font_sz = 60
        else:
            y_title = 140
            font_sz = 68
        
        font_title = get_font("HEADER", font_sz)
        title_str = "MATCHDAY"
        tw, th = get_text_dimensions(title_str, font_title)
        self.draw.text(
            ((self.width - tw) // 2, y_title),
            title_str,
            font=font_title,
            fill=Colors.TEXT_WHITE
        )
        
        font_sub = get_font("BODY", 24 if self.is_vertical else 20)
        sub_str = "PROSSIMA PARTITA"
        tw_sub, th_sub = get_text_dimensions(sub_str, font_sub)
        self.draw.text(
            ((self.width - tw_sub) // 2, y_title + th + 4),
            sub_str,
            font=font_sub,
            fill=Colors.ACCENT_CYAN
        )

        # 3. Matchup Card (Teams & VS badge)
        margin_x = int(self.width * 0.05)
        y_card = 400 if self.aspect_ratio == AspectRatio.RATIO_9_16 else (200 if self.is_wide_landscape else 270)
        card_h = 380 if self.aspect_ratio == AspectRatio.RATIO_9_16 else (280 if self.is_wide_landscape else 320)
        
        card_bbox = (margin_x, y_card, self.width - margin_x, y_card + card_h)

        draw_rounded_card(
            self.draw,
            card_bbox,
            radius=24,
            fill=Colors.CARD_BG,
            border=Colors.CARD_BORDER,
            border_width=2
        )

        # Draw Team Logos
        logo_sz = int(card_h * 0.44)
        logo_size = (logo_sz, logo_sz)
        
        home_logo = load_team_logo(
            logo_path=self.data.home_team.logo_path,
            team_name=self.data.home_team.name,
            size=logo_size,
            fallback_text=self.data.home_team.short_name,
            primary_color=self.data.home_team.primary_color or Colors.DEFAULT_HOME_COLOR
        )
        away_logo = load_team_logo(
            logo_path=self.data.away_team.logo_path,
            team_name=self.data.away_team.name,
            size=logo_size,
            fallback_text=self.data.away_team.short_name,
            primary_color=self.data.away_team.primary_color or Colors.DEFAULT_AWAY_COLOR
        )

        logo_y = y_card + int(card_h * 0.12)
        home_logo_x = margin_x + int(self.width * 0.06)
        away_logo_x = self.width - margin_x - int(self.width * 0.06) - logo_size[0]

        self.image.paste(home_logo, (home_logo_x, logo_y), home_logo)
        self.image.paste(away_logo, (away_logo_x, logo_y), away_logo)

        # Team Names
        font_team = get_font("BODY", 24 if self.is_vertical else 20)
        ht_w, _ = get_text_dimensions(self.data.home_team.name, font_team)
        ht_x = home_logo_x + (logo_size[0] - ht_w) // 2
        self.draw.text((ht_x, logo_y + logo_size[1] + 12), self.data.home_team.name, font=font_team, fill=Colors.TEXT_WHITE)

        at_w, _ = get_text_dimensions(self.data.away_team.name, font_team)
        at_x = away_logo_x + (logo_size[0] - at_w) // 2
        self.draw.text((at_x, logo_y + logo_size[1] + 12), self.data.away_team.name, font=font_team, fill=Colors.TEXT_WHITE)

        # Central VS Pill Badge
        vs_sz = int(card_h * 0.35)
        vs_x = (self.width - vs_sz) // 2
        vs_y = y_card + (card_h - vs_sz) // 2 - 5

        self.draw.ellipse(
            [vs_x, vs_y, vs_x + vs_sz, vs_y + vs_sz],
            fill=Colors.ACCENT_CARD_BG,
            outline=Colors.ACCENT_CYAN,
            width=3
        )

        font_vs = get_font("HEADER", 64 if self.is_vertical else 52)
        draw_text_centered(
            self.draw,
            "VS",
            font_vs,
            (vs_x, vs_y, vs_x + vs_sz, vs_y + vs_sz),
            fill=Colors.TEXT_WHITE
        )

        # 4. Event Details Card (Date, Time, Location)
        y_info = y_card + card_h + 30
        info_h = 360 if self.aspect_ratio == AspectRatio.RATIO_9_16 else (220 if self.is_wide_landscape else 260)
        info_bbox = (margin_x, y_info, self.width - margin_x, y_info + info_h)

        draw_rounded_card(
            self.draw,
            info_bbox,
            radius=20,
            fill=Colors.CARD_BG,
            border=Colors.CARD_BORDER,
            border_width=2
        )

        details = [
            ("📅  DATA", self.data.date or "Da Definire"),
            ("⏰  ORARIO", self.data.time or "21:00"),
            ("🏟️  CAMPO", self.data.location or self.data.field_name or "Centro Sportivo"),
        ]

        font_info_label = get_font("HEADER", 32 if self.is_vertical else 26)
        font_info_val = get_font("BODY", 24 if self.is_vertical else 20)

        item_height = info_h // len(details)
        start_y = y_info + 15

        for i, (label, val) in enumerate(details):
            curr_y = start_y + i * item_height
            self.draw.text((margin_x + 40, curr_y), label, font=font_info_label, fill=Colors.ACCENT_CYAN)
            val_w, _ = get_text_dimensions(val, font_info_val)
            self.draw.text((self.width - margin_x - 40 - val_w, curr_y + 4), val, font=font_info_val, fill=Colors.TEXT_WHITE)

            if i < len(details) - 1:
                self.draw.line(
                    [margin_x + 30, curr_y + item_height - 5, self.width - margin_x - 30, curr_y + item_height - 5],
                    fill=Colors.CARD_BORDER,
                    width=1
                )

        # 5. Footer Info
        self.draw_footer_brand(f"NON MANCARE!  •  {self.data.tournament}")

        return self.image
