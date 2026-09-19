package net.xhugo.cobblemonpcsortplus.sorter;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonSortMode;
import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.storage.pc.PCBox;
import com.cobblemon.mod.common.api.storage.pc.PCPosition;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.PokemonSizeCategory;
import com.cobblemon.mod.common.pokemon.properties.HiddenAbilityProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.xhugo.cobblemonpcsortplus.config.BoxSortMode;
import net.xhugo.cobblemonpcsortplus.config.SortProfile;

/**
 * Moves Pokémon by swapping PC slots. Cobblemon owns storage and observer packets;
 * this class never writes Pokémon NBT itself.
 */
public final class PCSorter {
    private static final int SLOTS_PER_BOX = 30;
    private static final Set<String> TYPE_TAGS = Set.of(
            "NORMAL", "FIRE", "WATER", "ELECTRIC", "GRASS", "ICE", "FIGHTING", "POISON",
            "GROUND", "FLYING", "PSYCHIC", "BUG", "ROCK", "GHOST", "DRAGON", "DARK",
            "STEEL", "FAIRY"
    );

    private PCSorter() {
    }

    /** Moves only Pokémon that start in a Source box. */
    public static SortResult sort(ServerPlayer player, SortProfile profile) {
        return sort(player, profile, false);
    }

    /**
     * Sorts the whole PC and compacts toward the best matching destination
     * (more tags, then stricter High IV, then lower box number).
     */
    public static SortResult sortAll(ServerPlayer player, SortProfile profile) {
        return sort(player, profile, true);
    }

    private static SortResult sort(ServerPlayer player, SortProfile profile, boolean allBoxes) {
        profile.normalize();
        if (!profile.enabled) return new SortResult(0, 0, 0, 0);

        PCStore pc = Cobblemon.INSTANCE.getStorage().getPC(player);
        List<InitialPosition> positions = new ArrayList<>();
        int skippedInvalidBoxes = 0;

        if (allBoxes) {
            for (int box = 0; box < pc.getBoxes().size(); box++) {
                if (profile.isProtectedBox(box + 1)) continue;
                collectOccupiedSlots(pc, box, positions);
            }
        } else {
            for (int sourceBoxNumber : boxesWithTag(profile.boxTags, SortProfile.SOURCE_TAG)) {
                if (profile.isProtectedBox(sourceBoxNumber)) continue;
                int sourceBox = sourceBoxNumber - 1;
                if (!isValidBox(pc, sourceBox)) {
                    skippedInvalidBoxes++;
                    continue;
                }
                collectOccupiedSlots(pc, sourceBox, positions);
            }
        }

        int moved = 0;
        int unmatched = 0;
        int fullDestinations = 0;
        for (InitialPosition initial : positions) {
            PCPosition source = new PCPosition(initial.box(), initial.slot());
            Pokemon pokemon = pc.get(source);
            if (pokemon == null) continue;

            int destinationBox = findDestinationBox(pc, pokemon, initial.box(), profile);
            if (destinationBox == BoxRouter.NONE) {
                unmatched++;
                continue;
            }
            if (destinationBox == BoxRouter.FULL) {
                fullDestinations++;
                continue;
            }
            if (destinationBox == BoxRouter.STAY) {
                continue;
            }

            PCPosition destination = firstEmptySlot(pc, destinationBox - 1);
            if (destination == null) {
                fullDestinations++;
                continue;
            }
            pc.swap(source, destination);
            moved++;
        }
        if (allBoxes) sortEveryBox(pc, player, profile.finalSortMode(), profile);
        return new SortResult(moved, unmatched, fullDestinations, skippedInvalidBoxes);
    }

