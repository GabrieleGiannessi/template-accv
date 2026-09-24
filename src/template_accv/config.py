"""
Configuration module for template-accv.
Defines design system tokens, color palettes, canvas dimensions, and path constants.
"""

from enum import Enum
from pathlib import Path

# Paths
BASE_DIR = Path(__file__).resolve().parent.parent.parent
ASSETS_DIR = BASE_DIR / "assets"
FONTS_DIR = ASSETS_DIR / "fonts"
LOGOS_DIR = ASSETS_DIR / "logos"
BACKGROUNDS_DIR = ASSETS_DIR / "backgrounds"
TEMPLATES_DIR = ASSETS_DIR / "templates"
FIGURINA_TEMPLATE_PATH = TEMPLATES_DIR / "figurina.png"
PLAYERS_DIR = ASSETS_DIR / "players"
PLAYERS_HEADSHOT_DIR = PLAYERS_DIR / "headshot"
PLAYERS_TRANSPARENT_DIR = PLAYERS_HEADSHOT_DIR / "transparent"
DATA_DIR = BASE_DIR / "data"
OUTPUT_DIR = BASE_DIR / "output"
FIGURINE_OUTPUT_DIR = OUTPUT_DIR / "figurine"

# Ensure directories exist
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
FIGURINE_OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
FONTS_DIR.mkdir(parents=True, exist_ok=True)
LOGOS_DIR.mkdir(parents=True, exist_ok=True)
BACKGROUNDS_DIR.mkdir(parents=True, exist_ok=True)
TEMPLATES_DIR.mkdir(parents=True, exist_ok=True)


class AspectRatio(str, Enum):
    RATIO_9_16 = "9:16"       # 1080 x 1920 (Story / Reels / TikTok)
    RATIO_4_3 = "4:3"         # 1440 x 1080 (Classic Landscape Post)
    RATIO_16_9 = "16:9"       # 1920 x 1080 (Banner / Widescreen)
    RATIO_1_1 = "1:1"         # 1080 x 1080 (Square)
    RATIO_4_5 = "4:5"         # 1080 x 1350 (Portrait Post)
    POST = "post"             # Legacy alias 1:1
    STORY = "story"           # Legacy alias 9:16


class GraphicStyle(str, Enum):
    CLASSIC = "classic"       # Glassmorphism cards layout with neon accents
    PHOTO = "photo"           # Minimal photo-overlay layout matching reference graphic


class FigurinaFormat(str, Enum):
    CARD = "card"             # Figurina classica 1024x1536
    STORY = "story"           # Formato Instagram Story 9:16 (1080x1920) con card e sfondo
    POST = "post"             # Formato Instagram Post 4:5 (1080x1350)
    ALL = "all"               # Genera tutti i formati


# Dimensions
DIMENSIONS = {
    AspectRatio.RATIO_9_16: (1080, 1920),
    AspectRatio.RATIO_4_3: (1440, 1080),
    AspectRatio.RATIO_16_9: (1920, 1080),
    AspectRatio.RATIO_1_1: (1080, 1080),
    AspectRatio.RATIO_4_5: (1080, 1350),
    AspectRatio.POST: (1080, 1080),
    AspectRatio.STORY: (1080, 1920),
}


# Color Palette (RGB or RGBA tuples)
class Colors:
    # Backgrounds
    BG_DARK = (15, 18, 28, 255)              # Deep obsidian charcoal
    BG_GRADIENT_END = (8, 10, 16, 255)       # Ultra dark footer/gradient end
    CARD_BG = (28, 34, 52, 220)              # Glassmorphism dark card with opacity
    CARD_BORDER = (55, 65, 95, 180)          # Subtle card outline
    ACCENT_CARD_BG = (22, 27, 40, 240)       # Darker inner card

    # Brand & Accents
    ACCENT_CYAN = (0, 229, 255)              # Electric Cyan / ACCV Primary
    ACCENT_GOLD = (255, 193, 7)              # MVP Gold
    ACCENT_GREEN = (46, 213, 115)            # Win / Success Neon Green
    ACCENT_RED = (255, 71, 87)                # Highlight Red
    ACCENT_ORANGE = (255, 127, 80)           # Coral accent

    # Text Colors
    TEXT_WHITE = (255, 255, 255, 255)
    TEXT_LIGHT_GRAY = (200, 205, 220, 255)
    TEXT_MUTED = (130, 140, 165, 255)
    TEXT_GOLD = (255, 215, 0, 255)
    TEXT_DARK = (15, 18, 28, 255)

    # Team Branding Default
    DEFAULT_HOME_COLOR = (0, 229, 255)        # Electric Cyan for ACCV
    DEFAULT_AWAY_COLOR = (255, 99, 132)       # Neon Pink/Red for Opponent


# Font Configurations
FONTS = {
    "HEADER": "BebasNeue-Regular.ttf",       # Impactful score numbers & headers
    "BODY": "Montserrat-Bold.ttf",           # Player names, subtitles
    "REGULAR": "Montserrat-Regular.ttf",     # Match details, dates, venues
    "LIGHT": "Montserrat-Light.ttf",
}

DEFAULT_TEAM_NAME = "A.C.C.V."
DEFAULT_TEAM_SHORT = "ACCV"
