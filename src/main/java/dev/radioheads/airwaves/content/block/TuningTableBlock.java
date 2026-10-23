package dev.radioheads.airwaves.content.block;

import com.mojang.serialization.MapCodec;
import dev.radioheads.airwaves.api.block.HorizontalMultiblockBlock;
import dev.radioheads.airwaves.api.item.Tunable;
import dev.radioheads.airwaves.content.menu.TuningTableMenu;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Locale;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TuningTableBlock extends HorizontalMultiblockBlock<TuningTableBlock.Part> {
    public static final MapCodec<TuningTableBlock> CODEC = simpleCodec(TuningTableBlock::new);
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    private static final VoxelShape SHAPE = Block.box(0.0F, 0.0F, 0.0F, 16.0F, 14.0F, 16.0F);

    private static final Map<Part, BlockPos> LAYOUT = Map.of(
            Part.LEFT, new BlockPos(-1, 0, 0),
            Part.CENTER, BlockPos.ZERO,
            Part.RIGHT, new BlockPos(1, 0, 0)
    );

    public TuningTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HorizontalMultiblockBlock<Part>> codec() {
        return CODEC;
    }

    @Override
    protected EnumProperty<Part> getPartProperty() {
        return PART;
    }

    @Override
    protected Part getOriginPart() {
        return Part.CENTER;
    }

    @Override
    protected Map<Part, BlockPos> getLayout() {
        return LAYOUT;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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
