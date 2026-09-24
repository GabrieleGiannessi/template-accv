"""
Main entry point script for template-accv social graphics automation.
Provides unified CLI for:
- Starting local Web Studio server (`python main.py serve`)
- Generating match graphics (`python main.py generate` or `python main.py [args]`)
- Generating Panini trading card stickers (`python main.py figurina`)
"""

import argparse
import json
from pathlib import Path
import sys

# Ensure src/ is on python path
src_path = Path(__file__).resolve().parent / "src"
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

from template_accv.config import (
    AspectRatio,
    OUTPUT_DIR,
    DATA_DIR,
    FIGURINE_OUTPUT_DIR,
    PLAYERS_TRANSPARENT_DIR,
    FigurinaFormat,
    GraphicStyle,
)
from template_accv.models import Team, Scorer, MatchResult, NextMatch, MVP, FigurinaCard
from template_accv.generators.match_result import MatchResultGenerator
from template_accv.generators.next_match import NextMatchGenerator
from template_accv.generators.mvp import MVPGenerator
from template_accv.generators.figurina import (
    FigurinaGenerator,
    get_player_card,
    load_roster,
)
from template_accv.server import run_server


def add_generator_arguments(parser):
    """Add generation arguments to a parser."""
    parser.add_argument(
        "--type", "-t",
        choices=["all", "result", "next", "mvp", "figurina"],
        default="all",
        help="Tipo di grafica da generare (default: all)"
    )
    parser.add_argument(
        "--format", "-f",
        default="both",
        help="Formato output: 9:16, 4:3, 1:1, 4:5, 16:9, both, story, post, card, all"
    )
    parser.add_argument(
        "--home-team", "-ht",
        type=str,
        default=None,
        help="Nome squadra di casa (es. 'A.C.C.V.')"
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
        help="Punteggio risultato (es. '5-2' o '5:2')"
    )
    parser.add_argument(
        "--home-score",
        type=int,
        default=None,
        help="Gol segnati squadra di casa"
    )
    parser.add_argument(
        "--away-score",
        type=int,
        default=None,
        help="Gol segnati squadra ospite"
    )
    parser.add_argument(
        "--emotion", "-e",
        type=str,
        default=None,
        help="Categoria emozionale sfondo (es. felicità, tristezza, polemica, normale, foto squadra)"
    )
    parser.add_argument(
        "--bg-image", "--bg",
        dest="bg_image",
        type=str,
        default=None,
        help="Percorso immagine di sfondo specifica"
    )
    parser.add_argument(
        "--contrast",
        type=float,
        default=1.0,
        help="Fattore contrasto per lo sfondo (default: 1.0)"
    )
    parser.add_argument(
        "--no-contrast",
        action="store_true",
        help="Riduci contrasto sfondo per effetto flat"
    )
    parser.add_argument(
        "--player", "-p",
        type=str,
        default=None,
        help="ID o nome giocatore per la figurina (es. 'bouba', 'elia')"
    )
    parser.add_argument(
        "--no-banner",
        action="store_true",
        help="Ometti targhetta nome/ruolo/logo sulla figurina"
    )
    parser.add_argument(
        "--data", "-d",
        type=str,
        default=str(DATA_DIR / "example_match.json"),
        help="File JSON con i dati della partita (default: data/example_match.json)"
    )
    parser.add_argument(
        "--output", "-o",
        type=str,
        default=str(OUTPUT_DIR),
        help="Cartella di destinazione per salvare le immagini (default: output/)"
    )


