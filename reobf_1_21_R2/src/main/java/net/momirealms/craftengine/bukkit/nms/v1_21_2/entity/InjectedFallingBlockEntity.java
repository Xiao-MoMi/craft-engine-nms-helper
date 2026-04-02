package net.momirealms.craftengine.bukkit.nms.v1_21_2.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
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
        for (Item ceitem : customState.getDrops(builder, world, null)) {
            world.dropItemNaturally(position, ceitem);
        }
        return null;
    }

    @SuppressWarnings({"all", "removal"})
    @Override
    public void tick() {
        if (this.blockState.isAir()) {
            this.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.DESPAWN);
        } else {
            Block block = this.blockState.getBlock();
            ++this.time;
            this.applyGravity();
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.applyEffectsFromBlocks();
            if (this.level().paperConfig().fixes.fallingBlockHeightNerf.test((v) -> this.getY() > (double)v)) {
                if (this.dropItem) {
                    Level var19 = this.level();
                    if (var19 instanceof ServerLevel) {
                        ServerLevel serverLevel = (ServerLevel)var19;
                        if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                            this.spawnAtLocation(serverLevel, block);
                        }
                    }
                }

                this.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.OUT_OF_WORLD);
                return;
            }

            this.handlePortal();
            Level world = this.level();
            if (world instanceof ServerLevel) {
                ServerLevel worldserver = (ServerLevel)world;
                if (this.isAlive() || this.forceTickAfterTeleportToDuplicate) {
                    BlockPos blockposition = this.blockPosition();
                    ConcretePowderBlockBehavior behavior = BlockStateUtils.getOptionalCustomBlockState(this.getBlockState())
                            .map(ImmutableBlockState::behavior)
                            .map(it -> it.getFirst(ConcretePowderBlockBehavior.class))
                            .orElse(null);
                    boolean flag = this.blockState.getBlock() instanceof ConcretePowderBlock || behavior != null;
                    boolean flag1 = flag && this.level().getFluidState(blockposition).is(FluidTags.WATER);
                    double d0 = this.getDeltaMovement().lengthSqr();
                    if (flag && d0 > (double)1.0F) {
                        BlockHitResult movingobjectpositionblock = this.level().clip(new ClipContext(new Vec3(super.xo, super.yo, super.zo), this.position(), net.minecraft.world.level.ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this));
                        if (movingobjectpositionblock.getType() != HitResult.Type.MISS && this.level().getFluidState(movingobjectpositionblock.getBlockPos()).is(FluidTags.WATER)) {
                            blockposition = movingobjectpositionblock.getBlockPos();
                            flag1 = true;
                        }
                    }

                    if (!this.onGround() && !flag1) {
                        if (this.time > 100 && this.autoExpire && (blockposition.getY() <= this.level().getMinY() || blockposition.getY() > this.level().getMaxY()) || this.time > 600 && this.autoExpire) {
                            if (this.dropItem && worldserver.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                this.spawnAtLocation(worldserver, block);
                            }

                            this.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.DROP);
                        }
                    } else {
                        BlockState iblockdata = this.level().getBlockState(blockposition);
                        this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, (double)-0.5F, 0.7));
                        if (!iblockdata.is(Blocks.MOVING_PISTON)) {
                            if (this.cancelDrop) {
                                this.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.DESPAWN);
                                this.callOnBrokenAfterFall(block, blockposition);
                            } else {
                                boolean flag2 = iblockdata.canBeReplaced(new DirectionalPlaceContext(this.level(), blockposition, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                                boolean flag3 = FallingBlock.isFree(this.level().getBlockState(blockposition.below())) && (!flag || !flag1);
                                boolean flag4 = this.blockState.canSurvive(this.level(), blockposition) && !flag3;
                                if (flag2 && flag4) {
                                    if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && this.level().getFluidState(blockposition).getType() == Fluids.WATER) {
                                        this.blockState = (BlockState)this.blockState.setValue(BlockStateProperties.WATERLOGGED, true);
                                    }

                                    if (!CraftEventFactory.callEntityChangeBlockEvent(this, blockposition, this.blockState)) {
                                        this.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.DESPAWN);
                                        return;
                                    }

                                    if (!this.level().setBlock(blockposition, this.blockState, 3)) {
                                        if (this.dropItem && worldserver.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                            this.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.DROP);
                                            this.callOnBrokenAfterFall(block, blockposition);
                                            this.spawnAtLocation(worldserver, block);
                                        }
                                    } else {
                                        ((ServerLevel)this.level()).getChunkSource().chunkMap.broadcast(this, new ClientboundBlockUpdatePacket(blockposition, this.level().getBlockState(blockposition)));
                                        this.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.DESPAWN);
                                        if (block instanceof Fallable) {
                                            ((Fallable)block).onLand(this.level(), blockposition, this.blockState, iblockdata, this);
                                        }

                                        if (this.blockData != null && this.blockState.hasBlockEntity()) {
                                            BlockEntity tileentity = this.level().getBlockEntity(blockposition);
                                            if (tileentity != null) {
                                                CompoundTag nbttagcompound = tileentity.saveWithoutMetadata(this.level().registryAccess());

                                                for(String s : this.blockData.getAllKeys()) {
                                                    nbttagcompound.put(s, this.blockData.get(s).copy());
                                                }

                                                try {
                                                    tileentity.loadWithComponents(nbttagcompound, this.level().registryAccess());
                                                } catch (Exception exception) {
                                                    LOGGER.error("Failed to load block entity from falling block", exception);
                                                }

                                                tileentity.setChanged();
                                            }
                                        }
                                    }
                                } else {
                                    this.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.DROP);
                                    if (this.dropItem && worldserver.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                        this.callOnBrokenAfterFall(block, blockposition);
                                        this.spawnAtLocation(worldserver, block);
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
