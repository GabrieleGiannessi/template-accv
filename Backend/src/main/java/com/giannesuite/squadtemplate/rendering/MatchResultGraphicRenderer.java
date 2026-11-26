package com.giannesuite.squadtemplate.rendering;

import com.giannesuite.squadtemplate.utils.GraphicBackgroundLoader;
import com.giannesuite.squadtemplate.utils.GraphicImageSupport;
import com.giannesuite.squadtemplate.service.dto.MatchResultGraphicRequest;
import com.giannesuite.squadtemplate.service.dto.ScorerGraphicRequest;
import com.giannesuite.squadtemplate.service.dto.TeamGraphicRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Port Java2D del layout photo-overlay della grafica Risultato. */
@Component
public class MatchResultGraphicRenderer {
    private final Path assetsPath;

    public MatchResultGraphicRenderer(@Value("${graphics.assets-path:../assets}") String assetsPath) {
        this.assetsPath = Path.of(assetsPath).toAbsolutePath().normalize();
    }

    public BufferedImage render(MatchResultGraphicRequest request) {
        String format = request.format() == null ? "9:16" : request.format().toLowerCase(Locale.ROOT);
        int[] dimensions = dimensions(format);
        BufferedImage image = GraphicBackgroundLoader.load(assetsPath, dimensions[0], dimensions[1], request.bg_path(), request.emotion(),
                request.contrast_factor() == null ? 1 : request.contrast_factor(), Boolean.TRUE.equals(request.remove_contrast()),
                0, request.bg_zoom() == null ? 1 : request.bg_zoom(), request.bg_x() == null ? 0.5 : request.bg_x(),
                request.bg_y() == null ? 0.5 : request.bg_y());
        GraphicCanvas canvas = new GraphicCanvas(image, assetsPath);
        try { draw(canvas, request, format); } finally { canvas.close(); }
        return image;
    }

    public String resolveBackgroundPath(MatchResultGraphicRequest request) {
        Path path = GraphicBackgroundLoader.resolveBackground(assetsPath, request.bg_path(), request.emotion());
        return path == null ? null : path.toString();
    }

