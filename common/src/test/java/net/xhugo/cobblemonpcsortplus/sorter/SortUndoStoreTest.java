package net.xhugo.cobblemonpcsortplus.sorter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SortUndoStoreTest {
    @TempDir
    Path worldRoot;

    @Test
    void snapshotRoundtripsThroughJson() {
        UUID first = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID second = UUID.fromString("22222222-2222-2222-2222-222222222222");
        String json = "{\"slots\":["
                + "{\"uuid\":\"" + first + "\",\"box\":0,\"slot\":3},"
                + "{\"uuid\":\"" + second + "\",\"box\":2,\"slot\":29}"
                + "]}";
        PcSnapshot snapshot = PcSnapshot.fromJson(json);
        assertNotNull(snapshot);
        PcSnapshot restored = PcSnapshot.fromJson(snapshot.toJson());
        assertNotNull(restored);
        assertEquals(snapshot.toJson(), restored.toJson());
    }

    @Test
    void corruptJsonIsRejected() {
        assertEquals(null, PcSnapshot.fromJson("{not json"));
        assertEquals(null, PcSnapshot.fromJson("{}"));
    }

    @Test
    void snapshotSurvivesAMemoryDropTheWayLogoutDoes() {
        UUID player = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        PcSnapshot snapshot = PcSnapshot.fromJson(
                "{\"slots\":[{\"uuid\":\"11111111-1111-1111-1111-111111111111\",\"box\":1,\"slot\":4}]}"
        );
        SortUndoStore.replace(player, snapshot, worldRoot);
        assertTrue(Files.exists(SortUndoStore.undoFile(worldRoot, player)));

        SortUndoStore.dropMemory();
        assertTrue(SortUndoStore.has(player, worldRoot));

        PcSnapshot taken = SortUndoStore.take(player, worldRoot);
        assertNotNull(taken);
        assertEquals(snapshot.toJson(), taken.toJson());
        assertFalse(SortUndoStore.has(player, worldRoot));
        assertFalse(Files.exists(SortUndoStore.undoFile(worldRoot, player)));
    }
}
