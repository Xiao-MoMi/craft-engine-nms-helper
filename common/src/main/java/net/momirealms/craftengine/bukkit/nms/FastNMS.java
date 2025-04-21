package net.momirealms.craftengine.bukkit.nms;

import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.*;

@SuppressWarnings("unused")
public abstract class FastNMS {
    public static final FastNMS INSTANCE = instance();

    private static FastNMS instance() {
        String path = getImplPath();
        try {
            Class<?> clazz = Class.forName("net.momirealms.craftengine.bukkit.nms." + path + ".FastNMSImpl");
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (FastNMS) constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to initialize craftengine nms helper", e);
        }
    }

    private static @NotNull String getImplPath() {
        String bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
        String classSuffix;
        switch (bukkitVersion) {
            case "1.21.5" -> classSuffix = "v1_21_5";
            case "1.21.4" -> classSuffix = "v1_21_4";
            case "1.21.2", "1.21.3" -> classSuffix = "v1_21_2";
            case "1.21", "1.21.1" -> classSuffix = "v1_21";
            case "1.20.5", "1.20.6" -> classSuffix = "v1_20_5";
            case "1.20.3", "1.20.4" -> classSuffix = "v1_20_3";
            case "1.20.2" -> classSuffix = "v1_20_2";
            case "1.20", "1.20.1" -> classSuffix = "v1_20";
            default -> throw new UnsupportedVersionException();
        }
        return classSuffix;
    }

    public abstract CollisionEntity createCollisionInteraction(Object world, Object aabb,
                                                               double x, double y, double z, boolean canProjectileHit, boolean canCollide, boolean blocksBuilding);

    public abstract Object method$PalettedContainer$getAndSet(Object palettedContainer, int x, int y, int z, Object blockState);

    public abstract BlockData method$CraftBlockData$fromData(Object blockState);

    public abstract int method$IdMapper$getId(Object idMapper, Object t);

    public abstract Object method$IdMapper$byId(Object idMapper, int id);

    public abstract Object method$CraftBlockData$getState(BlockData blockData);

    public abstract int method$BlockStateBase$getLightEmission(Object blockState);

    public abstract boolean method$BlockStateBase$canOcclude(Object blockState);

    public abstract void method$LevelChunkSection$setBlockState(Object section, int x, int y, int z, Object blockState, boolean lock);

    public abstract Object method$LevelChunkSection$getBlockState(Object section, int x, int y, int z);

    public abstract Object field$CraftChunk$worldServer(Chunk chunk);

    public abstract Object method$ServerLevel$getChunkSource(Object serverLevel);

    public abstract Object method$ServerChunkCache$getChunkAtIfLoadedMainThread(Object serverChunkCache, int x, int z);

    public abstract Object field$LevelChunkSection$states(Object section);

    public abstract Object[] method$ChunkAccess$getSections(Object chunk);

    public abstract Object field$ChunkAccess$blockEntities(Object chunkAccess);

    public abstract Object field$CraftWorld$ServerLevel(World world);

    public abstract Block method$CraftBlock$at(Object world, Object blockPos);

    public abstract Object field$AbstractFurnaceBlockEntity$recipeType(Object furnaceBlockEntity);

    public abstract ItemStack method$CraftItemStack$asCraftMirror(Object itemStack);

    public abstract Object field$ResourceKey$location(Object resourceKey);

    public abstract Object field$RecipeHolder$id(Object recipeHolder);

    public abstract World method$Level$getCraftWorld(Object level);

    public abstract boolean method$Level$removeBlock(Object level, Object blockPos, boolean move);

    public abstract int field$Vec3i$x(Object vec3i);

    public abstract int field$Vec3i$y(Object vec3i);

    public abstract int field$Vec3i$z(Object vec3i);

    public abstract double field$Vec3$x(Object vec3);

    public abstract double field$Vec3$y(Object vec3);

    public abstract double field$Vec3$z(Object vec3);

    public abstract Object constructor$BlockPos(int x, int y, int z);

    public abstract Object method$BlockGetter$getBlockState(Object blockGetter, Object blockPos);

    public abstract Object method$CraftPlayer$getHandle(Player player);

    public abstract Object constructor$AABB(double x1, double y1, double z1, double x2, double y2, double z2);

    public abstract void method$LevelWriter$addFreshEntity(Object level, Object entity);

    public abstract Object method$CraftEntity$getHandle(Object entity);

    public abstract Object constructor$ClientboundSetPassengersPacket(int entityId, int... passengers);

