package com.airwaves.item;

import com.airwaves.block.AntennaBlock;
import com.airwaves.block.MastBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MastItem extends BlockItem {
   protected final MastBlock mb;

   public MastItem(MastBlock b, Item.Properties p) {
      super(b, p);
      this.mb = b;
   }

   public InteractionResult place(BlockPlaceContext ctx) {
      Level lv;
      BlockPos pos;
      Player pl;
      MastBlock target;
      BlockPos below;
      BlockState belowSt;
      BlockPos origin;
      int seg;
      boolean narrowing;
      label113: {
         lv = ctx.getLevel();
         pos = ctx.getClickedPos();
         pl = ctx.getPlayer();
         target = this.mb;
         below = pos.below();
         belowSt = lv.getBlockState(below);
         seg = 0;
         narrowing = false;
         Block b = belowSt.getBlock();
         if (b instanceof AntennaBlock under) {
            if (this.mb instanceof AntennaBlock) {
               Object[] b = AntennaBlock.base(lv, below);
               if (b == null || b[0] != this.mb) {
                  return InteractionResult.FAIL;
               }

               int w = ((AntennaBlock)this.mb).widthAt((Integer)b[1]);
               if (w == 0) {
                  return InteractionResult.FAIL;
               }

               target = AntennaBlock.forWidth(w);
               BlockPos col = under.column(below, belowSt);
               origin = col.offset(-(w / 2), 1, -(w / 2));
               seg = target.nextSeg(lv.getBlockState(col));
               narrowing = w < under.size();
               break label113;
            }
         }

         b = belowSt.getBlock();
         if (b instanceof MastBlock other) {
            if (other == this.mb) {
               origin = this.mb.origin(below, belowSt).above();
               seg = this.mb.nextSeg(belowSt);
               break label113;
            }
         }

         int n = this.mb.size();
         origin = pos.offset(-((n - 1) / 2), 0, -((n - 1) / 2));
      }

      int n = target.size();

      for(int dx = 0; dx < n; ++dx) {
         for(int dz = 0; dz < n; ++dz) {
            BlockPos q = origin.offset(dx, 0, dz);
            if (!lv.isInWorldBounds(q) || !lv.getBlockState(q).canBeReplaced()) {
               return InteractionResult.FAIL;
            }
         }
      }

      if (!lv.isClientSide) {
         for(int dx = 0; dx < n; ++dx) {
            for(int dz = 0; dz < n; ++dz) {
               BlockPos q = origin.offset(dx, 0, dz);
               BlockState s = (BlockState)target.defaultBlockState().setValue(MastBlock.TOP, !(lv.getBlockState(q.above()).getBlock() instanceof MastBlock));
               if (n > 1) {
                  s = (BlockState)((BlockState)s.setValue(target.px(), dx)).setValue(target.pz(), dz);
               }

               if (s.hasProperty(MastBlock.SEG)) {
                  s = (BlockState)s.setValue(MastBlock.SEG, seg);
               }

               lv.setBlock(q, s, 3);
            }
         }

         if (narrowing) {
            Block var25 = belowSt.getBlock();
            BlockPos var10001;
            if (var25 instanceof MastBlock) {
               MastBlock wb = (MastBlock)var25;
               var10001 = wb.origin(below, belowSt);
            } else {
               var10001 = below;
            }

            MastBlock.setNarrow(lv, var10001, true);
         }

         lv.playSound((Player)null, pos, target.defaultBlockState().getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.8F);
         if (pl == null || !pl.getAbilities().instabuild) {
            ctx.getItemInHand().shrink(1);
         }
      }

      return InteractionResult.sidedSuccess(lv.isClientSide);
   }
}