def parse_args():
    parser = argparse.ArgumentParser(
        description="⚽ ACCV - Calcetto Social Graphics Automation & Studio",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    
    subparsers = parser.add_subparsers(dest="command", help="Comando da eseguire")

    # Subcommand: serve
    serve_parser = subparsers.add_parser("serve", help="Avvia il server Web locale con UI Studio")
    serve_parser.add_argument("--host", default="0.0.0.0", help="Indirizzo host (default: 0.0.0.0)")
    serve_parser.add_argument("--port", "-p", type=int, default=8000, help="Porta HTTP (default: 8000)")
    serve_parser.add_argument("--open", "-b", action="store_true", help="Apri automaticamente il browser")
    serve_parser.add_argument("--public", action="store_true", help="Mostra opzioni per condividere pubblicamente la demo online")

    # Subcommand: generate
    gen_parser = subparsers.add_parser("generate", help="Genera grafiche social per partite")
    add_generator_arguments(gen_parser)

    # Subcommand: figurina
    fig_parser = subparsers.add_parser("figurina", help="Genera figurine Panini calciatori ACCV")
    fig_parser.add_argument("--player", "-p", type=str, default=None, help="ID o nome giocatore")
    fig_parser.add_argument("--format", "-f", default="all", choices=["card", "story", "post", "all"], help="Formato figurina")
    fig_parser.add_argument("--no-banner", action="store_true", help="Disabilita targhetta Panini")
    fig_parser.add_argument("--interactive", "-i", action="store_true", help="Avvia procedura guidata interattiva")
    fig_parser.add_argument("--output", "-o", type=str, default=str(FIGURINE_OUTPUT_DIR), help="Cartella output")

    # Top-level arguments for backwards-compatible direct invocation (e.g. python main.py --type result)
    add_generator_arguments(parser)
    parser.add_argument("--interactive", "-i", action="store_true", help="Modalità interattiva (per figurine)")

    return parser.parse_args()


def load_match_data(json_path_str: str, args):
    """Load match data from JSON file and apply CLI overrides."""
    json_path = Path(json_path_str)
    data = {}
    if json_path.exists():
        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)

    # Teams with overrides
    home_name = args.home_team if getattr(args, "home_team", None) else data.get("home_team", {}).get("name", "A.C.C.V.")
    away_name = args.away_team if getattr(args, "away_team", None) else data.get("away_team", {}).get("name", "Avversario")
    
    home_team = Team(
        name=home_name,
        short_name=data.get("home_team", {}).get("short_name", "ACCV"),
        primary_color=tuple(data.get("home_team", {}).get("primary_color", [0, 229, 255])),
        logo_path=data.get("home_team", {}).get("logo_path")
    )
    away_team = Team(
        name=away_name,
        short_name=data.get("away_team", {}).get("short_name", "OPP"),
        primary_color=tuple(data.get("away_team", {}).get("primary_color", [255, 71, 87])),
        logo_path=data.get("away_team", {}).get("logo_path")
    )

    # Scores with overrides
    home_score = data.get("home_score", 0)
    away_score = data.get("away_score", 0)
    if getattr(args, "score", None):
        parts = args.score.replace(":", "-").split("-")
        if len(parts) == 2:
            try:
                home_score = int(parts[0].strip())
                away_score = int(parts[1].strip())
            except ValueError:
                pass
    if getattr(args, "home_score", None) is not None:
        home_score = args.home_score
    if getattr(args, "away_score", None) is not None:
        away_score = args.away_score

    home_scorers = [Scorer(s["name"], s.get("goals", 1)) for s in data.get("home_scorers", [])]
    away_scorers = [Scorer(s["name"], s.get("goals", 1)) for s in data.get("away_scorers", [])]

    match_res = MatchResult(
        home_team=home_team,
        away_team=away_team,
        home_score=home_score,
        away_score=away_score,
        home_scorers=home_scorers,
        away_scorers=away_scorers,
        tournament=data.get("tournament", "CAMPIONATO CALCETTO A 7"),
        matchday=data.get("matchday", "MATCHDAY"),
        date=data.get("date", ""),
        time=data.get("time", ""),
        location=data.get("location", ""),
        mvp_name=data.get("mvp", {}).get("player_name") if isinstance(data.get("mvp"), dict) else None
    )

    next_match_data = NextMatch(
        home_team=home_team,
        away_team=away_team,
        tournament=data.get("tournament", "CAMPIONATO CALCETTO A5"),
        matchday=data.get("next_matchday", "PROSSIMA PARTITA"),
        date=data.get("next_date", data.get("date", "")),
        time=data.get("next_time", data.get("time", "")),
        location=data.get("location", "")
    )

    mvp_info = data.get("mvp", {}) if isinstance(data.get("mvp"), dict) else {}
    mvp_data = MVP(
        player_name=mvp_info.get("player_name", "Giocatore ACCV"),
        jersey_number=str(mvp_info.get("jersey_number", "10")),
        position=mvp_info.get("position", "Attaccante"),
        goals=mvp_info.get("goals", 0),
        assists=mvp_info.get("assists", 0),
        saves=mvp_info.get("saves", 0),
        rating=str(mvp_info.get("rating", "9.0")),
        match_opponent=away_team.name,
        match_date=data.get("date", "")
    )

    return match_res, next_match_data, mvp_data


