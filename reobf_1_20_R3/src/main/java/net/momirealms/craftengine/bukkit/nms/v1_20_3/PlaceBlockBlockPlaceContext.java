package net.momirealms.craftengine.bukkit.nms.v1_20_3;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class PlaceBlockBlockPlaceContext extends BlockPlaceContext {
    public PlaceBlockBlockPlaceContext(Level level, InteractionHand hand, ItemStack itemStack, BlockHitResult hitResult) {
        super(level, null, hand, itemStack, hitResult);
        this.replaceClicked = level.getBlockState(hitResult.getBlockPos()).canBeReplaced(this);
    }

    public static PlaceBlockBlockPlaceContext at(Level level, BlockPos blockPos, Direction direction, ItemStack itemStack) {
        return new PlaceBlockBlockPlaceContext(
                level,
                InteractionHand.MAIN_HAND,
                itemStack,
                new BlockHitResult(
                        new Vec3(
                                blockPos.getX() + 0.5 + direction.getStepX() * 0.5,
                                blockPos.getY() + 0.5 + direction.getStepY() * 0.5,
                                blockPos.getZ() + 0.5 + direction.getStepZ() * 0.5
                        ),
                        direction,
                        blockPos,
                        false
                )
        );
    }

    @Override
    public @NotNull Direction getNearestLookingDirection() {
        return this.getHitResult().getDirection();
    }

    @Override
    public @NotNull Direction getNearestLookingVerticalDirection() {
        return this.getHitResult().getDirection() == Direction.UP ? Direction.UP : Direction.DOWN;
    }

    @Override
    public Direction @NotNull [] getNearestLookingDirections() {
        Direction direction = this.getHitResult().getDirection();
        Direction[] directions = new Direction[]{direction, null, null, null, null, direction.getOpposite()};
        int i = 0;

        for (Direction direction1 : Direction.values()) {
            if (direction1 != direction && direction1 != direction.getOpposite()) {
                directions[++i] = direction;
            }
        }

        return directions;
    }
}

