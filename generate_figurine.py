#!/usr/bin/env python3
"""
Figurine Calciatori Panini ACCV - Generatore Automatico e Guidato.

Script intuitivo e semplice pensato per essere utilizzato da chiunque:
- Esecuzione diretta: genera automaticamente le figurine per tutti i giocatori della squadra.
- Modalità interattiva guidata: consente di selezionare facilmente formato e giocatore.
- Supporto per formati social (Storie Instagram 9:16, Post 4:5 e Card classica 2:3).
"""

import argparse
from pathlib import Path
import sys

# Ensure src/ is on python path
BASE_DIR = Path(__file__).resolve().parent
src_path = BASE_DIR / "src"
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

from template_accv.config import (
    FIGURINE_OUTPUT_DIR,
    PLAYERS_TRANSPARENT_DIR,
    FigurinaFormat,
)
from template_accv.generators.figurina import (
    FigurinaGenerator,
    generate_all_figurine,
    get_player_card,
    load_roster,
)


def print_banner():
    print("""
╔══════════════════════════════════════════════════════════════════╗
║   ⚽  A.C.C.V.  -  GENERATORE FIGURINE STILE CALCIATORI PANINI   ║
║             Serie B Perini  •  Stagione Ufficiale                ║
╚══════════════════════════════════════════════════════════════════╝
""")


def get_available_players(photos_dir: Path, roster: dict):
    """Retrieve list of available players with their friendly display names."""
    files = sorted(
        [p for p in photos_dir.iterdir() if p.is_file() and p.suffix.lower() in [".png", ".webp"]]
    )
    players = []
    for f in files:
        key = f.stem.lower()
        info = roster.get(key, {})
        name = info.get("name", key.replace("_", "-").title())
        role = info.get("role", "Giocatore")
        num = f"#{info.get('number')}" if info.get("number") else ""
        players.append({
            "key": key,
            "filename": f.name,
            "path": f,
            "name": name,
            "role": role,
            "number": num,
        })
    return players


def interactive_wizard():
    """Guided interactive menu for non-expert users."""
    print_banner()
    roster = load_roster()
    players = get_available_players(PLAYERS_TRANSPARENT_DIR, roster)

    if not players:
        print(f"❌ Nessuna foto trovata nella cartella: {PLAYERS_TRANSPARENT_DIR}")
        return

    print(f"📁 Trovati {len(players)} giocatori nell'archivio fotografico.\n")

    # 1. Scelta Giocatori
    print("1️⃣  Cosa desideri generare?")
    print("   [1] Tutte le figurine della squadra (Batch)")
    print("   [2] Un singolo giocatore a scelta")
    
    choice_mode = input("\n👉 Seleziona un'opzione (1 o 2, default: 1): ").strip() or "1"
    
    selected_player = None
    if choice_mode == "2":
        print("\nElenco giocatori disponibili:")
        for idx, p in enumerate(players, 1):
            role_num = f"({p['role']} {p['number']})".strip()
            print(f"   [{idx:2d}] {p['name']:<24} {role_num}")

        sel_idx = input(f"\n👉 Inserisci il numero del giocatore (1-{len(players)}): ").strip()
        try:
            val = int(sel_idx)
            if 1 <= val <= len(players):
                selected_player = players[val - 1]
            else:
                print("⚠️ Indice non valido, genero tutte le figurine.")
        except ValueError:
            # Check by key or name
            match = next((p for p in players if p["key"] == sel_idx.lower() or sel_idx.lower() in p["name"].lower()), None)
            if match:
                selected_player = match
            else:
                print("⚠️ Giocatore non trovato, genero tutte le figurine.")

    # 2. Scelta Formato
    print("\n2️⃣  Che formato grafico desideri?")
    print("   [1] Solo Figurina Classica (Card Panini 1024x1536)")
    print("   [2] Formato Storia Instagram (9:16 - 1080x1920 con effetto 3D e sfondo)")
    print("   [3] Formato Post Instagram (4:5 - 1080x1350)")
    print("   [4] Entrambi: Figurina Classica + Storia Instagram (Consigliato!)")

    choice_fmt = input("\n👉 Seleziona il formato (1-4, default: 4): ").strip() or "4"
    fmt_map = {
        "1": [FigurinaFormat.CARD],
        "2": [FigurinaFormat.STORY],
        "3": [FigurinaFormat.POST],
        "4": [FigurinaFormat.CARD, FigurinaFormat.STORY],
    }
    formats_to_render = fmt_map.get(choice_fmt, [FigurinaFormat.CARD, FigurinaFormat.STORY])

    # 3. Targhetta Nome/Ruolo
    print("\n3️⃣  Vuoi includere la targhetta con nome, ruolo e logo societario?")
    print("   [1] Sì, stile album Panini (Consigliato)")
    print("   [2] No, solo la foto pulita del giocatore nel template")

    choice_banner = input("\n👉 Opzione (1 o 2, default: 1): ").strip() or "1"
    with_banner = choice_banner != "2"

    # Esecuzione
    print("\n" + "=" * 66)
    print("🚀 GENERAZIONE IN CORSO...")
    print("=" * 66 + "\n")

    out_dir = FIGURINE_OUTPUT_DIR
    out_dir.mkdir(parents=True, exist_ok=True)

    players_to_run = [selected_player] if selected_player else players
    count = 0

    for p in players_to_run:
        card_data = get_player_card(p["filename"], roster=roster, with_banner=with_banner)
        for fmt in formats_to_render:
            suffix = f"_{fmt.value}" if fmt != FigurinaFormat.CARD else ""
            out_file = out_dir / f"{p['key']}{suffix}.png"
            gen = FigurinaGenerator(card_data, format=fmt)
            gen.save(out_file)
            print(f"  ✓ {card_data.player_name:<22} [{fmt.value.upper()}] -> {out_file.name}")
            count += 1

    print("\n" + "=" * 66)
    print(f"✨ COMPLETATO! Generate con successo {count} immagini in:")
    print(f"📁 {out_dir.resolve()}")
    print("=" * 66 + "\n")


