package net.momirealms.craftengine.bukkit.nms.v1_20_3;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.papermc.paper.chunk.system.entity.EntityLookup;
import io.papermc.paper.world.ChunkEntitySlices;
import net.minecraft.core.*;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.nms.UnsupportedVersionException;
import net.momirealms.craftengine.core.util.ReflectionUtils;
import net.momirealms.craftengine.core.world.chunk.InjectedHolder;
import org.bukkit.Chunk;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_20_R3.CraftParticle;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntityType;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class FastNMSImpl extends FastNMS {

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
    public Object method$PalettedContainer$getAndSet(Object palettedContainer, int x, int y, int z, Object blockState) {
        return ((PalettedContainer) palettedContainer).getAndSet(x, y, z, blockState);
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
        return ((ServerChunkCache) serverChunkCache).getChunkAtIfLoadedMainThread(x, z);
    }

    @Override
    public Object field$LevelChunkSection$states(Object section) {
        return ((LevelChunkSection) section).states;
    }

    @Override
    public Object[] method$ChunkAccess$getSections(Object chunk) {
        return ((LevelChunk) chunk).getSections();
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
        return new ClientboundSetPassengersPacket(byteBuf);
    }

    @Override
    public boolean method$ServerLevel$isPreventingStatusUpdates(Object world, int x, int z) {
        ChunkEntitySlices slices = ((ServerLevel) world).getEntityLookup().getChunk(x, z);
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
        return new ClientboundLightUpdatePacket(
                (ChunkPos) chunkPos,
                (LevelLightEngine) lightEngine,
                skyChangedLightSectionFilter,
                blockChangedLightSectionFilter
        );
    }

    @Override
    public void method$ServerChunkCache$blockChanged(Object chunkCache, Object blockPos) {
        ((ServerChunkCache) chunkCache).blockChanged((BlockPos) blockPos);
    }

    @Override
    public void method$Connection$send(Object connection, Object packet) {
        ((Connection) connection).send((Packet<?>) packet);
    }

    @Override
    public List<Object> method$ChunkHolder$getPlayers(Object chunkHolder) {
        return (List) ((ChunkHolder) chunkHolder).getPlayers(false);
    }

    @Override
    public Object constructor$ClientboundBundlePacket(List<Object> packets) {
        return new ClientboundBundlePacket((List) packets);
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
    public void method$Level$levelEvent(Object level, int eventId, Object blockPos, int stateId) {
        ((Level) level).levelEvent(eventId, (BlockPos) blockPos, stateId);
    }

    @Override
    public Iterable<Object> method$ClientboundBundlePacket$subPackets(Object packet) {
        return (Iterable) ((ClientboundBundlePacket) packet).subPackets();
    }

    @Override
    public Object method$ResourceLocation$fromNamespaceAndPath(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    @Override
    public Object field$SoundEvent$location(Object soundEvent) {
        return ((SoundEvent) soundEvent).getLocation();
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
        return Component.Serializer.fromJson(element);
    }

    @Override
    public Object method$Component$Serializer$fromJson(String json) {
        return Component.Serializer.fromJson(json);
    }

    @Override
    public String method$Component$Serializer$toJson(Object component) {
        return Component.Serializer.toJson((Component) component);
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
        ((SoundEvent) soundEvent).writeToNetwork(new FriendlyByteBuf(buffer));
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
        return ((RecipeManager) recipeManager).getRecipeFor((RecipeType) recipeType, (Container) recipeInput, (Level) level, (ResourceLocation) resourceKeyOrLocation);
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
                e.chatSession()
        );
    }

    @Override
    public void method$ItemStack$setComponent(Object itemStack, Object resourceLocation, Object component) {
        throw new UnsupportedVersionException();
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
        return 4.5d;
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
        return ((net.minecraft.world.item.ItemStack) itemStack).hasAdventureModeBreakTagForBlock(BuiltInRegistries.BLOCK, (BlockInWorld) blockInWorld);
    }

    @Override
    public boolean method$ItemStack$canPlaceInAdventureMode(Object itemStack, Object blockInWorld) {
        return ((net.minecraft.world.item.ItemStack) itemStack).hasAdventureModePlaceTagForBlock(BuiltInRegistries.BLOCK, (BlockInWorld) blockInWorld);
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
        return new ClientboundResourcePackPushPacket(uuid, url, sha1, kick, (Component) component);
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
    public Object constructor$ClientboundSetEntityDataPacket(int entityId, List data) {
        return new ClientboundSetEntityDataPacket(entityId, data);
    }

    @Override
    public Object constructor$ClientboundAddEntityPacket(int id, UUID uuid,
                                                         double x, double y, double z, float xRot, float yRot,
                                                         Object type, int data, Object deltaMovement, double yHeadRot) {
        return new ClientboundAddEntityPacket(id, uuid, x, y, z, xRot, yRot, (EntityType<?>) type, data, (Vec3) deltaMovement, yHeadRot);
    }

    @Override
    public Object method$SynchedEntityData$DataValue$create(Object entityDataAccessor, Object value) {
        return SynchedEntityData.DataValue.create((EntityDataAccessor) entityDataAccessor, value);
    }

    @Override
    public Object constructor$EntityDataAccessor(int id, Object serializer) {
        return new EntityDataAccessor<>(id, (EntityDataSerializer) serializer);
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
    public final boolean checkEntityCollision(Object level, List<Object> aabbs, double x, double y, double z) {
        if (aabbs.isEmpty()) return true;
        ServerLevel serverLevel = (ServerLevel) level;
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
            if (!entity.isRemoved() && entity.blocksBuilding && Shapes.joinIsNotEmpty(finalShape, Shapes.create(entity.getBoundingBox()), BooleanOp.AND)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void method$ItemStack$applyComponents(Object itemStack, Object component) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object method$ItemStack$getItem(Object itemStack) {
        return ((net.minecraft.world.item.ItemStack) itemStack).getItem();
    }

    @Override
    public Object method$ItemStack$transmuteCopy(Object itemStack1, Object item, int count) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object method$ItemStack$getComponentsPatch(Object itemStack) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object method$ItemStack$getComponent(Object itemStack, Object type) {
        throw new UnsupportedVersionException();
    }

    @Override
    public boolean method$ItemStack$hasComponent(Object itemStack, Object type) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object method$ItemStack$removeComponent(Object itemStack, Object type) {
        throw new UnsupportedVersionException();
    }

    @Override
    public String getCustomItemId(Object itemStack) {
        net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) itemStack;
        CompoundTag tag = nmsStack.getTag();
        if (tag == null) return null;
        return tag.getString("craftengine:id");
    }

    @Override
    public void setCustomItemId(Object itemStack, String id) {
        net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) itemStack;
        CompoundTag tag = nmsStack.getOrCreateTag();
        tag.putString("craftengine:id", id);
    }

    @Override
    public void method$LevelChunk$markUnsaved(Object chunk) {
        ((LevelChunk) chunk).setUnsaved(true);
    }

    @Override
    public boolean method$LevelChunk$isUnsaved(Object chunk) {
        return ((LevelChunk) chunk).isUnsaved();
    }

    @Override
    public Object constructor$ClientboundSystemChatPacket(Object component, boolean overlay) {
        return new ClientboundSystemChatPacket((Component) component, overlay);
    }

    @Override
    public Object method$ServerChunkCache$getChunk(Object serverChunkCache, int x, int z, boolean load) {
        return ((ServerChunkCache) serverChunkCache).getChunk(x, z, load);
    }

    @Override
    public Object constructor$LevelChunkSection(Object section) {
        LevelChunkSection levelChunkSection = (LevelChunkSection) section;
        return new LevelChunkSection(levelChunkSection.getStates(), (PalettedContainer<Holder<Biome>>) levelChunkSection.getBiomes());
    }

    @Override
    public Object field$LevelChunkSection$biomes(Object section) {
        return ((LevelChunkSection) section).getBiomes();
    }

    @Override
    public boolean field$Entity$wasTouchingWater(Object entity) {
        return ((Entity) entity).wasTouchingWater;
    }

    @Override
    public Object constructor$ClientboundUpdateTagsPacket(Map<?, ?> tags) {
        return new ClientboundUpdateTagsPacket((Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload>) tags);
    }

    @Override
    public Object constructor$ClientboundActionBarPacket(Object component) {
        return new ClientboundSetActionBarTextPacket((Component) component);
    }

    @Override
    public Object constructor$ClientboundSetTitleTextPacket(Object component) {
        return new ClientboundSetTitleTextPacket((Component) component);
    }

    @Override
    public Object constructor$ClientboundSetSubtitleTextPacket(Object component) {
        return new ClientboundSetSubtitleTextPacket((Component) component);
    }

    @Override
    public Object constructor$ClientboundSetTitlesAnimationPacket(int fadeIn, int stay, int fadeOut) {
        return new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);
    }

    @Override
    public Object field$Player$containerMenu(Object player) {
        return ((net.minecraft.world.entity.player.Player) player).containerMenu;
    }

    @Override
    public Particle method$CraftParticle$toBukkit(Object nmsParticle) {
        return CraftParticle.minecraftToBukkit((ParticleType) nmsParticle);
    }

    @Override
    public boolean method$SignalGetter$hasNeighborSignal(Object level, Object blockPos) {
        return ((SignalGetter) level).hasNeighborSignal((BlockPos) blockPos);
    }

    @Override
    public Object method$BlockState$getBlock(Object blockState) {
        return ((BlockState) blockState).getBlock();
    }

    @Override
    public Object method$BlockState$getShape(Object blockState, Object level, Object blockPos, Object collisionContext) {
        return ((BlockState) blockState).getShape((BlockGetter) level, (BlockPos) blockPos, (CollisionContext) collisionContext);
    }

    @Override
    public Object method$BlockState$getCollisionShape(Object blockState, Object level, Object blockPos, Object collisionContext) {
        return ((BlockState) blockState).getCollisionShape((BlockGetter) level, (BlockPos) blockPos, (CollisionContext) collisionContext);
    }

    @Override
    public Object method$BlockState$getBlockSupportShape(Object blockState, Object level, Object blockPos) {
        return ((BlockState) blockState).getBlockSupportShape((BlockGetter) level, (BlockPos) blockPos);
    }

    @Override
    public boolean method$LightEngine$hasDifferentLightProperties(Object oldState, Object newState, Object blockGetter, Object blockPos) {
        return LightEngine.hasDifferentLightProperties((BlockGetter) blockGetter, (BlockPos) blockPos, (BlockState) oldState, (BlockState) newState);
    }

    @Override
    public Object constructor$FriendlyByteBuf(ByteBuf buf) {
        return new FriendlyByteBuf(buf);
    }

    @Override
    public ItemStack method$FriendlyByteBuf$readItem(Object buf) {
        return CraftItemStack.asCraftMirror(((FriendlyByteBuf) buf).readItem());
    }

    @Override
    public void method$FriendlyByteBuf$writeItem(Object buf, ItemStack itemStack) {
        ((FriendlyByteBuf) buf).writeItem(CraftItemStack.unwrap(itemStack));
    }

    @Override
    public ItemStack method$FriendlyByteBuf$readUntrustedItem(Object buf) {
        throw new UnsupportedVersionException();
    }

    @Override
    public void method$FriendlyByteBuf$writeUntrustedItem(Object buf, ItemStack itemStack) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Codec method$DataComponentType$codec(Object componentType) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object field$ItemStack$getOrCreateTag(Object itemStack) {
        return ((net.minecraft.world.item.ItemStack) itemStack).getOrCreateTag();
    }

    @Override
    public Object constructor$ShortTag(short s) {
        return ShortTag.valueOf(s);
    }

    @Override
    public short method$ShortTag$value(Object shortTag) {
        return ((ShortTag) shortTag).getAsShort();
    }

    @Override
    public Object constructor$IntTag(int i) {
        return IntTag.valueOf(i);
    }

    @Override
    public int method$IntTag$value(Object intTag) {
        return ((IntTag) intTag).getAsInt();
    }

    @Override
    public Object constructor$LongTag(long l) {
        return LongTag.valueOf(l);
    }

    @Override
    public long method$LongTag$value(Object longTag) {
        return ((LongTag) longTag).getAsLong();
    }

    @Override
    public Object constructor$ByteTag(byte b) {
        return ByteTag.valueOf(b);
    }

    @Override
    public byte method$ByteTag$value(Object byteTag) {
        return ((ByteTag) byteTag).getAsByte();
    }

    @Override
    public Object constructor$FloatTag(float f) {
        return FloatTag.valueOf(f);
    }

    @Override
    public float method$FloatTag$value(Object floatTag) {
        return ((FloatTag) floatTag).getAsFloat();
    }

    @Override
    public Object constructor$DoubleTag(double d) {
        return DoubleTag.valueOf(d);
    }

    @Override
    public double method$DoubleTag$value(Object doubleTag) {
        return ((DoubleTag) doubleTag).getAsDouble();
    }

    @Override
    public Object constructor$ByteArrayTag(byte[] bytes) {
        return new ByteArrayTag(bytes);
    }

    @Override
    public byte[] method$ByteArrayTag$value(Object byteArrayTag) {
        return ((ByteArrayTag) byteArrayTag).getAsByteArray();
    }

    @Override
    public Object constructor$IntArrayTag(int[] ints) {
        return new IntArrayTag(ints);
    }

    @Override
    public int[] method$IntArrayTag$value(Object intArrayTag) {
        return ((IntArrayTag) intArrayTag).getAsIntArray();
    }

    @Override
    public Object constructor$LongArrayTag(long[] longs) {
        return new LongArrayTag(longs);
    }

    @Override
    public long[] method$LongArrayTag$value(Object longArrayTag) {
        return ((LongArrayTag) longArrayTag).getAsLongArray();
    }

    @Override
    public Object constructor$StringTag(String string) {
        return StringTag.valueOf(string);
    }

    @Override
    public String method$StringTag$value(Object stringTag) {
        return ((StringTag) stringTag).getAsString();
    }

    @Override
    public Object constructor$ListTag() {
        return new ListTag();
    }

    @Override
    public Object method$ListTag$get(Object listTag, int index) {
        return ((ListTag) listTag).get(index);
    }

    @Override
    public void method$ListTag$add(Object listTag, int index, Object value) {
        ((ListTag) listTag).addTag(index, (Tag) value);
    }

    @Override
    public Object method$ListTag$remove(Object listTag, int index) {
        return ((ListTag) listTag).remove(index);
    }

    @Override
    public Object method$CompoundTag$get(Object compoundTag, String key) {
        return ((CompoundTag) compoundTag).get(key);
    }

    @Override
    public void method$CompoundTag$put(Object compoundTag, String key, Object value) {
        ((CompoundTag) compoundTag).put(key, (Tag) value);
    }

    @Override
    public void method$CompoundTag$remove(Object compoundTag, String key) {
        ((CompoundTag) compoundTag).remove(key);
    }

    @Override
    public Object constructor$CompoundTag() {
        return new CompoundTag();
    }

    @Override
    public RegistryAccess registryAccess() {
        return MinecraftServer.getServer().registryAccess();
    }

    @Override
    public Object method$StatePredicate$always(boolean trueOrFalse) {
        return (BlockBehaviour.StatePredicate) (blockState, blockGetter, blockPos) -> trueOrFalse;
    }

    @Override
    public Object method$ResourceKey$create(Object registry, Object resourceLocation) {
        return ResourceKey.create((ResourceKey) registry, (ResourceLocation) resourceLocation);
    }

    @Override
    public List<Object> field$SimpleContainer$items(Object simpleContainer) {
        return (List) ((SimpleContainer) simpleContainer).items;
    }

    @Override
    public Object field$SingleRecipeInput$item(Object singleRecipeInput) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object field$AbstractFurnaceBlockEntity$getItem(Object entity, int slot) {
        return ((AbstractFurnaceBlockEntity) entity).getItem(slot);
    }

    @Override
    public Object method$TagParser$parseCompoundFully(String nbt) throws CommandSyntaxException {
        return TagParser.parseTag(nbt);
    }

    @Override
    public Object method$Registry$getValue(Object registry, Object resourceLocation) {
        return ((Registry) registry).get((ResourceLocation) resourceLocation);
    }

    @Override
    public Object constructor$ItemStack(Object item, int count) {
        return new net.minecraft.world.item.ItemStack((ItemLike) item, count);
    }

    @Override
    public void method$LevelAccessor$scheduleBlockTick(Object levelAccessor, Object blockPos, Object block, int ticks) {
        ((LevelAccessor) levelAccessor).scheduleTick((BlockPos) blockPos, (net.minecraft.world.level.block.Block) block, ticks);
    }

    @Override
    public void method$ItemStack$setTag(Object itemStack, Object compoundTag) {
        ((net.minecraft.world.item.ItemStack) itemStack).setTag((CompoundTag) compoundTag);
    }

    @Override
    public Object method$AbstractContainerMenu$getCarried(Object menu) {
        return ((AbstractContainerMenu) menu).getCarried();
    }

    @Override
    public void method$AbstractContainerMenu$broadcastFullState(Object menu) {
        ((AbstractContainerMenu) menu).broadcastFullState();
    }

    @Override
    public List<Object> field$AbstractContainerMenu$dataSlots(Object menu) {
        return (List) ((AbstractContainerMenu) menu).dataSlots;
    }

    @Override
    public int method$DataSlot$get(Object dataSlot) {
        return ((DataSlot) dataSlot).get();
    }

    @Override
    public void field$Player$containerMenu(Object player, Object menu) {
        ((net.minecraft.world.entity.player.Player) player).containerMenu = (AbstractContainerMenu) menu;
    }

    @Override
    public int field$AbstractContainerMenu$containerId(Object containerMenu) {
        return ((AbstractContainerMenu) containerMenu).containerId;
    }

    @Override
    public Object method$AbstractContainerMenu$getSlot(Object containerMenu, int slot) {
        return ((AbstractContainerMenu) containerMenu).getSlot(slot);
    }

    @Override
    public Object constructor$ClientboundContainerSetDataPacket(int containerId, int id, int data) {
        return new ClientboundContainerSetDataPacket(containerId, id, data);
    }

    @Override
    public Object method$Slot$getItem(Object slot) {
        return ((Slot) slot).getItem();
    }

    @Override
    public Object method$Block$defaultState(Object block) {
        return ((net.minecraft.world.level.block.Block) block).defaultBlockState();
    }

    @Override
    public boolean method$BlockStateBase$isSignalSource(Object blockState) {
        return ((BlockBehaviour.BlockStateBase) blockState).isSignalSource();
    }

    @Override
    public void method$LevelAccessor$scheduleFluidTick(Object levelAccessor, Object blockPos, Object fluid, int ticks) {
        ((LevelAccessor) levelAccessor).scheduleTick((BlockPos) blockPos, (Fluid) fluid, ticks);
    }

    @Override
    public Object method$Level$getFluidState(Object level, Object blockPos) {
        return ((Level) level).getFluidState((BlockPos) blockPos);
    }

    @Override
    public Object method$FluidState$getType(Object fluidState) {
        return ((FluidState) fluidState).getType();
    }

    @Override
    public boolean method$Explosion$canTriggerBlocks(Object explosion) {
        throw new UnsupportedVersionException();
    }

    @Override
    public boolean method$BlockStateBase$canSurvive(Object blockState, Object level, Object blockPos) {
        return ((BlockState) blockState).canSurvive((LevelReader) level, (BlockPos) blockPos);
    }

    @Override
    public boolean method$BlockStateBase$isCollisionShapeFullBlock(Object blockState, Object level, Object blockPos) {
        return ((BlockState) blockState).isCollisionShapeFullBlock((BlockGetter) level, (BlockPos) blockPos);
    }

    @Override
    public String method$ResourceLocation$namespace(Object resourceLocation) {
        return ((ResourceLocation) resourceLocation).getNamespace();
    }

    @Override
    public String method$ResourceLocation$path(Object resourceLocation) {
        return ((ResourceLocation) resourceLocation).getPath();
    }

    @Override
    public boolean method$BlockStateBase$is(Object blockState, Object tag) {
        return ((BlockBehaviour.BlockStateBase) blockState).is(((TagKey) tag));
    }

    @Override
    public boolean method$BlockStateBase$isAir(Object blockState) {
        return ((BlockBehaviour.BlockStateBase) blockState).isAir();
    }

    @Override
    public boolean method$Block$canSupportRigidBlock(Object level, Object pos) {
        return net.minecraft.world.level.block.Block.canSupportRigidBlock((Level) level, (BlockPos) pos);
    }

    @Override
    public boolean method$Block$canSupportCenter(Object level, Object pos, Object direction) {
        return net.minecraft.world.level.block.Block.canSupportCenter((Level) level, (BlockPos) pos, (Direction) direction);
    }

    @Override
    public void method$Level$setBlocksDirty(Object level, Object blockPos, Object oldState, Object newState) {
        ((Level) level).setBlocksDirty((BlockPos) blockPos, (BlockState) oldState, (BlockState) newState);
    }

    @Override
    public boolean method$BlockStateBase$isFaceSturdy(Object blockState, Object level, Object pos, Object face, Object supportType) {
        return ((BlockBehaviour.BlockStateBase) blockState).isFaceSturdy((BlockGetter) level, (BlockPos) pos, (Direction) face, (SupportType) supportType);
    }

    @Override
    public int method$EntityGetter$getEntitiesOfClass(Object entityGetter, Object aabb, Class entityClass) {
        return ((EntityGetter) entityGetter).getEntitiesOfClass(entityClass, (AABB) aabb, EntitySelector.NO_SPECTATORS.and(entity -> !entity.isIgnoringBlockTriggers())).size();
    }

    @Override
    public Object method$AABB$move(Object aabb, Object pos) {
        return ((AABB) aabb).move((BlockPos) pos);
    }

    @Override
    public void method$Level$updateNeighborsAt(Object levelAccessor, Object blockPos, Object block) {
        ((Level) levelAccessor).updateNeighborsAt((BlockPos) blockPos, (net.minecraft.world.level.block.Block) block);
    }

    @Override
    public Object field$Entity$trackedEntity(Object entity) {
        return ((Entity) entity).tracker;
    }

    @Override
    public Object field$ChunkMap$TrackedEntity$serverEntity(Object trackedEntity) {
        return ((ChunkMap.TrackedEntity) trackedEntity).serverEntity;
    }

    @Override
    public boolean method$AbstractArrow$isInGround(Object entity) {
        return ((AbstractArrow) entity).inGround;
    }

    @Override
    public void method$ServerPlayerConnection$send(Object connection, Object packet) {
        ((ServerPlayerConnection) connection).send((Packet<?>) packet);
    }

    @Override
    public Map method$TagNetworkSerialization$serializeTagsToNetwork() {
        return TagNetworkSerialization.serializeTagsToNetwork(MinecraftServer.getServer().registries());
    }

    @Override
    public void method$TagNetworkSerialization$NetworkPayload$write(Object networkPayload, Object buffer) {
        ((TagNetworkSerialization.NetworkPayload) networkPayload).write(new FriendlyByteBuf((ByteBuf) buffer));
    }

    @Override
    public Object method$TagNetworkSerialization$NetworkPayload$read(Object buffer) {
        return TagNetworkSerialization.NetworkPayload.read(new FriendlyByteBuf((ByteBuf) buffer));
    }

    @Override
    public Object method$Registry$getKey(Object registry, Object value) {
        return ((Registry) registry).getKey(value);
    }

    @Override
    public boolean method$ItemStack$isEmpty(Object stack) {
        return ((net.minecraft.world.item.ItemStack) stack).isEmpty();
    }

    @Override
    public ItemStack method$CraftItemStack$asCraftCopy(ItemStack stack) {
        return CraftItemStack.asCraftCopy(stack);
    }

    @Override
    public Object method$Item$components(Object item) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object method$DataComponentMap$get(Object dataComponentMap, Object componentType) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object method$TagKey$create(Object registry, Object location) {
        return TagKey.create((ResourceKey<? extends Registry<Object>>) registry, (ResourceLocation) location);
    }

    @Override
    public boolean method$BlockStateBase$isReplaceable(Object blockState) {
        return ((BlockState) blockState).canBeReplaced();
    }

    @Override
    public void method$ClientboundSetEntityDataPacket$pack(List<?> dataValues, Object friendlyByteBuf) {
        FriendlyByteBuf buf = new FriendlyByteBuf((ByteBuf) friendlyByteBuf);
        for (SynchedEntityData.DataValue<?> dataValue : ((List<SynchedEntityData.DataValue<?>>) dataValues)) {
            dataValue.write(buf);
        }
        buf.writeByte(255);
    }

    @Override
    public List<Object> method$ClientboundSetEntityDataPacket$unpack(Object friendlyByteBuf) {
        FriendlyByteBuf buf = new FriendlyByteBuf((ByteBuf) friendlyByteBuf);
        List<Object> list = new ArrayList();
        int i;
        while ((i = buf.readUnsignedByte()) != 255) {
            list.add(SynchedEntityData.DataValue.read(buf, i));
        }
        return list;
    }

    @Override
    public int method$ClientboundEntityPositionSyncPacket$id(Object packet) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object field$ClientboundEntityPositionSyncPacket$values(Object packet) {
        throw new UnsupportedVersionException();
    }

    @Override
    public boolean field$ClientboundEntityPositionSyncPacket$onGround(Object packet) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object field$PositionMoveRotation$position(Object values) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object field$PositionMoveRotation$deltaMovement(Object values) {
        throw new UnsupportedVersionException();
    }

    @Override
    public float field$PositionMoveRotation$yRot(Object values) {
        throw new UnsupportedVersionException();
    }

    @Override
    public float field$PositionMoveRotation$xRot(Object values) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object constructor$PositionMoveRotation(Object position, Object deltaMovement, float yRot, float xRot) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object constructor$ClientboundEntityPositionSyncPacket(int entityId, Object values, boolean onGround) {
        throw new UnsupportedVersionException();
    }

    @Override
    public short field$ClientboundMoveEntityPacket$xa(Object packet) {
        return ((ClientboundMoveEntityPacket) packet).getXa();
    }

    @Override
    public short field$ClientboundMoveEntityPacket$ya(Object packet) {
        return ((ClientboundMoveEntityPacket) packet).getYa();
    }

    @Override
    public short field$ClientboundMoveEntityPacket$za(Object packet) {
        return ((ClientboundMoveEntityPacket) packet).getZa();
    }

    @Override
    public byte field$ClientboundMoveEntityPacket$yRot(Object packet) {
        return ((ClientboundMoveEntityPacket) packet).getyRot();
    }

    @Override
    public byte field$ClientboundMoveEntityPacket$xRot(Object packet) {
        return ((ClientboundMoveEntityPacket) packet).getxRot();
    }

    @Override
    public boolean field$ClientboundMoveEntityPacket$onGround(Object packet) {
        return ((ClientboundMoveEntityPacket) packet).isOnGround();
    }

    @Override
    public Object constructor$ClientboundMoveEntityPacket$PosRot(int entityId, short xa, short ya, short za, byte yRot, byte xRot, boolean onGround) {
        return new ClientboundMoveEntityPacket.PosRot(entityId, xa, ya, za, yRot, xRot, onGround);
    }

    @Override
    public Map<String, Map<Class<?>, Integer>> gamePacketIdsByClazz() {
        Map<String, Map<Class<?>, Integer>> gamePacketIdsByClazz = new HashMap<>();
        Map<Class<?>, Integer> serverBoundIds = new HashMap<>();
        Map<Class<?>, Integer> clientBoundIds = new HashMap<>();
        gamePacketIdsByClazz.put("serverbound", serverBoundIds);
        gamePacketIdsByClazz.put("clientbound", clientBoundIds);
        ConnectionProtocol.PLAY.getPacketsByIds(PacketFlow.SERVERBOUND).forEach((id, packet) -> serverBoundIds.put(packet, id));
        ConnectionProtocol.PLAY.getPacketsByIds(PacketFlow.CLIENTBOUND).forEach((id, packet) -> clientBoundIds.put(packet, id));
        return gamePacketIdsByClazz;
    }

    @Override
    public Map<String, Map<String, Integer>> gamePacketIdsByName() {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object method$FriendlyByteBuf$readById(Object buffer, Object idMap) {
        return new FriendlyByteBuf((ByteBuf) buffer).readById((IdMap) idMap);
    }

    @Override
    public Object method$ClientboundLevelParticlesPacket$readParticle(Object buffer, Object particleType) {
        return this.readParticle(new FriendlyByteBuf((ByteBuf) buffer), (ParticleType) particleType);
    }

    private <T extends ParticleOptions> T readParticle(FriendlyByteBuf buf, ParticleType<T> type) {
        return type.getDeserializer().fromNetwork(type, buf);
    }

    @Override
    public Object method$BlockParticleOption$getType(Object particle) {
        return ((BlockParticleOption) particle).getType();
    }

    @Override
    public Object constructor$BlockParticleOption(Object particleType, Object blockState) {
        return new BlockParticleOption((ParticleType<BlockParticleOption>) particleType, (BlockState) blockState);
    }

    @Override
    public void method$FriendlyByteBuf$writeId(Object buffer, Object particle, Object idMap) {
        (new FriendlyByteBuf((ByteBuf) buffer)).writeId((IdMap) idMap, ((ParticleOptions) particle).getType());
    }

    @Override
    public void method$ParticleOptions$writeToNetwork(Object particle, Object buffer) {
        ((ParticleOptions) particle).writeToNetwork(new FriendlyByteBuf((ByteBuf) buffer));
    }

    @Override
    public Object method$StreamCodec$decode(Object streamCodec, Object byteBuffer) {
        throw new UnsupportedVersionException();
    }

    @Override
    public void method$StreamCodec$encode(Object streamCodec, Object byteBuffer, Object value) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object method$SoundEvent$location(Object soundEvent) {
        return ((SoundEvent) soundEvent).getLocation();
    }

    @Override
    public Object constructor$SoundEvent(Object location, Optional<Float> fixedRange) {
        return fixedRange.map((f) -> SoundEvent.createFixedRangeEvent((ResourceLocation) location, f)).orElseGet(() -> SoundEvent.createVariableRangeEvent((ResourceLocation) location));
    }

    @Override
    public Optional<Float> method$SoundEvent$fixedRange(Object soundEvent) {
        float range1 = ((SoundEvent) soundEvent).getRange(100.0F);
        float range2 = ((SoundEvent) soundEvent).getRange(0.0F);
        if (range1 == 16.0F * 100.0F && range2 == 16.0F) {
            return Optional.empty();
        }
        return Optional.of(range1);
    }

    @Override
    public Object method$LootParams$Builder$getOptionalParameter(Object lootParamsBuilder, Object key) {
        return ((LootParams.Builder) lootParamsBuilder).getOptionalParameter((LootContextParam) key);
    }

    @Override
    public Object method$LootParams$Builder$getLevel(Object lootParamsBuilder) {
        return ((LootParams.Builder) lootParamsBuilder).getLevel();
    }

    @Override
    public Player method$ServerPlayer$getBukkitEntity(Object player) {
        return ((ServerPlayer) player).getBukkitEntity();
    }

    @Override
    public void method$Block$dropResources(Object state, Object level, Object pos) {
        net.minecraft.world.level.block.Block.dropResources((BlockState) state, (Level) level, (BlockPos) pos);
    }

    @Override
    public BlockRedstoneEvent method$CraftEventFactory$callRedstoneChange(Object world, Object pos, int oldCurrent, int newCurrent) {
        return CraftEventFactory.callRedstoneChange((Level) world, (BlockPos) pos, oldCurrent, newCurrent);
    }

    @Override
    public boolean method$Level$destroyBlock(Object level, Object pos, boolean drop) {
        return ((Level) level).destroyBlock((BlockPos) pos, drop);
    }

    @Override
    public Object method$itemStack$save(Object itemStack, Object compoundTag) {
        return ((net.minecraft.world.item.ItemStack) itemStack).save((CompoundTag) compoundTag);
    }

    @Override
    public Object method$ItemStack$getTag(Object itemStack) {
        return ((net.minecraft.world.item.ItemStack) itemStack).getTag();
    }

    @Override
    public Set method$CompoundTag$entrySet(Object compoundTag) {
        return ((CompoundTag) compoundTag).tags.entrySet();
    }

    @Override
    public Object method$CompoundTag$merge(Object tag1, Object tag2) {
        return ((CompoundTag) tag1).merge((CompoundTag) tag2);
    }

    @Override
    public Object method$CompoundTag$copy(Object compoundTag) {
        return ((CompoundTag) compoundTag).copy();
    }

    @Override
    public void method$Player$startSleepInBed(Object player, Object pos, boolean force) {
        ((net.minecraft.world.entity.player.Player) player).startSleepInBed((BlockPos) pos, force);
    }

    @Override
    public Object field$ServerboundResourcePackPacket$action(Object packet) {
        return ((ServerboundResourcePackPacket) packet).action();
    }

    @Override
    public UUID field$ServerboundResourcePackPacket$id(Object packet) {
        return ((ServerboundResourcePackPacket) packet).id();
    }

    @Override
    public Object method$Block$asItem(Object block) {
        return ((net.minecraft.world.level.block.Block) block).asItem();
    }

    @Override
    public Object method$RegistryAccess$lookupOrThrow(Object registryAccess, Object resourceKey) {
        return ((RegistryAccess) registryAccess).registryOrThrow((ResourceKey) resourceKey);
    }

    @Override
    public int method$Registry$getId(Object registry, Object value) {
        return ((Registry) registry).getId(value);
    }

    @Override
    public Optional<Object> method$Registry$getHolderByResourceLocation(Object registry, Object resourceLocation) {
        return ((Registry) registry).getHolder(ResourceKey.create(((Registry) registry).key(), (ResourceLocation) resourceLocation));
    }

    @Override
    public Optional<Object> method$Registry$getHolderByResourceKey(Object registry, Object resourceKey) {
        return ((Registry) registry).getHolder((ResourceKey) resourceKey);
    }

    @Override
    public Object method$ServerLevel$getEntityLookup(Object serverLevel) {
        return ((ServerLevel) serverLevel).getEntityLookup();
    }

    @Override
    public Object method$EntityLookup$get(Object entityLookup, int id) {
        return ((EntityLookup) entityLookup).get(id);
    }

    @Override
    public boolean field$BlockBehavior$hasCollision(Object block) {
        return ((BlockBehaviour) block).hasCollision;
    }
}
