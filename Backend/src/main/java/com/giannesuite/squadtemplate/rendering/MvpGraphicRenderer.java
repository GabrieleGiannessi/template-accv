package com.giannesuite.squadtemplate.rendering;

import com.giannesuite.squadtemplate.utils.GraphicBackgroundLoader;
import com.giannesuite.squadtemplate.utils.GraphicPreviewSupport;
import com.giannesuite.squadtemplate.utils.Constants;
import com.giannesuite.squadtemplate.utils.Utilities;
import com.giannesuite.squadtemplate.service.dto.MvpGraphicRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Stream;

/** Port Java2D della grafica MVP originariamente renderizzata con Pillow. */
@Component
public class MvpGraphicRenderer {
    private final Path assetsPath;

    public MvpGraphicRenderer(@Value("${graphics.assets-path:../assets}") String assetsPath) {
        this.assetsPath = Path.of(assetsPath).toAbsolutePath().normalize();
    }

    public BufferedImage render(MvpGraphicRequest request) {
        String ratio = request.format() == null ? "4:5" : request.format().toLowerCase(Locale.ROOT);
        int[] dimensions = dimensions(ratio);
        BufferedImage image = GraphicBackgroundLoader.load(assetsPath, dimensions[0], dimensions[1], request.bg_path(), request.emotion(),
                request.contrast_factor() == null ? 1.0 : request.contrast_factor(), Boolean.TRUE.equals(request.remove_contrast()),
                150, 1.0, 0.5, 0.5);
        GraphicCanvas canvas = new GraphicCanvas(image, assetsPath);
        try {
            drawMvp(canvas, request, ratio);
            return image;
        } finally {
            canvas.close();
        }
    }

    private void drawMvp(GraphicCanvas canvas, MvpGraphicRequest data, String ratio) {
        int width = canvas.width();
        int height = canvas.height();
        boolean vertical = height > width;
        boolean wide = width > height;
        String opponent = value(data.match_opponent(), "").toUpperCase(Locale.ROOT);
        drawHeader(canvas, vertical, wide, opponent.isBlank() ? "PREMIO INDIVIDUALE" : "MATCH vs " + opponent);

        int titleY = ratio.equals("9:16") ? 220 : wide ? 110 : 140;
        int titleSize = ratio.equals("9:16") ? 80 : wide ? 60 : 68;
        Font titleFont = canvas.font("HEADER", titleSize);
        canvas.drawCenteredAt("MIGLIORE IN CAMPO", titleFont, new Color(255, 215, 0), width / 2, titleY + titleFont.getSize());

        int marginX = (int) (width * 0.05);
        int cardY = ratio.equals("9:16") ? 400 : wide ? 200 : 260;
        int cardHeight = ratio.equals("9:16") ? 340 : wide ? 260 : 280;
        canvas.roundedCard(marginX, cardY, width - marginX * 2, cardHeight, 24,
                new Color(30, 26, 18, 230), Constants.GOLD, 3);

        int numberSize = (int) (cardHeight * 0.44);
        int numberX = marginX + (int) (width * 0.05);
        int numberY = cardY + (cardHeight - numberSize) / 2;
        canvas.roundedCard(numberX, numberY, numberSize, numberSize, 20, Constants.GOLD, null, 0);
        int numberFontSize = vertical ? 84 : 68;
        canvas.drawCentered("#" + value(data.jersey_number(), "10"), canvas.font("HEADER", numberFontSize),
                new Color(15, 18, 28), numberX, numberY, numberSize, numberSize);

        int nameX = numberX + numberSize + (int) (width * 0.04);
        int nameSize = vertical ? 38 : 30;
        int nameY = cardY + (int) (cardHeight * 0.25);
        canvas.drawText(value(data.player_name(), "Giocatore ACCV").toUpperCase(Locale.ROOT),
                canvas.font("BODY", nameSize), Constants.WHITE, nameX, nameY + nameSize);
        int positionSize = vertical ? 24 : 20;
        int positionY = cardY + (int) (cardHeight * 0.55);
        canvas.drawText("RUOLO: " + value(data.position(), "Attaccante").toUpperCase(Locale.ROOT),
                canvas.font("REGULAR", positionSize), Constants.GOLD, nameX, positionY + positionSize);

        int statsY = cardY + cardHeight + 30;
        int statsHeight = ratio.equals("9:16") ? 360 : wide ? 220 : 260;
        canvas.roundedCard(marginX, statsY, width - marginX * 2, statsHeight, 20,
                Constants.CARD_BG, Constants.CARD_BORDER, 2);
        String[][] stats = data.saves() != null && data.saves() > 0
                ? new String[][]{{"⚽  GOL SEGNATI", integer(data.goals())}, {"👟  ASSIST", integer(data.assists())},
                    {"🧤  PARATE", integer(data.saves())}, {"⭐  VOTO PAGELLA", value(data.rating(), "9.0")}}
                : new String[][]{{"⚽  GOL SEGNATI", integer(data.goals())}, {"👟  ASSIST", integer(data.assists())},
                    {"⭐  VOTO PAGELLA", value(data.rating(), "9.0")}};
        int itemHeight = statsHeight / stats.length;
        int startY = statsY + 15;
        int labelSize = vertical ? 32 : 26;
        int valueSize = vertical ? 42 : 34;
        for (int i = 0; i < stats.length; i++) {
            int baseline = startY + i * itemHeight + labelSize;
            canvas.drawText(stats[i][0], canvas.font("HEADER", labelSize), Constants.WHITE, marginX + 40, baseline);
            Font valueFont = canvas.font("HEADER", valueSize);
            int valueWidth = canvas.graphics().getFontMetrics(valueFont).stringWidth(stats[i][1]);
            canvas.drawText(stats[i][1], valueFont, Constants.GOLD, width - marginX - 50 - valueWidth, baseline - 4);
            if (i < stats.length - 1) {
                canvas.graphics().setColor(Constants.CARD_BORDER);
                canvas.graphics().drawLine(marginX + 30, startY + (i + 1) * itemHeight - 5,
                        width - marginX - 30, startY + (i + 1) * itemHeight - 5);
            }
        }
        drawFooter(canvas, vertical, "CONGRATULAZIONI AL GIOCATORE!");
    }