    private void draw(GraphicCanvas c, MatchResultGraphicRequest d, String format) {
        int w = c.width(), h = c.height(), center = w / 2;
        boolean vertical = h > w;
        desaturateAndTint(c.image());
        bottomVignette(c.image());
        boolean hasDetails = !list(d.home_scorers()).isEmpty() || !list(d.away_scorers()).isEmpty()
                || !list(d.home_yellow_cards()).isEmpty() || !list(d.away_yellow_cards()).isEmpty()
                || !list(d.home_red_cards()).isEmpty() || !list(d.away_red_cards()).isEmpty();
        String variant = text(d.graphic_variant(), "balanced");

        double logoScale = variant.equals("score") ? 0.27 : variant.equals("scorers") ? 0.18 : 0.22;
        int logoSize = (int) (Math.min(w, h) * logoScale);
        TeamGraphicRequest home = d.home_team(), away = d.away_team();
        String homeName = home == null ? "A.C.C.V." : home.safeName("A.C.C.V.");
        String awayName = away == null ? "Avversario" : away.safeName("Avversario");
        BufferedImage homeLogo = GraphicImageSupport.logo(assetsPath, home == null ? null : home.logo_path(), homeName,
                home == null ? "ACCV" : home.safeShortName("ACCV"), logoSize,
                home == null ? new int[]{0, 229, 255} : home.primary_color());
        BufferedImage awayLogo = GraphicImageSupport.logo(assetsPath, away == null ? null : away.logo_path(), awayName,
                away == null ? "OPP" : away.safeShortName("OPP"), logoSize,
                away == null ? new int[]{255, 99, 132} : away.primary_color());
        int spacing = (int) (w * 0.14);
        int homeX = center - spacing - logoSize, awayX = center + spacing;
        int logoY = (int) (h * (variant.equals("scorers") && hasDetails && vertical ? 0.45
                : hasDetails && vertical ? 0.50 : hasDetails ? 0.38 : 0.68));
        c.graphics().drawImage(homeLogo, homeX, logoY, null);
        c.graphics().drawImage(awayLogo, awayX, logoY, null);

        if (Boolean.TRUE.equals(d.mix_team_colors())) drawColorFrame(c, home, away);
        c.graphics().setColor(new Color(12, 12, 12));
        c.graphics().setStroke(new BasicStroke(vertical ? 14 : 10));
        c.graphics().drawRect(0, 0, w - 1, h - 1);

        String competition = text(d.competition_description(), "").trim().toUpperCase(Locale.ROOT);
        String season = text(d.season_description(), "").trim().toUpperCase(Locale.ROOT);
        if (Boolean.TRUE.equals(d.include_competition_info()) && (!competition.isEmpty() || !season.isEmpty())) {
            drawCompetitionHeader(c, competition, season, d.competition_logo_path(), vertical);
        }

        drawTeamName(c, homeName, homeX, logoY, logoSize, vertical);
        drawTeamName(c, awayName, awayX, logoY, logoSize, vertical);

        int scoreSize = variant.equals("score") ? (vertical ? 224 : 180)
                : variant.equals("scorers") ? (vertical ? 178 : 142) : (vertical ? 204 : 162);
        String score = integer(d.home_score()) + "-" + integer(d.away_score());
        Font scoreFont = c.font("HEADER", scoreSize);
        int scoreWidth = c.graphics().getFontMetrics(scoreFont).stringWidth(score);
        int scoreX = center - scoreWidth / 2;
        int scoreY = logoY + (logoSize - c.graphics().getFontMetrics(scoreFont).getHeight()) / 2 - 16;
        drawPillowText(c.graphics(), score, scoreFont, scoreX + 4, scoreY + 4, new Color(10, 10, 10, 240));
        for (int dx : new int[]{-3, -2, -1, 1, 2, 3}) for (int dy : new int[]{-3, -2, -1, 1, 2, 3})
            drawPillowText(c.graphics(), score, scoreFont, scoreX + dx, scoreY + dy, new Color(212, 175, 55));
        drawPillowText(c.graphics(), score, scoreFont, scoreX, scoreY, Color.WHITE);

        int subtitleY = logoY + logoSize + 35;
        int subtitleSize = vertical ? 58 : 46;
        Font subtitleFont = c.font("HEADER", subtitleSize);
        String subtitle = "MATCH RESULT";
        drawOutlinedCentered(c, subtitle, subtitleFont, center, subtitleY, new Color(212, 175, 55), Color.BLACK, 3);
        if (hasDetails) {
            int detailY = subtitleY + c.graphics().getFontMetrics(subtitleFont).getHeight() + 14;
            List<DetailRow> homeRows = rows(d.home_scorers(), d.home_yellow_cards(), d.home_red_cards());
            List<DetailRow> awayRows = rows(d.away_scorers(), d.away_yellow_cards(), d.away_red_cards());
            int maxRows = Math.max(1, Math.max(homeRows.size(), awayRows.size()));
            int base = variant.equals("scorers") ? (vertical ? 58 : 46) : (vertical ? 50 : 40);
            int available = Math.max(1, h - 20 - detailY);
            int lineHeight = Math.min(base + 24, Math.max(30, available / maxRows));
            int fontSize = Math.min(base, lineHeight - 10);
            drawRows(c, homeRows, (int) (w * 0.035), center - 18, detailY, lineHeight, fontSize, vertical);
            drawRows(c, awayRows, center + 18, (int) (w * 0.965), detailY, lineHeight, fontSize, vertical);
        }
    }

