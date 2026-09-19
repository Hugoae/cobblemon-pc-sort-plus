package net.xhugo.cobblemonpcsortplus.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Rebindable keys used only while the Cobblemon PC screen is open. */
public final class CpsKeybinds {
    public static final String CATEGORY = "key.categories.cobblemonpcsortplus";

    public static final KeyMapping SORT = mapping("sort", GLFW.GLFW_KEY_R);
    public static final KeyMapping SORT_ALL = mapping("sort_all", GLFW.GLFW_KEY_T);
    public static final KeyMapping UNDO = mapping("undo", GLFW.GLFW_KEY_Z);

    private CpsKeybinds() {
    }

    public static List<KeyMapping> mappings() {
        return List.of(SORT, SORT_ALL, UNDO);
    }

    public static boolean matches(KeyMapping mapping, int keyCode, int scanCode) {
        return !mapping.isUnbound() && mapping.matches(keyCode, scanCode);
    }

    public static Component withShortcut(String tooltipKey, KeyMapping mapping) {
        Component tooltip = Component.translatable(tooltipKey);
        if (mapping.isUnbound()) return tooltip;
        return tooltip.copy().append("\n").append(
                Component.translatable("screen.cobblemonpcsortplus.shortcut", mapping.getTranslatedKeyMessage())
        );
    }

    private static KeyMapping mapping(String name, int keyCode) {
        return new KeyMapping(
                "key.cobblemonpcsortplus." + name,
                InputConstants.Type.KEYSYM,
                keyCode,
                CATEGORY
        );
    }
}
