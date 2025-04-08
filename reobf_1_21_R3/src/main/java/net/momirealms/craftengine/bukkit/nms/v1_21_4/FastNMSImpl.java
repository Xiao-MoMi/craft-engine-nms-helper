package net.momirealms.craftengine.bukkit.nms.v1_21_4;

import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.ChunkEntitySlices;
import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.util.ItemObfuscationSession;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.*;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class FastNMSImpl extends FastNMS {
    private static final RegistryAccess REGISTRY_ACCESS = MinecraftServer.getServer().registryAccess();

    // 以后简化代码的时候可以移除
    @Deprecated
    @Override
    public CollisionEntity createCollisionEntity(Object world, Object aabb, double x, double y, double z, boolean canProjectileHit) {
        return new CollisionInteraction(EntityType.INTERACTION, (Level) world, x, y, z, (AABB) aabb, canProjectileHit);
    }

    @Override
    public CollisionEntity createCollisionShulker(Object world, Object aabb, double x, double y, double z, boolean canProjectileHit) {
        return new CollisionShulker(EntityType.SHULKER, (Level) world, x, y, z, (AABB) aabb, canProjectileHit);
    }


    @Override
    public Object method$PalettedContainer$getAndSet(Object palettedContainer, int x, int y, int z, Object blockState) {
        PalettedContainer pc = (PalettedContainer) palettedContainer;
        return pc.getAndSet(x, y, z, blockState);
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
        BlockState blockStateBase = (BlockState) blockState;
        return blockStateBase.getLightEmission();
    }

    @Override
    public boolean method$BlockStateBase$canOcclude(Object blockState) {
        BlockState blockStateBase = (BlockState) blockState;
        return blockStateBase.canOcclude();
    }

    @Override
    public void method$LevelChunkSection$setBlockState(Object section, int x, int y, int z, Object blockState, boolean lock) {
        LevelChunkSection levelChunkSection = (LevelChunkSection) section;
        levelChunkSection.setBlockState(x, y, z, (BlockState) blockState, lock);
    }

    @Override
    public Object method$LevelChunkSection$getBlockState(Object section, int x, int y, int z) {
        LevelChunkSection levelChunkSection = (LevelChunkSection) section;
        return levelChunkSection.getBlockState(x, y, z);
    }

    @Override
    public Object field$CraftChunk$worldServer(Chunk chunk) {
        CraftChunk craftChunk = (CraftChunk) chunk;
        return craftChunk.getCraftWorld().getHandle();
    }

    @Override
    public Object method$ServerLevel$getChunkSource(Object serverLevel) {
        ServerLevel world = (ServerLevel) serverLevel;
        return world.getChunkSource();
    }

    @Override
    public Object method$ServerChunkCache$getChunkAtIfLoadedMainThread(Object serverChunkCache, int x, int z) {
        ServerChunkCache chunkCache = (ServerChunkCache) serverChunkCache;
        return chunkCache.getChunkAtIfCachedImmediately(x, z);
    }

    @Override
    public Object field$LevelChunkSection$states(Object section) {
        LevelChunkSection levelChunkSection = (LevelChunkSection) section;
        return levelChunkSection.states;
    }

    @Override
    public Object[] method$ChunkAccess$getSections(Object chunk) {
        LevelChunk levelChunk = (LevelChunk) chunk;
        return levelChunk.getSections();
    }

    @Override
    public Object field$ChunkAccess$blockEntities(Object chunkAccess) {
        ChunkAccess access = (ChunkAccess) chunkAccess;
        return access.blockEntities;
    }

    @Override
    public Object field$CraftWorld$ServerLevel(World world) {
        CraftWorld craftWorld = (CraftWorld) world;
        return craftWorld.getHandle();
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
        BlockGetter blockGetterImpl = (BlockGetter) blockGetter;
        return blockGetterImpl.getBlockState((BlockPos) blockPos);
    }

    @Override
    public Object method$CraftPlayer$getHandle(Player player) {
        CraftPlayer playerImpl = (CraftPlayer) player;
        return playerImpl.getHandle();
    }

    @Override
    public Object constructor$AABB(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new AABB(x1, y1, z1, x2, y2, z2);
    }

    @Override
    public void method$Entity$setBoundingBox(Object entity, Object aabb) {
        Entity e = (Entity) entity;
        e.setBoundingBox((AABB) aabb);
    }

    @Override
    public void method$LevelWriter$addFreshEntity(Object level, Object entity) {
        LevelWriter levelWriter = (LevelWriter) level;
        levelWriter.addFreshEntity((Entity) entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public Object method$CraftEntity$getHandle(Object entity) {
        CraftEntity craftEntity = (CraftEntity) entity;
        return craftEntity.getHandle();
    }

    @Override
    public Object constructor$ClientboundSetPassengersPacket(int entityId, int... passengers) {
        FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
        byteBuf.writeVarInt(entityId);
        byteBuf.writeVarIntArray(passengers);
        return ClientboundSetPassengersPacket.STREAM_CODEC.decode(byteBuf);
    }

    @Override
    public boolean isPreventingStatusUpdates(World world, int x, int z) {
        ServerLevel serverLevel = ((CraftWorld) world).getHandle();
        ChunkEntitySlices slices = serverLevel.moonrise$getEntityLookup().getChunk(x, z);

        return slices != null && slices.isPreventingStatusUpdates();
    }

    @Override
    public Object field$ClientboundLevelChunkWithLightPacket$chunkData(Object packet) {
        ClientboundLevelChunkWithLightPacket levelChunkPacket = (ClientboundLevelChunkWithLightPacket) packet;
        return levelChunkPacket.getChunkData();
    }

    @Override
    public int method$Entity$getId(Object entity) {
        Entity entityImpl = (Entity) entity;
        return entityImpl.getId();
    }

    @Override
    public boolean method$LevelWriter$setBlock(Object level, Object blockPos, Object blockState, int flags) {
        LevelWriter levelWriter = (LevelWriter) level;
        return levelWriter.setBlock((BlockPos) blockPos, (BlockState) blockState, flags);
    }

    @Override
    public Object method$ServerChunkCache$getVisibleChunkIfPresent(Object chunkSource, long chunkKey) {
        ServerChunkCache serverChunkCache = (ServerChunkCache) chunkSource;
        return serverChunkCache.chunkMap.getVisibleChunkIfPresent(chunkKey);
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
        ServerChunkCache serverChunkCache = (ServerChunkCache) chunkCache;
        serverChunkCache.blockChanged((BlockPos) blockPos);
    }

    @Override
    public void sendPacket(Object player, Object packet) {
        ServerPlayer playerImpl = (ServerPlayer) player;
        playerImpl.connection.send((Packet<?>) packet);
    }

    @Override
    public List<Object> method$ChunkHolder$getPlayers(Object chunkHolder) {
        ChunkHolder chunkHolderImpl = (ChunkHolder) chunkHolder;
        return (List) chunkHolderImpl.moonrise$getPlayers(false);
    }

    @Override
    public Object constructor$ClientboundBundlePacket(List<Object> packets) {
        Iterable<Packet<? super ClientGamePacketListener>> iterable = (Iterable) packets;
        return new ClientboundBundlePacket(iterable);
    }

    @Override
    public Object field$Player$connection$connection(Object player) {
        ServerPlayer playerImpl = (ServerPlayer) player;
        return playerImpl.connection.connection;
    }

    @Override
    public Object field$Player$connection$connection$channel(Object player) {
        ServerPlayer playerImpl = (ServerPlayer) player;
        return playerImpl.connection.connection.channel;
    }

    @Override
    public void method$BlockStateBase$onPlace(Object blockState, Object world, Object blockPos, Object oldBlockState, boolean movedByPiston) {
        BlockBehaviour.BlockStateBase blockStateBase = (BlockBehaviour.BlockStateBase) blockState;
        blockStateBase.onPlace((Level) world, (BlockPos) blockPos, (BlockState) oldBlockState, movedByPiston);
    }

    @Override
    public void method$Level$levelEvent(Object level, int eventId, Object blockPos, int stateId) {
        Level levelImpl = (Level) level;
        levelImpl.levelEvent(eventId, (BlockPos) blockPos, stateId);
    }

    @Override
    public Iterable<Object> method$ClientboundBundlePacket$subPackets(Object packet){
        ClientboundBundlePacket packetImpl = (ClientboundBundlePacket) packet;
        return (Iterable) packetImpl.subPackets();
    }

    @Override
    public Object field$ClientboundSoundPacket$soundEvent(Object packet) {
        ClientboundSoundPacket packetImpl = (ClientboundSoundPacket) packet;
        return packetImpl.getSound().value();
    }

    @Override
    public Object fastConstructor$ClientboundSoundPacket(Object newSoundEvent, Object soundPacket) {
        SoundEvent event = (SoundEvent) newSoundEvent;
        ClientboundSoundPacket soundPacketImpl = (ClientboundSoundPacket) soundPacket;
        return new ClientboundSoundPacket(Holder.direct(event),
                soundPacketImpl.getSource(),
                soundPacketImpl.getX(), soundPacketImpl.getY(), soundPacketImpl.getZ(),
                soundPacketImpl.getVolume(), soundPacketImpl.getPitch(), soundPacketImpl.getSeed());
    }

    @Override
    public Object method$ResourceLocation$fromNamespaceAndPath(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    @Override
    public Object field$SoundEvent$location(Object soundEvent) {
        SoundEvent event = (SoundEvent) soundEvent;
        return event.location();
    }

    @Override
    public int field$ServerboundInteractPacket$entityId(Object packet) {
        ServerboundInteractPacket packetImpl = (ServerboundInteractPacket) packet;
        return packetImpl.getEntityId();
    }

    @Override
    public IntList field$ClientboundRemoveEntitiesPacket$entityIds(Object packet) {
        ClientboundRemoveEntitiesPacket packetImpl = (ClientboundRemoveEntitiesPacket) packet;
        return packetImpl.getEntityIds();
    }

    @Override
    public Object field$ClientboundAddEntityPacket$type(Object packet) {
        ClientboundAddEntityPacket packetImpl = (ClientboundAddEntityPacket) packet;
        return packetImpl.getType();
    }

    @Override
    public int field$ClientboundAddEntityPacket$entityId(Object packet) {
        ClientboundAddEntityPacket packetImpl = (ClientboundAddEntityPacket) packet;
        return packetImpl.getId();
    }

    @Override
    public int field$ClientboundAddEntityPacket$data(Object packet) {
        ClientboundAddEntityPacket packetImpl = (ClientboundAddEntityPacket) packet;
        return packetImpl.getData();
    }

    @Override
    public Object field$ServerboundSwingPacket$hand(Object packet) {
        ServerboundSwingPacket packetImpl = (ServerboundSwingPacket) packet;
        return packetImpl.getHand();
    }

    @Override
    public Object field$ClientboundLevelParticlesPacket$particle(Object packet) {
        ClientboundLevelParticlesPacket packetImpl = (ClientboundLevelParticlesPacket) packet;
        return packetImpl.getParticle();
    }

    @Override
    public Object field$BlockParticleOption$blockState(Object object) {
        BlockParticleOption option = (BlockParticleOption) object;
        return option.getState();
    }

    @Override
    public Object field$ServerboundPlayerActionPacket$pos(Object packet) {
        ServerboundPlayerActionPacket packetImpl = (ServerboundPlayerActionPacket) packet;
        return packetImpl.getPos();
    }

    @Override
    public Object field$ServerboundPlayerActionPacket$action(Object packet) {
        ServerboundPlayerActionPacket packetImpl = (ServerboundPlayerActionPacket) packet;
        return packetImpl.getAction();
    }

    @Override
    public Object method$CraftItemStack$asNMSCopy(ItemStack itemStack) {
        return CraftItemStack.asNMSCopy(itemStack);
    }

    @Override
    public Object constructor$RegistryFriendlyByteBuf(Object buf, Object access) {
        return new RegistryFriendlyByteBuf((ByteBuf) buf, (RegistryAccess) access);
    }

    @Override
    public List<Object> field$ClientboundSetEntityDataPacket$packedItems(Object packet) {
        ClientboundSetEntityDataPacket packetImpl = (ClientboundSetEntityDataPacket) packet;
        return (List) packetImpl.packedItems();
    }

    @Override
    public int field$SynchedEntityData$DataValue$id(Object data) {
        SynchedEntityData.DataValue synchedEntityData = (SynchedEntityData.DataValue) data;
        return synchedEntityData.id();
    }

    @Override
    public Object field$SynchedEntityData$DataValue$value(Object data) {
        SynchedEntityData.DataValue synchedEntityData = (SynchedEntityData.DataValue) data;
        return synchedEntityData.value();
    }

    @Override
    public Object field$SynchedEntityData$DataValue$serializer(Object data) {
        SynchedEntityData.DataValue synchedEntityData = (SynchedEntityData.DataValue) data;
        return synchedEntityData.serializer();
    }

    @Override
    public Object constructor$SynchedEntityData$DataValue(int id, Object serializer, Object data) {
        return new SynchedEntityData.DataValue<>(id, (EntityDataSerializer) serializer, data);
    }

    @Override
    public int field$ClientboundSetEntityDataPacket$id(Object packet) {
        ClientboundSetEntityDataPacket packetImpl = (ClientboundSetEntityDataPacket) packet;
        return packetImpl.id();
    }

    @Override
    public Object method$Component$Serializer$fromJson(JsonElement element) {
        return Component.Serializer.fromJson(element, REGISTRY_ACCESS);
    }

    @Override
    public Object method$Component$Serializer$fromJson(String json) {
        return Component.Serializer.fromJson(json, REGISTRY_ACCESS);
    }

    @Override
    public String method$Component$Serializer$toJson(Object component) {
        return Component.Serializer.toJson((Component) component, REGISTRY_ACCESS);
    }

    @Override
    public List<NamespacedKey> getAllVanillaItems() {
        List<NamespacedKey> list = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation location = BuiltInRegistries.ITEM.getKey(item);
            list.add(new NamespacedKey(location.getNamespace(), location.getPath()));
        }
        return list;
    }

    @Override
    public org.bukkit.entity.Entity method$Entity$getBukkitEntity(Object entity) {
        Entity entityImpl = (Entity) entity;
        return entityImpl.getBukkitEntity();
    }

    @Override
    public int method$ClientboundEntityPositionSyncPacket$id(Object packet) {
        return ((ClientboundEntityPositionSyncPacket) packet).id();
    }

    @Override
    public void method$ClientboundSetEntityDataPacket$pack(List<?> dataValues, Object friendlyByteBuf) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf((ByteBuf) friendlyByteBuf, REGISTRY_ACCESS);
        try (ItemObfuscationSession ignored = ItemObfuscationSession.start(GlobalConfiguration.get().anticheat.obfuscation.items.binding.level)) {
            for(SynchedEntityData.DataValue<?> dataValue : ((List<SynchedEntityData.DataValue<?>>)dataValues)) {
                dataValue.write(buf);
            }
        }
        buf.writeByte(255);
    }

    @Override
    public List<Object> method$ClientboundSetEntityDataPacket$unpack(Object friendlyByteBuf) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf((ByteBuf) friendlyByteBuf, REGISTRY_ACCESS);
        List<Object> list = new ArrayList();
        int i;
        while((i = buf.readUnsignedByte()) != 255) {
            list.add(SynchedEntityData.DataValue.read(buf, i));
        }
        return list;
    }

    @Override
    public Map<String, Map<Class<?>, Integer>> method$getGamePacketIdsByClazz() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Map<String, Integer>> method$getGamePacketIdsByName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<NamespacedKey> getAllVanillaSounds() {
        List<NamespacedKey> list = new ArrayList<>();
        for (SoundEvent event : BuiltInRegistries.SOUND_EVENT) {
            list.add(new NamespacedKey(event.location().getNamespace(), event.location().getPath()));
        }
        return list;
    }

    @Override
    public Object constructor$ClientboundLevelChunkPacketData(Object buffer, int x, int z) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf((ByteBuf) buffer, REGISTRY_ACCESS);
        return new ClientboundLevelChunkPacketData(buf, x, z);
    }

    @Override
    public Object constructor$ClientboundLightUpdatePacketData(Object buffer, int x, int z) {
        FriendlyByteBuf buf = new FriendlyByteBuf((ByteBuf) buffer);
        return new ClientboundLightUpdatePacketData(buf, x, z);
    }

    @Override
    public void method$ClientboundLevelChunkPacketData$write(Object chunkData, Object buffer) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf((ByteBuf) buffer, REGISTRY_ACCESS);
        ((ClientboundLevelChunkPacketData)chunkData).write(buf);
    }

    @Override
    public void method$ClientboundLightUpdatePacketData$write(Object lightData, Object buffer) {
        FriendlyByteBuf buf = new FriendlyByteBuf((ByteBuf) buffer);
        ((ClientboundLightUpdatePacketData)lightData).write(buf);
    }

    @Override
    public Object method$ParticleTypes$STREAM_CODEC$decode(Object buffer) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf((ByteBuf) buffer, REGISTRY_ACCESS);
        return ParticleTypes.STREAM_CODEC.decode(buf);
    }

    @Override
    public void method$ParticleTypes$STREAM_CODEC$encode(Object buffer, Object particle) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf((ByteBuf) buffer, REGISTRY_ACCESS);
        ParticleTypes.STREAM_CODEC.encode(buf, ((ParticleOptions)particle));
    }

    @Override
    public Object constructor$BlockParticleOption(Object particleType, Object blockState) {
        return new BlockParticleOption((ParticleType<BlockParticleOption>) particleType, (BlockState) blockState);
    }

    @Override
    public Object method$BlockParticleOption$getType(Object particle) {
        return ((BlockParticleOption)particle).getType();
    }
}
