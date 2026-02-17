package com.cspinventory.util;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModelImageResolver {

    private static final String BASE_PATH = "/com/cspinventory/assets/models/";
    private static final List<String> EXTENSIONS = List.of(".png", ".jpg", ".jpeg", ".webp");
    private static final int CACHE_LIMIT = 256;
    private static final Map<String, Optional<Image>> IMAGE_CACHE = Collections.synchronizedMap(
            new LinkedHashMap<>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Optional<Image>> eldest) {
                    return size() > CACHE_LIMIT;
                }
            }
    );
    private static volatile Path externalModelsDir;

    private ModelImageResolver() {
    }

    public static Image resolve(String model) {
        if (model == null || model.isBlank()) {
            return null;
        }

        String cleaned = model.trim();
        String cacheKey = buildCacheKey(cleaned);
        Optional<Image> cached = IMAGE_CACHE.get(cacheKey);
        if (cached != null) {
            return cached.orElse(null);
        }

        Image externalImage = resolveFromExternalDirectory(cleaned);
        if (externalImage != null) {
            IMAGE_CACHE.put(cacheKey, Optional.of(externalImage));
            return externalImage;
        }

        for (String fileName : buildCandidates(cleaned)) {
            String resourcePath = BASE_PATH + fileName;
            try (InputStream in = ModelImageResolver.class.getResourceAsStream(resourcePath)) {
                if (in == null) {
                    continue;
                }
                Image resolved = new Image(in);
                IMAGE_CACHE.put(cacheKey, Optional.of(resolved));
                return resolved;
            } catch (Exception ignored) {
                // keep searching next candidate
            }
        }
        IMAGE_CACHE.put(cacheKey, Optional.empty());
        return null;
    }

    public static void configureExternalModelsDirectory(Path directory) {
        externalModelsDir = directory == null ? null : directory.toAbsolutePath().normalize();
        clearCache();
    }

    public static void clearCache() {
        IMAGE_CACHE.clear();
    }

    private static Image resolveFromExternalDirectory(String model) {
        Path baseDir = externalModelsDir;
        if (baseDir == null) {
            return null;
        }

        for (String fileName : buildCandidates(model)) {
            Path candidate = baseDir.resolve(fileName);
            if (!Files.isRegularFile(candidate)) {
                continue;
            }
            try {
                return new Image(candidate.toUri().toString());
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

    private static String buildCacheKey(String model) {
        Path baseDir = externalModelsDir;
        String root = baseDir == null ? "" : baseDir.toString();
        return root + "|" + model.toLowerCase(Locale.ROOT);
    }
}
