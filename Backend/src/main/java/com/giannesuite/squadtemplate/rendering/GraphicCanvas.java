package com.giannesuite.squadtemplate.rendering;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Tela Java2D condivisa: rendering antialias, testo e pannelli arrotondati. */
public final class GraphicCanvas {
    private final BufferedImage image;
    private final Graphics2D graphics;
    private final Path assetsPath;

    public GraphicCanvas(BufferedImage image, Path assetsPath) {
        this.image = image;
        this.assetsPath = assetsPath;
        this.graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    public BufferedImage image() { return image; }
    public int width() { return image.getWidth(); }
    public int height() { return image.getHeight(); }
    public Graphics2D graphics() { return graphics; }

    public Font font(String key, int size) {
        String filename = switch (key) {
            case "HEADER" -> "BebasNeue-Regular.ttf";
            case "BODY" -> "Montserrat-Bold.ttf";
            case "REGULAR" -> "Montserrat-Regular.ttf";
            default -> "Montserrat-Light.ttf";
        };
        Path file = assetsPath.resolve("fonts").resolve(filename);
        if (Files.isRegularFile(file)) {
            try {
                return Font.createFont(Font.TRUETYPE_FONT, file.toFile()).deriveFont((float) size);
            } catch (IOException | FontFormatException ignored) {
                // Usa il font di sistema se il file non è valido.
            }
        }
        int style = "HEADER".equals(key) ? Font.BOLD : Font.PLAIN;
        return new Font(Font.SANS_SERIF, style, size);
    }

    public void roundedCard(int x, int y, int width, int height, int radius, Color fill, Color border, int borderWidth) {
        var originalComposite = graphics.getComposite();
        // Pillow ImageDraw writes the RGBA tuple into the destination pixels;
        // it does not blend translucent fills with the pixels underneath.
        graphics.setComposite(AlphaComposite.Src);
        graphics.setColor(fill);
        graphics.fill(new RoundRectangle2D.Float(x, y, width, height, radius * 2f, radius * 2f));
        if (border != null && borderWidth > 0) {
            graphics.setColor(border);
            graphics.setStroke(new java.awt.BasicStroke(borderWidth));
            graphics.draw(new RoundRectangle2D.Float(x, y, width, height, radius * 2f, radius * 2f));
        }
        graphics.setComposite(originalComposite);
    }

    public void drawText(String value, Font font, Color color, int x, int baselineY) {
        graphics.setFont(font);
        graphics.setColor(color);
        graphics.drawString(value == null ? "" : value, x, baselineY);
    }

    public void drawCentered(String value, Font font, Color color, int boxX, int boxY, int boxWidth, int boxHeight) {
        graphics.setFont(font);
        var metrics = graphics.getFontMetrics(font);
        int x = boxX + (boxWidth - metrics.stringWidth(value)) / 2;
        int y = boxY + (boxHeight - metrics.getHeight()) / 2 + metrics.getAscent();
        drawText(value, font, color, x, y);
    }

    public void drawCenteredAt(String value, Font font, Color color, int centerX, int baselineY) {
        graphics.setFont(font);
        drawText(value, font, color, centerX - graphics.getFontMetrics(font).stringWidth(value) / 2, baselineY);
    }

    public void close() { graphics.dispose(); }
}