    public abstract boolean isPreventingStatusUpdates(World world, int x, int z);

    public abstract Object field$ClientboundLevelChunkWithLightPacket$chunkData(Object packet);

    public abstract int method$Entity$getId(Object entity);

    public abstract boolean method$LevelWriter$setBlock(Object level, Object blockPos, Object blockState, int flags);

    public abstract Object method$ServerChunkCache$getVisibleChunkIfPresent(Object chunkSource, long chunkKey);

    public abstract Object constructor$ChunkPos(int x, int z);

    public abstract Object constructor$ClientboundLightUpdatePacket(Object chunkPos, Object lightEngine, BitSet skyChangedLightSectionFilter, BitSet blockChangedLightSectionFilter);

    public abstract void method$ServerChunkCache$blockChanged(Object chunkCache, Object blockPos);

    public abstract void sendPacket(Object player, Object packet);

    public abstract List<Object> method$ChunkHolder$getPlayers(Object chunkHolder);

    public abstract Object constructor$ClientboundBundlePacket(List<Object> packets);

    public abstract Object field$Player$connection$connection(Object player);

    public abstract Object field$Player$connection$connection$channel(Object player);

    public abstract void method$BlockStateBase$onPlace(Object blockState, Object world, Object blockPos, Object oldBlockState, boolean movedByPiston);

    public abstract void method$Level$levelEvent(Object level, int eventId, Object blockPos, int stateId);

    public abstract Iterable<Object> method$ClientboundBundlePacket$subPackets(Object packet);

    public abstract Object method$ResourceLocation$fromNamespaceAndPath(String namespace, String path);

    public abstract Object field$SoundEvent$location(Object soundEvent);

    public abstract int field$ServerboundInteractPacket$entityId(Object packet);

    public abstract Object field$ClientboundAddEntityPacket$type(Object packet);

    public abstract int field$ClientboundAddEntityPacket$entityId(Object packet);

    public abstract Object field$ServerboundSwingPacket$hand(Object packet);

    public abstract Object field$BlockParticleOption$blockState(Object object);

    public abstract Object field$ServerboundPlayerActionPacket$pos(Object packet);

    public abstract Object field$ServerboundPlayerActionPacket$action(Object packet);

    public abstract Object method$CraftItemStack$asNMSCopy(ItemStack itemStack);

    public abstract int field$SynchedEntityData$DataValue$id(Object data);

    public abstract Object field$SynchedEntityData$DataValue$value(Object data);

    public abstract Object field$SynchedEntityData$DataValue$serializer(Object data);

    public abstract Object constructor$SynchedEntityData$DataValue(int id, Object serializer, Object data);

    public abstract Object method$Component$Serializer$fromJson(JsonElement element);

    public abstract Object method$Component$Serializer$fromJson(String json);

    public abstract String method$Component$Serializer$toJson(Object component);

    public abstract List<NamespacedKey> getAllVanillaItems();

    public abstract org.bukkit.entity.Entity method$Entity$getBukkitEntity(Object entity);

    public abstract int method$ClientboundEntityPositionSyncPacket$id(Object packet);

    public abstract void method$ClientboundSetEntityDataPacket$pack(List<?> dataValues, Object friendlyByteBuf);

    public abstract List<Object> method$ClientboundSetEntityDataPacket$unpack(Object friendlyByteBuf);

    public abstract Map<String, Map<Class<?>, Integer>> method$getGamePacketIdsByClazz();

    public abstract Map<String, Map<String, Integer>> method$getGamePacketIdsByName();

    public abstract List<NamespacedKey> getAllVanillaSounds();

    public abstract Object method$ParticleTypes$STREAM_CODEC$decode(Object buffer);

    public abstract void method$ParticleTypes$STREAM_CODEC$encode(Object buffer, Object particle);

    public abstract Object constructor$BlockParticleOption(Object particleType, Object blockState);

    public abstract Object method$BlockParticleOption$getType(Object particle);

    public abstract Object method$ClientboundLevelParticlesPacket$readParticle(Object buffer, Object particleType);

    public abstract Object method$FriendlyByteBuf$readById(Object buffer, Object idMap);

    public abstract void method$FriendlyByteBuf$writeId(Object buffer, Object particle, Object idMap);

    public abstract void method$ParticleOptions$writeToNetwork(Object particle, Object buffer);

    public abstract Optional<Object> method$BuiltInRegistries$byId(Object registry, int id);

    public abstract Optional<Integer> method$BuiltInRegistries$getId(Object registry, Object value);

    public abstract String[] method$SoundEvent$location(Object soundEvent);

