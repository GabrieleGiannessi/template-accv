"""
Match Result graphic generator (Risultato Finale).
Renders 9:16, 4:3, 16:9, 1:1, and 4:5 social media graphics for match scores.
Supports both 'classic' glassmorphism card layout and 'photo' minimal photo-overlay layout matching reference graphic.
"""

from pathlib import Path
from typing import Optional, Union
from PIL import Image, ImageDraw, ImageEnhance, ImageOps

from template_accv.config import AspectRatio, GraphicStyle, Colors
from template_accv.models import MatchResult
from template_accv.generators.base import BaseGraphicGenerator
from template_accv.utils.fonts import get_font
from template_accv.generators.figurina import load_roster
from template_accv.utils.image_fx import (
    draw_rounded_card,
    draw_text_centered,
    get_text_dimensions,
    get_fitted_font,
    format_team_name_vertical,
    load_team_logo,
)


class MatchResultGenerator(BaseGraphicGenerator):
    _ball_icon = None
    _card_icons = {}
    def __init__(
        self,
        match_result: MatchResult,
        aspect_ratio: AspectRatio = AspectRatio.RATIO_9_16,
        bg_path: Optional[str] = None,
        emotion: Optional[str] = None,
        contrast_factor: float = 1.0,
        remove_contrast: bool = False,
        style: Optional[Union[GraphicStyle, str]] = None,
        bg_zoom: float = 1.0,
        bg_x: float = 0.5,
        bg_y: float = 0.5,
    ):
        super().__init__(
            aspect_ratio=aspect_ratio,
            bg_path=bg_path,
            emotion=emotion,
            contrast_factor=contrast_factor,
            remove_contrast=remove_contrast,
            dark_overlay_alpha=0,  # Clean photo background with bottom gradient overlay
            bg_zoom=bg_zoom,
            bg_x=bg_x,
            bg_y=bg_y,
        )
        self.data = match_result

    def _draw_ball_icon(self, x: int, y: int, size: int) -> None:
        if MatchResultGenerator._ball_icon is None:
            icon_path = Path(__file__).resolve().parents[3] / "assets" / "icons" / "ball.png"
            MatchResultGenerator._ball_icon = Image.open(icon_path).convert("RGBA")
        icon = MatchResultGenerator._ball_icon.resize((size, size), Image.Resampling.LANCZOS)
        self.image.alpha_composite(icon, (x, y))

    def _draw_card_icon(self, x: int, y: int, size: int, color: tuple) -> None:
        icon_name = "yellow_card.png" if color[1] > color[2] else "red_card.png"
        if icon_name not in MatchResultGenerator._card_icons:
            icon_path = Path(__file__).resolve().parents[3] / "assets" / "icons" / icon_name
            source = Image.open(icon_path).convert("RGBA")
            MatchResultGenerator._card_icons[icon_name] = source.crop(source.getchannel("A").getbbox())
        source = MatchResultGenerator._card_icons[icon_name]
        icon_width = max(1, round(source.width * size / source.height))
        icon = source.resize((icon_width, size), Image.Resampling.LANCZOS)
        self.image.alpha_composite(icon, (x, y))

    @staticmethod
    def _short_player_name(name: str) -> str:
        parts = name.strip().split()
        if len(parts) == 1:
            return name.strip()
        if len(parts[-1]) == 2 and parts[-1].endswith("."):
            return parts[0]
        return parts[-1]

    def _collect_detail_rows(self, scorers: list, yellow_cards: list, red_cards: list, display_names: Optional[dict] = None) -> list:
        rows = []

        def find_or_add(name: str) -> dict:
            clean_name = " ".join(name.strip().casefold().split())
            short_name = " ".join(self._short_player_name(name).casefold().split())
            aliases = {clean_name, short_name} - {""}
            row = next((candidate for candidate in rows if candidate["aliases"] & aliases), None)
            if row is None:
                row = {"aliases": aliases, "label": self._short_player_name(name), "goals": 0, "cards": []}
                rows.append(row)
            else:
                row["aliases"].update(aliases)
            return row

        for scorer in scorers:
            row = find_or_add(scorer.name)
            row["label"] = (display_names or {}).get(scorer.name.strip().casefold()) or self._short_player_name(scorer.name)
            row["goals"] += max(1, int(scorer.goals))
        for name in yellow_cards:
            find_or_add(name)["cards"].append((255, 210, 0, 255))
        for name in red_cards:
            find_or_add(name)["cards"].append((220, 38, 38, 255))
        return rows

    def _draw_detail_block(self, bounds: tuple, rows: list, font_size: int, line_height: int) -> None:
        x1, y1, x2, _ = bounds
        center_x = (x1 + x2) // 2
        y = y1

        def draw_row(row: dict) -> None:
            nonlocal y
            target_icon_size = 42 if self.is_vertical else 32
            icon_count = row["goals"] + len(row["cards"])
            icon_size = max(18, min(target_icon_size, line_height - 8, int((x2 - x1 - 24) * 0.58 / max(1, icon_count))))
            card_size = max(16, round(icon_size * 0.88))
            ball_gap = max(3, icon_size // 7)
            card_gap = max(3, card_size // 7)
            balls_w = row["goals"] * icon_size + max(0, row["goals"] - 1) * ball_gap
            card_width = max(1, round(card_size * 0.68))
            cards_w = len(row["cards"]) * card_width + max(0, len(row["cards"]) - 1) * card_gap
            between_groups = ball_gap if row["goals"] and row["cards"] else 0
            icons_w = balls_w + cards_w + between_groups
            max_text_w = max(20, x2 - x1 - icons_w - (6 if icon_count else 0) - 16)
            label = row["label"].upper()
            row_font = get_fitted_font(label, "HEADER", max_text_w, font_size, min_size=max(14, font_size // 2))
            text_w, text_h = get_text_dimensions(label, row_font)
            total_w = text_w + (6 if icon_count else 0) + icons_w
            start_x = center_x - total_w // 2
            text_y = y + max(0, (line_height - text_h) // 2)
            self.draw.text((start_x, text_y), label, font=row_font, fill=Colors.TEXT_WHITE)
            icon_x = start_x + text_w + (6 if icon_count else 0)
            icon_y = y + (line_height - icon_size) // 2
            for index in range(row["goals"]):
                self._draw_ball_icon(icon_x, icon_y, icon_size)
                icon_x += icon_size
                if index < row["goals"] - 1:
                    icon_x += ball_gap
            if row["goals"] and row["cards"]:
                icon_x += card_gap
            for index, color in enumerate(row["cards"]):
                card_y = y + (line_height - card_size) // 2
                self._draw_card_icon(icon_x, card_y, card_size, color)
                icon_x += card_width
                if index < len(row["cards"]) - 1:
                    icon_x += card_gap
            y += line_height

        for row in rows:
            draw_row(row)

    def render(self) -> Image.Image:
        """Render minimal photo-overlay graphic matching reference design."""
        return self.render_photo_style()

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
        
        has_details = any((
            self.data.home_scorers, self.data.away_scorers,
            self.data.home_yellow_cards, self.data.away_yellow_cards,
            self.data.home_red_cards, self.data.away_red_cards,
        ))
        logo_y = int(self.height * (0.50 if has_details and self.is_vertical else 0.38 if has_details else 0.68))

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

        if has_details:
            details_y = y_subtitle + th_sub + 14
            roster = load_roster()
            display_names = {
                str(player.get("name", "")).strip().casefold(): str(player.get("display_name", "")).strip()
                for player in roster.values()
                if player.get("display_name") and player.get("name")
            }
            home_is_accv = "ACCV" in self.data.home_team.name.upper()
            away_is_accv = "ACCV" in self.data.away_team.name.upper()
            home_rows = self._collect_detail_rows(
                self.data.home_scorers, self.data.home_yellow_cards, self.data.home_red_cards,
                display_names if home_is_accv else None,
            )
            away_rows = self._collect_detail_rows(
                self.data.away_scorers, self.data.away_yellow_cards, self.data.away_red_cards,
                display_names if away_is_accv else None,
            )
            max_rows = max(len(home_rows), len(away_rows), 1)
            base_font_size = 38 if self.is_vertical else 30
            available_height = max(1, self.height - 20 - details_y)
            details_line_height = min(base_font_size + 16, max(30, available_height // max_rows))
            details_font_size = min(base_font_size, details_line_height - 12)
            self._draw_detail_block(
                (int(self.width * 0.035), details_y, center_x - 18, self.height - 20),
                home_rows, details_font_size, details_line_height,
            )
            self._draw_detail_block(
                (center_x + 18, details_y, int(self.width * 0.965), self.height - 20),
                away_rows, details_font_size, details_line_height,
            )

        return self.image

