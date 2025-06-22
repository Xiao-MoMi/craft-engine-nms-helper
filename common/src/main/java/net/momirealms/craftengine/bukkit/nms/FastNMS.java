package net.momirealms.craftengine.bukkit.nms;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.momirealms.craftengine.core.util.ReflectionUtils;
import net.momirealms.craftengine.core.world.chunk.InjectedHolder;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

@SuppressWarnings({"unused", "rawtypes"})
public abstract class FastNMS {
    private static final Class<?> clazz$SharedConstants = requireNonNull(ReflectionUtils.getClazz("net.minecraft.SharedConstants"));
    private static final Field field$SharedConstants$VERSION_STRING = requireNonNull(ReflectionUtils.getDeclaredField(clazz$SharedConstants, String.class, 1));
    public static final FastNMS INSTANCE = instance();

    private static FastNMS instance() {
        try {
            String path = getImplPath();
            Class<?> clazz = Class.forName("net.momirealms.craftengine.bukkit.nms." + path + ".FastNMSImpl");
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (FastNMS) constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to initialize craftengine nms helper", e);
        }
    }

    private static @NotNull String getImplPath() throws IllegalAccessException {
        String versionString = (String) field$SharedConstants$VERSION_STRING.get(null);
        versionString = versionString.split("-", 2)[0];
        String classSuffix;
        switch (versionString) {
            case "1.21.6" -> classSuffix = "v1_21_6";
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

    public abstract InjectedHolder.Palette createInjectedPalettedContainerHolder(Object palettedContainer) throws InstantiationException;

    public abstract InjectedHolder.Section createInjectedLevelChunkSectionHolder(Object levelChunkSection);

    public abstract CollisionEntity createCollisionBoat(Object world, Object aabb,
                                                        double x, double y, double z, boolean canProjectileHit, boolean canCollide, boolean blocksBuilding);

    public abstract CollisionEntity createCollisionInteraction(Object world, Object aabb,
                                                               double x, double y, double z, boolean canProjectileHit, boolean canCollide, boolean blocksBuilding);

    public abstract Object method$PalettedContainer$getAndSet(Object palettedContainer, int x, int y, int z, Object blockState);

    public abstract BlockData method$CraftBlockData$fromData(Object blockState);

    public abstract int method$IdMapper$getId(Object idMapper, Object t);

    public abstract Object method$IdMapper$byId(Object idMapper, int id);

    public abstract Object method$CraftBlockData$getState(BlockData blockData);

    public abstract int method$BlockStateBase$getLightEmission(Object blockState);

    public abstract boolean method$BlockStateBase$canOcclude(Object blockState);

    public abstract Object method$LevelChunkSection$setBlockState(Object section, int x, int y, int z, Object blockState, boolean lock);

    public abstract Object method$LevelChunkSection$getBlockState(Object section, int x, int y, int z);

    public abstract Object field$CraftChunk$worldServer(Chunk chunk);

    public abstract Object method$ServerLevel$getChunkSource(Object serverLevel);

    public abstract Object method$ServerChunkCache$getChunkAtIfLoadedMainThread(Object serverChunkCache, int x, int z);

    public abstract Object method$ServerChunkCache$getChunk(Object serverChunkCache, int x, int z, boolean load);

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

    public abstract boolean method$LevelWriter$addFreshEntity(Object level, Object entity);

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

    public abstract Optional<Object> method$IdMap$byId(Object registry, int id);

    public abstract Optional<Integer> method$IdMap$getId(Object registry, Object value);

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

    public abstract void method$ItemStack$applyComponents(Object itemStack, Object component);

    public abstract Object method$ItemStack$getItem(Object itemStack);

    public abstract Object method$ItemStack$transmuteCopy(Object itemStack, Object item, int count);

    public abstract Object method$ItemStack$getComponentsPatch(Object itemStack);

    public abstract Object getComponentType(String namespace, String id);

    public abstract Object getComponent(Object itemStack, Object type);

    public abstract boolean hasComponent(Object itemStack, Object type);

    public abstract Object removeComponent(Object itemStack, Object type);

    public abstract String getCustomItemId(Object itemStack);

    public abstract void setCustomItemId(Object itemStack, String id);

    public abstract Runnable getBukkitTaskRunnable(BukkitTask bukkitTask);

    public abstract Object constructor$ClientboundLevelChunkWithLightPacket(net.momirealms.craftengine.core.util.FriendlyByteBuf buf);

    public abstract void method$ClientboundLevelChunkWithLightPacket$write(Object packet, net.momirealms.craftengine.core.util.FriendlyByteBuf buf);

    public abstract byte[] field$ClientboundLevelChunkPacketData$buffer(Object chunkData);

    public abstract boolean method$GrassBlock$isValidBonemealTarget(Object level, Object pos, Object state);

    public abstract void method$GrassBlock$performBoneMeal(Object level, Object random, Object blockPos, Object state, Object thisBlock);

    public abstract void method$LevelChunk$markUnsaved(Object chunk);

    public abstract boolean method$LevelChunk$isUnsaved(Object chunk);

    public abstract org.bukkit.entity.Entity getBukkitEntityById(World world, int entityId);

    public abstract Object constructor$ClientboundSystemChatPacket(Object component, boolean overlay);

    public abstract Object constructor$LevelChunkSection(Object section);

    public abstract Object field$LevelChunkSection$biomes(Object section);

    public abstract Object field$Entity$trackedEntity(Object entity);

    public abstract Object field$ChunkMap$TrackedEntity$serverEntity(Object trackedEntity);

    public abstract void method$ServerEntity$sendChanges(Object serverEntity);

    public abstract boolean method$AbstractArrow$isInGround(Object entity);

    public abstract boolean field$Entity$wasTouchingWater(Object entity);

    public abstract int field$ClientboundEntityPositionSyncPacket$id(Object packet);

    public abstract Object field$ClientboundEntityPositionSyncPacket$values(Object packet);

    public abstract boolean field$ClientboundEntityPositionSyncPacket$onGround(Object packet);

    public abstract Object constructor$ClientboundEntityPositionSyncPacket(int entityId, Object values, boolean onGround);

    public abstract Object field$PositionMoveRotation$position(Object values);

    public abstract Object field$PositionMoveRotation$deltaMovement(Object values);

    public abstract float field$PositionMoveRotation$yRot(Object values);

    public abstract float field$PositionMoveRotation$xRot(Object values);

    public abstract Object constructor$PositionMoveRotation(Object position, Object deltaMovement, float yRot, float xRot);

    public abstract UUID field$ClientboundAddEntityPacket$uuid(Object packet);

    public abstract double field$ClientboundAddEntityPacket$x(Object packet);

    public abstract double field$ClientboundAddEntityPacket$y(Object packet);

    public abstract double field$ClientboundAddEntityPacket$z(Object packet);

    public abstract float field$ClientboundAddEntityPacket$yRot(Object packet);

    public abstract float field$ClientboundAddEntityPacket$xRot(Object packet);

    public abstract float field$ClientboundAddEntityPacket$yHeadRot(Object packet);

    public abstract double field$ClientboundAddEntityPacket$xa(Object packet);

    public abstract double field$ClientboundAddEntityPacket$ya(Object packet);

    public abstract double field$ClientboundAddEntityPacket$za(Object packet);

    public abstract int field$ClientboundAddEntityPacket$data(Object packet);

    public abstract short field$ClientboundMoveEntityPacket$xa(Object packet);

    public abstract short field$ClientboundMoveEntityPacket$ya(Object packet);

    public abstract short field$ClientboundMoveEntityPacket$za(Object packet);

    public abstract byte field$ClientboundMoveEntityPacket$yRot(Object packet);

    public abstract byte field$ClientboundMoveEntityPacket$xRot(Object packet);

    public abstract boolean field$ClientboundMoveEntityPacket$onGround(Object packet);

    public abstract Object constructor$ClientboundMoveEntityPacket$PosRot(int entityId, short xa, short ya, short za, byte yRot, byte xRot, boolean onGround);

    public abstract Object constructor$Vec3(double x, double y, double z);

    public abstract Object constructor$ClientboundTeleportEntityPacket(int entityId, double x, double y, double z, byte yRot, byte xRot, boolean onGround);

    public abstract Object method$Registry$key(Object registry);

    public abstract Map<?, ?> method$TagNetworkSerialization$serializeTagsToNetwork();

    public abstract void method$TagNetworkSerialization$NetworkPayload$write(Object networkPayload, Object buffer);

    public abstract Object method$TagNetworkSerialization$NetworkPayload$read(Object buffer);

    public abstract Object constructor$ClientboundUpdateTagsPacket(Map<?, ?> tags);

    public abstract Object constructor$ClientboundActionBarPacket(Object component);

    public abstract Object constructor$ClientboundSetTitleTextPacket(Object component);

    public abstract Object constructor$ClientboundSetSubtitleTextPacket(Object component);

    public abstract Object constructor$ClientboundSetTitlesAnimationPacket(int fadeIn, int stay, int fadeOut);
    public abstract Object method$ItemStack$copyWithCount(Object stack, int count);

    public abstract Object method$ItemStack$copy(Object stack);

    public abstract float method$EnchantmentHelper$getTridentSpinAttackStrength(Object stack, Object entity);

    public abstract boolean method$Entity$isInWaterOrRain(Object entity);

    public abstract boolean method$ItemStack$nextDamageWillBreak(Object stack);

    public abstract void method$ItemStack$setDamageValue(Object stack, int damage);

    public abstract int method$ItemStack$getDamageValue(Object stack);

    public abstract Object method$EnchantmentHelper$pickHighestLevel(Object stack);

    public abstract Object method$Projectile$ThrownTrident$spawnProjectileFromRotationDelayed(Object level, Object spawnedFrom, Object owner, float z, float velocity, float innaccuracy);

    public abstract Object method$Projectile$Delayed$projectile(Object projectile);

    public abstract boolean method$Projectile$Delayed$attemptSpawn(Object projectile);

    public abstract Object field$Player$containerMenu(Object player);

    public abstract void field$Player$containerMenu(Object player, Object menu);

    public abstract void method$AbstractContainerMenu$sendAllDataToRemote(Object menu);

    public abstract void method$ItemStack$hurtWithoutBreaking(Object stack, int damage, Object player);

    public abstract void method$ItemStack$consume(Object stack, int amount, Object player);

    public abstract Object field$AbstractArrow$pickupItemStack(Object entity);

    public abstract void field$AbstractArrow$pickupItemStack(Object entity, Object pickupItemStack);

    public abstract boolean method$Player$hasInfiniteMaterials(Object player);

    public abstract Object field$AbstractArrow$pickup(Object entity);

    public abstract void field$AbstractArrow$pickup(Object entity, Object pickup);

    public abstract void method$Level$playSound(Object level, @Nullable Object entity, Object sourceEntity, Object sound, Object source, float volume, float pitch);

    public abstract float method$Entity$getYRot(Object entity);

    public abstract float method$Entity$getXRot(Object entity);

    public abstract void method$CraftEventFactory$callPlayerRiptideEvent(Object player, Object tridentItemStack, float velocityX, float velocityY, float velocityZ);

    public abstract void method$Entity$push(Object entity, double x, double y, double z);

    public abstract void method$Player$startAutoSpinAttack(Object player, int ticks, float damage, Object itemStack);

    public abstract boolean method$Entity$onGround(Object entity);

    public abstract void method$Entity$move(Object entity, Object type, Object movement);

    public abstract boolean method$ItemStack$isEmpty(Object stack);

    public abstract Object method$Holder$value(Object holder);

    public abstract boolean field$Entity$hurtMarked(Object entity);

    public abstract void field$Entity$hurtMarked(Object entity, boolean hurtMarked);

    public abstract Object constructor$ThrownTrident(Object level, Object owner, Object stack);

    public abstract void method$ThrownTrident$shootFromRotation(Object entity, Object shooter, float pitch, float yaw, float roll, float speed, float divergence);

    public abstract void method$ItemStack$hurtAndBreak(Object stack, int amount, Object entity, Object slot);

    public abstract Object method$LivingEntity$getSlotForHand(Object hand);

    public abstract Object method$LivingEntity$getUsedItemHand(Object entity);

    public abstract Object method$Player$getInventory(Object player);

    public abstract void method$Inventory$removeItem(Object inventory, Object stack);

    public abstract void method$ItemStack$hurtAndBreak(Object stack, int amount, Object entity, Consumer<?> breakCallback);

    public abstract Object method$Player$getAbilities(Object player);

    public abstract boolean field$Abilities$instabuild(Object abilities);

    public abstract void method$LivingEntity$broadcastBreakEvent(Object entity, Object hand);

    public abstract Object field$ThrownTrident$tridentItem(Object entity);

    public abstract void field$ThrownTrident$tridentItem(Object entity, Object tridentItem);

    public abstract ItemStack ensureCraftItemStack(ItemStack itemStack);

    public abstract Particle method$CraftParticle$toBukkit(Object nmsParticle);

    public abstract boolean method$SignalGetter$hasNeighborSignal(Object level, Object blockPos);

    public abstract Object method$BlockState$getBlock(Object blockState);

    public abstract Object method$BlockState$getShape(Object blockState, Object level, Object blockPos, Object collisionContext);

    public abstract Object method$BlockState$getCollisionShape(Object blockState, Object level, Object blockPos, Object collisionContext);

    public abstract Object method$BlockState$getBlockSupportShape(Object blockState, Object level, Object blockPos);

    public abstract boolean method$LightEngine$hasDifferentLightProperties(Object oldState, Object newState, Object blockGetter, Object blockPos);

    public abstract Object constructor$FriendlyByteBuf(ByteBuf buf);

    public abstract ItemStack method$FriendlyByteBuf$readItem(Object buf);

    public abstract void method$FriendlyByteBuf$writeItem(Object buf, ItemStack itemStack);

    public abstract ItemStack method$FriendlyByteBuf$readUntrustedItem(Object buf);

    public abstract void method$FriendlyByteBuf$writeUntrustedItem(Object buf, ItemStack itemStack);

    public abstract Codec method$DataComponentType$codec(Object componentType);

    public abstract Object field$ItemStack$getOrCreateTag(Object itemStack);

    public abstract Object constructor$ShortTag(short s);

    public abstract short method$ShortTag$value(Object shortTag);

    public abstract Object constructor$IntTag(int i);

    public abstract int method$IntTag$value(Object intTag);

    public abstract Object constructor$LongTag(long l);

    public abstract long method$LongTag$value(Object longTag);

    public abstract Object constructor$ByteTag(byte b);

    public abstract byte method$ByteTag$value(Object byteTag);

    public abstract Object constructor$FloatTag(float f);

    public abstract float method$FloatTag$value(Object floatTag);

    public abstract Object constructor$DoubleTag(double d);

    public abstract double method$DoubleTag$value(Object doubleTag);

    public abstract Object constructor$ByteArrayTag(byte[] bytes);

    public abstract byte[] method$ByteArrayTag$value(Object byteArrayTag);

    public abstract Object constructor$IntArrayTag(int[] ints);

    public abstract int[] method$IntArrayTag$value(Object intArrayTag);

    public abstract Object constructor$LongArrayTag(long[] longs);

    public abstract long[] method$LongArrayTag$value(Object longArrayTag);

    public abstract Object constructor$StringTag(String string);

    public abstract String method$StringTag$value(Object stringTag);

    public abstract Object constructor$ListTag();

    public abstract Object method$ListTag$get(Object listTag, int index);

    public abstract void method$ListTag$add(Object listTag, int index, Object value);

    public abstract Object method$ListTag$remove(Object listTag, int index);

    public abstract Object method$CompoundTag$get(Object compoundTag, String key);

    public abstract void method$CompoundTag$put(Object compoundTag, String key, Object value);

    public abstract void method$CompoundTag$remove(Object compoundTag, String key);

    public abstract Object constructor$CompoundTag();

    public abstract byte[] method$NbtIo$toBytes(Object tag) throws IOException;

    public abstract Object method$NbtIo$fromBytes(byte[] bytes) throws IOException;

    public abstract Object registryAccess();

    public abstract Object method$StatePredicate$always(boolean trueOrFalse);

    public abstract Object method$ResourceKey$create(Object registry, Object resourceLocation);

    public abstract List<Object> field$SimpleContainer$items(Object simpleContainer);

    public abstract Object field$SingleRecipeInput$item(Object singleRecipeInput);

    public abstract Object field$AbstractFurnaceBlockEntity$getItem(Object entity, int slot);

    public abstract Object method$TagParser$parseCompoundFully(String nbt) throws CommandSyntaxException;

    public abstract Object method$Registry$getValue(Object registry, Object resourceLocation);

    public abstract Object constructor$ItemStack(Object item, int count);

    public abstract void method$LevelAccessor$scheduleBlockTick(Object levelAccessor, Object blockPos, Object block, int ticks);

    public abstract void method$LevelAccessor$scheduleFluidTick(Object levelAccessor, Object blockPos, Object fluid, int ticks);

    public abstract void method$ItemStack$setTag(Object itemStack, Object compoundTag);

    public abstract Object method$AbstractContainerMenu$getCarried(Object menu);

    public abstract void method$AbstractContainerMenu$broadcastFullState(Object menu);

    public abstract List<Object> field$AbstractContainerMenu$dataSlots(Object menu);

    public abstract int method$DataSlot$get(Object dataSlot);

    public abstract int field$AbstractContainerMenu$containerId(Object containerMenu);

    public abstract Object method$AbstractContainerMenu$getSlot(Object containerMenu, int slot);

    public abstract Object method$Slot$getItem(Object slot);

    public abstract Object constructor$ClientboundContainerSetDataPacket(int containerId, int id, int data);

    public abstract boolean method$Entity$canBeCollidedWith(Object entity);

    public abstract Object itemStackToCompoundTag(ItemStack itemStack);

    public abstract Object method$Block$defaultState(Object block);

    public abstract boolean method$BlockStateBase$isSignalSource(Object blockState);

    public abstract Object method$Level$getFluidState(Object level, Object blockPos);

    public abstract Object method$FluidState$getType(Object fluidState);

    public abstract boolean method$Explosion$canTriggerBlocks(Object explosion);

    public abstract boolean method$BlockStateBase$canSurvive(Object blockState, Object level, Object blockPos);

    public abstract boolean method$BlockStateBase$isCollisionShapeFullBlock(Object blockState, Object level, Object blockPos);

    public abstract boolean method$BlockStateBase$isTagKeyBlock(Object blockState, Object tag);

    public abstract Object method$TagKey$create(Object registry, Object location);

    public abstract void method$Level$updateNeighborsAt(Object level, Object blockPos, Object block);

    public abstract boolean method$Block$canSupportRigidBlock(Object level, Object pos);

    public abstract boolean method$Block$canSupportCenter(Object level, Object pos, Object direction);

    public abstract void method$Level$setBlocksDirty(Object level, Object blockPos, Object oldState, Object newState);

    public abstract void method$BlockStateBase$isFaceSturdy(Object blockState, Object level, Object pos, Object face, Object supportType);

    public abstract int method$BasePressurePlateBlock$getEntityCount(Object entityGetter, Object aabb, Class entityClass);

    public abstract Object method$AABB$move(Object aabb, Object pos);

    public abstract void method$LevelAccessor$scheduleBlockTick(Object levelAccessor, Object blockPos, Object block, int ticks, Object priority);

    public abstract boolean method$LevelWriter$destroyBlock(Object level, Object pos, boolean dropBlock, @Nullable Object entity, int recursionLeft);

    public abstract boolean method$BlockStateBase$isAir(Object blockState);

    public abstract Object method$WorldlyContainerHolder$getContainer(Object block, Object state, Object level, Object pos);

    public abstract boolean method$BlockStateBase$hasBlockEntity(Object blockState);

    public abstract Object method$BlockGetter$getBlockEntity(Object blockGetter, Object blockPos);

    public abstract Object method$ChestBlock$getContainer(Object chest, Object state, Object level, Object pos, boolean override);

    public abstract Object method$EntityGetter$getEntities(Object entityGetter, @Nullable Object entity, Object area, Predicate<Object> predicate);

    public abstract Object method$AABB$ofSize(Object center, double xSize, double ySize, double zSize);

    public abstract Object method$BlockPos$getCenter(Object blockPos);

    public abstract boolean method$Entity$isAlive(Object entity);

    public abstract IntStream method$HopperBlockEntity$getSlots(Object container, Object direction);

    public abstract Object method$Container$removeItem(Object container, int slot, int amount);

    public abstract void method$Container$setChanged(Object container);

    public abstract void method$Container$setItem(Object container, int slot, Object stack);

    public abstract Object method$EntityGetter$getEntitiesOfClass(Object entityGetter, Class entityClass, Object area, Predicate<Object> filter);

    public abstract Object method$ItemEntity$getItem(Object itemEntity);

    public abstract void method$ItemStack$shrink(Object itemStack, int decrement);

    public abstract int method$ItemStack$getCount(Object itemStack);

    public abstract void method$Entity$discard(Object entity);

    public abstract Object method$BlockItem$place(Object blockItem, Object context);

    public abstract Object constructor$PlaceBlockBlockPlaceContext(Object level, Object hand, Object itemStack, Object hitResult);

    public abstract boolean method$InteractionResult$consumesAction(Object interactionResult);

    public abstract Object constructor$BlockHitResult(Override location, Object direction, Object blockPos, boolean inside);

    public abstract float method$EntityType$getHeight(Object entityType);

    public abstract Object constructor$ItemEntity(Object level, double posX, double posY, double posZ, Object itemStack);

    public abstract void method$ItemEntity$setDefaultPickUpDelay(Object itemEntity);
}