    private void drawCompetitionHeader(GraphicCanvas c, String competition, String season, String logoPath, boolean vertical) {
        int w = c.width(), h = c.height();
        int headerY = (int) (h * (vertical ? 0.14 : 0.10));
        int maxWidth = (int) (w * 0.36), fontSize = vertical ? 48 : 36;
        Font compFont = fitted(c, competition, "HEADER", maxWidth, fontSize, 14);
        Font seasonFont = fitted(c, season, "HEADER", maxWidth, fontSize, 14);
        int compWidth = textWidth(c.graphics(), competition, compFont);
        int seasonWidth = textWidth(c.graphics(), season, seasonFont);
        int gap = !competition.isEmpty() && !season.isEmpty() ? 28 : 0;
        int start = (w - compWidth - gap - seasonWidth) / 2;
        int baseline = headerY + Math.max(c.graphics().getFontMetrics(compFont).getAscent(), c.graphics().getFontMetrics(seasonFont).getAscent());
        if (!competition.isEmpty()) drawOutlinedText(c.graphics(), competition, compFont, start, baseline, Color.WHITE, new Color(10, 16, 18), 2);
        if (!season.isEmpty()) drawOutlinedText(c.graphics(), season, seasonFont, start + compWidth + gap, baseline,
                new Color(235, 190, 73), new Color(10, 16, 18), 2);
        Path logo = GraphicImageSupport.resolve(assetsPath, logoPath);
        if (logo != null) try {
            BufferedImage source = ImageIO.read(logo.toFile());
            int extent = vertical ? 98 : 74;
            double scale = Math.min((double) extent / source.getWidth(), (double) extent / source.getHeight());
            int lw = (int) (source.getWidth() * scale), lh = (int) (source.getHeight() * scale);
            c.graphics().drawImage(source, w - lw - (vertical ? 34 : 24), Math.max(12, headerY - (extent - fontSize) / 2), lw, lh, null);
        } catch (IOException ignored) { }
    }

    private void drawTeamName(GraphicCanvas c, String name, int logoX, int logoY, int logoSize, boolean vertical) {
        int maxWidth = logoSize + 40, initialSize = vertical ? 56 : 44;
        String upper = name.toUpperCase(Locale.ROOT);
        List<String> lines = new ArrayList<>();
        Font font = c.font("HEADER", initialSize);
        if ((textWidth(c.graphics(), upper, font) > maxWidth || name.length() > 14) && name.trim().contains(" ")) {
            String[] words = upper.trim().split("\\s+");
            int mid = words.length / 2;
            lines.add(String.join(" ", java.util.Arrays.copyOfRange(words, 0, mid)));
            lines.add(String.join(" ", java.util.Arrays.copyOfRange(words, mid, words.length)));
            font = fitted(c, String.join(" ", lines), "HEADER", maxWidth, initialSize, 18);
        } else font = fitted(c, upper, "HEADER", maxWidth, initialSize, 18);
        int lineHeight = c.graphics().getFontMetrics(font).getHeight();
        int y = logoY - linesHeight(lines.isEmpty() ? List.of(upper) : lines, lineHeight) - 12;
        for (String line : lines.isEmpty() ? List.of(upper) : lines) {
            int x = logoX + (logoSize - textWidth(c.graphics(), line, font)) / 2;
            drawPillowText(c.graphics(), line, font, x, y, Color.WHITE);
            y += lineHeight + 4;
        }
    }

