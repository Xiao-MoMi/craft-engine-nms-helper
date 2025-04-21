package net.momirealms.craftengine.bukkit.nms.v1_20_5;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.papermc.paper.util.DataSanitizationUtil;
import io.papermc.paper.world.ChunkEntitySlices;
import net.minecraft.advancements.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.cookie.CookiePacketTypes;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.ping.PingPacketTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.nms.UnsupportedVersionException;
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
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class FastNMSImpl extends FastNMS {
    private static final RegistryAccess REGISTRY_ACCESS = MinecraftServer.getServer().registryAccess();

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
        return chunkCache.getChunkAtIfLoadedMainThread(x, z);
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
        ChunkEntitySlices slices = serverLevel.getEntityLookup().getChunk(x, z);
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
        return (List) chunkHolderImpl.getPlayers(false);
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
    public Object method$ResourceLocation$fromNamespaceAndPath(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    @Override
    public Object field$SoundEvent$location(Object soundEvent) {
        SoundEvent event = (SoundEvent) soundEvent;
        return event.getLocation();
    }

    @Override
    public int field$ServerboundInteractPacket$entityId(Object packet) {
        ServerboundInteractPacket packetImpl = (ServerboundInteractPacket) packet;
        return packetImpl.getEntityId();
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
    public Object field$ServerboundSwingPacket$hand(Object packet) {
        ServerboundSwingPacket packetImpl = (ServerboundSwingPacket) packet;
        return packetImpl.getHand();
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
        throw new UnsupportedVersionException();
    }

    @Override
    public void method$ClientboundSetEntityDataPacket$pack(List<?> dataValues, Object friendlyByteBuf) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf((ByteBuf) friendlyByteBuf, REGISTRY_ACCESS);
        try (DataSanitizationUtil.DataSanitizer ignored = DataSanitizationUtil.start(true)) {
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
        throw new UnsupportedVersionException();
    }

    @Override
    @SuppressWarnings("UnusedAssignment")
    public Map<String, Map<String, Integer>> method$getGamePacketIdsByName() {
        Map<String, Map<String, Integer>> gamePacketIdsByName = new HashMap<>();
        Map<String, Integer> serverBoundIds = new HashMap<>();
        Map<String, Integer> clientBoundIds = new HashMap<>();
        gamePacketIdsByName.put("clientbound", clientBoundIds);
        gamePacketIdsByName.put("serverbound", serverBoundIds);

        int clientIndex = 0;
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_BUNDLE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_ADD_ENTITY.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_ADD_EXPERIENCE_ORB.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_ANIMATE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_AWARD_STATS.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_BLOCK_CHANGED_ACK.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_BLOCK_DESTRUCTION.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_BLOCK_ENTITY_DATA.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_BLOCK_EVENT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_BLOCK_UPDATE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_BOSS_EVENT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_CHANGE_DIFFICULTY.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_CHUNK_BATCH_FINISHED.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_CHUNK_BATCH_START.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_CHUNKS_BIOMES.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_CLEAR_TITLES.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_COMMAND_SUGGESTIONS.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_COMMANDS.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_CONTAINER_CLOSE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_CONTAINER_SET_CONTENT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_CONTAINER_SET_DATA.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_CONTAINER_SET_SLOT.id().toString(), clientIndex++);
        clientBoundIds.put(CookiePacketTypes.CLIENTBOUND_COOKIE_REQUEST.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_COOLDOWN.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_CUSTOM_CHAT_COMPLETIONS.id().toString(), clientIndex++);
        clientBoundIds.put(CommonPacketTypes.CLIENTBOUND_CUSTOM_PAYLOAD.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_DAMAGE_EVENT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_DEBUG_SAMPLE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_DELETE_CHAT.id().toString(), clientIndex++);
        clientBoundIds.put(CommonPacketTypes.CLIENTBOUND_DISCONNECT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_DISGUISED_CHAT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_ENTITY_EVENT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_EXPLODE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_FORGET_LEVEL_CHUNK.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_GAME_EVENT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_HORSE_SCREEN_OPEN.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_HURT_ANIMATION.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_INITIALIZE_BORDER.id().toString(), clientIndex++);
        clientBoundIds.put(CommonPacketTypes.CLIENTBOUND_KEEP_ALIVE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_LEVEL_CHUNK_WITH_LIGHT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_LEVEL_EVENT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_LEVEL_PARTICLES.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_LIGHT_UPDATE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_LOGIN.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_MAP_ITEM_DATA.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_MERCHANT_OFFERS.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_MOVE_ENTITY_POS.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_MOVE_ENTITY_POS_ROT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_MOVE_ENTITY_ROT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_MOVE_VEHICLE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_OPEN_BOOK.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_OPEN_SCREEN.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_OPEN_SIGN_EDITOR.id().toString(), clientIndex++);
        clientBoundIds.put(CommonPacketTypes.CLIENTBOUND_PING.id().toString(), clientIndex++);
        clientBoundIds.put(PingPacketTypes.CLIENTBOUND_PONG_RESPONSE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_PLACE_GHOST_RECIPE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_PLAYER_ABILITIES.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_PLAYER_CHAT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_PLAYER_COMBAT_END.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_PLAYER_COMBAT_ENTER.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_PLAYER_COMBAT_KILL.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_PLAYER_INFO_REMOVE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_PLAYER_INFO_UPDATE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_PLAYER_LOOK_AT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_PLAYER_POSITION.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_RECIPE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_REMOVE_ENTITIES.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_REMOVE_MOB_EFFECT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_RESET_SCORE.id().toString(), clientIndex++);
        clientBoundIds.put(CommonPacketTypes.CLIENTBOUND_RESOURCE_PACK_POP.id().toString(), clientIndex++);
        clientBoundIds.put(CommonPacketTypes.CLIENTBOUND_RESOURCE_PACK_PUSH.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_RESPAWN.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_ROTATE_HEAD.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SECTION_BLOCKS_UPDATE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SELECT_ADVANCEMENTS_TAB.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SERVER_DATA.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_ACTION_BAR_TEXT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_BORDER_CENTER.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_BORDER_LERP_SIZE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_BORDER_SIZE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_BORDER_WARNING_DELAY.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_BORDER_WARNING_DISTANCE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_CAMERA.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_CARRIED_ITEM.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_CHUNK_CACHE_CENTER.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_CHUNK_CACHE_RADIUS.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_DEFAULT_SPAWN_POSITION.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_DISPLAY_OBJECTIVE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_ENTITY_DATA.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_ENTITY_LINK.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_ENTITY_MOTION.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_EQUIPMENT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_EXPERIENCE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_HEALTH.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_OBJECTIVE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_PASSENGERS.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_PLAYER_TEAM.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_SCORE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_SIMULATION_DISTANCE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_SUBTITLE_TEXT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_TIME.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_TITLE_TEXT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SET_TITLES_ANIMATION.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SOUND_ENTITY.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SOUND.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_START_CONFIGURATION.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_STOP_SOUND.id().toString(), clientIndex++);
        clientBoundIds.put(CommonPacketTypes.CLIENTBOUND_STORE_COOKIE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_SYSTEM_CHAT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_TAB_LIST.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_TAG_QUERY.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_TAKE_ITEM_ENTITY.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_TELEPORT_ENTITY.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_TICKING_STATE.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_TICKING_STEP.id().toString(), clientIndex++);
        clientBoundIds.put(CommonPacketTypes.CLIENTBOUND_TRANSFER.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_UPDATE_ADVANCEMENTS.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_UPDATE_ATTRIBUTES.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_UPDATE_MOB_EFFECT.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_UPDATE_RECIPES.id().toString(), clientIndex++);
        clientBoundIds.put(CommonPacketTypes.CLIENTBOUND_UPDATE_TAGS.id().toString(), clientIndex++);
        clientBoundIds.put(GamePacketTypes.CLIENTBOUND_PROJECTILE_POWER.id().toString(), clientIndex++);

        int serverIndex = 0;
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_ACCEPT_TELEPORTATION.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_BLOCK_ENTITY_TAG_QUERY.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_CHANGE_DIFFICULTY.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_CHAT_ACK.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_CHAT_COMMAND.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_CHAT_COMMAND_SIGNED.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_CHAT.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_CHAT_SESSION_UPDATE.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_CHUNK_BATCH_RECEIVED.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_CLIENT_COMMAND.id().toString(), serverIndex++);
        serverBoundIds.put(CommonPacketTypes.SERVERBOUND_CLIENT_INFORMATION.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_COMMAND_SUGGESTION.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_CONFIGURATION_ACKNOWLEDGED.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_CONTAINER_BUTTON_CLICK.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_CONTAINER_CLICK.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_CONTAINER_CLOSE.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_CONTAINER_SLOT_STATE_CHANGED.id().toString(), serverIndex++);
        serverBoundIds.put(CookiePacketTypes.SERVERBOUND_COOKIE_RESPONSE.id().toString(), serverIndex++);
        serverBoundIds.put(CommonPacketTypes.SERVERBOUND_CUSTOM_PAYLOAD.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_DEBUG_SAMPLE_SUBSCRIPTION.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_EDIT_BOOK.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_ENTITY_TAG_QUERY.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_INTERACT.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_JIGSAW_GENERATE.id().toString(), serverIndex++);
        serverBoundIds.put(CommonPacketTypes.SERVERBOUND_KEEP_ALIVE.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_LOCK_DIFFICULTY.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_MOVE_PLAYER_POS.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_MOVE_PLAYER_POS_ROT.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_MOVE_PLAYER_ROT.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_MOVE_PLAYER_STATUS_ONLY.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_MOVE_VEHICLE.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_PADDLE_BOAT.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_PICK_ITEM.id().toString(), serverIndex++);
        serverBoundIds.put(PingPacketTypes.SERVERBOUND_PING_REQUEST.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_PLACE_RECIPE.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_PLAYER_ABILITIES.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_PLAYER_ACTION.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_PLAYER_COMMAND.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_PLAYER_INPUT.id().toString(), serverIndex++);
        serverBoundIds.put(CommonPacketTypes.SERVERBOUND_PONG.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_RECIPE_BOOK_CHANGE_SETTINGS.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_RECIPE_BOOK_SEEN_RECIPE.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_RENAME_ITEM.id().toString(), serverIndex++);
        serverBoundIds.put(CommonPacketTypes.SERVERBOUND_RESOURCE_PACK.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_SEEN_ADVANCEMENTS.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_SELECT_TRADE.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_SET_BEACON.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_SET_CARRIED_ITEM.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_SET_COMMAND_BLOCK.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_SET_COMMAND_MINECART.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_SET_CREATIVE_MODE_SLOT.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_SET_JIGSAW_BLOCK.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_SET_STRUCTURE_BLOCK.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_SIGN_UPDATE.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_SWING.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_TELEPORT_TO_ENTITY.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_USE_ITEM_ON.id().toString(), serverIndex++);
        serverBoundIds.put(GamePacketTypes.SERVERBOUND_USE_ITEM.id().toString(), serverIndex++);

        return gamePacketIdsByName;
    }

    @Override
    public List<NamespacedKey> getAllVanillaSounds() {
        List<NamespacedKey> list = new ArrayList<>();
        for (SoundEvent event : BuiltInRegistries.SOUND_EVENT) {
            list.add(new NamespacedKey(event.getLocation().getNamespace(), event.getLocation().getPath()));
        }
        return list;
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

    @Override
    public Object method$ClientboundLevelParticlesPacket$readParticle(Object buffer, Object particleType) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object method$FriendlyByteBuf$readById(Object buffer, Object idMap) {
        throw new UnsupportedVersionException();
    }

    @Override
    public void method$FriendlyByteBuf$writeId(Object buffer, Object particle, Object idMap) {
        throw new UnsupportedVersionException();
    }

    @Override
    public void method$ParticleOptions$writeToNetwork(Object particle, Object buffer) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Optional<Object> method$BuiltInRegistries$byId(Object registry, int id) {
        Object object = ((IdMap)registry).byId(id);
        if (object == null) {
            return Optional.empty();
        }
        return Optional.of(object);
    }

    @Override
    public Optional<Integer> method$BuiltInRegistries$getId(Object registry, Object value) {
        int id = ((IdMap)registry).getId(value);
        return id == -1 ? Optional.empty() : Optional.of(id);
    }

    @Override
    public String[] method$SoundEvent$location(Object soundEvent) {
        ResourceLocation location = ((SoundEvent)soundEvent).getLocation();
        return new String[]{location.getNamespace(), location.getPath()};
    }

    @Override
    public Optional<Float> method$SoundEvent$fixedRange(Object soundEvent) {
        float range1 = ((SoundEvent)soundEvent).getRange(100.0F);
        float range2 = ((SoundEvent)soundEvent).getRange(0.0F);
        if (range1 == 16.0F * 100.0F && range2 == 16.0F) {
            return Optional.empty();
        }
        return Optional.of(range1);
    }

    @Override
    public Object constructor$SoundEvent(Object location, Object fixedRange) {
        ResourceLocation id = (ResourceLocation) location;
        Optional<Float> distanceToTravel = (Optional)fixedRange;
        return distanceToTravel.map((float_) -> SoundEvent.createFixedRangeEvent(id, float_)).orElseGet(() -> SoundEvent.createVariableRangeEvent(id));
    }

    @Override
    public void method$SoundEvent$directEncode(ByteBuf buffer, Object soundEvent) {
        SoundEvent event = (SoundEvent) soundEvent;
        SoundEvent.DIRECT_STREAM_CODEC.encode(buffer, event);
    }

    @Override
    public List<Object> field$ClientboundPlayerInfoUpdatePacket$entries(Object packet) {
        ClientboundPlayerInfoUpdatePacket updatePacket = (ClientboundPlayerInfoUpdatePacket) packet;
        return (List) updatePacket.entries();
    }

    @Override
    public EnumSet<? extends Enum> field$ClientboundPlayerInfoUpdatePacket$actions(Object packet) {
        ClientboundPlayerInfoUpdatePacket updatePacket = (ClientboundPlayerInfoUpdatePacket) packet;
        return updatePacket.actions();
    }

    @Override
    public Object constructor$ClientboundPlayerInfoUpdatePacket(EnumSet actions, List entries) {
        return new ClientboundPlayerInfoUpdatePacket((EnumSet<ClientboundPlayerInfoUpdatePacket.Action>) actions, (List<ClientboundPlayerInfoUpdatePacket.Entry>) entries);
    }

    @Override
    public Optional method$RecipeManager$getRecipeFor(Object recipeManager, Object recipeType, Object recipeInput, Object level, Object resourceKeyOrLocation) {
        RecipeManager manager = (RecipeManager) recipeManager;
        return manager.getRecipeFor((RecipeType) recipeType, (Container) recipeInput, (Level) level, (ResourceLocation) resourceKeyOrLocation);
    }

    @Override
    public Object field$ClientboundPlayerInfoUpdatePacket$Entry$displayName(Object entry) {
        ClientboundPlayerInfoUpdatePacket.Entry e = (ClientboundPlayerInfoUpdatePacket.Entry) entry;
        return e.displayName();
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
    public Object field$ClientboundSetCursorItemPacket$item(Object packet) {
        throw new UnsupportedVersionException();
    }

    @Override
    public List<Object> field$ClientboundContainerSetContentPacket$items(Object packet) {
        ClientboundContainerSetContentPacket itemPacket = (ClientboundContainerSetContentPacket) packet;
        return (List) itemPacket.getItems();
    }

    @Override
    public Object field$ClientboundContainerSetContentPacket$carriedItem(Object packet) {
        ClientboundContainerSetContentPacket itemPacket = (ClientboundContainerSetContentPacket) packet;
        return itemPacket.getCarriedItem();
    }

    @Override
    public Object field$ClientboundContainerSetSlotPacket$item(Object packet) {
        ClientboundContainerSetSlotPacket itemPacket = (ClientboundContainerSetSlotPacket) packet;
        return itemPacket.getItem();
    }

    @Override
    public Object field$ClientboundSetPlayerInventoryPacket$contents(Object packet) {
        throw new UnsupportedVersionException();
    }

    @Override
    public void resetComponent(Object itemStack, Object resourceLocation) {
        net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) itemStack;
        DataComponentType type = Objects.requireNonNull(BuiltInRegistries.DATA_COMPONENT_TYPE.get((ResourceLocation) resourceLocation));
        Object t = nmsStack.getItem().components().get(type);
        nmsStack.set(type, t);
    }

    @Override
    public void setComponent(Object itemStack, Object resourceLocation, Object component) {
        net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) itemStack;
        DataComponentType type = Objects.requireNonNull(BuiltInRegistries.DATA_COMPONENT_TYPE.get((ResourceLocation) resourceLocation));
        nmsStack.set(type, component);
    }

    @Override
    public void removeComponent(Object itemStack, Object resourceLocation) {
        net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) itemStack;
        DataComponentType type = Objects.requireNonNull(BuiltInRegistries.DATA_COMPONENT_TYPE.get((ResourceLocation) resourceLocation));
        nmsStack.remove(type);
    }

    @Override
    public Object field$CraftItemStack$handle(ItemStack itemStack) {
        CraftItemStack stack = (CraftItemStack) itemStack;
        return stack.handle;
    }

    @Override
    public Object field$ServerPlayer$gameMode(Object player) {
        ServerPlayer serverPlayer = (ServerPlayer) player;
        return serverPlayer.gameMode;
    }

    @Override
    public void setMayBuild(Object player, boolean can) {
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.getAbilities().mayBuild = can;
    }

    @Override
    public boolean mayBuild(Object player) {
        ServerPlayer serverPlayer = (ServerPlayer) player;
        return serverPlayer.getAbilities().mayBuild;
    }

    @Override
    public double getInteractionRange(Object player) {
        ServerPlayer serverPlayer = (ServerPlayer) player;
        return Optional.ofNullable(serverPlayer.getAttribute(Attributes.BLOCK_INTERACTION_RANGE)).map(AttributeInstance::getValue).orElse(4.5d);
    }

    @Override
    public int field$MinecraftServer$currentTick() {
        return MinecraftServer.currentTick;
    }

    @Override
    public float method$BlockStateBase$getDestroyProgress(Object blockState, Object player, Object level, Object blockPos) {
        BlockBehaviour.BlockStateBase state = (BlockBehaviour.BlockStateBase) blockState;
        return state.getDestroyProgress((ServerPlayer) player, (BlockGetter) level, (BlockPos) blockPos);
    }

    @Override
    public boolean method$ItemStack$isCorrectToolForDrops(Object itemStack, Object blockState) {
        net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) itemStack;
        return nmsStack.isCorrectToolForDrops((BlockState) blockState);
    }

    @Override
    public boolean method$Player$hasCorrectToolForDrops(Object player, Object state) {
        net.minecraft.world.entity.player.Player nmsPlayer = (net.minecraft.world.entity.player.Player) player;
        return nmsPlayer.hasCorrectToolForDrops((BlockState) state);
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
    public boolean canBreakInAdventureMode(Object itemStack, Object blockInWorld) {
        net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) itemStack;
        return nmsStack.canBreakBlockInAdventureMode((BlockInWorld) blockInWorld);
    }

    @Override
    public boolean canPlaceInAdventureMode(Object itemStack, Object blockInWorld) {
        net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) itemStack;
        return nmsStack.canPlaceOnBlockInAdventureMode((BlockInWorld) blockInWorld);
    }

    @Override
    public Object method$Direction$getOpposite(Object direction) {
        Direction d = (Direction) direction;
        return d.getOpposite();
    }

    @Override
    public Object method$BlockPos$relative(Object blockPos, Object direction) {
        BlockPos pos = (BlockPos) blockPos;
        return pos.relative((Direction) direction);
    }

    @Override
    public String field$ClientboundResourcePackPushPacket$url(Object packet) {
        ClientboundResourcePackPushPacket pack = (ClientboundResourcePackPushPacket) packet;
        return pack.url();
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
        ClientboundResourcePackPushPacket pack = (ClientboundResourcePackPushPacket) packet;
        return pack.id();
    }

    @Override
    public Object constructor$ServerboundResourcePackPacket$SUCCESSFULLY_LOADED(UUID uuid) {
        return new ServerboundResourcePackPacket(uuid, ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED);
    }

    @Override
    public Object toNMSEntityType(org.bukkit.entity.EntityType entityType) {
        return CraftEntityType.bukkitToMinecraft(entityType);
    }

    @Override
    public boolean method$BonemealableBlock$isValidBonemealTarget(Object block, Object level, Object blockPos, Object state) {
        BonemealableBlock bonemealableBlock = (BonemealableBlock) block;
        return bonemealableBlock.isValidBonemealTarget((LevelReader) level, (BlockPos) blockPos, (BlockState) state);
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
            serverLevel.setBlock(blockPos, Blocks.BARRIER.defaultBlockState(), 4);
            packet.timestamp = System.currentTimeMillis();
            serverPlayer.connection.handleUseItemOn(packet);
        } finally {
            serverLevel.setBlock(blockPos, previous, 4);
            serverPlayer.connection.send(new ClientboundBlockUpdatePacket(serverLevel, blockPos));
        }
    }

    @Override
    public void registerAdvancement(String[] key, Object jsonAdvancement) {
        ResourceLocation location = new ResourceLocation(key[0], key[1]);
        RegistryOps<JsonElement> ops = REGISTRY_ACCESS.createSerializationContext(JsonOps.INSTANCE);
        Advancement advancement = Advancement.CODEC.parse(ops, (JsonElement) jsonAdvancement).getOrThrow(JsonParseException::new);
        if (advancement != null) {
            MinecraftServer server = MinecraftServer.getServer();
            ServerAdvancementManager serverAdvancementManager = server.getAdvancements();
            AdvancementHolder holder = new AdvancementHolder(location, advancement);
            serverAdvancementManager.advancements.put(location, holder);
            AdvancementTree tree = serverAdvancementManager.tree();
            tree.addAll(List.of(holder));
            AdvancementNode node = tree.get(location);
            if (node != null) {
                AdvancementNode root = node.root();
                if (root.holder().value().display().isPresent()) {
                    TreeNodePosition.run(root);
                }
            }
        }
    }
}
