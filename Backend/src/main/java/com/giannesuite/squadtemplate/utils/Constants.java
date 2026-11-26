package com.giannesuite.squadtemplate.utils;

import java.awt.Color;
import java.util.Set;

/** Costanti condivise tra i moduli dell'applicazione. */
public final class Constants {
    public static final Set<String> SUPPORTED_IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");
    public static final int MAX_UPLOAD_BYTES = 20 * 1024 * 1024;
    public static final String PNG_MEDIA_TYPE = "image/png";
    public static final Color CYAN = new Color(0, 229, 255);
    public static final Color GOLD = new Color(255, 193, 7);
    public static final Color WHITE = Color.WHITE;
    public static final Color MUTED = new Color(130, 140, 165);
    public static final Color CARD_BG = new Color(28, 34, 52, 220);
    public static final Color CARD_BORDER = new Color(55, 65, 95, 180);

    private Constants() { }
}
