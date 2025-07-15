package net.momirealms.craftengine.bukkit.nms;

import com.google.common.hash.HashCode;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.chunk.InjectedHolder;
import org.bukkit.Chunk;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;

@SuppressWarnings({"unused", "rawtypes"})
public abstract class FastNMS {
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
        return switch (VersionHelper.MINECRAFT_VERSION.version()) {
            case "1.21.6", "1.21.7" -> "v1_21_6";
            case "1.21.5" -> "v1_21_5";
            case "1.21.4" -> "v1_21_4";
            case "1.21.2", "1.21.3" -> "v1_21_2";
            case "1.21", "1.21.1" -> "v1_21";
            case "1.20.5", "1.20.6" -> "v1_20_5";
            case "1.20.3", "1.20.4" -> "v1_20_3";
            case "1.20.2" -> "v1_20_2";
            case "1.20", "1.20.1" -> "v1_20";
            default -> throw new UnsupportedVersionException();
        };
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

    public abstract boolean method$ServerLevel$isPreventingStatusUpdates(Object serverLevel, int x, int z);

    public abstract int method$Entity$getId(Object entity);

    public abstract boolean method$LevelWriter$setBlock(Object level, Object blockPos, Object blockState, int flags);

    public abstract Object method$ServerChunkCache$getVisibleChunkIfPresent(Object chunkSource, long chunkKey);

    public abstract Object constructor$ChunkPos(int x, int z);

    public abstract Object constructor$ClientboundLightUpdatePacket(Object chunkPos, Object lightEngine, BitSet skyChangedLightSectionFilter, BitSet blockChangedLightSectionFilter);

    public abstract void method$ServerChunkCache$blockChanged(Object chunkCache, Object blockPos);

    public abstract void method$Connection$send(Object connection, Object packet);

    public abstract List<Object> method$ChunkHolder$getPlayers(Object chunkHolder);

    public abstract Object constructor$ClientboundBundlePacket(List<Object> packets);

    public abstract Object field$Player$connection(Object player);

    public abstract Object field$ServerGamePacketListenerImpl$connection(Object serverGamePacketListener);

    public abstract void method$ServerPlayerConnection$send(Object connection, Object packet);

    public abstract Channel field$Connection$channel(Object connection);

    public abstract void method$BlockStateBase$onPlace(Object blockState, Object world, Object blockPos, Object oldBlockState, boolean movedByPiston);

    public abstract void method$LevelAccessor$levelEvent(Object level, int eventId, Object blockPos, int stateId);

    public abstract Iterable<Object> method$ClientboundBundlePacket$subPackets(Object packet);

    public abstract Object method$ResourceLocation$fromNamespaceAndPath(String namespace, String path);

    public abstract Object field$SoundEvent$location(Object soundEvent);

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

    public abstract org.bukkit.entity.Entity method$Entity$getBukkitEntity(Object entity);

    public abstract Optional<Object> method$IdMap$byId(Object registry, int id);

    public abstract Optional<Integer> method$IdMap$getId(Object registry, Object value);

    public abstract void method$SoundEvent$directEncode(ByteBuf buffer, Object soundEvent);

    public abstract List<Object> field$ClientboundPlayerInfoUpdatePacket$entries(Object packet);

    public abstract EnumSet<? extends Enum> field$ClientboundPlayerInfoUpdatePacket$actions(Object packet);

    public abstract Object constructor$ClientboundPlayerInfoUpdatePacket(EnumSet actions, List entries);

    public abstract Optional method$RecipeManager$getRecipeFor(Object recipeManager, Object recipeType, Object recipeInput, Object level, Object resourceKeyOrLocation);

    public abstract Object field$ClientboundPlayerInfoUpdatePacket$Entry$displayName(Object entry);

    public abstract Object constructor$ClientboundPlayerInfoUpdatePacket$Entry(Object entry, Object newDisplayName);

    public abstract void method$ItemStack$setComponent(Object itemStack, Object resourceLocation, Object component);

