package com.airwaves.be;

import com.airwaves.ModRegistry;
import com.airwaves.item.TapeItem;
import com.airwaves.logic.RadioNetwork;
import com.airwaves.logic.SoundCapture;
import com.airwaves.logic.TapeAudio;
import com.airwaves.logic.TapeData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TapePlayerBE extends MicrophoneBE {
   public static final int IDLE = 0;
   public static final int PLAY = 1;
   public static final int REC = 2;
   public final SimpleContainer inv = new SimpleContainer(1) {
      public void setChanged() {
         super.setChanged();
         TapePlayerBE.this.onTapeChanged();
      }

      public int getMaxStackSize() {
         return 1;
      }
   };
   public int mode = 0;
   public int pos;
   public boolean loop;
   private boolean lastSignal;
   private List<TapeData.Cue> recording = new ArrayList();
   private int cueIdx;
   private List<TapeAudio.Frame> voice = new ArrayList();
   private List<TapeAudio.Frame> playing = List.of();
   private long recStart;
   private int voiceIdx;
   private final Map<ResourceLocation, Track> music = new LinkedHashMap();
   private boolean resume;
   public final ContainerData data = new ContainerData() {
      public int get(int i) {
         int var10000;
         switch (i) {
            case 0 -> var10000 = TapePlayerBE.this.mode;
            case 1 -> var10000 = TapePlayerBE.this.pos;
            case 2 -> var10000 = TapePlayerBE.this.mode == 2 ? 6000 : TapePlayerBE.this.tape().length();
            default -> var10000 = TapePlayerBE.this.loop ? 1 : 0;
         }

         return var10000;
      }

      public void set(int i, int v) {
      }

      public int getCount() {
         return 4;
      }
   };

   public TapePlayerBE(BlockPos p, BlockState s) {
      super((BlockEntityType)ModRegistry.TAPE_PLAYER_BE.get(), p, s);
   }

   public Vec3 anchor(int slot) {
      return CableNode.facing(this, (double)8.0F, (double)2.0F, (double)15.0F);
   }

   public ItemStack stack() {
      return this.inv.getItem(0);
   }

   public TapeData tape() {
      ItemStack s = this.stack();
      return s.getItem() instanceof TapeItem ? TapeItem.data(s) : TapeData.EMPTY;
   }

   public boolean isRecording() {
      return this.mode == 2;
   }

   public void button(int id) {
      switch (id) {
         case 0:
            if (this.mode == 1) {
               this.stop();
            } else {
               this.play();
            }
            break;
         case 1:
            if (this.mode == 2) {
               this.finishRecording();
            } else {
               this.record();
            }
            break;
         case 2:
            this.loop = !this.loop;
            this.setChanged();
            break;
         case 3:
            boolean was = this.mode == 1;
            this.stop();
            this.pos = 0;
            if (was) {
               this.play();
            }
      }

   }

   public void play() {
      if (this.level != null && !this.level.isClientSide && !this.tape().blank()) {
         if (this.mode == 2) {
            this.finishRecording();
         }

         if (this.pos >= this.tape().length()) {
            this.pos = 0;
         }

         this.mode = 1;
         this.seek();
         this.setChanged();
      }
   }

   private void seek() {
      this.resume = false;
      this.cueIdx = this.firstCueAt(this.pos);
      this.playing = this.level != null && this.level.getServer() != null ? TapeAudio.load(this.level.getServer(), this.tape().audio()) : List.of();

      for(this.voiceIdx = 0; this.voiceIdx < this.playing.size() && ((TapeAudio.Frame)this.playing.get(this.voiceIdx)).ms() < this.pos * 50; ++this.voiceIdx) {
      }

      this.music.clear();

      for(TapeData.Cue c : this.tape().cues()) {
         if (c.tick() >= this.pos) {
            break;
         }

         if (c.kind() == 1) {
            this.music.put(c.sound(), new Track(c.tick(), c.volume(), c.pitch()));
         } else if (c.kind() == 2) {
            this.music.remove(c.sound());
         }
      }

      Level var4 = this.level;
      if (var4 instanceof ServerLevel sl) {
         this.heartbeat(sl);
      }

   }

   public void stop() {
      if (this.mode == 2) {
         this.finishRecording();
      } else {
         this.mode = 0;
         this.stopMusic();
         this.setChanged();
      }
   }

   public void record() {
      if (this.level != null && !this.level.isClientSide && this.stack().getItem() instanceof TapeItem) {
         this.stopMusic();
         this.recording = new ArrayList();
         this.voice = new ArrayList();
         this.recStart = System.currentTimeMillis();
         this.pos = 0;
         this.mode = 2;
         this.setChanged();
      }
   }

   private void finishRecording() {
      ItemStack s = this.stack();
      if (s.getItem() instanceof TapeItem && this.pos > 0) {
         String audio = "";
         if (!this.voice.isEmpty() && this.level != null && this.level.getServer() != null) {
            audio = UUID.randomUUID().toString();
            TapeAudio.save(this.level.getServer(), audio, this.voice);
         }

         s.set((DataComponentType)ModRegistry.TAPE.get(), TapeData.of(this.pos, this.recording, audio, this.voice.size() / 50));
      }

      this.recording = new ArrayList();
      this.voice = new ArrayList();
      this.mode = 0;
      this.pos = 0;
      this.setChanged();
   }

   public void onRedstone(boolean powered) {
      if (powered && !this.lastSignal && !this.tape().blank()) {
         this.stop();
         this.pos = 0;
         this.play();
      }

      this.lastSignal = powered;
   }

   private void onTapeChanged() {
      this.setChanged();
      if (this.level != null && !this.level.isClientSide) {
         if (!(this.stack().getItem() instanceof TapeItem)) {
            if (this.mode == 2) {
               this.recording = new ArrayList();
               this.voice = new ArrayList();
               this.mode = 0;
            } else if (this.mode == 1) {
               this.stop();
            }

            this.pos = 0;
         }

      }
   }

   public void capture(ResourceLocation id, float vol, float pitch, byte kind) {
      if (this.mode == 2 && this.recording.size() < 4000) {
         this.recording.add(new TapeData.Cue(this.pos, id, vol, pitch, kind));
      }
   }

   public void captureVoice(byte[] frame) {
      if (this.mode == 2 && this.voice.size() < 15000) {
         int ms = (int)Math.min(System.currentTimeMillis() - this.recStart, (long)this.pos * 50L + 49L);
         if (!this.voice.isEmpty()) {
            ms = Math.max(ms, ((TapeAudio.Frame)this.voice.get(this.voice.size() - 1)).ms());
         }

         this.voice.add(new TapeAudio.Frame(ms, (byte[])(([B)frame).clone()));
      }
   }

   public UUID voiceId() {
      return UUID.nameUUIDFromBytes(("airwaves_tape:" + this.worldPosition.asLong()).getBytes());
   }

   public void serverTick() {
      Level var2 = this.level;
      if (var2 instanceof ServerLevel sl) {
         if (this.mode == 2) {
            if (++this.pos >= 6000) {
               this.finishRecording();
            }

         } else if (this.mode == 1) {
            TapeData t = this.tape();
            if (t.blank()) {
               this.stop();
            } else {
               if (this.resume) {
                  this.seek();
               }

               if (this.pos % 20 == 0) {
                  this.heartbeat(sl);
               }

               List<TapeData.Cue> cues = t.cues();

               while(this.cueIdx < cues.size() && ((TapeData.Cue)cues.get(this.cueIdx)).tick() <= this.pos) {
                  this.emit(sl, (TapeData.Cue)cues.get(this.cueIdx++));
               }

               int until = (this.pos + 1) * 50;

               while(this.voiceIdx < this.playing.size() && ((TapeAudio.Frame)this.playing.get(this.voiceIdx)).ms() < until) {
                  this.speak(sl, ((TapeAudio.Frame)this.playing.get(this.voiceIdx++)).data());
               }

               if (++this.pos >= t.length()) {
                  this.stopMusic();
                  if (this.loop) {
                     this.pos = 0;
                     this.cueIdx = 0;
                     this.voiceIdx = 0;
                  } else {
                     this.mode = 0;
                     this.pos = 0;
                  }

                  this.setChanged();
               }

            }
         }
      }
   }

   private int firstCueAt(int tick) {
      List<TapeData.Cue> cues = this.tape().cues();

      int i;
      for(i = 0; i < cues.size() && ((TapeData.Cue)cues.get(i)).tick() < tick; ++i) {
      }

      return i;
   }

   private boolean wired() {
      return this.feeds != null && this.feeds.active && !this.feeds.isRemoved();
   }

   private void emit(ServerLevel sl, TapeData.Cue c) {
      boolean on = this.wired();
      if (c.kind() == 2) {
         this.stopOne(sl, c.sound(), on);
      } else if (c.kind() == 1) {
         Track tr = new Track(c.tick(), c.volume(), c.pitch());
         this.music.put(c.sound(), tr);
         this.beat(sl, c.sound(), tr, on);
      } else {
         SoundCapture.suppress = on;

         try {
            sl.playSound((Player)null, (double)this.worldPosition.getX() + (double)0.5F, (double)this.worldPosition.getY() + (double)0.5F, (double)this.worldPosition.getZ() + (double)0.5F, SoundEvent.createVariableRangeEvent(c.sound()), SoundSource.RECORDS, c.volume(), c.pitch());
         } finally {
            SoundCapture.suppress = false;
         }

         if (on) {
            this.feeds.onAir(sl);
            RadioNetwork.broadcastSound(sl, this.feeds.getBlockPos().asLong(), c.sound(), c.volume() * this.line, c.pitch(), c.kind(), this.worldPosition.asLong());
         }

      }
   }

   private void heartbeat(ServerLevel sl) {
      boolean on = this.wired();

      for(Map.Entry<ResourceLocation, Track> e : this.music.entrySet()) {
         this.beat(sl, (ResourceLocation)e.getKey(), (Track)e.getValue(), on);
      }

   }

   private void beat(ServerLevel sl, ResourceLocation id, Track tr, boolean on) {
      int off = Math.max(0, this.pos - tr.start()) * 50;
      long key = this.worldPosition.asLong();
      RadioNetwork.sendSound(sl, this.worldPosition, id, 2.5F * tr.volume(), tr.pitch(), 1.0F, true, (byte)1, key, off);
      if (on) {
         this.feeds.onAir(sl);
         RadioNetwork.broadcastSound(sl, this.feeds.getBlockPos().asLong(), id, tr.volume() * this.line, tr.pitch(), (byte)1, key, off);
      }

   }

   private void speak(ServerLevel sl, byte[] frame) {
      UUID id = this.voiceId();
      RadioNetwork.send(sl, this.worldPosition, id, frame, 1.0F, true, 1.0F);
      if (this.wired()) {
         this.feeds.onAir(sl);
         RadioNetwork.broadcast(sl, this.feeds.getBlockPos().asLong(), id, frame, this.line);
      } else {
         RadioNetwork.voiceFromPoint(sl, this.worldPosition.getCenter(), id, frame);
      }

   }

   private void stopOne(ServerLevel sl, ResourceLocation id, boolean on) {
      this.music.remove(id);
      long key = this.worldPosition.asLong();
      RadioNetwork.sendSound(sl, this.worldPosition, id, 1.0F, 1.0F, 1.0F, true, (byte)2, key, 0);
      if (on) {
         RadioNetwork.broadcastSound(sl, this.feeds.getBlockPos().asLong(), id, 1.0F, 1.0F, (byte)2, key);
      }

   }

   private void stopMusic() {
      Level var2 = this.level;
      if (var2 instanceof ServerLevel sl) {
         if (!this.music.isEmpty()) {
            boolean on = this.wired();

            for(ResourceLocation id : new ArrayList(this.music.keySet())) {
               this.stopOne(sl, id, on);
            }

            return;
         }
      }

      this.music.clear();
   }

   public void setRemoved() {
      this.stopMusic();
      super.setRemoved();
   }

   protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
      super.saveAdditional(tag, r);
      ContainerHelper.saveAllItems(tag, this.inv.getItems(), r);
      tag.putBoolean("loop", this.loop);
      tag.putBoolean("signal", this.lastSignal);
      tag.putInt("mode", this.mode == 1 ? 1 : 0);
      tag.putInt("pos", this.mode == 1 ? this.pos : 0);
   }

   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
      super.loadAdditional(tag, r);
      this.inv.getItems().set(0, ItemStack.EMPTY);
      ContainerHelper.loadAllItems(tag, this.inv.getItems(), r);
      this.loop = tag.getBoolean("loop");
      this.lastSignal = tag.getBoolean("signal");
      this.mode = tag.getInt("mode") == 1 ? 1 : 0;
      this.pos = tag.getInt("pos");
      this.resume = this.mode == 1;
   }

   private static record Track(int start, float volume, float pitch) {
   }
}
