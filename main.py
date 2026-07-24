"""
Main entry point script for template-accv graphic template generation.
Reads match sample data and renders Instagram Post (1:1) and Story (9:16) graphics.
Supports generating individual graphic types or formats via CLI parameters.
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


def parse_args():
    parser = argparse.ArgumentParser(
        description="Generatore automatizzato di grafiche social per A.C. C.V."
    )
    parser.add_argument(
        "--type", "-t",
        choices=["all", "result", "next", "mvp"],
        default="all",
        help="Tipo di grafica da generare (default: all)"
    )
    parser.add_argument(
        "--format", "-f",
        choices=["both", "post", "story"],
        default="both",
        help="Formato di output da generare (default: both)"
    )
    parser.add_argument(
        "--data", "-d",
        type=str,
        default=str(DATA_DIR / "example_match.json"),
        help="Percorso al file JSON con i dati della partita"
    )
    parser.add_argument(
        "--output", "-o",
        type=str,
        default=str(OUTPUT_DIR),
        help="Cartella di destinazione per salvare le immagini"
    )
    return parser.parse_args()


def load_match_data(json_path_str: str):
    json_path = Path(json_path_str)
    if json_path.exists():
        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        home_team = Team(
            name=data["home_team"]["name"],
            short_name=data["home_team"]["short_name"],
            primary_color=tuple(data["home_team"]["primary_color"]) if "primary_color" in data["home_team"] else None,
            logo_path=data["home_team"].get("logo_path")
        )
        away_team = Team(
            name=data["away_team"]["name"],
            short_name=data["away_team"]["short_name"],
            primary_color=tuple(data["away_team"]["primary_color"]) if "primary_color" in data["away_team"] else None,
            logo_path=data["away_team"].get("logo_path")
        )
        home_scorers = [Scorer(s["name"], s.get("goals", 1)) for s in data.get("home_scorers", [])]
        away_scorers = [Scorer(s["name"], s.get("goals", 1)) for s in data.get("away_scorers", [])]

        match_res = MatchResult(
            home_team=home_team,
            away_team=away_team,
            home_score=data.get("home_score", 0),
            away_score=data.get("away_score", 0),
            home_scorers=home_scorers,
            away_scorers=away_scorers,
            tournament=data.get("tournament", "CAMPIONATO CALCETTO A5"),
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
    else:
        # Fallback default objects
        home_team = Team(name="A.C.C.V", short_name="ACCV", primary_color=(0, 229, 255))
        away_team = Team(name="REAL MATRID", short_name="MAT", primary_color=(255, 71, 87))
        match_res = MatchResult(
            home_team=home_team,
            away_team=away_team,
            home_score=5,
            away_score=2,
            home_scorers=[Scorer("Rossi", 3), Scorer("Bianchi", 2)],
            away_scorers=[Scorer("Ferrari", 2)],
            mvp_name="Rossi"
        )
        next_match_data = NextMatch(home_team=home_team, away_team=away_team, date="30/07/2026")
        mvp_data = MVP(player_name="Mario Rossi", goals=3, assists=1)
        return match_res, next_match_data, mvp_data


def generate():
    args = parse_args()
    out_dir = Path(args.output)
    out_dir.mkdir(parents=True, exist_ok=True)

    print("=" * 60)
    print("  ⚽ ACCV SOCIAL GRAPHICS AUTOMATION GENERATOR ⚽")
    print("=" * 60)

    match_res, next_match_data, mvp_data = load_match_data(args.data)
    
    formats_to_gen = []
    if args.format in ["both", "post"]:
        formats_to_gen.append(AspectRatio.POST)
    if args.format in ["both", "story"]:
        formats_to_gen.append(AspectRatio.STORY)

    # 1. Match Result
    if args.type in ["all", "result"]:
        print("\n Rendering RISULTATO FINALE graphics...")
        for fmt in formats_to_gen:
            filename = f"match_result_{fmt.value}.png"
            gen = MatchResultGenerator(match_res, aspect_ratio=fmt)
            path = gen.save(str(out_dir / filename))
            print(f"  ✓ Saved {fmt.value.upper()}: {path}")

    # 2. Next Match
    if args.type in ["all", "next"]:
        print("\n Rendering PROSSIMA PARTITA graphics...")
        for fmt in formats_to_gen:
            filename = f"next_match_{fmt.value}.png"
            gen = NextMatchGenerator(next_match_data, aspect_ratio=fmt)
            path = gen.save(str(out_dir / filename))
            print(f"  ✓ Saved {fmt.value.upper()}: {path}")

    # 3. MVP
    if args.type in ["all", "mvp"]:
        print("\n Rendering MIGLIORE IN CAMPO (MVP) graphics...")
        for fmt in formats_to_gen:
            filename = f"mvp_{fmt.value}.png"
            gen = MVPGenerator(mvp_data, aspect_ratio=fmt)
            path = gen.save(str(out_dir / filename))
            print(f"  ✓ Saved {fmt.value.upper()}: {path}")

    print("\n" + "=" * 60)
    print(f"  ✨ GRAFICHE GENERATE CON SUCCESSO IN: {out_dir} ✨")
    print("=" * 60)


if __name__ == "__main__":
    generate()
