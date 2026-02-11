package net.momirealms.craftengine.bukkit.nms.v1_20_5;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import io.papermc.paper.chunk.system.entity.EntityLookup;
import io.papermc.paper.world.ChunkEntitySlices;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.IdDispatchCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.handshake.HandshakeProtocols;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.StatusProtocols;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.nms.UnsupportedVersionException;
import net.momirealms.craftengine.bukkit.nms.v1_20_5.block.PaperStatePropertyAccessor;
import net.momirealms.craftengine.bukkit.nms.v1_20_5.chunk.InjectedLevelChunkSection;
import net.momirealms.craftengine.bukkit.nms.v1_20_5.chunk.InjectedPalettedContainer;
import net.momirealms.craftengine.bukkit.nms.v1_20_5.collision.CollisionBoat;
import net.momirealms.craftengine.bukkit.nms.v1_20_5.collision.CollisionInteraction;
import net.momirealms.craftengine.bukkit.nms.v1_20_5.collision.NonCollisionBoat;
import net.momirealms.craftengine.bukkit.nms.v1_20_5.collision.NonCollisionInteraction;
import net.momirealms.craftengine.bukkit.nms.v1_20_5.entity.InjectedFallingBlockEntity;
import net.momirealms.craftengine.bukkit.nms.v1_20_5.entity.InjectedPaperEntityCallbacks;
import net.momirealms.craftengine.bukkit.nms.v1_20_5.inventory.SimpleStorageContainer;
import net.momirealms.craftengine.bukkit.nms.v1_20_5.loot.CraftEngineItem;
import net.momirealms.craftengine.bukkit.nms.v1_20_5.recipe.*;
import net.momirealms.craftengine.bukkit.nms.v1_20_5.worldgen.*;
import net.momirealms.craftengine.bukkit.util.BukkitReflectionUtils;
import net.momirealms.craftengine.core.block.StatePropertyAccessor;
import net.momirealms.craftengine.core.item.recipe.*;
import net.momirealms.craftengine.core.plugin.network.ConnectionState;
import net.momirealms.craftengine.core.plugin.network.PacketFlow;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ReflectionUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.chunk.InjectedHolder;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.*;
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
    public InjectedHolder.Palette createInjectedPalettedContainerHolder(Object palettedContainer) throws InstantiationException {
        InjectedPalettedContainer injectedObject = (InjectedPalettedContainer) ReflectionUtils.UNSAFE.allocateInstance(InjectedPalettedContainer.class);
        injectedObject.setTarget(palettedContainer);
        return injectedObject;
    }

    @Override
    public InjectedHolder.Section createInjectedLevelChunkSectionHolder(Object levelChunkSection) {
        LevelChunkSection section = (LevelChunkSection) levelChunkSection;
        return new InjectedLevelChunkSection(section.getStates(), (PalettedContainer<Holder<Biome>>) section.getBiomes());
    }

    @Override
    public void injectedWorldGen(CEWorld world, Object chunkMap) {
        ChunkGenerator generator = ((ChunkMap) chunkMap).generator;
        if (!(generator instanceof InjectedChunkGenerator)) {
            ((ChunkMap) chunkMap).generator = new InjectedChunkGenerator(world, generator);
        }
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
    public Object field$RecipeHolder$id(Object recipeHolder) {
        return ((RecipeHolder) recipeHolder).id();
    }

    @Override
    public boolean method$ServerLevel$isPreventingStatusUpdates(Object world, int x, int z) {
        ChunkEntitySlices slices = ((ServerLevel) world).getEntityLookup().getChunk(x, z);
        return slices != null && slices.isPreventingStatusUpdates();
    }

    @Override
    public void method$Connection$send(Object connection, Object packet, Object sendListener) {
        ((Connection) connection).send((Packet<?>) packet, ((PacketSendListener) sendListener));
    }

    @Override
    public Object method$Component$Serializer$fromJson(JsonElement element) {
        return Component.Serializer.fromJson(element, registryAccess());
    }

    @Override
    public Object method$Component$Serializer$fromJson(String json) {
        return Component.Serializer.fromJson(json, registryAccess());
    }

    @Override
    public String method$Component$Serializer$toJson(Object component) {
        return Component.Serializer.toJson((Component) component, registryAccess());
    }

    @Override
    public void method$SoundEvent$directEncode(ByteBuf buffer, Object soundEvent) {
        SoundEvent.DIRECT_STREAM_CODEC.encode(buffer, (SoundEvent) soundEvent);
    }

    @Override
    public double method$Player$getInteractionRange(Object player) {
        return Optional.ofNullable(((net.minecraft.world.entity.player.Player) player).getAttribute(Attributes.BLOCK_INTERACTION_RANGE)).map(AttributeInstance::getValue).orElse(4.5d);
    }

    @Override
    public boolean method$ItemStack$canBreakInAdventureMode(Object itemStack, Object blockInWorld) {
        return ((net.minecraft.world.item.ItemStack) itemStack).canBreakBlockInAdventureMode((BlockInWorld) blockInWorld);
    }

    @Override
    public boolean method$ItemStack$canPlaceInAdventureMode(Object itemStack, Object blockInWorld) {
        return ((net.minecraft.world.item.ItemStack) itemStack).canPlaceOnBlockInAdventureMode((BlockInWorld) blockInWorld);
    }

    @Override
    public void simulateInteraction(Object player, Object direction, double x, double y, double z, Object pos) {
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ServerLevel serverLevel = serverPlayer.serverLevel();
        BlockPos blockPos = (BlockPos) pos;
        BlockState previous = serverLevel.getBlockStateIfLoaded(blockPos);
        if (previous == null) return;
        Vec3 vec3 = new Vec3(x, y, z);
        ServerboundUseItemOnPacket packet = new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND,
                new BlockHitResult(vec3, (Direction) direction, blockPos, false),
                0);
        try {
            serverLevel.setBlock(blockPos, Blocks.COBWEB.defaultBlockState(), 4);
            packet.timestamp = System.currentTimeMillis();
            serverPlayer.connection.handleUseItemOn(packet);
        } finally {
            serverLevel.setBlock(blockPos, previous, 4);
            serverPlayer.connection.send(new ClientboundBlockUpdatePacket(serverLevel, blockPos));
        }
    }

    @Override
    public final boolean checkEntityCollision(Object level, List<Object> aabbs, Predicate<Object> entityFilter) {
        if (aabbs.isEmpty()) return true;
        CommonLevelAccessor serverLevel = (CommonLevelAccessor) level;
        List<VoxelShape> shapes = Lists.newArrayList();
        for (Object ab : aabbs) {
            AABB aabb = (AABB) ab;
            VoxelShape voxelShape = Shapes.create(aabb);
            shapes.add(voxelShape);
        }
        VoxelShape finalShape;
        if (shapes.size() == 1) {
            finalShape = shapes.get(0);
        } else if (shapes.size() == 2) {
            finalShape = Shapes.or(shapes.get(0), shapes.get(1));
        } else {
            finalShape = Shapes.or(shapes.get(0), shapes.subList(1, shapes.size()).toArray(new VoxelShape[0]));
        }
        if (finalShape.isEmpty()) return true;
        if (serverLevel.getBlockCollisions(null, finalShape.bounds()).iterator().hasNext()) {
            return false;
        }
        List<Entity> entities = serverLevel.getEntities(null, finalShape.bounds());
        for (Entity entity : entities) {
            if (entityFilter.test(entity)) {
                if (!entity.isRemoved() && entity.blocksBuilding && Shapes.joinIsNotEmpty(finalShape, Shapes.create(entity.getBoundingBox()), BooleanOp.AND)) {
                    return false;
                }
            }
        }
        return true;
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

    @Override
    public boolean method$LightEngine$hasDifferentLightProperties(Object oldState, Object newState) {
        BlockState oldBlockState = (BlockState) oldState;
        BlockState newBlockState = (BlockState) newState;
        return newBlockState.getLightEmission() != oldBlockState.getLightEmission() || newBlockState.useShapeForLightOcclusion() || oldBlockState.useShapeForLightOcclusion();
    }

    @Override
    public ItemStack method$FriendlyByteBuf$readItem(Object buf) {
        return CraftItemStack.asCraftMirror(net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf));
    }

    @Override
    public void method$FriendlyByteBuf$writeItem(Object buf, ItemStack itemStack) {
        net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, CraftItemStack.unwrap(itemStack));
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.ItemStack> ITEM_UNTRUSTED_CODEC =
            net.minecraft.world.item.ItemStack.validatedStreamCodec(net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC);

    @Override
    public ItemStack method$FriendlyByteBuf$readUntrustedItem(Object buf) {
        return CraftItemStack.asCraftMirror(ITEM_UNTRUSTED_CODEC.decode((RegistryFriendlyByteBuf) buf));
    }

    @Override
    public void method$FriendlyByteBuf$writeUntrustedItem(Object buf, ItemStack itemStack) {
        ITEM_UNTRUSTED_CODEC.encode((RegistryFriendlyByteBuf) buf, CraftItemStack.unwrap(itemStack));
    }

    private RegistryAccess registryAccess() {
        return MinecraftServer.getServer().registryAccess();
    }

    @Override
    public Object method$StatePredicate$always(boolean trueOrFalse) {
        return (BlockBehaviour.StatePredicate) (blockState, blockGetter, blockPos) -> trueOrFalse;
    }

    @Override
    public Map<ConnectionState, Map<PacketFlow, Map<Class<?>, Integer>>> gamePacketIdsByClazz() {
        throw new UnsupportedVersionException();
    }

    private static final Field field$IdDispatchCodec$byId = Objects.requireNonNull(
            ReflectionUtils.getDeclaredField(IdDispatchCodec.class, List.class, 0)
    );
    private static final Class<?> clazz$IdDispatchCodec$Entry = Objects.requireNonNull(BukkitReflectionUtils.findReobfOrMojmapClass(
            "network.codec.IdDispatchCodec$b",
            "network.codec.IdDispatchCodec$Entry"
    ));
    private static final Field field$IdDispatchCodec$Entry$type = Objects.requireNonNull(
            ReflectionUtils.getDeclaredField(clazz$IdDispatchCodec$Entry, Object.class, 0)
    );

    @Override
    public Map<ConnectionState, Map<PacketFlow, Map<String, Integer>>> gamePacketIdsByName() {
        Map<ConnectionState, Map<PacketFlow, Map<String, Integer>>> allPacketIdsByName = new HashMap<>();
        Map<ConnectionProtocol, List<ProtocolInfo<? extends PacketListener>>> collect = Stream.of(
                HandshakeProtocols.SERVERBOUND,
                StatusProtocols.CLIENTBOUND,
                StatusProtocols.SERVERBOUND,
                LoginProtocols.CLIENTBOUND,
                LoginProtocols.SERVERBOUND,
                ConfigurationProtocols.CLIENTBOUND,
                ConfigurationProtocols.SERVERBOUND,
                GameProtocols.CLIENTBOUND.bind(b -> new RegistryFriendlyByteBuf(b, registryAccess())),
                GameProtocols.SERVERBOUND.bind(b -> new RegistryFriendlyByteBuf(b, registryAccess()))
        ).collect(Collectors.groupingBy(ProtocolInfo::id));
        try {
            for (Map.Entry<ConnectionProtocol, List<ProtocolInfo<? extends PacketListener>>> entry : collect.entrySet()) {
                Map<PacketFlow, Map<String, Integer>> protocolPacketIdsByName = new HashMap<>();
                allPacketIdsByName.put(ConnectionState.valueOf(entry.getKey().name().toUpperCase(Locale.ROOT)), protocolPacketIdsByName);
                Map<String, Integer> serverBoundIds = new HashMap<>();
                Map<String, Integer> clientBoundIds = new HashMap<>();
                protocolPacketIdsByName.put(PacketFlow.SERVERBOUND, serverBoundIds);
                protocolPacketIdsByName.put(PacketFlow.CLIENTBOUND, clientBoundIds);
                for (ProtocolInfo<? extends PacketListener> protocol : entry.getValue()) {
                    List<?> byId = (List<?>) field$IdDispatchCodec$byId.get(protocol.codec());
                    if (protocol.flow() == net.minecraft.network.protocol.PacketFlow.SERVERBOUND) {
                        for (int i = 0; i < byId.size(); ++i) {
                            PacketType<?> type = (PacketType<?>) field$IdDispatchCodec$Entry$type.get(byId.get(i));
                            serverBoundIds.put(type.id().toString(), i);
                        }
                    } else if (protocol.flow() == net.minecraft.network.protocol.PacketFlow.CLIENTBOUND) {
                        for (int i = 0; i < byId.size(); ++i) {
                            PacketType<?> type = (PacketType<?>) field$IdDispatchCodec$Entry$type.get(byId.get(i));
                            clientBoundIds.put(type.id().toString(), i);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return allPacketIdsByName;
    }

    @Override
    public Object constructor$InjectedHashedStack(Object hashedStack, net.momirealms.craftengine.core.entity.player.Player player) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Inventory createSimpleStorageContainer(InventoryHolder owner, int size, boolean canPlaceItem, boolean canTakeItem) {
        return new CraftInventory(new SimpleStorageContainer(owner, size, canPlaceItem, canTakeItem));
    }

}
