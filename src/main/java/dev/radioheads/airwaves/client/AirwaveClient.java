package dev.radioheads.airwaves.client;

import dev.radioheads.airwaves.Airwaves;
import dev.radioheads.airwaves.content.screen.TuningTableScreen;
import dev.radioheads.airwaves.registry.AirwaveMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Airwaves.MOD_ID, value = Dist.CLIENT)
public class AirwaveClient {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(AirwaveMenuTypes.TUNING_TABLE.get(), TuningTableScreen::new);
    }
}
