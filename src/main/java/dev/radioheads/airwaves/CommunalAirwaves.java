package dev.radioheads.airwaves;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(CommunalAirwaves.MODID)
public class CommunalAirwaves {
    public static final String MODID = "airwaves";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CommunalAirwaves(IEventBus modEventBus, ModContainer modContainer) {
    }
}
