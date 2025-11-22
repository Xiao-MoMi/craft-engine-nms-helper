package net.momirealms.craftengine.bukkit.nms;

import com.google.common.hash.HashCode;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.momirealms.craftengine.core.block.StatePropertyAccessor;
import net.momirealms.craftengine.core.item.recipe.*;
import net.momirealms.craftengine.core.plugin.network.ConnectionState;
import net.momirealms.craftengine.core.plugin.network.PacketFlow;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.chunk.InjectedHolder;
import org.bukkit.Chunk;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
            case "1.21.9", "1.21.10" -> "v1_21_9";
            case "1.21.6", "1.21.7", "1.21.8" -> "v1_21_6";
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

    public abstract StatePropertyAccessor createStatePropertyAccessor(Object blockState);

    public abstract Object toMinecraftIngredient(Ingredient<ItemStack> ingredient);

    public abstract Object getCraftEngineLootItemType();

    public abstract Object getCraftEngineCustomSimpleStateProviderType();

    public abstract Object getCraftEngineCustomWeightedStateProviderType();

    public abstract Object getCraftEngineCustomRotatedBlockProviderType();

    public abstract Object getCraftEngineCustomRandomizedIntStateProviderType();

    public abstract Object getCraftEngineCustomSimpleBlockFeature();

    public abstract InjectedHolder.Palette createInjectedPalettedContainerHolder(Object palettedContainer) throws InstantiationException;

    public abstract InjectedHolder.Section createInjectedLevelChunkSectionHolder(Object levelChunkSection);

    public abstract void injectedWorldGen(CEWorld world, Object chunkMap);

    public abstract CollisionEntity createCollisionBoat(Object world, Object aabb,
                                                        double x, double y, double z, boolean canProjectileHit, boolean canCollide, boolean blocksBuilding);

    public abstract CollisionEntity createCollisionInteraction(Object world, Object aabb,
                                                               double x, double y, double z, boolean canProjectileHit, boolean canCollide, boolean blocksBuilding);

    public abstract Object createShapedRecipe(CustomShapedRecipe<ItemStack> recipe);

    public abstract Object createShapelessRecipe(CustomShapelessRecipe<ItemStack> recipe);

    public abstract Object createSmokingRecipe(CustomSmokingRecipe<ItemStack> recipe);

    public abstract Object createSmeltingRecipe(CustomSmeltingRecipe<ItemStack> recipe);

    public abstract Object createBlastingRecipe(CustomBlastingRecipe<ItemStack> recipe);

    public abstract Object createCampfireRecipe(CustomCampfireRecipe<ItemStack> recipe);

    public abstract Object createStonecuttingRecipe(CustomStoneCuttingRecipe<ItemStack> recipe);

    public abstract Object createSmithingTransformRecipe(CustomSmithingTransformRecipe<ItemStack> recipe);

    public abstract Object createSmithingTrimRecipe(CustomSmithingTrimRecipe<ItemStack> recipe);

    public abstract Object createInjectedFallingBlockEntity(Object level, Object pos, Object blockState);

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

    public abstract void method$Connection$send(Object connection, Object packet, Object sendListener);

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

    public abstract boolean checkEntityCollision(Object level, List<Object> aabbs);

    public abstract void method$ItemStack$applyComponents(Object itemStack, Object component);

    public abstract Object method$ItemStack$getItem(Object itemStack);

    public abstract Object method$ItemStack$transmuteCopy(Object itemStack, Object item, int count);

    public abstract Object method$ItemStack$getComponentsPatch(Object itemStack);

    public abstract Object method$ItemStack$getComponent(Object itemStack, Object type);

    public abstract boolean method$ItemStack$hasComponent(Object itemStack, Object type);

    public abstract boolean method$ItemStack$hasNonDefaultComponent(Object itemStack, Object type);

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

    public abstract boolean method$LightEngine$hasDifferentLightProperties(Object oldState, Object newState);

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

    public abstract List<Object> method$EntityGetter$getEntitiesOfClass(Object entityGetter, Class entityClass, Object area, Predicate filter);

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

    public abstract Map<ConnectionState, Map<PacketFlow, Map<Class<?>, Integer>>> gamePacketIdsByClazz();

    public abstract Map<ConnectionState, Map<PacketFlow, Map<String, Integer>>> gamePacketIdsByName();

    public abstract Object method$FriendlyByteBuf$readById(Object buffer, Object idMap);

    public abstract Object method$ClientboundLevelParticlesPacket$readParticle(Object buffer, Object particleType);

    public abstract Object method$BlockParticleOption$getType(Object particle);

    public abstract Object constructor$BlockParticleOption(Object particleType, Object blockState);

    public abstract void method$FriendlyByteBuf$writeId(Object buffer, Object particle, Object idMap);

    public abstract void method$ParticleOptions$writeToNetwork(Object particle, Object buffer);

    public abstract Object method$SoundEvent$location(Object soundEvent);

    public abstract Object constructor$SoundEvent(Object location, Optional<Float> fixedRange);

    public abstract Optional<Float> method$SoundEvent$fixedRange(Object soundEvent);

    public abstract Object method$LootParams$Builder$getOptionalParameter(Object lootParamsBuilder, Object key);

    public abstract Object method$LootParams$Builder$getLevel(Object lootParamsBuilder);

    public abstract Player method$ServerPlayer$getBukkitEntity(Object player);

    public abstract void method$Block$dropResources(Object state, Object level, Object pos);

    public abstract BlockRedstoneEvent method$CraftEventFactory$callRedstoneChange(Object world, Object pos, int oldCurrent, int newCurrent);

    public abstract boolean method$LevelWriter$destroyBlock(Object level, Object pos, boolean drop);

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

    public abstract Object createDecoratedHashOpsGenerator(DynamicOps<HashCode> value);

    public abstract Object method$StateHolder$getValue(Object stateHolder, Object property);

    public abstract Object method$Entity$getType(Object entity);

    public abstract void method$BlockableEventLoop$scheduleOnMain(Runnable runnable);

    public abstract void method$Connection$handleDisconnection(Object connection);

    public abstract Object method$PacketSendListener$thenRun(Runnable runnable);

    public abstract void method$Connection$disconnect(Object connection, Object disconnectReason);

    public abstract boolean method$ItemStack$is(Object itemStack, Object tag);

    public abstract Object method$DyeItem$getDyeColor(Object dyeItem);

    public abstract int method$DyeColor$getTextureDiffuseColor(Object dyeColor);

    public abstract Object method$CraftInventoryCrafting$getMatrixInventory(CraftingInventory inventory);

    public abstract void method$CraftingContainer$setCurrentRecipe(Object container, Object recipe);

    public abstract Object method$CraftInventoryCrafting$getResultInventory(CraftingInventory inventory);

    public abstract void method$ResultContainer$setRecipeUsed(Object container, Object recipe);

    public abstract void method$RecipeManager$addRecipe(Object recipeManager, Object recipeHolder);

    public abstract Object method$Ingredient$of(Object[] items);

    public abstract void method$RecipeMap$removeRecipe(Object recipeMap, Object id);

    public abstract void method$RecipeManager$removeRecipe(Object recipeManager, Object id);

    public abstract Object constructor$RecipeHolder(Object id, Object recipe);

    public abstract int method$CraftingInput$ingredientCount(Object input);

    public abstract int method$CraftingInput$size(Object input);

    public abstract Object method$CraftingInput$getItem(Object input, int index);

    public abstract int method$Container$getContainerSize(Object container);

    public abstract boolean method$Item$canBeDepleted(Object item);

    public abstract int method$DyeColor$getFireworkColor(Object dyeColor);

    public abstract Object method$MinecraftServer$getRecipeManager(Object server);

    public abstract Object field$RecipeManager$recipes(Object recipeManager);

    public abstract Object method$MinecraftServer$getServer();

    public abstract Object constructor$InjectedHashedStack(Object hashedStack, net.momirealms.craftengine.core.entity.player.Player player);

    public abstract int field$ServerboundContainerClickPacket$containerId(Object packet);

    public abstract int field$ServerboundContainerClickPacket$stateId(Object packet);

    public abstract short field$ServerboundContainerClickPacket$slotNum(Object packet);

    public abstract byte field$ServerboundContainerClickPacket$buttonNum(Object packet);

    public abstract Object field$ServerboundContainerClickPacket$clickType(Object packet);

    public abstract Int2ObjectMap field$ServerboundContainerClickPacket$changedSlots(Object packet);

    public abstract Object field$ServerboundContainerClickPacket$carriedItem(Object packet);

    public abstract Object constructor$ServerboundContainerClickPacket(int containerId, int stateId, short slotNum, byte buttonNum, Object clickType, Int2ObjectMap changedSlots, Object carriedItem);

    public abstract Object method$CraftInventory$getInventory(Inventory inventory);

    public abstract boolean method$BlockStateBase$isBlock(Object blockState, Object block);

    public abstract Object field$BlockHitResult$blockPos(Object result);

    public abstract Object field$HitResult$location(Object result);

    public abstract boolean field$BlockHitResul$miss(Object result);

    public abstract Object field$BlockHitResul$direction(Object result);

    public abstract Object method$ChunkSource$getLightEngine(Object chunkSource);

    public abstract void method$Level$updateNeighbourForOutputSignal(Object level, Object pos, Object block);

    public abstract Object method$ItemStack$of(Object compoundTag);

    public abstract Object constructor$ClientboundRemoveEntitiesPacket(IntList entities);

    public abstract boolean method$Entity$causeFallDamage(Object entity, Number fallDistance, float damageMultiplier, Object damageSource);

    public abstract Object method$Entity$damageSources(Object entity);

    public abstract Object method$DamageSources$fall(Object damageSources);

    public abstract boolean method$Entity$getSharedFlag(Object entity, int flag);

    public abstract Object method$Entity$getDeltaMovement(Object entity);

    public abstract void method$Entity$setDeltaMovement(Object entity, double x, double y, double z);

    public abstract boolean field$Entity$hurtMarked(Object entity);

    public abstract void field$Entity$hurtMarked(Object entity, boolean hurtMarked);

    public abstract Inventory createSimpleStorageContainer(InventoryHolder owner, int size, boolean canPlaceItem, boolean canTakeItem);

    public abstract Object method$FluidState$createLegacyBlock(Object fluidState);

    public abstract boolean method$LevelSection$hasOnlyAir(Object levelSection);

    public abstract void method$LightEventListener$updateSectionStatus(Object lightEngine, Object sectionPos, boolean hasOnlyAir);

    public abstract void method$ThreadedLevelLightEngine$checkBlock(Object levelEngine, Object blockPos);

    public abstract Object method$SectionPos$of(int x, int y, int z);

    public abstract Object method$BlockBehaviour$BlockStateBase$getSoundType(Object blockState);

    public abstract Object field$SoundType$breakSound(Object soundType);

    public abstract Object field$SoundType$placeSound(Object soundType);

    public abstract Object field$SoundType$hitSound(Object soundType);

    public abstract Object field$SoundType$fallSound(Object soundType);

    public abstract Object field$SoundType$stepSound(Object soundType);

    public abstract float field$SoundType$volume(Object soundType);

    public abstract float field$SoundType$pitch(Object soundType);

    public abstract Object method$Holder$direct(Object value);

    public abstract Object constructor$ClientboundSoundPacket(Object sound, Object source, double x, double y, double z, float volume, float pitch, long seed);

    public abstract boolean method$LeadItem$bindPlayerMobs(Object player, Object world, Object pos);

    public abstract boolean method$FenceGateBlock$connectsToDirection(Object state, Object direction);

    public abstract boolean method$Entity$isSpectator(Object entity);

    public abstract boolean method$Entity$isIgnoringBlockTriggers(Object entity);

    public abstract Object method$VoxelShape$bounds(Object voxelShape);

    public abstract void method$Level$updateNeighborsAt(Object levelAccessor, Object pos, Object block, @Nullable Object orientation);

    public abstract @Nullable Object method$ExperimentalRedstoneUtils$initialOrientation(Object level, @Nullable Object front, @Nullable Object up);

    public abstract void method$LevelAccessor$playSound(Object level, @Nullable Object entity, Object pos, Object sound, Object source, float volume, float pitch);

    public abstract void method$LevelAccessor$gameEvent(Object level, @Nullable Object entity, Object gameEvent, Object pos);

    public abstract void method$BlockBehaviour$BlockStateBase$tick(Object blockState, Object level, Object pos);

    public abstract Object method$Holder$value(Object holder);

    public abstract Object method$SynchedEntityData$get(Object synchedEntityData, Object dataParameter);

    public abstract void method$BlockBehaviour$BlockStateBase$randomTick(Object blockState, Object level, Object pos);

    public abstract Object field$BlockBehaviour$BlockStateBase$fluidState(Object blockState);

    public abstract int field$FluidState$amount(Object fluidState);

    public abstract Object method$StateHolder$trySetValue(Object stateHolder, Object property, Comparable value);

    public abstract boolean method$Inventory$add(Object inventory, Object itemStack);

    public abstract Object method$ServerPlayer$drop(Object serverPlayer, Object droppedItem, boolean dropAround, boolean traceItem, boolean callEvent, Consumer operation);

    public abstract void method$ItemEntity$makeFakeItem(Object itemEntity);

    public abstract void method$ItemEntity$setNoPickUpDelay(Object itemEntity);

    public abstract void method$ItemEntity$setTarget(Object itemEntity, UUID uuid);

    public abstract void method$AbstractContainerMenu$broadcastChanges(Object menu);

    public abstract Object field$Entity$entityData(Object entity);

    public abstract void method$SynchedEntityData$set(Object synchedEntityData, Object dataParameter, Object value, boolean force);

    public abstract Object method$DataComponentExactPredicate$allOf(Object componentMap);

    public abstract Object method$ItemStack$getComponents(Object itemStack);

    public abstract Object method$Item$builtInRegistryHolder(Object item);

    public abstract Object field$ItemCost$itemStack(Object itemCost);

    public abstract Object constructor$ItemCost(Object holder, int count, Object predicate);

    public abstract Iterable method$BundleContents$items(Object bundleContents);

    public abstract Object constructor$BundleContents(List items);

    public abstract List field$ItemContainerContents$items(Object contents);

    public abstract Object method$ItemContainerContents$fromItems(List list);

    public abstract Object constructor$BlockPlaceContext(Object level, Object player, Object interactionHand, Object itemStack, Object hitResult);

    public abstract Object constructor$BlockHitResult(Object location, Object direction, Object blockPos, boolean inside);

    public abstract Object method$BlockItem$getBlock(Object blockItem);

    public abstract Object method$Block$getStateForPlacement(Object block, Object blockPlaceContext);

    public abstract Object constructor$Vec3(double x, double y, double z);

    public abstract Object method$AbstractContainerMenu$quickMoveStack(Object menu, Object player, int slot);

    public abstract Object method$CraftingContainer$getCurrentRecipe(Object container);

    public abstract void method$ItemStack$hurtAndBreak(Object itemStack, int amount, Object livingEntity, Object slot);

    public abstract Object constructor$ClientboundEntityPositionSyncPacket(int entityId, double x, double y, double z, float yRot, float xRot, boolean onGround);

    public abstract Object field$ServerChunkCache$chunkMap(Object chunkSource);

    public abstract Map field$ClientboundUpdateTagsPacket$tags(Object packet);

    public abstract int method$LightEngine$getLightBlockInto(@Nullable("1.21.2+") Object level, Object state1, @Nullable("1.21.2+") Object pos1, Object state2, @Nullable("1.21.2+") Object pos2, Object direction, int defaultReturnValue);

    public abstract int method$BlockBehaviour$BlockStateBase$getLightBlock(Object blockStateBase, @Nullable("1.21.2+") Object level, @Nullable("1.21.2+") Object pos);

    public abstract boolean method$FluidState$is(Object fluidState, Object tag);

    public abstract int method$LevelReader$getMaxLocalRawBrightness(Object level, Object pos);

    public abstract Object method$BlockPos$offset(Object pos, int x, int y, int z);

    public abstract Object field$Player$inventoryMenu(Object serverPlayer);

    public abstract Object method$InventoryMenu$getCraftSlots(Object menu);

    public abstract void method$InventoryMenu$slotsChanged(Object menu, Object container);

    public abstract int method$Inventory$clearOrCountMatchingItems(Object inventory, Predicate stackPredicate, int maxCount, Object container);
}
