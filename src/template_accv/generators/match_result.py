"""
Match Result graphic generator (Risultato Finale).
Renders 1080x1080 (Post) and 1080x1920 (Story) graphics for 5-a-side calcetto match scores.
"""

from PIL import Image, ImageDraw

from template_accv.config import AspectRatio, Colors
from template_accv.models import MatchResult
from template_accv.generators.base import BaseGraphicGenerator
from template_accv.utils.fonts import get_font
from template_accv.utils.image_fx import (
    draw_rounded_card,
    draw_text_centered,
    get_text_dimensions,
    load_team_logo,
)


class MatchResultGenerator(BaseGraphicGenerator):
    def __init__(self, match_result: MatchResult, aspect_ratio: AspectRatio = AspectRatio.POST):
        super().__init__(aspect_ratio)
        self.data = match_result

    def render(self) -> Image.Image:
        """Render full match result image."""
        # 1. Header (Tournament & Matchday)
        self.draw_top_header(self.data.tournament, self.data.matchday)
        
        # 2. Main Title Banner ("RISULTATO FINALE")
        is_story = (self.aspect_ratio == AspectRatio.STORY)
        y_title = 140 if not is_story else 230
        
        font_title = get_font("HEADER", 64 if not is_story else 72)
        title_str = "RISULTATO FINALE"
        tw, th = get_text_dimensions(title_str, font_title)
        self.draw.text(
            ((self.width - tw) // 2, y_title),
            title_str,
            font=font_title,
            fill=Colors.TEXT_WHITE
        )
        
        # Accent glowing bar under title
        bar_w = 160
        self.draw.rectangle(
            [(self.width - bar_w) // 2, y_title + th + 10, (self.width + bar_w) // 2, y_title + th + 14],
            fill=Colors.ACCENT_CYAN
        )

        # 3. Team Crests & Score Board Card
        y_card = 260 if not is_story else 400
        card_h = 320 if not is_story else 380
        margin_x = 50
        card_bbox = (margin_x, y_card, self.width - margin_x, y_card + card_h)
        
        # Draw Glassmorphism card background
        draw_rounded_card(
            self.draw,
            card_bbox,
            radius=24,
            fill=Colors.CARD_BG,
            border=Colors.CARD_BORDER,
            border_width=2
        )

        # Draw Team Logos
        logo_size = (130, 130) if not is_story else (150, 150)
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

        # Paste Logos inside card
        logo_y = y_card + 40 if not is_story else y_card + 50
        home_logo_x = margin_x + 60
        away_logo_x = self.width - margin_x - 60 - logo_size[0]
        
        self.image.paste(home_logo, (home_logo_x, logo_y), home_logo)
        self.image.paste(away_logo, (away_logo_x, logo_y), away_logo)

        # Team Names under logos
        font_team = get_font("BODY", 22 if not is_story else 26)
        
        # Home Team Name
        ht_w, ht_h = get_text_dimensions(self.data.home_team.name, font_team)
        ht_x = home_logo_x + (logo_size[0] - ht_w) // 2
        self.draw.text((ht_x, logo_y + logo_size[1] + 15), self.data.home_team.name, font=font_team, fill=Colors.TEXT_WHITE)

        # Away Team Name
        at_w, at_h = get_text_dimensions(self.data.away_team.name, font_team)
        at_x = away_logo_x + (logo_size[0] - at_w) // 2
        self.draw.text((at_x, logo_y + logo_size[1] + 15), self.data.away_team.name, font=font_team, fill=Colors.TEXT_WHITE)

        # Score Pill Badge in Center
        score_box_w = 240 if not is_story else 280
        score_box_h = 110 if not is_story else 130
        score_x = (self.width - score_box_w) // 2
        score_y = y_card + (card_h - score_box_h) // 2 - 10

        # Score Pill Background (Neon outline)
        self.draw.rounded_rectangle(
            [score_x, score_y, score_x + score_box_w, score_y + score_box_h],
            radius=20,
            fill=Colors.ACCENT_CARD_BG,
            outline=Colors.ACCENT_CYAN,
            width=3
        )

        font_score = get_font("HEADER", 84 if not is_story else 96)
        score_str = f"{self.data.home_score}  -  {self.data.away_score}"
        draw_text_centered(
            self.draw,
            score_str,
            font_score,
            (score_x, score_y, score_x + score_box_w, score_y + score_box_h),
            fill=Colors.TEXT_WHITE
        )

        # 4. Goal Scorers Section
        y_scorers = y_card + card_h + 30 if not is_story else y_card + card_h + 50
        scorers_h = 240 if not is_story else 420
        scorers_bbox = (margin_x, y_scorers, self.width - margin_x, y_scorers + scorers_h)

        draw_rounded_card(
            self.draw,
            scorers_bbox,
            radius=20,
            fill=Colors.CARD_BG,
            border=Colors.CARD_BORDER,
            border_width=2
        )

        # Section Header: "MARCATORI"
        font_sc_head = get_font("HEADER", 30 if not is_story else 36)
        sc_head_text = "⚽  MARCATORI  ⚽"
        tw, th = get_text_dimensions(sc_head_text, font_sc_head)
        self.draw.text(
            ((self.width - tw) // 2, y_scorers + 20),
            sc_head_text,
            font=font_sc_head,
            fill=Colors.ACCENT_CYAN
        )

        # Divider line
        self.draw.line(
            [margin_x + 30, y_scorers + 65, self.width - margin_x - 30, y_scorers + 65],
            fill=Colors.CARD_BORDER,
            width=1
        )

        # Home Scorers (Left Side) vs Away Scorers (Right Side)
        font_scorer_item = get_font("REGULAR", 20 if not is_story else 24)
        item_y_start = y_scorers + 85
        
        # Home Scorers
        home_x = margin_x + 40
        for i, s in enumerate(self.data.home_scorers):
            curr_y = item_y_start + i * 36
            if curr_y + 30 > y_scorers + scorers_h - 10:
                break
            txt = f"• {s.to_summary()}"
            self.draw.text((home_x, curr_y), txt, font=font_scorer_item, fill=Colors.TEXT_WHITE)

        # Away Scorers
        away_x = self.width // 2 + 40
        for i, s in enumerate(self.data.away_scorers):
            curr_y = item_y_start + i * 36
            if curr_y + 30 > y_scorers + scorers_h - 10:
                break
            txt = f"• {s.to_summary()}"
            self.draw.text((away_x, curr_y), txt, font=font_scorer_item, fill=Colors.TEXT_WHITE)

        # 5. MVP Highlight Tag (if present)
        if self.data.mvp_name:
            y_mvp = y_scorers + scorers_h + 25 if not is_story else y_scorers + scorers_h + 40
            mvp_w = self.width - (margin_x * 2)
            mvp_h = 60 if not is_story else 80
            
            draw_rounded_card(
                self.draw,
                (margin_x, y_mvp, margin_x + mvp_w, y_mvp + mvp_h),
                radius=15,
                fill=(40, 35, 20, 230),
                border=Colors.ACCENT_GOLD,
                border_width=2
            )
            
            font_mvp = get_font("BODY", 22 if not is_story else 26)
            mvp_text = f"⭐  MIGLIORE IN CAMPO:  {self.data.mvp_name.upper()}"
            draw_text_centered(
                self.draw,
                mvp_text,
                font_mvp,
                (margin_x, y_mvp, margin_x + mvp_w, y_mvp + mvp_h),
                fill=Colors.TEXT_GOLD
            )

        # 6. Footer Info
        loc_date = ""
        if self.data.date or self.data.location:
            parts = [p for p in [self.data.date, self.data.time, self.data.location] if p]
            loc_date = "  |  ".join(parts)
            
        self.draw_footer_brand(loc_date)

        return self.image