def resolve_formats(fmt_arg: str):
    """Map format string to list of AspectRatio enums."""
    fmt = fmt_arg.lower().strip()
    if fmt == "all":
        return [
            AspectRatio.RATIO_9_16,
            AspectRatio.RATIO_4_3,
            AspectRatio.RATIO_1_1,
            AspectRatio.RATIO_4_5,
            AspectRatio.RATIO_16_9,
        ]
    elif fmt in ["both", "default"]:
        return [AspectRatio.POST, AspectRatio.STORY]
    elif fmt in ["9:16", "story"]:
        return [AspectRatio.RATIO_9_16]
    elif fmt in ["4:3"]:
        return [AspectRatio.RATIO_4_3]
    elif fmt in ["1:1", "post"]:
        return [AspectRatio.RATIO_1_1]
    elif fmt in ["4:5"]:
        return [AspectRatio.RATIO_4_5]
    elif fmt in ["16:9"]:
        return [AspectRatio.RATIO_16_9]
    return [AspectRatio.RATIO_9_16]


def run_figurina(args):
    """Execute figurina generation."""
    out_dir = Path(getattr(args, "output", str(FIGURINE_OUTPUT_DIR)))
    out_dir.mkdir(parents=True, exist_ok=True)
    with_banner = not getattr(args, "no_banner", False)
    roster = load_roster()

    fmt_arg = getattr(args, "format", "all").lower()
    fmt_map = {
        "card": [FigurinaFormat.CARD],
        "story": [FigurinaFormat.STORY],
        "post": [FigurinaFormat.POST],
        "all": [FigurinaFormat.CARD, FigurinaFormat.STORY],
    }
    formats_to_render = fmt_map.get(fmt_arg, [FigurinaFormat.CARD, FigurinaFormat.STORY])

    player_arg = getattr(args, "player", None)
    if player_arg:
        card_data = get_player_card(player_arg, roster=roster, with_banner=with_banner)
        for fmt in formats_to_render:
            suffix = f"_{fmt.value}" if fmt != FigurinaFormat.CARD else ""
            out_file = out_dir / f"{Path(card_data.photo_path).stem}{suffix}.png"
            gen = FigurinaGenerator(card_data, format=fmt)
            gen.save(out_file)
            print(f"  ✓ Saved {card_data.player_name} ({fmt.value.upper()}): {out_file}")
    else:
        photo_files = sorted(
            [p for p in PLAYERS_TRANSPARENT_DIR.iterdir() if p.is_file() and p.suffix.lower() in [".png", ".webp"]]
        )
        print(f"\n🚀 Rendering FIGURINE per {len(photo_files)} giocatori...")
        for p_file in photo_files:
            card_data = get_player_card(p_file.name, roster=roster, with_banner=with_banner)
            for fmt in formats_to_render:
                suffix = f"_{fmt.value}" if fmt != FigurinaFormat.CARD else ""
                out_file = out_dir / f"{p_file.stem}{suffix}.png"
                gen = FigurinaGenerator(card_data, format=fmt)
                gen.save(out_file)
                print(f"  ✓ Saved {card_data.player_name} ({fmt.value.upper()}): {out_file.name}")


