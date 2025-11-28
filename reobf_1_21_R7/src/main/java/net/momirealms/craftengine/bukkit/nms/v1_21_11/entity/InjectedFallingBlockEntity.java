package net.momirealms.craftengine.bukkit.nms.v1_21_11.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.momirealms.craftengine.bukkit.block.behavior.ConcretePowderBlockBehavior;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.properties.BooleanProperty;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldPosition;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Optional;

public class InjectedFallingBlockEntity extends FallingBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    public InjectedFallingBlockEntity(Level level, double x, double y, double z, BlockState state) {
        super(level, x, y, z, state);
    }

    public static InjectedFallingBlockEntity fall(@NotNull Level level, BlockPos pos, BlockState blockState) {
        ImmutableBlockState customBlockState = BlockStateUtils.getOptionalCustomBlockState(blockState).orElse(null);
        BlockState finalBlockState = blockState;
        if (customBlockState != null) {
            for (Property<?> property : customBlockState.getProperties()) {
                if (!property.name().equals("waterlogged") && property.valueClass() != Boolean.class) continue;
                finalBlockState = customBlockState.get((BooleanProperty) property)
                        ? (BlockState) customBlockState.with((BooleanProperty) property, false).customBlockState().literalObject()
                        : blockState;
                break;
            }
        } else {
            finalBlockState = blockState.hasProperty(BlockStateProperties.WATERLOGGED)
                    ? blockState.setValue(BlockStateProperties.WATERLOGGED, false)
                    : blockState;
        }
        InjectedFallingBlockEntity fallingBlockEntity = new InjectedFallingBlockEntity(
                level,
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5,
                finalBlockState
        );
        if (!CraftEventFactory.callEntityChangeBlockEvent(fallingBlockEntity, pos, blockState.getFluidState().createLegacyBlock())) return fallingBlockEntity;
        level.setBlock(pos, blockState.getFluidState().createLegacyBlock(), 3);
        level.addFreshEntity(fallingBlockEntity);
        return fallingBlockEntity;
    }

    @Override
    public ItemEntity spawnAtLocation(@NotNull ServerLevel level, @NotNull ItemLike item) {
        Optional<ImmutableBlockState> optionalCustomState = BlockStateUtils.getOptionalCustomBlockState(super.getBlockState());
        if (optionalCustomState.isEmpty()) return null;
        ImmutableBlockState customState = optionalCustomState.get();
        World world = new BukkitWorld(this.level().getWorld());
        WorldPosition position = new WorldPosition(world, this.xo, this.yo, this.zo);
        ContextHolder.Builder builder = ContextHolder.builder()
                .withParameter(DirectContextParameters.FALLING_BLOCK, true)
                .withParameter(DirectContextParameters.POSITION, position);
        for (Item<Object> ceitem : customState.getDrops(builder, world, null)) {
            world.dropItemNaturally(position, ceitem);
        }
        return null;
    }

    @SuppressWarnings({"all", "removal"})
    @Override
    public void tick() {
        if (this.blockState.isAir()) {
            this.discard(EntityRemoveEvent.Cause.DESPAWN);
        } else {
            Block block = this.blockState.getBlock();
            ++this.time;
            this.applyGravity();
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.applyEffectsFromBlocks();
            if (this.level().paperConfig().fixes.fallingBlockHeightNerf.test((v) -> this.getY() > (double)v)) {
                if (this.dropItem) {
                    Level var22 = this.level();
                    if (var22 instanceof ServerLevel) {
                        ServerLevel serverLevel = (ServerLevel)var22;
                        if ((Boolean)serverLevel.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.ENTITY_DROPS)) {
                            this.spawnAtLocation(serverLevel, block);
                        }
                    }
                }

                this.discard(EntityRemoveEvent.Cause.OUT_OF_WORLD);
                return;
            }

            this.handlePortal();
            Level var3 = this.level();
            if (var3 instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)var3;
                if (this.isAlive() || this.forceTickAfterTeleportToDuplicate) {
                    BlockPos blockPos = this.blockPosition();
                    ConcretePowderBlockBehavior behavior = BlockStateUtils.getOptionalCustomBlockState(this.getBlockState())
                            .map(ImmutableBlockState::behavior)
                            .map(it -> it.getAs(ConcretePowderBlockBehavior.class).orElse(null))
                            .orElse(null);
                    boolean flag = this.blockState.getBlock() instanceof ConcretePowderBlock || behavior != null;
                    boolean flag1 = flag && this.level().getFluidState(blockPos).is(FluidTags.WATER);
                    double d = this.getDeltaMovement().lengthSqr();
                    if (flag && d > (double)1.0F) {
                        BlockHitResult blockHitResult = this.level().clip(new ClipContext(new Vec3(this.xo, this.yo, this.zo), this.position(), net.minecraft.world.level.ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this));
                        if (blockHitResult.getType() != HitResult.Type.MISS && this.level().getFluidState(blockHitResult.getBlockPos()).is(FluidTags.WATER)) {
                            blockPos = blockHitResult.getBlockPos();
                            flag1 = true;
                        }
                    }

                    if (!this.onGround() && !flag1) {
                        if (this.time > 100 && this.autoExpire && (blockPos.getY() <= this.level().getMinY() || blockPos.getY() > this.level().getMaxY()) || this.time > 600 && this.autoExpire) {
                            if (this.dropItem && (Boolean)serverLevel.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.ENTITY_DROPS)) {
                                this.spawnAtLocation(serverLevel, block);
                            }

                            this.discard(EntityRemoveEvent.Cause.DROP);
                        }
                    } else {
                        BlockState blockState = this.level().getBlockState(blockPos);
                        this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, (double)-0.5F, 0.7));
                        if (!blockState.is(Blocks.MOVING_PISTON)) {
                            if (this.cancelDrop) {
                                this.discard(EntityRemoveEvent.Cause.DESPAWN);
                                this.callOnBrokenAfterFall(block, blockPos);
                            } else {
                                boolean canBeReplaced = blockState.canBeReplaced(new DirectionalPlaceContext(this.level(), blockPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                                boolean flag2 = FallingBlock.isFree(this.level().getBlockState(blockPos.below())) && (!flag || !flag1);
                                boolean flag3 = this.blockState.canSurvive(this.level(), blockPos) && !flag2;
                                if (canBeReplaced && flag3) {
                                    if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && this.level().getFluidState(blockPos).getType() == Fluids.WATER) {
                                        this.blockState = (BlockState)this.blockState.setValue(BlockStateProperties.WATERLOGGED, true);
                                    }

                                    if (!CraftEventFactory.callEntityChangeBlockEvent(this, blockPos, this.blockState)) {
                                        this.discard(EntityRemoveEvent.Cause.DESPAWN);
                                        return;
                                    }

                                    if (this.level().setBlock(blockPos, this.blockState, 3)) {
                                        serverLevel.getChunkSource().chunkMap.sendToTrackingPlayers(this, new ClientboundBlockUpdatePacket(blockPos, this.level().getBlockState(blockPos)));
                                        this.discard(EntityRemoveEvent.Cause.DESPAWN);
                                        if (block instanceof Fallable) {
                                            Fallable fallable = (Fallable)block;
                                            fallable.onLand(this.level(), blockPos, this.blockState, blockState, this);
                                        }

                                        if (this.blockData != null && this.blockState.hasBlockEntity()) {
                                            BlockEntity blockEntity = this.level().getBlockEntity(blockPos);
                                            if (blockEntity != null) {
                                                try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), LOGGER)) {
                                                    RegistryAccess registryAccess = this.level().registryAccess();
                                                    TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, registryAccess);
                                                    blockEntity.saveWithoutMetadata(tagValueOutput);
                                                    CompoundTag compoundTag = tagValueOutput.buildResult();
                                                    this.blockData.forEach((string, tag) -> compoundTag.put(string, tag.copy()));
                                                    blockEntity.loadWithComponents(TagValueInput.create(scopedCollector, registryAccess, compoundTag));
                                                } catch (Exception var19) {
                                                    LOGGER.error("Failed to load block entity from falling block", var19);
                                                }

                                                blockEntity.setChanged();
                                            }
                                        }
                                    } else if (this.dropItem && (Boolean)serverLevel.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.ENTITY_DROPS)) {
                                        this.discard(EntityRemoveEvent.Cause.DROP);
                                        this.callOnBrokenAfterFall(block, blockPos);
                                        this.spawnAtLocation(serverLevel, block);
                                    }
                                } else {
                                    this.discard(EntityRemoveEvent.Cause.DROP);
                                    if (this.dropItem && (Boolean)serverLevel.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.ENTITY_DROPS)) {
                                        this.callOnBrokenAfterFall(block, blockPos);
                                        this.spawnAtLocation(serverLevel, block);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        }

    }
}
