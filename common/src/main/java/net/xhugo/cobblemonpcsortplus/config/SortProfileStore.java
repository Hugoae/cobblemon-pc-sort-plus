package net.xhugo.cobblemonpcsortplus.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.xhugo.cobblemonpcsortplus.CobblemonPCSortPlus;
import net.xhugo.cobblemonpcsortplus.platform.Services;

/** Persists profiles independently from Cobblemon's PC save files. */
public final class SortProfileStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private SortProfileStore() {
    }

    public static SortProfile load(UUID playerId) {
        Path profilePath = profilePath(playerId);
        if (!Files.exists(profilePath)) {
            SortProfile profile = SortProfile.defaults();
            save(playerId, profile);
            return profile;
        }

        try (Reader reader = Files.newBufferedReader(profilePath, StandardCharsets.UTF_8)) {
            return parseProfile(reader);
        } catch (Exception exception) {
            CobblemonPCSortPlus.LOGGER.error("Could not read profile {}: {}", profilePath, exception.getMessage());
            return SortProfile.defaults();
        }
    }

    public static boolean save(UUID playerId, SortProfile profile) {
        profile.normalize();
        Path profilePath = profilePath(playerId);
        Path temporaryPath = profilePath.resolveSibling(profilePath.getFileName() + ".tmp");
        try {
            Files.createDirectories(profileDirectory());
            try (Writer writer = Files.newBufferedWriter(temporaryPath, StandardCharsets.UTF_8)) {
                GSON.toJson(profile, writer);
            }
            try {
                Files.move(temporaryPath, profilePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporaryPath, profilePath, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException exception) {
            CobblemonPCSortPlus.LOGGER.error("Could not save profile {}: {}", profilePath, exception.getMessage());
            return false;
        }
    }

    public static String serialize(SortProfile profile) {
        profile.normalize();
        return GSON.toJson(profile);
    }

    public static SortProfile deserialize(String json) {
        try {
            return parseProfile(new StringReader(json));
        } catch (Exception exception) {
            return SortProfile.defaults();
        }
    }

    /**
     * Reads the current array-of-tags map. Also accepts the original single-string
     * {@code boxTags} values and Fabric-era {@code sourceBoxes}/{@code fallbackBox} fields.
     */
    private static SortProfile parseProfile(Reader reader) {
        JsonElement parsed = JsonParser.parseReader(reader);
        if (!parsed.isJsonObject()) return SortProfile.defaults();
        JsonObject root = parsed.getAsJsonObject();
        JsonElement rawTags = root.remove("boxTags");
        JsonElement legacySources = root.remove("sourceBoxes");
        JsonElement legacyFallback = root.remove("fallbackBox");
        SortProfile profile = GSON.fromJson(root, SortProfile.class);
        if (profile == null) profile = SortProfile.defaults();

        if (rawTags != null && rawTags.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : rawTags.getAsJsonObject().entrySet()) {
                int box;
                try {
                    box = Integer.parseInt(entry.getKey());
                } catch (NumberFormatException ignored) {
                    continue;
                }
                Set<String> tags = new LinkedHashSet<>();
                if (entry.getValue().isJsonPrimitive()) {
                    tags.add(entry.getValue().getAsString());
                } else if (entry.getValue().isJsonArray()) {
                    entry.getValue().getAsJsonArray().forEach(tag -> tags.add(tag.getAsString()));
                }
                profile.setTagsForBox(box, tags);
            }
        }
        if (legacySources != null && legacySources.isJsonArray()) {
            for (JsonElement box : legacySources.getAsJsonArray()) {
                int boxNumber = box.getAsInt();
                Set<String> tags = profile.tagsForBox(boxNumber);
                tags.add("SOURCE");
                profile.setTagsForBox(boxNumber, tags);
            }
        }
        if (legacyFallback != null && legacyFallback.isJsonPrimitive()) {
            int boxNumber = legacyFallback.getAsInt();
            Set<String> tags = profile.tagsForBox(boxNumber);
            tags.add("FALLBACK");
            profile.setTagsForBox(boxNumber, tags);
        }
        profile.normalize();
        return profile;
    }

    private static Path profileDirectory() {
        return Services.PLATFORM.configDirectory().resolve(CobblemonPCSortPlus.MOD_ID).resolve("profiles");
    }

    private static Path profilePath(UUID playerId) {
        return profileDirectory().resolve(playerId + ".json");
    }
}