def run_generation(args):
    """Run graphic generation based on CLI args."""
    out_dir = Path(getattr(args, "output", str(OUTPUT_DIR)))
    out_dir.mkdir(parents=True, exist_ok=True)

    print("=" * 66)
    print("  ⚽ ACCV SOCIAL GRAPHICS AUTOMATION GENERATOR ⚽")
    print("=" * 66)

    graphic_type = getattr(args, "type", "all")
    if graphic_type == "figurina":
        run_figurina(args)
        return

    match_res, next_match_data, mvp_data = load_match_data(getattr(args, "data", str(DATA_DIR / "example_match.json")), args)
    formats_to_gen = resolve_formats(getattr(args, "format", "both"))

    emotion = getattr(args, "emotion", None)
    bg_image = getattr(args, "bg_image", None)
    contrast = getattr(args, "contrast", 1.0)
    no_contrast = getattr(args, "no_contrast", False)

    # 1. Match Result
    if graphic_type in ["all", "result"]:
        print("\n🎨 Rendering RISULTATO FINALE...")
        for fmt in formats_to_gen:
            filename = f"match_result_{fmt.value.replace(':', '_')}.png"
            gen = MatchResultGenerator(
                match_res,
                aspect_ratio=fmt,
                bg_path=bg_image,
                emotion=emotion,
                contrast_factor=contrast,
                remove_contrast=no_contrast
            )
            path = gen.save(str(out_dir / filename))
            print(f"  ✓ Saved {fmt.value.upper()}: {path}")

    # 2. Next Match
    if graphic_type in ["all", "next"]:
        print("\n📅 Rendering PROSSIMA PARTITA...")
        for fmt in formats_to_gen:
            filename = f"next_match_{fmt.value.replace(':', '_')}.png"
            gen = NextMatchGenerator(
                next_match_data,
                aspect_ratio=fmt,
                bg_path=bg_image,
                emotion=emotion,
                contrast_factor=contrast,
                remove_contrast=no_contrast
            )
            path = gen.save(str(out_dir / filename))
            print(f"  ✓ Saved {fmt.value.upper()}: {path}")

    # 3. MVP
    if graphic_type in ["all", "mvp"]:
        print("\n⭐ Rendering MIGLIORE IN CAMPO (MVP)...")
        for fmt in formats_to_gen:
            filename = f"mvp_{fmt.value.replace(':', '_')}.png"
            gen = MVPGenerator(
                mvp_data,
                aspect_ratio=fmt,
                bg_path=bg_image,
                emotion=emotion,
                contrast_factor=contrast,
                remove_contrast=no_contrast
            )
            path = gen.save(str(out_dir / filename))
            print(f"  ✓ Saved {fmt.value.upper()}: {path}")

    print("\n" + "=" * 66)
    print(f"  ✨ GRAFICHE GENERATE CON SUCCESSO IN: {out_dir} ✨")
    print("=" * 66)


def main():
    args = parse_args()
    cmd = getattr(args, "command", None)

    if cmd == "serve":
        run_server(host=args.host, port=args.port, open_browser=args.open, public=getattr(args, "public", False))
    elif cmd == "figurina":
        if getattr(args, "interactive", False):
            from generate_figurine import interactive_wizard
            interactive_wizard()
        else:
            run_figurina(args)
    elif cmd == "generate":
        run_generation(args)
    else:
        # Default behavior or legacy arguments passed without subcommand
        if getattr(args, "interactive", False) or getattr(args, "type", None) == "figurina":
            run_figurina(args)
        else:
            run_generation(args)


if __name__ == "__main__":
    main()