    public abstract Object field$CraftItemStack$handle(ItemStack itemStack);

    public abstract Object field$ServerPlayer$gameMode(Object player);

    public abstract void field$Player$mayBuild(Object player, boolean can);

    public abstract boolean field$Player$mayBuild(Object player);

    public abstract double method$Player$getInteractionRange(Object player);

    public abstract int field$MinecraftServer$currentTick();

    public abstract float method$BlockStateBase$getDestroyProgress(Object blockState, Object player, Object level, Object blockPos);

    public abstract boolean method$ItemStack$isCorrectToolForDrops(Object itemStack, Object blockState);

    public abstract boolean method$Player$hasCorrectToolForDrops(Object player, Object state);

    public abstract Object constructor$ClientboundBlockDestructionPacket(int entityId, Object blockPos, int stage);

    public abstract Object constructor$ClientboundLevelEventPacket(int id, Object blockPos, int data, boolean global);

    public abstract Object constructor$BlockInWorld(Object level, Object blockPos, boolean loadChunk);

    public abstract boolean method$ItemStack$canBreakInAdventureMode(Object itemStack, Object blockInWorld);

    public abstract boolean method$ItemStack$canPlaceInAdventureMode(Object itemStack, Object blockInWorld);

    public abstract Object method$Direction$getOpposite(Object direction);

    public abstract Object method$BlockPos$relative(Object blockPos, Object direction);

    public abstract String field$ClientboundResourcePackPushPacket$url(Object packet);

    public abstract Object constructor$ClientboundResourcePackPushPacket(UUID uuid, String url, String sha1, boolean kick, Object component);

    public abstract Object constructor$ClientboundResourcePackPopPacket(UUID uuid);

    public abstract UUID field$ClientboundResourcePackPushPacket$uuid(Object packet);

    public abstract Object constructor$ServerboundResourcePackPacket$SUCCESSFULLY_LOADED(UUID uuid);

    public abstract Object method$CraftEntityType$toNMSEntityType(org.bukkit.entity.EntityType entityType);

    public abstract boolean method$BonemealableBlock$isValidBonemealTarget(Object block, Object level, Object blockPos, Object state);

    public abstract Object constructor$ClientboundSetEntityDataPacket(int entityId, List data);

    public abstract Object constructor$ClientboundAddEntityPacket(int id, UUID uuid,
                                                                  double x, double y, double z, float xRot, float yRot,
                                                                  Object type, int data, Object deltaMovement, double yHeadRot);

    public abstract Object method$SynchedEntityData$DataValue$create(Object entityDataAccessor, Object value);

    public abstract Object constructor$EntityDataAccessor(int id, Object serializer);

    public abstract void simulateInteraction(Object player, Object direction, double x, double y, double z, Object pos);

    public abstract boolean checkEntityCollision(Object level, List<Object> aabbs, double x, double y, double z);

    public abstract void method$ItemStack$applyComponents(Object itemStack, Object component);

    public abstract Object method$ItemStack$getItem(Object itemStack);

    public abstract Object method$ItemStack$transmuteCopy(Object itemStack, Object item, int count);

    public abstract Object method$ItemStack$getComponentsPatch(Object itemStack);

    public abstract Object method$ItemStack$getComponent(Object itemStack, Object type);

    public abstract boolean method$ItemStack$hasComponent(Object itemStack, Object type);

    public abstract Object method$ItemStack$removeComponent(Object itemStack, Object type);

    public abstract String getCustomItemId(Object itemStack);

    public abstract void setCustomItemId(Object itemStack, String id);

    public abstract void method$LevelChunk$markUnsaved(Object chunk);

    public abstract boolean method$LevelChunk$isUnsaved(Object chunk);

    public abstract Object constructor$ClientboundSystemChatPacket(Object component, boolean overlay);

    public abstract Object constructor$LevelChunkSection(Object section);

    public abstract Object field$LevelChunkSection$biomes(Object section);

    public abstract boolean field$Entity$wasTouchingWater(Object entity);

