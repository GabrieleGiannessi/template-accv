import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';

type SectionId = 'result' | 'next' | 'mvp' | 'figurina' | 'roster';
type Team = { key: string; id?: number; name: string; short_name?: string; logo_url?: string; logo_filename?: string; primary_color?: number[] };
type PreviewResponse = { status: string; preview?: string; width?: number; height?: number; bg_path?: string };

@Component({
  selector: 'accv-root',
  standalone: true,
  imports: [CommonModule, FormsModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatExpansionModule, MatProgressSpinnerModule, MatSlideToggleModule, MatTooltipModule,
    MatButtonToggleModule, MatCardModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  readonly teamSides: ('home' | 'away')[] = ['home', 'away'];
  readonly cardTypes = [{ id: 'yellow', label: 'Gialli' }, { id: 'red', label: 'Rossi' }];
  readonly sections: { id: SectionId; title: string; icon: string; description: string }[] = [
    { id: 'result', title: 'Risultato', icon: 'emoji_events', description: 'Prepara e scarica la grafica del risultato.' },
    { id: 'next', title: 'Prossima Partita', icon: 'event', description: 'Crea la locandina della prossima sfida.' },
    { id: 'mvp', title: 'Migliore in Campo', icon: 'star', description: 'Celebra il giocatore della partita.' },
    { id: 'figurina', title: 'Figurine Panini', icon: 'style', description: 'Crea una card personalizzata del calciatore.' },
    { id: 'roster', title: 'Gestione', icon: 'settings', description: 'Gestisci rosa, squadre, stagioni e competizioni.' }
  ];
  readonly formats = [
    { id: '9:16', label: 'Story / Reels', size: '1080 × 1920' },
    { id: '4:3', label: 'Post orizzontale', size: '1440 × 1080' },
    { id: '16:9', label: 'Widescreen', size: '1920 × 1080' },
    { id: '1:1', label: 'Post quadrato', size: '1080 × 1080' },
    { id: '4:5', label: 'Post verticale', size: '1080 × 1350' }
  ];
  active: SectionId = 'result';
  managementTab: 'players' | 'teams' | 'seasons' | 'competitions' = 'players';
  editingRecord: any = null;
  savingRecord = false;
  loading = true;
  config: any = null;
  preview = '';
  previewSize = '';
  rendering = false;
  selectedFormat = '9:16';
  showSafeZone = false;
  toast = '';
  error = '';
  private timer?: ReturnType<typeof setTimeout>;
  private requestNumber = 0;
  private toastTimer?: ReturnType<typeof setTimeout>;

  result: any = {
    format: '9:16', tournament: 'CAMPIONATO CALCETTO A5', location: 'Centro Sportivo ACCV - Campo A',
    home_team: { name: 'A.C.C.V.', short_name: 'ACCV', primary_color: [16, 185, 129] },
    away_team: { name: 'Avversari', short_name: 'OSP' }, home_score: 0, away_score: 0,
    home_scorers: [], away_scorers: [], home_yellow_cards: [], away_yellow_cards: [], home_red_cards: [], away_red_cards: [],
    emotion: 'felicita', bg_path: null, bg_zoom: 1, bg_x: 0.5, bg_y: 0.5, graphic_variant: 'balanced',
    include_competition_info: true, mix_team_colors: false, remove_contrast: false, contrast_factor: 1
  };
  next: any = { format: '9:16', tournament: 'CAMPIONATO CALCETTO A5', location: 'Centro Sportivo ACCV - Campo A',
    field_name: 'Campo A', home_team: { name: 'A.C.C.V.', short_name: 'ACCV' }, away_team: { name: 'Avversari', short_name: 'OSP' },
    emotion: 'normale', bg_path: null, contrast_factor: 1, remove_contrast: false };
  mvp: any = { format: '4:5', player_name: '', jersey_number: '10', position: 'Pivot / Attaccante', goals: 3,
    assists: 2, saves: 0, rating: '9.5', match_opponent: '', emotion: 'felicita', bg_path: null, contrast_factor: 1, remove_contrast: false };
  figurina: any = { player_key: '', player_name: '', role: '', jersey_number: '', with_banner: true, format: 'story' };

  constructor(private readonly http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<any>('/api/config').subscribe({
      next: (data) => {
        this.config = data;
        const accv = this.teams.find((team: Team) => team.key === 'accv' || team.name.toUpperCase().includes('ACCV'));
        if (accv) this.result.home_team = this.teamForGraphic(accv);
        const example = data.example_match;
        if (example?.home_team) {
          this.result.tournament = example.tournament || this.result.tournament;
          this.result.location = example.location || this.result.location;
          this.result.away_team = example.away_team || this.result.away_team;
        }
        this.loading = false;
        this.schedulePreview();
      },
      error: () => { this.loading = false; this.error = 'Non riesco a collegarmi al backend Spring Boot. Avvialo su localhost:8080.'; }
    });
  }

  ngOnDestroy(): void { clearTimeout(this.timer); clearTimeout(this.toastTimer); }
  get current(): any { return this.active === 'next' ? this.next : this.active === 'mvp' ? this.mvp : this.active === 'figurina' ? this.figurina : this.result; }
  get teams(): Team[] { return this.config?.teams || []; }
  get players(): any[] { return this.config?.players || []; }
  get backgrounds(): any[] { return this.config?.backgrounds || []; }
  get emotions(): string[] { return this.config?.emotions || ['normale', 'felicita', 'tristezza', 'polemica']; }
  get managementRecords(): any[] {
    if (!this.config) return [];
    return this.managementTab === 'players' ? this.players : this.managementTab === 'teams' ? this.teams :
      this.managementTab === 'seasons' ? this.config.seasons || [] : this.config.competitions || [];
  }
  get competitionSeasonKeys(): string[] { return (this.editingRecord?.seasons || []).map((item: any) => item.season_key); }
  get competitionSeasons(): any[] {
    const selected = this.config?.competitions?.find((c: any) => c.key === this.result.competition_key) || this.config?.competitions?.[0];
    return (selected?.seasons || []).map((s: any) => ({ ...s, description: this.config?.seasons?.find((v: any) => v.key === s.season_key)?.description || s.season_key }));
  }
  get title(): string { return this.sections.find((s) => s.id === this.active)?.title || ''; }
  get activeIcon(): string { return this.sections.find((s) => s.id === this.active)?.icon || 'settings'; }
  get selectedFormatSize(): string { return this.formats.find((f) => f.id === this.selectedFormat)?.size || 'Anteprima'; }
  get description(): string { return this.sections.find((s) => s.id === this.active)?.description || ''; }
  get warnings(): string[] {
    const list: string[] = [];
    if (!this.result.away_team?.name) list.push('Seleziona la squadra avversaria.');
    for (const side of ['home', 'away']) {
      const total = (this.result[`${side}_scorers`] || []).reduce((sum: number, item: any) => sum + Number(item.goals || 0), 0);
      if (total !== Number(this.result[`${side}_score`])) list.push(`I marcatori di ${this.result[`${side}_team`].name} non coincidono con il punteggio.`);
    }
    return list;
  }

  setSection(section: SectionId): void {
    this.active = section;
    if (section === 'figurina') this.selectedFormat = '9:16';
    this.schedulePreview();
  }
  schedulePreview(): void {
    if (this.loading || this.active === 'roster') return;
    clearTimeout(this.timer);
    this.timer = setTimeout(() => void this.renderPreview(), 300);
  }
  private async renderPreview(): Promise<void> {
    const request = ++this.requestNumber;
    const endpoint: Record<string, string> = { result: 'result', next: 'next', mvp: 'mvp', figurina: 'figurina' };
    const payload = this.payload();
    this.rendering = true;
    try {
      const response = await firstValueFrom(this.http.post<PreviewResponse>(`/api/preview/${endpoint[this.active]}`, payload));
      if (request !== this.requestNumber) return;
      if (response.status === 'ok' && response.preview) {
        this.preview = response.preview;
        this.previewSize = response.width && response.height ? `${response.width} × ${response.height}` : '';
        this.error = '';
        if (response.bg_path && this.active === 'result' && !this.result.bg_path) this.result.bg_path = response.bg_path;
      }
    } catch { if (request === this.requestNumber) this.error = 'Anteprima non disponibile. Verifica i dati inseriti e il backend.'; }
    finally { if (request === this.requestNumber) this.rendering = false; }
  }
  private payload(): any {
    const format = this.active === 'figurina' ? (this.selectedFormat === '9:16' ? 'story' : this.selectedFormat === '4:5' ? 'post' : 'card') : this.selectedFormat;
    return { ...this.current, format, ...(this.active === 'figurina' ? {} : {}) };
  }
  chooseFormat(format: string): void { this.selectedFormat = format; this.schedulePreview(); }
  changeTeam(side: 'home' | 'away', key: string): void {
    const team = this.teams.find((item) => item.key === key);
    if (!team) return;
    const target = this.active === 'next' ? this.next : this.result;
    target[`${side}_team`] = this.teamForGraphic(team);
    if (this.active === 'mvp' && side === 'away') this.mvp.match_opponent = team.name;
    this.schedulePreview();
  }
  private teamForGraphic(team: Team): any { return { ...team, logo_path: team.logo_filename ? `assets/logos/${team.logo_filename}` : null }; }
  swapTeams(): void {
    const data = this.active === 'next' ? this.next : this.result;
    [data.home_team, data.away_team] = [data.away_team, data.home_team];
    if (this.active === 'result') {
      [data.home_score, data.away_score] = [data.away_score, data.home_score];
      for (const key of ['scorers', 'yellow_cards', 'red_cards']) [data[`home_${key}`], data[`away_${key}`]] = [data[`away_${key}`], data[`home_${key}`]];
    }
    this.schedulePreview();
  }
  changeScore(side: 'home' | 'away', delta: number): void { const key = `${side}_score`; this.result[key] = Math.max(0, Number(this.result[key] || 0) + delta); this.schedulePreview(); }
  addScorer(side: 'home' | 'away', name: string): void {
    if (!name) return;
    const list = this.result[`${side}_scorers`];
    const existing = list.find((v: any) => v.name === name);
    existing ? existing.goals++ : list.push({ name, goals: 1 });
    this.result[`${side}_score`] = Number(this.result[`${side}_score`] || 0) + 1;
    this.schedulePreview();
  }
  removeScorer(side: 'home' | 'away', index: number): void {
    const list = this.result[`${side}_scorers`];
    const scorer = list[index];
    if (scorer.goals > 1) scorer.goals--; else list.splice(index, 1);
    this.result[`${side}_score`] = Math.max(0, Number(this.result[`${side}_score`] || 0) - 1);
    this.schedulePreview();
  }
  addCard(side: 'home' | 'away', color: string, name: string): void {
    if (!name) return;
    const key = `${side}_${color}_cards`;
    this.result[key] = [...(this.result[key] || []), name];
    this.schedulePreview();
  }
  removeCard(side: 'home' | 'away', color: string, index: number): void {
    const key = `${side}_${color}_cards`;
    this.result[key] = (this.result[key] || []).filter((_: string, i: number) => i !== index);
    this.schedulePreview();
  }
  choosePlayer(player: any): void {
    this.figurina = { ...this.figurina, player_key: player.key, player_name: player.name, role: player.role, jersey_number: player.number || '' };
    this.schedulePreview();
  }
  chooseFigurinaPlayer(key: string): void { const player = this.players.find((item) => item.key === key); if (player) this.choosePlayer(player); }
  setFigurinaFormat(format: string): void {
    this.figurina.format = format;
    this.selectedFormat = format === 'story' ? '9:16' : format === 'post' ? '4:5' : '1:1';
    this.schedulePreview();
  }
  chooseBackground(path: string): void { this.current.bg_path = path || null; this.schedulePreview(); }
  chooseEmotion(emotion: string): void { this.current.emotion = emotion; this.current.bg_path = null; this.schedulePreview(); }
  setManagementTab(tab: 'players' | 'teams' | 'seasons' | 'competitions'): void { this.managementTab = tab; this.editingRecord = null; }
  newRecord(): void {
    const common = { id: null, key: '', notes: '' };
    const initial: Record<string, any> = {
      players: { ...common, name: '', display_name: '', role: 'Giocatore', number: '', team_id: this.teams.find((t) => t.key === 'accv')?.id || this.teams[0]?.id || '' },
      teams: { ...common, name: '', extended_name: '', short_name: '', primaryColorHex: '#10b981', secondaryColorHex: '#ef4444', tertiaryColorHex: '#3b82f6', rivalry: 3, logo_filename: '' },
      seasons: { ...common, description: '' },
      competitions: { ...common, description: '', logo_filename: '', seasons: [] }
    };
    this.editingRecord = initial[this.managementTab];
  }
  editRecord(item: any): void {
    this.editingRecord = structuredClone(item);
    if (this.managementTab === 'teams') {
      this.editingRecord.primaryColorHex = this.rgbToHex(item.primary_color);
      this.editingRecord.secondaryColorHex = this.rgbToHex(item.secondary_color);
      this.editingRecord.tertiaryColorHex = this.rgbToHex(item.tertiary_color);
    }
  }
  updateCompetitionSeasons(keys: string[]): void {
    if (!this.editingRecord) return;
    const previous = new Map<string, any>((this.editingRecord.seasons || []).map((item: any) => [item.season_key, item]));
    this.editingRecord.seasons = keys.map((key) => previous.get(key) || { season_key: key, team_keys: this.teams.map((team) => team.key) });
  }
  seasonDescription(key: string): string { return this.config?.seasons?.find((season: any) => season.key === key)?.description || key; }
  cancelEdit(): void { this.editingRecord = null; }
  private rgbToHex(rgb?: number[]): string {
    if (!rgb || rgb.length < 3) return '#3b6154';
    return `#${rgb.slice(0, 3).map((v) => Math.max(0, Math.min(255, Number(v))).toString(16).padStart(2, '0')).join('')}`;
  }
  private hexToRgb(value: string): number[] {
    const clean = value.replace('#', '');
    return [0, 2, 4].map((offset) => parseInt(clean.slice(offset, offset + 2), 16));
  }
  async saveRecord(): Promise<void> {
    if (!this.editingRecord || this.savingRecord) return;
    const record = this.editingRecord;
    const endpoint = `/api/${this.managementTab}${record.id ? `/${record.id}` : ''}`;
    let body: any = { ...record };
    if (this.managementTab === 'players') body = { key: record.key, name: record.name, display_name: record.display_name || '', role: record.role || 'Giocatore', number: record.number === '' ? '' : String(record.number), team_id: Number(record.team_id), photo_filename: record.photo_filename || null };
    if (this.managementTab === 'teams') body = { key: record.key, name: record.name, extended_name: record.extended_name || '', short_name: record.short_name || record.name.slice(0, 3).toUpperCase(), notes: record.notes || '', rivalry: Number(record.rivalry) || 3, logo_filename: record.logo_filename || `${record.key}.png`, primary_color: this.hexToRgb(record.primaryColorHex), secondary_color: this.hexToRgb(record.secondaryColorHex), tertiary_color: this.hexToRgb(record.tertiaryColorHex) };
    if (this.managementTab === 'seasons') body = { key: record.key, description: record.description, notes: record.notes || '' };
    if (this.managementTab === 'competitions') body = { key: record.key, description: record.description, notes: record.notes || '', logo_filename: record.logo_filename || '', seasons: record.seasons || [] };
    this.savingRecord = true;
    try {
      await firstValueFrom(this.http.request(record.id ? 'PUT' : 'POST', endpoint, { body }));
      await this.reloadConfig();
      this.editingRecord = null;
      this.notify('Modifiche salvate');
    } catch (error: any) {
      const message = error?.error?.message || error?.error?.error || 'Controlla i campi e riprova.';
      this.notify(`Salvataggio non riuscito: ${message}`);
    } finally { this.savingRecord = false; }
  }
  async deleteRecord(item: any): Promise<void> {
    const label = item.name || item.description;
    if (!item.id || !confirm(`Eliminare “${label}”?`)) return;
    try {
      await firstValueFrom(this.http.delete(`/api/${this.managementTab}/${item.id}`));
      await this.reloadConfig();
      this.notify('Elemento eliminato');
    } catch (error: any) { this.notify(error?.error?.message || 'Eliminazione non riuscita: elemento in uso o non disponibile.'); }
  }
  private async reloadConfig(): Promise<void> { this.config = await firstValueFrom(this.http.get<any>('/api/config')); }
  async uploadManagementAsset(event: Event, type: 'player' | 'logo'): Promise<void> {
    const file = (event.target as HTMLInputElement).files?.[0];
    const key = this.editingRecord?.key;
    if (!file || !key) { this.notify('Inserisci prima il codice dell’elemento'); return; }
    const extension = file.name.includes('.') ? file.name.slice(file.name.lastIndexOf('.')).toLowerCase() : '.png';
    const filename = type === 'player' ? `${key}${extension}` : `${this.managementTab === 'competitions' ? 'competition_' : ''}${key}${extension}`;
    try {
      const data = await new Promise<string>((resolve, reject) => { const reader = new FileReader(); reader.onload = () => resolve(String(reader.result)); reader.onerror = () => reject(reader.error); reader.readAsDataURL(file); });
      await firstValueFrom(this.http.post('/api/upload', { upload_type: type, filename, data }));
      if (type === 'player') this.editingRecord.photo_filename = filename;
      else this.editingRecord.logo_filename = filename;
      this.notify('Immagine caricata; salva il record per aggiornare il catalogo');
    } catch { this.notify('Caricamento immagine non riuscito'); }
  }
  async uploadBackground(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    const data = await new Promise<string>((resolve, reject) => { const reader = new FileReader(); reader.onload = () => resolve(String(reader.result)); reader.onerror = () => reject(reader.error); reader.readAsDataURL(file); });
    this.http.post<any>('/api/upload', { upload_type: 'background', filename: file.name, data }).subscribe({
      next: (result) => { this.chooseBackground(result.path); this.notify('Sfondo caricato'); },
      error: () => this.notify('Caricamento non riuscito')
    });
  }
  async download(): Promise<void> { await this.downloadBlob('/api/download', { ...this.payload(), type: this.active }, `accv_${this.active}_${this.selectedFormat.replace(':', '_')}.png`); }
  async downloadAll(): Promise<void> {
    await this.downloadBlob('/api/download-all', { ...this.payload(), type: this.active }, `accv_${this.active}_pack.zip`);
  }
  private async downloadBlob(url: string, body: any, filename: string): Promise<void> {
    try {
      const blob = await firstValueFrom(this.http.post(url, body, { responseType: 'blob' }));
      const objectUrl = URL.createObjectURL(blob);
      const anchor = document.createElement('a'); anchor.href = objectUrl; anchor.download = filename; anchor.click(); URL.revokeObjectURL(objectUrl);
      this.notify('Download completato');
    } catch { this.notify('Download non riuscito'); }
  }
  async copyPreview(): Promise<void> {
    try { const blob = await (await fetch(this.preview)).blob(); await navigator.clipboard.write([new ClipboardItem({ 'image/png': blob })]); this.notify('Grafica copiata'); }
    catch { this.notify('Copia non supportata dal browser'); }
  }
  async sharePreview(): Promise<void> {
    if (!this.preview || !navigator.share) { this.notify('Condivisione non supportata dal browser'); return; }
    try {
      const blob = await (await fetch(this.preview)).blob();
      const file = new File([blob], `accv_${this.active}.png`, { type: 'image/png' });
      await navigator.share({ files: [file], title: 'ACCV Designer Lab', text: 'Grafica ufficiale A.C.C.V.' });
    } catch { this.notify('Condivisione non completata'); }
  }
  notify(message: string): void { this.toast = message; clearTimeout(this.toastTimer); this.toastTimer = setTimeout(() => this.toast = '', 3000); }
}
