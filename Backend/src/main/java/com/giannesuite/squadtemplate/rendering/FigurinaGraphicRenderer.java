package com.giannesuite.squadtemplate.rendering;

import com.giannesuite.squadtemplate.utils.GraphicImageSupport;
import com.giannesuite.squadtemplate.utils.GraphicPreviewSupport;
import com.giannesuite.squadtemplate.utils.Constants;
import com.giannesuite.squadtemplate.service.dto.FigurinaGraphicRequest;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/** Port Java2D della figurina e dei suoi compositing Story/Post. */
@Component
public class FigurinaGraphicRenderer {
    private static final int X1 = 76, Y1 = 186, X2 = 948, Y2 = 1321, CORNER = 28, BANNER_H = 120;
    private final Path assets;

    public FigurinaGraphicRenderer(@Value("${graphics.assets-path:../assets}") String assetsPath) {
        assets = Path.of(assetsPath).toAbsolutePath().normalize();
    }

    public BufferedImage render(FigurinaGraphicRequest request) {
        String format = request.format() == null ? "story" : request.format().toLowerCase(Locale.ROOT);
        BufferedImage card = renderCard(request);
        return switch (format) {
            case "story", "9:16" -> renderStory(card);
            case "post", "4:5" -> renderPost(card);
            default -> card;
        };
    }

    private BufferedImage renderCard(FigurinaGraphicRequest d) {
        BufferedImage template = readRequired(resolveAsset("templates/figurina.png"), "Template figurina non trovato");
        Path photoPath = resolvePhoto(d);
        BufferedImage photo = readRequired(photoPath, "Foto giocatore non trovata");
        int[] bounds = alphaBounds(photo);
        if (bounds != null) photo = photo.getSubimage(bounds[0], bounds[1], bounds[2] - bounds[0], bounds[3] - bounds[1]);

        int boxW = X2 - X1, boxH = Y2 - Y1;
        double aspect = (double) photo.getWidth() / Math.max(1, photo.getHeight());
        double scale;
        int scaledW, scaledH, posX, posY;
        boolean banner = !Boolean.FALSE.equals(d.with_banner());
        if (aspect < 0.5) {
            int availableH = banner ? boxH - BANNER_H : boxH;
            scale = Math.min((availableH * 0.96) / photo.getHeight(), (boxW * 0.88) / photo.getWidth());
            scaledW = Math.max(1, (int) (photo.getWidth() * scale)); scaledH = Math.max(1, (int) (photo.getHeight() * scale));
            posX = X1 + (boxW - scaledW) / 2;
            posY = banner ? Y2 - BANNER_H - scaledH + 4 : Y2 - scaledH;
        } else if (aspect > 0.85) {
            scale = Math.min((boxH * 0.78) / photo.getHeight(), (boxW * 0.85) / photo.getWidth());
            scaledW = Math.max(1, (int) (photo.getWidth() * scale)); scaledH = Math.max(1, (int) (photo.getHeight() * scale));
            posX = X1 + (boxW - scaledW) / 2; posY = Y2 - scaledH;
        } else {
            scale = Math.min((boxH * 0.88) / photo.getHeight(), (boxW * 0.95) / photo.getWidth());
            scaledW = Math.max(1, (int) (photo.getWidth() * scale)); scaledH = Math.max(1, (int) (photo.getHeight() * scale));
            posX = X1 + (boxW - scaledW) / 2; posY = Y2 - scaledH;
        }
        BufferedImage resizedPhoto = resize(photo, scaledW, scaledH);
        Graphics2D g = template.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setClip(new RoundRectangle2D.Double(X1, Y1, boxW, boxH, CORNER * 2.0, CORNER * 2.0));
        g.drawImage(resizedPhoto, posX, posY, null);
        g.setClip(null);
        g.dispose();
        if (banner) drawBanner(template, d);
        return template;
    }