    public abstract Object constructor$ClientboundUpdateTagsPacket(Map<?, ?> tags);

    public abstract Object constructor$ClientboundActionBarPacket(Object component);

    public abstract Object constructor$ClientboundSetTitleTextPacket(Object component);

    public abstract Object constructor$ClientboundSetSubtitleTextPacket(Object component);

    public abstract Object constructor$ClientboundSetTitlesAnimationPacket(int fadeIn, int stay, int fadeOut);

    public abstract Object field$Player$containerMenu(Object player);

    public abstract void field$Player$containerMenu(Object player, Object menu);

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

    public abstract Object registryAccess();

    public abstract Object method$StatePredicate$always(boolean trueOrFalse);

    public abstract Object method$ResourceKey$create(Object registry, Object resourceLocation);

    public abstract List<Object> field$SimpleContainer$items(Object simpleContainer);

    public abstract Object field$SingleRecipeInput$item(Object singleRecipeInput);

    public abstract Object field$AbstractFurnaceBlockEntity$getItem(Object entity, int slot);

    public abstract Object method$TagParser$parseCompoundFully(String nbt) throws CommandSyntaxException;

    public abstract Object method$Registry$getValue(Object registry, Object resourceLocation);

    public abstract Object constructor$ItemStack(Object item, int count);

    public abstract void method$ScheduledTickAccess$scheduleBlockTick(Object levelAccessor, Object blockPos, Object block, int ticks);

    public abstract void method$ScheduledTickAccess$scheduleFluidTick(Object levelAccessor, Object blockPos, Object fluid, int ticks);

    public abstract void method$ItemStack$setTag(Object itemStack, Object compoundTag);

    public abstract Object method$AbstractContainerMenu$getCarried(Object menu);

    public abstract void method$AbstractContainerMenu$broadcastFullState(Object menu);

    public abstract List<Object> field$AbstractContainerMenu$dataSlots(Object menu);

    public abstract int method$DataSlot$get(Object dataSlot);

    public abstract int field$AbstractContainerMenu$containerId(Object containerMenu);

    public abstract Object method$AbstractContainerMenu$getSlot(Object containerMenu, int slot);

    public abstract Object method$Slot$getItem(Object slot);

    public abstract Object constructor$ClientboundContainerSetDataPacket(int containerId, int id, int data);

    public abstract Object method$Block$defaultState(Object block);

    public abstract boolean method$BlockStateBase$isSignalSource(Object blockState);

    public abstract Object method$BlockGetter$getFluidState(Object level, Object blockPos);

    public abstract Object method$FluidState$getType(Object fluidState);

    public abstract boolean method$Explosion$canTriggerBlocks(Object explosion);

    public abstract boolean method$BlockStateBase$canSurvive(Object blockState, Object level, Object blockPos);

    public abstract boolean method$BlockStateBase$isCollisionShapeFullBlock(Object blockState, Object level, Object blockPos);

    public abstract String method$ResourceLocation$namespace(Object resourceLocation);

    public abstract String method$ResourceLocation$path(Object resourceLocation);

    public abstract boolean method$BlockStateBase$is(Object blockState, Object tag);

    public abstract boolean method$BlockStateBase$isAir(Object blockState);

    public abstract boolean method$Block$canSupportRigidBlock(Object level, Object pos);

    public abstract boolean method$Block$canSupportCenter(Object level, Object pos, Object direction);

    public abstract void method$Level$setBlocksDirty(Object level, Object blockPos, Object oldState, Object newState);

    public abstract boolean method$BlockStateBase$isFaceSturdy(Object blockState, Object level, Object pos, Object face, Object supportType);

    public abstract int method$EntityGetter$getEntitiesOfClass(Object entityGetter, Object aabb, Class entityClass);

    public abstract Object method$AABB$move(Object aabb, Object pos);

    public abstract void method$Level$updateNeighborsAt(Object levelAccessor, Object blockPos, Object block);

    public abstract Object field$Entity$trackedEntity(Object entity);