    private void drawRows(GraphicCanvas c, List<DetailRow> rows, int x1, int x2, int y, int lineHeight, int fontSize, boolean vertical) {
        int centerX = (x1 + x2) / 2;
        for (DetailRow row : rows) {
            int iconCount = row.goals + row.ownGoals + row.cards.size();
            int target = vertical ? 42 : 32;
            int iconSize = Math.max(18, Math.min(target, Math.min(lineHeight - 8, (int) ((x2 - x1 - 24) * 0.58 / Math.max(1, iconCount)))));
            int cardSize = Math.max(16, (int) Math.round(iconSize * 0.88));
            int ballGap = Math.max(3, iconSize / 7), cardGap = Math.max(3, cardSize / 7);
            int ballsWidth = row.goals + row.ownGoals == 0 ? 0 : (row.goals + row.ownGoals) * iconSize + Math.max(0, row.goals + row.ownGoals - 1) * ballGap;
            int cardWidth = Math.max(1, (int) Math.round(cardSize * 0.68));
            int cardsWidth = row.cards.isEmpty() ? 0 : row.cards.size() * cardWidth + (row.cards.size() - 1) * cardGap;
            int iconsWidth = ballsWidth + cardsWidth + (row.goals > 0 && !row.cards.isEmpty() ? ballGap : 0);
            String label = row.label.toUpperCase(Locale.ROOT);
            int maxText = Math.max(20, x2 - x1 - iconsWidth - (iconCount > 0 ? 6 : 0) - 16);
            Font font = fitted(c, label, "HEADER", maxText, fontSize, Math.max(14, fontSize / 2));
            int textW = textWidth(c.graphics(), label, font);
            int total = textW + (iconCount > 0 ? 6 : 0) + iconsWidth;
            int start = centerX - total / 2;
            int textY = y + Math.max(0, (lineHeight - c.graphics().getFontMetrics(font).getHeight()) / 2);
            drawPillowText(c.graphics(), label, font, start, textY, Color.WHITE);
            int iconX = start + textW + (iconCount > 0 ? 6 : 0), iconY = y + (lineHeight - iconSize) / 2;
            for (int i = 0; i < row.goals; i++) { drawIcon(c.graphics(), "ball.png", iconX, iconY, iconSize, null); iconX += iconSize + (i < row.goals - 1 ? ballGap : 0); }
            for (int i = 0; i < row.ownGoals; i++) { drawIcon(c.graphics(), "ball.png", iconX, iconY, iconSize, new Color(205, 95, 95)); iconX += iconSize + (i < row.ownGoals - 1 ? ballGap : 0); }
            if (row.goals + row.ownGoals > 0 && !row.cards.isEmpty()) iconX += cardGap;
            for (int i = 0; i < row.cards.size(); i++) {
                String icon = row.cards.get(i) == 0 ? "yellow_card.png" : "red_card.png";
                drawIcon(c.graphics(), icon, iconX, y + (lineHeight - cardSize) / 2, cardSize, null);
                iconX += cardWidth + (i < row.cards.size() - 1 ? cardGap : 0);
            }
            y += lineHeight;
        }
    }

