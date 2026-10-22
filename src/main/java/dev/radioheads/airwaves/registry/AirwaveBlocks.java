package dev.radioheads.airwaves.registry;

import com.tterrag.registrate.util.entry.BlockEntry;
import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.block.TuningTableBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

public final class AirwaveBlocks {
    private AirwaveBlocks() {}

    public static final BlockEntry<TuningTableBlock> TUNING_TABLE =
            Airwaves.REGISTRATE.block("tuning_table", TuningTableBlock::new)
                    .properties(p -> p
                            .strength(2.5f)
                            .mapColor(MapColor.WOOD)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                    )
                    .blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.getEntry()).forAllStates(state -> {
                        TuningTableBlock.Part part = state.getValue(TuningTableBlock.PART);
                        Direction facing = state.getValue(TuningTableBlock.FACING);

                        return new ConfiguredModel[] {
                                new ConfiguredModel(
                                        prov.models().getExistingFile(prov.modLoc("block/tuning_table_" + part.getSerializedName())),
                                        0,
                                        facing.get2DDataValue() * 90,
                                        false
                                )
                        };
                    }))
                    .item()
                    .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/tuning_table_center")))
                    .build()
                    .register();

    public static void init() {}
}
