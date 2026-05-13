package net.momirealms.craftengine.bukkit.nms.v1_21;

import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.EntityLookup;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.handshake.HandshakeProtocols;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.StatusProtocols;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.phys.AABB;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import net.momirealms.craftengine.bukkit.nms.DelegatingContainer;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.nms.UnsupportedVersionException;
import net.momirealms.craftengine.bukkit.nms.v1_21.block.PaperStatePropertyAccessor;
import net.momirealms.craftengine.bukkit.nms.v1_21.chunk.InjectedLevelChunkSection;
import net.momirealms.craftengine.bukkit.nms.v1_21.chunk.InjectedPalettedContainer;
import net.momirealms.craftengine.bukkit.nms.v1_21.collision.CollisionBoat;
import net.momirealms.craftengine.bukkit.nms.v1_21.collision.CollisionInteraction;
import net.momirealms.craftengine.bukkit.nms.v1_21.collision.NonCollisionBoat;
import net.momirealms.craftengine.bukkit.nms.v1_21.collision.NonCollisionInteraction;
import net.momirealms.craftengine.bukkit.nms.v1_21.entity.InjectedFallingBlockEntity;
import net.momirealms.craftengine.bukkit.nms.v1_21.entity.InjectedPaperLevelCallback;
import net.momirealms.craftengine.bukkit.nms.v1_21.inventory.CustomContainer;
import net.momirealms.craftengine.bukkit.nms.v1_21.inventory.CustomWorldlyContainer;
import net.momirealms.craftengine.bukkit.nms.v1_21.loot.CraftEngineItem;
import net.momirealms.craftengine.bukkit.nms.v1_21.recipe.*;
import net.momirealms.craftengine.bukkit.nms.v1_21.worldgen.*;
import net.momirealms.craftengine.bukkit.world.BukkitContainer;
import net.momirealms.craftengine.bukkit.world.gen.InjectedChunkGenerator;
import net.momirealms.craftengine.core.block.StatePropertyAccessor;
import net.momirealms.craftengine.core.item.recipe.*;
import net.momirealms.craftengine.core.plugin.network.ConnectionState;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.InjectedWorldCallback;
import net.momirealms.craftengine.core.world.WorldlyContainer;
import net.momirealms.craftengine.core.world.chunk.InjectedStorage;
import net.momirealms.craftengine.libraries.reflection.SReflection;
import net.momirealms.craftengine.proxy.minecraft.world.level.chunk.LevelChunkSectionProxy;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public final class FastNMSImpl extends FastNMS {

    @Override
    public Object createBiomePlacementFilter(Predicate<Key> filter) {
        return new BiomeFilter(filter);
    }

    @Override
    public InjectedWorldCallback createInjectedWorldCallbacks(Object worldCallback, Object entityLookup) {
        return new InjectedPaperLevelCallback((LevelCallback<Entity>) worldCallback, (EntityLookup) entityLookup);
    }

    @Override
    public StatePropertyAccessor createStatePropertyAccessor(Object blockState) {
        return new PaperStatePropertyAccessor((BlockState) blockState);
    }

    @Override
    public Object toMinecraftIngredient(net.momirealms.craftengine.core.item.recipe.Ingredient ingredient) {
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
        InjectedLevelChunkSection newSection = new InjectedLevelChunkSection(section.getStates(), (PalettedContainer<Holder<Biome>>) section.getBiomes());
        LevelChunkSectionProxy.INSTANCE.setNonEmptyBlockCount(newSection, LevelChunkSectionProxy.INSTANCE.getNonEmptyBlockCount(section));
        LevelChunkSectionProxy.INSTANCE.setTickingBlockCount(newSection, LevelChunkSectionProxy.INSTANCE.getTickingBlockCount(section));
        LevelChunkSectionProxy.INSTANCE.setTickingFluidCount(newSection, LevelChunkSectionProxy.INSTANCE.getTickingFluidCount(section));
        LevelChunkSectionProxy.INSTANCE.setSpecialCollidingBlocks$legacy(newSection, LevelChunkSectionProxy.INSTANCE.getSpecialCollidingBlocks$legacy(section));
        LevelChunkSectionProxy.INSTANCE.setTickingBlocks(newSection, LevelChunkSectionProxy.INSTANCE.getTickingBlocks(section));
        return newSection;
    }

    @Override
    public InjectedChunkGenerator createInjectedChunkGenerator(CEWorld world, Object generator) {
        return new InjectedCustomChunkGenerator(world, (ChunkGenerator) generator);
    }

    @Override
    public CollisionEntity createCollisionBoat(Object world, Object aabb, double x, double y, double z, boolean canProjectileHit, boolean canCollide, boolean blocksBuilding) {
        if (canCollide) {
            return new CollisionBoat(EntityType.BOAT, (Level) world, x, y, z, (AABB) aabb, canProjectileHit, blocksBuilding);
        } else {
            return new NonCollisionBoat(EntityType.BOAT, (Level) world, x, y, z, (AABB) aabb, canProjectileHit, blocksBuilding);
        }
    }

    @Override
    public CollisionEntity createCollisionInteraction(Object world, Object aabb, double x, double y, double z, boolean canProjectileHit, boolean canCollide, boolean blocksBuilding) {
        if (canCollide) {
            return new CollisionInteraction(EntityType.INTERACTION, (Level) world, x, y, z, (AABB) aabb, canProjectileHit, blocksBuilding);
        } else {
            return new NonCollisionInteraction(EntityType.INTERACTION, (Level) world, x, y, z, (AABB) aabb, canProjectileHit, blocksBuilding);
        }
    }

    @Override
    public Object createShapedRecipe(CustomShapedRecipe recipe) {
        return InjectedShapedRecipe.of(recipe);
    }

    @Override
    public Object createShapelessRecipe(CustomShapelessRecipe recipe) {
        return InjectedShapelessRecipe.of(recipe);
    }

    @Override
    public Object createSmokingRecipe(CustomSmokingRecipe recipe) {
        return InjectedSmokingRecipe.of(recipe);
    }

    @Override
    public Object createSmeltingRecipe(CustomSmeltingRecipe recipe) {
        return InjectedSmeltingRecipe.of(recipe);
    }

    @Override
    public Object createBlastingRecipe(CustomBlastingRecipe recipe) {
        return InjectedBlastingRecipe.of(recipe);
    }

    @Override
    public Object createCampfireRecipe(CustomCampfireRecipe recipe) {
        return InjectedCampfireCookingRecipe.of(recipe);
    }

    @Override
    public Object createStonecuttingRecipe(CustomStoneCuttingRecipe recipe) {
        return InjectedStonecuttingRecipe.of(recipe);
    }

    @Override
    public Object createSmithingTransformRecipe(CustomSmithingTransformRecipe recipe) {
        return InjectedSmithingTransformRecipe.of(recipe);
    }

    @Override
    public Object createSmithingTrimRecipe(CustomSmithingTrimRecipe recipe) {
        return InjectedSmithingTrimRecipe.of(recipe);
    }

    @Override
    public Object createDyeRecipe(CustomDyeRecipe recipe) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object createInjectedFallingBlockEntity(Object level, Object pos, Object blockState) {
        if (VersionHelper.isFolia()) {
            return FallingBlockEntity.fall((Level) level, (BlockPos) pos, (BlockState) blockState);
        }
        return InjectedFallingBlockEntity.fall((Level) level, (BlockPos) pos, (BlockState) blockState);
    }

    @Override
    public Object createInjectedFurnaceCachedCheck(Object recipeType, Object blockEntity) {
        return new InjectedFurnaceCachedCheck<>((RecipeType<?>) recipeType, (BlockEntity) blockEntity);
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.ItemStack> ITEM_UNTRUSTED_CODEC =
            net.minecraft.world.item.ItemStack.validatedStreamCodec(net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC);

    private RegistryAccess registryAccess() {
        return MinecraftServer.getServer().registryAccess();
    }

    @Override
    public Object createAlwaysStatePredicate(boolean trueOrFalse) {
        return (BlockBehaviour.StatePredicate) (blockState, blockGetter, blockPos) -> trueOrFalse;
    }

    @Override
    public Map<ConnectionState, Map<net.momirealms.craftengine.core.plugin.network.PacketFlow, Map<Class<?>, Integer>>> gamePacketIdsByClazz() {
        throw new UnsupportedVersionException();
    }

    @Override
    public Map<ConnectionState, Map<net.momirealms.craftengine.core.plugin.network.PacketFlow, Map<String, Integer>>> gamePacketIdsByName() {
        Map<ConnectionState, Map<net.momirealms.craftengine.core.plugin.network.PacketFlow, Map<String, Integer>>> allPacketIdsByName = new HashMap<>();
        Map<ConnectionProtocol, List<ProtocolInfo.Unbound<? extends PacketListener, ? extends ByteBuf>>> collect = Stream.of(
                HandshakeProtocols.SERVERBOUND_TEMPLATE,
                StatusProtocols.CLIENTBOUND_TEMPLATE,
                StatusProtocols.SERVERBOUND_TEMPLATE,
                LoginProtocols.CLIENTBOUND_TEMPLATE,
                LoginProtocols.SERVERBOUND_TEMPLATE,
                ConfigurationProtocols.CLIENTBOUND_TEMPLATE,
                ConfigurationProtocols.SERVERBOUND_TEMPLATE,
                GameProtocols.CLIENTBOUND_TEMPLATE,
                GameProtocols.SERVERBOUND_TEMPLATE
        ).collect(Collectors.groupingBy(ProtocolInfo.Unbound::id));
        for (Map.Entry<ConnectionProtocol, List<ProtocolInfo.Unbound<? extends PacketListener, ? extends ByteBuf>>> entry : collect.entrySet()) {
            Map<net.momirealms.craftengine.core.plugin.network.PacketFlow, Map<String, Integer>> protocolPacketIdsByName = new HashMap<>();
            allPacketIdsByName.put(ConnectionState.valueOf(entry.getKey().name().toUpperCase(Locale.ROOT)), protocolPacketIdsByName);
            Map<String, Integer> serverBoundIds = new HashMap<>();
            Map<String, Integer> clientBoundIds = new HashMap<>();
            protocolPacketIdsByName.put(net.momirealms.craftengine.core.plugin.network.PacketFlow.SERVERBOUND, serverBoundIds);
            protocolPacketIdsByName.put(net.momirealms.craftengine.core.plugin.network.PacketFlow.CLIENTBOUND, clientBoundIds);
            for (ProtocolInfo.Unbound<? extends PacketListener, ? extends ByteBuf> protocol : entry.getValue()) {
                if (protocol.flow() == PacketFlow.SERVERBOUND) {
                    protocol.listPackets(((type, protocolId) -> serverBoundIds.put(type.id().toString(), protocolId)));
                } else if (protocol.flow() == PacketFlow.CLIENTBOUND) {
                    protocol.listPackets(((type, protocolId) -> clientBoundIds.put(type.id().toString(), protocolId)));
                }
            }
        }
        return allPacketIdsByName;
    }

    @Override
    public Object createInjectedHashedStack(Object hashedStack, net.momirealms.craftengine.core.entity.player.Player player) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object createUntrustedItemCodec() {
        return ItemStack.validatedStreamCodec(ItemStack.OPTIONAL_STREAM_CODEC);
    }

    @Override
    public DelegatingContainer createContainer(BukkitContainer container) {
        if (container instanceof WorldlyContainer worldlyContainer) {
            return new CustomWorldlyContainer(worldlyContainer);
        } else {
            return new CustomContainer(container);
        }
    }
}
