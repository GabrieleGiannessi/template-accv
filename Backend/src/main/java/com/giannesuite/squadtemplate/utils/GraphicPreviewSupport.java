package com.giannesuite.squadtemplate.utils;

import com.giannesuite.squadtemplate.service.dto.RenderedGraphicDto;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public final class GraphicPreviewSupport {
    private GraphicPreviewSupport() { }

    public static RenderedGraphicDto asPng(BufferedImage image, String format) throws IOException {
        return asPng(image, format, null);
    }

    public static RenderedGraphicDto asPng(BufferedImage image, String format, String bgPath) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        String uri = "data:" + Constants.PNG_MEDIA_TYPE + ";base64," + Base64.getEncoder().encodeToString(output.toByteArray());
        return new RenderedGraphicDto("ok", uri, image.getWidth(), image.getHeight(), format, bgPath);
    }
}
