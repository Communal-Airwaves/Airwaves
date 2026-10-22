package dev.radioheads.airwaves;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import dev.radioheads.airwaves.registry.*;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(Airwaves.MOD_ID)
public class Airwaves {
    public static final String MOD_ID = "airwaves";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final AirwaveRegistrate REGISTRATE = AirwaveRegistrate.create(MOD_ID)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );

    public Airwaves(IEventBus modEventBus, ModContainer modContainer) {
        REGISTRATE.registerEventListeners(modEventBus);

        AirwaveItems.init();
        AirwaveBlocks.init();
        AirwaveBlockEntities.init();
        AirwaveRadioBands.init();
        AirwaveModulations.init();
        AirwaveTiers.init();
        AirwaveLang.init();
        AirwaveMenuTypes.MENU.register(modEventBus);

        AirwaveDataComponent.DATA_COMPONENTS.register(modEventBus);
    }

    @Contract("_ -> new")
    public static ResourceLocation asResource(String path) {
        return path.contains(":")
                ? ResourceLocation.tryParse(path)
                : ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

}