    private void drawHeader(GraphicCanvas canvas, boolean vertical, boolean wide, String secondary) {
        int y = vertical ? (canvas.height() == 1920 ? 80 : 55) : wide ? 40 : 50;
        int size = vertical ? 26 : 22;
        Font tournament = canvas.font("REGULAR", size);
        canvas.drawCenteredAt("MVP ACCV", tournament, Constants.CYAN, canvas.width() / 2, y + size);
        if (!secondary.isBlank()) {
            int subSize = vertical ? 56 : 46;
            Font sub = canvas.font("HEADER", subSize);
            canvas.drawCenteredAt(secondary.toUpperCase(Locale.ROOT), sub, Constants.WHITE, canvas.width() / 2, y + 32 + subSize);
        }
    }

    private void drawFooter(GraphicCanvas canvas, boolean vertical, String detail) {
        int y = vertical ? canvas.height() - (canvas.height() == 1920 ? 100 : 75) : canvas.height() - 60;
        Font detailFont = canvas.font("REGULAR", vertical ? 22 : 19);
        canvas.drawCenteredAt(detail, detailFont, Constants.MUTED, canvas.width() / 2, y - 32 + detailFont.getSize());
        Font brand = canvas.font("BODY", vertical ? 20 : 17);
        canvas.drawCenteredAt("A.C.C.V.  •  OFFICIAL GRAPHICS", brand, Constants.CYAN, canvas.width() / 2, y + brand.getSize());
    }

