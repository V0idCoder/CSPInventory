package com.cspinventory.util;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ModelImageResolver {

    private static final String BASE_PATH = "/com/cspinventory/assets/models/";
    private static final List<String> EXTENSIONS = List.of(".png", ".jpg", ".jpeg", ".webp");

    private ModelImageResolver() {
    }

    public static Image resolve(String model) {
        if (model == null || model.isBlank()) {
            return null;
        }

        String cleaned = model.trim();
        for (String fileName : buildCandidates(cleaned)) {
            String resourcePath = BASE_PATH + fileName;
            try (InputStream in = ModelImageResolver.class.getResourceAsStream(resourcePath)) {
                if (in == null) {
                    continue;
                }
                return new Image(in);
            } catch (Exception ignored) {
                // keep searching next candidate
            }
        }
        return null;
    }

    private static List<String> buildCandidates(String model) {
        List<String> candidates = new ArrayList<>();
        candidates.add(model);

        String lower = model.toLowerCase(Locale.ROOT);
        boolean hasExt = EXTENSIONS.stream().anyMatch(lower::endsWith);
        if (!hasExt) {
            for (String ext : EXTENSIONS) {
                candidates.add(model + ext);
            }
        }
        return candidates;
    }
}