    /** Uses Cobblemon's built-in comparator and sends the normal box update. */
    private static void sortEveryBox(
            PCStore pc,
            ServerPlayer player,
            BoxSortMode configuredMode,
            SortProfile profile
    ) {
        PokemonSortMode mode = switch (configuredMode) {
            case NAME -> PokemonSortMode.NAME;
            case POKEDEX -> PokemonSortMode.POKEDEX_NUMBER;
            case LEVEL -> PokemonSortMode.LEVEL;
            case NONE -> null;
        };
        if (mode == null) return;
        for (int boxIndex = 0; boxIndex < pc.getBoxes().size(); boxIndex++) {
            if (profile.isProtectedBox(boxIndex + 1)) continue;
            PCBox box = pc.getBoxes().get(boxIndex);
            if (!box.iterator().hasNext()) continue;
            box.sort(mode, false);
            box.sendTo(player);
        }
    }

    private static void collectOccupiedSlots(PCStore pc, int box, List<InitialPosition> positions) {
        for (int slot = 0; slot < SLOTS_PER_BOX; slot++) {
            if (pc.get(new PCPosition(box, slot)) != null) positions.add(new InitialPosition(box, slot));
        }
    }

    /** Returns a one-based destination, or {@link BoxRouter} sentinels. */
    private static int findDestinationBox(PCStore pc, Pokemon pokemon, int sourceBox, SortProfile profile) {
        return BoxRouter.pick(
                profile,
                sourceBox + 1,
                countPerfectIvs(pokemon.getIvs()),
                tag -> matchesTag(pokemon, tag),
                box -> isValidBox(pc, box - 1),
                box -> firstEmptySlot(pc, box - 1) != null
        );
    }

    private static List<Integer> boxesWithTag(Map<Integer, ? extends Set<String>> boxTags, String tag) {
        String desired = SortProfile.normaliseTag(tag);
        List<Integer> boxes = new ArrayList<>();
        boxTags.forEach((box, configuredTags) -> {
            if (configuredTags.stream().map(SortProfile::normaliseTag).anyMatch(desired::equals)) boxes.add(box);
        });
        boxes.sort(Integer::compareTo);
        return boxes;
    }

    private static boolean matchesTag(Pokemon pokemon, String unnormalisedTag) {
        String tag = SortProfile.normaliseTag(unnormalisedTag);
        return switch (tag) {
            case "SHINY" -> pokemon.getShiny();
            case "ALPHA" -> pokemon.isAlpha();
            case "LEGENDARY" -> pokemon.isLegendary();
            case "MYTHICAL" -> pokemon.isMythical();
            case "HIDDEN_ABILITY" -> hasHiddenAbility(pokemon);
            case "MALE" -> pokemon.getGender() == Gender.MALE;
            case "FEMALE" -> pokemon.getGender() == Gender.FEMALE;
            case "HELD_ITEM" -> !pokemon.heldItem().isEmpty();
            case "FOSSIL" -> hasTaxonomicLabel(pokemon, CobblemonPokemonLabels.FOSSIL);
            case "PSEUDO_LEGENDARY" -> hasTaxonomicLabel(pokemon, CobblemonPokemonLabels.POWERHOUSE);
            case "ULTRA_BEAST" -> pokemon.isUltraBeast()
                    || hasTaxonomicLabel(pokemon, CobblemonPokemonLabels.ULTRA_BEAST);
            case "PARADOX" -> hasTaxonomicLabel(pokemon, CobblemonPokemonLabels.PARADOX);
            case "BABY" -> hasTaxonomicLabel(pokemon, CobblemonPokemonLabels.BABY);
            case "REGIONAL" -> isRegionalForm(pokemon);
            case "LEVEL_1" -> pokemon.getLevel() == 1;
            case "LEVEL_100" -> pokemon.getLevel() >= Cobblemon.INSTANCE.getConfig().getMaxPokemonLevel();
            case "GEN_1" -> pokemon.hasLabels(CobblemonPokemonLabels.GENERATION_1);
            case "GEN_2" -> pokemon.hasLabels(CobblemonPokemonLabels.GENERATION_2);
            case "GEN_3" -> pokemon.hasLabels(CobblemonPokemonLabels.GENERATION_3);
            case "GEN_4" -> pokemon.hasLabels(CobblemonPokemonLabels.GENERATION_4);
            case "GEN_5" -> pokemon.hasLabels(CobblemonPokemonLabels.GENERATION_5);
            case "GEN_6" -> pokemon.hasLabels(CobblemonPokemonLabels.GENERATION_6);
            case "GEN_7" -> pokemon.hasLabels(CobblemonPokemonLabels.GENERATION_7)
                    || pokemon.hasLabels(CobblemonPokemonLabels.GENERATION_7B);
            case "GEN_8" -> pokemon.hasLabels(CobblemonPokemonLabels.GENERATION_8)
                    || pokemon.hasLabels(CobblemonPokemonLabels.GENERATION_8A);
            case "GEN_9" -> pokemon.hasLabels(CobblemonPokemonLabels.GENERATION_9);
            case "SIZE_XS" -> pokemon.getSizeCategory() == PokemonSizeCategory.XS;
            case "SIZE_S" -> pokemon.getSizeCategory() == PokemonSizeCategory.S;
            case "SIZE_M" -> pokemon.getSizeCategory() == PokemonSizeCategory.M;
            case "SIZE_L" -> pokemon.getSizeCategory() == PokemonSizeCategory.L;
            case "SIZE_XL" -> pokemon.getSizeCategory() == PokemonSizeCategory.XL;
            default -> TYPE_TAGS.contains(tag) && containsType(pokemon, tag);
        };
    }

