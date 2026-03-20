package net.momirealms.craftengine.bukkit.nms;

import net.momirealms.craftengine.bukkit.world.gen.InjectedChunkGenerator;
import net.momirealms.craftengine.core.block.StatePropertyAccessor;
import net.momirealms.craftengine.core.item.recipe.*;
import net.momirealms.craftengine.core.plugin.network.ConnectionState;
import net.momirealms.craftengine.core.plugin.network.PacketFlow;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.InjectedWorldCallback;
import net.momirealms.craftengine.core.world.chunk.InjectedStorage;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public abstract class FastNMS {
    public static final FastNMS INSTANCE = instance();

    private static FastNMS instance() {
        try {
            String path = getImplPath();
            Class<?> clazz = Class.forName("net.momirealms.craftengine.bukkit.nms." + path + ".FastNMSImpl");
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (FastNMS) constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to initialize craftengine nms helper", e);
        }
    }

    private static @NotNull String getImplPath() throws IllegalAccessException {
        String version = VersionHelper.MINECRAFT_VERSION.version();
        return switch (version) {
            case "1.21.11" -> "v1_21_11";
            case "1.21.9", "1.21.10" -> "v1_21_9";
            case "1.21.6", "1.21.7", "1.21.8" -> "v1_21_6";
            case "1.21.5" -> "v1_21_5";
            case "1.21.4" -> "v1_21_4";
            case "1.21.2", "1.21.3" -> "v1_21_2";
            case "1.21", "1.21.1" -> "v1_21";
            case "1.20.5", "1.20.6" -> "v1_20_5";
            case "1.20.3", "1.20.4" -> "v1_20_3";
            case "1.20.2" -> "v1_20_2";
            case "1.20", "1.20.1" -> "v1_20";
            default -> throw new UnsupportedVersionException(version);
        };
    }

    public abstract Object createBiomePlacementFilter(Predicate<Key> filter);

    public abstract InjectedWorldCallback createInjectedWorldCallbacks(Object worldCallback, Object entityLookup);

    public abstract StatePropertyAccessor createStatePropertyAccessor(Object blockState);

    public abstract Object toMinecraftIngredient(Ingredient ingredient);

    public abstract Object getCraftEngineLootItemType();

    public abstract Object getCraftEngineCustomSimpleStateProviderType();

    public abstract Object getCraftEngineCustomWeightedStateProviderType();

    public abstract Object getCraftEngineCustomRotatedBlockProviderType();

    public abstract Object getCraftEngineCustomRandomizedIntStateProviderType();

    public abstract Object getCraftEngineCustomSimpleBlockFeature();

    public abstract InjectedStorage.Palette createInjectedPalettedContainer(Object palettedContainer);

    public abstract InjectedStorage.Section createInjectedLevelChunkSection(Object levelChunkSection);

    public abstract InjectedChunkGenerator createInjectedChunkGenerator(CEWorld world, Object generator);

    public abstract CollisionEntity createCollisionBoat(Object world, Object aabb,
                                                        double x, double y, double z, boolean canProjectileHit, boolean canCollide, boolean blocksBuilding);

    public abstract CollisionEntity createCollisionInteraction(Object world, Object aabb,
                                                               double x, double y, double z, boolean canProjectileHit, boolean canCollide, boolean blocksBuilding);

    public abstract Object createShapedRecipe(CustomShapedRecipe recipe);

    public abstract Object createShapelessRecipe(CustomShapelessRecipe recipe);

    public abstract Object createSmokingRecipe(CustomSmokingRecipe recipe);

    public abstract Object createSmeltingRecipe(CustomSmeltingRecipe recipe);

    public abstract Object createBlastingRecipe(CustomBlastingRecipe recipe);

    public abstract Object createCampfireRecipe(CustomCampfireRecipe recipe);

    public abstract Object createStonecuttingRecipe(CustomStoneCuttingRecipe recipe);

    public abstract Object createSmithingTransformRecipe(CustomSmithingTransformRecipe recipe);

    public abstract Object createSmithingTrimRecipe(CustomSmithingTrimRecipe recipe);

    public abstract Object createInjectedFallingBlockEntity(Object level, Object pos, Object blockState);

    public abstract Object createInjectedFurnaceCachedCheck(Object recipeType, Object blockEntity);

    public abstract Map<ConnectionState, Map<PacketFlow, Map<Class<?>, Integer>>> gamePacketIdsByClazz();

    public abstract Map<ConnectionState, Map<PacketFlow, Map<String, Integer>>> gamePacketIdsByName();

    public abstract Inventory createSimpleStorageContainer(InventoryHolder owner, int size, boolean canPlaceItem, boolean canTakeItem);

    public abstract Object createInjectedHashedStack(Object hashedStack, net.momirealms.craftengine.core.entity.player.Player player);

    public abstract Object createAlwaysStatePredicate(boolean trueOrFalse);

    public abstract Object createUntrustedItemCodec();
}
