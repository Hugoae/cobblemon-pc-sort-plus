package net.xhugo.cobblemonpcsortplus.sorter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.xhugo.cobblemonpcsortplus.CobblemonPCSortPlus;

/** One-level last-sort snapshots. Memory cache plus a file in the world save. */
public final class SortUndoStore {
    private static final Map<UUID, PcSnapshot> SNAPSHOTS = new ConcurrentHashMap<>();

    private SortUndoStore() {
    }

    public static void replace(UUID playerId, PcSnapshot snapshot, Path worldRoot) {
        SNAPSHOTS.put(playerId, snapshot);
        write(worldRoot, playerId, snapshot);
    }

    public static PcSnapshot take(UUID playerId, Path worldRoot) {
        PcSnapshot snapshot = SNAPSHOTS.remove(playerId);
        if (snapshot == null) snapshot = read(worldRoot, playerId);
        delete(worldRoot, playerId);
        return snapshot;
    }

    public static boolean has(UUID playerId, Path worldRoot) {
        if (SNAPSHOTS.containsKey(playerId)) return true;
        PcSnapshot snapshot = read(worldRoot, playerId);
        if (snapshot == null) return false;
        SNAPSHOTS.put(playerId, snapshot);
        return true;
    }

    public static void dropPlayer(UUID playerId) {
        SNAPSHOTS.remove(playerId);
    }

    public static void dropAll() {
        SNAPSHOTS.clear();
    }

    static void dropMemory() {
        dropAll();
    }

    static Path undoFile(Path worldRoot, UUID playerId) {
        return worldRoot.resolve(CobblemonPCSortPlus.MOD_ID).resolve("undo").resolve(playerId + ".json");
    }

    private static void write(Path worldRoot, UUID playerId, PcSnapshot snapshot) {
        if (worldRoot == null) return;
        Path path = undoFile(worldRoot, playerId);
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(temporary, snapshot.toJson(), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            CobblemonPCSortPlus.LOGGER.error("Could not save undo snapshot {}: {}", path, exception.getMessage());
        }
    }

    private static PcSnapshot read(Path worldRoot, UUID playerId) {
        if (worldRoot == null) return null;
        Path path = undoFile(worldRoot, playerId);
        if (!Files.exists(path)) return null;
        try {
            return PcSnapshot.fromJson(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            CobblemonPCSortPlus.LOGGER.error("Could not read undo snapshot {}: {}", path, exception.getMessage());
            return null;
        }
    }

    private static void delete(Path worldRoot, UUID playerId) {
        if (worldRoot == null) return;
        try {
            Files.deleteIfExists(undoFile(worldRoot, playerId));
        } catch (IOException exception) {
            CobblemonPCSortPlus.LOGGER.error("Could not delete undo snapshot {}: {}", undoFile(worldRoot, playerId), exception.getMessage());
        }
    }
}
