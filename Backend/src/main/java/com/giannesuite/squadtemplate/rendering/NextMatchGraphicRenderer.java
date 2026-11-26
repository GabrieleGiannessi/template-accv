package com.giannesuite.squadtemplate.rendering;

import com.giannesuite.squadtemplate.utils.GraphicBackgroundLoader;
import com.giannesuite.squadtemplate.utils.GraphicImageSupport;
import com.giannesuite.squadtemplate.utils.Constants;
import com.giannesuite.squadtemplate.service.dto.NextMatchGraphicRequest;
import com.giannesuite.squadtemplate.service.dto.TeamGraphicRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Locale;

@Component
public class NextMatchGraphicRenderer {
    private final Path assetsPath;

    public NextMatchGraphicRenderer(@Value("${graphics.assets-path:../assets}") String assetsPath) {
        this.assetsPath = Path.of(assetsPath).toAbsolutePath().normalize();
    }

    public BufferedImage render(NextMatchGraphicRequest request) {
        String format = request.format() == null ? "9:16" : request.format().toLowerCase(Locale.ROOT);
        int[] size = dimensions(format);
        double contrast = request.contrast_factor() == null ? 1.0 : request.contrast_factor();
        BufferedImage image = GraphicBackgroundLoader.load(assetsPath, size[0], size[1], request.bg_path(), request.emotion(),
                contrast, Boolean.TRUE.equals(request.remove_contrast()), 150, 1.0, 0.5, 0.5);
        GraphicCanvas canvas = new GraphicCanvas(image, assetsPath);
        try { draw(canvas, request, format); } finally { canvas.close(); }
        return image;
    }