    private static int countPerfectIvs(IVs ivs) {
        int perfect = 0;
        for (Stats stat : new Stats[] {
                Stats.HP, Stats.ATTACK, Stats.DEFENCE,
                Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Stats.SPEED
        }) {
            if (ivs.get(stat) == IVs.MAX_VALUE) perfect++;
        }
        return perfect;
    }

    private static boolean hasHiddenAbility(Pokemon pokemon) {
        return new HiddenAbilityProperty(true).matches(pokemon);
    }

    /**
     * Form labels replace species labels when present, so Mega Tyranitar would
     * otherwise drop {@code powerhouse}. Taxonomic chips also check the species.
     */
    private static boolean hasTaxonomicLabel(Pokemon pokemon, String label) {
        if (pokemon.hasLabels(label)) return true;
        for (String speciesLabel : pokemon.getSpecies().getLabels()) {
            if (speciesLabel.equalsIgnoreCase(label)) return true;
        }
        return false;
    }

    /** Alola / Galar / Hisui / Paldea forms only. Native-region tags like kantonian_form do not count. */
    private static boolean isRegionalForm(Pokemon pokemon) {
        return pokemon.hasLabels(CobblemonPokemonLabels.ALOLAN_FORM)
                || pokemon.hasLabels(CobblemonPokemonLabels.GALARIAN_FORM)
                || pokemon.hasLabels(CobblemonPokemonLabels.HISUIAN_FORM)
                || pokemon.hasLabels(CobblemonPokemonLabels.PALDEAN_FORM);
    }

    private static boolean containsType(Pokemon pokemon, String tag) {
        for (var type : pokemon.getTypes()) {
            if (type.getName().toUpperCase(Locale.ROOT).equals(tag)) return true;
        }
        return false;
    }

    private static boolean isValidBox(PCStore pc, int boxIndex) {
        return boxIndex >= 0 && boxIndex < pc.getBoxes().size();
    }

    private static PCPosition firstEmptySlot(PCStore pc, int boxIndex) {
        if (!isValidBox(pc, boxIndex)) return null;
        for (int slot = 0; slot < SLOTS_PER_BOX; slot++) {
            PCPosition position = new PCPosition(boxIndex, slot);
            if (pc.get(position) == null) return position;
        }
        return null;
    }

    private record InitialPosition(int box, int slot) {
    }
}
