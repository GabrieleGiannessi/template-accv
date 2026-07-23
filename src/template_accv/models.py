"""
Data models for match graphics.
"""

from dataclasses import dataclass, field
from typing import List, Optional


@dataclass
class Team:
    name: str
    short_name: str
    logo_path: Optional[str] = None
    primary_color: Optional[tuple] = None


@dataclass
class Scorer:
    name: str
    goals: int = 1
    minutes: Optional[List[int]] = field(default_factory=list)

    def to_summary(self) -> str:
        """Returns string like 'Rossi (2)' or 'Bianchi'."""
        if self.goals > 1:
            return f"{self.name} ({self.goals})"
        return self.name


@dataclass
class MatchResult:
    home_team: Team
    away_team: Team
    home_score: int
    away_score: int
    home_scorers: List[Scorer] = field(default_factory=list)
    away_scorers: List[Scorer] = field(default_factory=list)
    tournament: str = "Campionato Calcetto A 7"
    matchday: str = "Giornata 1"
    date: str = ""
    time: str = ""
    location: str = ""
    mvp_name: Optional[str] = None


@dataclass
class NextMatch:
    home_team: Team
    away_team: Team
    tournament: str = "Campionato Calcetto A "
    matchday: str = "Giornata Prossima"
    date: str = ""
    time: str = ""
    location: str = ""
    field_name: str = ""


@dataclass
class MVP:
    player_name: str
    jersey_number: str = "10"
    position: str = "Attaccante"
    goals: int = 0
    assists: int = 0
    saves: int = 0
    rating: str = "9.5"
    photo_path: Optional[str] = None
    match_opponent: str = ""
    match_date: str = ""
