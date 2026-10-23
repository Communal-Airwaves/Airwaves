package dev.radioheads.airwaves.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public abstract class MultiblockBlock<P extends Enum<P> & StringRepresentable> extends Block {
    protected MultiblockBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(getPartProperty(), getOriginPart()));
    }

    /**
     * The property storing which part of the structure this block is.
     * Must return a constant (e.g. a static field): it is called during construction.
     */
    protected abstract EnumProperty<P> getPartProperty();

    /**
     * The part that the block the player places is. Its layout offset must be {@link BlockPos#ZERO}.
     * Must return a constant: it is called during construction.
     */
    protected abstract P getOriginPart();

    /**
     * Offset of every part from the origin, in the unrotated frame.
     * The origin part maps to {@link BlockPos#ZERO}.
     */
    protected abstract Map<P, BlockPos> getLayout();

    /**
     * Converts a layout offset into world space for the given state.
     * Subclasses override this to apply rotation or other transforms.
     */
    protected BlockPos transformOffset(BlockPos offset, BlockState state) {
        return offset;
    }

    protected BlockState getPlacementState(BlockState state, BlockPlaceContext context) {
        return state;
    }

    protected BlockPos getOriginPos(BlockPos pos, BlockState state) {
        P part = state.getValue(getPartProperty());
        return pos.subtract(transformOffset(getLayout().get(part), state));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(getPartProperty());
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = getPlacementState(this.defaultBlockState(), context);
        BlockPos origin = context.getClickedPos();
        Level level = context.getLevel();

        for (Map.Entry<P, BlockPos> entry : getLayout().entrySet()) {
            if (entry.getKey() == getOriginPart()) {
                continue;
            }
            BlockPos target = origin.offset(transformOffset(entry.getValue(), state));
            if (!level.getBlockState(target).canBeReplaced(context)) {
                return null;
            }
        }

        return state.setValue(getPartProperty(), getOriginPart());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) {
            return;
        }

        for (Map.Entry<P, BlockPos> entry : getLayout().entrySet()) {
            if (entry.getKey() == getOriginPart()) {
                continue;
            }
            BlockPos target = pos.offset(transformOffset(entry.getValue(), state));
            level.setBlock(target, state.setValue(getPartProperty(), entry.getKey()), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);

        if (newState.is(this)) {
            return;
        }

        BlockPos origin = getOriginPos(pos, state);
        for (Map.Entry<P, BlockPos> entry : getLayout().entrySet()) {
            BlockPos partPos = origin.offset(transformOffset(entry.getValue(), state));
            if (partPos.equals(pos)) {
                continue;
            }

            BlockState other = level.getBlockState(partPos);
            if (other.is(this) && other.getValue(getPartProperty()) == entry.getKey()) {
                level.setBlock(partPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (params.getOptionalParameter(LootContextParams.THIS_ENTITY) == null) {
            return List.of();
        }
        return super.getDrops(state, params);
    }
}