    public abstract Object field$ChunkMap$TrackedEntity$serverEntity(Object trackedEntity);

    public abstract boolean method$AbstractArrow$isInGround(Object entity);

    public abstract Map method$TagNetworkSerialization$serializeTagsToNetwork();

    public abstract void method$TagNetworkSerialization$NetworkPayload$write(Object networkPayload, Object buffer);

    public abstract Object method$TagNetworkSerialization$NetworkPayload$read(Object buffer);

    public abstract Object method$Registry$getKey(Object registry, Object value);

    public abstract boolean method$ItemStack$isEmpty(Object stack);

    public abstract ItemStack method$CraftItemStack$asCraftCopy(ItemStack stack);

    public abstract Object method$Item$components(Object item);

    public abstract Object method$DataComponentMap$get(Object dataComponentMap, Object componentType);

    public abstract Object method$TagKey$create(Object registry, Object location);

    public abstract boolean method$BlockStateBase$isReplaceable(Object blockState);

    public abstract void method$ClientboundSetEntityDataPacket$pack(List<?> dataValues, Object friendlyByteBuf);

    public abstract List<Object> method$ClientboundSetEntityDataPacket$unpack(Object friendlyByteBuf);

    public abstract int method$ClientboundEntityPositionSyncPacket$id(Object packet);

    public abstract Object field$ClientboundEntityPositionSyncPacket$values(Object packet);

    public abstract boolean field$ClientboundEntityPositionSyncPacket$onGround(Object packet);

    public abstract Object field$PositionMoveRotation$position(Object values);

    public abstract Object field$PositionMoveRotation$deltaMovement(Object values);

    public abstract float field$PositionMoveRotation$yRot(Object values);

    public abstract float field$PositionMoveRotation$xRot(Object values);

    public abstract Object constructor$PositionMoveRotation(Object position, Object deltaMovement, float yRot, float xRot);

    public abstract Object constructor$ClientboundEntityPositionSyncPacket(int entityId, Object values, boolean onGround);

    public abstract short field$ClientboundMoveEntityPacket$xa(Object packet);

    public abstract short field$ClientboundMoveEntityPacket$ya(Object packet);

    public abstract short field$ClientboundMoveEntityPacket$za(Object packet);

    public abstract byte field$ClientboundMoveEntityPacket$yRot(Object packet);

    public abstract byte field$ClientboundMoveEntityPacket$xRot(Object packet);

    public abstract boolean field$ClientboundMoveEntityPacket$onGround(Object packet);

    public abstract Object constructor$ClientboundMoveEntityPacket$PosRot(int entityId, short xa, short ya, short za, byte yRot, byte xRot, boolean onGround);

    public abstract Map<String, Map<Class<?>, Integer>> gamePacketIdsByClazz();

    public abstract Map<String, Map<String, Integer>> gamePacketIdsByName();

    public abstract Object method$FriendlyByteBuf$readById(Object buffer, Object idMap);

    public abstract Object method$ClientboundLevelParticlesPacket$readParticle(Object buffer, Object particleType);

    public abstract Object method$BlockParticleOption$getType(Object particle);

    public abstract Object constructor$BlockParticleOption(Object particleType, Object blockState);

    public abstract void method$FriendlyByteBuf$writeId(Object buffer, Object particle, Object idMap);

    public abstract void method$ParticleOptions$writeToNetwork(Object particle, Object buffer);

    public abstract Object method$StreamCodec$decode(Object streamCodec, Object byteBuffer);

    public abstract void method$StreamCodec$encode(Object streamCodec, Object byteBuffer, Object value);

    public abstract Object method$SoundEvent$location(Object soundEvent);

    public abstract Object constructor$SoundEvent(Object location, Optional<Float> fixedRange);

    public abstract Optional<Float> method$SoundEvent$fixedRange(Object soundEvent);

    public abstract Object method$LootParams$Builder$getOptionalParameter(Object lootParamsBuilder, Object key);