    private void drawIcon(Graphics2D g, String name, int x, int y, int size, Color tint) {
        Path path = assetsPath.resolve("icons").resolve(name);
        if (!java.nio.file.Files.isRegularFile(path)) return;
        try {
            BufferedImage source = ImageIO.read(path.toFile());
            int[] bounds = alphaBounds(source);
            if (bounds == null) return;
            int sw = bounds[2] - bounds[0], sh = bounds[3] - bounds[1];
            int width = Math.max(1, (int) Math.round((double) sw * size / sh));
            BufferedImage cropped = source.getSubimage(bounds[0], bounds[1], sw, sh);
            BufferedImage icon = new BufferedImage(width, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D ig = icon.createGraphics();
            ig.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            ig.drawImage(cropped, 0, 0, width, size, null);
            ig.dispose();
            if (tint != null) for (int iy = 0; iy < size; iy++) for (int ix = 0; ix < width; ix++) {
                int pixel = icon.getRGB(ix, iy), a = pixel >>> 24;
                int r = (int) (((pixel >>> 16) & 255) * 0.62 + tint.getRed() * 0.38);
                int gr = (int) (((pixel >>> 8) & 255) * 0.62 + tint.getGreen() * 0.38);
                int b = (int) ((pixel & 255) * 0.62 + tint.getBlue() * 0.38);
                icon.setRGB(ix, iy, (a << 24) | (r << 16) | (gr << 8) | b);
            }
            g.drawImage(icon, x, y, null);
        } catch (IOException ignored) { }
    }

    private List<DetailRow> rows(List<ScorerGraphicRequest> scorers, List<String> yellow, List<String> red) {
        List<DetailRow> rows = new ArrayList<>();
        for (ScorerGraphicRequest scorer : list(scorers)) {
            String name = text(scorer.name(), "").trim();
            if (name.isEmpty()) continue;
            String label = shortName(name);
            DetailRow row = find(rows, Boolean.TRUE.equals(scorer.own_goal()) ? "Autogol" : name, Boolean.TRUE.equals(scorer.own_goal()) ? "Autogol" : label);
            if (Boolean.TRUE.equals(scorer.own_goal())) row.ownGoals += Math.max(1, integer(scorer.goals()));
            else row.goals += Math.max(1, integer(scorer.goals()));
        }
        for (String name : list(yellow)) if (name != null && !name.isBlank()) find(rows, name, shortName(name)).cards.add(0);
        for (String name : list(red)) if (name != null && !name.isBlank()) find(rows, name, shortName(name)).cards.add(1);
        Map<String, Integer> counts = new HashMap<>();
        for (DetailRow row : rows) if (!row.fullName.equals("Autogol")) counts.merge(row.label.toLowerCase(Locale.ROOT), 1, Integer::sum);
        for (DetailRow row : rows) if (!row.fullName.equals("Autogol") && counts.getOrDefault(row.label.toLowerCase(Locale.ROOT), 0) > 1) {
            String[] parts = row.fullName.split("\\s+");
            if (parts.length > 1) row.label = parts[parts.length - 1] + " " + parts[0].substring(0, 1).toUpperCase(Locale.ROOT) + ".";
        }
        return rows;
    }

    private DetailRow find(List<DetailRow> rows, String name, String label) {
        String key = Normalizer.normalize(name.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFKC).replaceAll("\\s+", " ");
        for (DetailRow row : rows) if (row.key.equals(key)) return row;
        DetailRow row = new DetailRow(key, name, label);
        rows.add(row); return row;
    }

    private void drawColorFrame(GraphicCanvas c, TeamGraphicRequest home, TeamGraphicRequest away) {
        Color h = GraphicImageSupport.color(home == null ? null : home.primary_color(), new Color(16, 185, 129));
        Color a = GraphicImageSupport.color(away == null ? null : away.primary_color(), new Color(239, 68, 68));
        Graphics2D g = c.graphics();
        int band = Math.max(22, Math.min(54, Math.round(Math.min(c.width(), c.height()) * 0.03f)));
        java.util.Random random = new java.util.Random(731);
        for (int side = 0; side < 4; side++) {
            for (int i = 0; i < 3; i++) {
                Color color = side == 0 || side == 2 ? h : a;
                g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 155));
                g.setStroke(new BasicStroke(Math.max(8, band / 3), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int segments = 18;
                for (int j = 0; j < segments; j++) {
                    int p1 = j * (side % 2 == 0 ? c.width() : c.height()) / segments;
                    int p2 = (j + 1) * (side % 2 == 0 ? c.width() : c.height()) / segments;
                    int inset1 = band / 3 + random.nextInt(Math.max(1, band / 2));
                    int inset2 = band / 3 + random.nextInt(Math.max(1, band / 2));
                    if (side == 0) g.drawLine(p1, inset1, p2, inset2);
                    else if (side == 2) g.drawLine(p1, c.height() - inset1, p2, c.height() - inset2);
                    else if (side == 1) g.drawLine(inset1, p1, inset2, p2);
                    else g.drawLine(c.width() - inset1, p1, c.width() - inset2, p2);
                }
            }
        }
    }

    private void desaturateAndTint(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
            int p = image.getRGB(x, y), r = (p >>> 16) & 255, g = (p >>> 8) & 255, b = p & 255;
            int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            r = (int) (gray + (r - gray) * 0.55); g = (int) (gray + (g - gray) * 0.55); b = (int) (gray + (b - gray) * 0.55);
            int mean = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            r = clamp((int) (mean + (r - mean) * 1.1)); g = clamp((int) (mean + (g - mean) * 1.1)); b = clamp((int) (mean + (b - mean) * 1.1));
            r = clamp((int) (r * 0.85 + 12 * 0.15)); g = clamp((int) (g * 0.85 + 28 * 0.15)); b = clamp((int) (b * 0.85 + 22 * 0.15));
            image.setRGB(x, y, (255 << 24) | (r << 16) | (g << 8) | b);
        }
    }

