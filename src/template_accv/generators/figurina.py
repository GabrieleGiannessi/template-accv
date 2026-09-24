"""
Figurina (Panini-style trading card) graphic generator.
Composites transparent player headshots into the Panini Serie B Perini sticker template.
Supports:
- Classic Figurina Sticker (1024x1536)
- Instagram Story (1080x1920) with floating 3D card and atmospheric background
- Instagram Post (1080x1350, 4:5 portrait)
- Optional Panini name bar with player name, role, jersey number badge, and ACCV logo
"""

from enum import Enum
import json
from pathlib import Path
from typing import Dict, List, Optional, Tuple, Union

from PIL import Image, ImageDraw, ImageFilter, ImageFont

from template_accv.config import (
    ASSETS_DIR,
    BASE_DIR,
    DATA_DIR,
    FIGURINA_TEMPLATE_PATH,
    FIGURINE_OUTPUT_DIR,
    FigurinaFormat,
    LOGOS_DIR,
    OUTPUT_DIR,
    PLAYERS_TRANSPARENT_DIR,
    Colors,
)
from template_accv.models import FigurinaCard
from template_accv.utils.fonts import get_font
from template_accv.utils.image_fx import (
    create_gradient_background,
    draw_text_centered,
    get_fitted_font,
    get_text_dimensions,
)


