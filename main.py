"""
Main entry point script for template-accv graphic template generation.
Supports rendering graphics for match result, next match, and MVP stats
across all social media formats (9:16, 4:3, 16:9, 1:1, 4:5).
Full support for CLI direct input (teams, scores, background image, emotion category, contrast filters).
"""

import argparse
import json
from pathlib import Path
import sys

# Ensure src/ is on python path
src_path = Path(__file__).resolve().parent / "src"
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

from template_accv.config import AspectRatio, OUTPUT_DIR, DATA_DIR
from template_accv.models import Team, Scorer, MatchResult, NextMatch, MVP
from template_accv.generators.match_result import MatchResultGenerator
from template_accv.generators.next_match import NextMatchGenerator
from template_accv.generators.mvp import MVPGenerator
from template_accv.utils.logo_generator import generate_sample_team_logos


def parse_args():
    parser = argparse.ArgumentParser(
        description="Generatore automatizzato di grafiche social per A.C. C.V."
    )
    # Output types & formats
    parser.add_argument(
        "--type", "-t",
        choices=["all", "result", "next", "mvp"],
        default="all",
        help="Tipo di grafica da generare (default: all)"
    )
    parser.add_argument(
        "--format", "-f",
        choices=["all", "both", "9:16", "4:3", "16:9", "1:1", "4:5", "post", "story"],
        default="both",
        help="Formato social media da generare: 9:16, 4:3, 16:9, 1:1, 4:5 o 'all' (default: both)"
    )
    parser.add_argument(
        "--style", "-st",
        choices=["classic", "photo"],
        default="classic",
        help="Stile layout del risultato finale: 'classic' (cards glassmorphic) o 'photo' (minimal photo-overlay) (default: classic)"
    )
    
    # Direct CLI input arguments (teams & result)
    parser.add_argument(
        "--home-team", "-ht",
        type=str,
        default=None,
        help="Nome squadra di casa (es. 'A.C. C.V.')"
    )
    parser.add_argument(
        "--away-team", "-at",
        type=str,
        default=None,
        help="Nome squadra ospite (es. 'Real Matrid')"
    )
    parser.add_argument(
        "--score", "-s",
        type=str,
        default=None,
        help="Risultato della partita (es. '5-2' o '3:1')"
    )
    parser.add_argument(
        "--home-score",
        type=int,
        default=None,
        help="Gol squadra di casa"
    )
    parser.add_argument(
        "--away-score",
        type=int,
        default=None,
        help="Gol squadra ospite"
    )
    
    # Background & Emotion options
    parser.add_argument(
        "--bg-image", "--bg",
        type=str,
        default=None,
        help="Percorso ad una immagine di sfondo specifica"
    )
    parser.add_argument(
        "--emotion", "-e",
        type=str,
        default=None,
        help="Categoria di emozione per scelta sfondo random (es. 'felicità', 'tristezza', 'polemica', 'normale', 'foto squadra')"
    )
    
    # Contrast Filters
    parser.add_argument(
        "--contrast",
        type=float,
        default=1.0,
        help="Fattore di contrasto per l'immagine di sfondo (es. 0.5 per ridurre, 1.0 normale)"
    )
    parser.add_argument(
        "--no-contrast",
        action="store_true",
        help="Riduce il contrasto dall'immagine di sfondo per un effetto flat/soft"
    )

    # Path options
    parser.add_argument(
        "--data", "-d",
        type=str,
        default=str(DATA_DIR / "example_match.json"),
        help="Percorso al file JSON con i dati della partita (usato se i dati da CLI non sono completi)"
    )
    parser.add_argument(
        "--output", "-o",
        type=str,
        default=str(OUTPUT_DIR),
        help="Cartella di destinazione per salvare le immagini"
    )
    return parser.parse_args()


