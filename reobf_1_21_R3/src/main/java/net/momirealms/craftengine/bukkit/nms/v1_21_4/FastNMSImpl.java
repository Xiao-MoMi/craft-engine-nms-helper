package net.momirealms.craftengine.bukkit.nms.v1_21_4;

import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.ChunkEntitySlices;
import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.EntityLookup;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.util.ItemObfuscationSession;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.advancements.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
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
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.lighting.LightEngine;
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
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.world.chunk.InjectedHolder;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftParticle;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.scheduler.CraftTask;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class FastNMSImpl extends FastNMS {
    private static final RegistryAccess REGISTRY_ACCESS = MinecraftServer.getServer().registryAccess();

    @Override
    public InjectedHolder.Palette createInjectedPalettedContainerHolder(Object palettedContainer) throws InstantiationException {
        InjectedPalettedContainer injectedObject = (InjectedPalettedContainer) Reflections.UNSAFE.allocateInstance(InjectedPalettedContainer.class);
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
    public Object method$LevelChunkSection$setBlockState(Object section, int x, int y, int z, Object blockState, boolean lock) {
        LevelChunkSection levelChunkSection = (LevelChunkSection) section;
        return levelChunkSection.setBlockState(x, y, z, (BlockState) blockState, lock);
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
    public boolean method$LevelWriter$addFreshEntity(Object level, Object entity) {
        LevelWriter levelWriter = (LevelWriter) level;
        return levelWriter.addFreshEntity((Entity) entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
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
        throw new UnsupportedVersionException();
    }

    @Override
    public Map<String, Map<String, Integer>> method$getGamePacketIdsByName() {
        throw new UnsupportedVersionException();
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
    public Optional<Object> method$IdMap$byId(Object registry, int id) {
        Object object = ((IdMap)registry).byId(id);
        if (object == null) {
            return Optional.empty();
        }
        return Optional.of(object);
    }

    @Override
    public Optional<Integer> method$IdMap$getId(Object registry, Object value) {
        int id = ((IdMap)registry).getId(value);
        return id == -1 ? Optional.empty() : Optional.of(id);
    }

    @Override
    public String[] method$SoundEvent$location(Object soundEvent) {
        ResourceLocation location = ((SoundEvent)soundEvent).location();
        return new String[]{location.getNamespace(), location.getPath()};
    }

    @Override
    public Optional<Float> method$SoundEvent$fixedRange(Object soundEvent) {
        return ((SoundEvent)soundEvent).fixedRange();
    }

    @Override
    public Object constructor$SoundEvent(Object location, Object fixedRange) {
        return new SoundEvent((ResourceLocation) location, (Optional) fixedRange);
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
        return manager.getRecipeFor((RecipeType) recipeType, (RecipeInput) recipeInput, (Level) level, (ResourceKey) resourceKeyOrLocation);
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
                e.showHat(),
                e.listOrder(),
                e.chatSession()
        );
    }

    @Override
    public Object field$ClientboundSetCursorItemPacket$item(Object packet) {
        ClientboundSetCursorItemPacket itemPacket = (ClientboundSetCursorItemPacket) packet;
        return itemPacket.contents();
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
        ClientboundSetPlayerInventoryPacket inventoryPacket = (ClientboundSetPlayerInventoryPacket) packet;
        return inventoryPacket.contents();
    }

    @Override
    public Object field$CraftItemStack$handle(ItemStack itemStack) {
        return CraftItemStack.unwrap(itemStack);
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
            serverLevel.setBlock(blockPos, Blocks.COBWEB.defaultBlockState(), 4);
            packet.timestamp = System.currentTimeMillis();
            serverPlayer.connection.handleUseItemOn(packet);
        } finally {
            serverLevel.setBlock(blockPos, previous, 4);
            serverPlayer.connection.send(new ClientboundBlockUpdatePacket(serverLevel, blockPos));
        }
    }

    @Override
    public void registerAdvancement(String[] key, Object jsonAdvancement) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(key[0], key[1]);
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
        net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) itemStack;
        nmsStack.applyComponents((DataComponentPatch) component);
    }

    @Override
    public Object method$ItemStack$transmuteCopy(Object itemStack1, Object itemStack2) {
        net.minecraft.world.item.ItemStack nmsStack1 = (net.minecraft.world.item.ItemStack) itemStack1;
        net.minecraft.world.item.ItemStack nmsStack2 = (net.minecraft.world.item.ItemStack) itemStack2;
        return nmsStack1.transmuteCopy(nmsStack2.getItem(), 1);
    }

    @Override
    public Object method$ItemStack$getComponentsPatch(Object itemStack) {
        net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) itemStack;
        return nmsStack.getComponentsPatch();
    }

    @Override
    public Object getComponentType(String namespace, String id) {
        return BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(ResourceLocation.fromNamespaceAndPath(namespace, id));
    }

    @Override
    public Object getComponent(Object itemStack, Object type) {
        net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) itemStack;
        return nmsStack.get((DataComponentType<?>) type);
    }

    @Override
    public boolean hasComponent(Object itemStack, Object type) {
        net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) itemStack;
        return nmsStack.has((DataComponentType<?>) type);
    }

    @Override
    public Object removeComponent(Object itemStack, Object type) {
        net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) itemStack;
        return nmsStack.remove((DataComponentType<?>) type);
    }

    @Override
    public void resetComponent(Object itemStack, Object componentType) {
        net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) itemStack;
        DataComponentType type = (DataComponentType) componentType;
        Object c = nmsStack.getItem().components().get(type);
        nmsStack.set(type, c);
    }

    @Override
    public void setComponent(Object itemStack, Object componentType, Object component) {
        net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) itemStack;
        DataComponentType type = (DataComponentType) componentType;
        nmsStack.set(type, component);
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
    public Runnable getBukkitTaskRunnable(BukkitTask bukkitTask) {
        CraftTask craftTask = (CraftTask) bukkitTask;
        return craftTask.rTask;
    }

    @Override
    public Object constructor$ClientboundLevelChunkWithLightPacket(net.momirealms.craftengine.core.util.FriendlyByteBuf buf) {
        throw new UnsupportedVersionException();
    }

    @Override
    public void method$ClientboundLevelChunkWithLightPacket$write(Object packet, net.momirealms.craftengine.core.util.FriendlyByteBuf buf) {
        throw new UnsupportedVersionException();
    }

    @Override
    public byte[] field$ClientboundLevelChunkPacketData$buffer(Object chunkData) {
        return ((ClientboundLevelChunkPacketData) chunkData).getReadBuffer().array();
    }

    @Override
    public boolean method$GrassBlock$isValidBonemealTarget(Object level, Object pos, Object state) {
        return ((LevelReader) level).getBlockState(((BlockPos) pos).above()).isAir();
    }

    @Override
    public void method$GrassBlock$performBoneMeal(Object level, Object random, Object blockPos, Object state, Object thisBlock) {
        ServerLevel level0 = (ServerLevel) level;
        RandomSource random0 = (RandomSource) random;
        BlockPos blockPos0 = ((BlockPos) blockPos).above();
        BlockState blockState0 = Blocks.SHORT_GRASS.defaultBlockState();
        Optional<Holder.Reference<PlacedFeature>> optional = level0.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE).get(VegetationPlacements.GRASS_BONEMEAL);

        out : for(int i = 0; i < 128; ++i) {
            BlockPos blockPos1 = blockPos0;

            for(int i1 = 0; i1 < i / 16; ++i1) {
                blockPos1 = blockPos1.offset(
                        random0.nextInt(3) - 1,
                        (((RandomSource) random).nextInt(3) - 1) * random0.nextInt(3) / 2,
                        random0.nextInt(3) - 1
                );
                Object downBlockPos1 = FastNMS.INSTANCE.method$BlockPos$relative(blockPos1, Reflections.instance$Direction$DOWN);
                if (!level0.getBlockState(blockPos1.below()).is(((net.minecraft.world.level.block.Block) thisBlock))
                        || level0.getBlockState(blockPos1).isCollisionShapeFullBlock(level0, blockPos1)) {
                    continue out;
                }

                BlockState blockState1 = level0.getBlockState(blockPos1);
                if (blockState1.is(blockState0.getBlock()) && random0.nextInt(10) == 0) {
                    BonemealableBlock bonemealableBlock = (BonemealableBlock) blockState0.getBlock();
                    if (bonemealableBlock.isValidBonemealTarget(level0, blockPos1, blockState1)) {
                        bonemealableBlock.performBonemeal(level0, random0, blockPos1, blockState1);
                    }
                }


                if (blockState1.isAir()) {
                    Holder<PlacedFeature> holder;
                    if (((RandomSource) random).nextInt(8) == 0) {
                        List<ConfiguredFeature<?, ?>> flowerFeatures = level0.getBiome(blockPos1).value().getGenerationSettings().getFlowerFeatures();
                        if (flowerFeatures.isEmpty()) {
                            continue;
                        }
                        int randomInt = random0.nextInt(flowerFeatures.size());
                        holder = ((RandomPatchConfiguration)((ConfiguredFeature)flowerFeatures.get(randomInt)).config()).feature();
                    } else {
                        if (optional.isEmpty()) {
                            continue;
                        }
                        holder = optional.get();
                    }
                    holder.value().place(level0, level0.getChunkSource().getGenerator(), random0, blockPos1);
                }
            }
        }
    }

    @Override
    public void method$LevelChunk$markUnsaved(Object chunk) {
        LevelChunk levelChunk = (LevelChunk) chunk;
        levelChunk.markUnsaved();
    }

    @Override
    public boolean method$LevelChunk$isUnsaved(Object chunk) {
        LevelChunk levelChunk = (LevelChunk) chunk;
        return levelChunk.isUnsaved();
    }

    @Override
    public org.bukkit.entity.Entity getBukkitEntityById(World world, int entityId) {
        ServerLevel serverLevel = ((CraftWorld) world).getHandle();
        EntityLookup entityLookup = serverLevel.moonrise$getEntityLookup();
        Entity entity = entityLookup.get(entityId);
        return entity != null ? entity.getBukkitEntity() : null;
    }

    @Override
    public Object constructor$ClientboundSystemChatPacket(Object component, boolean overlay) {
        return new ClientboundSystemChatPacket((Component) component, overlay);
    }

    @Override
    public Object method$ServerChunkCache$getChunk(Object serverChunkCache, int x, int z, boolean load) {
        ServerChunkCache chunkCache = (ServerChunkCache) serverChunkCache;
        return chunkCache.getChunk(x, z, load);
    }

    @Override
    public Object constructor$LevelChunkSection(Object section) {
        LevelChunkSection levelChunkSection = (LevelChunkSection) section;
        return new LevelChunkSection(levelChunkSection.getStates(), (PalettedContainer<Holder<Biome>>) levelChunkSection.getBiomes());
    }

    @Override
    public Object field$LevelChunkSection$biomes(Object section) {
        LevelChunkSection levelChunkSection = (LevelChunkSection) section;
        return levelChunkSection.getBiomes();
    }

    @Override
    public Object field$Entity$trackedEntity(Object entity) {
        Entity nmsEntity = (Entity) entity;
        return nmsEntity.moonrise$getTrackedEntity();
    }

    @Override
    public Object filed$ChunkMap$TrackedEntity$serverEntity(Object trackedEntity) {
        return ((ChunkMap.TrackedEntity) trackedEntity).serverEntity;
    }

    @Override
    public void method$ServerEntity$sendChanges(Object serverEntity) {
        ((ServerEntity) serverEntity).sendChanges();
    }

    @Override
    public boolean method$AbstractArrow$isInGround(Object entity) {
        AbstractArrow abstractArrow = (AbstractArrow) entity;
        return abstractArrow.isInGround();
    }

    @Override
    public boolean field$Entity$wasTouchingWater(Object entity) {
        Entity entityImpl = (Entity) entity;
        return entityImpl.wasTouchingWater;
    }

    @Override
    public int field$ClientboundEntityPositionSyncPacket$id(Object packet) {
        ClientboundEntityPositionSyncPacket packetImpl = (ClientboundEntityPositionSyncPacket) packet;
        return packetImpl.id();
    }

    @Override
    public Object field$ClientboundEntityPositionSyncPacket$values(Object packet) {
        ClientboundEntityPositionSyncPacket packetImpl = (ClientboundEntityPositionSyncPacket) packet;
        return packetImpl.values();
    }

    @Override
    public boolean field$ClientboundEntityPositionSyncPacket$onGround(Object packet) {
        ClientboundEntityPositionSyncPacket packetImpl = (ClientboundEntityPositionSyncPacket) packet;
        return packetImpl.onGround();
    }

    @Override
    public Object constructor$ClientboundEntityPositionSyncPacket(int entityId, Object values, boolean onGround) {
        PositionMoveRotation positionMoveRotationImpl = (PositionMoveRotation) values;
        return new ClientboundEntityPositionSyncPacket(entityId, positionMoveRotationImpl, onGround);
    }

    @Override
    public Object field$PositionMoveRotation$position(Object values) {
        PositionMoveRotation positionMoveRotation = (PositionMoveRotation) values;
        return positionMoveRotation.position();
    }

    @Override
    public Object field$PositionMoveRotation$deltaMovement(Object values) {
        PositionMoveRotation positionMoveRotation = (PositionMoveRotation) values;
        return positionMoveRotation.deltaMovement();
    }

    @Override
    public float field$PositionMoveRotation$yRot(Object values) {
        PositionMoveRotation positionMoveRotation = (PositionMoveRotation) values;
        return positionMoveRotation.yRot();
    }

    @Override
    public float field$PositionMoveRotation$xRot(Object values) {
        PositionMoveRotation positionMoveRotation = (PositionMoveRotation) values;
        return positionMoveRotation.xRot();
    }

    @Override
    public Object constructor$PositionMoveRotation(Object position, Object deltaMovement, float yRot, float xRot) {
        Vec3 positionImpl = (Vec3) position;
        Vec3 deltaMovementImpl = (Vec3) deltaMovement;
        return new PositionMoveRotation(positionImpl, deltaMovementImpl, yRot, xRot);
    }

    @Override
    public UUID field$ClientboundAddEntityPacket$uuid(Object packet) {
        ClientboundAddEntityPacket packetImpl = (ClientboundAddEntityPacket) packet;
        return packetImpl.getUUID();
    }

    @Override
    public double field$ClientboundAddEntityPacket$x(Object packet) {
        ClientboundAddEntityPacket packetImpl = (ClientboundAddEntityPacket) packet;
        return packetImpl.getX();
    }

    @Override
    public double field$ClientboundAddEntityPacket$y(Object packet) {
        ClientboundAddEntityPacket packetImpl = (ClientboundAddEntityPacket) packet;
        return packetImpl.getY();
    }

    @Override
    public double field$ClientboundAddEntityPacket$z(Object packet) {
        ClientboundAddEntityPacket packetImpl = (ClientboundAddEntityPacket) packet;
        return packetImpl.getZ();
    }

    @Override
    public float field$ClientboundAddEntityPacket$yRot(Object packet) {
        ClientboundAddEntityPacket packetImpl = (ClientboundAddEntityPacket) packet;
        return packetImpl.getYRot();
    }

    @Override
    public float field$ClientboundAddEntityPacket$xRot(Object packet) {
        ClientboundAddEntityPacket packetImpl = (ClientboundAddEntityPacket) packet;
        return packetImpl.getXRot();
    }

    @Override
    public float field$ClientboundAddEntityPacket$yHeadRot(Object packet) {
        ClientboundAddEntityPacket packetImpl = (ClientboundAddEntityPacket) packet;
        return packetImpl.getYHeadRot();
    }

    @Override
    public double field$ClientboundAddEntityPacket$xa(Object packet) {
        ClientboundAddEntityPacket packetImpl = (ClientboundAddEntityPacket) packet;
        return packetImpl.getXa();
    }

    @Override
    public double field$ClientboundAddEntityPacket$ya(Object packet) {
        ClientboundAddEntityPacket packetImpl = (ClientboundAddEntityPacket) packet;
        return packetImpl.getYa();
    }

    @Override
    public double field$ClientboundAddEntityPacket$za(Object packet) {
        ClientboundAddEntityPacket packetImpl = (ClientboundAddEntityPacket) packet;
        return packetImpl.getZa();
    }

    @Override
    public int field$ClientboundAddEntityPacket$data(Object packet) {
        ClientboundAddEntityPacket packetImpl = (ClientboundAddEntityPacket) packet;
        return packetImpl.getData();
    }

    @Override
    public short field$ClientboundMoveEntityPacket$xa(Object packet) {
        ClientboundMoveEntityPacket packetImpl = (ClientboundMoveEntityPacket) packet;
        return packetImpl.getXa();
    }

    @Override
    public short field$ClientboundMoveEntityPacket$ya(Object packet) {
        ClientboundMoveEntityPacket packetImpl = (ClientboundMoveEntityPacket) packet;
        return packetImpl.getYa();
    }

    @Override
    public short field$ClientboundMoveEntityPacket$za(Object packet) {
        ClientboundMoveEntityPacket packetImpl = (ClientboundMoveEntityPacket) packet;
        return packetImpl.getZa();
    }

    @Override
    public byte field$ClientboundMoveEntityPacket$yRot(Object packet) {
        ClientboundMoveEntityPacket packetImpl = (ClientboundMoveEntityPacket) packet;
        return Mth.packDegrees(packetImpl.getyRot());
    }

    @Override
    public byte field$ClientboundMoveEntityPacket$xRot(Object packet) {
        ClientboundMoveEntityPacket packetImpl = (ClientboundMoveEntityPacket) packet;
        return Mth.packDegrees(packetImpl.getxRot());
    }

    @Override
    public boolean field$ClientboundMoveEntityPacket$onGround(Object packet) {
        ClientboundMoveEntityPacket packetImpl = (ClientboundMoveEntityPacket) packet;
        return packetImpl.isOnGround();
    }

    @Override
    public Object constructor$ClientboundMoveEntityPacket$PosRot(int entityId, short xa, short ya, short za, byte yRot, byte xRot, boolean onGround) {
        return new ClientboundMoveEntityPacket.PosRot(entityId, xa, ya, za, yRot, xRot, onGround);
    }

    @Override
    public Object constructor$Vec3(double x, double y, double z) {
        return new Vec3(x, y, z);
    }

    @Override
    public Object constructor$ClientboundTeleportEntityPacket(int entityId, double x, double y, double z, byte yRot, byte xRot, boolean onGround) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object method$Registry$key(Object registry) {
        return ((Registry) registry).key();
    }

    @Override
    public Map<?, ?> method$TagNetworkSerialization$serializeTagsToNetwork() {
        return TagNetworkSerialization.serializeTagsToNetwork(MinecraftServer.getServer().registries());
    }

    @Override
    public void method$TagNetworkSerialization$NetworkPayload$write(Object networkPayload, Object buffer) {
        var payload = (TagNetworkSerialization.NetworkPayload) networkPayload;
        FriendlyByteBuf buf = new FriendlyByteBuf((ByteBuf) buffer);
        payload.write(buf);
    }

    @Override
    public Object method$TagNetworkSerialization$NetworkPayload$read(Object buffer) {
        FriendlyByteBuf buf = new FriendlyByteBuf((ByteBuf) buffer);
        return TagNetworkSerialization.NetworkPayload.read(buf);
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
    public Object method$ItemStack$copyWithCount(Object stack, int count) {
        return ((net.minecraft.world.item.ItemStack) stack).copyWithCount(count);
    }

    @Override
    public Object method$ItemStack$copy(Object stack) {
        return ((net.minecraft.world.item.ItemStack) stack).copy();
    }

    @Override
    public float method$EnchantmentHelper$getTridentSpinAttackStrength(Object stack, Object entity) {
        return EnchantmentHelper.getTridentSpinAttackStrength((net.minecraft.world.item.ItemStack) stack, (LivingEntity) entity);
    }

    @Override
    public boolean method$Entity$isInWaterOrRain(Object entity) {
        return ((net.minecraft.world.entity.Entity) entity).isInWaterOrRain();
    }

    @Override
    public boolean method$ItemStack$nextDamageWillBreak(Object stack) {
        return ((net.minecraft.world.item.ItemStack) stack).nextDamageWillBreak();
    }

    @Override
    public void method$ItemStack$setDamageValue(Object stack, int damage) {
        ((net.minecraft.world.item.ItemStack) stack).setDamageValue(damage);
    }

    @Override
    public int method$ItemStack$getDamageValue(Object stack) {
        return ((net.minecraft.world.item.ItemStack) stack).getDamageValue();
    }

    @Override
    public Object method$EnchantmentHelper$pickHighestLevel(Object stack) {
        return EnchantmentHelper.pickHighestLevel((net.minecraft.world.item.ItemStack) stack, EnchantmentEffectComponents.TRIDENT_SOUND).orElse(SoundEvents.TRIDENT_THROW);
    }

    @Override
    public Object method$Projectile$ThrownTrident$spawnProjectileFromRotationDelayed(Object level, Object spawnedFrom, Object owner, float z, float velocity, float innaccuracy) {
        return Projectile.spawnProjectileFromRotationDelayed(
                ThrownTrident::new,
                (ServerLevel)level,
                (net.minecraft.world.item.ItemStack)spawnedFrom,
                (LivingEntity)owner,
                z,
                velocity,
                innaccuracy
        );
    }

    @Override
    public Object method$Projectile$Delayed$projectile(Object projectile) {
        return ((Projectile.Delayed<?>) projectile).projectile();
    }

    @Override
    public boolean method$Projectile$Delayed$attemptSpawn(Object projectile) {
        return ((Projectile.Delayed<?>) projectile).attemptSpawn();
    }

    @Override
    public Object field$Player$containerMenu(Object player) {
        return ((net.minecraft.world.entity.player.Player) player).containerMenu;
    }

    @Override
    public void method$AbstractContainerMenu$sendAllDataToRemote(Object menu) {
        ((AbstractContainerMenu) menu).sendAllDataToRemote();
    }

    @Override
    public void method$ItemStack$hurtWithoutBreaking(Object stack, int damage, Object player) {
        ((net.minecraft.world.item.ItemStack) stack).hurtWithoutBreaking(damage, (net.minecraft.world.entity.player.Player) player);
    }

    @Override
    public void method$ItemStack$consume(Object stack, int amount, Object player) {
        ((net.minecraft.world.item.ItemStack) stack).consume(amount, (net.minecraft.world.entity.player.Player) player);
    }

    @Override
    public Object field$AbstractArrow$pickupItemStack(Object entity) {
        return ((AbstractArrow) entity).pickupItemStack;
    }

    @Override
    public void field$AbstractArrow$pickupItemStack(Object entity, Object pickupItemStack) {
        ((AbstractArrow) entity).pickupItemStack = (net.minecraft.world.item.ItemStack) pickupItemStack;
    }

    @Override
    public boolean method$Player$hasInfiniteMaterials(Object player) {
        return ((net.minecraft.world.entity.player.Player) player).hasInfiniteMaterials();
    }

    @Override
    public Object field$AbstractArrow$pickup(Object entity) {
        return ((AbstractArrow) entity).pickup;
    }

    @Override
    public void field$AbstractArrow$pickup(Object entity, Object pickup) {
        ((AbstractArrow) entity).pickup = (AbstractArrow.Pickup) pickup;
    }

    @Override
    public void method$Level$playSound(Object level, @Nullable Object entity, Object sourceEntity, Object sound, Object source, float volume, float pitch) {
        ((Level) level).playSound((net.minecraft.world.entity.player.Player) entity, (Entity) sourceEntity, (SoundEvent) sound, (SoundSource) source, volume, pitch);
    }

    @Override
    public float method$Entity$getYRot(Object entity) {
        return ((net.minecraft.world.entity.Entity) entity).getYRot();
    }

    @Override
    public float method$Entity$getXRot(Object entity) {
        return ((net.minecraft.world.entity.Entity) entity).getXRot();
    }

    @Override
    public void method$CraftEventFactory$callPlayerRiptideEvent(Object player, Object tridentItemStack, float velocityX, float velocityY, float velocityZ) {
        CraftEventFactory.callPlayerRiptideEvent(
                (net.minecraft.world.entity.player.Player) player,
                (net.minecraft.world.item.ItemStack) tridentItemStack,
                velocityX, velocityY, velocityZ
        );
    }

    @Override
    public void method$Entity$push(Object entity, double x, double y, double z) {
        ((net.minecraft.world.entity.Entity) entity).push(x, y, z);
    }

    @Override
    public void method$Player$startAutoSpinAttack(Object player, int ticks, float damage, Object itemStack) {
        ((net.minecraft.world.entity.player.Player) player).startAutoSpinAttack(ticks, damage, (net.minecraft.world.item.ItemStack) itemStack);
    }

    @Override
    public boolean method$Entity$onGround(Object entity) {
        return ((net.minecraft.world.entity.Entity) entity).onGround();
    }

    @Override
    public void method$Entity$move(Object entity, Object type, Object movement) {
        ((net.minecraft.world.entity.Entity) entity).move((MoverType) type, (Vec3) movement);
    }

    @Override
    public boolean method$ItemStack$isEmpty(Object stack) {
        return ((net.minecraft.world.item.ItemStack) stack).isEmpty();
    }

    @Override
    public Object method$Holder$value(Object holder) {
        return ((Holder<?>) holder).value();
    }

    @Override
    public boolean field$Entity$hurtMarked(Object entity) {
        return ((Entity) entity).hurtMarked;
    }

    @Override
    public void field$Entity$hurtMarked(Object entity, boolean hurtMarked) {
        ((Entity) entity).hurtMarked = hurtMarked;
    }

    @Override
    public Object constructor$ThrownTrident(Object level, Object owner, Object stack) {
        return new ThrownTrident((Level) level, (LivingEntity) owner, (net.minecraft.world.item.ItemStack) stack);
    }

    @Override
    public void method$ThrownTrident$shootFromRotation(Object entity, Object shooter, float pitch, float yaw, float roll, float speed, float divergence) {
        ((ThrownTrident) entity).shootFromRotation((Entity) shooter, pitch, yaw, roll, speed, divergence);
    }

    @Override
    public void method$ItemStack$hurtAndBreak(Object stack, int amount, Object entity, Object slot) {
        ((net.minecraft.world.item.ItemStack) stack).hurtAndBreak(amount, (LivingEntity) entity, (EquipmentSlot) slot);
    }

    @Override
    public Object method$LivingEntity$getSlotForHand(Object hand) {
        return LivingEntity.getSlotForHand((InteractionHand) hand);
    }

    @Override
    public Object method$LivingEntity$getUsedItemHand(Object entity) {
        return ((LivingEntity) entity).getUsedItemHand();
    }

    @Override
    public Object method$Player$getInventory(Object player) {
        return ((net.minecraft.world.entity.player.Player) player).getInventory();
    }

    @Override
    public void method$Inventory$removeItem(Object inventory, Object stack) {
        ((Inventory) inventory).removeItem((net.minecraft.world.item.ItemStack) stack);
    }

    @Override
    public void method$ItemStack$hurtAndBreak(Object stack, int amount, Object entity, Consumer<?> breakCallback) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object method$Player$getAbilities(Object player) {
        return ((net.minecraft.world.entity.player.Player) player).getAbilities();
    }

    @Override
    public boolean field$Abilities$instabuild(Object abilities) {
        return ((Abilities) abilities).instabuild;
    }

    @Override
    public void method$LivingEntity$broadcastBreakEvent(Object entity, Object hand) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object field$ThrownTrident$tridentItem(Object entity) {
        throw new UnsupportedVersionException();
    }

    @Override
    public void field$ThrownTrident$tridentItem(Object entity, Object tridentItem) {
        throw new UnsupportedVersionException();
    }

    @Override
    public ItemStack ensureCraftItemStack(ItemStack itemStack) {
        if (itemStack instanceof CraftItemStack craftItemStack) {
            return craftItemStack;
        } else {
            return CraftItemStack.asCraftCopy(itemStack);
        }
    }

    @Override
    public Particle method$CraftParticle$toBukkit(Object nmsParticle) {
        return CraftParticle.minecraftToBukkit((ParticleType) nmsParticle);
    }

    @Override
    public boolean method$SignalGetter$hasNeighborSignal(Object level, Object blockPos) {
        SignalGetter levelObj = (SignalGetter) level;
        return levelObj.hasNeighborSignal((BlockPos) blockPos);
    }

    @Override
    public Object method$BlockState$getBlock(Object blockState) {
        BlockState block = (BlockState) blockState;
        return block.getBlock();
    }

    @Override
    public Object method$BlockState$getShape(Object blockState, Object level, Object blockPos, Object collisionContext) {
        BlockState state = (BlockState) blockState;
        return state.getShape((BlockGetter) level, (BlockPos) blockPos, (CollisionContext) collisionContext);
    }

    @Override
    public Object method$BlockState$getCollisionShape(Object blockState, Object level, Object blockPos, Object collisionContext) {
        BlockState state = (BlockState) blockState;
        return state.getCollisionShape((BlockGetter) level, (BlockPos) blockPos, (CollisionContext) collisionContext);
    }

    @Override
    public Object method$BlockState$getBlockSupportShape(Object blockState, Object level, Object blockPos) {
        BlockState state = (BlockState) blockState;
        return state.getBlockSupportShape((BlockGetter) level, (BlockPos) blockPos);
    }

    @Override
    public boolean method$LightEngine$hasDifferentLightProperties(Object oldState, Object newState, Object blockGetter, Object blockPos) {
        return LightEngine.hasDifferentLightProperties((BlockState) oldState, (BlockState) newState);
    }

    @Override
    public Object constructor$FriendlyByteBuf(ByteBuf buf) {
        return new RegistryFriendlyByteBuf(buf, REGISTRY_ACCESS);
    }

    @Override
    public ItemStack method$FriendlyByteBuf$readItem(Object buf) {
        RegistryFriendlyByteBuf byteBuf = (RegistryFriendlyByteBuf) buf;
        net.minecraft.world.item.ItemStack itemStack = net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.decode(byteBuf);
        return CraftItemStack.asCraftMirror(itemStack);
    }

    @Override
    public void method$FriendlyByteBuf$writeItem(Object buf, ItemStack itemStack) {
        RegistryFriendlyByteBuf byteBuf = (RegistryFriendlyByteBuf) buf;
        net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC.encode(byteBuf, CraftItemStack.unwrap(itemStack));
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.item.ItemStack> ITEM_UNTRUSTED_CODEC =
            net.minecraft.world.item.ItemStack.validatedStreamCodec(net.minecraft.world.item.ItemStack.OPTIONAL_STREAM_CODEC).apply(ByteBufCodecs::trackDepth);

    @Override
    public ItemStack method$FriendlyByteBuf$readUntrustedItem(Object buf) {
        RegistryFriendlyByteBuf byteBuf = (RegistryFriendlyByteBuf) buf;
        net.minecraft.world.item.ItemStack itemStack = ITEM_UNTRUSTED_CODEC.decode(byteBuf);
        return CraftItemStack.asCraftMirror(itemStack);
    }

    @Override
    public void method$FriendlyByteBuf$writeUntrustedItem(Object buf, ItemStack itemStack) {
        RegistryFriendlyByteBuf byteBuf = (RegistryFriendlyByteBuf) buf;
        ITEM_UNTRUSTED_CODEC.encode(byteBuf, CraftItemStack.unwrap(itemStack));
    }

    @Override
    public Codec method$DataComponentType$codec(Object componentType) {
        DataComponentType type = (DataComponentType) componentType;
        return type.codec();
    }

    @Override
    public Object field$ItemStack$getOrCreateTag(Object itemStack) {
        throw new UnsupportedVersionException();
    }

    @Override
    public Object constructor$ShortTag(short s) {
        return ShortTag.valueOf(s);
    }

    @Override
    public short method$ShortTag$value(Object shortTag) {
        ShortTag tag = (ShortTag) shortTag;
        return tag.getAsShort();
    }

    @Override
    public Object constructor$IntTag(int i) {
        return IntTag.valueOf(i);
    }

    @Override
    public int method$IntTag$value(Object intTag) {
        IntTag tag = (IntTag) intTag;
        return tag.getAsInt();
    }

    @Override
    public Object constructor$LongTag(long l) {
        return LongTag.valueOf(l);
    }

    @Override
    public long method$LongTag$value(Object longTag) {
        LongTag tag = (LongTag) longTag;
        return tag.getAsLong();
    }

    @Override
    public Object constructor$ByteTag(byte b) {
        return ByteTag.valueOf(b);
    }

    @Override
    public byte method$ByteTag$value(Object byteTag) {
        ByteTag tag = (ByteTag) byteTag;
        return tag.getAsByte();
    }

    @Override
    public Object constructor$FloatTag(float f) {
        return FloatTag.valueOf(f);
    }

    @Override
    public float method$FloatTag$value(Object floatTag) {
        FloatTag tag = (FloatTag) floatTag;
        return tag.getAsFloat();
    }

    @Override
    public Object constructor$DoubleTag(double d) {
        return DoubleTag.valueOf(d);
    }

    @Override
    public double method$DoubleTag$value(Object doubleTag) {
        DoubleTag tag = (DoubleTag) doubleTag;
        return tag.getAsDouble();
    }

    @Override
    public Object constructor$ByteArrayTag(byte[] bytes) {
        return new ByteArrayTag(bytes);
    }

    @Override
    public byte[] method$ByteArrayTag$value(Object byteArrayTag) {
        ByteArrayTag tag = (ByteArrayTag) byteArrayTag;
        return tag.getAsByteArray();
    }

    @Override
    public Object constructor$IntArrayTag(int[] ints) {
        return new IntArrayTag(ints);
    }

    @Override
    public int[] method$IntArrayTag$value(Object intArrayTag) {
        IntArrayTag tag = (IntArrayTag) intArrayTag;
        return tag.getAsIntArray();
    }

    @Override
    public Object constructor$LongArrayTag(long[] longs) {
        return new LongArrayTag(longs);
    }

    @Override
    public long[] method$LongArrayTag$value(Object longArrayTag) {
        LongArrayTag tag = (LongArrayTag) longArrayTag;
        return tag.getAsLongArray();
    }

    @Override
    public Object constructor$StringTag(String string) {
        return StringTag.valueOf(string);
    }

    @Override
    public String method$StringTag$value(Object stringTag) {
        StringTag tag = (StringTag) stringTag;
        return tag.getAsString();
    }

    @Override
    public Object constructor$ListTag() {
        return new ListTag();
    }

    @Override
    public Object method$ListTag$get(Object listTag, int index) {
        ListTag tag = (ListTag) listTag;
        return tag.get(index);
    }

    @Override
    public void method$ListTag$add(Object listTag, int index, Object value) {
        ListTag tag = (ListTag) listTag;
        tag.addTag(index, (Tag) value);
    }

    @Override
    public Object method$ListTag$remove(Object listTag, int index) {
        ListTag tag = (ListTag) listTag;
        return tag.remove(index);
    }

    @Override
    public Object method$CompoundTag$get(Object compoundTag, String key) {
        CompoundTag tag = (CompoundTag) compoundTag;
        return tag.get(key);
    }

    @Override
    public void method$CompoundTag$put(Object compoundTag, String key, Object value) {
        CompoundTag tag = (CompoundTag) compoundTag;
        tag.put(key, (Tag) value);
    }

    @Override
    public void method$CompoundTag$remove(Object compoundTag, String key) {
        CompoundTag tag = (CompoundTag) compoundTag;
        tag.remove(key);
    }

    @Override
    public Object constructor$CompoundTag() {
        return new CompoundTag();
    }

    @Override
    public byte[] method$NbtIo$toBytes(Object tag) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {
            NbtIo.writeUnnamedTag((Tag) tag, dataOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    @Override
    public Object method$NbtIo$fromBytes(byte[] bytes) throws IOException {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream)) {
            return readUnnamedTag(dataInputStream, NbtAccounter.unlimitedHeap());
        }
    }

    private static Tag readUnnamedTag(DataInput input, NbtAccounter tracker) throws IOException {
        byte type = input.readByte();
        if (type == 0) {
            return EndTag.INSTANCE;
        } else {
            StringTag.skipString(input);
            return readTagSafe(input, tracker, type);
        }
    }

    private static Tag readTagSafe(DataInput input, NbtAccounter tracker, byte typeId) {
        try {
            return TagTypes.getType(typeId).load(input, tracker);
        } catch (IOException ioexception) {
            CrashReport crashreport = CrashReport.forThrowable(ioexception, "Loading NBT data");
            CrashReportCategory crc = crashreport.addCategory("NBT Tag");
            crc.setDetail("Tag type", typeId);
            throw new ReportedNbtException(crashreport);
        }
    }
}
