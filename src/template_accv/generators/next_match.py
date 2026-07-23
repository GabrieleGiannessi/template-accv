"""
Next Match graphic generator (Prossima Partita / Matchday promo).
Renders 1080x1080 (Post) and 1080x1920 (Story) promo graphics for upcoming matches.
"""

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
    def __init__(self, next_match: NextMatch, aspect_ratio: AspectRatio = AspectRatio.POST):
        super().__init__(aspect_ratio)
        self.data = next_match

    def render(self) -> Image.Image:
        """Render full Next Match promo graphic."""
        # 1. Header
        self.draw_top_header(self.data.tournament, self.data.matchday)

        # 2. Main Title Banner ("PROSSIMA PARTITA")
        is_story = (self.aspect_ratio == AspectRatio.STORY)
        y_title = 140 if not is_story else 230
        
        font_title = get_font("HEADER", 68 if not is_story else 80)
        title_str = "MATCHDAY"
        tw, th = get_text_dimensions(title_str, font_title)
        self.draw.text(
            ((self.width - tw) // 2, y_title),
            title_str,
            font=font_title,
            fill=Colors.TEXT_WHITE
        )
        
        # Subtitle "PROSSIMA PARTITA"
        font_sub = get_font("BODY", 22 if not is_story else 26)
        sub_str = "PROSSIMA PARTITA"
        tw_sub, th_sub = get_text_dimensions(sub_str, font_sub)
        self.draw.text(
            ((self.width - tw_sub) // 2, y_title + th + 4),
            sub_str,
            font=font_sub,
            fill=Colors.ACCENT_CYAN
        )

        # 3. Matchup Card (Teams & VS badge)
        y_card = 280 if not is_story else 420
        card_h = 320 if not is_story else 380
        margin_x = 50
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
        logo_size = (140, 140) if not is_story else (160, 160)
        home_logo = load_team_logo(
            self.data.home_team.logo_path,
            size=logo_size,
            fallback_text=self.data.home_team.short_name,
            primary_color=self.data.home_team.primary_color or Colors.DEFAULT_HOME_COLOR
        )
        away_logo = load_team_logo(
            self.data.away_team.logo_path,
            size=logo_size,
            fallback_text=self.data.away_team.short_name,
            primary_color=self.data.away_team.primary_color or Colors.DEFAULT_AWAY_COLOR
        )

        logo_y = y_card + 40 if not is_story else y_card + 50
        home_logo_x = margin_x + 60
        away_logo_x = self.width - margin_x - 60 - logo_size[0]

        self.image.paste(home_logo, (home_logo_x, logo_y), home_logo)
        self.image.paste(away_logo, (away_logo_x, logo_y), away_logo)

        # Team Names
        font_team = get_font("BODY", 22 if not is_story else 26)
        ht_w, _ = get_text_dimensions(self.data.home_team.name, font_team)
        ht_x = home_logo_x + (logo_size[0] - ht_w) // 2
        self.draw.text((ht_x, logo_y + logo_size[1] + 15), self.data.home_team.name, font=font_team, fill=Colors.TEXT_WHITE)

        at_w, _ = get_text_dimensions(self.data.away_team.name, font_team)
        at_x = away_logo_x + (logo_size[0] - at_w) // 2
        self.draw.text((at_x, logo_y + logo_size[1] + 15), self.data.away_team.name, font=font_team, fill=Colors.TEXT_WHITE)

        # Central VS Pill Badge
        vs_w, vs_h = (110, 110) if not is_story else (130, 130)
        vs_x = (self.width - vs_w) // 2
        vs_y = y_card + (card_h - vs_h) // 2 - 10

        self.draw.ellipse(
            [vs_x, vs_y, vs_x + vs_w, vs_y + vs_h],
            fill=Colors.ACCENT_CARD_BG,
            outline=Colors.ACCENT_CYAN,
            width=3
        )

        font_vs = get_font("HEADER", 56 if not is_story else 64)
        draw_text_centered(
            self.draw,
            "VS",
            font_vs,
            (vs_x, vs_y, vs_x + vs_w, vs_y + vs_h),
            fill=Colors.TEXT_WHITE
        )

        # 4. Event Details Card (Date, Time, Location)
        y_info = y_card + card_h + 35 if not is_story else y_card + card_h + 60
        info_h = 240 if not is_story else 380
        info_bbox = (margin_x, y_info, self.width - margin_x, y_info + info_h)

        draw_rounded_card(
            self.draw,
            info_bbox,
            radius=20,
            fill=Colors.CARD_BG,
            border=Colors.CARD_BORDER,
            border_width=2
        )

        # Details list
        font_info_label = get_font("HEADER", 28 if not is_story else 34)
        font_info_val = get_font("BODY", 22 if not is_story else 26)

        details = [
            ("📅  DATA", self.data.date or "Da Definire"),
            ("⏰  ORARIO", self.data.time or "21:00"),
            ("🏟️  CAMPO", self.data.location or self.data.field_name or "Centro Sportivo"),
        ]

        item_height = 65 if not is_story else 90
        start_y = y_info + (25 if not is_story else 40)

        for i, (label, val) in enumerate(details):
            curr_y = start_y + i * item_height
            # Label
            self.draw.text((margin_x + 50, curr_y), label, font=font_info_label, fill=Colors.ACCENT_CYAN)
            # Value right aligned or spaced
            val_w, _ = get_text_dimensions(val, font_info_val)
            self.draw.text((self.width - margin_x - 50 - val_w, curr_y + 4), val, font=font_info_val, fill=Colors.TEXT_WHITE)

            if i < len(details) - 1:
                # Divider line
                self.draw.line(
                    [margin_x + 40, curr_y + item_height - 10, self.width - margin_x - 40, curr_y + item_height - 10],
                    fill=Colors.CARD_BORDER,
                    width=1
                )

        # 5. Footer Info
        self.draw_footer_brand(f"NON MANCARE!  •  {self.data.tournament}")

        return self.image