    private BufferedImage loadBackground(MvpGraphicRequest request, int width, int height) {
        Path path = resolveBackground(request.bg_path(), request.emotion());
        BufferedImage source = null;
        if (path != null) {
            try { source = ImageIO.read(path.toFile()); } catch (IOException ignored) { }
        }
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        if (source == null) {
            g.setColor(new Color(15, 18, 28));
            g.fillRect(0, 0, width, height);
        } else {
            liftDarkImage(source);
            double scale = Math.max((double) width / source.getWidth(), (double) height / source.getHeight());
            double zoom = Math.max(1.0, Math.min(2.5, 1.0));
            scale *= zoom;
            int scaledW = (int) Math.ceil(source.getWidth() * scale);
            int scaledH = (int) Math.ceil(source.getHeight() * scale);
            BufferedImage scaled = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D sg = scaled.createGraphics();
            sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            sg.drawImage(source, 0, 0, scaledW, scaledH, null);
            sg.dispose();
            int cropX = (scaledW - width) / 2;
            int cropY = (scaledH - height) / 2;
            g.drawImage(scaled, -cropX, -cropY, null);
            applyContrast(canvas, request.contrast_factor() == null ? 1.0 : request.contrast_factor(),
                    Boolean.TRUE.equals(request.remove_contrast()));
        }
        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(new Color(12, 16, 26, 150));
        g.fillRect(0, 0, width, height);
        g.dispose();
        return canvas;
    }

    private void liftDarkImage(BufferedImage image) {
        long luminanceSum = 0;
        long pixelCount = (long) image.getWidth() * image.getHeight();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int red = (pixel >>> 16) & 255;
                int green = (pixel >>> 8) & 255;
                int blue = pixel & 255;
                luminanceSum += (int) (0.299 * red + 0.587 * green + 0.114 * blue);
            }
        }
        double meanLuminance = (double) luminanceSum / pixelCount;
        if (meanLuminance >= 82) return;
        double factor = Math.min(1.22, 1.0 + (82.0 - meanLuminance) / 420.0);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int alpha = pixel >>> 24;
                int red = clamp((int) (((pixel >>> 16) & 255) * factor));
                int green = clamp((int) (((pixel >>> 8) & 255) * factor));
                int blue = clamp((int) ((pixel & 255) * factor));
                image.setRGB(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
            }
        }
    }

    private void applyContrast(BufferedImage image, double factor, boolean removeContrast) {
        double actual = removeContrast ? 0.4 : factor;
        if (actual == 1.0) return;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int a = pixel >>> 24;
                int r = (pixel >>> 16) & 255;
                int g = (pixel >>> 8) & 255;
                int b = pixel & 255;
                int luminance = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                r = clamp((int) (luminance + (r - luminance) * actual));
                g = clamp((int) (luminance + (g - luminance) * actual));
                b = clamp((int) (luminance + (b - luminance) * actual));
                image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
    }

    private Path resolveBackground(String explicit, String emotion) {
        if (explicit != null && !explicit.isBlank()) {
            Path raw = Path.of(explicit);
            Path[] candidates = {raw, assetsPath.resolve(raw), assetsPath.getParent().resolve(raw)};
            for (Path candidate : candidates) if (Files.isRegularFile(candidate)) return candidate.normalize();
        }
        Path backgrounds = assetsPath.resolve("backgrounds");
        if (emotion != null && !emotion.isBlank()) {
            String wanted = Utilities.normalizeText(emotion);
            for (Path root : new Path[]{backgrounds.resolve("temi"), backgrounds}) {
                if (Files.isDirectory(root)) {
                    try (Stream<Path> children = Files.list(root)) {
                        Path dir = children.filter(Files::isDirectory)
                                .filter(p -> Utilities.normalizeText(p.getFileName().toString().replaceFirst("(?i)^tema\\s+", "")).equals(wanted))
                                .findFirst().orElse(null);
                        Path image = firstImage(dir);
                        if (image != null) return image;
                    } catch (IOException ignored) { }
                }
            }
        }
        Path standard = firstImage(backgrounds.resolve("standard"));
        if (standard != null) return standard;
        Path defaultImage = backgrounds.resolve("std.JPG");
        return Files.isRegularFile(defaultImage) ? defaultImage : firstImage(backgrounds);
    }

    private Path firstImage(Path directory) {
        if (directory == null || !Files.isDirectory(directory)) return null;
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(Files::isRegularFile).filter(p -> {
                String name = p.getFileName().toString().toLowerCase(Locale.ROOT);
                return Constants.SUPPORTED_IMAGE_EXTENSIONS.contains(name.substring(name.lastIndexOf('.') + 1));
            }).sorted().findFirst().orElse(null);
        } catch (IOException e) { return null; }
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

    private String value(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
    private String integer(Integer value) { return String.valueOf(value == null ? 0 : value); }
    private int clamp(int value) { return Math.max(0, Math.min(255, value)); }
}
