package net.momirealms.craftengine.bukkit.nms;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.ints.IntList;
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
import java.util.BitSet;
import java.util.List;

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

    // 以后简化代码的时候可以移除
    @Deprecated
    public abstract CollisionEntity createCollisionEntity(Object world, Object aabb,
                                                          double x, double y, double z, boolean canProjectileHit);

    public abstract CollisionEntity createCollisionShulker(Object world, Object aabb,
                                                          double x, double y, double z, boolean canProjectileHit);

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

    public abstract void method$Entity$setBoundingBox(Object entity, Object aabb);

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

    public abstract Object field$ClientboundSoundPacket$soundEvent(Object packet);

    public abstract Object fastConstructor$ClientboundSoundPacket(Object newSoundEvent, Object soundPacket);

    public abstract Object method$ResourceLocation$fromNamespaceAndPath(String namespace, String path);

    public abstract Object field$SoundEvent$location(Object soundEvent);

    public abstract int field$ServerboundInteractPacket$entityId(Object packet);

    public abstract IntList field$ClientboundRemoveEntitiesPacket$entityIds(Object packet);

    public abstract Object field$ClientboundAddEntityPacket$type(Object packet);

    public abstract int field$ClientboundAddEntityPacket$entityId(Object packet);

    public abstract int field$ClientboundAddEntityPacket$data(Object packet);

    public abstract Object field$ServerboundSwingPacket$hand(Object packet);

    public abstract Object field$ClientboundLevelParticlesPacket$particle(Object packet);

    public abstract Object field$BlockParticleOption$blockState(Object object);

    public abstract Object field$ServerboundPlayerActionPacket$pos(Object packet);

    public abstract Object field$ServerboundPlayerActionPacket$action(Object packet);

    public abstract Object method$CraftItemStack$asNMSCopy(ItemStack itemStack);

    public abstract Object constructor$RegistryFriendlyByteBuf(Object buf, Object access);

    public abstract List<Object> field$ClientboundSetEntityDataPacket$packedItems(Object packet);

    public abstract int field$SynchedEntityData$DataValue$id(Object data);

    public abstract Object field$SynchedEntityData$DataValue$value(Object data);

    public abstract Object field$SynchedEntityData$DataValue$serializer(Object data);

    public abstract Object constructor$SynchedEntityData$DataValue(int id, Object serializer, Object data);

    public abstract int field$ClientboundSetEntityDataPacket$id(Object packet);

    public abstract Object method$Component$Serializer$fromJson(JsonElement element);

    public abstract Object method$Component$Serializer$fromJson(String json);

    public abstract String method$Component$Serializer$toJson(Object component);

    public abstract List<NamespacedKey> getAllVanillaItems();

    public abstract org.bukkit.entity.Entity method$Entity$getBukkitEntity(Object entity);

    public abstract int method$ClientboundEntityPositionSyncPacket$id(Object packet);

    public abstract void method$ClientboundSetEntityDataPacket$pack(List<?> dataValues, Object friendlyByteBuf);

    public abstract List<Object> method$ClientboundSetEntityDataPacket$unpack(Object friendlyByteBuf);
}
