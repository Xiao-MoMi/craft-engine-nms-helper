package net.momirealms.craftengine.bukkit.nms;

import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import net.momirealms.craftengine.core.block.StatePropertyAccessor;
import net.momirealms.craftengine.core.item.recipe.*;
import net.momirealms.craftengine.core.plugin.network.ConnectionState;
import net.momirealms.craftengine.core.plugin.network.PacketFlow;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.chunk.InjectedHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.*;
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

    public abstract Object createInjectedEntityCallbacks(Object worldCallback, Object entityLookup);

    public abstract StatePropertyAccessor createStatePropertyAccessor(Object blockState);

    public abstract Object toMinecraftIngredient(Ingredient<ItemStack> ingredient);

    public abstract Object getCraftEngineLootItemType();

    public abstract Object getCraftEngineCustomSimpleStateProviderType();

    public abstract Object getCraftEngineCustomWeightedStateProviderType();

    public abstract Object getCraftEngineCustomRotatedBlockProviderType();

    public abstract Object getCraftEngineCustomRandomizedIntStateProviderType();

    public abstract Object getCraftEngineCustomSimpleBlockFeature();

    public abstract InjectedHolder.Palette createInjectedPalettedContainerHolder(Object palettedContainer) throws InstantiationException;

    public abstract InjectedHolder.Section createInjectedLevelChunkSectionHolder(Object levelChunkSection);

    public abstract void injectedWorldGen(CEWorld world, Object chunkMap);

    public abstract CollisionEntity createCollisionBoat(Object world, Object aabb,
                                                        double x, double y, double z, boolean canProjectileHit, boolean canCollide, boolean blocksBuilding);

    public abstract CollisionEntity createCollisionInteraction(Object world, Object aabb,
                                                               double x, double y, double z, boolean canProjectileHit, boolean canCollide, boolean blocksBuilding);

    public abstract Object createShapedRecipe(CustomShapedRecipe<ItemStack> recipe);

    public abstract Object createShapelessRecipe(CustomShapelessRecipe<ItemStack> recipe);

    public abstract Object createSmokingRecipe(CustomSmokingRecipe<ItemStack> recipe);

    public abstract Object createSmeltingRecipe(CustomSmeltingRecipe<ItemStack> recipe);

    public abstract Object createBlastingRecipe(CustomBlastingRecipe<ItemStack> recipe);

    public abstract Object createCampfireRecipe(CustomCampfireRecipe<ItemStack> recipe);

    public abstract Object createStonecuttingRecipe(CustomStoneCuttingRecipe<ItemStack> recipe);

    public abstract Object createSmithingTransformRecipe(CustomSmithingTransformRecipe<ItemStack> recipe);

    public abstract Object createSmithingTrimRecipe(CustomSmithingTrimRecipe<ItemStack> recipe);

    public abstract Object createInjectedFallingBlockEntity(Object level, Object pos, Object blockState);

    public abstract void simulateInteraction(Object player, Object direction, double x, double y, double z, Object pos);

    public abstract boolean checkEntityCollision(Object level, List<Object> aabbs, Predicate<Object> entityFilter);

    public abstract String getCustomItemId(Object itemStack);

    public abstract void setCustomItemId(Object itemStack, String id);

    public abstract Map<ConnectionState, Map<PacketFlow, Map<Class<?>, Integer>>> gamePacketIdsByClazz();

    public abstract Map<ConnectionState, Map<PacketFlow, Map<String, Integer>>> gamePacketIdsByName();

    public abstract Inventory createSimpleStorageContainer(InventoryHolder owner, int size, boolean canPlaceItem, boolean canTakeItem);

    public abstract Object constructor$InjectedHashedStack(Object hashedStack, net.momirealms.craftengine.core.entity.player.Player player);

    public abstract Object method$StatePredicate$always(boolean trueOrFalse);

    // todo 修改下面的

    public abstract Object field$RecipeHolder$id(Object recipeHolder);

    public abstract boolean method$ServerLevel$isPreventingStatusUpdates(Object serverLevel, int x, int z);

    public abstract void method$Connection$send(Object connection, Object packet, Object sendListener);

    public abstract Object method$Component$Serializer$fromJson(JsonElement element);

    public abstract Object method$Component$Serializer$fromJson(String json);

    public abstract String method$Component$Serializer$toJson(Object component);

    public abstract void method$SoundEvent$directEncode(ByteBuf buffer, Object soundEvent);

    public abstract double method$Player$getInteractionRange(Object player);

    public abstract boolean method$ItemStack$canBreakInAdventureMode(Object itemStack, Object blockInWorld);

    public abstract boolean method$ItemStack$canPlaceInAdventureMode(Object itemStack, Object blockInWorld);

    public abstract boolean method$LightEngine$hasDifferentLightProperties(Object oldState, Object newState);

    public abstract ItemStack method$FriendlyByteBuf$readItem(Object buf);

    public abstract void method$FriendlyByteBuf$writeItem(Object buf, ItemStack itemStack);

    public abstract ItemStack method$FriendlyByteBuf$readUntrustedItem(Object buf);

    public abstract void method$FriendlyByteBuf$writeUntrustedItem(Object buf, ItemStack itemStack);

}