    private void drawBanner(BufferedImage card, FigurinaGraphicRequest d) {
        Graphics2D g = card.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setComposite(AlphaComposite.SrcOver);
        int y = Y2 - BANNER_H;
        g.setColor(new Color(10, 24, 44, 245));
        g.fill(new RoundRectangle2D.Double(X1, y, X2 - X1, BANNER_H, CORNER * 2.0, CORNER * 2.0));
        g.setColor(new Color(0, 229, 255, 220)); g.setStroke(new BasicStroke(2));
        g.draw(new RoundRectangle2D.Double(X1 + 1, y + 1, X2 - X1 - 2, BANNER_H - 2, CORNER * 2.0, CORNER * 2.0));
        g.setColor(new Color(0, 229, 255)); g.setStroke(new BasicStroke(3));
        g.drawLine(X1 + 4, y, X2 - 4, y);

        BufferedImage logo = loadCardLogo(d.team_logo_path());
        int logoX = X1 + 18, logoY = y + (BANNER_H - logo.getHeight()) / 2;
        g.drawImage(logo, logoX, logoY, null);

        int badgeReserved = 0;
        String jersey = d.jersey_number();
        if (jersey != null && !jersey.isBlank()) {
            int badge = 84, bx = X2 - badge - 18, by = y + (BANNER_H - badge) / 2;
            g.setColor(new Color(18, 32, 58));
            g.fill(new RoundRectangle2D.Double(bx, by, badge, badge, 36, 36));
            g.setColor(Constants.CYAN); g.setStroke(new BasicStroke(2));
            g.draw(new RoundRectangle2D.Double(bx + 1, by + 1, badge - 2, badge - 2, 36, 36));
            drawCentered(g, "#" + jersey, new Font(Font.SANS_SERIF, Font.BOLD, 52), bx, by, badge, badge, Color.WHITE);
            badgeReserved = badge + 28;
        }
        Path fontDir = assets.resolve("fonts");
        GraphicCanvas fontCanvas = new GraphicCanvas(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), assets);
        try {
            int textX = logoX + logo.getWidth() + 20;
            int maxWidth = X2 - textX - badgeReserved - 15;
            String playerName = text(d.player_name(), "Giocatore").toUpperCase(Locale.ROOT);
            Font nameFont = fit(fontCanvas, playerName, "HEADER", maxWidth, 52, 28);
            drawTop(g, playerName, nameFont, textX, y + 14, Color.WHITE);
            String role = text(d.role(), "").toUpperCase(Locale.ROOT);
            String team = text(d.team_name(), "A.C.C.V.").toUpperCase(Locale.ROOT);
            String subtitle = !role.isBlank() && !team.isBlank() ? role + "  |  " + team : !role.isBlank() ? role : !team.isBlank() ? team : "A.C.C.V.";
            Font sub = fit(fontCanvas, subtitle, "REGULAR", maxWidth, 24, 14);
            drawTop(g, subtitle, sub, textX, y + 64, Constants.CYAN);
        } finally { fontCanvas.close(); }
        g.dispose();
    }

    private BufferedImage renderStory(BufferedImage card) {
        int w = 1080, h = 1920;
        BufferedImage bg = gradient(w, h);
        int cardH = 1380, cardW = (int) (card.getWidth() * ((double) cardH / card.getHeight()));
        BufferedImage scaled = resize(card, cardW, cardH);
        int x = (w - cardW) / 2, y = (h - cardH) / 2 + 30;
        BufferedImage shadow = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = shadow.createGraphics(); sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        sg.setColor(new Color(0, 0, 0, 200)); sg.fill(new RoundRectangle2D.Double(x - 6, y + 12, cardW + 12, cardH + 12, 70, 70)); sg.dispose();
        shadow = blur(shadow, 28);
        Graphics2D g = bg.createGraphics(); g.drawImage(shadow, 0, 0, null); g.drawImage(scaled, x, y, null);
        GraphicCanvas c = new GraphicCanvas(bg, assets);
        try {
            Font sub = c.font("REGULAR", 26), title = c.font("HEADER", 60), foot = c.font("REGULAR", 22);
            c.drawCenteredAt("A.C.C.V.  -  ROSTER UFFICIALE", sub, Constants.CYAN, w / 2, 70 + sub.getSize());
            c.drawCenteredAt("COLLEZIONE FIGURINE CALCIATORI", title, Color.WHITE, w / 2, 106 + title.getSize());
            c.drawCenteredAt("SERIE B PERINI  -  STAGIONE 2026", foot, Constants.MUTED, w / 2, h - 65 + foot.getSize());
        } finally { c.close(); }
        g.dispose(); return bg;
    }

    private BufferedImage renderPost(BufferedImage card) {
        int w = 1080, h = 1350;
        BufferedImage bg = gradient(w, h);
        int cardH = 1080, cardW = (int) (card.getWidth() * ((double) cardH / card.getHeight()));
        BufferedImage scaled = resize(card, cardW, cardH);
        int x = (w - cardW) / 2, y = (h - cardH) / 2 + 25;
        BufferedImage shadow = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = shadow.createGraphics(); sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        sg.setColor(new Color(0, 0, 0, 190)); sg.fill(new RoundRectangle2D.Double(x - 5, y + 10, cardW + 10, cardH + 8, 60, 60)); sg.dispose();
        shadow = blur(shadow, 24);
        Graphics2D g = bg.createGraphics(); g.drawImage(shadow, 0, 0, null); g.drawImage(scaled, x, y, null);
        GraphicCanvas c = new GraphicCanvas(bg, assets);
        try {
            Font title = c.font("HEADER", 46);
            c.drawCenteredAt("A.C.C.V.  -  FIGURINE UFFICIALI", title, Color.WHITE, w / 2, 45 + title.getSize());
        } finally { c.close(); }
        g.dispose(); return bg;
    }

    private BufferedImage gradient(int w, int h) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        for (int y = 0; y < h; y++) {
            double t = (double) y / h;
            int r = (int) (14 + (6 - 14) * t), gr = (int) (18 + (8 - 18) * t), b = (int) (28 + (14 - 28) * t);
            g.setColor(new Color(r, gr, b)); g.drawLine(0, y, w, y);
        }
        BufferedImage spotlight = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D spot = spotlight.createGraphics();
        spot.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        spot.setComposite(AlphaComposite.Src);
        int cx = w / 2, cy = (int) (h * 0.35), maxR = (int) (Math.min(w, h) * 0.6);
        for (int radius = maxR; radius > 0; radius -= 5) {
            int alpha = (int) (35 * (1.0 - Math.pow((double) radius / maxR, 1.5)));
            spot.setColor(new Color(0, 229, 255, alpha)); spot.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
        }
        spot.dispose();
        g.setComposite(AlphaComposite.SrcOver); g.drawImage(spotlight, 0, 0, null);
        g.dispose(); return image;
    }

    private BufferedImage blur(BufferedImage image, int radius) {
        // Three box-blur passes approximate Pillow's GaussianBlur at the target radius.
        int r = Math.max(1, radius / 3);
        BufferedImage result = image;
        for (int i = 0; i < 3; i++) result = boxBlur(result, r);
        return result;
    }

    private BufferedImage boxBlur(BufferedImage source, int radius) {
        int w = source.getWidth(), h = source.getHeight();
        int[] data = source.getRGB(0, 0, w, h, null, 0, w), temp = new int[data.length], out = new int[data.length];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            long[] sums = new long[4]; int count = 0;
            for (int k = Math.max(0, x - radius); k <= Math.min(w - 1, x + radius); k++) { add(sums, data[y * w + k]); count++; }
            temp[y * w + x] = rgba(sums, count);
        }
        for (int x = 0; x < w; x++) for (int y = 0; y < h; y++) {
            long[] sums = new long[4]; int count = 0;
            for (int k = Math.max(0, y - radius); k <= Math.min(h - 1, y + radius); k++) { add(sums, temp[k * w + x]); count++; }
            out[y * w + x] = rgba(sums, count);
        }
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        result.setRGB(0, 0, w, h, out, 0, w); return result;
    }

    private void add(long[] s, int p) { s[0] += p >>> 24; s[1] += (p >>> 16) & 255; s[2] += (p >>> 8) & 255; s[3] += p & 255; }
    private int rgba(long[] s, int n) { return ((int)(s[0]/n)<<24)|((int)(s[1]/n)<<16)|((int)(s[2]/n)<<8)|(int)(s[3]/n); }

    private BufferedImage loadCardLogo(String path) {
        Path p = GraphicImageSupport.resolve(assets, path == null || path.isBlank() ? "logos/accv.png" : path);
        BufferedImage canvas = new BufferedImage(86, 86, BufferedImage.TYPE_INT_ARGB);
        if (p != null) try {
            BufferedImage img = ImageIO.read(p.toFile()); int[] bounds = alphaBounds(img);
            if (bounds != null) img = img.getSubimage(bounds[0], bounds[1], bounds[2]-bounds[0], bounds[3]-bounds[1]);
            double scale = Math.min(86.0 / img.getWidth(), 86.0 / img.getHeight());
            int w = Math.max(1,(int)(img.getWidth()*scale)), h = Math.max(1,(int)(img.getHeight()*scale));
            Graphics2D g = canvas.createGraphics(); g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(img,(86-w)/2,(86-h)/2,w,h,null);g.dispose();return canvas;
        } catch(IOException ignored) { }
        Graphics2D g = canvas.createGraphics(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(20,28,45));g.fillOval(2,2,82,82);g.setColor(Constants.CYAN);g.setStroke(new BasicStroke(3));g.drawOval(2,2,82,82);
        drawCentered(g,"ACCV",new Font(Font.SANS_SERIF,Font.BOLD,36),0,0,86,86,Color.WHITE);g.dispose();return canvas;
    }

    private Path resolvePhoto(FigurinaGraphicRequest d) {
        Path direct = GraphicImageSupport.resolve(assets, d.photo_path()); if (direct != null) return direct;
        String key = d.player_key();
        Path folder = assets.resolve("players/headshot/transparent");
        if (key != null && !key.isBlank() && Files.isDirectory(folder)) try (var files = Files.list(folder)) {
            String clean = Path.of(key).getFileName().toString().replaceFirst("\\.[^.]+$", "").toLowerCase(Locale.ROOT);
            Path found = files.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).matches(".*\\.(png|jpe?g|webp)$"))
                    .filter(p -> p.getFileName().toString().replaceFirst("\\.[^.]+$", "").toLowerCase(Locale.ROOT).equals(clean)
                            || p.getFileName().toString().toLowerCase(Locale.ROOT).contains(clean)).sorted().findFirst().orElse(null);
            if (found != null) return found;
        } catch (IOException ignored) { }
        throw new IllegalArgumentException("Foto giocatore non trovata per la figurina");
    }

    private Path resolveAsset(String relative) { return assets.resolve(relative); }
    private BufferedImage readRequired(Path path, String message) { try { if (path != null && Files.isRegularFile(path)) return ImageIO.read(path.toFile()); } catch(IOException ignored) { } throw new IllegalArgumentException(message); }
    private BufferedImage resize(BufferedImage source,int w,int h){BufferedImage out=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);Graphics2D g=out.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);g.drawImage(source,0,0,w,h,null);g.dispose();return out;}
    private int[] alphaBounds(BufferedImage image){int minX=image.getWidth(),minY=image.getHeight(),maxX=-1,maxY=-1;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if((image.getRGB(x,y)>>>24)!=0){minX=Math.min(minX,x);minY=Math.min(minY,y);maxX=Math.max(maxX,x);maxY=Math.max(maxY,y);}return maxX<minX?null:new int[]{minX,minY,maxX+1,maxY+1};}
    private void drawTop(Graphics2D g,String text,Font font,int x,int top,Color color){g.setFont(font);g.setColor(color);g.drawString(text,x,top+g.getFontMetrics().getAscent());}
    private void drawCentered(Graphics2D g,String text,Font font,int x,int y,int w,int h,Color color){g.setFont(font);var m=g.getFontMetrics(font);g.setColor(color);g.drawString(text,x+(w-m.stringWidth(text))/2,y+(h-m.getHeight())/2+m.getAscent());}
    private Font fit(GraphicCanvas c,String text,String key,int max,int start,int min){Font f=c.font(key,start);for(int s=start;s>=min;s-=2){f=c.font(key,s);if(c.graphics().getFontMetrics(f).stringWidth(text)<=max)break;}return f;}
    private String text(String value,String fallback){return value==null||value.isBlank()?fallback:value;}
}
