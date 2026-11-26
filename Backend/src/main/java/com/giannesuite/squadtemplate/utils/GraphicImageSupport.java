package com.giannesuite.squadtemplate.utils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GraphicImageSupport {
    private GraphicImageSupport() { }

    public static Path resolve(Path assets, String path) {
        if (path == null || path.isBlank()) return null;
        Path raw = Path.of(path);
        Path[] options = {raw, assets.resolve(raw), assets.getParent().resolve(raw)};
        for (Path option : options) if (Files.isRegularFile(option)) return option.toAbsolutePath().normalize();
        return null;
    }

    public static BufferedImage logo(Path assets, String logoPath, String teamName, String fallback,
                              int size, int[] color) {
        Path path = resolve(assets, logoPath);
        BufferedImage source = null;
        if (path != null) {
            try { source = ImageIO.read(path.toFile()); } catch (IOException ignored) { }
        }
        BufferedImage canvas = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (source != null) {
            int[] bounds = alphaBounds(source);
            if (bounds != null) {
                int sourceW = bounds[2] - bounds[0];
                int sourceH = bounds[3] - bounds[1];
                int inner = Math.max(10, size - 24);
                double scale = Math.min((double) inner / sourceW, (double) inner / sourceH);
                int w = Math.max(1, (int) Math.round(sourceW * scale));
                int h = Math.max(1, (int) Math.round(sourceH * scale));
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.drawImage(source, (size - w) / 2, (size - h) / 2, (size + w) / 2, (size + h) / 2,
                        bounds[0], bounds[1], bounds[2], bounds[3], null);
                g.dispose();
                return canvas;
            }
        }
        int r = size / 2 - 4;
        Color primary = color(color, new Color(0, 229, 255));
        g.setColor(new Color(22, 27, 40, 240));
        g.fillOval(size / 2 - r, size / 2 - r, r * 2, r * 2);
        g.setColor(primary);
        g.setStroke(new java.awt.BasicStroke(4));
        g.drawOval(size / 2 - r, size / 2 - r, r * 2, r * 2);
        String text = fallback == null || fallback.isBlank() ? teamName : fallback;
        if (text == null || text.isBlank()) text = "TEAM";
        text = text.substring(0, Math.min(3, text.length())).toUpperCase();
        var font = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, Math.max(12, (int) (r * 0.7)));
        g.setFont(font);
        var metrics = g.getFontMetrics();
        g.setColor(Color.WHITE);
        g.drawString(text, (size - metrics.stringWidth(text)) / 2, (size - metrics.getHeight()) / 2 + metrics.getAscent());
        g.dispose();
        return canvas;
    }

    public static Color color(int[] rgb, Color fallback) {
        if (rgb == null || rgb.length < 3) return fallback;
        return new Color(clamp(rgb[0]), clamp(rgb[1]), clamp(rgb[2]));
    }

    private static int[] alphaBounds(BufferedImage image) {
        int minX = image.getWidth(), minY = image.getHeight(), maxX = -1, maxY = -1;
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
            if ((image.getRGB(x, y) >>> 24) == 0) continue;
            minX = Math.min(minX, x); minY = Math.min(minY, y);
            maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
        }
        return maxX < minX ? null : new int[]{minX, minY, maxX + 1, maxY + 1};
    }

    private static int clamp(int value) { return Math.max(0, Math.min(255, value)); }
}
