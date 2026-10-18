package com.airwaves.client.ponder;

import com.airwaves.ModRegistry;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class AirwavesPonderPlugin implements PonderPlugin {
   public static final ResourceLocation TAG = ResourceLocation.fromNamespaceAndPath("airwaves", "radio");

   public String getModId() {
      return "airwaves";
   }

   private static ResourceLocation id(DeferredHolder<?, ?> h) {
      return h.getId();
   }

   public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> h) {
      ResourceLocation[] tx = new ResourceLocation[]{id((DeferredHolder)ModRegistry.TRANSMITTER_ITEMS.get(0))};
      ResourceLocation[] rx = new ResourceLocation[]{id((DeferredHolder)ModRegistry.RECEIVER_ITEMS.get(0))};
      h.forComponents(tx).addStoryBoard("transmitter/setup", AirwavesScenes::transmitter, new ResourceLocation[]{TAG}).addStoryBoard("transmitter/microphone", AirwavesScenes::microphone, new ResourceLocation[]{TAG});
      h.forComponents(new ResourceLocation[]{id(ModRegistry.MICROPHONE_ITEM)}).addStoryBoard("transmitter/microphone", AirwavesScenes::microphone, new ResourceLocation[]{TAG});
      h.forComponents(rx).addStoryBoard("receiver/setup", AirwavesScenes::receiver, new ResourceLocation[]{TAG});
      h.forComponents(new ResourceLocation[]{id(ModRegistry.SPEAKER_ITEM)}).addStoryBoard("receiver/setup", AirwavesScenes::receiver, new ResourceLocation[]{TAG});
      h.forComponents(new ResourceLocation[]{id(ModRegistry.ANTENNA_ITEM)}).addStoryBoard("antennas/stacking", AirwavesScenes::antennas, new ResourceLocation[]{TAG}).addStoryBoard("antennas/yagi", AirwavesScenes::yagi, new ResourceLocation[]{TAG}).addStoryBoard("transmitter/setup", AirwavesScenes::transmitter, new ResourceLocation[]{TAG});
      h.forComponents(new ResourceLocation[]{id(ModRegistry.COPPER_WIRE)}).addStoryBoard("transmitter/microphone", AirwavesScenes::microphone, new ResourceLocation[]{TAG}).addStoryBoard("cables/insulators", AirwavesScenes::insulators, new ResourceLocation[]{TAG});
      h.forComponents(new ResourceLocation[]{id(ModRegistry.INSULATOR_ITEM)}).addStoryBoard("cables/insulators", AirwavesScenes::insulators, new ResourceLocation[]{TAG});
      h.forComponents(new ResourceLocation[]{id(ModRegistry.RADIO_TOWER_ITEM)}).addStoryBoard("towers/radio_tower", AirwavesScenes::radioTower, new ResourceLocation[]{TAG});
      h.forComponents(new ResourceLocation[]{id(ModRegistry.RELAY_TOWER_SMALL_ITEM), id(ModRegistry.RELAY_TOWER_BIG_ITEM)}).addStoryBoard("towers/relay", AirwavesScenes::relayTower, new ResourceLocation[]{TAG});
      h.forComponents(new ResourceLocation[]{id(ModRegistry.JAMMER_ITEM)}).addStoryBoard("jammer/basics", AirwavesScenes::jammer, new ResourceLocation[]{TAG});
      h.forComponents(new ResourceLocation[]{id(ModRegistry.CIRCUIT_WORKBENCH_ITEM), id(ModRegistry.CIRCUIT_BOARD), id(ModRegistry.RESISTOR), id(ModRegistry.CAPACITOR), id(ModRegistry.LED), id(ModRegistry.TRANSISTOR), id(ModRegistry.COPPER_COIL), id(ModRegistry.CRYSTAL_OSCILLATOR)}).addStoryBoard("workbench/boards", AirwavesScenes::workbench, new ResourceLocation[]{TAG}).addStoryBoard("workbench/circuit_box", AirwavesScenes::circuitBox, new ResourceLocation[]{TAG});

      for(DeferredItem<Item> b : ModRegistry.BOARDS.values()) {
         h.forComponents(new ResourceLocation[]{id(b)}).addStoryBoard("workbench/boards", AirwavesScenes::workbench, new ResourceLocation[]{TAG}).addStoryBoard("workbench/circuit_box", AirwavesScenes::circuitBox, new ResourceLocation[]{TAG});
      }

      h.forComponents(new ResourceLocation[]{id(ModRegistry.CIRCUIT_BOX_ITEM)}).addStoryBoard("workbench/circuit_box", AirwavesScenes::circuitBox, new ResourceLocation[]{TAG}).addStoryBoard("workbench/boards", AirwavesScenes::workbench, new ResourceLocation[]{TAG});
      h.forComponents(new ResourceLocation[]{id(ModRegistry.TAPE_PLAYER_ITEM), id(ModRegistry.TAPE_ITEM)}).addStoryBoard("tape/player", AirwavesScenes::tapePlayer, new ResourceLocation[]{TAG});
      h.forComponents(new ResourceLocation[]{id(ModRegistry.YAGI_ANTENNA_ITEM)}).addStoryBoard("antennas/yagi", AirwavesScenes::yagi, new ResourceLocation[]{TAG}).addStoryBoard("antennas/stacking", AirwavesScenes::antennas, new ResourceLocation[]{TAG});
      h.forComponents(new ResourceLocation[]{id(ModRegistry.DIMENSIONAL_RELAY_ITEM)}).addStoryBoard("dimensional/relay", AirwavesScenes::dimensionalRelay, new ResourceLocation[]{TAG});
      h.forComponents(new ResourceLocation[]{id(ModRegistry.RADIO_ITEM), id(ModRegistry.PORTABLE_RADIO), id(ModRegistry.TUNING_TABLE_ITEM), id(ModRegistry.FREQUENCY_CARD)}).addStoryBoard("radio/basics", AirwavesScenes::radio, new ResourceLocation[]{TAG});
      h.forComponents(new ResourceLocation[]{id(ModRegistry.RADIO_CONSOLE_ITEM)}).addStoryBoard("airship/console", AirwavesScenes::airship, new ResourceLocation[]{TAG});
      h.forComponents(new ResourceLocation[]{id(ModRegistry.CREATIVE_TRANSMITTER_ITEM), id(ModRegistry.CREATIVE_RECEIVER_ITEM), id(ModRegistry.CREATIVE_ANTENNA_ITEM)}).addStoryBoard("creative/blocks", AirwavesScenes::creative, new ResourceLocation[]{TAG});
   }

   public void registerTags(PonderTagRegistrationHelper<ResourceLocation> h) {
      h.registerTag(TAG).addToIndex().item((ItemLike)ModRegistry.RADIO_ITEM.get(), true, false).title("Airwaves Radio").description("Transmitters, receivers and everything that carries a voice over the air").register();
      h.addToTag(TAG).add(id(ModRegistry.RADIO_ITEM)).add(id(ModRegistry.MICROPHONE_ITEM)).add(id(ModRegistry.TAPE_PLAYER_ITEM)).add(id(ModRegistry.SPEAKER_ITEM)).add(id((DeferredHolder)ModRegistry.TRANSMITTER_ITEMS.get(0))).add(id((DeferredHolder)ModRegistry.RECEIVER_ITEMS.get(0))).add(id(ModRegistry.ANTENNA_ITEM)).add(id(ModRegistry.YAGI_ANTENNA_ITEM)).add(id(ModRegistry.JAMMER_ITEM)).add(id(ModRegistry.INSULATOR_ITEM)).add(id(ModRegistry.CIRCUIT_WORKBENCH_ITEM)).add(id(ModRegistry.CIRCUIT_BOX_ITEM)).add(id(ModRegistry.DIMENSIONAL_RELAY_ITEM)).add(id(ModRegistry.RADIO_TOWER_ITEM)).add(id(ModRegistry.RELAY_TOWER_SMALL_ITEM)).add(id(ModRegistry.RADIO_CONSOLE_ITEM)).add(id(ModRegistry.TUNING_TABLE_ITEM)).add(id(ModRegistry.CREATIVE_TRANSMITTER_ITEM)).add(id(ModRegistry.CREATIVE_RECEIVER_ITEM)).add(id(ModRegistry.CREATIVE_ANTENNA_ITEM));
   }
}
