package com.giannesuite.squadtemplate.utils;

import java.text.Normalizer;
import java.util.Locale;

/** Metodi semplici condivisi da più moduli. */
public final class Utilities {
    private Utilities() { }

    public static String normalizeText(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('_', ' ').replace('-', ' ');
    }
}
