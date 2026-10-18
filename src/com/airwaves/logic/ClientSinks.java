package com.airwaves.logic;

import com.airwaves.be.AudioSink;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ClientSinks {
   public static final Set<AudioSink> SINKS = ConcurrentHashMap.newKeySet();
   public static Consumer<BlockPos> GONE = (p) -> {
   };

   public static void removed(AudioSink s, BlockEntity be) {
      SINKS.remove(s);
      if (be.getLevel() != null && be.getLevel().isClientSide) {
         GONE.accept(be.getBlockPos());
      }

   }

   private ClientSinks() {
   }
}
