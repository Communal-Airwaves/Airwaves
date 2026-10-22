package dev.radioheads.airwaves.content.block;

import com.mojang.serialization.MapCodec;
import dev.radioheads.airwaves.api.item.Tunable;
import dev.radioheads.airwaves.content.menu.TuningTableMenu;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Locale;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TuningTableBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<TuningTableBlock> CODEC = simpleCodec(TuningTableBlock::new);
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    private static final VoxelShape SHAPE = Block.box(0.0F, 0.0F, 0.0F, 16.0F, 14.0F, 16.0F);

    public TuningTableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.CENTER));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext c) {
        Direction f = c.getHorizontalDirection();
        BlockPos p = c.getClickedPos();
        Level l = c.getLevel();

        return l.getBlockState(p.relative(f.getClockWise())).canBeReplaced(c)
                && l.getBlockState(p.relative(f.getCounterClockWise())).canBeReplaced(c)
                ? defaultBlockState().setValue(FACING, f).setValue(PART, Part.CENTER)
                : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) {
            Direction f = state.getValue(FACING);
            level.setBlock(pos.relative(f.getCounterClockWise()), state.setValue(PART, Part.LEFT), Block.UPDATE_ALL);
            level.setBlock(pos.relative(f.getClockWise()), state.setValue(PART, Part.RIGHT), Block.UPDATE_ALL);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor level,
                                    BlockPos pos, BlockPos neighbourPos) {
        Direction f = state.getValue(FACING);
        Part part = state.getValue(PART);
        boolean neighbourIsCenter = neighbourState.is(this) && neighbourState.getValue(PART) == Part.CENTER;
        boolean neighbourIsSide = neighbourState.is(this) && neighbourState.getValue(PART) != Part.CENTER;

        if (part == Part.CENTER) {
            if ((direction == f.getClockWise() || direction == f.getCounterClockWise()) && !neighbourIsSide) {
                return Blocks.AIR.defaultBlockState();
            }
        } else {
            Direction toCenter = part == Part.LEFT ? f.getClockWise() : f.getCounterClockWise();
            if (direction == toCenter && !neighbourIsCenter) {
                return Blocks.AIR.defaultBlockState();
            }
        }

        return state;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        // The table consists of three blocks. Only the block a player breaks directly may drop an
        // item; the other two parts are torn down by the shape update, which removes them without
        // passing an entity to the loot context.
        if (params.getOptionalParameter(LootContextParams.THIS_ENTITY) == null) {
            return List.of();
        }

        return super.getDrops(state, params);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty() && !(player.getMainHandItem().getItem() instanceof Tunable)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            MenuProvider provider = new SimpleMenuProvider(
                    (id, inv, p) -> new TuningTableMenu(id, inv, ContainerLevelAccess.create(level, pos)),
                    Component.translatable("airwaves.container.tuning_table")
            );
            player.openMenu(provider);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public enum Part implements StringRepresentable {
        LEFT, CENTER, RIGHT;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
