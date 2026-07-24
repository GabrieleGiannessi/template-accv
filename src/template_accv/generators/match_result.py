"""
Match Result graphic generator (Risultato Finale).
Renders 9:16, 4:3, 16:9, 1:1, and 4:5 social media graphics for match scores.
Supports both 'classic' glassmorphism card layout and 'photo' minimal photo-overlay layout matching reference graphic.
"""

from typing import Optional, Union
from PIL import Image, ImageDraw, ImageEnhance, ImageOps

from template_accv.config import AspectRatio, GraphicStyle, Colors
from template_accv.models import MatchResult
from template_accv.generators.base import BaseGraphicGenerator
from template_accv.utils.fonts import get_font
from template_accv.utils.image_fx import (
    draw_rounded_card,
    draw_text_centered,
    get_text_dimensions,
    get_fitted_font,
    format_team_name_vertical,
    load_team_logo,
)


class MatchResultGenerator(BaseGraphicGenerator):
    def __init__(
        self,
        match_result: MatchResult,
        aspect_ratio: AspectRatio = AspectRatio.RATIO_9_16,
        style: Union[GraphicStyle, str] = GraphicStyle.CLASSIC,
        bg_path: Optional[str] = None,
        emotion: Optional[str] = None,
        contrast_factor: float = 1.0,
        remove_contrast: bool = False
    ):
        self.style = GraphicStyle(style) if isinstance(style, str) else style
        # Photo style uses clean background photo without heavy full-canvas dark overlay
        overlay_alpha = 0 if self.style == GraphicStyle.PHOTO else 150
        
        super().__init__(
            aspect_ratio=aspect_ratio,
            bg_path=bg_path,
            emotion=emotion,
            contrast_factor=contrast_factor,
            remove_contrast=remove_contrast,
            dark_overlay_alpha=overlay_alpha
        )
        self.data = match_result

    def render(self) -> Image.Image:
        """Render graphic based on selected style."""
        if self.style == GraphicStyle.PHOTO:
            return self.render_photo_style()
        return self.render_classic_style()

    def render_photo_style(self) -> Image.Image:
        """
        Minimal Photo-Overlay Layout (Matching reference image):
        Full-bleed photo with desaturated dark green tint, dark bottom gradient,
        logos & score arranged horizontally on bottom section, and 'MATCH RESULT' subtitle.
        """
        # Desaturate and apply subtle dark-green tone tint to action photo
        rgb_img = self.image.convert("RGB")
        desaturated = ImageEnhance.Color(rgb_img).enhance(0.55)
        desaturated = ImageEnhance.Contrast(desaturated).enhance(1.1)
        
        tint_layer = Image.new("RGB", (self.width, self.height), (12, 28, 22))
        blended = Image.blend(desaturated, tint_layer, 0.15)
        self.image = blended.convert("RGBA")

        # Bottom Gradient Vignette for lower section
        vignette = Image.new("RGBA", (self.width, self.height), (0, 0, 0, 0))
        vig_draw = ImageDraw.Draw(vignette)
        vig_start_y = int(self.height * 0.52)
        for y in range(vig_start_y, self.height):
            ratio = (y - vig_start_y) / (self.height - vig_start_y)
            alpha = int(240 * (ratio ** 1.3))
            vig_draw.line([(0, y), (self.width, y)], fill=(6, 8, 10, alpha))
        
        self.image = Image.alpha_composite(self.image, vignette)
        self.draw = ImageDraw.Draw(self.image)

        # Bottom Layout Calculations
        center_x = self.width // 2
        logo_sz = int(min(self.width, self.height) * 0.22)
        logo_size = (logo_sz, logo_sz)

        # Load Logos
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

        logo_spacing = int(self.width * 0.125)
        home_logo_x = center_x - logo_spacing - logo_size[0]
        away_logo_x = center_x + logo_spacing
        
        logo_y = int(self.height * 0.68)

        # Paste Logos onto canvas FIRST
        self.image.paste(home_logo, (home_logo_x, logo_y), home_logo)
        self.image.paste(away_logo, (away_logo_x, logo_y), away_logo)

        # Re-initialize Draw context AFTER paste
        self.draw = ImageDraw.Draw(self.image)

        # Outer canvas dark frame border
        border_thick = 14 if self.is_vertical else 10
        self.draw.rectangle(
            [0, 0, self.width - 1, self.height - 1],
            outline=(12, 12, 12, 255),
            width=border_thick
        )

        # Team Names above Logos with multiline vertical wrapping for long team names
        max_team_w = logo_size[0] + 40
        home_lines, font_home = format_team_name_vertical(self.data.home_team.name, "HEADER", max_team_w, initial_size=42 if self.is_vertical else 32)
        away_lines, font_away = format_team_name_vertical(self.data.away_team.name, "HEADER", max_team_w, initial_size=42 if self.is_vertical else 32)

        # Draw Home Team Name (stacked vertically if multiline)
        ht_heights = [get_text_dimensions(line, font_home)[1] for line in home_lines]
        total_ht_h = sum(ht_heights) + (len(home_lines) - 1) * 4
        curr_y = logo_y - total_ht_h - 12
        for line in home_lines:
            lw, lh = get_text_dimensions(line, font_home)
            lx = home_logo_x + (logo_size[0] - lw) // 2
            self.draw.text((lx, curr_y), line, font=font_home, fill=Colors.TEXT_WHITE)
            curr_y += lh + 4

        # Draw Away Team Name (stacked vertically if multiline)
        at_heights = [get_text_dimensions(line, font_away)[1] for line in away_lines]
        total_at_h = sum(at_heights) + (len(away_lines) - 1) * 4
        curr_y = logo_y - total_at_h - 12
        for line in away_lines:
            lw, lh = get_text_dimensions(line, font_away)
            lx = away_logo_x + (logo_size[0] - lw) // 2
            self.draw.text((lx, curr_y), line, font=font_away, fill=Colors.TEXT_WHITE)
            curr_y += lh + 4

        # Center Score Numbers ("3 - 4") - Increased Impact Font Size
        font_score_sz = 165 if self.is_vertical else 130
        font_score = get_font("HEADER", font_score_sz)
        score_str = f"{self.data.home_score}-{self.data.away_score}"
        
        tw_score, th_score = get_text_dimensions(score_str, font_score)
        score_x = center_x - tw_score // 2
        score_y = logo_y + (logo_size[1] - th_score) // 2 - 16

        # Draw Score text with golden outline and white fill
        shadow_offset = 4
        self.draw.text((score_x + shadow_offset, score_y + shadow_offset), score_str, font=font_score, fill=(10, 10, 10, 240))
        for dx in [-3, -2, -1, 1, 2, 3]:
            for dy in [-3, -2, -1, 1, 2, 3]:
                self.draw.text((score_x + dx, score_y + dy), score_str, font=font_score, fill=(212, 175, 55, 255))
        self.draw.text((score_x, score_y), score_str, font=font_score, fill=Colors.TEXT_WHITE)

        # Bottom Subtitle "MATCH RESULT" - Increased Font Size
        y_subtitle = logo_y + logo_size[1] + 35
        font_sub_sz = 44 if self.is_vertical else 34
        font_sub = get_font("HEADER", font_sub_sz)
        sub_text = "MATCH RESULT"
        tw_sub, th_sub = get_text_dimensions(sub_text, font_sub)
        
        sub_x = center_x - tw_sub // 2
        self.draw.text((sub_x, y_subtitle), sub_text, font=font_sub, fill=(212, 175, 55, 255))

        return self.image

    def render_classic_style(self) -> Image.Image:
        """Render classic glassmorphism card layout."""
        # 1. Header (Tournament & Matchday)
        self.draw_top_header(self.data.tournament, self.data.matchday)
        
        # 2. Title Banner ("RISULTATO FINALE")
        if self.aspect_ratio == AspectRatio.RATIO_9_16:
            y_title = 220
            font_title_sz = 72
        elif self.aspect_ratio == AspectRatio.RATIO_4_5:
            y_title = 160
            font_title_sz = 68
        elif self.is_wide_landscape:
            y_title = 110
            font_title_sz = 58
        else:
            y_title = 140
            font_title_sz = 64

        font_title = get_font("HEADER", font_title_sz)
        title_str = "RISULTATO FINALE"
        tw, th = get_text_dimensions(title_str, font_title)
        self.draw.text(
            ((self.width - tw) // 2, y_title),
            title_str,
            font=font_title,
            fill=Colors.TEXT_WHITE
        )
        
        # Accent glowing bar under title
        bar_w = int(self.width * 0.18)
        self.draw.rectangle(
            [(self.width - bar_w) // 2, y_title + th + 8, (self.width + bar_w) // 2, y_title + th + 12],
            fill=Colors.ACCENT_CYAN
        )

        # 3. Team Crests & Score Board Card
        margin_x = int(self.width * 0.05)
        
        if self.aspect_ratio == AspectRatio.RATIO_9_16:
            y_card = 390
            card_h = 380
        elif self.aspect_ratio == AspectRatio.RATIO_4_5:
            y_card = 280
            card_h = 320
        elif self.is_wide_landscape:
            y_card = 200
            card_h = 280
        else:
            y_card = 250
            card_h = 310

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
        logo_sz = int(card_h * 0.42)
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

        # Team Names under logos
        font_team_sz = 24 if self.is_vertical else 20
        font_team = get_font("BODY", font_team_sz)
        
        ht_w, ht_h = get_text_dimensions(self.data.home_team.name, font_team)
        ht_x = home_logo_x + (logo_size[0] - ht_w) // 2
        self.draw.text((ht_x, logo_y + logo_size[1] + 12), self.data.home_team.name, font=font_team, fill=Colors.TEXT_WHITE)

        at_w, at_h = get_text_dimensions(self.data.away_team.name, font_team)
        at_x = away_logo_x + (logo_size[0] - at_w) // 2
        self.draw.text((at_x, logo_y + logo_size[1] + 12), self.data.away_team.name, font=font_team, fill=Colors.TEXT_WHITE)

        # Score Box in Center
        score_box_w = int(self.width * 0.26)
        score_box_h = int(card_h * 0.38)
        score_x = (self.width - score_box_w) // 2
        score_y = y_card + (card_h - score_box_h) // 2 - 5

        self.draw.rounded_rectangle(
            [score_x, score_y, score_x + score_box_w, score_y + score_box_h],
            radius=18,
            fill=Colors.ACCENT_CARD_BG,
            outline=Colors.ACCENT_CYAN,
            width=3
        )

        font_score_sz = 86 if self.is_vertical else 72
        font_score = get_font("HEADER", font_score_sz)
        score_str = f"{self.data.home_score} - {self.data.away_score}"
        draw_text_centered(
            self.draw,
            score_str,
            font_score,
            (score_x, score_y, score_x + score_box_w, score_y + score_box_h),
            fill=Colors.TEXT_WHITE
        )

        # 4. Goal Scorers Section
        y_scorers = y_card + card_h + 25
        if self.aspect_ratio == AspectRatio.RATIO_9_16:
            scorers_h = 420
        elif self.is_wide_landscape:
            scorers_h = 240
        else:
            scorers_h = 280

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
        font_sc_head_sz = 34 if self.is_vertical else 28
        font_sc_head = get_font("HEADER", font_sc_head_sz)
        sc_head_text = "⚽  MARCATORI  ⚽"
        tw, th = get_text_dimensions(sc_head_text, font_sc_head)
        self.draw.text(
            ((self.width - tw) // 2, y_scorers + 16),
            sc_head_text,
            font=font_sc_head,
            fill=Colors.ACCENT_CYAN
        )

        # Divider line
        self.draw.line(
            [margin_x + 30, y_scorers + 58, self.width - margin_x - 30, y_scorers + 58],
            fill=Colors.CARD_BORDER,
            width=1
        )

        # Scorers listing
        font_scorer_sz = 22 if self.is_vertical else 19
        font_scorer_item = get_font("REGULAR", font_scorer_sz)
        item_y_start = y_scorers + 72
        
        # Home Scorers
        home_x = margin_x + int(self.width * 0.04)
        for i, s in enumerate(self.data.home_scorers):
            curr_y = item_y_start + i * 34
            if curr_y + 25 > y_scorers + scorers_h - 10:
                break
            txt = f"• {s.to_summary()}"
            self.draw.text((home_x, curr_y), txt, font=font_scorer_item, fill=Colors.TEXT_WHITE)

        # Away Scorers
        away_x = self.width // 2 + int(self.width * 0.04)
        for i, s in enumerate(self.data.away_scorers):
            curr_y = item_y_start + i * 34
            if curr_y + 25 > y_scorers + scorers_h - 10:
                break
            txt = f"• {s.to_summary()}"
            self.draw.text((away_x, curr_y), txt, font=font_scorer_item, fill=Colors.TEXT_WHITE)

        # 5. MVP Highlight Tag (if present)
        if self.data.mvp_name:
            y_mvp = y_scorers + scorers_h + 20
            mvp_w = self.width - (margin_x * 2)
            mvp_h = 65 if self.is_vertical else 55
            
            draw_rounded_card(
                self.draw,
                (margin_x, y_mvp, margin_x + mvp_w, y_mvp + mvp_h),
                radius=15,
                fill=(40, 35, 20, 230),
                border=Colors.ACCENT_GOLD,
                border_width=2
            )
            
            font_mvp_sz = 24 if self.is_vertical else 20
            font_mvp = get_font("BODY", font_mvp_sz)
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
