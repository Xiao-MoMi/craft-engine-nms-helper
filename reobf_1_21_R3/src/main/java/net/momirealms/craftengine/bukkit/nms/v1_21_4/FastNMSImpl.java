package net.momirealms.craftengine.bukkit.nms.v1_21_4;

import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.EntityLookup;
import io.netty.buffer.ByteBuf;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.util.ItemObfuscationSession;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.handshake.HandshakeProtocols;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.StatusProtocols;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.phys.AABB;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.nms.UnsupportedVersionException;
import net.momirealms.craftengine.bukkit.nms.v1_21_4.block.PaperStatePropertyAccessor;
import net.momirealms.craftengine.bukkit.nms.v1_21_4.chunk.InjectedLevelChunkSection;
import net.momirealms.craftengine.bukkit.nms.v1_21_4.chunk.InjectedPalettedContainer;
import net.momirealms.craftengine.bukkit.nms.v1_21_4.collision.CollisionBoat;
import net.momirealms.craftengine.bukkit.nms.v1_21_4.collision.CollisionInteraction;
import net.momirealms.craftengine.bukkit.nms.v1_21_4.collision.NonCollisionBoat;
import net.momirealms.craftengine.bukkit.nms.v1_21_4.collision.NonCollisionInteraction;
import net.momirealms.craftengine.bukkit.nms.v1_21_4.entity.InjectedFallingBlockEntity;
import net.momirealms.craftengine.bukkit.nms.v1_21_4.entity.InjectedPaperEntityCallbacks;
import net.momirealms.craftengine.bukkit.nms.v1_21_4.inventory.SimpleStorageContainer;
import net.momirealms.craftengine.bukkit.nms.v1_21_4.loot.CraftEngineItem;
import net.momirealms.craftengine.bukkit.nms.v1_21_4.recipe.*;
import net.momirealms.craftengine.bukkit.nms.v1_21_4.worldgen.*;
import net.momirealms.craftengine.core.block.StatePropertyAccessor;
import net.momirealms.craftengine.core.item.recipe.*;
import net.momirealms.craftengine.core.plugin.network.ConnectionState;
import net.momirealms.craftengine.core.plugin.network.PacketFlow;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.chunk.InjectedStorage;
import net.momirealms.craftengine.proxy.minecraft.server.level.ChunkMapProxy;
import net.momirealms.sparrow.reflection.SReflection;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"unchecked", "rawtypes", "unused", "deprecation"})
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
        return new CustomSimpleBlockFeature();
    }

    @Override
    public InjectedStorage.Palette createInjectedPalettedContainerHolder(Object palettedContainer) {
        InjectedPalettedContainer injectedObject = SReflection.allocateInstance(InjectedPalettedContainer.class);
        injectedObject.delegated = (PalettedContainer) palettedContainer;
        return injectedObject;
    }

    @Override
    public InjectedStorage.Section createInjectedLevelChunkSectionHolder(Object levelChunkSection) {
        LevelChunkSection section = (LevelChunkSection) levelChunkSection;
        return new InjectedLevelChunkSection(section.getStates(), (PalettedContainer<Holder<Biome>>) section.getBiomes());
    }

    @Override
    public void injectedWorldGen(CEWorld world, Object chunkMap) {
        WorldGenContext worldGenContext = ((ChunkMap) chunkMap).worldGenContext;
        if (!(worldGenContext.generator() instanceof InjectedChunkGenerator)) {
            WorldGenContext context = new WorldGenContext(
                    worldGenContext.level(),
                    new InjectedChunkGenerator(world, worldGenContext.generator()),
                    worldGenContext.structureManager(),
                    worldGenContext.lightEngine(),
                    worldGenContext.mainThreadExecutor(),
                    worldGenContext.unsavedListener()
            );
            ChunkMapProxy.INSTANCE.setWorldGenContext(chunkMap, context);
        }
    }

    @Override
    public CollisionEntity createCollisionBoat(Object world, Object aabb, double x, double y, double z, boolean canProjectileHit, boolean canCollide, boolean blocksBuilding) {
        if (canCollide) {
            return new CollisionBoat(EntityType.OAK_BOAT, (Level) world, x, y, z, (AABB) aabb, canProjectileHit, blocksBuilding);
        } else {
            return new NonCollisionBoat(EntityType.OAK_BOAT, (Level) world, x, y, z, (AABB) aabb, canProjectileHit, blocksBuilding);
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
    @SuppressWarnings("deprecation")
    public String getCustomItemId(Object itemStack) {
        net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) itemStack;
        CustomData customData = nmsStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return null;
        Tag tag = customData.getUnsafe().get("craftengine:id");
        if (tag instanceof StringTag tag1) {
            return tag1.getAsString();
        }
        return null;
    }

    @Override
    public void setCustomItemId(Object itemStack, String id) {
        net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) itemStack;
        CustomData customData = nmsStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("craftengine:id", id);
            nmsStack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));
        } else {
            CompoundTag copied = customData.copyTag();
            copied.putString("craftengine:id", id);
            nmsStack.set(DataComponents.CUSTOM_DATA, CustomData.of(copied));
        }
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.ItemStack> ITEM_UNTRUSTED_CODEC;

    static {
        StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.ItemStack> codec;
        try {
            codec = net.minecraft.world.item.ItemStack.validatedStreamCodec(net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC).apply(ByteBufCodecs::trackDepth);
        } catch (NoSuchMethodError error) {
            codec = net.minecraft.world.item.ItemStack.validatedStreamCodec(net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC);
        }
        ITEM_UNTRUSTED_CODEC = codec;
    }

    private RegistryAccess registryAccess() {
        return MinecraftServer.getServer().registryAccess();
    }

    @Override
    public Object createAlwaysStatePredicate(boolean trueOrFalse) {
        return (BlockBehaviour.StatePredicate) (blockState, blockGetter, blockPos) -> trueOrFalse;
    }

    private static Boolean HAS_ANTICHEAT;

    private static boolean hasAntiCheat() {
        if (HAS_ANTICHEAT == null) {
            boolean has;
            try {
                ItemObfuscationSession.ObfuscationLevel level = GlobalConfiguration.get().anticheat.obfuscation.items.binding.level;
                has = true;
            } catch (NoSuchFieldError error) {
                has = false;
            }
            HAS_ANTICHEAT = has;
        }
        return HAS_ANTICHEAT;
    }

    @Override
    public Map<ConnectionState, Map<PacketFlow, Map<Class<?>, Integer>>> gamePacketIdsByClazz() {
        throw new UnsupportedVersionException();
    }

    @Override
    public Map<ConnectionState, Map<PacketFlow, Map<String, Integer>>> gamePacketIdsByName() {
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
                if (protocol.flow() == net.minecraft.network.protocol.PacketFlow.SERVERBOUND) {
                    protocol.listPackets(((type, protocolId) -> serverBoundIds.put(type.id().toString(), protocolId)));
                } else if (protocol.flow() == net.minecraft.network.protocol.PacketFlow.CLIENTBOUND) {
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
    public Inventory createSimpleStorageContainer(InventoryHolder owner, int size, boolean canPlaceItem, boolean canTakeItem) {
        return new CraftInventory(new SimpleStorageContainer(owner, size, canPlaceItem, canTakeItem));
    }

}
