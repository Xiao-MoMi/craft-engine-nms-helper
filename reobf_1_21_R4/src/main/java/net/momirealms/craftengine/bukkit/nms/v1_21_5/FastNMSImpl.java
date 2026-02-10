package net.momirealms.craftengine.bukkit.nms.v1_21_5;

import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.ChunkEntitySlices;
import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.EntityLookup;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.handshake.HandshakeProtocols;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.StatusProtocols;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.nms.UnsupportedVersionException;
import net.momirealms.craftengine.bukkit.nms.v1_21_5.block.PaperStatePropertyAccessor;
import net.momirealms.craftengine.bukkit.nms.v1_21_5.chunk.InjectedLevelChunkSection;
import net.momirealms.craftengine.bukkit.nms.v1_21_5.chunk.InjectedPalettedContainer;
import net.momirealms.craftengine.bukkit.nms.v1_21_5.collision.CollisionBoat;
import net.momirealms.craftengine.bukkit.nms.v1_21_5.collision.CollisionInteraction;
import net.momirealms.craftengine.bukkit.nms.v1_21_5.collision.NonCollisionBoat;
import net.momirealms.craftengine.bukkit.nms.v1_21_5.collision.NonCollisionInteraction;
import net.momirealms.craftengine.bukkit.nms.v1_21_5.entity.InjectedFallingBlockEntity;
import net.momirealms.craftengine.bukkit.nms.v1_21_5.entity.InjectedPaperEntityCallbacks;
import net.momirealms.craftengine.bukkit.nms.v1_21_5.inventory.SimpleStorageContainer;
import net.momirealms.craftengine.bukkit.nms.v1_21_5.loot.CraftEngineItem;
import net.momirealms.craftengine.bukkit.nms.v1_21_5.network.InjectedHashedStack;
import net.momirealms.craftengine.bukkit.nms.v1_21_5.recipe.*;
import net.momirealms.craftengine.bukkit.nms.v1_21_5.worldgen.*;
import net.momirealms.craftengine.core.block.StatePropertyAccessor;
import net.momirealms.craftengine.core.item.recipe.*;
import net.momirealms.craftengine.core.plugin.network.ConnectionState;
import net.momirealms.craftengine.core.plugin.network.PacketFlow;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ReflectionUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.chunk.InjectedHolder;
import net.momirealms.craftengine.proxy.minecraft.server.level.ChunkMapProxy;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

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
    public Object method$PalettedContainer$getAndSet(Object palettedContainer, int x, int y, int z, Object blockState) {
        return ((PalettedContainer) palettedContainer).getAndSet(x, y, z, blockState);
    }

    @Override
    public Object method$PalettedContainer$getAndSetUnchecked(Object palettedContainer, int x, int y, int z, Object blockState) {
        return ((PalettedContainer) palettedContainer).getAndSetUnchecked(x, y, z, blockState);
    }

    @Override
    public BlockData method$CraftBlockData$fromData(Object blockState) {
        return CraftBlockData.fromData((BlockState) blockState);
    }

    @Override
    public int method$IdMapper$getId(Object idMapper, Object t) {
        return ((IdMapper) idMapper).getId(t);
    }

    @Override
    public Object method$IdMapper$byId(Object idMapper, int id) {
        return ((IdMapper) idMapper).byId(id);
    }

    @Override
    public Object method$CraftBlockData$getState(BlockData blockData) {
        return ((CraftBlockData) blockData).getState();
    }

    @Override
    public int method$BlockStateBase$getLightEmission(Object blockState) {
        return ((BlockState) blockState).getLightEmission();
    }

    @Override
    public boolean method$BlockStateBase$canOcclude(Object blockState) {
        return ((BlockState) blockState).canOcclude();
    }

    @Override
    public Object method$LevelChunkSection$setBlockState(Object section, int x, int y, int z, Object blockState, boolean lock) {
        return ((LevelChunkSection) section).setBlockState(x, y, z, (BlockState) blockState, lock);
    }

    @Override
    public Object method$LevelChunkSection$getBlockState(Object section, int x, int y, int z) {
        return ((LevelChunkSection) section).getBlockState(x, y, z);
    }

    @Override
    public Object field$CraftChunk$worldServer(Chunk chunk) {
        return ((CraftChunk) chunk).getCraftWorld().getHandle();
    }

    @Override
    public Object method$ServerLevel$getChunkSource(Object serverLevel) {
        return ((ServerLevel) serverLevel).getChunkSource();
    }

    @Override
    public Object method$ServerChunkCache$getChunkAtIfLoadedMainThread(Object serverChunkCache, int x, int z) {
        return ((ServerChunkCache) serverChunkCache).getChunkAtIfLoadedImmediately(x, z);
    }

    @Override
    public Object field$LevelChunkSection$states(Object section) {
        return ((LevelChunkSection) section).states;
    }

    @Override
    public Object[] method$ChunkAccess$getSections(Object chunk) {
        return ((ChunkAccess) chunk).getSections();
    }

    @Override
    public Object field$ChunkAccess$blockEntities(Object chunkAccess) {
        return ((ChunkAccess) chunkAccess).blockEntities;
    }

    @Override
    public Object field$CraftWorld$ServerLevel(World world) {
        return ((CraftWorld) world).getHandle();
    }

    @Override
    public Block method$CraftBlock$at(Object world, Object blockPos) {
        return CraftBlock.at((LevelAccessor) world, (BlockPos) blockPos);
    }

    @Override
    public Object field$AbstractFurnaceBlockEntity$recipeType(Object furnaceBlockEntity) {
        return ((AbstractFurnaceBlockEntity) furnaceBlockEntity).recipeType;
    }

    @Override
    public ItemStack method$CraftItemStack$asCraftMirror(Object itemStack) {
        return CraftItemStack.asCraftMirror((net.minecraft.world.item.ItemStack) itemStack);
    }

    @Override
    public Object field$ResourceKey$location(Object resourceKey) {
        return ((ResourceKey) resourceKey).location();
    }

    @Override
    public Object field$RecipeHolder$id(Object recipeHolder) {
        return ((RecipeHolder) recipeHolder).id();
    }

    @Override
    public World method$Level$getCraftWorld(Object level) {
        return ((Level) level).getWorld();
    }

    @Override
    public boolean method$Level$removeBlock(Object level, Object blockPos, boolean move) {
        return ((Level) level).removeBlock((BlockPos) blockPos, move);
    }

    @Override
    public int field$Vec3i$x(Object vec3i) {
        return ((Vec3i) vec3i).getX();
    }

    @Override
    public int field$Vec3i$y(Object vec3i) {
        return ((Vec3i) vec3i).getY();
    }

    @Override
    public int field$Vec3i$z(Object vec3i) {
        return ((Vec3i) vec3i).getZ();
    }

    @Override
    public double field$Vec3$x(Object vec3) {
        return ((Vec3) vec3).x();
    }

    @Override
    public double field$Vec3$y(Object vec3) {
        return ((Vec3) vec3).y();
    }

    @Override
    public double field$Vec3$z(Object vec3) {
        return ((Vec3) vec3).z();
    }

    @Override
    public Object constructor$BlockPos(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }

    @Override
    public Object method$BlockGetter$getBlockState(Object blockGetter, Object blockPos) {
        return ((BlockGetter) blockGetter).getBlockState((BlockPos) blockPos);
    }

    @Override
    public Object method$CraftPlayer$getHandle(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    @Override
    public Object constructor$AABB(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new AABB(x1, y1, z1, x2, y2, z2);
    }

    @Override
    public boolean method$LevelWriter$addFreshEntity(Object level, Object entity) {
        return ((LevelWriter) level).addFreshEntity((Entity) entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public Object method$CraftEntity$getHandle(Object entity) {
        return ((CraftEntity) entity).getHandle();
    }

    @Override
    public Object constructor$ClientboundSetPassengersPacket(int entityId, int... passengers) {
        FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
        byteBuf.writeVarInt(entityId);
        byteBuf.writeVarIntArray(passengers);
        return ClientboundSetPassengersPacket.STREAM_CODEC.decode(byteBuf);
    }

    @Override
    public boolean method$ServerLevel$isPreventingStatusUpdates(Object world, int x, int z) {
        ChunkEntitySlices slices = ((ServerLevel) world).moonrise$getEntityLookup().getChunk(x, z);
        return slices != null && slices.isPreventingStatusUpdates();
    }

    @Override
    public int method$Entity$getId(Object entity) {
        return ((Entity) entity).getId();
    }

    @Override
    public boolean method$LevelWriter$setBlock(Object level, Object blockPos, Object blockState, int flags) {
        return ((LevelWriter) level).setBlock((BlockPos) blockPos, (BlockState) blockState, flags);
    }

    @Override
    public Object method$ServerChunkCache$getVisibleChunkIfPresent(Object chunkSource, long chunkKey) {
        return ((ServerChunkCache) chunkSource).chunkMap.getVisibleChunkIfPresent(chunkKey);
    }

    @Override
    public Object constructor$ChunkPos(int x, int z) {
        return new ChunkPos(x, z);
    }

    @Override
    public Object constructor$ClientboundLightUpdatePacket(Object chunkPos, Object lightEngine, BitSet skyChangedLightSectionFilter, BitSet blockChangedLightSectionFilter) {
        return new ClientboundLightUpdatePacket((ChunkPos) chunkPos, (LevelLightEngine) lightEngine, skyChangedLightSectionFilter, blockChangedLightSectionFilter);
    }

    @Override
    public void method$ServerChunkCache$blockChanged(Object chunkCache, Object blockPos) {
        ((ServerChunkCache) chunkCache).blockChanged((BlockPos) blockPos);
    }

    @Override
    public void method$Connection$send(Object connection, Object packet, Object sendListener) {
        ((Connection) connection).send((Packet<?>) packet, ((PacketSendListener) sendListener));
    }

    @Override
    public List<Object> method$ChunkHolder$getPlayers(Object chunkHolder) {
        return (List) ((ChunkHolder) chunkHolder).moonrise$getPlayers(false);
    }

    @Override
    public Object constructor$ClientboundBundlePacket(List<Object> packets) {
        return new ClientboundBundlePacket((Iterable) packets);
    }

    @Override
    public Object field$Player$connection(Object player) {
        return ((ServerPlayer) player).connection;
    }

    @Override
    public Object field$ServerGamePacketListenerImpl$connection(Object serverGamePacketListener) {
        return ((ServerGamePacketListenerImpl) serverGamePacketListener).connection;
    }

    @Override
    public Channel field$Connection$channel(Object connection) {
        return ((Connection) connection).channel;
    }

    @Override
    public void method$BlockStateBase$onPlace(Object blockState, Object world, Object blockPos, Object oldBlockState, boolean movedByPiston) {
        ((BlockBehaviour.BlockStateBase) blockState).onPlace((Level) world, (BlockPos) blockPos, (BlockState) oldBlockState, movedByPiston);
    }

    @Override
    public void method$LevelAccessor$levelEvent(Object level, int eventId, Object blockPos, int stateId) {
        ((LevelAccessor) level).levelEvent(eventId, (BlockPos) blockPos, stateId);
    }

    @Override
    public Iterable<Object> method$ClientboundBundlePacket$subPackets(Object packet) {
        return (Iterable) ((ClientboundBundlePacket) packet).subPackets();
    }

    @Override
    public Object method$ResourceLocation$fromNamespaceAndPath(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    @Override
    public Object field$SoundEvent$location(Object soundEvent) {
        return ((SoundEvent) soundEvent).location();
    }

    @Override
    public Object field$ServerboundSwingPacket$hand(Object packet) {
        return ((ServerboundSwingPacket) packet).getHand();
    }

    @Override
    public Object field$BlockParticleOption$blockState(Object object) {
        return ((BlockParticleOption) object).getState();
    }

    @Override
    public Object field$ServerboundPlayerActionPacket$pos(Object packet) {
        return ((ServerboundPlayerActionPacket) packet).getPos();
    }

    @Override
    public Object field$ServerboundPlayerActionPacket$action(Object packet) {
        return ((ServerboundPlayerActionPacket) packet).getAction();
    }

    @Override
    public Object method$CraftItemStack$asNMSCopy(ItemStack itemStack) {
        return CraftItemStack.asNMSCopy(itemStack);
    }

    @Override
    public int field$SynchedEntityData$DataValue$id(Object data) {
        return ((SynchedEntityData.DataValue) data).id();
    }

    @Override
    public Object field$SynchedEntityData$DataValue$value(Object data) {
        return ((SynchedEntityData.DataValue) data).value();
    }

    @Override
    public Object field$SynchedEntityData$DataValue$serializer(Object data) {
        return ((SynchedEntityData.DataValue) data).serializer();
    }

    @Override
    public Object constructor$SynchedEntityData$DataValue(int id, Object serializer, Object data) {
        return new SynchedEntityData.DataValue<>(id, (EntityDataSerializer) serializer, data);
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
    public org.bukkit.entity.Entity method$Entity$getBukkitEntity(Object entity) {
        return ((Entity) entity).getBukkitEntity();
    }

    @Override
    public Optional<Object> method$IdMap$byId(Object registry, int id) {
        Object object = ((IdMap) registry).byId(id);
        if (object == null) {
            return Optional.empty();
        }
        return Optional.of(object);
    }

    @Override
    public Optional<Integer> method$IdMap$getId(Object registry, Object value) {
        int id = ((IdMap) registry).getId(value);
        return id == -1 ? Optional.empty() : Optional.of(id);
    }

    @Override
    public void method$SoundEvent$directEncode(ByteBuf buffer, Object soundEvent) {
        SoundEvent.DIRECT_STREAM_CODEC.encode(buffer, (SoundEvent) soundEvent);
    }

    @Override
    public List<Object> field$ClientboundPlayerInfoUpdatePacket$entries(Object packet) {
        return (List) ((ClientboundPlayerInfoUpdatePacket) packet).entries();
    }

    @Override
    public EnumSet<? extends Enum> field$ClientboundPlayerInfoUpdatePacket$actions(Object packet) {
        return ((ClientboundPlayerInfoUpdatePacket) packet).actions();
    }

    @Override
    public Object constructor$ClientboundPlayerInfoUpdatePacket(EnumSet actions, List entries) {
        return new ClientboundPlayerInfoUpdatePacket((EnumSet<ClientboundPlayerInfoUpdatePacket.Action>) actions, (List<ClientboundPlayerInfoUpdatePacket.Entry>) entries);
    }

    @Override
    public Optional method$RecipeManager$getRecipeFor(Object recipeManager, Object recipeType, Object recipeInput, Object level, Object resourceKeyOrLocation) {
        return ((RecipeManager) recipeManager).getRecipeFor((RecipeType) recipeType, (RecipeInput) recipeInput, (Level) level, (ResourceKey) resourceKeyOrLocation);
    }

    @Override
    public Object field$ClientboundPlayerInfoUpdatePacket$Entry$displayName(Object entry) {
        return ((ClientboundPlayerInfoUpdatePacket.Entry) entry).displayName();
    }

    @Override
    public Object constructor$ClientboundPlayerInfoUpdatePacket$Entry(Object entry, Object newDisplayName) {
        ClientboundPlayerInfoUpdatePacket.Entry e = (ClientboundPlayerInfoUpdatePacket.Entry) entry;
        return new ClientboundPlayerInfoUpdatePacket.Entry(
                e.profileId(),
                e.profile(),
                e.listed(),
                e.latency(),
                e.gameMode(),
                (Component) newDisplayName,
                e.showHat(),
                e.listOrder(),
                e.chatSession()
        );
    }

    @Override
    public Object field$CraftItemStack$handle(ItemStack itemStack) {
        return CraftItemStack.unwrap(itemStack);
    }

    @Override
    public Object field$ServerPlayer$gameMode(Object player) {
        return ((ServerPlayer) player).gameMode;
    }

    @Override
    public void field$Player$mayBuild(Object player, boolean can) {
        ((net.minecraft.world.entity.player.Player) player).getAbilities().mayBuild = can;
    }

    @Override
    public boolean field$Player$mayBuild(Object player) {
        return ((net.minecraft.world.entity.player.Player) player).getAbilities().mayBuild;
    }

    @Override
    public double method$Player$getInteractionRange(Object player) {
        return Optional.ofNullable(((net.minecraft.world.entity.player.Player) player).getAttribute(Attributes.BLOCK_INTERACTION_RANGE)).map(AttributeInstance::getValue).orElse(4.5d);
    }

    @Override
    public int field$MinecraftServer$currentTick() {
        return MinecraftServer.currentTick;
    }

    @Override
    public float method$BlockStateBase$getDestroyProgress(Object blockState, Object player, Object level, Object blockPos) {
        return ((BlockBehaviour.BlockStateBase) blockState).getDestroyProgress((ServerPlayer) player, (BlockGetter) level, (BlockPos) blockPos);
    }

    @Override
    public boolean method$ItemStack$isCorrectToolForDrops(Object itemStack, Object blockState) {
        return ((net.minecraft.world.item.ItemStack) itemStack).isCorrectToolForDrops((BlockState) blockState);
    }

    @Override
    public boolean method$Player$hasCorrectToolForDrops(Object player, Object state) {
        return ((net.minecraft.world.entity.player.Player) player).hasCorrectToolForDrops((BlockState) state);
    }

    @Override
    public Object constructor$ClientboundBlockDestructionPacket(int entityId, Object blockPos, int stage) {
        return new ClientboundBlockDestructionPacket(entityId, (BlockPos) blockPos, stage);
    }

    @Override
    public Object constructor$ClientboundLevelEventPacket(int id, Object blockPos, int data, boolean global) {
        return new ClientboundLevelEventPacket(id, (BlockPos) blockPos, data, global);
    }

    @Override
    public Object constructor$BlockInWorld(Object level, Object blockPos, boolean loadChunk) {
        return new BlockInWorld((LevelReader) level, (BlockPos) blockPos, loadChunk);
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
    public Object method$Direction$getOpposite(Object direction) {
        return ((Direction) direction).getOpposite();
    }

    @Override
    public Object method$BlockPos$relative(Object blockPos, Object direction) {
        return ((BlockPos) blockPos).relative((Direction) direction);
    }

    @Override
    public String field$ClientboundResourcePackPushPacket$url(Object packet) {
        return ((ClientboundResourcePackPushPacket) packet).url();
    }

    @Override
    public Object constructor$ClientboundResourcePackPushPacket(UUID uuid, String url, String sha1, boolean kick, Object component) {
        return new ClientboundResourcePackPushPacket(uuid, url, sha1, kick, Optional.ofNullable((Component) component));
    }

    @Override
    public Object constructor$ClientboundResourcePackPopPacket(UUID uuid) {
        return new ClientboundResourcePackPopPacket(Optional.ofNullable(uuid));
    }

    @Override
    public UUID field$ClientboundResourcePackPushPacket$uuid(Object packet) {
        return ((ClientboundResourcePackPushPacket) packet).id();
    }

    @Override
    public Object constructor$ServerboundResourcePackPacket$SUCCESSFULLY_LOADED(UUID uuid) {
        return new ServerboundResourcePackPacket(uuid, ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED);
    }

    @Override
    public Object method$CraftEntityType$toNMSEntityType(org.bukkit.entity.EntityType entityType) {
        return CraftEntityType.bukkitToMinecraft(entityType);
    }

    @Override
    public boolean method$BonemealableBlock$isValidBonemealTarget(Object block, Object level, Object blockPos, Object state) {
        return ((BonemealableBlock) block).isValidBonemealTarget((LevelReader) level, (BlockPos) blockPos, (BlockState) state);
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
        if (tag instanceof StringTag(String value)) {
            return value;
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
    public Object method$ServerChunkCache$getChunk(Object serverChunkCache, int x, int z, boolean load) {
        return ((ServerChunkCache) serverChunkCache).getChunk(x, z, load);
    }

    @Override
    public boolean method$LightEngine$hasDifferentLightProperties(Object oldState, Object newState) {
        return LightEngine.hasDifferentLightProperties((BlockState) oldState, (BlockState) newState);
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
            net.minecraft.world.item.ItemStack.validatedStreamCodec(net.minecraft.world.item.ItemStack.OPTIONAL_UNTRUSTED_STREAM_CODEC).apply(ByteBufCodecs::trackDepth);

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
    public void method$ServerPlayerConnection$send(Object connection, Object packet) {
        ((ServerPlayerConnection) connection).send((Packet<?>) packet);
    }

    @Override
    public Map<ConnectionState, Map<PacketFlow, Map<Class<?>, Integer>>> gamePacketIdsByClazz() {
        throw new UnsupportedVersionException();
    }

    @Override
    public Map<ConnectionState, Map<PacketFlow, Map<String, Integer>>> gamePacketIdsByName() {
        Map<ConnectionState, Map<net.momirealms.craftengine.core.plugin.network.PacketFlow, Map<String, Integer>>> allPacketIdsByName = new HashMap<>();
        Map<ConnectionProtocol, List<ProtocolInfo.Details>> collect = Stream.of(
                HandshakeProtocols.SERVERBOUND_TEMPLATE,
                StatusProtocols.CLIENTBOUND_TEMPLATE,
                StatusProtocols.SERVERBOUND_TEMPLATE,
                LoginProtocols.CLIENTBOUND_TEMPLATE,
                LoginProtocols.SERVERBOUND_TEMPLATE,
                ConfigurationProtocols.CLIENTBOUND_TEMPLATE,
                ConfigurationProtocols.SERVERBOUND_TEMPLATE,
                GameProtocols.CLIENTBOUND_TEMPLATE,
                GameProtocols.SERVERBOUND_TEMPLATE
        ).map(ProtocolInfo.DetailsProvider::details).collect(Collectors.groupingBy(ProtocolInfo.Details::id));
        for (Map.Entry<ConnectionProtocol, List<ProtocolInfo.Details>> entry : collect.entrySet()) {
            Map<net.momirealms.craftengine.core.plugin.network.PacketFlow, Map<String, Integer>> protocolPacketIdsByName = new HashMap<>();
            allPacketIdsByName.put(ConnectionState.valueOf(entry.getKey().name().toUpperCase(Locale.ROOT)), protocolPacketIdsByName);
            Map<String, Integer> serverBoundIds = new HashMap<>();
            Map<String, Integer> clientBoundIds = new HashMap<>();
            protocolPacketIdsByName.put(net.momirealms.craftengine.core.plugin.network.PacketFlow.SERVERBOUND, serverBoundIds);
            protocolPacketIdsByName.put(net.momirealms.craftengine.core.plugin.network.PacketFlow.CLIENTBOUND, clientBoundIds);
            for (ProtocolInfo.Details protocol : entry.getValue()) {
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
    public Object method$ServerLevel$getEntityLookup(Object serverLevel) {
        return ((ServerLevel) serverLevel).moonrise$getEntityLookup();
    }

    @Override
    public Object constructor$InjectedHashedStack(Object hashedStack, net.momirealms.craftengine.core.entity.player.Player player) {
        return new InjectedHashedStack((HashedStack) hashedStack, player);
    }

    @Override
    public Inventory createSimpleStorageContainer(InventoryHolder owner, int size, boolean canPlaceItem, boolean canTakeItem) {
        return new CraftInventory(new SimpleStorageContainer(owner, size, canPlaceItem, canTakeItem));
    }

}
