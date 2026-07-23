"""
MVP / Player of the Match graphic generator.
Renders 1080x1080 (Post) and 1080x1920 (Story) graphics for player of the match stats.
"""

from PIL import Image

from template_accv.config import AspectRatio, Colors
from template_accv.models import MVP
from template_accv.generators.base import BaseGraphicGenerator
from template_accv.utils.fonts import get_font
from template_accv.utils.image_fx import (
    draw_rounded_card,
    draw_text_centered,
    get_text_dimensions,
)


class MVPGenerator(BaseGraphicGenerator):
    def __init__(self, mvp: MVP, aspect_ratio: AspectRatio = AspectRatio.POST):
        super().__init__(aspect_ratio)
        self.data = mvp

    def render(self) -> Image.Image:
        """Render MVP graphic."""
        is_story = (self.aspect_ratio == AspectRatio.STORY)
        
        # 1. Top Header
        matchday_info = f"MATCH vs {self.data.match_opponent.upper()}" if self.data.match_opponent else "GIORNATA MATCH"
        self.draw_top_header("MVP ACCV", matchday_info)

        # 2. Main Title Banner ("MIGLIORE IN CAMPO")
        y_title = 140 if not is_story else 230
        
        font_title = get_font("HEADER", 68 if not is_story else 80)
        title_str = "MIGLIORE IN CAMPO"
        tw, th = get_text_dimensions(title_str, font_title)
        self.draw.text(
            ((self.width - tw) // 2, y_title),
            title_str,
            font=font_title,
            fill=Colors.TEXT_GOLD
        )

        # 3. Main Player Showcase Card
        y_card = 260 if not is_story else 400
        card_h = 280 if not is_story else 340
        margin_x = 50
        card_bbox = (margin_x, y_card, self.width - margin_x, y_card + card_h)

        # Gold-bordered card
        draw_rounded_card(
            self.draw,
            card_bbox,
            radius=24,
            fill=(30, 26, 18, 230),
            border=Colors.ACCENT_GOLD,
            border_width=3
        )

        # Player Jersey # Pill Badge
        num_w, num_h = (120, 120) if not is_story else (140, 140)
        num_x = margin_x + 50
        num_y = y_card + (card_h - num_h) // 2
        
        self.draw.rounded_rectangle(
            [num_x, num_y, num_x + num_w, num_y + num_h],
            radius=24,
            fill=Colors.ACCENT_GOLD
        )
        
        font_num = get_font("HEADER", 76 if not is_story else 88)
        draw_text_centered(
            self.draw,
            f"#{self.data.jersey_number}",
            font_num,
            (num_x, num_y, num_x + num_w, num_y + num_h),
            fill=Colors.TEXT_DARK
        )

        # Player Name & Position
        name_x = num_x + num_w + 40
        
        font_name = get_font("BODY", 36 if not is_story else 42)
        font_pos = get_font("REGULAR", 22 if not is_story else 26)
        
        self.draw.text(
            (name_x, y_card + (60 if not is_story else 80)),
            self.data.player_name.upper(),
            font=font_name,
            fill=Colors.TEXT_WHITE
        )
        
        self.draw.text(
            (name_x, y_card + (115 if not is_story else 145)),
            f"RUOLO: {self.data.position.upper()}",
            font=font_pos,
            fill=Colors.TEXT_GOLD
        )

        # 4. Stats Grid Card
        y_stats = y_card + card_h + 35 if not is_story else y_card + card_h + 60
        stats_h = 240 if not is_story else 380
        stats_bbox = (margin_x, y_stats, self.width - margin_x, y_stats + stats_h)

        draw_rounded_card(
            self.draw,
            stats_bbox,
            radius=20,
            fill=Colors.CARD_BG,
            border=Colors.CARD_BORDER,
            border_width=2
        )

        # Stats Items
        stats = [
            ("⚽  GOL SEGNATI", str(self.data.goals)),
            ("👟  ASSIST", str(self.data.assists)),
            ("⭐  VOTO PAGELLA", str(self.data.rating)),
        ]
        
        if self.data.saves > 0:
            stats.insert(2, ("🧤  PARATE", str(self.data.saves)))

        item_height = 65 if not is_story else 85
        start_y = y_stats + (25 if not is_story else 40)

        for i, (label, val) in enumerate(stats):
            curr_y = start_y + i * item_height
            font_stat_label = get_font("HEADER", 28 if not is_story else 34)
            font_stat_val = get_font("HEADER", 36 if not is_story else 44)
            
            self.draw.text((margin_x + 50, curr_y), label, font=font_stat_label, fill=Colors.TEXT_WHITE)
            
            val_w, _ = get_text_dimensions(val, font_stat_val)
            self.draw.text((self.width - margin_x - 60 - val_w, curr_y - 4), val, font=font_stat_val, fill=Colors.ACCENT_GOLD)

            if i < len(stats) - 1:
                self.draw.line(
                    [margin_x + 40, curr_y + item_height - 10, self.width - margin_x - 40, curr_y + item_height - 10],
                    fill=Colors.CARD_BORDER,
                    width=1
                )

        # 5. Footer Info
        self.draw_footer_brand("CONGRATULAZIONI AL GIOCATORE!")

        return self.image
