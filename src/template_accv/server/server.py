"""
HTTP Web Server & REST API for ACCV Social Graphics Automation.
Provides in-memory graphic rendering, live previews, asset management, and static UI serving.
Zero-dependency: uses Python 3 standard library ThreadingHTTPServer.
"""

import base64
from http.server import HTTPServer, ThreadingHTTPServer, SimpleHTTPRequestHandler
import io
import json
import mimetypes
import os
from pathlib import Path
import re
import sys
from typing import Any, Dict, Optional
import urllib.parse
import webbrowser
import zipfile

from template_accv.config import (
    ASSETS_DIR,
    BASE_DIR,
    DATA_DIR,
    LOGOS_DIR,
    OUTPUT_DIR,
    PLAYERS_TRANSPARENT_DIR,
    BACKGROUNDS_DIR,
    AspectRatio,
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

WEB_DIR = BASE_DIR / "web"
TEAMS_FILE = DATA_DIR / "teams.json"
PLAYERS_FILE = DATA_DIR / "players.json"


def load_teams() -> Dict[str, dict]:
    """Load teams from data/teams.json or initialize from assets/logos/."""
    if TEAMS_FILE.exists():
        try:
            with open(TEAMS_FILE, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception as e:
            print(f"Error loading teams.json: {e}")
            
    teams = {}
    if LOGOS_DIR.exists():
        for p in sorted(LOGOS_DIR.iterdir()):
            if p.is_file() and p.suffix.lower() in [".png", ".jpg", ".jpeg", ".webp"]:
                key = p.stem.lower()
                display_name = p.stem.replace("_", " ").title()
                if "accv" in key:
                    display_name = "A.C.C.V."
                    short = "ACCV"
                else:
                    words = [w for w in display_name.split() if w]
                    short = "".join([w[0] for w in words]).upper()[:4]
                    if len(short) < 3 and len(display_name) >= 3:
                        short = display_name[:3].upper()
                teams[key] = {
                    "name": display_name,
                    "short_name": short,
                    "primary_color": [16, 185, 129] if "accv" in key else [239, 68, 68],
                    "logo_filename": p.name
                }
    return teams


def save_teams(teams: Dict[str, dict]):
    """Save teams dictionary to data/teams.json."""
    with open(TEAMS_FILE, "w", encoding="utf-8") as f:
        json.dump(teams, f, indent=2, ensure_ascii=False)



def parse_aspect_ratio(val: str) -> AspectRatio:
    """Safely map string to AspectRatio enum."""
    val = (val or "").strip().lower()
    for ar in AspectRatio:
        if ar.value.lower() == val or ar.name.lower() == val:
            return ar
    mapping = {
        "9:16": AspectRatio.RATIO_9_16,
        "story": AspectRatio.RATIO_9_16,
        "4:3": AspectRatio.RATIO_4_3,
        "16:9": AspectRatio.RATIO_16_9,
        "1:1": AspectRatio.RATIO_1_1,
        "post": AspectRatio.RATIO_1_1,
        "4:5": AspectRatio.RATIO_4_5,
    }
    return mapping.get(val, AspectRatio.RATIO_9_16)


def parse_figurina_format(val: str) -> FigurinaFormat:
    """Safely map string to FigurinaFormat enum."""
    val = (val or "").strip().lower()
    for ff in FigurinaFormat:
        if ff.value.lower() == val:
            return ff
    mapping = {
        "card": FigurinaFormat.CARD,
        "story": FigurinaFormat.STORY,
        "post": FigurinaFormat.POST,
        "all": FigurinaFormat.ALL,
    }
    return mapping.get(val, FigurinaFormat.CARD)


def build_match_result_from_payload(payload: Dict[str, Any]) -> tuple[MatchResultGenerator, AspectRatio]:
    """Helper to instantiate MatchResultGenerator from JSON dict."""
    fmt = parse_aspect_ratio(payload.get("format", "9:16"))
    style = payload.get("style", "classic")
    
    # Teams
    ht_data = payload.get("home_team", {})
    at_data = payload.get("away_team", {})
    
    ht_color = tuple(ht_data["primary_color"]) if ht_data.get("primary_color") else None
    at_color = tuple(at_data["primary_color"]) if at_data.get("primary_color") else None
    
    home_team = Team(
        name=ht_data.get("name", "A.C.C.V."),
        short_name=ht_data.get("short_name", "ACCV"),
        primary_color=ht_color,
        logo_path=ht_data.get("logo_path")
    )
    away_team = Team(
        name=at_data.get("name", "Avversario"),
        short_name=at_data.get("short_name", "OPP"),
        primary_color=at_color,
        logo_path=at_data.get("logo_path")
    )
    
    # Scorers
    home_scorers = [
        Scorer(name=s.get("name", ""), goals=int(s.get("goals", 1)))
        for s in payload.get("home_scorers", [])
        if s.get("name")
    ]
    away_scorers = [
        Scorer(name=s.get("name", ""), goals=int(s.get("goals", 1)))
        for s in payload.get("away_scorers", [])
        if s.get("name")
    ]
    
    match_result = MatchResult(
        home_team=home_team,
        away_team=away_team,
        home_score=int(payload.get("home_score", 0)),
        away_score=int(payload.get("away_score", 0)),
        home_scorers=home_scorers,
        away_scorers=away_scorers,
        tournament=payload.get("tournament", "CAMPIONATO CALCETTO A5"),
        matchday=payload.get("matchday", "GIORNATA 1"),
        date=payload.get("date", ""),
        time=payload.get("time", ""),
        location=payload.get("location", ""),
        mvp_name=payload.get("mvp_name") or None
    )
    
    gen = MatchResultGenerator(
        match_result=match_result,
        aspect_ratio=fmt,
        style=style,
        bg_path=payload.get("bg_path"),
        emotion=payload.get("emotion") or None,
        contrast_factor=float(payload.get("contrast_factor", 1.0)),
        remove_contrast=bool(payload.get("remove_contrast", False))
    )
    return gen, fmt


def build_next_match_from_payload(payload: Dict[str, Any]) -> tuple[NextMatchGenerator, AspectRatio]:
    """Helper to instantiate NextMatchGenerator from JSON dict."""
    fmt = parse_aspect_ratio(payload.get("format", "9:16"))
    
    ht_data = payload.get("home_team", {})
    at_data = payload.get("away_team", {})
    
    home_team = Team(
        name=ht_data.get("name", "A.C.C.V."),
        short_name=ht_data.get("short_name", "ACCV"),
        logo_path=ht_data.get("logo_path")
    )
    away_team = Team(
        name=at_data.get("name", "Avversario"),
        short_name=at_data.get("short_name", "OPP"),
        logo_path=at_data.get("logo_path")
    )
    
    next_match = NextMatch(
        home_team=home_team,
        away_team=away_team,
        tournament=payload.get("tournament", "CAMPIONATO CALCETTO A5"),
        matchday=payload.get("matchday", "PROSSIMA PARTITA"),
        date=payload.get("date", ""),
        time=payload.get("time", ""),
        location=payload.get("location", ""),
        field_name=payload.get("field_name", "")
    )
    
    gen = NextMatchGenerator(
        next_match=next_match,
        aspect_ratio=fmt,
        bg_path=payload.get("bg_path"),
        emotion=payload.get("emotion") or None,
        contrast_factor=float(payload.get("contrast_factor", 1.0)),
        remove_contrast=bool(payload.get("remove_contrast", False))
    )
    return gen, fmt


def build_mvp_from_payload(payload: Dict[str, Any]) -> tuple[MVPGenerator, AspectRatio]:
    """Helper to instantiate MVPGenerator from JSON dict."""
    fmt = parse_aspect_ratio(payload.get("format", "4:5"))
    
    mvp = MVP(
        player_name=payload.get("player_name", "Giocatore ACCV"),
        jersey_number=str(payload.get("jersey_number", "10")),
        position=payload.get("position", "Attaccante"),
        goals=int(payload.get("goals", 0)),
        assists=int(payload.get("assists", 0)),
        saves=int(payload.get("saves", 0)),
        rating=str(payload.get("rating", "9.0")),
        photo_path=payload.get("photo_path") or None,
        match_opponent=payload.get("match_opponent", ""),
        match_date=payload.get("match_date", "")
    )
    
    gen = MVPGenerator(
        mvp=mvp,
        aspect_ratio=fmt,
        bg_path=payload.get("bg_path"),
        emotion=payload.get("emotion") or None,
        contrast_factor=float(payload.get("contrast_factor", 1.0)),
        remove_contrast=bool(payload.get("remove_contrast", False))
    )
    return gen, fmt


def build_figurina_from_payload(payload: Dict[str, Any]) -> tuple[FigurinaGenerator, FigurinaFormat]:
    """Helper to instantiate FigurinaGenerator from JSON dict."""
    fmt = parse_figurina_format(payload.get("format", "story"))
    player_key = payload.get("player_key", "")
    roster = load_roster()
    
    with_banner = payload.get("with_banner", True)
    
    if player_key:
        card_data = get_player_card(player_key, roster=roster, with_banner=with_banner)
    else:
        # Custom input
        photo_path = payload.get("photo_path")
        if not photo_path:
            # Pick first available photo
            photos = sorted([p for p in PLAYERS_TRANSPARENT_DIR.iterdir() if p.is_file()])
            photo_path = str(photos[0]) if photos else ""
            
        card_data = FigurinaCard(
            player_name=payload.get("player_name", "Giocatore"),
            photo_path=photo_path,
            role=payload.get("role", "Giocatore"),
            jersey_number=payload.get("jersey_number"),
            team_name="A.C.C.V.",
            with_banner=with_banner
        )
        
    # Override any fields provided in payload
    if "player_name" in payload and payload["player_name"]:
        card_data.player_name = payload["player_name"]
    if "role" in payload:
        card_data.role = payload["role"]
    if "jersey_number" in payload:
        card_data.jersey_number = str(payload["jersey_number"]) if payload["jersey_number"] else None
    card_data.with_banner = with_banner
    
    gen = FigurinaGenerator(card_data=card_data, format=fmt)
    return gen, fmt


class ACCVRequestHandler(SimpleHTTPRequestHandler):
    """Custom HTTP handler with REST endpoints and static file serving."""
    
    def end_headers(self):
        # Enable CORS for local development and mobile network access
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")
        self.send_header("Cache-Control", "no-cache, no-store, must-revalidate")
        super().end_headers()

    def do_OPTIONS(self):
        self.send_response(200)
        self.end_headers()

    def do_GET(self):
        parsed = urllib.parse.urlparse(self.path)
        path = parsed.path

        if path == "/api/config":
            self.handle_get_config()
        elif path == "/api/players":
            self.handle_get_players()
        elif path == "/api/teams":
            self.handle_get_teams()
        elif path.startswith("/assets/"):
            # Serve asset file directly
            rel_path = path[len("/assets/"):]
            full_path = ASSETS_DIR / rel_path
            self.serve_file(full_path)
        elif path.startswith("/output/"):
            rel_path = path[len("/output/"):]
            full_path = OUTPUT_DIR / rel_path
            self.serve_file(full_path)
        elif path in ["/", "/index.html"]:
            self.serve_file(WEB_DIR / "index.html")
        elif path == "/app.jsx":
            self.serve_file(WEB_DIR / "app.jsx", content_type="text/javascript")
        else:
            # Fallback to web directory
            local_file = WEB_DIR / path.lstrip("/")
            if local_file.exists() and local_file.is_file():
                self.serve_file(local_file)
            else:
                self.serve_file(WEB_DIR / "index.html")

    def do_POST(self):
        parsed = urllib.parse.urlparse(self.path)
        path = parsed.path

        content_length = int(self.headers.get("Content-Length", 0))
        post_data = self.rfile.read(content_length)

        try:
            if path == "/api/preview/result":
                payload = json.loads(post_data.decode("utf-8")) if post_data else {}
                gen, fmt = build_match_result_from_payload(payload)
                self.send_json({
                    "status": "ok",
                    "preview": gen.to_data_uri(),
                    "width": gen.width,
                    "height": gen.height,
                    "format": fmt.value
                })

            elif path == "/api/preview/next":
                payload = json.loads(post_data.decode("utf-8")) if post_data else {}
                gen, fmt = build_next_match_from_payload(payload)
                self.send_json({
                    "status": "ok",
                    "preview": gen.to_data_uri(),
                    "width": gen.width,
                    "height": gen.height,
                    "format": fmt.value
                })

            elif path == "/api/preview/mvp":
                payload = json.loads(post_data.decode("utf-8")) if post_data else {}
                gen, fmt = build_mvp_from_payload(payload)
                self.send_json({
                    "status": "ok",
                    "preview": gen.to_data_uri(),
                    "width": gen.width,
                    "height": gen.height,
                    "format": fmt.value
                })

            elif path == "/api/preview/figurina":
                payload = json.loads(post_data.decode("utf-8")) if post_data else {}
                gen, fmt = build_figurina_from_payload(payload)
                rendered = gen.render()
                self.send_json({
                    "status": "ok",
                    "preview": gen.to_data_uri(),
                    "width": rendered.width,
                    "height": rendered.height,
                    "format": fmt.value
                })

            elif path == "/api/download":
                payload = json.loads(post_data.decode("utf-8")) if post_data else {}
                graphic_type = payload.get("type", "result")
                fmt_str = payload.get("format", "story")
                
                if graphic_type == "result":
                    gen, _ = build_match_result_from_payload(payload)
                    filename = f"match_result_{fmt_str}.png"
                elif graphic_type == "next":
                    gen, _ = build_next_match_from_payload(payload)
                    filename = f"next_match_{fmt_str}.png"
                elif graphic_type == "mvp":
                    gen, _ = build_mvp_from_payload(payload)
                    filename = f"mvp_{fmt_str}.png"
                elif graphic_type == "figurina":
                    gen, _ = build_figurina_from_payload(payload)
                    player_name = payload.get("player_name", "figurina").lower().replace(" ", "_")
                    filename = f"figurina_{player_name}_{fmt_str}.png"
                else:
                    self.send_error(400, "Unknown graphic type")
                    return

                img_bytes = gen.to_bytes()
                self.send_binary(img_bytes, filename=filename)

            elif path == "/api/download-all":
                payload = json.loads(post_data.decode("utf-8")) if post_data else {}
                graphic_type = payload.get("type", "result")
                
                zip_buffer = io.BytesIO()
                with zipfile.ZipFile(zip_buffer, "w", zipfile.ZIP_DEFLATED) as zf:
                    if graphic_type == "result":
                        formats = [AspectRatio.RATIO_9_16, AspectRatio.RATIO_4_3, AspectRatio.RATIO_1_1, AspectRatio.RATIO_4_5]
                        for fmt in formats:
                            payload_copy = dict(payload)
                            payload_copy["format"] = fmt.value
                            gen, _ = build_match_result_from_payload(payload_copy)
                            zf.writestr(f"match_result_{fmt.value.replace(':', '_')}.png", gen.to_bytes())

                    elif graphic_type == "next":
                        formats = [AspectRatio.RATIO_9_16, AspectRatio.RATIO_4_3, AspectRatio.RATIO_1_1]
                        for fmt in formats:
                            payload_copy = dict(payload)
                            payload_copy["format"] = fmt.value
                            gen, _ = build_next_match_from_payload(payload_copy)
                            zf.writestr(f"next_match_{fmt.value.replace(':', '_')}.png", gen.to_bytes())

                    elif graphic_type == "figurina":
                        # Generate for selected player in all 3 formats
                        for f in [FigurinaFormat.CARD, FigurinaFormat.STORY, FigurinaFormat.POST]:
                            payload_copy = dict(payload)
                            payload_copy["format"] = f.value
                            gen, _ = build_figurina_from_payload(payload_copy)
                            player_name = payload.get("player_name", "figurina").lower().replace(" ", "_")
                            zf.writestr(f"{player_name}_{f.value}.png", gen.to_bytes())
                
                zip_data = zip_buffer.getvalue()
                self.send_binary(zip_data, content_type="application/zip", filename=f"accv_{graphic_type}_pack.zip")

            elif path == "/api/teams":
                payload = json.loads(post_data.decode("utf-8")) if post_data else {}
                teams = load_teams()
                if "key" in payload and "name" in payload:
                    key = payload["key"].lower().strip().replace(" ", "_")
                    teams[key] = {
                        "name": payload["name"],
                        "extended_name": payload.get("extended_name", ""),
                        "short_name": payload.get("short_name", key[:3].upper()),
                        "primary_color": payload.get("primary_color", [16, 185, 129]),
                        "secondary_color": payload.get("secondary_color", [239, 68, 68]),
                        "tertiary_color": payload.get("tertiary_color", [59, 130, 246]),
                        "rivalry": payload.get("rivalry", 3),
                        "notes": payload.get("notes", ""),
                        "logo_filename": payload.get("logo_filename")
                    }
                elif isinstance(payload, dict):
                    teams = payload
                save_teams(teams)
                self.send_json({"status": "ok", "message": "Squadra salvata con successo", "teams": teams})

            elif path == "/api/teams/delete":
                payload = json.loads(post_data.decode("utf-8")) if post_data else {}
                key = payload.get("key")
                teams = load_teams()
                if key in teams:
                    del teams[key]
                    save_teams(teams)
                    self.send_json({"status": "ok", "message": f"Squadra {key} eliminata", "teams": teams})
                else:
                    self.send_json({"status": "error", "message": "Squadra non trovata"}, status=404)

            elif path == "/api/players":
                payload = json.loads(post_data.decode("utf-8")) if post_data else {}
                players = load_roster()
                if "key" in payload and "name" in payload:
                    key = payload["key"].lower().strip().replace(" ", "-")
                    players[key] = {
                        "name": payload["name"],
                        "role": payload.get("role", "Giocatore"),
                        "number": str(payload.get("number", "")),
                        "team": payload.get("team", "A.C.C.V.")
                    }
                elif isinstance(payload, dict):
                    players = payload
                with open(PLAYERS_FILE, "w", encoding="utf-8") as f:
                    json.dump(players, f, indent=2, ensure_ascii=False)
                self.send_json({"status": "ok", "message": "Calciatori salvati con successo", "players": players})

            elif path == "/api/players/delete":
                payload = json.loads(post_data.decode("utf-8")) if post_data else {}
                key = payload.get("key")
                players = load_roster()
                if key in players:
                    del players[key]
                    with open(PLAYERS_FILE, "w", encoding="utf-8") as f:
                        json.dump(players, f, indent=2, ensure_ascii=False)
                    self.send_json({"status": "ok", "message": f"Calciatore {key} eliminato", "players": players})
                else:
                    self.send_json({"status": "error", "message": "Calciatore non trovato"}, status=404)

            elif path == "/api/upload":
                # Handle base64 image upload
                payload = json.loads(post_data.decode("utf-8")) if post_data else {}
                upload_type = payload.get("upload_type", "background")  # background, player, logo
                filename = payload.get("filename", "upload.png")
                b64_data = payload.get("data", "")
                
                if "," in b64_data:
                    b64_data = b64_data.split(",", 1)[1]
                    
                file_bytes = base64.b64decode(b64_data)
                
                if upload_type == "background":
                    target_path = BACKGROUNDS_DIR / filename
                elif upload_type == "player":
                    target_path = PLAYERS_TRANSPARENT_DIR / filename
                elif upload_type == "logo":
                    target_path = LOGOS_DIR / filename
                else:
                    target_path = ASSETS_DIR / filename
                    
                target_path.parent.mkdir(parents=True, exist_ok=True)
                with open(target_path, "wb") as f:
                    f.write(file_bytes)
                    
                self.send_json({
                    "status": "ok",
                    "filename": filename,
                    "path": str(target_path),
                    "url": f"/assets/{target_path.relative_to(ASSETS_DIR)}"
                })

            else:
                self.send_error(404, "Endpoint not found")

        except Exception as e:
            import traceback
            traceback.print_exc()
            self.send_json({"status": "error", "error": str(e)}, status=500)

    def handle_get_config(self):
        """Return app configuration, teams, players, and options."""
        # 1. Teams (from data/teams.json or assets/logos/)
        teams_dict = load_teams()
        teams = []
        for key, t in sorted(teams_dict.items(), key=lambda x: x[1].get("name", "")):
            logo_fn = t.get("logo_filename")
            logo_url = f"/assets/logos/{logo_fn}" if logo_fn else None
            teams.append({
                "key": key,
                "name": t.get("name", key.title()),
                "short_name": t.get("short_name", key[:3].upper()),
                "primary_color": t.get("primary_color", [16, 185, 129]),
                "extended_name": t.get("extended_name", ""),
                "secondary_color": t.get("secondary_color", [239, 68, 68]),
                "tertiary_color": t.get("tertiary_color", [59, 130, 246]),
                "rivalry": t.get("rivalry", 3),
                "notes": t.get("notes", ""),
                "logo_filename": logo_fn,
                "logo_url": logo_url
            })
        
        # 2. Players
        roster = load_roster()
        players_list = []
        cutouts = set()
        if PLAYERS_TRANSPARENT_DIR.exists():
            for p in sorted(PLAYERS_TRANSPARENT_DIR.iterdir()):
                if p.is_file() and p.suffix.lower() in [".png", ".webp"]:
                    cutouts.add(p.stem.lower())
                    
        for key, pinfo in roster.items():
            has_cutout = key.lower() in cutouts
            players_list.append({
                "key": key,
                "name": pinfo.get("name", key.title()),
                "role": pinfo.get("role", "Giocatore"),
                "number": pinfo.get("number", ""),
                "team": pinfo.get("team", "A.C.C.V."),
                "has_photo": has_cutout,
                "photo_url": f"/assets/players/headshot/transparent/{key}.png" if has_cutout else None
            })

        # 3. Emotions
        emotions = []
        if BACKGROUNDS_DIR.exists():
            for p in sorted(BACKGROUNDS_DIR.iterdir()):
                if p.is_dir() and p.name.startswith("tema "):
                    emotions.append(p.name.replace("tema ", ""))
                elif p.is_dir() and p.name == "foto squadra":
                    emotions.append("foto squadra")
                    
        # 4. Background images
        backgrounds = []
        if BACKGROUNDS_DIR.exists():
            for p in sorted(BACKGROUNDS_DIR.glob("**/*")):
                if p.is_file() and p.suffix.lower() in [".jpg", ".jpeg", ".png", ".webp"]:
                    category = "Altro"
                    if "tema " in p.parent.name:
                        category = p.parent.name.replace("tema ", "").capitalize()
                    elif p.parent.name == "foto squadra":
                        category = "Foto Squadra"
                    elif p.parent == BACKGROUNDS_DIR:
                        category = "Predefinito"
                    backgrounds.append({
                        "filename": p.name,
                        "relative_path": str(p.relative_to(BACKGROUNDS_DIR)),
                        "full_path": str(p),
                        "url": f"/assets/backgrounds/{p.relative_to(BACKGROUNDS_DIR)}",
                        "category": category
                    })

        # 5. Example Match Data
        example_file = DATA_DIR / "example_match.json"
        example_data = {}
        if example_file.exists():
            try:
                with open(example_file, "r", encoding="utf-8") as f:
                    example_data = json.load(f)
            except Exception:
                pass

        data = {
            "teams": teams,
            "players": players_list,
            "emotions": emotions,
            "backgrounds": backgrounds,
            "formats": [
                {"id": "9:16", "name": "Story / Reels (9:16)", "dims": "1080x1920", "recommended": "Instagram Story, Reels, TikTok"},
                {"id": "4:3", "name": "Post Orizzontale (4:3)", "dims": "1440x1080", "recommended": "Facebook, Feed Orizzontale"},
                {"id": "1:1", "name": "Post Quadrato (1:1)", "dims": "1080x1080", "recommended": "Instagram Feed Quadrato"},
                {"id": "4:5", "name": "Post Verticale (4:5)", "dims": "1080x1350", "recommended": "Instagram Portrait"},
                {"id": "16:9", "name": "Widescreen (16:9)", "dims": "1920x1080", "recommended": "Banner, X / Twitter"}
            ],
            "figurina_formats": [
                {"id": "story", "name": "Storia Instagram (9:16)", "desc": "Card 3D fluttuante con sfondo e faretto neon"},
                {"id": "card", "name": "Figurina Pura (Card Panini)", "desc": "Formato stampa / card classica 1024x1536"},
                {"id": "post", "name": "Post Portrait (4:5)", "desc": "Card per feed Instagram verticale 1080x1350"}
            ],
            "example_match": example_data
        }
        self.send_json(data)

    def handle_get_players(self):
        roster = load_roster()
        self.send_json(roster)

    def handle_get_teams(self):
        teams = load_teams()
        self.send_json(teams)

    def serve_file(self, path: Path, content_type: Optional[str] = None):
        """Serve a static file from disk."""
        if not path.exists() or not path.is_file():
            self.send_error(404, f"File not found: {path.name}")
            return
            
        if not content_type:
            content_type, _ = mimetypes.guess_type(str(path))
            if not content_type:
                content_type = "application/octet-stream"

        try:
            with open(path, "rb") as f:
                content = f.read()
            self.send_response(200)
            self.send_header("Content-Type", content_type)
            self.send_header("Content-Length", str(len(content)))
            self.end_headers()
            self.wfile.write(content)
        except Exception as e:
            self.send_error(500, f"Error reading file: {e}")

    def send_json(self, data: Any, status: int = 200):
        body = json.dumps(data, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def send_binary(self, data: bytes, content_type: str = "image/png", filename: Optional[str] = None, status: int = 200):
        self.send_response(status)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(data)))
        if filename:
            self.send_header("Content-Disposition", f'attachment; filename="{filename}"')
        self.end_headers()
        self.wfile.write(data)


def run_server(host: str = "0.0.0.0", port: int = 8000, open_browser: bool = False, public: bool = False):
    """Start local/cloud web server with ThreadingHTTPServer."""
    if "PORT" in os.environ:
        try:
            port = int(os.environ["PORT"])
        except ValueError:
            pass

    server_address = (host, port)
    httpd = ThreadingHTTPServer(server_address, ACCVRequestHandler)
    
    url = f"http://{'localhost' if host == '0.0.0.0' else host}:{port}"
    print("\n" + "=" * 66)
    print("  ⚽ ACCV SOCIAL GRAPHICS WEB STUDIO ⚽")
    print("=" * 66)
    print(f"  🚀 Web App attiva su:       {url}")
    print(f"  📱 Accessibile da mobile:   http://<tuo-ip-locale>:{port}")
    if public:
        print("  🌍 Per condividere pubblicamente, usa Cloudflare Tunnel o Localtunnel:")
        print(f"     npx localtunnel --port {port}   oppure   cloudflared tunnel --url http://localhost:{port}")
    print("  💡 Premi Ctrl+C per arrestare il server")
    print("=" * 66 + "\n")
    
    if open_browser:
        try:
            webbrowser.open(url)
        except Exception:
            pass

    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\n\nArresto del server ACCV in corso...")
        httpd.server_close()
        print("Server arrestato.")
