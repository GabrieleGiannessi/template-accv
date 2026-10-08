const { useState, useEffect, useRef, useMemo, useCallback } = React;

// --- Clean SVG Icons ---
function Icon({ name, className = "w-5 h-5", ...props }) {
  const icons = {
    trophy: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" />
      </svg>
    ),
    calendar: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
    ),
    star: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
      </svg>
    ),
    badge: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
    ),
    users: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
      </svg>
    ),
    download: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
      </svg>
    ),
    zip: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" />
      </svg>
    ),
    copy: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
      </svg>
    ),
    share: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z" />
      </svg>
    ),
    swap: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
      </svg>
    ),
    plus: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
      </svg>
    ),
    minus: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M20 12H4" />
      </svg>
    ),
    check: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
      </svg>
    ),
    trash: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
      </svg>
    ),
    edit: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
      </svg>
    ),
    upload: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
      </svg>
    ),
    image: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
    ),
    shield: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
      </svg>
    ),
    settings: (
      <svg className={className} fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2} {...props}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M10.325 4.317a1.724 1.724 0 013.35 0 1.724 1.724 0 002.573 1.066 1.724 1.724 0 012.37 2.37 1.724 1.724 0 001.066 2.573 1.724 1.724 0 010 3.35 1.724 1.724 0 00-1.066 2.573 1.724 1.724 0 01-2.37 2.37 1.724 1.724 0 00-2.573 1.066 1.724 1.724 0 01-3.35 0 1.724 1.724 0 00-2.573-1.066 1.724 1.724 0 01-2.37-2.37 1.724 1.724 0 00-1.066-2.573 1.724 1.724 0 010-3.35 1.724 1.724 0 001.066-2.573 1.724 1.724 0 012.37-2.37 1.724 1.724 0 002.573-1.066z" /><path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
      </svg>
    )
  };
  return icons[name] || null;
}

const PlayerRosterRow = React.memo(function PlayerRosterRow({ player, onEdit, onDelete, onPhotoUpload }) {
  return (
    <tr className="hover:bg-slate-50 transition-colors">
      <td className="py-2 px-4">
        <label className="relative cursor-pointer group flex items-center">
          <input type="file" accept="image/png,image/webp" className="hidden" onChange={(e) => onPhotoUpload(e, player.key)} />
          <div className="w-10 h-10 rounded-full bg-slate-100 overflow-hidden border border-slate-200 flex items-center justify-center group-hover:border-accvGreen">
            {player.photo_url ? <img loading="lazy" decoding="async" src={player.photo_url} alt={player.name} className="w-full h-full object-cover object-top" /> : <span className="text-[10px] text-slate-400">No foto</span>}
          </div>
          <span className="ml-2 text-[10px] text-accvGreen group-hover:underline font-semibold">Cambia</span>
        </label>
      </td>
      <td className="py-2 px-4 font-bold text-slate-900">{player.name}</td>
      <td className="py-2 px-4 text-slate-600">{player.display_name || "—"}</td>
      <td className="py-2 px-4 text-slate-600">{player.role}</td>
      <td className="py-2 px-4 text-center font-bold text-accvGreenDark">#{player.number}</td>
      <td className="py-2 px-4 text-right space-x-1">
        <button onClick={() => onEdit(player)} className="p-1.5 rounded-lg text-slate-500 hover:text-accvGreen hover:bg-slate-100" title="Modifica Calciatore"><Icon name="edit" className="w-4 h-4" /></button>
        <button onClick={() => onDelete(player.key, player.name)} className="p-1.5 rounded-lg text-slate-400 hover:text-accvRed hover:bg-red-50" title="Elimina Calciatore"><Icon name="trash" className="w-4 h-4" /></button>
      </td>
    </tr>
  );
});

