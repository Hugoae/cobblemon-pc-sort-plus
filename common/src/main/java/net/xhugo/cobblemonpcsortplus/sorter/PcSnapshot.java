package net.xhugo.cobblemonpcsortplus.sorter;

import com.cobblemon.mod.common.api.storage.StoreCoordinates;
import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Occupied PC slots captured before a sort, restored with Cobblemon swaps. */
public final class PcSnapshot {
    private static final int SLOTS_PER_BOX = 30;
    private final List<Slot> slots;

    private PcSnapshot(List<Slot> slots) {
        this.slots = List.copyOf(slots);
    }

    public static PcSnapshot capture(PCStore pc) {
        List<Slot> slots = new ArrayList<>();
        for (int box = 0; box < pc.getBoxes().size(); box++) {
            for (int slot = 0; slot < SLOTS_PER_BOX; slot++) {
                Pokemon pokemon = pc.get(new PCPosition(box, slot));
                if (pokemon != null) slots.add(new Slot(pokemon.getUuid(), box, slot));
            }
        }
        return new PcSnapshot(slots);
    }

    public static PcSnapshot fromJson(String json) {
        try {
            JsonElement parsed = JsonParser.parseString(json);
            if (!parsed.isJsonObject()) return null;
            JsonElement rawSlots = parsed.getAsJsonObject().get("slots");
            if (rawSlots == null || !rawSlots.isJsonArray()) return null;
            List<Slot> slots = new ArrayList<>();
            for (JsonElement entry : rawSlots.getAsJsonArray()) {
                if (!entry.isJsonObject()) continue;
                JsonObject object = entry.getAsJsonObject();
                UUID uuid = UUID.fromString(object.get("uuid").getAsString());
                int box = object.get("box").getAsInt();
                int slot = object.get("slot").getAsInt();
                slots.add(new Slot(uuid, box, slot));
            }
            return new PcSnapshot(slots);
        } catch (Exception ignored) {
            return null;
        }
    }

    public String toJson() {
        JsonArray array = new JsonArray();
        for (Slot slot : slots) {
            JsonObject object = new JsonObject();
            object.addProperty("uuid", slot.uuid().toString());
            object.addProperty("box", slot.box());
            object.addProperty("slot", slot.slot());
            array.add(object);
        }
        JsonObject root = new JsonObject();
        root.add("slots", array);
        return root.toString();
    }

    /** Puts every still-present Pokémon back in its captured slot. */
    public int restore(PCStore pc) {
        int moved = 0;
        for (Slot slot : slots) {
            Pokemon pokemon = pc.get(slot.uuid());
            if (pokemon == null) continue;
            PCPosition desired = new PCPosition(slot.box(), slot.slot());
            if (!isValid(pc, desired)) continue;
            PCPosition current = currentPosition(pokemon);
            if (current == null) continue;
            if (current.getBox() == desired.getBox() && current.getSlot() == desired.getSlot()) continue;
            pc.swap(current, desired);
            moved++;
        }
        return moved;
    }

    private static PCPosition currentPosition(Pokemon pokemon) {
        StoreCoordinates<?> coordinates = pokemon.getStoreCoordinates().get();
        if (coordinates == null || !(coordinates.getStore() instanceof PCStore)) return null;
        Object position = coordinates.getPosition();
        return position instanceof PCPosition pcPosition ? pcPosition : null;
    }

    private static boolean isValid(PCStore pc, PCPosition position) {
        return position.getBox() >= 0
                && position.getBox() < pc.getBoxes().size()
                && position.getSlot() >= 0
                && position.getSlot() < SLOTS_PER_BOX;
    }

    record Slot(UUID uuid, int box, int slot) {
    }
}