    public abstract Object method$LootParams$Builder$getLevel(Object lootParamsBuilder);

    public abstract Player method$ServerPlayer$getBukkitEntity(Object player);

    public abstract void method$Block$dropResources(Object state, Object level, Object pos);

    public abstract BlockRedstoneEvent method$CraftEventFactory$callRedstoneChange(Object world, Object pos, int oldCurrent, int newCurrent);

    public abstract boolean method$Level$destroyBlock(Object level, Object pos, boolean drop);

    public abstract Object method$itemStack$save(Object itemStack, Object compoundTag);

    public abstract Object method$ItemStack$getTag(Object itemStack);

    public abstract Set<Map.Entry> method$CompoundTag$entrySet(Object compoundTag);

    public abstract Object method$CompoundTag$merge(Object tag1, Object tag2);

    public abstract Object method$CompoundTag$copy(Object compoundTag);

    public abstract void method$Player$startSleepInBed(Object player, Object pos, boolean force);

    public abstract Object field$ServerboundResourcePackPacket$action(Object packet);

    public abstract UUID field$ServerboundResourcePackPacket$id(Object packet);

    public abstract Object method$Block$asItem(Object block);

    public abstract Object method$RegistryAccess$lookupOrThrow(Object registryAccess, Object resourceKey);

    public abstract int method$Registry$getId(Object registry, Object value);

    public abstract Optional<Object> method$Registry$getHolderByResourceLocation(Object registry, Object resourceLocation);

    public abstract Optional<Object> method$Registry$getHolderByResourceKey(Object registry, Object resourceKey);

    public abstract Object method$ServerLevel$getEntityLookup(Object serverLevel);

    public abstract Object method$EntityLookup$get(Object entityLookup, int id);

    public abstract boolean field$BlockBehavior$hasCollision(Object block);

    public abstract Object method$Connection$getPacketListener(Object connection);

    public abstract Object constructor$ServerResourcePackConfigurationTask(Object info);

    public abstract Object constructor$ServerResourcePackInfo(UUID id, String url, String hash, boolean isRequired, @Nullable Object prompt);

    public abstract void method$ServerConfigurationPacketListenerImpl$returnToWorld(Object packetListener);

    public abstract boolean method$BlockStateBase$isPathFindable(Object blockState, Object blockGetter, Object blockPos, Object type);

    public abstract void method$LevelAccessor$levelEvent(Object level, Object entity, int eventId, Object blockPos, int stateId);

    public abstract Object field$Player$abilities(Object player);

    public abstract boolean field$Abilities$instabuild(Object abilities);

    public abstract GameProfile field$ClientboundLoginFinishedPacket$gameProfile(Object packet);

    public abstract Optional method$TrimMaterials$getFromIngredient(Object itemStack);

    public abstract Optional method$TrimPatterns$getFromTemplate(Object itemStack);

    public abstract Object constructor$ArmorTrim(Object trimMaterial, Object trimPattern);

    public abstract Object method$CustomData$getUnsafe(Object customData);

    public abstract boolean method$ServerLevel$setChunkForced(Object serverLevel, int chunkX, int chunkZ, boolean add);

    public abstract boolean method$LevelReader$isClientSide(Object level);

    public abstract void method$ScheduledTickAccess$scheduleBlockTick(Object level, Object blockPos, Object block, int ticks, Object priority);

    public abstract Object method$StreamDecoder$decode(Object streamDecoder, Object buf);

    public abstract void method$StreamEncoder$encode(Object streamEncoder, Object buf, Object value);

    public abstract boolean method$HashedStack$matches(Object hashedStack, Object itemStack, Object hashGenerator);

    public abstract Object method$Player$getInventory(Object player);

    public abstract Object method$Container$getItem(Object container, int slot);

    public abstract Object method$HashedStack$create(Object itemStack, Object hashGenerator);

    public abstract DataResult<Object> method$TypedDataComponent$encodeValue(Object typedDataComponent, DynamicOps<HashCode> value);
}