// --- Reusable Component: Background Selector (Emotions, Gallery, File Upload) ---
function BackgroundSelector({
  emotion,
  bgPath,
  onSelectEmotion,
  onSelectBgPath,
  onClearCustomBg,
  config,
  showToast
}) {
  const [mode, setMode] = useState("emotion"); // emotion, gallery, upload
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef(null);

  const handleFileUpload = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploading(true);
    const reader = new FileReader();
    reader.onload = () => {
      const b64Data = reader.result;
      const cleanFilename = `custom_${Date.now()}_${file.name.replace(/[^a-zA-Z0-9._-]/g, "_")}`;

      fetch("/api/upload", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          upload_type: "background",
          filename: cleanFilename,
          data: b64Data
        })
      })
        .then((r) => r.json())
        .then((res) => {
          setUploading(false);
          if (res.status === "ok") {
            onSelectBgPath(res.path);
            showToast("✓ Foto di sfondo caricata e applicata!");
          } else {
            alert("Errore upload sfondo: " + res.error);
          }
        })
        .catch((err) => {
          setUploading(false);
          alert("Errore upload sfondo: " + err);
        });
    };
    reader.readAsDataURL(file);
  };

  return (
    <div className="space-y-3 pt-3 border-t border-slate-200">
      <div className="flex items-center justify-between">
        <label className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center space-x-1.5">
          <Icon name="image" className="w-4 h-4 text-accvGreen" />
          <span>Sfondo della Locandina</span>
        </label>
        
        {/* Active bg status & reset */}
        {bgPath && (
          <button
            type="button"
            onClick={onClearCustomBg}
            className="text-[11px] font-bold text-accvRed hover:underline flex items-center space-x-1"
          >
            <span>Rimuovi foto personalizzata</span>
          </button>
        )}
      </div>

      {/* Mode Switcher Tabs */}
      <div className="flex bg-slate-100 p-1 rounded-xl border border-slate-200 text-xs font-bold">
        <button
          type="button"
          onClick={() => {
            setMode("emotion");
            onClearCustomBg();
          }}
          className={`flex-1 py-1.5 rounded-lg transition-all ${
            mode === "emotion"
              ? "bg-white text-accvGreenDark shadow-sm font-extrabold"
              : "text-slate-600 hover:text-slate-900"
          }`}
        >
          Per Emozione
        </button>
        <button
          type="button"
          onClick={() => setMode("gallery")}
          className={`flex-1 py-1.5 rounded-lg transition-all ${
            mode === "gallery"
              ? "bg-white text-accvGreenDark shadow-sm font-extrabold"
              : "text-slate-600 hover:text-slate-900"
          }`}
        >
          Galleria Foto ({config.backgrounds?.length || 0})
        </button>
        <button
          type="button"
          onClick={() => setMode("upload")}
          className={`flex-1 py-1.5 rounded-lg transition-all ${
            mode === "upload"
              ? "bg-white text-accvGreenDark shadow-sm font-extrabold"
              : "text-slate-600 hover:text-slate-900"
          }`}
        >
          Importa da File
        </button>
      </div>

      {/* Mode 1: Emotion Categories */}
      {mode === "emotion" && (
        <div className="space-y-2">
          <div className="grid grid-cols-3 sm:grid-cols-5 gap-2">
            {config.emotions.map((em) => (
              <button
                key={em}
                type="button"
                onClick={() => onSelectEmotion(em)}
                className={`py-2 px-2 rounded-xl text-xs font-bold capitalize transition-all border ${
                  emotion === em
                    ? "bg-accvGreen text-white border-accvGreen shadow-green-glow"
                    : "bg-white text-slate-700 border-slate-200 hover:border-accvGreen hover:bg-slate-50"
                }`}
              >
                {em}
              </button>
            ))}
          </div>
          <p className="text-[11px] text-slate-500 italic">
            Lo script selezionerà automaticamente una foto casuale appartenente a questa categoria.
          </p>
        </div>
      )}

      {/* Mode 2: Visual Photo Gallery */}
      {mode === "gallery" && (
        <div className="space-y-2">
          <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 gap-2.5 max-h-56 overflow-y-auto p-2 bg-slate-50 rounded-xl border border-slate-200">
            {config.backgrounds.map((bg) => {
              const isSelected = bgPath === bg.full_path || bgPath === bg.relative_path;
              return (
                <button
                  key={bg.relative_path}
                  type="button"
                  onClick={() => onSelectBgPath(bg.full_path)}
                  className={`relative rounded-xl overflow-hidden border-2 transition-all aspect-video group ${
                    isSelected
                      ? "border-accvGreen ring-2 ring-accvGreen/30 shadow-md scale-95"
                      : "border-slate-200 hover:border-accvGreen"
                  }`}
                >
                  <img src={bg.url} alt={bg.filename} className="w-full h-full object-cover" />
                  <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                    <span className="text-[10px] text-white font-bold bg-black/60 px-1.5 py-0.5 rounded">Scegli</span>
                  </div>
                  <div className="absolute bottom-0 inset-x-0 bg-slate-900/80 px-1 py-0.5 text-[9px] text-white truncate text-center">
                    {bg.category}
                  </div>
                  {isSelected && (
                    <div className="absolute top-1 right-1 bg-accvGreen text-white rounded-full p-0.5">
                      <Icon name="check" className="w-3 h-3" />
                    </div>
                  )}
                </button>
              );
            })}
          </div>
        </div>
      )}

      {/* Mode 3: File System Upload */}
      {mode === "upload" && (
        <div className="space-y-2">
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileUpload}
            accept="image/*"
            className="hidden"
          />
          <div
            onClick={() => fileInputRef.current?.click()}
            className="border-2 border-dashed border-slate-300 hover:border-accvGreen bg-slate-50 hover:bg-accvGreenLight/40 rounded-xl p-5 text-center cursor-pointer transition-all space-y-2"
          >
            <div className="w-10 h-10 rounded-full bg-white text-accvGreen mx-auto flex items-center justify-center border border-slate-200 shadow-sm">
              <Icon name="upload" className="w-5 h-5 text-accvGreen" />
            </div>
            <div>
              <div className="text-xs font-bold text-slate-800">
                {uploading ? "Caricamento foto in corso..." : "Clicca o trascina qui una foto dal tuo computer"}
              </div>
              <div className="text-[11px] text-slate-500">Formati supportati: JPG, PNG, WEBP (foto scattata in partita, ecc.)</div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// --- Main Application Component ---
function App() {
  const [config, setConfig] = useState(null);
  const [loadingConfig, setLoadingConfig] = useState(true);
  const [activeTab, setActiveTab] = useState("result"); // result, next, mvp, figurina, roster
  
  // Preview State
  const [previewUri, setPreviewUri] = useState(null);
  const [rendering, setRendering] = useState(false);
  const previewRequestId = useRef(0);
  const [selectedFormat, setSelectedFormat] = useState("9:16");
  const [showSafeZone, setShowSafeZone] = useState(false);
  const [toastMessage, setToastMessage] = useState(null);

  // Form States: Match Result
  const [resultData, setResultData] = useState({
    tournament: "CAMPIONATO CALCETTO A5",
    matchday: "GIORNATA 5",
    date: "Giovedì 23 Luglio 2026",
    time: "21:30",
    location: "Centro Sportivo ACCV - Campo A",
    home_team: { name: "A.C.C.V.", short_name: "ACCV", primary_color: [16, 185, 129] },
    away_team: { name: "REAL MATRID", short_name: "MAT", primary_color: [239, 68, 68] },
    home_score: 0,
    away_score: 0,
    home_scorers: [],
    away_scorers: [],
    home_yellow_cards: [],
    away_yellow_cards: [],
    home_red_cards: [],
    away_red_cards: [],
    mvp_name: "Mario Rossi",
    emotion: "felicita",
    bg_path: null,
    contrast_factor: 1.0,
    remove_contrast: false
  });

  // Form States: Next Match
  const [nextData, setNextData] = useState({
    tournament: "CAMPIONATO CALCETTO A5",
    matchday: "PROSSIMA PARTITA",
    date: "Giovedì 30 Luglio 2026",
    time: "21:30",
    location: "Centro Sportivo ACCV - Campo A",
    field_name: "Campo A",
    home_team: { name: "A.C.C.V.", short_name: "ACCV" },
    away_team: { name: "INTER CALCETTO", short_name: "INT" },
    emotion: "normale",
    bg_path: null,
    contrast_factor: 1.0,
    remove_contrast: false
  });

  // Form States: MVP
  const [mvpData, setMvpData] = useState({
    player_name: "Mario Rossi",
    jersey_number: "10",
    position: "Pivot / Attaccante",
    goals: 3,
    assists: 2,
    saves: 0,
    rating: "9.5",
    match_opponent: "Real Matrid",
    match_date: "23 Luglio 2026",
    emotion: "felicita",
    bg_path: null,
    contrast_factor: 1.0,
    remove_contrast: false
  });

  // Form States: Figurina
  const [figurinaData, setFigurinaData] = useState({
    player_key: "bouba",
    player_name: "Boubacar Gadio",
    role: "Centrocampista",
    jersey_number: "8",
    with_banner: true,
    format: "story"
  });

  const showToast = (msg) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3500);
  };

  const reloadConfig = () => {
    fetch("/api/config")
      .then((res) => res.json())
      .then((data) => {
        setConfig(data);
      });
  };

  // Load initial config from backend
  useEffect(() => {
    fetch("/api/config")
      .then((res) => res.json())
      .then((data) => {
        setConfig(data);
        const accvTeam = data.teams?.find((team) => team.key === "accv" || team.name.toUpperCase().includes("ACCV"));
        if (accvTeam) {
          setResultData((prev) => ({ ...prev, home_team: { ...prev.home_team, ...accvTeam } }));
        }
        if (data.example_match && data.example_match.home_team) {
          const em = data.example_match;
          setResultData((prev) => ({
            ...prev,
            tournament: em.tournament || prev.tournament,
            matchday: em.matchday || prev.matchday,
            date: em.date || prev.date,
            time: em.time || prev.time,
            location: em.location || prev.location,
            home_team: accvTeam ? { ...prev.home_team, ...accvTeam } : prev.home_team,
            away_team: em.away_team || prev.away_team,
            home_score: 0,
            away_score: 0,
            home_scorers: [],
            away_scorers: [],
            mvp_name: em.mvp?.player_name || prev.mvp_name
          }));
        }
        setLoadingConfig(false);
      })
      .catch((err) => {
        console.error("Config fetch error:", err);
        setLoadingConfig(false);
      });
  }, []);

  // Update Figurina Form when player selection changes
  const handleSelectFigurinaPlayer = (player) => {
    setFigurinaData((prev) => ({
      ...prev,
      player_key: player.key,
      player_name: player.name,
      role: player.role,
      jersey_number: player.number || ""
    }));
  };

  // Auto-suggest emotion on score change while keeping the currently selected background.
  useEffect(() => {
    if (activeTab !== "result") return;
    const isAccvHome = resultData.home_team.name.toUpperCase().includes("ACCV");
    const isAccvAway = resultData.away_team.name.toUpperCase().includes("ACCV");
    let emotion = null;
    if (isAccvHome) {
      emotion = resultData.home_score > resultData.away_score ? "felicita" : resultData.home_score < resultData.away_score ? "tristezza" : "polemica";
    } else if (isAccvAway) {
      emotion = resultData.away_score > resultData.home_score ? "felicita" : resultData.away_score < resultData.home_score ? "tristezza" : "polemica";
    }
    if (emotion && emotion !== resultData.emotion) {
      setResultData((prev) => ({ ...prev, emotion }));
    }
  }, [resultData.home_score, resultData.away_score, activeTab]);

  // Request Render Preview (debounced in memory)
  useEffect(() => {
    const requestId = ++previewRequestId.current;
    if (loadingConfig || activeTab === "roster") return;

    const timer = setTimeout(() => {
      setRendering(true);

      let endpoint = "/api/preview/result";
      let payload = { ...resultData, format: selectedFormat };

      if (activeTab === "next") {
        endpoint = "/api/preview/next";
        payload = { ...nextData, format: selectedFormat };
      } else if (activeTab === "mvp") {
        endpoint = "/api/preview/mvp";
        payload = { ...mvpData, format: selectedFormat };
      } else if (activeTab === "figurina") {
        endpoint = "/api/preview/figurina";
        payload = { ...figurinaData, format: selectedFormat === "9:16" ? "story" : selectedFormat === "4:5" ? "post" : "card" };
      }

      fetch(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      })
        .then((res) => res.json())
        .then((res) => {
          if (requestId !== previewRequestId.current) return;
          if (res.status === "ok" && res.preview) {
            setPreviewUri(res.preview);
            if (activeTab === "result" && res.bg_path) {
              setResultData((prev) => prev.bg_path ? prev : { ...prev, bg_path: res.bg_path });
            }
          }
          setRendering(false);
        })
        .catch((err) => {
          if (requestId !== previewRequestId.current) return;
          console.error("Preview render failed:", err);
          setRendering(false);
        });
    }, 300);

    return () => clearTimeout(timer);
  }, [activeTab, selectedFormat, resultData, nextData, mvpData, figurinaData, loadingConfig]);

  // Action: Download Single PNG
  const handleDownload = () => {
    if (!previewUri) return;
    fetch(previewUri)
      .then((res) => res.blob())
      .then((blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `accv_${activeTab}_${selectedFormat.replace(":", "_")}.png`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
        showToast("✓ Grafica scaricata in PNG!");
      })
      .catch((err) => alert("Errore download: " + err));
  };

  // Action: Download All Formats (ZIP)
  const handleDownloadAll = () => {
    const payload = {
      type: activeTab,
      ...(activeTab === "result" ? resultData : {}),
      ...(activeTab === "next" ? nextData : {}),
      ...(activeTab === "figurina" ? figurinaData : {})
    };

    fetch("/api/download-all", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    })
      .then((res) => res.blob())
      .then((blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `accv_${activeTab}_pack.zip`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        showToast("✓ Archivio ZIP scaricato!");
      })
      .catch((err) => alert("Errore download archivio: " + err));
  };

  // Action: Copy to Clipboard
  const handleCopyToClipboard = async () => {
    if (!previewUri) return;
    try {
      const res = await fetch(previewUri);
      const blob = await res.blob();
      await navigator.clipboard.write([
        new ClipboardItem({ "image/png": blob })
      ]);
      showToast("✓ Copiato negli appunti!");
    } catch (err) {
      showToast("⚠️ Il tuo browser non supporta la copia diretta di immagini.");
    }
  };

  // Action: Web Share API (Mobile Instagram / WhatsApp)
  const handleShare = async () => {
    if (!previewUri || !navigator.share) {
      showToast("Condivisione non supportata. Usa il tasto Scarica.");
      return;
    }
    try {
      const res = await fetch(previewUri);
      const blob = await res.blob();
      const file = new File([blob], `accv_${activeTab}.png`, { type: "image/png" });
      await navigator.share({
        files: [file],
        title: "ACCV Locandina Social",
        text: "Grafica ufficiale calcetto A.C.C.V."
      });
      showToast("✓ Aperta schermata di condivisione!");
    } catch (err) {}
  };

  // Swap Home and Away teams
  const handleSwapTeams = () => {
    if (activeTab === "result") {
      setResultData((prev) => ({
        ...prev,
        home_team: prev.away_team,
        away_team: prev.home_team,
        home_score: prev.away_score,
        away_score: prev.home_score,
        home_scorers: prev.away_scorers,
        away_scorers: prev.home_scorers,
        home_yellow_cards: prev.away_yellow_cards || [],
        away_yellow_cards: prev.home_yellow_cards || [],
        home_red_cards: prev.away_red_cards || [],
        away_red_cards: prev.home_red_cards || []
      }));
    } else if (activeTab === "next") {
      setNextData((prev) => ({
        ...prev,
        home_team: prev.away_team,
        away_team: prev.home_team
      }));
    }
  };

  if (loadingConfig) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center space-y-4 py-24">
        <div className="w-12 h-12 border-4 border-accvGreen border-t-transparent rounded-full animate-spin"></div>
        <div className="text-xl font-header tracking-wider text-accvGreenDark">Caricamento Studio ACCV...</div>
      </div>
    );
  }

  return (
    <div className="flex-1 flex flex-col bg-accvBg min-h-screen">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="fixed top-5 right-5 z-50 bg-white border border-accvGreen text-slate-800 px-5 py-3 rounded-xl shadow-lg flex items-center space-x-3 transition-all animate-bounce">
          <div className="w-6 h-6 rounded-full bg-accvGreen text-white flex items-center justify-center">
            <Icon name="check" className="w-4 h-4" />
          </div>
          <span className="font-bold text-sm">{toastMessage}</span>
        </div>
      )}

      {/* Top Navigation Bar: Clean White with Green & Gold Accents */}
      <header className="bg-white border-b border-slate-200 sticky top-0 z-40 px-4 lg:px-8 py-3.5 flex flex-wrap items-center justify-between gap-4 shadow-sm">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-xl bg-accvGreen text-white flex items-center justify-center font-header text-2xl font-black shadow-green-glow">
            ⚽
          </div>
          <div>
            <h1 className="font-header text-2xl lg:text-3xl tracking-widest text-slate-900 leading-none">
              Designer Lab
            </h1>
            <p className="text-[11px] text-accvGoldDark uppercase tracking-wider font-bold">
              <strong>ACCV</strong>: <strong>Automazione Grafiche & Rose Campionato</strong>
            </p>
          </div>
        </div>

        {/* Navigation Tabs */}
        <nav className="flex items-center bg-slate-100 p-1 rounded-xl border border-slate-200 overflow-x-auto max-w-full">
          {[
            { id: "result", label: "Risultato", icon: "trophy" },
            { id: "next", label: "Prossima Partita", icon: "calendar" },
            { id: "mvp", label: "Migliore in Campo", icon: "star" },
            { id: "figurina", label: "Figurine Panini", icon: "badge" },
            { id: "roster", label: "Gestione", icon: "settings" }
          ].map((tab) => {
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => {
                  setActiveTab(tab.id);
                  if (tab.id === "result") {
                    const accvTeam = config.teams?.find((team) => team.key === "accv" || team.name.toUpperCase().includes("ACCV"));
                    setResultData((prev) => ({ ...prev, home_team: accvTeam || prev.home_team, away_team: prev.away_team.name.toUpperCase().includes("ACCV") ? (config.teams.find((team) => !team.name.toUpperCase().includes("ACCV")) || prev.away_team) : prev.away_team, home_score: 0, away_score: 0, home_scorers: [], away_scorers: [], home_yellow_cards: [], away_yellow_cards: [], home_red_cards: [], away_red_cards: [] }));
                  }
                  if (tab.id === "figurina" && selectedFormat !== "9:16") {
                    setSelectedFormat("9:16");
                  }
                }}
                className={`flex items-center space-x-2 px-3.5 py-2 rounded-lg text-xs font-bold transition-all whitespace-nowrap ${
                  isActive
                    ? "bg-accvGreen text-white shadow-sm font-extrabold"
                    : "text-slate-600 hover:text-slate-900 hover:bg-white/60"
                }`}
              >
                <Icon name={tab.icon} className={`w-4 h-4 ${isActive ? "text-white" : "text-accvGreen"}`} />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </nav>
      </header>

      {/* Main Studio Body: 2-Column Split Screen */}
      <main className={`flex-1 w-full mx-auto p-4 lg:p-6 ${activeTab === "roster" ? "max-w-none" : "max-w-7xl grid grid-cols-1 lg:grid-cols-12 gap-6"}`}>
        
        {/* LEFT COLUMN: Controls & Forms (lg:col-span-7) */}
        <div className={activeTab === "roster" ? "w-full" : "lg:col-span-7 space-y-6"}>
          {activeTab === "result" && (
            <MatchResultForm
              data={resultData}
              onChange={setResultData}
              config={config}
              onSwap={handleSwapTeams}
              showToast={showToast}
            />
          )}

          {activeTab === "next" && (
            <NextMatchForm
              data={nextData}
              onChange={setNextData}
              config={config}
              onSwap={handleSwapTeams}
              showToast={showToast}
            />
          )}

          {activeTab === "mvp" && (
            <MVPForm
              data={mvpData}
              onChange={setMvpData}
              config={config}
              showToast={showToast}
            />
          )}

          {activeTab === "figurina" && (
            <FigurinaForm
              data={figurinaData}
              onChange={setFigurinaData}
              config={config}
              onSelectPlayer={handleSelectFigurinaPlayer}
            />
          )}

          {activeTab === "roster" && (
            <RosterAndTeamsManager
              config={config}
              onConfigReload={reloadConfig}
              showToast={showToast}
            />
          )}
        </div>

        {/* RIGHT COLUMN: Live Preview & Output Studio (lg:col-span-5) */}
        {activeTab !== "roster" && (
          <div className="lg:col-span-5">
            <div className="sticky top-20 space-y-4">
              <PreviewPanel
                previewUri={previewUri}
                rendering={rendering}
                selectedFormat={selectedFormat}
                onSelectFormat={setSelectedFormat}
                activeTab={activeTab}
                showSafeZone={showSafeZone}
                onToggleSafeZone={() => setShowSafeZone(!showSafeZone)}
                onDownload={handleDownload}
                onDownloadAll={handleDownloadAll}
                onCopy={handleCopyToClipboard}
                onShare={handleShare}
              />
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

// --- TAB 1: Match Result Form ---
function CollapsibleSection({ title, children, className = "" }) {
  const [expanded, setExpanded] = useState(true);
  return (
    <section className="clean-card p-5">
      <button
        type="button"
        aria-expanded={expanded}
        onClick={() => setExpanded((current) => !current)}
        className="w-full flex items-center justify-between text-left text-xs uppercase tracking-wider font-bold text-accvGreen"
      >
        <span>{title}</span>
        <span className="text-slate-400 text-base" aria-hidden="true">{expanded ? "−" : "+"}</span>
      </button>
      {expanded && <div className={`mt-4 ${className}`}>{children}</div>}
    </section>
  );
}

function toDateFieldValue(value) {
  const text = String(value || "").trim();
  const iso = text.match(/^(\d{4}-\d{2}-\d{2})/);
  if (iso) return iso[1];
  const match = text.match(/(?:[A-Za-zÀ-ÿ]+\s+)?(\d{1,2})\s+([A-Za-zÀ-ÿ]+)\s+(\d{4})/i);
  if (!match) return "";
  const months = { gennaio: "01", febbraio: "02", marzo: "03", aprile: "04", maggio: "05", giugno: "06", luglio: "07", agosto: "08", settembre: "09", ottobre: "10", novembre: "11", dicembre: "12" };
  const month = months[match[2].toLowerCase()];
  return month ? `${match[3]}-${month}-${match[1].padStart(2, "0")}` : "";
}

function CompetitionSeasonSelector({ data, onChange, config, showMatchDetails = false, collapsible = false }) {
  const competitions = config.competitions || [];
  const competition = competitions.find(item => item.key === data.competition_key) || competitions[0];
  const seasonLinks = competition?.seasons || [];
  const seasons = seasonLinks.map(link => config.seasons?.find(item => item.key === link.season_key)).filter(Boolean);
  const season = seasons.find(item => item.key === data.season_key) || seasons[0];
  const association = seasonLinks.find(link => link.season_key === season?.key);
  const teams = (association?.team_keys || []).map(key => config.teams.find(team => team.key === key)).filter(Boolean);

  const applySelection = (nextCompetition, nextSeason) => {
    const link = nextCompetition?.seasons?.find(item => item.season_key === nextSeason?.key);
    const eligibleTeams = (link?.team_keys || []).map(key => config.teams.find(team => team.key === key)).filter(Boolean);
    const pickTeam = (current, excluded) => eligibleTeams.find(team => team.name === current && team.name !== excluded) || eligibleTeams.find(team => team.name !== excluded);
    const home = pickTeam(data.home_team?.name, null);
    const away = pickTeam(data.away_team?.name, home?.name);
    onChange({
      ...data,
      competition_key: nextCompetition?.key || "",
      season_key: nextSeason?.key || "",
      tournament: nextCompetition?.description || "",
      home_team: home ? { ...data.home_team, name: home.name, short_name: home.short_name, logo_path: `assets/logos/${home.filename || home.logo_filename}` } : data.home_team,
      away_team: away ? { ...data.away_team, name: away.name, short_name: away.short_name, logo_path: `assets/logos/${away.filename || away.logo_filename}` } : data.away_team
    });
  };

  useEffect(() => {
    if (competition && (data.competition_key !== competition.key || data.season_key !== season?.key)) {
      applySelection(competition, season);
    }
  }, [config.competitions, config.seasons]);

  const fields = <div className="space-y-4">
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div><label className="text-[11px] font-bold text-slate-600 uppercase">Competizione</label><select value={competition?.key || ""} onChange={e => { const next = competitions.find(item => item.key === e.target.value); const nextSeason = next?.seasons?.map(link => config.seasons?.find(item => item.key === link.season_key)).find(Boolean); applySelection(next, nextSeason); }} disabled={!competitions.length} className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-bold"><option value="">{competitions.length ? "Seleziona competizione" : "Nessuna competizione configurata"}</option>{competitions.map(item => <option key={item.key} value={item.key}>{item.description}</option>)}</select></div>
      <div><label className="text-[11px] font-bold text-slate-600 uppercase">Stagione</label><select value={season?.key || ""} onChange={e => applySelection(competition, seasons.find(item => item.key === e.target.value))} disabled={!seasons.length} className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-bold"><option value="">{seasons.length ? "Seleziona stagione" : "Nessuna stagione associata"}</option>{seasons.map(item => <option key={item.key} value={item.key}>{item.description}</option>)}</select></div>
      {!teams.length && <p className="md:col-span-2 text-xs text-amber-700">La stagione selezionata non ha squadre associate a questa competizione.</p>}
    </div>
    {showMatchDetails && <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-4 border-t border-slate-100">
      <div><label className="text-[11px] font-bold text-slate-600 uppercase">Giornata</label><input type="text" value={data.matchday || ""} onChange={e => onChange({ ...data, matchday: e.target.value })} className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-medium" /></div>
      <div><label className="text-[11px] font-bold text-slate-600 uppercase">Data</label><input type="date" value={toDateFieldValue(data.date)} onChange={e => onChange({ ...data, date: e.target.value })} className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-medium" /></div>
      <div><label className="text-[11px] font-bold text-slate-600 uppercase">Ora</label><input type="time" value={data.time || ""} onChange={e => onChange({ ...data, time: e.target.value })} className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-medium" /></div>
    </div>}
  </div>;
  return collapsible ? <CollapsibleSection title="Competizione attuale">{fields}</CollapsibleSection> : <div className="clean-card p-5">{fields}</div>;
}

function MatchResultForm({ data, onChange, config, onSwap, showToast }) {
  const updateField = (field, val) => onChange({ ...data, [field]: val });
  const homeIsAccv = data.home_team.name.toUpperCase().includes("ACCV");
  const awayIsAccv = data.away_team.name.toUpperCase().includes("ACCV");
  const accvIsHome = homeIsAccv || !awayIsAccv;
  const accvScorerType = accvIsHome ? "home" : "away";
  const opponentScorerType = accvIsHome ? "away" : "home";
  const accvScorers = data[accvIsHome ? "home_scorers" : "away_scorers"] || [];
  const opponentScorers = data[accvIsHome ? "away_scorers" : "home_scorers"] || [];
  const opponentTeam = accvIsHome ? data.away_team : data.home_team;

  const updateScorers = (listKey, nextList, scoreKey, scoreDelta) => {
    onChange({ ...data, [listKey]: nextList, [scoreKey]: Math.max(0, (Number(data[scoreKey]) || 0) + scoreDelta) });
  };

  const addScorer = (teamType, playerName = "Giocatore", goals = 1) => {
    const listKey = teamType === "home" ? "home_scorers" : "away_scorers";
    const current = data[listKey] || [];
    const existing = current.find((s) => s.name === playerName);
    if (existing) {
      const updated = current.map((s) => s.name === playerName ? { ...s, goals: s.goals + 1 } : s);
      updateScorers(listKey, updated, `${teamType}_score`, goals);
    } else {
      updateScorers(listKey, [...current, { name: playerName, goals }], `${teamType}_score`, goals);
    }
  };

  const removeOrDecrementScorer = (teamType, index) => {
    const listKey = teamType === "home" ? "home_scorers" : "away_scorers";
    const list = [...(data[listKey] || [])];
    if (list[index].goals > 1) {
      list[index].goals -= 1;
    } else {
      list.splice(index, 1);
    }
    updateScorers(listKey, list, `${teamType}_score`, -1);
  };

  const addCard = (teamType, color, playerName) => {
    const key = `${teamType}_${color}_cards`;
    if (playerName?.trim()) onChange({ ...data, [key]: [...(data[key] || []), playerName.trim()] });
  };
  const removeCard = (teamType, color, index) => {
    const key = `${teamType}_${color}_cards`;
    onChange({ ...data, [key]: (data[key] || []).filter((_, i) => i !== index) });
  };

  const selectedTeams = (() => {
    const competition = (config.competitions || []).find(item => item.key === data.competition_key) || (config.competitions || [])[0];
    const seasonLink = competition?.seasons?.find(link => link.season_key === data.season_key) || competition?.seasons?.[0];
    return (seasonLink?.team_keys || []).map(key => config.teams.find(team => team.key === key)).filter(Boolean);
  })();

  return (
    <div className="space-y-5">
      <CompetitionSeasonSelector data={data} onChange={onChange} config={config} showMatchDetails collapsible />
      {/* 1. Score & Teams Hero Card */}
      <CollapsibleSection title="Tabellone Risultato" className="space-y-4">
        <div className="flex justify-end border-b border-slate-100 pb-3">
          <button
            onClick={onSwap}
            type="button"
            className="flex items-center space-x-1 px-3 py-1.5 rounded-lg bg-accvGreenLight hover:bg-accvGreen hover:text-white text-accvGreenDark text-xs font-bold transition-all border border-accvGreen/20"
          >
            <Icon name="swap" className="w-4 h-4" />
            <span>Inverti Casa/Trasferta</span>
          </button>
        </div>

        {/* Big Interactive Score Display */}
        <div className="grid grid-cols-5 items-center gap-3 py-2">
          {/* Home Team */}
          <div className="col-span-2 text-center space-y-2">
            <select
              value={data.home_team.name}
              onChange={(e) => {
                const team = config.teams.find((t) => t.name === e.target.value);
                onChange({
                  ...data,
                  home_team: {
                    ...data.home_team,
                    name: e.target.value,
                    short_name: team ? team.short_name : data.home_team.short_name,
                    logo_path: team ? `assets/logos/${team.filename || team.logo_filename}` : null
                  }
                });
              }}
              className="clean-input w-full text-xs font-bold rounded-lg p-2 text-center truncate"
            >
              {selectedTeams.filter((t) => t.name !== data.away_team.name).map((t) => (
                <option key={t.key} value={t.name}>{t.name}</option>
              ))}
            </select>

            <div className="flex items-center justify-center space-x-2">
              <button
                type="button"
                onClick={() => updateField("home_score", Math.max(0, data.home_score - 1))}
                className="w-8 h-8 rounded-lg bg-slate-100 text-slate-700 hover:bg-accvRed hover:text-white font-bold flex items-center justify-center border border-slate-200"
              >
                <Icon name="minus" className="w-4 h-4" />
              </button>
              <span className="font-header text-5xl lg:text-6xl text-accvGreenDark font-bold tracking-tight w-16 text-center">
                {data.home_score}
              </span>
              <button
                type="button"
                onClick={() => updateField("home_score", data.home_score + 1)}
                className="w-8 h-8 rounded-lg bg-slate-100 text-slate-700 hover:bg-accvGreen hover:text-white font-bold flex items-center justify-center border border-slate-200"
              >
                <Icon name="plus" className="w-4 h-4" />
              </button>
            </div>
            <div className="text-[11px] text-slate-500 uppercase font-semibold">Squadra Casa</div>
          </div>

          {/* VS Divider */}
          <div className="col-span-1 text-center font-header text-3xl text-accvGold">
            VS
          </div>

          {/* Away Team */}
          <div className="col-span-2 text-center space-y-2">
            <select
              value={data.away_team.name}
              onChange={(e) => {
                const team = config.teams.find((t) => t.name === e.target.value);
                onChange({
                  ...data,
                  away_team: {
                    ...data.away_team,
                    name: e.target.value,
                    short_name: team ? team.short_name : data.away_team.short_name,
                    logo_path: team ? `assets/logos/${team.filename || team.logo_filename}` : null
                  }
                });
              }}
              className="clean-input w-full text-xs font-bold rounded-lg p-2 text-center truncate"
            >
              {selectedTeams.filter((t) => t.name !== data.home_team.name).map((t) => (
                <option key={t.key} value={t.name}>{t.name}</option>
              ))}
            </select>

            <div className="flex items-center justify-center space-x-2">
              <button
                type="button"
                onClick={() => updateField("away_score", Math.max(0, data.away_score - 1))}
                className="w-8 h-8 rounded-lg bg-slate-100 text-slate-700 hover:bg-accvRed hover:text-white font-bold flex items-center justify-center border border-slate-200"
              >
                <Icon name="minus" className="w-4 h-4" />
              </button>
              <span className="font-header text-5xl lg:text-6xl text-slate-700 font-bold tracking-tight w-16 text-center">
                {data.away_score}
              </span>
              <button
                type="button"
                onClick={() => updateField("away_score", data.away_score + 1)}
                className="w-8 h-8 rounded-lg bg-slate-100 text-slate-700 hover:bg-accvGreen hover:text-white font-bold flex items-center justify-center border border-slate-200"
              >
                <Icon name="plus" className="w-4 h-4" />
              </button>
            </div>
            <div className="text-[11px] text-slate-500 uppercase font-semibold">Ospiti</div>
          </div>
        </div>
      </CollapsibleSection>

      {/* 2. Marcatori Rapidi (Scorers) */}
      <CollapsibleSection title="Marcatori Partita" className="space-y-4">

        {/* Home Scorers */}
        <div className="space-y-2">
          <div className="text-xs font-semibold text-slate-700 flex items-center justify-between">
            <span>Marcatori ACCV:</span>
            <span className="text-[11px] text-slate-500">Clicca sui giocatori della rosa sotto per aggiungere un gol:</span>
          </div>

          {/* Quick Roster Chips for ACCV */}
          <div className="flex flex-wrap gap-1.5 max-h-40 overflow-y-auto p-2 bg-slate-50 rounded-xl border border-slate-200">
            {config.players.filter((p) => !["Allenatore", "Dirigente"].includes(p.role)).map((p) => (
              <button
                key={p.key}
                type="button"
                onClick={() => addScorer(accvScorerType, p.name)}
                className="px-2.5 py-1 text-[11px] rounded-lg bg-white hover:bg-accvGreen hover:text-white text-slate-700 transition-all font-semibold border border-slate-200 shadow-sm"
              >
                +{p.name}
              </button>
            ))}
          </div>

          {/* Current Home Scorers List */}
          <div className="flex flex-wrap gap-2 pt-1">
            {accvScorers.map((s, idx) => (
              <div key={idx} className="flex items-center space-x-1.5 bg-accvGreenLight border border-accvGreen/30 px-3 py-1 rounded-lg text-xs font-bold text-accvGreenDark">
                <span>{s.name}</span>
                <span>({s.goals})</span>
                <button
                  onClick={() => removeOrDecrementScorer(accvScorerType, idx)}
                  type="button"
                  className="text-accvRed hover:text-red-700 ml-1 font-bold"
                >
                  ✕
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Away Scorers Quick Input */}
        <div className="pt-2 border-t border-slate-100 space-y-2">
          <div className="text-xs font-semibold text-slate-700">Marcatori {opponentTeam.name}:</div>
          <div className="flex gap-2">
            <input
              type="text"
              id="away-scorer-input"
              className="clean-input flex-1 px-3 py-1.5 text-xs rounded-lg"
              onKeyDown={(e) => {
                if (e.key === "Enter" && e.target.value.trim()) {
                  addScorer(opponentScorerType, e.target.value.trim());
                  e.target.value = "";
                }
              }}
            />
            <button
              type="button"
              onClick={() => {
                const inp = document.getElementById("away-scorer-input");
                if (inp && inp.value.trim()) {
                  addScorer(opponentScorerType, inp.value.trim());
                  inp.value = "";
                }
              }}
              className="px-3 py-1.5 bg-slate-800 hover:bg-slate-900 text-white text-xs font-bold rounded-lg shadow-sm"
            >
              + Aggiungi
            </button>
          </div>
          <div className="flex flex-wrap gap-2 pt-1">
            {opponentScorers.map((s, idx) => (
              <div key={idx} className="flex items-center space-x-1.5 bg-slate-100 border border-slate-200 px-3 py-1 rounded-lg text-xs font-bold text-slate-800">
                <span>{s.name}</span>
                <span className="text-slate-500">({s.goals})</span>
                <button
                  onClick={() => removeOrDecrementScorer(opponentScorerType, idx)}
                  type="button"
                  className="text-accvRed hover:text-red-700 ml-1 font-bold"
                >
                  ✕
                </button>
              </div>
            ))}
          </div>
        </div>
      </CollapsibleSection>

      {/* 3. Cartellini */}
      <CollapsibleSection title="Cartellini" className="space-y-4">
        {[{ type: accvScorerType, label: "ACCV", roster: true }, { type: opponentScorerType, label: opponentTeam.name, roster: false }].map((team) => (
          <div key={team.type} className="space-y-3">
            <div className="text-xs font-semibold text-slate-700">{team.label}</div>
            {[
              { color: "yellow", label: "Gialli", style: "bg-yellow-100 border-yellow-300 text-yellow-900" },
              { color: "red", label: "Rossi", style: "bg-red-100 border-red-300 text-red-900" }
            ].map((card) => {
              const key = `${team.type}_${card.color}_cards`;
              const names = data[key] || [];
              return <div key={card.color} className="space-y-2">
                <div className="text-[11px] font-bold text-slate-500 uppercase">Cartellini {card.label}</div>
                {team.roster ? <div className="flex flex-wrap gap-1.5 max-h-24 overflow-y-auto p-2 bg-slate-50 rounded-xl border border-slate-200">
                  {config.players.filter((p) => !["Allenatore", "Dirigente"].includes(p.role)).map((p) => <button key={p.key} type="button" onClick={() => addCard(team.type, card.color, p.name)} className="px-2.5 py-1 text-[11px] rounded-lg bg-white hover:bg-slate-200 text-slate-700 font-semibold border border-slate-200">+{p.name}</button>)}
                </div> : <div className="flex gap-2">
                  <input id={`${team.type}-${card.color}-input`} type="text" className="clean-input flex-1 px-3 py-1.5 text-xs rounded-lg" placeholder="Cognome giocatore" onKeyDown={(e) => { if (e.key === "Enter") { addCard(team.type, card.color, e.target.value); e.target.value = ""; } }} />
                  <button type="button" onClick={() => { const input = document.getElementById(`${team.type}-${card.color}-input`); addCard(team.type, card.color, input?.value); if (input) input.value = ""; }} className="px-3 py-1.5 bg-slate-800 text-white text-xs font-bold rounded-lg">+ Aggiungi</button>
                </div>}
                <div className="flex flex-wrap gap-2">{names.map((name, index) => <div key={`${name}-${index}`} className={`flex items-center gap-1.5 border px-3 py-1 rounded-lg text-xs font-bold ${card.style}`}><span>{name}</span><span aria-label={`cartellino ${card.label.toLowerCase()}`} className={`inline-block w-2.5 h-3.5 rounded-[2px] ${card.color === "yellow" ? "bg-yellow-400" : "bg-red-600"}`} /><button type="button" onClick={() => removeCard(team.type, card.color, index)} className="text-accvRed ml-1 font-bold">✕</button></div>)}</div>
              </div>;
            })}
            {team.type === accvScorerType && <div className="border-t border-slate-100" />}
          </div>
        ))}
      </CollapsibleSection>

      {/* Sfondo & Filtri */}
      <CollapsibleSection title="Sfondo & Filtri" className="space-y-4">
        {/* Background Selector */}
        <BackgroundSelector
          emotion={data.emotion}
          bgPath={data.bg_path}
          onSelectEmotion={(em) => onChange({ ...data, emotion: em, bg_path: null })}
          onSelectBgPath={(path) => updateField("bg_path", path)}
          onClearCustomBg={() => updateField("bg_path", null)}
          config={config}
          showToast={showToast}
        />

        {/* Contrast Filter */}
        <div className="flex items-center space-x-4 pt-2">
          <label className="flex items-center space-x-2 text-xs font-semibold cursor-pointer text-slate-700">
            <input
              type="checkbox"
              checked={data.remove_contrast}
              onChange={(e) => updateField("remove_contrast", e.target.checked)}
              className="rounded border-slate-300 text-accvGreen focus:ring-accvGreen"
            />
            <span>Effetto Flat (Riduci contrasto sfondo per far risaltare il testo)</span>
          </label>
        </div>
      </CollapsibleSection>
    </div>
  );
}

// --- TAB 2: Next Match Form ---
function NextMatchForm({ data, onChange, config, onSwap, showToast }) {
  const updateField = (field, val) => onChange({ ...data, [field]: val });
  const selectedTeams = (() => {
    const competition = (config.competitions || []).find(item => item.key === data.competition_key) || (config.competitions || [])[0];
    const seasonLink = competition?.seasons?.find(link => link.season_key === data.season_key) || competition?.seasons?.[0];
    return (seasonLink?.team_keys || []).map(key => config.teams.find(team => team.key === key)).filter(Boolean);
  })();

  return (
    <div className="clean-card p-5 space-y-5">
      <CompetitionSeasonSelector data={data} onChange={onChange} config={config} />
      <div className="flex items-center justify-between border-b border-slate-100 pb-3">
        <span className="text-accvGreen font-bold text-xs uppercase tracking-wider flex items-center space-x-1.5">
          <Icon name="calendar" className="w-4 h-4 text-accvGreen" />
          <span>Locandina Prossima Partita</span>
        </span>
        <button
          onClick={onSwap}
          type="button"
          className="flex items-center space-x-1 px-3 py-1.5 rounded-lg bg-accvGreenLight hover:bg-accvGreen hover:text-white text-accvGreenDark text-xs font-bold transition-all border border-accvGreen/20"
        >
          <Icon name="swap" className="w-4 h-4" />
          <span>Inverti Squadre</span>
        </button>
      </div>

      {/* Teams selector */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="text-[11px] font-bold text-slate-600 uppercase">Squadra Casa</label>
          <select
            value={data.home_team.name}
            onChange={(e) => {
              const team = selectedTeams.find((t) => t.name === e.target.value);
              updateField("home_team", {
                name: e.target.value,
                short_name: team ? team.short_name : "HOM",
                logo_path: team ? `assets/logos/${team.filename || team.logo_filename}` : null
              });
            }}
            className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-bold"
          >
            {selectedTeams.filter(t => t.name !== data.away_team.name).map((t) => (
              <option key={t.key} value={t.name}>{t.name}</option>
            ))}
          </select>
        </div>

        <div>
          <label className="text-[11px] font-bold text-slate-600 uppercase">Squadra Ospite</label>
          <select
            value={data.away_team.name}
            onChange={(e) => {
              const team = selectedTeams.find((t) => t.name === e.target.value);
              updateField("away_team", {
                name: e.target.value,
                short_name: team ? team.short_name : "OPP",
                logo_path: team ? `assets/logos/${team.filename || team.logo_filename}` : null
              });
            }}
            className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-bold"
          >
            {selectedTeams.filter(t => t.name !== data.home_team.name).map((t) => (
              <option key={t.key} value={t.name}>{t.name}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Event Details */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="text-[11px] font-bold text-slate-600 uppercase">Titolo / Matchday</label>
          <input
            type="text"
            value={data.matchday}
            onChange={(e) => updateField("matchday", e.target.value)}
            className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-medium"
          />
        </div>
        <div>
          <label className="text-[11px] font-bold text-slate-600 uppercase">Data & Calcio d'Inizio</label>
          <input
            type="text"
            value={data.date}
            onChange={(e) => updateField("date", e.target.value)}
            className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-medium"
          />
        </div>
        <div>
          <label className="text-[11px] font-bold text-slate-600 uppercase">Impianto / Campo Sportivo</label>
          <input
            type="text"
            value={data.location}
            onChange={(e) => updateField("location", e.target.value)}
            className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-medium"
          />
        </div>
      </div>

      {/* Background Selector */}
      <BackgroundSelector
        emotion={data.emotion}
        bgPath={data.bg_path}
        onSelectEmotion={(em) => onChange({ ...data, emotion: em, bg_path: null })}
        onSelectBgPath={(path) => updateField("bg_path", path)}
        onClearCustomBg={() => updateField("bg_path", null)}
        config={config}
        showToast={showToast}
      />
    </div>
  );
}

// --- TAB 3: MVP Form ---
function MVPForm({ data, onChange, config, showToast }) {
  const updateField = (field, val) => onChange({ ...data, [field]: val });

  return (
    <div className="clean-card p-5 space-y-5">
      <div className="border-b border-slate-100 pb-3 flex items-center justify-between">
        <span className="text-accvGoldDark font-bold text-xs uppercase tracking-wider flex items-center space-x-1.5">
          <Icon name="star" className="w-4 h-4 text-accvGold" />
          <span>Card Migliore in Campo (Stile FUT / eSports)</span>
        </span>
      </div>

      {/* Player Roster Quick Picker */}
      <div className="space-y-2">
        <label className="text-[11px] font-bold text-slate-600 uppercase">Seleziona Giocatore della Rosa</label>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 max-h-36 overflow-y-auto p-2 bg-slate-50 rounded-xl border border-slate-200">
          {config.players.filter((p) => !["Allenatore", "Dirigente"].includes(p.role)).map((p) => (
            <button
              key={p.key}
              type="button"
              onClick={() => {
                onChange({
                  ...data,
                  player_name: p.name,
                  jersey_number: p.number || "10",
                  position: p.role
                });
              }}
              className={`p-2 rounded-lg text-left text-xs font-bold transition-all border ${
                data.player_name === p.name
                  ? "bg-accvGoldLight border-accvGold text-accvGoldDark shadow-sm font-extrabold"
                  : "bg-white text-slate-700 border-slate-200 hover:border-accvGold hover:bg-amber-50/30"
              }`}
            >
              <div className="truncate">{p.name}</div>
              <div className="text-[10px] text-slate-500 font-normal">{p.role} #{p.number}</div>
            </button>
          ))}
        </div>
      </div>

      {/* Stats and Rating */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div>
          <label className="text-[11px] font-bold text-accvGoldDark uppercase">Voto Pagella</label>
          <input
            type="text"
            value={data.rating}
            onChange={(e) => updateField("rating", e.target.value)}
            className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-bold text-accvGoldDark"
          />
        </div>
        <div>
          <label className="text-[11px] font-bold text-slate-600 uppercase">Gol Segnati</label>
          <input
            type="number"
            min="0"
            value={data.goals}
            onChange={(e) => updateField("goals", parseInt(e.target.value) || 0)}
            className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-medium"
          />
        </div>
        <div>
          <label className="text-[11px] font-bold text-slate-600 uppercase">Assist</label>
          <input
            type="number"
            min="0"
            value={data.assists}
            onChange={(e) => updateField("assists", parseInt(e.target.value) || 0)}
            className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-medium"
          />
        </div>
        <div>
          <label className="text-[11px] font-bold text-slate-600 uppercase">Parate Decisive</label>
          <input
            type="number"
            min="0"
            value={data.saves}
            onChange={(e) => updateField("saves", parseInt(e.target.value) || 0)}
            className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-medium"
          />
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="text-[11px] font-bold text-slate-600 uppercase">Squadra Avversaria</label>
          <input
            type="text"
            value={data.match_opponent}
            onChange={(e) => updateField("match_opponent", e.target.value)}
            className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-medium"
          />
        </div>
        <div>
          <label className="text-[11px] font-bold text-slate-600 uppercase">Data Partita</label>
          <input
            type="text"
            value={data.match_date}
            onChange={(e) => updateField("match_date", e.target.value)}
            className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-medium"
          />
        </div>
      </div>

      {/* Background Selector */}
      <BackgroundSelector
        emotion={data.emotion}
        bgPath={data.bg_path}
        onSelectEmotion={(em) => onChange({ ...data, emotion: em, bg_path: null })}
        onSelectBgPath={(path) => updateField("bg_path", path)}
        onClearCustomBg={() => updateField("bg_path", null)}
        config={config}
        showToast={showToast}
      />
    </div>
  );
}

// --- TAB 4: Figurina Panini Form ---
function FigurinaForm({ data, onChange, config, onSelectPlayer }) {
  const updateField = (field, val) => onChange({ ...data, [field]: val });

  return (
    <div className="clean-card p-5 space-y-5">
      <div className="border-b border-slate-100 pb-3 flex items-center justify-between">
        <span className="text-accvGreen font-bold text-xs uppercase tracking-wider flex items-center space-x-1.5">
          <Icon name="badge" className="w-4 h-4 text-accvGreen" />
          <span>Figurine Calciatori Panini - Serie B Perini</span>
        </span>
      </div>

      {/* Visual Players Selector */}
      <div className="space-y-2">
        <label className="text-[11px] font-bold text-slate-600 uppercase">Seleziona Calciatore ACCV</label>
        <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-6 gap-2 max-h-56 overflow-y-auto p-2 bg-slate-50 rounded-xl border border-slate-200">
          {config.players.map((p) => {
            const isSelected = data.player_key === p.key;
            return (
              <button
                key={p.key}
                type="button"
                onClick={() => onSelectPlayer(p)}
                className={`p-2 rounded-xl text-center transition-all border flex flex-col items-center justify-center space-y-1.5 ${
                  isSelected
                    ? "bg-accvGreenLight border-accvGreen shadow-sm"
                    : "bg-white border-slate-200 hover:border-slate-300"
                }`}
              >
                <div className="w-12 h-12 rounded-full bg-slate-100 overflow-hidden border border-slate-200 flex items-center justify-center">
                  {p.photo_url ? (
                    <img src={p.photo_url} alt={p.name} className="w-full h-full object-cover object-top" />
                  ) : (
                    <span className="font-bold text-xs text-accvGreen">#{p.number}</span>
                  )}
                </div>
                <div className="text-[11px] font-bold text-slate-800 truncate w-full">{p.name.split(" ")[0]}</div>
                <div className="text-[9px] text-slate-500 truncate w-full">{p.role}</div>
              </button>
            );
          })}
        </div>
      </div>

      {/* Player details */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <div className="sm:col-span-2">
          <label className="text-[11px] font-bold text-slate-600 uppercase">Nome e Cognome sulla Card</label>
          <input
            type="text"
            value={data.player_name}
            onChange={(e) => updateField("player_name", e.target.value)}
            className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-bold"
          />
        </div>
        <div>
          <label className="text-[11px] font-bold text-slate-600 uppercase">Numero Maglia</label>
          <input
            type="text"
            value={data.jersey_number}
            onChange={(e) => updateField("jersey_number", e.target.value)}
            className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg text-center font-bold text-accvGreen"
          />
        </div>
      </div>

      <div className="flex items-center space-x-6 pt-2 border-t border-slate-100">
        <label className="flex items-center space-x-2 text-xs font-semibold cursor-pointer text-slate-700">
          <input
            type="checkbox"
            checked={data.with_banner}
            onChange={(e) => updateField("with_banner", e.target.checked)}
            className="rounded border-slate-300 text-accvGreen focus:ring-accvGreen"
          />
          <span>Includi Targhetta Ufficiale Panini (Nome, Ruolo, Logo ACCV)</span>
        </label>
      </div>
    </div>
  );
}

// --- TAB 5: Roster & Teams Manager (FULL CRUD) ---
function RosterAndTeamsManager({ config, onConfigReload, showToast }) {
  const [managerTab, setManagerTab] = useState("my-team"); // my-team, teams
  const [searchQuery, setSearchQuery] = useState("");
  const [playerSort, setPlayerSort] = useState({ key: "name", direction: "asc" });
  const [teamSort, setTeamSort] = useState({ key: "name", direction: "asc" });

  // Modals state for Player CRUD
  const [editingPlayer, setEditingPlayer] = useState(null); // null or player obj
  const [isPlayerModalOpen, setIsPlayerModalOpen] = useState(false);

  // Modals state for Team CRUD
  const [editingTeam, setEditingTeam] = useState(null); // null or team obj
  const [isTeamModalOpen, setIsTeamModalOpen] = useState(false);
  const [editingSeason, setEditingSeason] = useState(null);
  const [editingCompetition, setEditingCompetition] = useState(null);

  // --- PLAYERS CRUD ---
  const handleSavePlayer = (e) => {
    e.preventDefault();
    if (!editingPlayer.name || !editingPlayer.key) {
      alert("Inserisci nome e identificativo del calciatore.");
      return;
    }
    const staffRole = ["Allenatore", "Dirigente"].includes(editingPlayer.role);
    const jerseyNumber = editingPlayer.number === "" && staffRole ? null : Number(editingPlayer.number);
    if (jerseyNumber !== null && (!Number.isInteger(jerseyNumber) || jerseyNumber < 1 || jerseyNumber > 99)) {
      alert("Il numero di maglia deve essere compreso tra 1 e 99.");
      return;
    }
    if (jerseyNumber !== null && (config.players || []).some((player) => Number(player.number) === jerseyNumber && player.key !== editingPlayer.key)) {
      alert("Questo numero di maglia è già assegnato a un altro giocatore.");
      return;
    }

    fetch("/api/players", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        key: editingPlayer.key,
        name: editingPlayer.name,
        display_name: editingPlayer.display_name || "",
        role: editingPlayer.role || "Giocatore",
        number: jerseyNumber === null ? "" : String(jerseyNumber),
        team: "A.C.C.V."
      })
    })
      .then((r) => r.json())
      .then(() => {
        setIsPlayerModalOpen(false);
        setEditingPlayer(null);
        onConfigReload();
        showToast("✓ Calciatore salvato con successo!");
      })
      .catch((err) => alert("Errore salvataggio: " + err));
  };

  const handleDeletePlayer = useCallback((key, name) => {
    if (!confirm(`Sei sicuro di voler eliminare ${name} dalla rosa?`)) return;

    fetch("/api/players/delete", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ key })
    })
      .then((r) => r.json())
      .then(() => {
        onConfigReload();
        showToast(`✓ Calciatore ${name} rimosso.`);
      })
      .catch((err) => alert("Errore eliminazione: " + err));
  }, [onConfigReload, showToast]);

  const handlePlayerPhotoUpload = useCallback((e, playerKey) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      fetch("/api/upload", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          upload_type: "player",
          filename: `${playerKey}.png`,
          data: reader.result
        })
      })
        .then((r) => r.json())
        .then(() => {
          onConfigReload();
          showToast("✓ Foto sagomata del calciatore aggiornata!");
        });
    };
    reader.readAsDataURL(file);
  }, [onConfigReload, showToast]);

  // --- TEAMS CRUD ---
  const handleSaveTeam = (e) => {
    e.preventDefault();
    if (!editingTeam.name || !editingTeam.key) {
      alert("Inserisci nome e identificativo della squadra.");
      return;
    }

    fetch("/api/teams", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        key: editingTeam.key,
        name: editingTeam.name,
        extended_name: editingTeam.extended_name || "",
        short_name: editingTeam.short_name || editingTeam.name.slice(0, 3).toUpperCase(),
        primary_color: editingTeam.primary_color || [16, 185, 129],
        secondary_color: editingTeam.secondary_color || [239, 68, 68],
        tertiary_color: editingTeam.tertiary_color || [59, 130, 246],
        ...(editingTeam.key === "accv" ? {} : { rivalry: Number(editingTeam.rivalry) || 3 }),
        notes: editingTeam.notes || "",
        logo_filename: editingTeam.logo_filename || `${editingTeam.key}.png`
      })
    })
      .then((r) => r.json())
      .then(() => {
        setIsTeamModalOpen(false);
        setEditingTeam(null);
        onConfigReload();
        showToast("✓ Squadra salvata con successo!");
      })
      .catch((err) => alert("Errore salvataggio squadra: " + err));
  };

  const handleDeleteTeam = (key, name) => {
    if (!confirm(`Sei sicuro di voler eliminare la squadra ${name} dal campionato?`)) return;

    fetch("/api/teams/delete", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ key })
    })
      .then((r) => r.json())
      .then(() => {
        onConfigReload();
        showToast(`✓ Squadra ${name} eliminata.`);
      })
      .catch((err) => alert("Errore eliminazione squadra: " + err));
  };

  const handleTeamLogoUpload = (e, teamKey) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      const filename = `${teamKey}.png`;
      fetch("/api/upload", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          upload_type: "logo",
          filename: filename,
          data: reader.result
        })
      })
        .then((r) => r.json())
        .then(() => {
          onConfigReload();
          showToast("✓ Stemma logo squadra aggiornato!");
        });
    };
    reader.readAsDataURL(file);
  };

  // Filtered lists
  const filteredPlayers = useMemo(() => (config.players || []).filter(
    (p) =>
      p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (p.display_name || "").toLowerCase().includes(searchQuery.toLowerCase()) ||
      p.role.toLowerCase().includes(searchQuery.toLowerCase()) ||
      String(p.number).includes(searchQuery)
  ).sort((a, b) => {
    const left = playerSort.key === "number" ? Number(a.number) : String(a[playerSort.key] || "").toLowerCase();
    const right = playerSort.key === "number" ? Number(b.number) : String(b[playerSort.key] || "").toLowerCase();
    return (left > right ? 1 : left < right ? -1 : 0) * (playerSort.direction === "asc" ? 1 : -1);
  }), [config.players, searchQuery, playerSort]);

  const filteredTeams = useMemo(() => (config.teams || []).filter(
    (t) => t.key !== "accv" && !t.name.toUpperCase().includes("ACCV") && (
      t.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      t.short_name.toLowerCase().includes(searchQuery.toLowerCase())
    )
  ).sort((a, b) => {
    const left = teamSort.key === "rivalry" ? Number(a.rivalry || 3) : String(a[teamSort.key] || "").toLowerCase();
    const right = teamSort.key === "rivalry" ? Number(b.rivalry || 3) : String(b[teamSort.key] || "").toLowerCase();
    return (left > right ? 1 : left < right ? -1 : 0) * (teamSort.direction === "asc" ? 1 : -1);
  }), [config.teams, searchQuery, teamSort]);

  const accvTeam = (config.teams || []).find((team) => team.key === "accv" || team.name.toUpperCase().includes("ACCV"));
  const accvPlayers = filteredPlayers;
  const openAccvTeamEditor = () => {
    if (!accvTeam) return;
    setEditingTeam({
      ...accvTeam,
      isEditing: true,
      extended_name: accvTeam.extended_name || "",
      primary_color: accvTeam.primary_color || [16, 185, 129],
      secondary_color: accvTeam.secondary_color || [239, 68, 68],
      tertiary_color: accvTeam.tertiary_color || [59, 130, 246],
      notes: accvTeam.notes || ""
    });
    setIsTeamModalOpen(true);
  };

  const saveRecord = async (kind, record) => {
    const response = await fetch(`/api/${kind}`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(record) });
    const result = await response.json();
    if (!response.ok) throw new Error(result.error || "Salvataggio non riuscito");
    setEditingSeason(null); setEditingCompetition(null); onConfigReload(); showToast("✓ Dati salvati con successo!");
  };
  const deleteRecord = async (kind, record, label) => {
    if (!confirm(`Eliminare ${label} “${record.description}”?`)) return;
    const response = await fetch(`/api/${kind}/delete`, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ key: record.key }) });
    if (!response.ok) { alert("Errore durante l'eliminazione."); return; }
    onConfigReload(); showToast(`✓ ${label} eliminata.`);
  };
  const uploadCompetitionLogo = (file, key) => new Promise((resolve, reject) => {
    if (!file) return resolve("");
    const reader = new FileReader();
    reader.onload = () => fetch("/api/upload", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ upload_type: "logo", filename: `competition_${key}${file.name.substring(file.name.lastIndexOf(".")) || ".png"}`, data: reader.result }) }).then(r => r.json()).then(data => resolve(data.filename)).catch(reject);
    reader.onerror = reject; reader.readAsDataURL(file);
  });

  const handleEditPlayer = useCallback((player) => {
    setEditingPlayer({ ...player, isEditing: true });
    setIsPlayerModalOpen(true);
  }, []);

  return (
    <div className="w-full max-w-none mx-auto space-y-5">
      <div className="flex items-center gap-2 bg-white p-2 rounded-2xl border border-slate-200 shadow-sm">
        <button onClick={() => setManagerTab("my-team")} className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold ${managerTab === "my-team" ? "bg-accvGreen text-white" : "text-slate-600 hover:bg-slate-100"}`}>
          <Icon name="shield" className="w-4 h-4" /> Mia Squadra ({config.players?.length || 0})
        </button>
        <button onClick={() => setManagerTab("teams")} className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold ${managerTab === "teams" ? "bg-accvGreen text-white" : "text-slate-600 hover:bg-slate-100"}`}>
          <Icon name="users" className="w-4 h-4" /> Squadre ({filteredTeams.length})
        </button>
        <button onClick={() => setManagerTab("seasons")} className={`px-4 py-2 rounded-xl text-xs font-bold ${managerTab === "seasons" ? "bg-accvGreen text-white" : "text-slate-600 hover:bg-slate-100"}`}>Stagioni ({config.seasons?.length || 0})</button>
        <button onClick={() => setManagerTab("competitions")} className={`px-4 py-2 rounded-xl text-xs font-bold ${managerTab === "competitions" ? "bg-accvGreen text-white" : "text-slate-600 hover:bg-slate-100"}`}>Competizioni ({config.competitions?.length || 0})</button>
      </div>

      {managerTab === "my-team" && accvTeam && (
        <section className="clean-card p-5 lg:p-7 flex flex-col lg:flex-row lg:items-center gap-6">
          <label className="relative cursor-pointer group shrink-0">
            <input type="file" accept="image/*" className="hidden" onChange={(e) => handleTeamLogoUpload(e, accvTeam.key)} />
            <div className="w-28 h-28 rounded-2xl bg-slate-50 border border-slate-200 p-3 flex items-center justify-center group-hover:border-accvGreen">
              {accvTeam.logo_url ? <img src={accvTeam.logo_url} alt={accvTeam.name} className="w-full h-full object-contain" /> : <span className="font-bold text-xl text-slate-400">{accvTeam.short_name}</span>}
            </div>
            <span className="absolute -bottom-2 left-1/2 -translate-x-1/2 whitespace-nowrap rounded-full bg-white border border-slate-200 px-2 py-1 text-[10px] font-semibold text-accvGreen">Cambia logo</span>
          </label>
          <div className="flex-1 min-w-0">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <div className="text-[10px] font-bold uppercase tracking-wider text-accvGreen">Squadra protagonista</div>
                <h2 className="mt-1 text-2xl font-header font-bold text-slate-900">{accvTeam.name}</h2>
                {accvTeam.extended_name && <div className="mt-1 text-sm text-slate-600">{accvTeam.extended_name}</div>}
              </div>
              <button onClick={openAccvTeamEditor} className="flex items-center gap-2 px-4 py-2 bg-accvGreen hover:bg-accvGreenDark text-white text-xs font-bold rounded-xl shadow-sm">
                <Icon name="edit" className="w-4 h-4" /> Modifica squadra
              </button>
            </div>
            <div className="mt-4 flex flex-wrap items-center gap-3 text-xs text-slate-600">
              <span className="rounded-lg bg-slate-100 px-2.5 py-1.5 font-bold">Sigla: {accvTeam.short_name}</span>
              {[accvTeam.primary_color || [16, 185, 129], accvTeam.secondary_color || [239, 68, 68], accvTeam.tertiary_color || [59, 130, 246]].map((color, index) => (
                <span key={index} title={`Colore ${index + 1}`} className="w-5 h-5 rounded-full border border-slate-300" style={{ backgroundColor: `rgb(${color[0]}, ${color[1]}, ${color[2]})` }} />
              ))}
              {accvTeam.notes && <span className="basis-full">{accvTeam.notes}</span>}
            </div>
          </div>
        </section>
      )}

      {(managerTab === "my-team" || managerTab === "teams") && <section className="clean-card p-4 lg:p-5 space-y-3 flex flex-col">
        <div className="order-2 flex flex-col lg:flex-row lg:items-center gap-3 border-t border-slate-100 pt-3">
          <input type="text" value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} className="clean-input w-full lg:flex-1 px-4 py-2.5 text-xs rounded-xl font-medium" />
          {managerTab === "teams" && (
            <div className="flex items-center gap-2">
              <label className="text-[10px] font-bold uppercase text-slate-500">Ordina per</label>
              <select value={teamSort.key} onChange={(e) => setTeamSort((prev) => ({ ...prev, key: e.target.value }))} className="clean-input px-3 py-2 text-xs rounded-lg">
                <option value="name">Nome</option><option value="extended_name">Nome esteso</option><option value="short_name">Sigla</option><option value="rivalry">Rivalità</option>
              </select>
              <button onClick={() => setTeamSort((prev) => ({ ...prev, direction: prev.direction === "asc" ? "desc" : "asc" }))} className="px-3 py-2 bg-slate-100 hover:bg-slate-200 rounded-lg text-xs font-bold" aria-label="Inverti ordinamento">{teamSort.direction === "asc" ? "↑" : "↓"}</button>
            </div>
          )}
        </div>
        <div className="order-1 flex flex-col items-start gap-3">
          <div>
            <h2 className="text-sm font-bold text-slate-900">{managerTab === "my-team" ? "Giocatori della rosa" : "Squadre"}</h2>
            <p className="text-[11px] text-slate-500">{managerTab === "my-team" ? `${filteredPlayers.length} elementi` : `${filteredTeams.length} squadre`}</p>
          </div>
          {managerTab === "my-team" ? (
            <button onClick={() => { setEditingPlayer({ key: "", name: "", display_name: "", role: "Centrocampista", number: "", isEditing: false }); setIsPlayerModalOpen(true); }} className="flex items-center gap-2 px-4 py-2 bg-accvGreen hover:bg-accvGreenDark text-white text-xs font-bold rounded-xl shadow-sm">
              <Icon name="plus" className="w-4 h-4" /> Nuovo Calciatore
            </button>
          ) : (
            <button onClick={() => { setEditingTeam({ key: "", name: "", extended_name: "", short_name: "", primary_color: [16, 185, 129], secondary_color: [239, 68, 68], tertiary_color: [59, 130, 246], rivalry: 3, notes: "" }); setIsTeamModalOpen(true); }} className="flex items-center gap-2 px-4 py-2 bg-accvGreen hover:bg-accvGreenDark text-white text-xs font-bold rounded-xl shadow-sm">
              <Icon name="plus" className="w-4 h-4" /> Nuova Squadra
            </button>
          )}
        </div>
      </section>}

      {managerTab === "my-team" && (
        <div className="clean-card overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 uppercase text-[10px] font-bold">
                <th className="py-3 px-4">Foto Sagoma</th>
                {[{ key: "name", label: "Nome Completo" }, { key: "display_name", label: "Nome Mostrato" }, { key: "role", label: "Ruolo" }, { key: "number", label: "N° Maglia" }].map(({ key, label }) => (
                  <th key={key} className={`py-3 px-4 ${key === "number" ? "text-center" : ""}`}>
                    <button onClick={() => setPlayerSort((prev) => ({ key, direction: prev.key === key && prev.direction === "asc" ? "desc" : "asc" }))} className="hover:text-accvGreen">{label} {playerSort.key === key ? (playerSort.direction === "asc" ? "↑" : "↓") : "↕"}</button>
                  </th>
                ))}
                <th className="py-3 px-4 text-right">Azioni</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 font-medium">
              {accvPlayers.map((player) => (
                <PlayerRosterRow key={player.key} player={player} onEdit={handleEditPlayer} onDelete={handleDeletePlayer} onPhotoUpload={handlePlayerPhotoUpload} />
              ))}
            </tbody>
          </table>
        </div>
      )}

      {managerTab === "teams" && (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-5">
          {filteredTeams.map((t) => {
            const teamColors = [t.primary_color || [16, 185, 129], t.secondary_color || [239, 68, 68], t.tertiary_color || [59, 130, 246]];
            return (
              <div key={t.key} className="clean-card min-h-44 p-6 flex items-center justify-between gap-4 hover:shadow-card-hover transition-all">
                <div className="flex items-center gap-4 min-w-0">
                  <label className="relative cursor-pointer group shrink-0"><input type="file" accept="image/*" className="hidden" onChange={(e) => handleTeamLogoUpload(e, t.key)} /><div className="w-20 h-20 rounded-2xl bg-slate-50 border border-slate-200 p-2 flex items-center justify-center group-hover:border-accvGreen">{t.logo_url ? <img src={t.logo_url} alt={t.name} className="w-full h-full object-contain" /> : <span className="font-bold text-lg text-slate-400">{t.short_name}</span>}</div></label>
                  <div className="min-w-0"><div className="font-bold text-slate-900 text-base truncate">{t.name}</div>{t.extended_name && <div className="text-xs text-slate-500 truncate mt-1">{t.extended_name}</div>}<div className="flex items-center gap-2 mt-2"><span className="text-[10px] font-bold px-2 py-1 rounded bg-slate-100 text-slate-600">{t.short_name}</span>{teamColors.map((color, index) => <span key={index} className="w-4 h-4 rounded-full border border-slate-300" style={{ backgroundColor: `rgb(${color[0]}, ${color[1]}, ${color[2]})` }} />)}</div><div className="mt-2 text-xs text-slate-500">Rivalità {t.rivalry ?? 3}/5</div></div>
                </div>
                <div className="flex flex-col gap-2 shrink-0"><button onClick={() => { setEditingTeam({ ...t, isEditing: true, extended_name: t.extended_name || "", primary_color: t.primary_color || [16, 185, 129], secondary_color: t.secondary_color || [239, 68, 68], tertiary_color: t.tertiary_color || [59, 130, 246], rivalry: t.rivalry ?? 3, notes: t.notes || "" }); setIsTeamModalOpen(true); }} className="p-2 rounded-lg text-slate-500 hover:text-accvGreen hover:bg-slate-100" title="Modifica Squadra"><Icon name="edit" className="w-4 h-4" /></button><button onClick={() => handleDeleteTeam(t.key, t.name)} className="p-2 rounded-lg text-slate-400 hover:text-accvRed hover:bg-red-50" title="Elimina Squadra"><Icon name="trash" className="w-4 h-4" /></button></div>
              </div>
            );
          })}
        </div>
      )}

      {managerTab === "seasons" && (
        <section className="clean-card p-5 space-y-4">
          <div className="flex items-center justify-between"><div><h2 className="text-sm font-bold text-slate-900">Stagioni</h2><p className="text-[11px] text-slate-500">{config.seasons?.length || 0} stagioni configurate</p></div><button onClick={() => setEditingSeason({ key: "", description: "", notes: "", isEditing: false })} className="flex items-center gap-2 px-4 py-2 bg-accvGreen text-white text-xs font-bold rounded-xl"><Icon name="plus" className="w-4 h-4"/> Nuova Stagione</button></div>
          {(config.seasons || []).length === 0 ? <p className="py-8 text-center text-sm text-slate-400">Nessuna stagione inserita.</p> : <div className="divide-y divide-slate-100">{config.seasons.map(s => <div key={s.key} className="flex items-center justify-between py-3"><div><div className="font-bold text-sm text-slate-800">{s.description}</div>{s.notes && <p className="text-xs text-slate-500 mt-1">{s.notes}</p>}</div><div className="flex gap-2"><button onClick={() => setEditingSeason({ ...s, isEditing: true })} className="p-2 text-slate-500 hover:text-accvGreen" title="Modifica"><Icon name="edit" className="w-4 h-4"/></button><button onClick={() => deleteRecord("seasons", s, "stagione")} className="p-2 text-slate-400 hover:text-accvRed" title="Elimina"><Icon name="trash" className="w-4 h-4"/></button></div></div>)}</div>}
        </section>
      )}

      {managerTab === "competitions" && (
        <section className="clean-card p-5 space-y-4">
          <div className="flex items-center justify-between"><div><h2 className="text-sm font-bold text-slate-900">Competizioni</h2><p className="text-[11px] text-slate-500">{config.competitions?.length || 0} competizioni configurate</p></div><button onClick={() => setEditingCompetition({ key: "", description: "", notes: "", logo_filename: "", seasons: [], isEditing: false })} className="flex items-center gap-2 px-4 py-2 bg-accvGreen text-white text-xs font-bold rounded-xl"><Icon name="plus" className="w-4 h-4"/> Nuova Competizione</button></div>
          {(config.competitions || []).length === 0 ? <p className="py-8 text-center text-sm text-slate-400">Nessuna competizione inserita.</p> : <div className="grid gap-3">{config.competitions.map(c => <div key={c.key} className="flex items-center gap-4 border border-slate-100 rounded-xl p-3"><div className="w-14 h-14 rounded-lg bg-slate-50 flex items-center justify-center shrink-0">{c.logo_url ? <img src={c.logo_url} alt="" className="w-full h-full object-contain"/> : <Icon name="trophy" className="w-6 h-6 text-slate-300"/>}</div><div className="flex-1"><div className="font-bold text-sm">{c.description}</div><div className="text-xs text-slate-500">{(c.seasons || []).length} stagioni associate</div></div><button onClick={() => setEditingCompetition({ ...c, isEditing: true })} className="p-2 text-slate-500 hover:text-accvGreen" title="Modifica"><Icon name="edit" className="w-4 h-4"/></button><button onClick={() => deleteRecord("competitions", c, "competizione")} className="p-2 text-slate-400 hover:text-accvRed" title="Elimina"><Icon name="trash" className="w-4 h-4"/></button></div>)}</div>}
        </section>
      )}

      {(editingSeason || editingCompetition) && (() => {
        const isSeason = Boolean(editingSeason); const record = editingSeason || editingCompetition; const setRecord = isSeason ? setEditingSeason : setEditingCompetition; const kind = isSeason ? "stagione" : "competizione";
        const updateSeasonAssociation = (seasonKey, update) => setRecord(prev => ({ ...prev, seasons: (prev.seasons || []).map(s => s.season_key === seasonKey ? { ...s, ...update } : s) }));
        return <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4"><div className={`clean-card w-full ${isSeason ? "max-w-md" : "max-w-3xl"} max-h-[90vh] overflow-y-auto p-6 space-y-4 shadow-xl`}>
          <div className="flex justify-between border-b border-slate-100 pb-3"><h3 className="font-bold text-slate-900">{record.isEditing ? "Modifica" : "Nuova"} {kind}</h3><button onClick={() => setRecord(null)} className="text-slate-400">✕</button></div>
          <form onSubmit={async e => { e.preventDefault(); try { let data = { key: record.key, description: record.description, notes: record.notes || "" }; if (!isSeason) { data.seasons = record.seasons || []; data.logo_filename = record.logo_filename || ""; const file = e.currentTarget.elements.logo.files?.[0]; if (file) data.logo_filename = await uploadCompetitionLogo(file, record.key); } await saveRecord(isSeason ? "seasons" : "competitions", data); } catch (err) { alert(`Errore salvataggio: ${err.message}`); } }} className="space-y-4">
            {!record.isEditing && <label className="block text-[11px] font-bold text-slate-600 uppercase">Codice<input required value={record.key} onChange={e => setRecord({ ...record, key: e.target.value.toLowerCase().replace(/[^a-z0-9_-]+/g, "_") })} className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-mono"/></label>}
            <label className="block text-[11px] font-bold text-slate-600 uppercase">Descrizione<input required value={record.description} onChange={e => setRecord({ ...record, description: e.target.value })} className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg"/></label>
            <label className="block text-[11px] font-bold text-slate-600 uppercase">Note (facoltative)<textarea rows="2" value={record.notes || ""} onChange={e => setRecord({ ...record, notes: e.target.value })} className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg"/></label>
            {!isSeason && <><label className="block text-[11px] font-bold text-slate-600 uppercase">Logo{record.logo_url && <img src={record.logo_url} className="w-16 h-16 object-contain my-2"/>}<input name="logo" type="file" accept="image/*" className="block mt-2 text-xs"/></label>
              <div><div className="text-[11px] font-bold text-slate-600 uppercase mb-2">Stagioni e squadre partecipanti</div>{(config.seasons || []).length === 0 ? <p className="text-xs text-slate-400">Inserisci prima una stagione per associarla.</p> : <div className="space-y-3">{config.seasons.map(season => { const association = (record.seasons || []).find(s => s.season_key === season.key); const teamKeys = association?.team_keys || []; return <div key={season.key} className="rounded-xl border border-slate-200 p-3"><label className="flex items-center gap-2 font-bold text-sm"><input type="checkbox" checked={Boolean(association)} onChange={e => setRecord(prev => ({ ...prev, seasons: e.target.checked ? [...(prev.seasons || []), { season_key: season.key, team_keys: [] }] : (prev.seasons || []).filter(s => s.season_key !== season.key) }))}/>{season.description}</label>{association && <div className="grid sm:grid-cols-2 gap-2 mt-3">{(config.teams || []).map(team => <label key={team.key} className="flex items-center gap-2 text-xs text-slate-700"><input type="checkbox" checked={teamKeys.includes(team.key)} onChange={e => updateSeasonAssociation(season.key, { team_keys: e.target.checked ? [...teamKeys, team.key] : teamKeys.filter(key => key !== team.key) })}/>{team.name}</label>)}</div>}</div>})}</div>}</div>
            </>}
            <div className="flex justify-end gap-2 border-t pt-3"><button type="button" onClick={() => setRecord(null)} className="px-4 py-2 bg-slate-100 text-xs font-bold rounded-xl">Annulla</button><button className="px-4 py-2 bg-accvGreen text-white text-xs font-bold rounded-xl">Salva</button></div>
          </form>
        </div></div>;
      })()}

      {/* MODAL: PLAYER CREATE / EDIT */}
      {isPlayerModalOpen && editingPlayer && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="clean-card w-full max-w-md p-6 space-y-4 shadow-xl animate-scale-up">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="font-bold text-sm text-slate-900">
                {editingPlayer.name ? `Modifica Calciatore: ${editingPlayer.name}` : "Aggiungi Nuovo Calciatore ACCV"}
              </h3>
              <button onClick={() => setIsPlayerModalOpen(false)} className="text-slate-400 hover:text-slate-600 font-bold">✕</button>
            </div>

            <form onSubmit={handleSavePlayer} className="space-y-3">
              <div>
                <label className="text-[11px] font-bold text-slate-600 uppercase">Identificativo Chiave (es. 'rossi', 'bouba')</label>
                <input
                  type="text"
                  required
                  readOnly={Boolean(editingPlayer.isEditing)}
                  value={editingPlayer.key}
                  onChange={(e) => setEditingPlayer({ ...editingPlayer, key: e.target.value.toLowerCase().replace(/\s+/g, "-") })}
                  className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-mono font-bold"
                />
              </div>

              <div>
                <label className="text-[11px] font-bold text-slate-600 uppercase">Nome Completo</label>
                <input
                  type="text"
                  required
                  value={editingPlayer.name}
                  onChange={(e) => setEditingPlayer({ ...editingPlayer, name: e.target.value })}
                  className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-bold"
                />
              </div>

              <div>
                <label className="text-[11px] font-bold text-slate-600 uppercase">Nome Mostrato</label>
                <input
                  type="text"
                  value={editingPlayer.display_name || ""}
                  onChange={(e) => setEditingPlayer({ ...editingPlayer, display_name: e.target.value })}
                  className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-bold"
                  placeholder="Facoltativo, usato nella grafica Risultato"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-[11px] font-bold text-slate-600 uppercase">Ruolo</label>
                  <select
                    value={editingPlayer.role}
                    onChange={(e) => setEditingPlayer({ ...editingPlayer, role: e.target.value })}
                    className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-medium"
                  >
                    <option value="Portiere">Portiere</option>
                    <option value="Difensore">Difensore</option>
                    <option value="Centrocampista">Centrocampista</option>
                    <option value="Attaccante">Attaccante</option>
                    <option value="Allenatore">Allenatore</option>
                    <option value="Dirigente">Dirigente</option>
                  </select>
                </div>

                <div>
                  <label className="text-[11px] font-bold text-slate-600 uppercase">Numero di Maglia</label>
                  <input
                    type="number"
                    required={!['Allenatore', 'Dirigente'].includes(editingPlayer.role)}
                    min="1"
                    max="99"
                    step="1"
                    value={editingPlayer.number}
                    onChange={(e) => setEditingPlayer({ ...editingPlayer, number: e.target.value })}
                    className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-bold text-center text-accvGreen"
                  />
                </div>
              </div>

              <div className="flex justify-end space-x-2 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsPlayerModalOpen(false)}
                  className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold rounded-xl transition-all"
                >
                  Annulla
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-accvGreen hover:bg-accvGreenDark text-white text-xs font-bold rounded-xl shadow-sm transition-all"
                >
                  Salva Calciatore
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL: TEAM CREATE / EDIT */}
      {isTeamModalOpen && editingTeam && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="clean-card w-full max-w-md max-h-[90vh] overflow-y-auto p-6 space-y-4 shadow-xl animate-scale-up">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="font-bold text-sm text-slate-900">
                {editingTeam.name ? `Modifica Squadra: ${editingTeam.name}` : "Aggiungi Nuova Squadra Campionato"}
              </h3>
              <button onClick={() => setIsTeamModalOpen(false)} className="text-slate-400 hover:text-slate-600 font-bold">✕</button>
            </div>

            <form onSubmit={handleSaveTeam} className="space-y-3">
              <div>
                <label className="text-[11px] font-bold text-slate-600 uppercase">Codice</label>
                <input
                  type="text"
                  required
                  readOnly={Boolean(editingTeam.isEditing)}
                  value={editingTeam.key}
                  onChange={(e) => setEditingTeam({ ...editingTeam, key: e.target.value.toLowerCase().replace(/\s+/g, "_") })}
                  className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-mono font-bold"
                />
              </div>

              <div>
                <label className="text-[11px] font-bold text-slate-600 uppercase">Nome</label>
                <input
                  type="text"
                  required
                  value={editingTeam.name}
                  onChange={(e) => setEditingTeam({ ...editingTeam, name: e.target.value })}
                  className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-bold"
                />
              </div>

              <div>
                <label className="text-[11px] font-bold text-slate-600 uppercase">Nome Esteso</label>
                <input
                  type="text"
                  value={editingTeam.extended_name}
                  onChange={(e) => setEditingTeam({ ...editingTeam, extended_name: e.target.value })}
                  className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-medium"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-[11px] font-bold text-slate-600 uppercase">Sigla</label>
                  <input
                    type="text"
                    required
                    maxLength={4}
                    value={editingTeam.short_name}
                    onChange={(e) => setEditingTeam({ ...editingTeam, short_name: e.target.value.toUpperCase() })}
                    className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-bold text-center"
                  />
                </div>
              </div>

              <div>
                <label className="text-[11px] font-bold text-slate-600 uppercase">Colori rappresentativi</label>
                <div className="grid grid-cols-3 gap-3 mt-1">
                  {[
                    { field: "primary_color", label: "Primario" },
                    { field: "secondary_color", label: "Secondario" },
                    { field: "tertiary_color", label: "Terziario" }
                  ].map(({ field, label }) => (
                    <label key={field} className="flex items-center gap-2 px-2 py-1.5 rounded-lg border border-slate-200 bg-slate-50">
                      <input
                        type="color"
                        value={`#${editingTeam[field].map((x) => x.toString(16).padStart(2, "0")).join("")}`}
                        onChange={(e) => {
                          const hex = e.target.value;
                          const rgb = [1, 3, 5].map((offset) => parseInt(hex.slice(offset, offset + 2), 16));
                          setEditingTeam({ ...editingTeam, [field]: rgb });
                        }}
                        className="w-8 h-8 p-0 rounded border border-slate-300 cursor-pointer"
                      />
                      <span className="text-[10px] text-slate-600 font-semibold">{label}</span>
                    </label>
                  ))}
                </div>
              </div>

              {editingTeam.key !== "accv" && (
                <div>
                  <div className="flex items-center justify-between">
                    <label className="text-[11px] font-bold text-slate-600 uppercase">Grado di rivalità</label>
                    <span className="text-xs font-bold text-accvGreenDark">{editingTeam.rivalry} / 5</span>
                  </div>
                  <input
                    type="range"
                    min="1"
                    max="5"
                    step="1"
                    value={editingTeam.rivalry}
                    onChange={(e) => setEditingTeam({ ...editingTeam, rivalry: Number(e.target.value) })}
                    className="w-full mt-2 accent-accvGreen"
                  />
                  <div className="flex justify-between text-[10px] text-slate-500"><span>Bassa</span><span>Alta</span></div>
                </div>
              )}

              <div>
                <label className="text-[11px] font-bold text-slate-600 uppercase">Note (facoltative)</label>
                <textarea
                  rows="2"
                  value={editingTeam.notes}
                  onChange={(e) => setEditingTeam({ ...editingTeam, notes: e.target.value })}
                  className="clean-input w-full mt-1 px-3 py-2 text-xs rounded-lg font-medium resize-y"
                />
              </div>

              <div className="flex justify-end space-x-2 pt-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsTeamModalOpen(false)}
                  className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold rounded-xl transition-all"
                >
                  Annulla
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-accvGreen hover:bg-accvGreenDark text-white text-xs font-bold rounded-xl shadow-sm transition-all"
                >
                  Salva Squadra
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

// --- RIGHT COLUMN: Sticky Preview & Download Panel ---
function PreviewPanel({
  previewUri,
  rendering,
  selectedFormat,
  onSelectFormat,
  activeTab,
  showSafeZone,
  onToggleSafeZone,
  onDownload,
  onDownloadAll,
  onCopy,
  onShare
}) {
  const formats = [
    { id: "9:16", label: "Story (9:16)" },
    { id: "4:3", label: "Post (4:3)" },
    { id: "1:1", label: "Post (1:1)" },
    { id: "4:5", label: "Portrait (4:5)" },
    { id: "16:9", label: "Wide (16:9)" }
  ];

  return (
    <div className="clean-card p-5 space-y-4 shadow-md">
      {/* Header with Format Selector */}
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <span className="text-xs uppercase tracking-wider font-bold text-accvGreenDark flex items-center space-x-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-accvGreen animate-pulse"></span>
            <span>Live Studio Preview</span>
          </span>

          {/* Safe Zone Toggle (Only for vertical 9:16 formats) */}
          {selectedFormat === "9:16" && (
            <button
              onClick={onToggleSafeZone}
              className={`text-[11px] px-2.5 py-1 rounded-lg font-bold transition-all border ${
                showSafeZone
                  ? "bg-red-50 text-red-600 border-red-200"
                  : "bg-slate-50 text-slate-600 border-slate-200 hover:border-slate-300"
              }`}
            >
              {showSafeZone ? "✓ Safe Zone Attiva" : "Safe Zone IG"}
            </button>
          )}
        </div>

        {/* Format Selector Pills */}
        <div className="grid grid-cols-5 gap-1 bg-slate-100 p-1 rounded-xl border border-slate-200">
          {formats.map((f) => (
            <button
              key={f.id}
              onClick={() => onSelectFormat(f.id)}
              className={`py-1.5 text-[11px] font-bold rounded-lg transition-all text-center ${
                selectedFormat === f.id
                  ? "bg-white text-accvGreenDark font-extrabold shadow-sm"
                  : "text-slate-500 hover:text-slate-800"
              }`}
            >
              {f.label.split(" ")[0]}
            </button>
          ))}
        </div>
      </div>

      {/* Canvas / Render Display Container */}
      <div className="relative rounded-2xl overflow-hidden bg-slate-900 border border-slate-200 flex items-center justify-center min-h-[380px] max-h-[520px] shadow-inner">
        {rendering && (
          <div className="absolute inset-0 z-20 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center space-x-2">
            <div className="w-6 h-6 border-2 border-accvGreen border-t-transparent rounded-full animate-spin"></div>
            <span className="text-xs font-bold text-white tracking-wider">Rendering in memoria...</span>
          </div>
        )}

        {previewUri ? (
          <div className="relative w-full h-full flex items-center justify-center p-2">
            <img
              src={previewUri}
              alt="Social Media Graphic Preview"
              className="max-h-[500px] w-auto object-contain rounded-lg shadow-2xl transition-all"
            />

            {/* Instagram Safe Zone Guide */}
            {showSafeZone && selectedFormat === "9:16" && (
              <div className="safe-zone-overlay">
                <div className="safe-zone-top">ZONA INTERFACCIA STORIE (AVATAR / NOME)</div>
                <div className="safe-zone-bottom">ZONA INTERATTIVA (RISPONDI AL MESSAGGIO)</div>
              </div>
            )}
          </div>
        ) : (
          <div className="text-center text-slate-400 p-6">
            <Icon name="badge" className="w-10 h-10 mx-auto text-slate-600 mb-2" />
            <p className="text-xs">In attesa dei dati grafici...</p>
          </div>
        )}
      </div>

      {/* Action Buttons: Green Primary, Gold Secondary */}
      <div className="space-y-2 pt-1">
        <button
          onClick={onDownload}
          className="w-full py-3 px-4 rounded-xl bg-accvGreen hover:bg-accvGreenDark text-white font-extrabold text-sm tracking-wide shadow-sm hover:shadow-green-glow transition-all transform active:scale-95 flex items-center justify-center space-x-2"
        >
          <Icon name="download" className="w-5 h-5 text-white" />
          <span>SCARICA GRAFICA ({selectedFormat})</span>
        </button>

        <div className="grid grid-cols-3 gap-2">
          <button
            onClick={onDownloadAll}
            title="Scarica tutti i formati insieme in uno ZIP"
            className="py-2 px-3 rounded-xl bg-amber-50 hover:bg-amber-100 text-amber-800 text-xs font-bold border border-amber-200 flex items-center justify-center space-x-1.5 transition-all shadow-sm"
          >
            <Icon name="zip" className="w-4 h-4 text-amber-600" />
            <span>Tutti (ZIP)</span>
          </button>

          <button
            onClick={onCopy}
            title="Copia l'immagine negli appunti per incollarla subito"
            className="py-2 px-3 rounded-xl bg-slate-50 hover:bg-slate-100 text-slate-700 text-xs font-bold border border-slate-200 flex items-center justify-center space-x-1.5 transition-all shadow-sm"
          >
            <Icon name="copy" className="w-4 h-4 text-accvGreen" />
            <span>Copia</span>
          </button>

          <button
            onClick={onShare}
            title="Condividi direttamente su Instagram o WhatsApp da smartphone"
            className="py-2 px-3 rounded-xl bg-emerald-50 hover:bg-emerald-100 text-emerald-800 text-xs font-bold border border-emerald-200 flex items-center justify-center space-x-1.5 transition-all shadow-sm"
          >
            <Icon name="share" className="w-4 h-4 text-emerald-600" />
            <span>Condividi</span>
          </button>
        </div>
      </div>
    </div>
  );
}

// Render React App into DOM root
const rootEl = document.getElementById("root");
if (rootEl) {
  const root = ReactDOM.createRoot(rootEl);
  root.render(<App />);
}