def parse_arguments():
    parser = argparse.ArgumentParser(
        description="Generatore Figurine Panini ACCV - Facile e Veloce",
        formatter_class=argparse.RawTextHelpFormatter,
    )
    parser.add_argument(
        "--player", "-p",
        type=str,
        default=None,
        help="ID o nome del giocatore (es. 'bouba', 'elia', 'g-giannessi').\nSe non specificato, genera tutti i giocatori.",
    )
    parser.add_argument(
        "--format", "-f",
        choices=["card", "story", "post", "all"],
        default="all",
        help="Formato grafico:\n - card: figurina pura 1024x1536\n - story: storia Instagram 1080x1920 con sfondo e ombra 3D\n - post: post Instagram 4:5 1080x1350\n - all: figurina classica + storia (default: all)",
    )
    parser.add_argument(
        "--no-banner",
        action="store_true",
        help="Disabilita la targhetta con nome, ruolo e logo",
    )
    parser.add_argument(
        "--interactive", "-i",
        action="store_true",
        help="Avvia la procedura guidata interattiva passo-passo",
    )
    parser.add_argument(
        "--output", "-o",
        type=str,
        default=str(FIGURINE_OUTPUT_DIR),
        help=f"Cartella di destinazione (default: {FIGURINE_OUTPUT_DIR})",
    )
    return parser.parse_args()


def main():
    args = parse_arguments()

    # If --interactive or no arguments provided and stdin is a terminal, launch wizard
    if args.interactive or (len(sys.argv) == 1 and sys.stdin.isatty()):
        try:
            interactive_wizard()
            return
        except KeyboardInterrupt:
            print("\n\nOperazione annullata dall'utente.")
            return

    # Direct CLI execution
    print_banner()
    out_dir = Path(args.output)
    out_dir.mkdir(parents=True, exist_ok=True)
    with_banner = not args.no_banner
    roster = load_roster()

    fmt_map = {
        "card": [FigurinaFormat.CARD],
        "story": [FigurinaFormat.STORY],
        "post": [FigurinaFormat.POST],
        "all": [FigurinaFormat.CARD, FigurinaFormat.STORY],
    }
    formats_to_render = fmt_map.get(args.format, [FigurinaFormat.CARD, FigurinaFormat.STORY])

    if args.player:
        card_data = get_player_card(args.player, roster=roster, with_banner=with_banner)
        for fmt in formats_to_render:
            suffix = f"_{fmt.value}" if fmt != FigurinaFormat.CARD else ""
            out_file = out_dir / f"{Path(card_data.photo_path).stem}{suffix}.png"
            gen = FigurinaGenerator(card_data, format=fmt)
            gen.save(out_file)
            print(f"  ✓ Generata figurina: {card_data.player_name} [{fmt.value.upper()}] -> {out_file}")
    else:
        photo_files = sorted(
            [p for p in PLAYERS_TRANSPARENT_DIR.iterdir() if p.is_file() and p.suffix.lower() in [".png", ".webp"]]
        )
        print(f"🚀 Generazione batch per {len(photo_files)} giocatori...")
        for p_file in photo_files:
            card_data = get_player_card(p_file.name, roster=roster, with_banner=with_banner)
            for fmt in formats_to_render:
                suffix = f"_{fmt.value}" if fmt != FigurinaFormat.CARD else ""
                out_file = out_dir / f"{p_file.stem}{suffix}.png"
                gen = FigurinaGenerator(card_data, format=fmt)
                gen.save(out_file)
                print(f"  ✓ {card_data.player_name:<22} [{fmt.value.upper()}] -> {out_file.name}")

    print("\n" + "=" * 66)
    print(f"✨ COMPLETATO! Grafiche salvate in: {out_dir.resolve()}")
    print("=" * 66 + "\n")


if __name__ == "__main__":
    main()
