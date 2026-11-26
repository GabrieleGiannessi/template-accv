package com.giannesuite.squadtemplate.utils;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Stream;

/** Gestione condivisa degli sfondi delle grafiche Java2D. */
public final class GraphicBackgroundLoader {
    private GraphicBackgroundLoader() { }

    public static BufferedImage load(Path assetsPath, int width, int height, String bgPath, String emotion,
                                     double contrast, boolean removeContrast, int darkOverlayAlpha,
                                     double zoom, double positionX, double positionY) {
        Path path = resolveBackground(assetsPath, bgPath, emotion);
        BufferedImage source = null;
        if (path != null) {
            try { source = ImageIO.read(path.toFile()); } catch (IOException ignored) { }
        }
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        if (source == null) {
            graphics.setColor(new Color(15, 18, 28));
            graphics.fillRect(0, 0, width, height);
        } else {
            liftDarkImage(source);
            double fitScale = Math.max((double) width / source.getWidth(), (double) height / source.getHeight());
            double scale = fitScale * Math.max(1.0, Math.min(2.5, zoom));
            int scaledWidth = Math.max(width, (int) Math.ceil(source.getWidth() * scale));
            int scaledHeight = Math.max(height, (int) Math.ceil(source.getHeight() * scale));
            BufferedImage scaled = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D scaleGraphics = scaled.createGraphics();
            scaleGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            scaleGraphics.drawImage(source, 0, 0, scaledWidth, scaledHeight, null);
            scaleGraphics.dispose();
            double x = Math.max(0.0, Math.min(1.0, positionX));
            double y = Math.max(0.0, Math.min(1.0, positionY));
            int cropX = (int) ((scaledWidth - width) * x);
            int cropY = (int) ((scaledHeight - height) * y);
            graphics.drawImage(scaled, -cropX, -cropY, null);
            applyContrast(canvas, contrast, removeContrast);
        }
        if (darkOverlayAlpha > 0) {
            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.setColor(new Color(12, 16, 26, Math.max(0, Math.min(255, darkOverlayAlpha))));
            graphics.fillRect(0, 0, width, height);
        }
        graphics.dispose();
        return canvas;
    }

    private static void liftDarkImage(BufferedImage image) {
        long sum = 0;
        long count = (long) image.getWidth() * image.getHeight();
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
            int pixel = image.getRGB(x, y);
            sum += (int) (0.299 * ((pixel >>> 16) & 255) + 0.587 * ((pixel >>> 8) & 255) + 0.114 * (pixel & 255));
        }
        double mean = (double) sum / count;
        if (mean >= 82) return;
        double factor = Math.min(1.22, 1.0 + (82.0 - mean) / 420.0);
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
            int pixel = image.getRGB(x, y);
            int alpha = pixel >>> 24;
            int red = clamp((int) (((pixel >>> 16) & 255) * factor));
            int green = clamp((int) (((pixel >>> 8) & 255) * factor));
            int blue = clamp((int) ((pixel & 255) * factor));
            image.setRGB(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
        }
    }

    private static void applyContrast(BufferedImage image, double factor, boolean removeContrast) {
        double actual = removeContrast ? 0.4 : factor;
        if (actual == 1.0) return;
        long sum = 0;
        long count = (long) image.getWidth() * image.getHeight();
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
            int p = image.getRGB(x, y);
            sum += (int) (0.299 * ((p >>> 16) & 255) + 0.587 * ((p >>> 8) & 255) + 0.114 * (p & 255));
        }
        int mean = (int) (sum / count);
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
            int p = image.getRGB(x, y);
            int a = p >>> 24;
            int r = clamp((int) (mean + (((p >>> 16) & 255) - mean) * actual));
            int g = clamp((int) (mean + (((p >>> 8) & 255) - mean) * actual));
            int b = clamp((int) (mean + ((p & 255) - mean) * actual));
            image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
        }
    }

    public static Path resolveBackground(Path assetsPath, String explicit, String emotion) {
        if (explicit != null && !explicit.isBlank()) {
            Path raw = Path.of(explicit);
            Path[] candidates = {raw, assetsPath.resolve(raw), assetsPath.getParent().resolve(raw)};
            for (Path candidate : candidates) if (Files.isRegularFile(candidate)) return candidate.normalize();
        }
        Path backgrounds = assetsPath.resolve("backgrounds");
        if (emotion != null && !emotion.isBlank()) {
            String wanted = Utilities.normalizeText(emotion);
            for (Path root : new Path[]{backgrounds.resolve("temi"), backgrounds}) {
                if (!Files.isDirectory(root)) continue;
                try (Stream<Path> children = Files.list(root)) {
                    Path dir = children.filter(Files::isDirectory)
                            .filter(p -> Utilities.normalizeText(p.getFileName().toString().replaceFirst("(?i)^tema\\s+", "")).equals(wanted))
                            .findFirst().orElse(null);
                    Path image = firstImage(dir);
                    if (image != null) return image;
                } catch (IOException ignored) { }
            }
        }
        Path standard = firstImage(backgrounds.resolve("standard"));
        if (standard != null) return standard;
        Path defaultImage = backgrounds.resolve("std.JPG");
        return Files.isRegularFile(defaultImage) ? defaultImage : firstImage(backgrounds);
    }

    private static Path firstImage(Path directory) {
        if (directory == null || !Files.isDirectory(directory)) return null;
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(Files::isRegularFile).filter(p -> {
                String name = p.getFileName().toString().toLowerCase(Locale.ROOT);
                return Constants.SUPPORTED_IMAGE_EXTENSIONS.contains(name.substring(name.lastIndexOf('.') + 1));
            }).sorted().findFirst().orElse(null);
        } catch (IOException e) { return null; }
    }

    private static int clamp(int value) { return Math.max(0, Math.min(255, value)); }
}