def load_match_data(args):
    """Load match data from JSON if present, overlaid with direct CLI arguments."""
    json_path = Path(args.data)
    data = {}
    if json_path.exists():
        try:
            with open(json_path, "r", encoding="utf-8") as f:
                data = json.load(f)
        except Exception as e:
            print(f"Nota: Impossibile leggere {json_path}: {e}")

    # Determine home and away team names
    home_name = args.home_team or data.get("home_team", {}).get("name", "A.C. C.V.")
    home_short = data.get("home_team", {}).get("short_name", home_name[:4].upper())
    away_name = args.away_team or data.get("away_team", {}).get("name", "REAL MATRID")
    away_short = data.get("away_team", {}).get("short_name", away_name[:4].upper())

    home_team = Team(
        name=home_name,
        short_name=home_short,
        primary_color=tuple(data["home_team"]["primary_color"]) if "home_team" in data and "primary_color" in data["home_team"] else None,
        logo_path=data.get("home_team", {}).get("logo_path")
    )
    away_team = Team(
        name=away_name,
        short_name=away_short,
        primary_color=tuple(data["away_team"]["primary_color"]) if "away_team" in data and "primary_color" in data["away_team"] else None,
        logo_path=data.get("away_team", {}).get("logo_path")
    )

    # Determine scores
    home_score = args.home_score
    away_score = args.away_score
    if args.score:
        parts = args.score.replace(":", "-").split("-")
        if len(parts) == 2 and parts[0].strip().isdigit() and parts[1].strip().isdigit():
            home_score = int(parts[0].strip())
            away_score = int(parts[1].strip())

    if home_score is None:
        home_score = data.get("home_score", 5)
    if away_score is None:
        away_score = data.get("away_score", 2)

    home_scorers = [Scorer(s["name"], s.get("goals", 1)) for s in data.get("home_scorers", [
        {"name": "Rossi", "goals": 3},
        {"name": "Bianchi", "goals": 2}
    ])]
    away_scorers = [Scorer(s["name"], s.get("goals", 1)) for s in data.get("away_scorers", [
        {"name": "Ferrari", "goals": 2}
    ])]

    match_res = MatchResult(
        home_team=home_team,
        away_team=away_team,
        home_score=home_score,
        away_score=away_score,
        home_scorers=home_scorers,
        away_scorers=away_scorers,
        tournament=data.get("tournament", "CAMPIONATO CALCETTO A5"),
        matchday=data.get("matchday", "MATCHDAY 10"),
        date=data.get("date", "24/07/2026"),
        time=data.get("time", "21:00"),
        location=data.get("location", "Campo Centrale"),
        mvp_name=data.get("mvp", {}).get("player_name", "Rossi") if isinstance(data.get("mvp"), dict) else "Rossi"
    )

    next_match_data = NextMatch(
        home_team=home_team,
        away_team=away_team,
        tournament=data.get("tournament", "CAMPIONATO CALCETTO A5"),
        matchday=data.get("next_matchday", "GIORNATA PROSSIMA"),
        date=data.get("next_date", "30/07/2026"),
        time=data.get("next_time", "21:00"),
        location=data.get("location", "Centro Sportivo")
    )

    mvp_info = data.get("mvp", {}) if isinstance(data.get("mvp"), dict) else {}
    mvp_data = MVP(
        player_name=mvp_info.get("player_name", "Mario Rossi"),
        jersey_number=str(mvp_info.get("jersey_number", "10")),
        position=mvp_info.get("position", "Attaccante"),
        goals=mvp_info.get("goals", 3),
        assists=mvp_info.get("assists", 1),
        saves=mvp_info.get("saves", 0),
        rating=str(mvp_info.get("rating", "9.5")),
        match_opponent=away_team.name,
        match_date=data.get("date", "24/07/2026")
    )

    return match_res, next_match_data, mvp_data


def resolve_aspect_ratios(fmt_arg: str):
    """Map command line format choice to AspectRatio enum list."""
    if fmt_arg in ["all"]:
        return [
            AspectRatio.RATIO_9_16,
            AspectRatio.RATIO_4_3,
            AspectRatio.RATIO_16_9,
            AspectRatio.RATIO_1_1,
            AspectRatio.RATIO_4_5,
        ]
    elif fmt_arg in ["both"]:
        return [AspectRatio.RATIO_9_16, AspectRatio.RATIO_4_3]
    elif fmt_arg in ["story"]:
        return [AspectRatio.RATIO_9_16]
    elif fmt_arg in ["post"]:
        return [AspectRatio.RATIO_1_1]
    elif fmt_arg == "9:16":
        return [AspectRatio.RATIO_9_16]
    elif fmt_arg == "4:3":
        return [AspectRatio.RATIO_4_3]
    elif fmt_arg == "16:9":
        return [AspectRatio.RATIO_16_9]
    elif fmt_arg == "1:1":
        return [AspectRatio.RATIO_1_1]
    elif fmt_arg == "4:5":
        return [AspectRatio.RATIO_4_5]
    return [AspectRatio.RATIO_9_16, AspectRatio.RATIO_4_3]


def generate():
    args = parse_args()
    out_dir = Path(args.output)
    out_dir.mkdir(parents=True, exist_ok=True)

    # Generate sample team logos in assets/logos/ if missing
    generate_sample_team_logos()

    print("=" * 60)
    print("  ⚽ ACCV SOCIAL GRAPHICS AUTOMATION GENERATOR ⚽")
    print("=" * 60)

    match_res, next_match_data, mvp_data = load_match_data(args)
    formats_to_gen = resolve_aspect_ratios(args.format)

    gen_kwargs = {
        "bg_path": args.bg_image,
        "emotion": args.emotion,
        "contrast_factor": args.contrast,
        "remove_contrast": args.no_contrast,
    }

    # 1. Match Result
    if args.type in ["all", "result"]:
        print(f"\n Rendering RISULTATO FINALE graphics (style: {args.style.upper()})...")
        for fmt in formats_to_gen:
            filename = f"match_result_{args.style}_{fmt.value.replace(':', '_')}.png"
            gen = MatchResultGenerator(match_res, aspect_ratio=fmt, style=args.style, **gen_kwargs)
            path = gen.save(str(out_dir / filename))
            print(f"  ✓ Saved {fmt.value.upper()}: {path}")

    # 2. Next Match
    if args.type in ["all", "next"]:
        print("\n Rendering PROSSIMA PARTITA graphics...")
        for fmt in formats_to_gen:
            filename = f"next_match_{fmt.value.replace(':', '_')}.png"
            gen = NextMatchGenerator(next_match_data, aspect_ratio=fmt, **gen_kwargs)
            path = gen.save(str(out_dir / filename))
            print(f"  ✓ Saved {fmt.value.upper()}: {path}")

    # 3. MVP
    if args.type in ["all", "mvp"]:
        print("\n Rendering MIGLIORE IN CAMPO (MVP) graphics...")
        for fmt in formats_to_gen:
            filename = f"mvp_{fmt.value.replace(':', '_')}.png"
            gen = MVPGenerator(mvp_data, aspect_ratio=fmt, **gen_kwargs)
            path = gen.save(str(out_dir / filename))
            print(f"  ✓ Saved {fmt.value.upper()}: {path}")

    print("\n" + "=" * 60)
    print(f"  ✨ GRAFICHE GENERATE CON SUCCESSO IN: {out_dir} ✨")
    print("=" * 60)


if __name__ == "__main__":
    generate()
