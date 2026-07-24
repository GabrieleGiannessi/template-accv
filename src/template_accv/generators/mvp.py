"""
MVP / Player of the Match graphic generator.
Renders player of the match stats across social media aspect ratios (9:16, 4:3, 16:9, 1:1, 4:5).
"""

from typing import Optional
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
    def __init__(
        self,
        mvp: MVP,
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
        self.data = mvp

    def render(self) -> Image.Image:
        """Render MVP graphic."""
        # 1. Header
        matchday_info = f"MATCH vs {self.data.match_opponent.upper()}" if self.data.match_opponent else "GIORNATA MATCH"
        self.draw_top_header("MVP ACCV", matchday_info)

        # 2. Main Title Banner ("MIGLIORE IN CAMPO")
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
        title_str = "MIGLIORE IN CAMPO"
        tw, th = get_text_dimensions(title_str, font_title)
        self.draw.text(
            ((self.width - tw) // 2, y_title),
            title_str,
            font=font_title,
            fill=Colors.TEXT_GOLD
        )

        # 3. Main Player Showcase Card
        margin_x = int(self.width * 0.05)
        y_card = 400 if self.aspect_ratio == AspectRatio.RATIO_9_16 else (200 if self.is_wide_landscape else 260)
        card_h = 340 if self.aspect_ratio == AspectRatio.RATIO_9_16 else (260 if self.is_wide_landscape else 280)
        
        card_bbox = (margin_x, y_card, self.width - margin_x, y_card + card_h)

        draw_rounded_card(
            self.draw,
            card_bbox,
            radius=24,
            fill=(30, 26, 18, 230),
            border=Colors.ACCENT_GOLD,
            border_width=3
        )

        # Player Jersey # Badge
        num_sz = int(card_h * 0.44)
        num_x = margin_x + int(self.width * 0.05)
        num_y = y_card + (card_h - num_sz) // 2
        
        self.draw.rounded_rectangle(
            [num_x, num_y, num_x + num_sz, num_y + num_sz],
            radius=20,
            fill=Colors.ACCENT_GOLD
        )
        
        font_num = get_font("HEADER", 84 if self.is_vertical else 68)
        draw_text_centered(
            self.draw,
            f"#{self.data.jersey_number}",
            font_num,
            (num_x, num_y, num_x + num_sz, num_y + num_sz),
            fill=Colors.TEXT_DARK
        )

        # Player Name & Position
        name_x = num_x + num_sz + int(self.width * 0.04)
        font_name = get_font("BODY", 38 if self.is_vertical else 30)
        font_pos = get_font("REGULAR", 24 if self.is_vertical else 20)
        
        self.draw.text(
            (name_x, y_card + int(card_h * 0.25)),
            self.data.player_name.upper(),
            font=font_name,
            fill=Colors.TEXT_WHITE
        )
        
        self.draw.text(
            (name_x, y_card + int(card_h * 0.55)),
            f"RUOLO: {self.data.position.upper()}",
            font=font_pos,
            fill=Colors.TEXT_GOLD
        )

        # 4. Stats Grid Card
        y_stats = y_card + card_h + 30
        stats_h = 360 if self.aspect_ratio == AspectRatio.RATIO_9_16 else (220 if self.is_wide_landscape else 260)
        stats_bbox = (margin_x, y_stats, self.width - margin_x, y_stats + stats_h)

        draw_rounded_card(
            self.draw,
            stats_bbox,
            radius=20,
            fill=Colors.CARD_BG,
            border=Colors.CARD_BORDER,
            border_width=2
        )

        stats = [
            ("⚽  GOL SEGNATI", str(self.data.goals)),
            ("👟  ASSIST", str(self.data.assists)),
            ("⭐  VOTO PAGELLA", str(self.data.rating)),
        ]
        if self.data.saves > 0:
            stats.insert(2, ("🧤  PARATE", str(self.data.saves)))

        item_height = stats_h // len(stats)
        start_y = y_stats + 15

        for i, (label, val) in enumerate(stats):
            curr_y = start_y + i * item_height
            font_stat_label = get_font("HEADER", 32 if self.is_vertical else 26)
            font_stat_val = get_font("HEADER", 42 if self.is_vertical else 34)
            
            self.draw.text((margin_x + 40, curr_y), label, font=font_stat_label, fill=Colors.TEXT_WHITE)
            
            val_w, _ = get_text_dimensions(val, font_stat_val)
            self.draw.text((self.width - margin_x - 50 - val_w, curr_y - 4), val, font=font_stat_val, fill=Colors.ACCENT_GOLD)

            if i < len(stats) - 1:
                self.draw.line(
                    [margin_x + 30, curr_y + item_height - 5, self.width - margin_x - 30, curr_y + item_height - 5],
                    fill=Colors.CARD_BORDER,
                    width=1
                )

        # 5. Footer Info
        self.draw_footer_brand("CONGRATULAZIONI AL GIOCATORE!")

        return self.image