    public abstract Optional<Float> method$SoundEvent$fixedRange(Object soundEvent);

    public abstract Object constructor$SoundEvent(Object location, Object fixedRange);

    public abstract void method$SoundEvent$directEncode(ByteBuf buffer, Object soundEvent);

    public abstract List<Object> field$ClientboundPlayerInfoUpdatePacket$entries(Object packet);

    public abstract EnumSet<? extends Enum> field$ClientboundPlayerInfoUpdatePacket$actions(Object packet);

    public abstract Object constructor$ClientboundPlayerInfoUpdatePacket(EnumSet actions, List entries);

    public abstract Optional method$RecipeManager$getRecipeFor(Object recipeManager, Object recipeType, Object recipeInput, Object level, Object resourceKeyOrLocation);

    public abstract Object field$ClientboundPlayerInfoUpdatePacket$Entry$displayName(Object entry);

    public abstract Object constructor$ClientboundPlayerInfoUpdatePacket$Entry(Object entry, Object newDisplayName);

    public abstract Object field$ClientboundSetCursorItemPacket$item(Object packet);

    public abstract List<Object> field$ClientboundContainerSetContentPacket$items(Object packet);

    public abstract Object field$ClientboundContainerSetContentPacket$carriedItem(Object packet);

    public abstract Object field$ClientboundContainerSetSlotPacket$item(Object packet);

    public abstract Object field$ClientboundSetPlayerInventoryPacket$contents(Object packet);

    public abstract void resetComponent(Object itemStack, Object resourceLocation);

    public abstract void setComponent(Object itemStack, Object resourceLocation, Object component);

    public abstract void removeComponent(Object itemStack, Object resourceLocation);

    public abstract Object field$CraftItemStack$handle(ItemStack itemStack);

    public abstract Object field$ServerPlayer$gameMode(Object player);

    public abstract void setMayBuild(Object player, boolean can);

    public abstract boolean mayBuild(Object player);

    public abstract double getInteractionRange(Object player);

    public abstract int field$MinecraftServer$currentTick();

    public abstract float method$BlockStateBase$getDestroyProgress(Object blockState, Object player, Object level, Object blockPos);

    public abstract boolean method$ItemStack$isCorrectToolForDrops(Object itemStack, Object blockState);

    public abstract boolean method$Player$hasCorrectToolForDrops(Object player, Object state);

    public abstract Object constructor$ClientboundBlockDestructionPacket(int entityId, Object blockPos, int stage);

    public abstract Object constructor$ClientboundLevelEventPacket(int id, Object blockPos, int data, boolean global);

    public abstract Object constructor$BlockInWorld(Object level, Object blockPos, boolean loadChunk);

    public abstract boolean canBreakInAdventureMode(Object itemStack, Object blockInWorld);

    public abstract boolean canPlaceInAdventureMode(Object itemStack, Object blockInWorld);

    public abstract Object method$Direction$getOpposite(Object direction);

    public abstract Object method$BlockPos$relative(Object blockPos, Object direction);

    public abstract String field$ClientboundResourcePackPushPacket$url(Object packet);

    public abstract Object constructor$ClientboundResourcePackPushPacket(UUID uuid, String url, String sha1, boolean kick, Object component);

    public abstract Object constructor$ClientboundResourcePackPopPacket(UUID uuid);

    public abstract UUID field$ClientboundResourcePackPushPacket$uuid(Object packet);

    public abstract Object constructor$ServerboundResourcePackPacket$SUCCESSFULLY_LOADED(UUID uuid);

    public abstract Object toNMSEntityType(org.bukkit.entity.EntityType entityType);

    public abstract boolean method$BonemealableBlock$isValidBonemealTarget(Object block, Object level, Object blockPos, Object state);

    public abstract Object constructor$ClientboundSetEntityDataPacket(int entityId, List data);

    public abstract Object constructor$ClientboundAddEntityPacket(int id, UUID uuid,
                                                                  double x, double y, double z, float xRot, float yRot,
                                                                  Object type, int data, Object deltaMovement, double yHeadRot);

    public abstract Object method$SynchedEntityData$DataValue$create(Object entityDataAccessor, Object value);

    public abstract Object constructor$EntityDataAccessor(int id, Object serializer);

    public abstract void simulateInteraction(Object player, Object direction, double x, double y, double z, Object pos);

    public abstract void registerAdvancement(String[] key, Object jsonAdvancement);

    public abstract boolean checkEntityCollision(Object level, List<Object> aabbs, double x, double y, double z);
}