    private void bottomVignette(BufferedImage image) {
        int start = (int) (image.getHeight() * 0.52);
        for (int y = start; y < image.getHeight(); y++) {
            double ratio = (double) (y - start) / (image.getHeight() - start);
            int alpha = (int) (240 * Math.pow(ratio, 1.3));
            int red = (6 * alpha + 127) / 255, green = (8 * alpha + 127) / 255, blue = (10 * alpha + 127) / 255;
            int inv = 255 - alpha;
            for (int x = 0; x < image.getWidth(); x++) {
                int p = image.getRGB(x, y);
                int r = ((p >>> 16 & 255) * inv + 6 * alpha + 127) / 255;
                int g = ((p >>> 8 & 255) * inv + 8 * alpha + 127) / 255;
                int b = ((p & 255) * inv + 10 * alpha + 127) / 255;
                image.setRGB(x, y, (255 << 24) | (r << 16) | (g << 8) | b);
            }
        }
    }

    private void drawPillowText(Graphics2D g, String value, Font font, int x, int top, Color color) {
        g.setFont(font); g.setColor(color);
        g.drawString(value, x, top + g.getFontMetrics(font).getAscent());
    }

    private void drawOutlinedCentered(GraphicCanvas c, String value, Font font, int centerX, int top, Color fill, Color outline, int width) {
        int x = centerX - textWidth(c.graphics(), value, font) / 2;
        drawOutlinedText(c.graphics(), value, font, x, top + c.graphics().getFontMetrics(font).getAscent(), fill, outline, width);
    }

    private void drawOutlinedText(Graphics2D g, String text, Font font, int x, int baseline, Color fill, Color outline, int width) {
        g.setFont(font);
        var shape = font.createGlyphVector(g.getFontRenderContext(), text).getOutline(x, baseline);
        g.setColor(outline); g.setStroke(new BasicStroke(width * 2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(shape); g.setColor(fill); g.fill(shape);
    }

    private Font fitted(GraphicCanvas c, String text, String key, int max, int initial, int min) {
        Font font = c.font(key, initial);
        for (int size = initial; size > min; size -= 2) { font = c.font(key, size); if (textWidth(c.graphics(), text, font) <= max) break; }
        return font;
    }

    private int textWidth(Graphics2D g, String text, Font font) { return text == null || text.isEmpty() ? 0 : g.getFontMetrics(font).stringWidth(text); }
    private int linesHeight(List<String> lines, int h) { return lines.size() * h + Math.max(0, lines.size() - 1) * 4; }
    private int[] dimensions(String f) { return switch (f) { case "9:16", "story" -> new int[]{1080,1920}; case "4:3" -> new int[]{1440,1080}; case "16:9" -> new int[]{1920,1080}; case "1:1", "post" -> new int[]{1080,1080}; case "4:5" -> new int[]{1080,1350}; default -> throw new IllegalArgumentException("Formato grafica non supportato: " + f); }; }
    private int integer(Integer value) { return value == null ? 0 : value; }
    private int clamp(int value) { return Math.max(0, Math.min(255, value)); }
    private <T> List<T> list(List<T> values) { return values == null ? List.of() : values; }
    private String text(String v, String fallback) { return v == null ? fallback : v; }
    private int[] alphaBounds(BufferedImage image) { int[] b={image.getWidth(),image.getHeight(),-1,-1}; for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if((image.getRGB(x,y)>>>24)!=0){b[0]=Math.min(b[0],x);b[1]=Math.min(b[1],y);b[2]=Math.max(b[2],x);b[3]=Math.max(b[3],y);} if(b[2]<b[0])return null; b[2]++;b[3]++;return b; }
    private String shortName(String name) { String[] p=name.trim().split("\\s+"); if(p.length==1)return name.trim(); if(p[p.length-1].length()==2&&p[p.length-1].endsWith("."))return p[0]; return p[p.length-1]; }

    private static final class DetailRow {
        final String key, fullName; String label; int goals, ownGoals; final List<Integer> cards=new ArrayList<>();
        DetailRow(String key,String fullName,String label){this.key=key;this.fullName=fullName;this.label=label;}
    }
}
