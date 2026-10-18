package com.airwaves.client.ponder;

import com.airwaves.ModRegistry;
import com.airwaves.be.CableNode;
import com.airwaves.block.CircuitBoxBlock;
import com.airwaves.block.DimensionalRelayBlock;
import com.airwaves.block.JammerBlock;
import com.airwaves.block.OnAirSignBlock;
import com.airwaves.block.RadioBlock;
import com.airwaves.block.RadioConsoleBlock;
import com.airwaves.block.ReceiverBlock;
import com.airwaves.block.TransmitterBlock;
import com.airwaves.cable.CableType;
import com.airwaves.cable.Link;
import com.airwaves.logic.BoardDesign;
import com.airwaves.logic.BoardLayout;
import com.airwaves.structure.TowerCoreBE;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import java.util.List;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public final class AirwavesScenes {
   private static final String[] TUNER = new String[]{"........", "-kc--xc-", "........", "........", "........"};
   private static final String[] RADIO_BOARD = new String[]{"........", "-kc-xc-.", "..l..t--", "..r--r..", "........"};
   private static final String[] DISPLAY = new String[]{"........", ".l..l..l", "-r--r--r", "....t...", "........"};
   private static final String[] TIMERS = new String[]{"........", "-rc-rc--", "........", "........", "........"};
   private static final String[] INVERTER = new String[]{"........", "-tt-----", "........", "........", "........"};
   private static final String[] CLOCK = new String[]{"........", "-xc-----", "........", "........", "........"};
   private static final String[] LISTENER = new String[]{"........", "-kc--rl-", "........", "........", "........"};
   private static final String[] CHIPS = new String[]{"........", "-o-q-n--", "........", "........", "........"};

   private AirwavesScenes() {
   }

   private static void cable(CreateSceneBuilder scene, BlockPos a, BlockPos b, CableType t) {
      scene.world().modifyBlockEntity(a, BlockEntity.class, (be) -> {
         if (be instanceof CableNode n) {
            n.links().add(new Link(0, b, 0, t));
            n.cablesChanged();
         }

      });
      scene.world().modifyBlockEntity(b, BlockEntity.class, (be) -> {
         if (be instanceof CableNode n) {
            n.links().add(new Link(0, a, 0, t));
            n.cablesChanged();
         }

      });
   }

   private static void base(CreateSceneBuilder scene, SceneBuildingUtil util, int size) {
      scene.configureBasePlate(0, 0, size);
      scene.world().showSection(util.select().layer(0), Direction.UP);
      scene.idle(10);
   }

   public static void transmitter(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("transmitter_setup", "Building a Radio Station");
      base(scene, util, 7);
      BlockPos tx = util.grid().at(3, 1, 3);
      BlockPos copper = util.grid().at(3, 1, 2);
      scene.world().showSection(util.select().position(copper), Direction.DOWN);
      scene.idle(10);
      scene.overlay().showText(90).text("Transmitters need a block of Copper placed directly behind them. Any Copper block works, even weathered or waxed").pointAt(util.vector().topOf(copper)).placeNearTarget().attachKeyFrame();
      scene.idle(100);

      for(int y = 2; y <= 5; ++y) {
         scene.world().showSection(util.select().position(3, y, 2), Direction.DOWN);
         scene.idle(6);
      }

      scene.overlay().showText(90).text("An Antenna of the same size as the Transmitter has to stand on that Copper. It needs to be at least 4 layers tall").pointAt(util.vector().centerOf(3, 4, 2)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(90).text("The taller the Antenna, the further it reaches. A small Antenna 4 tall reaches about 600 blocks, 10 tall about 1700, and 20 tall about 3700").pointAt(util.vector().topOf(3, 5, 2)).placeNearTarget();
      scene.idle(100);
      scene.world().showSection(util.select().position(tx), Direction.DOWN);
      scene.idle(15);
      scene.overlay().showControls(util.vector().topOf(tx), Pointing.DOWN, 40).rightClick().withItem(new ItemStack(Items.AMETHYST_SHARD));
      scene.idle(10);
      scene.world().modifyBlock(tx, (s) -> (BlockState)s.setValue(TransmitterBlock.CRYSTAL, 1), false);
      scene.overlay().showText(90).text("Right-click the glass dome with Amethyst to fill the Crystal Chamber. An Amethyst Block gives a 10 percent cleaner signal than a shard").pointAt(util.vector().topOf(tx)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      Selection drive = util.select().fromTo(4, 1, 3, 6, 1, 3);
      scene.world().showSection(drive, Direction.WEST);
      scene.idle(10);
      scene.world().setKineticSpeed(drive.copy().add(util.select().position(tx)), 64.0F);
      scene.world().modifyBlock(tx, (s) -> (BlockState)s.setValue(TransmitterBlock.ACTIVE, true), false);
      scene.effects().indicateSuccess(tx);
      scene.overlay().showText(90).text("Transmitters are powered by Rotational Force. Connect a shaft to the left or right side. They need at least 16 RPM").pointAt(util.vector().blockSurface(tx, Direction.EAST)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(90).text("64 RPM gives full range, and 128 RPM gives 1.5 times the range. Stress per RPM: Small 4, Medium 8, Large 16, Giant 32").pointAt(util.vector().centerOf(5, 1, 3)).placeNearTarget();
      scene.idle(100);
      scene.overlay().showText(90).text("Once running, it broadcasts anyone talking within 2.5 blocks of it, or anything said into a cabled Microphone").pointAt(util.vector().blockSurface(tx, Direction.SOUTH)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(90).text("A Redstone signal puts it on standby. Engineer's Goggles show its range and anything it is missing").pointAt(util.vector().topOf(tx)).placeNearTarget();
      scene.idle(100);
      scene.markAsFinished();
   }

   public static void microphone(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("transmitter_microphone", "Microphones and Wires");
      base(scene, util, 7);
      BlockPos tx = util.grid().at(4, 1, 3);
      BlockPos mic = util.grid().at(1, 1, 3);
      scene.world().showSection(util.select().fromTo(4, 1, 2, 6, 6, 3), Direction.DOWN);
      scene.world().setKineticSpeed(util.select().fromTo(4, 1, 3, 6, 1, 3), 64.0F);
      scene.idle(15);
      scene.world().showSection(util.select().position(mic), Direction.DOWN);
      scene.idle(10);
      scene.overlay().showText(90).text("Microphones let you broadcast from further away, like from a studio or a stage").pointAt(util.vector().topOf(mic)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showControls(util.vector().centerOf(mic), Pointing.DOWN, 30).rightClick().withItem(new ItemStack((ItemLike)ModRegistry.COPPER_WIRE.get()));
      scene.idle(35);
      scene.overlay().showControls(util.vector().blockSurface(tx, Direction.SOUTH), Pointing.DOWN, 30).rightClick().withItem(new ItemStack((ItemLike)ModRegistry.COPPER_WIRE.get()));
      scene.idle(10);
      cable(scene, mic, tx, CableType.COPPER);
      scene.overlay().showText(90).text("Use a Wire on the Microphone, then on the jack of the Transmitter, to connect them").pointAt(util.vector().centerOf(2, 1, 3)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(90).text("Wires only carry sound. Copper reaches 16 blocks, Gold 24, Silver 32 and Fiber 64. Better Wires lose less quality").pointAt(util.vector().centerOf(3, 1, 3)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(90).text("Anyone within 3.5 blocks of the Microphone is now on air. A glowing pulse runs along the Wire while they talk").pointAt(util.vector().topOf(mic)).placeNearTarget();
      scene.idle(100);
      scene.overlay().showControls(util.vector().centerOf(mic), Pointing.DOWN, 30).rightClick().whileSneaking();
      scene.overlay().showText(90).text("Sneak-click a connector to take its cables down. You get the Wires back").pointAt(util.vector().topOf(mic)).placeNearTarget();
      scene.idle(100);
      scene.markAsFinished();
   }

   public static void receiver(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("receiver_setup", "Receivers, Speakers and the PA Horn");
      base(scene, util, 7);
      BlockPos rx = util.grid().at(2, 1, 3);
      BlockPos sp = util.grid().at(5, 1, 3);
      BlockPos horn = util.grid().at(5, 1, 5);
      BlockPos lamp = util.grid().at(1, 1, 3);
      scene.world().showSection(util.select().position(rx), Direction.DOWN);
      scene.idle(10);
      scene.overlay().showText(90).text("Receivers pick up one station and play it out loud").pointAt(util.vector().topOf(rx)).placeNearTarget().attachKeyFrame();
      scene.idle(100);

      for(int y = 2; y <= 4; ++y) {
         scene.world().showSection(util.select().position(2, y, 3), Direction.DOWN);
         scene.idle(6);
      }

      scene.overlay().showText(90).text("They need an Antenna of the same size, placed on top of them or on a Copper block behind them. Copper behind adds 25 percent more gain").pointAt(util.vector().centerOf(2, 3, 3)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showControls(util.vector().blockSurface(rx, Direction.SOUTH), Pointing.DOWN, 40).rightClick().withItem(new ItemStack(Items.AMETHYST_SHARD));
      scene.idle(10);
      scene.world().modifyBlock(rx, (s) -> (BlockState)((BlockState)s.setValue(ReceiverBlock.CRYSTAL, 1)).setValue(ReceiverBlock.ACTIVE, true), false);
      scene.overlay().showText(90).text("Fill the Crystal Chamber with Amethyst to switch it on. An Amethyst Block gives 15 percent more gain. Receivers do not need any Rotational Force").pointAt(util.vector().topOf(rx)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(90).text("Use an item tuned at the Tuning Table to set which station it plays").pointAt(util.vector().topOf(rx)).placeNearTarget();
      scene.idle(100);
      scene.world().showSection(util.select().position(sp), Direction.DOWN);
      scene.idle(10);
      cable(scene, rx, sp, CableType.COPPER);
      scene.overlay().showText(90).text("Speakers cabled to a Receiver play its sound. One Receiver can drive many Speakers").pointAt(util.vector().centerOf(sp)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.world().showSection(util.select().position(horn), Direction.DOWN);
      scene.idle(10);
      cable(scene, rx, horn, CableType.COPPER);
      scene.overlay().showText(90).text("PA Horns work just like Speakers, and are made for walls and poles outside").pointAt(util.vector().centerOf(horn)).placeNearTarget();
      scene.idle(100);
      scene.world().showSection(util.select().position(lamp), Direction.DOWN);
      scene.idle(10);
      scene.world().modifyBlock(lamp, (s) -> (BlockState)s.setValue(BlockStateProperties.LIT, true), false);
      scene.effects().indicateRedstone(lamp);
      scene.overlay().showText(90).text("Receivers also give out a Redstone signal from 0 to 15. The stronger the station, the stronger the signal").pointAt(util.vector().topOf(lamp)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.markAsFinished();
   }

   public static void antennas(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("antennas_stacking", "Building Antenna Masts");
      base(scene, util, 7);
      scene.world().showSection(util.select().position(1, 1, 3), Direction.DOWN);
      scene.idle(10);

      for(int y = 2; y <= 7; ++y) {
         scene.world().showSection(util.select().position(1, y, 3), Direction.DOWN);
         scene.idle(5);
      }

      scene.overlay().showText(90).text("Antennas are built upwards. Keep placing the same Antenna on top to make it taller").pointAt(util.vector().centerOf(1, 5, 3)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(90).text("The top layer always gets a cap with a beacon light").pointAt(util.vector().topOf(1, 7, 3)).placeNearTarget();
      scene.idle(100);
      scene.world().showSection(util.select().fromTo(4, 1, 2, 5, 3, 3), Direction.DOWN);
      scene.idle(15);
      scene.overlay().showText(90).text("Bigger Antennas are wider: Medium is 2x2, Big is 4x4 and Giant is 8x8. One item places a whole layer at once").pointAt(util.vector().topOf(5, 3, 3)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(90).text("Small Antennas grow up to 20 layers and Medium up to 40. Big ones reach 64 and Giant ones 96, getting narrower near the top").pointAt(util.vector().topOf(5, 3, 3)).placeNearTarget();
      scene.idle(100);
      scene.overlay().showText(90).text("Taller masts give more range and a cleaner signal. They can also be climbed like ladders").pointAt(util.vector().centerOf(1, 4, 3)).placeNearTarget();
      scene.idle(100);
      scene.markAsFinished();
   }

   public static void insulators(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("cables_insulators", "Running Cables over a Distance");
      base(scene, util, 9);
      BlockPos mic = util.grid().at(1, 1, 4);
      BlockPos ins = util.grid().at(4, 1, 4);
      BlockPos tx = util.grid().at(7, 1, 4);
      scene.world().showSection(util.select().position(mic).add(util.select().position(tx)), Direction.DOWN);
      scene.idle(15);
      scene.overlay().showText(90).text("Every type of Wire has a maximum length. If two blocks are too far apart, the cable cannot be placed").pointAt(util.vector().centerOf(4, 1, 4)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.world().showSection(util.select().position(ins), Direction.DOWN);
      scene.idle(10);
      cable(scene, mic, ins, CableType.GOLD);
      scene.idle(10);
      cable(scene, ins, tx, CableType.SILVER);
      scene.overlay().showText(90).text("Glass Insulators hold cables in between, so a line can go much further. Each one holds up to 4 cables, so a line can also split here").pointAt(util.vector().topOf(ins)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showBigLine(PonderPalette.OUTPUT, util.vector().centerOf(mic), util.vector().centerOf(tx), 80);
      scene.overlay().showText(90).text("Each block of Wire adds a little noise. Copper is the noisiest, and Fiber has no noise at all").pointAt(util.vector().centerOf(5, 1, 4)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(90).text("Any Airwaves cable joins any other at an insulator. With Power Grid installed they carry its electricity too").pointAt(util.vector().topOf(ins)).placeNearTarget();
      scene.idle(100);
      scene.markAsFinished();
   }

   public static void radioTower(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("radio_tower_drive", "Powering the Radio Tower");
      base(scene, util, 7);
      BlockPos core = util.grid().at(3, 1, 3);
      scene.world().showSection(util.select().fromTo(2, 1, 2, 5, 1, 5), Direction.DOWN);
      scene.idle(15);
      scene.overlay().showText(90).text("The Radio Tower is a complete broadcasting station, with a 27 block tall Antenna built in. It reaches about 38000 blocks. This is its base").pointAt(util.vector().topOf(core)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showOutline(PonderPalette.GREEN, "line", util.select().fromTo(3, 1, 2, 3, 1, 4), 100);
      scene.overlay().showText(90).text("A drive shaft runs from the cabinet in the middle out to both edges of the base").pointAt(util.vector().blockSurface(util.grid().at(3, 1, 2), Direction.NORTH)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      Selection drive = util.select().fromTo(3, 1, 0, 3, 1, 1);
      scene.world().showSection(drive, Direction.SOUTH);
      scene.idle(10);
      scene.world().setKineticSpeed(util.select().fromTo(3, 1, 0, 3, 1, 4), 64.0F);
      scene.overlay().showText(90).text("Connect Rotational Force to either end of the drive shaft. The Radio Tower needs 64 stress per RPM").pointAt(util.vector().centerOf(3, 1, 1)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showControls(util.vector().topOf(core), Pointing.DOWN, 40).rightClick().withItem(new ItemStack(Items.AMETHYST_BLOCK));
      scene.idle(10);
      scene.world().modifyBlock(core, (s) -> (BlockState)((BlockState)s.setValue(TowerCoreBE.CRYSTAL, 2)).setValue(TowerCoreBE.ACTIVE, true), false);
      scene.effects().indicateSuccess(core);
      scene.overlay().showText(90).text("Right-click near the cabinet with Amethyst to fill its Crystal Chamber. Once it has power and Amethyst, it goes on air").pointAt(util.vector().topOf(core)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(90).text("Cable a Microphone to the cabinet to broadcast. A Redstone signal on the cabinet puts it on standby").pointAt(util.vector().blockSurface(core, Direction.NORTH)).placeNearTarget();
      scene.idle(100);
      scene.markAsFinished();
   }

   public static void relayTower(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("relay_tower_drive", "Relay Towers");
      scene.configureBasePlate(0, 0, 7);
      scene.scaleSceneView(0.45F);
      scene.setSceneOffsetY(-3.0F);
      scene.world().showSection(util.select().layer(0), Direction.UP);
      scene.idle(10);
      BlockPos core = util.grid().at(3, 1, 3);
      scene.world().showSection(util.select().fromTo(0, 1, 2, 6, 1, 6), Direction.DOWN);
      scene.idle(10);
      scene.world().showSection(util.select().fromTo(0, 2, 0, 6, 20, 6), Direction.DOWN);
      scene.idle(15);
      scene.overlay().showText(90).text("Relay Towers pick up a station and send it out again from where they stand. The Relay Tower reaches about 5600 blocks, the Big Relay Tower about 14500").pointAt(util.vector().topOf(core)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.world().showSection(util.select().fromTo(3, 1, 0, 3, 1, 1), Direction.SOUTH);
      scene.idle(10);
      scene.world().setKineticSpeed(util.select().fromTo(3, 1, 0, 3, 1, 4), 64.0F);
      scene.overlay().showText(90).text("Like the Radio Tower, they are powered through the drive shaft in their base. The Relay Tower needs 12 stress per RPM, the Big Relay Tower 24").pointAt(util.vector().centerOf(3, 1, 1)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showControls(util.vector().topOf(core), Pointing.DOWN, 40).rightClick().withItem(new ItemStack((ItemLike)ModRegistry.RELAY_TOWER_SMALL_ITEM.get()));
      scene.overlay().showText(90).text("Use a tuned item on the Relay Tower to set which station it passes on").pointAt(util.vector().topOf(core)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(90).text("Relays can be chained up to 4 times. They only pass on stations heard with at least 20 percent quality. A Redstone signal switches them off").pointAt(util.vector().topOf(core)).placeNearTarget();
      scene.idle(100);
      scene.markAsFinished();
   }

   public static void jammer(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("jammer_basics", "The Signal Jammer");
      base(scene, util, 7);
      BlockPos j = util.grid().at(3, 1, 3);
      BlockPos lever = util.grid().at(3, 1, 4);
      BlockPos radio = util.grid().at(1, 1, 1);
      scene.world().showSection(util.select().position(radio), Direction.DOWN);
      scene.idle(10);
      scene.overlay().showText(90).text("This Radio is playing a station clearly").pointAt(util.vector().topOf(radio)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.world().showSection(util.select().position(j), Direction.DOWN);
      scene.idle(10);
      Selection drive = util.select().fromTo(4, 1, 3, 6, 1, 3);
      scene.world().showSection(drive, Direction.WEST);
      scene.world().setKineticSpeed(drive.copy().add(util.select().position(j)), 32.0F);
      scene.overlay().showText(90).text("Signal Jammers are powered by Rotational Force, just like Transmitters. They need 8 stress per RPM").pointAt(util.vector().centerOf(4, 1, 3)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.world().showSection(util.select().position(lever), Direction.DOWN);
      scene.idle(10);
      scene.world().toggleRedstonePower(util.select().position(lever));
      scene.world().modifyBlock(j, (s) -> (BlockState)s.setValue(JammerBlock.POWERED, true), false);
      scene.effects().indicateRedstone(lever);
      scene.overlay().showText(90).text("When given a Redstone signal, every station within 48 blocks turns into static. The closer, the worse").pointAt(util.vector().topOf(j)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(90).text("It also works as a barrier: any signal passing within 48 blocks on its way to a radio is jammed too. Only the Creative Receiver ignores Jammers").pointAt(util.vector().topOf(radio)).placeNearTarget();
      scene.idle(100);
      scene.markAsFinished();
   }

   public static void yagi(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("yagi_antenna", "Yagi Antennas: Radio with a Direction");
      base(scene, util, 7);
      BlockPos tx = util.grid().at(3, 1, 4);
      BlockPos yagi = util.grid().at(3, 6, 3);
      BlockPos front = util.grid().at(0, 1, 3);
      BlockPos back = util.grid().at(6, 1, 2);
      scene.world().showSection(util.select().fromTo(3, 1, 3, 3, 5, 3), Direction.DOWN);
      scene.idle(6);
      scene.world().showSection(util.select().fromTo(3, 1, 4, 6, 1, 4), Direction.DOWN);
      scene.world().setKineticSpeed(util.select().fromTo(3, 1, 4, 6, 1, 4), 64.0F);
      scene.idle(15);
      scene.overlay().showText(70).text("An ordinary antenna mast sends its station out the same way all round").pointAt(util.vector().topOf(3, 5, 3)).placeNearTarget().attachKeyFrame();
      scene.idle(80);
      scene.world().showSection(util.select().position(yagi), Direction.DOWN);
      scene.idle(10);
      scene.overlay().showText(80).text("Clamp a Yagi Antenna on top of the mast and it becomes a beam. The Yagi points the way you face when you place it").pointAt(util.vector().topOf(yagi)).placeNearTarget().attachKeyFrame();
      scene.idle(90);
      scene.world().showSection(util.select().position(front), Direction.DOWN);
      scene.world().showSection(util.select().position(back), Direction.DOWN);
      scene.idle(10);
      scene.world().modifyBlock(front, (st) -> (BlockState)st.setValue(RadioBlock.POWERED, true), false);
      scene.overlay().showBigLine(PonderPalette.GREEN, util.vector().centerOf(yagi), util.vector().topOf(front), 90);
      scene.effects().indicateSuccess(front);
      scene.overlay().showText(80).text("Straight ahead the station reaches 1.9 times as far as before").pointAt(util.vector().topOf(front)).placeNearTarget().attachKeyFrame();
      scene.idle(90);
      scene.overlay().showBigLine(PonderPalette.RED, util.vector().centerOf(yagi), util.vector().topOf(back), 90);
      scene.overlay().showText(80).text("To the sides it reaches about a third, and right behind barely an eighth: a real blind spot").pointAt(util.vector().topOf(back)).placeNearTarget();
      scene.idle(90);
      scene.overlay().showText(90).text("On a receiver's mast it listens the same way. Point two Yagis at each other for a long link that stations around you do not hear").pointAt(util.vector().topOf(yagi)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.markAsFinished();
   }

   private static ItemStack boardStack(String... rows) {
      BoardLayout l = new BoardLayout(String.join("", rows));
      List<BoardDesign> ds = l.analyse().designs();
      ItemStack st = new ItemStack((ds.isEmpty() ? BoardDesign.LOGIC : (BoardDesign)ds.get(0)).item());
      st.set((DataComponentType)ModRegistry.BOARD_LAYOUT.get(), l);
      return st;
   }

   private static void power(CreateSceneBuilder scene, Selection sel, boolean on) {
      scene.world().modifyBlocks(sel, (st) -> {
         if (st.hasProperty(BlockStateProperties.POWER)) {
            st = (BlockState)st.setValue(BlockStateProperties.POWER, on ? 15 : 0);
         }

         if (st.hasProperty(BlockStateProperties.POWERED)) {
            st = (BlockState)st.setValue(BlockStateProperties.POWERED, on);
         }

         if (st.hasProperty(BlockStateProperties.LIT)) {
            st = (BlockState)st.setValue(BlockStateProperties.LIT, on);
         }

         return st;
      }, false);
   }

   public static void workbench(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("workbench_boards", "Boardsmithing");
      base(scene, util, 5);
      BlockPos wb = util.grid().at(2, 1, 2);
      Vec3 top = util.vector().topOf(wb);
      scene.world().showSection(util.select().position(wb), Direction.DOWN);
      scene.idle(15);
      scene.overlay().showText(80).text("Every circuit board is soldered at the Circuit Workbench. There are no recipes: you design each circuit yourself").pointAt(top).placeNearTarget().attachKeyFrame();
      scene.idle(90);
      scene.overlay().showControls(top, Pointing.DOWN, 30).rightClick().withItem(new ItemStack((ItemLike)ModRegistry.CIRCUIT_BOARD.get()));
      scene.overlay().showText(70).text("Slot a Blank Circuit Board into the bench, then lay parts out on its 8 by 5 grid").pointAt(top).placeNearTarget().attachKeyFrame();
      scene.idle(80);
      BoardPanel.show(builder, top, 250, true, (String[])TUNER);
      scene.idle(8);
      scene.overlay().showText(60).text("Parts that touch work together. A coil beside a capacitor is a tuned tank, a crystal beside a capacitor an oscillator").pointAt(top).placeNearTarget();
      scene.idle(70);
      scene.overlay().showText(70).text("Then draw copper from the IN pads on the left to the OUT pads on the right. The moment it carries, the bench lists its stages").pointAt(top).placeNearTarget().attachKeyFrame();
      scene.idle(80);
      scene.overlay().showText(80).text("The stages decide every kind of board this circuit can become. Pick one of them, then take the finished board out").pointAt(top).placeNearTarget();
      scene.idle(110);
      BoardPanel.show(builder, top, 190, true, (String[])RADIO_BOARD);
      scene.idle(8);
      scene.overlay().showText(90).text("Add a transistor amplifier and an LED indicator, and the same tuner can now be a Radio Board too").pointAt(top).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(70).text("Stages can sit anywhere, as long as the copper joins them. Their order from IN to OUT matters later").pointAt(top).placeNearTarget();
      scene.idle(100);
      BoardPanel.show(builder, top, 150, true, (String[])DISPLAY);
      scene.idle(8);
      scene.overlay().showText(90).text("Three indicators make a Display Board. Any circuit that carries can at least be a Logic Board").pointAt(top).placeNearTarget().attachKeyFrame();
      scene.idle(150);
      scene.overlay().showControls(top, Pointing.DOWN, 50).withItem(boardStack(RADIO_BOARD));
      scene.overlay().showText(90).text("Every board keeps the circuit you soldered, and shows it on its icon. Two boards of the same kind can still behave differently").pointAt(top).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.markAsFinished();
   }

   public static void circuitBox(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("circuit_box", "Boards that Run on their Own");
      base(scene, util, 7);
      BlockPos lever = util.grid().at(1, 1, 3);
      BlockPos box = util.grid().at(3, 1, 3);
      BlockPos lamp = util.grid().at(5, 1, 3);
      Selection input = util.select().fromTo(1, 1, 3, 2, 1, 3);
      Selection output = util.select().fromTo(3, 1, 3, 5, 1, 3);
      Vec3 boxTop = util.vector().topOf(box);
      Vec3 panelAt = util.vector().topOf(lever);
      scene.world().showSection(util.select().position(box), Direction.DOWN);
      scene.idle(15);
      scene.overlay().showText(80).text("A Circuit Box runs any board on its own. Redstone goes in at its back, and the result comes out of its front").pointAt(boxTop).placeNearTarget().attachKeyFrame();
      scene.idle(60);
      scene.world().showSection(input, Direction.DOWN);
      scene.idle(6);
      scene.world().showSection(util.select().fromTo(4, 1, 3, 5, 1, 3), Direction.DOWN);
      scene.idle(40);
      BoardPanel.show(builder, panelAt, 190, false, (String[])TIMERS);
      scene.idle(10);
      scene.overlay().showControls(boxTop, Pointing.DOWN, 30).rightClick().withItem(boardStack(TIMERS));
      scene.world().modifyBlock(box, (st) -> (BlockState)st.setValue(CircuitBoxBlock.BOARD, true), false);
      scene.overlay().showText(70).text("The board inside is its program, read from IN to OUT. This one: redstone in, two timers, out").pointAt(boxTop).placeNearTarget().attachKeyFrame();
      scene.idle(80);
      power(scene, input, true);
      power(scene, output, true);
      scene.effects().indicateRedstone(lever);
      scene.idle(20);
      power(scene, input, false);
      scene.overlay().showText(60).text("Flick the lever back, and the timers hold the light on for two more seconds").pointAt(util.vector().topOf(lamp)).placeNearTarget();
      scene.idle(40);
      power(scene, output, false);
      scene.idle(50);
      BoardPanel.show(builder, panelAt, 130, false, (String[])INVERTER);
      scene.idle(10);
      scene.overlay().showControls(boxTop, Pointing.DOWN, 30).rightClick().withItem(boardStack(INVERTER));
      power(scene, output, true);
      scene.overlay().showText(70).text("Two transistors side by side are a logic gate, which inverts: the lamp is on until the lever is pulled").pointAt(util.vector().topOf(lamp)).placeNearTarget().attachKeyFrame();
      scene.idle(60);
      power(scene, input, true);
      power(scene, output, false);
      scene.effects().indicateRedstone(lever);
      scene.idle(30);
      power(scene, input, false);
      power(scene, output, true);
      scene.idle(40);
      BoardPanel.show(builder, panelAt, 130, false, (String[])CLOCK);
      scene.idle(10);
      scene.overlay().showControls(boxTop, Pointing.DOWN, 30).rightClick().withItem(boardStack(CLOCK));
      scene.overlay().showText(80).text("With a crystal oscillator first, the board is a clock: the box pulses on its own. Every extra crystal slows it down").pointAt(util.vector().topOf(lamp)).placeNearTarget().attachKeyFrame();

      for(int k = 0; k < 6; ++k) {
         power(scene, output, k % 2 == 1);
         scene.idle(10);
      }

      power(scene, output, false);
      scene.idle(50);
      BoardPanel.show(builder, panelAt, 170, false, (String[])LISTENER);
      scene.idle(10);
      scene.overlay().showControls(boxTop, Pointing.DOWN, 40).rightClick().withItem(new ItemStack((ItemLike)ModRegistry.FREQUENCY_CARD.get()));
      scene.overlay().showText(90).text("With a tuned tank first, the board listens. Tune the box like a receiver, and it gives a signal while that station is on the air").pointAt(boxTop).placeNearTarget().attachKeyFrame();
      scene.idle(70);
      power(scene, output, true);
      scene.effects().indicateSuccess(box);
      scene.idle(30);
      scene.overlay().showText(70).text("Indicators on the board light the lamp on the box itself, so you can see it working").pointAt(boxTop).placeNearTarget();
      scene.idle(80);
      power(scene, output, false);
      scene.idle(10);
      BoardPanel.show(builder, panelAt, 150, false, (String[])CHIPS);
      scene.idle(10);
      scene.overlay().showControls(boxTop, Pointing.DOWN, 30).rightClick().withItem(boardStack(CHIPS));
      scene.overlay().showText(90).text("Microchips and sensors need no partner. A motion sensor, a pulse chip and a counter chip: it counts everyone who walks past").pointAt(boxTop).placeNearTarget().attachKeyFrame();

      for(int k = 0; k < 3; ++k) {
         scene.idle(25);
         power(scene, output, true);
         scene.effects().indicateRedstone(box);
         scene.idle(6);
      }

      scene.idle(30);
      scene.overlay().showText(70).text("The output strength is the count so far, ready for a comparator or a lamp chain").pointAt(util.vector().topOf(lamp)).placeNearTarget();
      scene.idle(80);
      scene.markAsFinished();
   }

   public static void dimensionalRelay(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("dimensional_relay", "Radio Across Dimensions");
      scene.configureBasePlate(0, 0, 11);
      scene.scaleSceneView(0.75F);
      scene.world().showSection(util.select().fromTo(0, 0, 0, 4, 0, 4), Direction.UP);
      scene.idle(10);
      BlockPos tx = util.grid().at(1, 1, 2);
      BlockPos relayA = util.grid().at(3, 1, 3);
      BlockPos motorA = util.grid().at(4, 1, 3);
      BlockPos relayB = util.grid().at(7, 1, 3);
      BlockPos motorB = util.grid().at(6, 1, 3);
      BlockPos radio = util.grid().at(9, 1, 2);
      scene.world().showSection(util.select().fromTo(1, 1, 1, 1, 5, 2), Direction.DOWN);
      scene.idle(15);
      scene.overlay().showText(80).text("Radio waves never cross into another dimension on their own. A station in the Overworld cannot be heard in the Nether or the End").pointAt(util.vector().topOf(1, 5, 1)).placeNearTarget().attachKeyFrame();
      scene.idle(90);
      scene.world().showSection(util.select().position(relayA), Direction.DOWN);
      scene.idle(10);
      scene.overlay().showText(70).text("A Dimensional Relay carries them across. Build it where it can hear the stations you want to send").pointAt(util.vector().topOf(relayA)).placeNearTarget().attachKeyFrame();
      scene.idle(80);
      scene.world().showSection(util.select().position(motorA), Direction.WEST);
      scene.idle(10);
      scene.world().setKineticSpeed(util.select().fromTo(3, 1, 3, 4, 1, 3), 64.0F);
      scene.world().modifyBlock(relayA, (st) -> (BlockState)st.setValue(DimensionalRelayBlock.ACTIVE, true), false);
      scene.effects().indicateSuccess(relayA);
      scene.overlay().showText(80).text("Like every Airwaves machine it runs on Rotational Force, 16 stress per RPM, or on electricity through its built-in red + and blue - terminals").pointAt(util.vector().blockSurface(relayA, Direction.EAST)).placeNearTarget().attachKeyFrame();
      scene.idle(90);
      scene.overlay().showText(60).text("On its own it has no partner yet").pointAt(util.vector().topOf(relayA)).placeNearTarget();
      scene.idle(70);
      scene.world().showSection(util.select().fromTo(5, 1, 0, 5, 5, 3), Direction.DOWN);
      scene.idle(10);
      scene.world().showSection(util.select().fromTo(6, 0, 0, 10, 0, 4), Direction.UP);
      scene.idle(10);
      scene.world().showSection(util.select().position(relayB), Direction.DOWN);
      scene.idle(6);
      scene.world().showSection(util.select().position(motorB), Direction.EAST);
      scene.idle(10);
      scene.world().setKineticSpeed(util.select().fromTo(6, 1, 3, 7, 1, 3), -64.0F);
      scene.world().modifyBlock(relayB, (st) -> (BlockState)st.setValue(DimensionalRelayBlock.ACTIVE, true), false);
      scene.effects().indicateSuccess(relayB);
      scene.overlay().showText(80).text("Build a second Dimensional Relay in the Nether or the End, and power it the same way. The two link up on their own").pointAt(util.vector().topOf(relayB)).placeNearTarget().attachKeyFrame();
      scene.idle(90);
      Vec3 a = util.vector().topOf(relayA);
      Vec3 b = util.vector().topOf(relayB);
      scene.overlay().showBigLine(PonderPalette.BLUE, a.add((double)0.0F, 0.2, (double)0.0F), b.add((double)0.0F, 0.2, (double)0.0F), 100);
      scene.effects().emitParticles(util.vector().centerOf(5, 3, 2), scene.effects().simpleParticleEmitter(ParticleTypes.PORTAL, new Vec3((double)0.0F, 0.05, (double)0.0F)), 4.0F, 20);
      scene.overlay().showText(90).text("Now every station one relay hears is sent out again by the other, on its own frequency. It works both ways").pointAt(util.vector().centerOf(5, 3, 2)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.world().showSection(util.select().fromTo(9, 1, 1, 10, 1, 4), Direction.DOWN);
      scene.idle(10);
      scene.world().modifyBlock(radio, (st) -> (BlockState)st.setValue(RadioBlock.POWERED, true), false);
      scene.overlay().showText(80).text("A Radio in the Nether can now tune in to Overworld stations, and stations in the Nether reach the Overworld").pointAt(util.vector().topOf(radio)).placeNearTarget();
      scene.idle(90);
      scene.overlay().showText(80).text("A running relay keeps its own chunk loaded, so the link never drops. A Redstone signal switches it off").pointAt(util.vector().topOf(relayB)).placeNearTarget().attachKeyFrame();
      scene.idle(90);
      scene.overlay().showText(80).text("Engineer's Goggles show which dimensions a relay is linked to, and how many stations it sends and brings in").pointAt(util.vector().topOf(relayA)).placeNearTarget();
      scene.idle(90);
      scene.markAsFinished();
   }

   public static void radio(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("radio_basics", "Radios and the Tuning Table");
      scene.configureBasePlate(0, 0, 7);
      scene.world().showSection(util.select().layer(0), Direction.UP);
      scene.idle(10);
      BlockPos r = util.grid().at(1, 1, 2);
      BlockPos table = util.grid().at(5, 1, 2);
      scene.world().showSection(util.select().position(r), Direction.DOWN);
      scene.idle(15);
      scene.overlay().showControls(util.vector().topOf(r), Pointing.DOWN, 30).rightClick();
      scene.idle(10);
      scene.world().modifyBlock(r, (s) -> (BlockState)s.setValue(RadioBlock.POWERED, true), false);
      scene.overlay().showText(90).text("Right-click a Radio to switch it on or off").pointAt(util.vector().topOf(r)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showControls(util.vector().topOf(r), Pointing.DOWN, 30).rightClick().whileSneaking();
      scene.overlay().showText(90).text("Sneak-click it to jump to the next station it can hear").pointAt(util.vector().topOf(r)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(90).text("Radios stay quiet until someone talks. Distance and bad weather add static").pointAt(util.vector().topOf(r)).placeNearTarget();
      scene.idle(100);
      scene.world().showSection(util.select().fromTo(4, 1, 2, 6, 1, 2), Direction.DOWN);
      scene.idle(15);
      scene.overlay().showText(90).text("The Tuning Table sets the frequency of radio items. AM goes from 530 to 1700 kHz, FM from 87.5 to 108 MHz").pointAt(util.vector().topOf(table)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(90).text("It also shows which stations can be heard, and how far your own station reaches").pointAt(util.vector().topOf(table)).placeNearTarget();
      scene.idle(100);
      scene.overlay().showText(90).text("Only blocks on exactly the same frequency can hear each other. Rename an item in an Anvil to name your station").pointAt(util.vector().topOf(table)).placeNearTarget();
      scene.idle(100);
      scene.overlay().showText(90).text("The Portable Radio plays the station straight into your ears while it is in your hotbar. Sneak-click it to jump to the next station").pointAt(util.vector().topOf(r)).placeNearTarget();
      scene.idle(100);
      scene.markAsFinished();
   }

   public static void airship(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("airship_console", "The Radio Console and Airships");
      scene.configureBasePlate(0, 0, 13);
      scene.scaleSceneView(0.7F);
      scene.setSceneOffsetY(-1.0F);
      scene.world().showSection(util.select().layer(0), Direction.UP);
      scene.idle(10);
      ElementLink<WorldSectionElement> ship = scene.world().showIndependentSection(util.select().fromTo(0, 1, 0, 12, 9, 12), Direction.DOWN);
      scene.idle(20);
      BlockPos console = util.grid().at(4, 3, 6);
      BlockPos rx = util.grid().at(6, 3, 6);
      BlockPos horn = util.grid().at(8, 3, 6);
      BlockPos fan = util.grid().at(11, 2, 6);
      scene.world().setKineticSpeed(util.select().position(fan), 64.0F);
      scene.overlay().showText(90).text("All radio blocks work on Aeronautics airships, and use where the ship really is").pointAt(util.vector().centerOf(6, 7, 6)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.world().moveSection(ship, util.vector().of((double)0.0F, (double)0.5F, (double)0.0F), 30);
      scene.idle(30);
      scene.overlay().showText(90).text("Flying higher increases range. Transmitters and Relays get 50 percent more range for every 100 blocks above the ground").pointAt(util.vector().centerOf(6, 7, 6)).placeNearTarget();
      scene.idle(100);
      scene.overlay().showOutline(PonderPalette.GREEN, "console", util.select().position(console), 120);
      scene.overlay().showText(90).text("The Radio Console controls every radio block within 8 blocks of it. On a ship, that means the ship's own radio room").pointAt(util.vector().topOf(console).add((double)0.0F, (double)0.5F, (double)0.0F)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showControls(util.vector().topOf(console).add((double)0.0F, (double)0.5F, (double)0.0F), Pointing.DOWN, 40).rightClick();
      scene.idle(10);
      scene.world().modifyBlock(console, (s) -> (BlockState)s.setValue(RadioConsoleBlock.ACTIVE, true), false);
      scene.overlay().showText(90).text("Right-click it to seek the next station. Sneak-click it to see a report of everything it controls").pointAt(util.vector().topOf(console).add((double)0.0F, (double)0.5F, (double)0.0F)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(90).text("Here it controls the ship's Receiver, which plays through a PA Horn at the back").pointAt(util.vector().topOf(rx).add((double)0.0F, (double)0.5F, (double)0.0F)).placeNearTarget();
      scene.idle(100);
      scene.overlay().showText(90).text("A Redstone signal puts all of its Transmitters on standby. A Comparator can read the signal strength").pointAt(util.vector().topOf(console).add((double)0.0F, (double)0.5F, (double)0.0F)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.world().moveSection(ship, util.vector().of((double)0.0F, (double)-0.5F, (double)0.0F), 30);
      scene.idle(30);
      scene.overlay().showText(90).text("Cables can connect a ship to the ground, but they snap if pulled more than a quarter past their maximum length").pointAt(util.vector().centerOf(8, 3, 6)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(90).text("Radio blocks have airship weights. Big Transmitters are heavy, small blocks are light").pointAt(util.vector().centerOf(horn)).placeNearTarget();
      scene.idle(100);
      scene.markAsFinished();
   }

   public static void studio(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("studio_extras", "The On Air Sign");
      base(scene, util, 7);
      BlockPos sign = util.grid().at(3, 2, 4);
      BlockPos tx = util.grid().at(3, 1, 2);
      BlockPos mic = util.grid().at(1, 1, 3);
      BlockPos horn = util.grid().at(6, 3, 1);
      BlockPos rx = util.grid().at(5, 1, 2);
      scene.world().showSection(util.select().fromTo(1, 1, 5, 5, 2, 5), Direction.DOWN);
      scene.idle(10);
      scene.world().showSection(util.select().position(sign), Direction.SOUTH);
      scene.idle(10);
      scene.overlay().showText(90).text("On Air Signs are placed on walls, facing the way you click").pointAt(util.vector().blockSurface(sign, Direction.NORTH)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.world().showSection(util.select().position(tx).add(util.select().position(mic)), Direction.DOWN);
      scene.idle(10);
      cable(scene, mic, tx, CableType.COPPER);
      scene.overlay().showText(90).text("When a Transmitter within 12 blocks is sending a voice...").pointAt(util.vector().topOf(tx)).placeNearTarget().attachKeyFrame();
      scene.idle(70);
      scene.world().modifyBlock(tx, (s) -> (BlockState)s.setValue(TransmitterBlock.ON_AIR, true), false);
      scene.world().modifyBlock(sign, (s) -> (BlockState)s.setValue(OnAirSignBlock.LIT, true), false);
      scene.overlay().showText(90).text("...the sign lights up red, so everyone knows you are live").pointAt(util.vector().blockSurface(sign, Direction.NORTH)).placeNearTarget();
      scene.idle(100);
      scene.world().showSection(util.select().fromTo(5, 1, 1, 6, 3, 2), Direction.DOWN);
      scene.idle(10);
      cable(scene, rx, horn, CableType.COPPER);
      scene.overlay().showText(90).text("PA Horns can be mounted on posts and walls to play a Receiver's sound outside").pointAt(util.vector().centerOf(horn)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.markAsFinished();
   }

   public static void creative(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("creative_blocks", "Creative Radio Blocks");
      base(scene, util, 9);
      BlockPos ctx = util.grid().at(1, 1, 3);
      BlockPos cant = util.grid().at(4, 2, 2);
      BlockPos tx = util.grid().at(4, 1, 3);
      BlockPos crx = util.grid().at(7, 1, 3);
      BlockPos sp = util.grid().at(7, 1, 5);
      scene.overlay().showText(90).text("These creative blocks are useful for testing radio setups").independent(0).attachKeyFrame();
      scene.idle(100);
      scene.world().showSection(util.select().position(ctx), Direction.DOWN);
      scene.idle(10);
      scene.overlay().showText(90).text("The Creative Transmitter needs no Antenna, Amethyst or Rotational Force. It can be heard everywhere").pointAt(util.vector().topOf(ctx)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.world().showSection(util.select().fromTo(4, 1, 2, 6, 2, 3), Direction.DOWN);
      scene.world().setKineticSpeed(util.select().fromTo(4, 1, 3, 6, 1, 3), 64.0F);
      scene.idle(15);
      scene.overlay().showText(90).text("The Creative Antenna counts as a full Antenna of any size. Place it on the Copper behind a Transmitter or Receiver").pointAt(util.vector().centerOf(cant)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showControls(util.vector().blockSurface(cant, Direction.WEST), Pointing.LEFT, 50).rightClick();
      scene.overlay().showText(90).text("Hold right-click on the value box on its base to set its range, from 0 to 9999 blocks").pointAt(util.vector().blockSurface(cant, Direction.WEST)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(90).text("The Transmitter in front of it then reaches exactly that far").pointAt(util.vector().topOf(tx)).placeNearTarget();
      scene.idle(100);
      scene.world().showSection(util.select().position(crx).add(util.select().position(sp)), Direction.DOWN);
      scene.idle(10);
      cable(scene, crx, sp, CableType.COPPER);
      scene.overlay().showText(90).text("The Creative Receiver hears its station from anywhere, and Jammers do not affect it").pointAt(util.vector().topOf(crx)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.markAsFinished();
   }

   public static void tapePlayer(SceneBuilder builder, SceneBuildingUtil util) {
      CreateSceneBuilder scene = new CreateSceneBuilder(builder);
      scene.title("tape_player", "Recording and Broadcasting Tapes");
      base(scene, util, 9);
      BlockPos deck = util.grid().at(3, 1, 3);
      BlockPos tx = util.grid().at(6, 1, 3);
      BlockPos note = util.grid().at(2, 1, 1);
      BlockPos juke = util.grid().at(4, 1, 1);
      BlockPos lever = util.grid().at(0, 1, 3);
      BlockPos box = util.grid().at(1, 1, 3);
      scene.world().showSection(util.select().position(deck), Direction.DOWN);
      scene.idle(15);
      scene.overlay().showText(80).text("The Tape Player records sounds onto Tapes and plays them back").pointAt(util.vector().topOf(deck)).placeNearTarget().attachKeyFrame();
      scene.idle(90);
      scene.overlay().showControls(util.vector().blockSurface(deck, Direction.NORTH), Pointing.DOWN, 30).rightClick().withItem(new ItemStack((ItemLike)ModRegistry.TAPE_ITEM.get()));
      scene.idle(10);
      scene.overlay().showText(70).text("Right-click it with a Tape to load it, then right-click it empty-handed to open its screen").pointAt(util.vector().topOf(deck)).placeNearTarget();
      scene.idle(80);
      scene.world().showSection(util.select().position(note), Direction.DOWN);
      scene.idle(5);
      scene.world().showSection(util.select().position(juke), Direction.DOWN);
      scene.idle(10);
      scene.overlay().showText(100).text("Press Record. It has its own microphone: anything said within 6 blocks goes on the Tape, and so does every sound within 8, like note blocks, jukebox discs and mobs. A Tape holds up to 5 minutes").pointAt(util.vector().topOf(juke)).placeNearTarget().attachKeyFrame();

      for(int i = 0; i < 4; ++i) {
         scene.idle(18);
         scene.effects().emitParticles(util.vector().topOf(note), scene.effects().simpleParticleEmitter(ParticleTypes.NOTE, Vec3.ZERO), 1.0F, 1);
      }

      scene.overlay().showControls(util.vector().topOf(juke), Pointing.DOWN, 30).rightClick().withItem(new ItemStack(Items.MUSIC_DISC_CAT));
      scene.world().modifyBlock(juke, (st) -> (BlockState)st.setValue(BlockStateProperties.HAS_RECORD, true), false);
      scene.idle(40);
      scene.overlay().showText(90).text("Press Record again to stop. Play plays it back, and Loop repeats it forever. Rename a Tape in an Anvil to label it").pointAt(util.vector().topOf(deck)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.world().showSection(util.select().fromTo(6, 1, 2, 8, 5, 3), Direction.DOWN);
      scene.idle(15);
      cable(scene, deck, tx, CableType.COPPER);
      scene.overlay().showText(90).text("Wire it to a Transmitter like a Microphone, and whatever the Tape plays goes on air").pointAt(util.vector().centerOf(5, 1, 3)).placeNearTarget().attachKeyFrame();
      scene.idle(100);
      scene.overlay().showText(80).text("Without a Wire it just plays out loud, and any wired Microphone nearby picks it up").pointAt(util.vector().topOf(deck)).placeNearTarget();
      scene.idle(90);
      scene.world().showSection(util.select().fromTo(0, 1, 3, 2, 1, 3), Direction.EAST);
      scene.idle(15);
      Selection line = util.select().fromTo(0, 1, 3, 2, 1, 3);
      power(scene, line, true);
      scene.effects().indicateRedstone(util.grid().at(2, 1, 3));
      scene.overlay().showText(100).text("A Redstone pulse starts the Tape from the beginning. Put a timer board in a Circuit Box to play shows on a schedule").pointAt(util.vector().topOf(box)).placeNearTarget().attachKeyFrame();
      scene.idle(20);
      power(scene, line, false);
      scene.idle(90);
      scene.markAsFinished();
   }
}