class FigurinaGenerator:
    """
    Renders a Panini-style collectible sticker card from a player cutout and template.
    """

    # Template inner photo frame coordinates in figurina.png (1024x1536)
    FRAME_BOX: Tuple[int, int, int, int] = (76, 186, 948, 1321)
    CORNER_RADIUS: int = 28
    BANNER_HEIGHT: int = 120

    def __init__(
        self,
        card_data: FigurinaCard,
        format: Union[FigurinaFormat, str] = FigurinaFormat.CARD,
        template_path: Optional[Union[str, Path]] = None,
    ):
        self.data = card_data
        if isinstance(format, str):
            try:
                self.format = FigurinaFormat(format.lower())
            except ValueError:
                self.format = FigurinaFormat.CARD
        else:
            self.format = format

        self.template_path = Path(template_path) if template_path else FIGURINA_TEMPLATE_PATH

    def _render_base_card(self) -> Image.Image:
        """Render the 1024x1536 sticker card."""
        if not self.template_path.exists():
            raise FileNotFoundError(f"Figurina template not found at {self.template_path}")

        template = Image.open(self.template_path).convert("RGBA")
        canvas_w, canvas_h = template.size

        # 1. Load and process player photo
        photo_path = Path(self.data.photo_path)
        if not photo_path.exists():
            raise FileNotFoundError(f"Player photo not found at {photo_path}")

        player_img = Image.open(photo_path).convert("RGBA")

        # Trim transparent margins
        p_bbox = player_img.getbbox()
        if p_bbox:
            player_img = player_img.crop(p_bbox)

        # 2. Scale player to fit inner photo window
        box_w = self.FRAME_BOX[2] - self.FRAME_BOX[0]  # 872
        box_h = self.FRAME_BOX[3] - self.FRAME_BOX[1]  # 1135

        aspect = player_img.width / max(1, player_img.height)

        # Intelligent scaling according to pose type
        if aspect < 0.5:  # Full body standing
            avail_h = (box_h - self.BANNER_HEIGHT) if self.data.with_banner else box_h
            scale = min((avail_h * 0.96) / player_img.height, (box_w * 0.88) / player_img.width)
            scaled_w = max(1, int(player_img.width * scale))
            scaled_h = max(1, int(player_img.height * scale))
            pos_x = self.FRAME_BOX[0] + (box_w - scaled_w) // 2
            pos_y = (self.FRAME_BOX[3] - self.BANNER_HEIGHT - scaled_h + 4) if self.data.with_banner else (self.FRAME_BOX[3] - scaled_h)
        elif aspect > 0.85:  # Close headshot / square-ish portrait
            scale = min((box_h * 0.78) / player_img.height, (box_w * 0.85) / player_img.width)
            scaled_w = max(1, int(player_img.width * scale))
            scaled_h = max(1, int(player_img.height * scale))
            pos_x = self.FRAME_BOX[0] + (box_w - scaled_w) // 2
            pos_y = self.FRAME_BOX[3] - scaled_h
        else:  # Standard half-body / bust portrait
            scale = min((box_h * 0.88) / player_img.height, (box_w * 0.95) / player_img.width)
            scaled_w = max(1, int(player_img.width * scale))
            scaled_h = max(1, int(player_img.height * scale))
            pos_x = self.FRAME_BOX[0] + (box_w - scaled_w) // 2
            pos_y = self.FRAME_BOX[3] - scaled_h

        player_scaled = player_img.resize((scaled_w, scaled_h), Image.Resampling.LANCZOS)

        # 3. Create inner frame mask (rounded rectangle)
        mask = Image.new("L", (canvas_w, canvas_h), 0)
        draw_mask = ImageDraw.Draw(mask)
        draw_mask.rounded_rectangle(self.FRAME_BOX, radius=self.CORNER_RADIUS, fill=255)

        # Paste player onto transparent layer then mask it
        player_layer = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 0))
        player_layer.paste(player_scaled, (pos_x, pos_y), player_scaled)

        masked_player = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 0))
        masked_player.paste(player_layer, (0, 0), mask)

        # Composite player on top of base template
        card = template.copy()
        card.alpha_composite(masked_player)

        # 4. Optional: Panini Name Bar
        if self.data.with_banner:
            banner_layer = self._render_banner(canvas_w, canvas_h)
            card.alpha_composite(banner_layer)

        return card

    def _render_banner(self, canvas_w: int, canvas_h: int) -> Image.Image:
        """Render Panini-style player info bar at bottom of photo frame."""
        banner_layer = Image.new("RGBA", (canvas_w, canvas_h), (0, 0, 0, 0))
        draw = ImageDraw.Draw(banner_layer)

        x1, y1_frame, x2, y2 = self.FRAME_BOX
        banner_y1 = y2 - self.BANNER_HEIGHT

        # Draw dark navy banner card aligned with bottom of frame
        draw.rounded_rectangle(
            (x1, banner_y1, x2, y2),
            radius=self.CORNER_RADIUS,
            fill=(10, 24, 44, 245),
            outline=(0, 229, 255, 220),
            width=2,
        )

        # Top cyan highlight line
        draw.line([(x1 + 4, banner_y1), (x2 - 4, banner_y1)], fill=(0, 229, 255, 255), width=3)

        # Team Logo
        logo_img = self._load_team_logo()
        logo_x = x1 + 18
        logo_y = banner_y1 + (self.BANNER_HEIGHT - logo_img.height) // 2
        banner_layer.paste(logo_img, (logo_x, logo_y), logo_img)

        # Jersey Number Badge on right side
        badge_reserved_w = 0
        if self.data.jersey_number:
            badge_sz = 84
            badge_x = x2 - badge_sz - 18
            badge_y = banner_y1 + (self.BANNER_HEIGHT - badge_sz) // 2
            draw.rounded_rectangle(
                [badge_x, badge_y, badge_x + badge_sz, badge_y + badge_sz],
                radius=18,
                fill=(18, 32, 58, 255),
                outline=Colors.ACCENT_CYAN,
                width=2,
            )
            font_num = get_font("HEADER", 52)
            draw_text_centered(
                draw,
                f"#{self.data.jersey_number}",
                font_num,
                (badge_x, badge_y, badge_x + badge_sz, badge_y + badge_sz),
                fill=Colors.TEXT_WHITE,
            )
            badge_reserved_w = badge_sz + 28

        # Text labels
        text_x = logo_x + logo_img.width + 20
        max_text_w = x2 - text_x - badge_reserved_w - 15

        # Player Name (Big Header)
        font_name = get_fitted_font(
            self.data.player_name.upper(),
            font_name_key="HEADER",
            max_width=max_text_w,
            initial_size=52,
            min_size=28,
        )
        name_y = banner_y1 + 14
        draw.text((text_x, name_y), self.data.player_name.upper(), font=font_name, fill=Colors.TEXT_WHITE)

        # Role & Team Subtitle
        subtitle_parts = []
        if self.data.role:
            subtitle_parts.append(self.data.role.upper())
        if self.data.team_name:
            subtitle_parts.append(self.data.team_name.upper())

        subtitle = "  |  ".join(subtitle_parts) if subtitle_parts else "A.C.C.V."

        font_sub = get_fitted_font(subtitle, "REGULAR", max_text_w, 24, 14)
        sub_y = banner_y1 + 64
        draw.text((text_x, sub_y), subtitle, font=font_sub, fill=Colors.ACCENT_CYAN)

        return banner_layer

    def _load_team_logo(self) -> Image.Image:
        """Load ACCV or custom team logo resized for the banner."""
        target_size = (86, 86)
        logo_file = Path(self.data.team_logo_path) if self.data.team_logo_path else LOGOS_DIR / "accv.png"

        if logo_file.exists():
            try:
                img = Image.open(logo_file).convert("RGBA")
                bbox = img.getbbox()
                if bbox:
                    img = img.crop(bbox)
                img.thumbnail(target_size, Image.Resampling.LANCZOS)

                canvas = Image.new("RGBA", target_size, (0, 0, 0, 0))
                offset = ((target_size[0] - img.width) // 2, (target_size[1] - img.height) // 2)
                canvas.paste(img, offset, img)
                return canvas
            except Exception:
                pass

        # Fallback circle badge
        badge = Image.new("RGBA", target_size, (0, 0, 0, 0))
        draw = ImageDraw.Draw(badge)
        draw.ellipse([2, 2, target_size[0] - 2, target_size[1] - 2], fill=(20, 28, 45, 255), outline=Colors.ACCENT_CYAN, width=3)
        font_fb = get_font("HEADER", 36)
        tw, th = get_text_dimensions("ACCV", font_fb)
        draw.text(((target_size[0] - tw) // 2, (target_size[1] - th) // 2 - 2), "ACCV", font=font_fb, fill=Colors.TEXT_WHITE)
        return badge

    def _render_story(self, card: Image.Image) -> Image.Image:
        """Render 1080x1920 Instagram Story graphic featuring the 3D card."""
        sw, sh = 1080, 1920

        # 1. Dark atmospheric gradient with cyan spotlight
        bg = create_gradient_background(sw, sh, (14, 18, 28, 255), (6, 8, 14, 255), radial_spotlight=True)

        # 2. Scale card for story canvas
        card_h = 1380
        card_w = int(card.width * (card_h / card.height))
        card_scaled = card.resize((card_w, card_h), Image.Resampling.LANCZOS)

        card_x = (sw - card_w) // 2
        card_y = (sh - card_h) // 2 + 30

        # 3. Floating 3D Drop Shadow
        shadow = Image.new("RGBA", (sw, sh), (0, 0, 0, 0))
        shadow_draw = ImageDraw.Draw(shadow)
        shadow_draw.rounded_rectangle(
            [card_x - 6, card_y + 12, card_x + card_w + 6, card_y + card_h + 24],
            radius=35,
            fill=(0, 0, 0, 200),
        )
        shadow = shadow.filter(ImageFilter.GaussianBlur(28))

        bg.alpha_composite(shadow)
        bg.paste(card_scaled, (card_x, card_y), card_scaled)

        # 4. Header & Branding
        draw = ImageDraw.Draw(bg)
        font_sub = get_font("REGULAR", 26)
        font_title = get_font("HEADER", 60)

        sub_text = "A.C.C.V.  -  ROSTER UFFICIALE"
        tw_sub, _ = get_text_dimensions(sub_text, font_sub)
        draw.text(((sw - tw_sub) // 2, 70), sub_text, font=font_sub, fill=Colors.ACCENT_CYAN)

        main_text = "COLLEZIONE FIGURINE CALCIATORI"
        tw_main, _ = get_text_dimensions(main_text, font_title)
        draw.text(((sw - tw_main) // 2, 106), main_text, font=font_title, fill=Colors.TEXT_WHITE)

        # Footer
        font_foot = get_font("REGULAR", 22)
        foot_text = "SERIE B PERINI  -  STAGIONE 2026"
        tw_foot, _ = get_text_dimensions(foot_text, font_foot)
        draw.text(((sw - tw_foot) // 2, sh - 65), foot_text, font=font_foot, fill=Colors.TEXT_MUTED)

        return bg

    def _render_post(self, card: Image.Image) -> Image.Image:
        """Render 1080x1350 (4:5 Instagram Portrait Post) graphic."""
        pw, ph = 1080, 1350
        bg = create_gradient_background(pw, ph, (14, 18, 28, 255), (6, 8, 14, 255), radial_spotlight=True)

        card_h = 1080
        card_w = int(card.width * (card_h / card.height))
        card_scaled = card.resize((card_w, card_h), Image.Resampling.LANCZOS)

        card_x = (pw - card_w) // 2
        card_y = (ph - card_h) // 2 + 25

        shadow = Image.new("RGBA", (pw, ph), (0, 0, 0, 0))
        shadow_draw = ImageDraw.Draw(shadow)
        shadow_draw.rounded_rectangle(
            [card_x - 5, card_y + 10, card_x + card_w + 5, card_y + card_h + 18],
            radius=30,
            fill=(0, 0, 0, 190),
        )
        shadow = shadow.filter(ImageFilter.GaussianBlur(24))

        bg.alpha_composite(shadow)
        bg.paste(card_scaled, (card_x, card_y), card_scaled)

        # Header
        draw = ImageDraw.Draw(bg)
        font_title = get_font("HEADER", 46)
        title_text = "A.C.C.V.  -  FIGURINE UFFICIALI"
        tw, _ = get_text_dimensions(title_text, font_title)
        draw.text(((pw - tw) // 2, 45), title_text, font=font_title, fill=Colors.TEXT_WHITE)

        return bg

    def render(self) -> Image.Image:
        """Render graphic according to configured format."""
        base_card = self._render_base_card()

        if self.format == FigurinaFormat.STORY:
            return self._render_story(base_card)
        elif self.format == FigurinaFormat.POST:
            return self._render_post(base_card)
        else:
            return base_card

    def to_image(self) -> Image.Image:
        """Render and return the PIL Image object."""
        return self.render()

    def to_bytes(self, format: str = "PNG", quality: int = 95) -> bytes:
        """Render graphic and return raw bytes in memory without writing to disk."""
        import io
        img = self.render()
        buf = io.BytesIO()
        if format.upper() in ("JPEG", "JPG") and img.mode in ("RGBA", "LA", "P"):
            img = img.convert("RGB")
        img.save(buf, format=format, quality=quality)
        return buf.getvalue()

    def to_data_uri(self, format: str = "PNG", quality: int = 95) -> str:
        """Render graphic and return Base64 data URI (for direct browser preview)."""
        import base64
        raw = self.to_bytes(format=format, quality=quality)
        mime = "image/png" if format.upper() == "PNG" else "image/jpeg"
        b64 = base64.b64encode(raw).decode("ascii")
        return f"data:{mime};base64,{b64}"

    def save(self, filepath: Union[str, Path]) -> str:
        """Render and save image to filepath."""
        filepath = Path(filepath)
        filepath.parent.mkdir(parents=True, exist_ok=True)
        img = self.render()
        img.save(str(filepath), "PNG", quality=95)
        return str(filepath)



def load_roster(json_path: Optional[Union[str, Path]] = None) -> Dict[str, dict]:
    """Load player roster dictionary from JSON file."""
    path = Path(json_path) if json_path else DATA_DIR / "players.json"
    if path.exists():
        try:
            with open(path, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception as e:
            print(f"Warning: could not read {path}: {e}")
    return {}


def get_player_card(
    player_key_or_filename: str,
    roster: Optional[Dict[str, dict]] = None,
    photos_dir: Optional[Path] = None,
    with_banner: bool = True,
) -> FigurinaCard:
    """Build a FigurinaCard model for a player identifier."""
    if roster is None:
        roster = load_roster()

    if photos_dir is None:
        photos_dir = PLAYERS_TRANSPARENT_DIR

    clean_key = Path(player_key_or_filename).stem.lower().strip()

    # Find photo file
    photo_file = None
    for p in photos_dir.iterdir():
        if p.is_file() and p.suffix.lower() in [".png", ".jpg", ".jpeg", ".webp"]:
            if p.stem.lower() == clean_key or clean_key in p.stem.lower():
                photo_file = p
                break

    if not photo_file:
        raise FileNotFoundError(f"Foto non trovata per '{player_key_or_filename}' in {photos_dir}")

    # Lookup metadata or derive from filename
    info = roster.get(clean_key, {})

    if not info:
        name_parts = clean_key.replace("_", "-").split("-")
        derived_name = " ".join(p.capitalize() for p in name_parts)
    else:
        derived_name = info.get("name", clean_key.title())

    return FigurinaCard(
        player_name=info.get("name", derived_name),
        photo_path=str(photo_file),
        role=info.get("role", "Giocatore"),
        jersey_number=info.get("number"),
        team_name=info.get("team", "A.C.C.V."),
        with_banner=with_banner,
    )


def generate_all_figurine(
    photos_dir: Optional[Union[str, Path]] = None,
    output_dir: Optional[Union[str, Path]] = None,
    format: Union[FigurinaFormat, str] = FigurinaFormat.CARD,
    with_banner: bool = True,
    roster_path: Optional[Union[str, Path]] = None,
) -> List[str]:
    """
    Generate figurine for all transparent player photos found.
    Returns list of generated file paths.
    """
    p_dir = Path(photos_dir) if photos_dir else PLAYERS_TRANSPARENT_DIR
    out_dir = Path(output_dir) if output_dir else FIGURINE_OUTPUT_DIR
    out_dir.mkdir(parents=True, exist_ok=True)

    roster = load_roster(roster_path)
    photo_files = sorted(
        [p for p in p_dir.iterdir() if p.is_file() and p.suffix.lower() in [".png", ".webp"]]
    )

    if not photo_files:
        print(f"Nessuna foto trovata in {p_dir}")
        return []

    fmt_enum = FigurinaFormat(format) if isinstance(format, str) else format
    formats_to_render = (
        [FigurinaFormat.CARD, FigurinaFormat.STORY]
        if fmt_enum == FigurinaFormat.ALL
        else [fmt_enum]
    )

    generated_paths = []
    for p_file in photo_files:
        card_data = get_player_card(p_file.name, roster=roster, photos_dir=p_dir, with_banner=with_banner)

        for fmt in formats_to_render:
            suffix = f"_{fmt.value}" if fmt != FigurinaFormat.CARD else ""
            out_file = out_dir / f"{p_file.stem}{suffix}.png"

            gen = FigurinaGenerator(card_data, format=fmt)
            saved = gen.save(out_file)
            generated_paths.append(saved)
            print(f"  [OK] {card_data.player_name} ({fmt.value.upper()}) -> {saved}")

    return generated_paths
