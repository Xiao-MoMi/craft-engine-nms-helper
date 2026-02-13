package net.momirealms.craftengine.bukkit.nms.v1_20;

import io.papermc.paper.chunk.system.entity.EntityLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.phys.AABB;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.nms.UnsupportedVersionException;
import net.momirealms.craftengine.bukkit.nms.v1_20.block.PaperStatePropertyAccessor;
import net.momirealms.craftengine.bukkit.nms.v1_20.chunk.InjectedLevelChunkSection;
import net.momirealms.craftengine.bukkit.nms.v1_20.chunk.InjectedPalettedContainer;
import net.momirealms.craftengine.bukkit.nms.v1_20.collision.CollisionBoat;
import net.momirealms.craftengine.bukkit.nms.v1_20.collision.CollisionInteraction;
import net.momirealms.craftengine.bukkit.nms.v1_20.collision.NonCollisionBoat;
import net.momirealms.craftengine.bukkit.nms.v1_20.collision.NonCollisionInteraction;
import net.momirealms.craftengine.bukkit.nms.v1_20.entity.InjectedFallingBlockEntity;
import net.momirealms.craftengine.bukkit.nms.v1_20.entity.InjectedPaperEntityCallbacks;
import net.momirealms.craftengine.bukkit.nms.v1_20.inventory.SimpleStorageContainer;
import net.momirealms.craftengine.bukkit.nms.v1_20.loot.CraftEngineItem;
import net.momirealms.craftengine.bukkit.nms.v1_20.recipe.*;
import net.momirealms.craftengine.bukkit.nms.v1_20.worldgen.*;
import net.momirealms.craftengine.bukkit.world.gen.InjectedChunkGenerator;
import net.momirealms.craftengine.core.block.StatePropertyAccessor;
import net.momirealms.craftengine.core.item.recipe.*;
import net.momirealms.craftengine.core.plugin.network.ConnectionState;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.chunk.InjectedStorage;
import net.momirealms.sparrow.reflection.SReflection;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class FastNMSImpl extends FastNMS {

    @Override
    public Object createBiomePlacementFilter(Predicate<Key> filter) {
        return new BiomeFilter(filter);
    }

    @Override
    public Object createInjectedEntityCallbacks(Object worldCallback, Object entityLookup) {
        if (worldCallback instanceof InjectedPaperEntityCallbacks) return worldCallback;
        return new InjectedPaperEntityCallbacks((LevelCallback<Entity>) worldCallback, (EntityLookup) entityLookup);
    }

    @Override
    public StatePropertyAccessor createStatePropertyAccessor(Object blockState) {
        return new PaperStatePropertyAccessor((BlockState) blockState);
    }

    @Override
    public Object toMinecraftIngredient(net.momirealms.craftengine.core.item.recipe.Ingredient<ItemStack> ingredient) {
        return RecipeHelper.toMinecraft(ingredient);
    }

    @Override
    public Object getCraftEngineLootItemType() {
        return CraftEngineItem.TYPE;
    }

    @Override
    public Object getCraftEngineCustomSimpleStateProviderType() {
        return CustomSimpleStateProvider.TYPE;
    }

    @Override
    public Object getCraftEngineCustomWeightedStateProviderType() {
        return CustomWeightedStateProvider.TYPE;
    }

    @Override
    public Object getCraftEngineCustomRotatedBlockProviderType() {
        return CustomRotatedBlockProvider.TYPE;
    }

    @Override
    public Object getCraftEngineCustomRandomizedIntStateProviderType() {
        return CustomRandomizedIntStateProvider.TYPE;
    }

    @Override
    public Object getCraftEngineCustomSimpleBlockFeature() {
        return CustomSimpleBlockFeature.INSTANCE;
    }

    @Override
    public InjectedStorage.Palette createInjectedPalettedContainer(Object palettedContainer) {
        InjectedPalettedContainer injectedObject = SReflection.allocateInstance(InjectedPalettedContainer.class);
        injectedObject.delegated = (PalettedContainer) palettedContainer;
        return injectedObject;
    }

    @Override
    public InjectedStorage.Section createInjectedLevelChunkSection(Object levelChunkSection) {
        LevelChunkSection section = (LevelChunkSection) levelChunkSection;
        return new InjectedLevelChunkSection(section.getStates(), (PalettedContainer<Holder<Biome>>) section.getBiomes());
    }

    @Override
    public InjectedChunkGenerator createInjectedChunkGenerator(CEWorld world, Object generator) {
        return new InjectedCustomChunkGenerator(world, (ChunkGenerator) generator);
    }

    @Override
    public CollisionEntity createCollisionBoat(Object world, Object aabb, double x, double y, double z, boolean canProjectileHit, boolean canCollide, boolean blocksBuilding) {
        if (canCollide) return new CollisionBoat(EntityType.BOAT, (Level) world, x, y, z, (AABB) aabb, canProjectileHit, blocksBuilding);
        else return new NonCollisionBoat(EntityType.BOAT, (Level) world, x, y, z, (AABB) aabb, canProjectileHit, blocksBuilding);
    }

    @Override
    public CollisionEntity createCollisionInteraction(Object world, Object aabb, double x, double y, double z, boolean canProjectileHit, boolean canCollide, boolean blocksBuilding) {
        if (canCollide) return new CollisionInteraction(EntityType.INTERACTION, (Level) world, x, y, z, (AABB) aabb, canProjectileHit, blocksBuilding);
        else return new NonCollisionInteraction(EntityType.INTERACTION, (Level) world, x, y, z, (AABB) aabb, canProjectileHit, blocksBuilding);
    }

    @Override
    public Object createShapedRecipe(CustomShapedRecipe<ItemStack> recipe) {
        return InjectedShapedRecipe.of(recipe);
    }

    @Override
    public Object createShapelessRecipe(CustomShapelessRecipe<ItemStack> recipe) {
        return InjectedShapelessRecipe.of(recipe);
    }

    @Override
    public Object createSmokingRecipe(CustomSmokingRecipe<ItemStack> recipe) {
        return InjectedSmokingRecipe.of(recipe);
    }

    @Override
    public Object createSmeltingRecipe(CustomSmeltingRecipe<ItemStack> recipe) {
        return InjectedSmeltingRecipe.of(recipe);
    }

    @Override
    public Object createBlastingRecipe(CustomBlastingRecipe<ItemStack> recipe) {
        return InjectedBlastingRecipe.of(recipe);
    }

    @Override
    public Object createCampfireRecipe(CustomCampfireRecipe<ItemStack> recipe) {
        return InjectedCampfireCookingRecipe.of(recipe);
    }

    @Override
    public Object createStonecuttingRecipe(CustomStoneCuttingRecipe<ItemStack> recipe) {
        return InjectedStonecuttingRecipe.of(recipe);
    }

    @Override
    public Object createSmithingTransformRecipe(CustomSmithingTransformRecipe<ItemStack> recipe) {
        return InjectedSmithingTransformRecipe.of(recipe);
    }

    @Override
    public Object createSmithingTrimRecipe(CustomSmithingTrimRecipe<ItemStack> recipe) {
        return InjectedSmithingTrimRecipe.of(recipe);
    }

    @Override
    public Object createInjectedFallingBlockEntity(Object level, Object pos, Object blockState) {
        if (VersionHelper.isFolia()) {
            return FallingBlockEntity.fall((Level) level, (BlockPos) pos, (BlockState) blockState);
        }
        return InjectedFallingBlockEntity.fall((Level) level, (BlockPos) pos, (BlockState) blockState);
    }

    @Override
    public Object createAlwaysStatePredicate(boolean trueOrFalse) {
        return (BlockBehaviour.StatePredicate) (blockState, blockGetter, blockPos) -> trueOrFalse;
    }

    @Override
    public Map<ConnectionState, Map<net.momirealms.craftengine.core.plugin.network.PacketFlow, Map<Class<?>, Integer>>> gamePacketIdsByClazz() {
        Map<ConnectionState, Map<net.momirealms.craftengine.core.plugin.network.PacketFlow, Map<Class<?>, Integer>>> allPacketIdsByClazz = new HashMap<>();
        for (ConnectionProtocol protocol : ConnectionProtocol.values()) {
            ConnectionState state = ConnectionState.valueOf(protocol.name().toUpperCase(Locale.ROOT));
            Map<net.momirealms.craftengine.core.plugin.network.PacketFlow, Map<Class<?>, Integer>> protocolPacketIdsByClazz = new HashMap<>();
            Map<Class<?>, Integer> serverBoundIds = new HashMap<>();
            Map<Class<?>, Integer> clientBoundIds = new HashMap<>();
            protocolPacketIdsByClazz.put(net.momirealms.craftengine.core.plugin.network.PacketFlow.SERVERBOUND, serverBoundIds);
            protocolPacketIdsByClazz.put(net.momirealms.craftengine.core.plugin.network.PacketFlow.CLIENTBOUND, clientBoundIds);
            protocol.getPacketsByIds(PacketFlow.SERVERBOUND).forEach((id, packet) -> serverBoundIds.put(packet, id));
            protocol.getPacketsByIds(PacketFlow.CLIENTBOUND).forEach((id, packet) -> clientBoundIds.put(packet, id));
            allPacketIdsByClazz.put(state, protocolPacketIdsByClazz);
        }
        return allPacketIdsByClazz;
    }

    @Override
    public Map<ConnectionState, Map<net.momirealms.craftengine.core.plugin.network.PacketFlow, Map<String, Integer>>> gamePacketIdsByName() {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object createInjectedHashedStack(Object hashedStack, net.momirealms.craftengine.core.entity.player.Player player) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Inventory createSimpleStorageContainer(InventoryHolder owner, int size, boolean canPlaceItem, boolean canTakeItem) {
        return new CraftInventory(new SimpleStorageContainer(owner, size, canPlaceItem, canTakeItem));
    }

}