    private void draw(GraphicCanvas c, NextMatchGraphicRequest data, String format) {
        int w = c.width(), h = c.height();
        boolean vertical = h > w, wide = w > h;
        String tournament = text(data.tournament(), "CAMPIONATO CALCETTO A5");
        int headerY = vertical ? (h == 1920 ? 80 : 55) : wide ? 40 : 50;
        Font header = c.font("REGULAR", vertical ? 26 : 22);
        c.drawCenteredAt(tournament.toUpperCase(Locale.ROOT), header, Constants.CYAN, w / 2, headerY + header.getSize());

        int titleY = format.equals("9:16") || format.equals("story") ? 220 : wide ? 110 : 140;
        int titleSize = format.equals("9:16") || format.equals("story") ? 80 : wide ? 60 : 68;
        Font titleFont = c.font("HEADER", titleSize);
        c.drawCenteredAt("PROSSIMA PARTITA", titleFont, Constants.WHITE, w / 2, titleY + titleSize);
        Font subFont = c.font("BODY", vertical ? 24 : 20);
        double titleInkHeight = titleFont.createGlyphVector(c.graphics().getFontRenderContext(), "PROSSIMA PARTITA")
                .getVisualBounds().getHeight();
        int subtitleTop = titleY + (int) Math.ceil(titleInkHeight) + 4;
        c.drawCenteredAt(tournament.toUpperCase(Locale.ROOT), subFont, Constants.CYAN, w / 2,
                subtitleTop + c.graphics().getFontMetrics(subFont).getAscent());

        int margin = (int) (w * 0.05);
        int cardY = format.equals("9:16") || format.equals("story") ? 400 : wide ? 200 : 270;
        int cardH = format.equals("9:16") || format.equals("story") ? 380 : wide ? 280 : 320;
        c.roundedCard(margin, cardY, w - margin * 2, cardH, 24, Constants.CARD_BG, Constants.CARD_BORDER, 2);

        TeamGraphicRequest home = data.home_team(), away = data.away_team();
        String homeName = home == null ? "A.C.C.V." : home.safeName("A.C.C.V.");
        String awayName = away == null ? "Avversario" : away.safeName("Avversario");
        int logoSize = (int) (cardH * 0.44);
        BufferedImage homeLogo = GraphicImageSupport.logo(assetsPath, home == null ? null : home.logo_path(), homeName,
                home == null ? "ACCV" : home.safeShortName("ACCV"), logoSize,
                home == null ? null : home.primary_color());
        BufferedImage awayLogo = GraphicImageSupport.logo(assetsPath, away == null ? null : away.logo_path(), awayName,
                away == null ? "OPP" : away.safeShortName("OPP"), logoSize,
                away == null ? null : away.primary_color());
        int logoY = cardY + (int) (cardH * 0.12);
        int homeX = margin + (int) (w * 0.06);
        int awayX = w - margin - (int) (w * 0.06) - logoSize;
        c.graphics().drawImage(homeLogo, homeX, logoY, null);
        c.graphics().drawImage(awayLogo, awayX, logoY, null);

        Font teamFont = c.font("BODY", vertical ? 24 : 20);
        drawCentered(c, homeName, teamFont, homeX, logoY + logoSize + 12, logoSize, teamFont.getSize() * 2);
        drawCentered(c, awayName, teamFont, awayX, logoY + logoSize + 12, logoSize, teamFont.getSize() * 2);

        int vsSize = (int) (cardH * 0.35);
        int vsX = (w - vsSize) / 2, vsY = cardY + (cardH - vsSize) / 2 - 5;
        c.graphics().setColor(new Color(22, 27, 40, 240));
        c.graphics().fillOval(vsX, vsY, vsSize, vsSize);
        c.graphics().setColor(Constants.CYAN);
        c.graphics().setStroke(new java.awt.BasicStroke(3));
        c.graphics().drawOval(vsX, vsY, vsSize, vsSize);
        c.drawCentered("VS", c.font("HEADER", vertical ? 64 : 52), Constants.WHITE, vsX, vsY, vsSize, vsSize);

        String venue = text(data.location(), text(data.field_name(), ""));
        if (!venue.isBlank()) {
            int infoY = cardY + cardH + 30;
            int infoH = vertical ? 180 : wide ? 120 : 140;
            c.roundedCard(margin, infoY, w - margin * 2, infoH, 20, Constants.CARD_BG, Constants.CARD_BORDER, 2);
            Font labelFont = c.font("HEADER", vertical ? 32 : 26);
            Font valueFont = c.font("BODY", vertical ? 24 : 20);
            int centerY = infoY + infoH / 2;
            c.drawText("☒☒  CAMPO", labelFont, Constants.CYAN, margin + 40, centerY + labelFont.getSize() / 3);
            int valueWidth = c.graphics().getFontMetrics(valueFont).stringWidth(venue);
            c.drawText(venue, valueFont, Constants.WHITE, w - margin - 40 - valueWidth, centerY + valueFont.getSize() / 3);
        }

        int footY = vertical ? h - (h == 1920 ? 100 : 75) : h - 60;
        String detail = "NON MANCARE!  •  " + tournament;
        Font detailFont = c.font("REGULAR", vertical ? 22 : 19);
        c.drawCenteredAt(detail, detailFont, Constants.MUTED, w / 2, footY - 32 + detailFont.getSize());
        Font brand = c.font("BODY", vertical ? 20 : 17);
        c.drawCenteredAt("A.C.C.V.  •  OFFICIAL GRAPHICS", brand, Constants.CYAN, w / 2, footY + brand.getSize());
    }

    private void drawCentered(GraphicCanvas canvas, String text, Font font, int x, int y, int width, int height) {
        canvas.drawCenteredAt(text, font, Constants.WHITE, x + width / 2, y + font.getSize());
    }

    private int[] dimensions(String format) {
        return switch (format) {
            case "9:16", "story" -> new int[]{1080, 1920};
            case "4:3" -> new int[]{1440, 1080};
            case "16:9" -> new int[]{1920, 1080};
            case "1:1", "post" -> new int[]{1080, 1080};
            case "4:5" -> new int[]{1080, 1350};
            default -> throw new IllegalArgumentException("Formato grafica non supportato: " + format);
        };
    }

    private String text(String value, String fallback) { return value == null ? fallback : value; }
}
