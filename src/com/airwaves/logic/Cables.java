package com.airwaves.logic;

import com.airwaves.ModRegistry;
import com.airwaves.ModSounds;
import com.airwaves.be.CableNode;
import com.airwaves.be.MicrophoneBE;
import com.airwaves.be.ReceiverBE;
import com.airwaves.be.SpeakerBE;
import com.airwaves.be.TransmitterBE;
import com.airwaves.cable.CableType;
import com.airwaves.cable.Link;
import com.airwaves.cable.NodeRef;
import com.airwaves.compat.SableCompat;
import com.airwaves.item.CableItem;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class Cables {
   public static final int MAX_NODES = 512;

   private Cables() {
   }

   public static CableNode node(Level lv, BlockPos p) {
      CableNode var10000;
      if (lv.isLoaded(p)) {
         BlockEntity var3 = lv.getBlockEntity(p);
         if (var3 instanceof CableNode) {
            CableNode n = (CableNode)var3;
            var10000 = n;
            return var10000;
         }
      }

      var10000 = null;
      return var10000;
   }

   public static Item item(CableType t) {
      CableItem var10000;
      switch (t) {
         case GOLD -> var10000 = (CableItem)ModRegistry.GOLD_WIRE.get();
         case SILVER -> var10000 = (CableItem)ModRegistry.SILVER_WIRE.get();
         case FIBER -> var10000 = (CableItem)ModRegistry.FIBER_CABLE.get();
         default -> var10000 = (CableItem)ModRegistry.COPPER_WIRE.get();
      }

      return var10000;
   }

   public static boolean compatible(CableNode a, int sa, CableNode b, int sb) {
      if (!a.junction(sa) && !b.junction(sb)) {
         return a instanceof MicrophoneBE && b instanceof TransmitterBE || b instanceof MicrophoneBE && a instanceof TransmitterBE || a instanceof ReceiverBE && b instanceof SpeakerBE || b instanceof ReceiverBE && a instanceof SpeakerBE;
      } else {
         return true;
      }
   }

   public static boolean canLink(Level lv, NodeRef a, NodeRef b, CableType t) {
      if (a.pos().equals(b.pos())) {
         return false;
      } else {
         CableNode na = node(lv, a.pos());
         CableNode nb = node(lv, b.pos());
         if (na != null && nb != null) {
            if (a.slot() < na.connectors() && b.slot() < nb.connectors()) {
               if (SableCompat.distance(lv, na.anchor(a.slot()), nb.anchor(b.slot())) > (double)t.maxLen + (double)0.5F) {
                  return false;
               } else if (na.accepts(a.slot(), t) && nb.accepts(b.slot(), t)) {
                  if (na.links().has(a.slot(), b.pos(), b.slot())) {
                     return false;
                  } else {
                     return na.links().count(a.slot()) < na.maxLinks(a.slot()) && nb.links().count(b.slot()) < nb.maxLinks(b.slot()) ? compatible(na, a.slot(), nb, b.slot()) : false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   public static boolean link(Level lv, NodeRef a, NodeRef b, CableType t) {
      if (!canLink(lv, a, b, t)) {
         return false;
      } else {
         CableNode na = node(lv, a.pos());
         CableNode nb = node(lv, b.pos());
         na.links().add(new Link(a.slot(), b.pos(), b.slot(), t));
         nb.links().add(new Link(b.slot(), a.pos(), a.slot(), t));
         na.cablesChanged();
         nb.cablesChanged();
         return true;
      }
   }

   public static List<ItemStack> unlink(Level lv, BlockPos p, int slot) {
      CableNode n = node(lv, p);
      List<ItemStack> out = new ArrayList();
      if (n == null) {
         return out;
      } else {
         for(Link l : new ArrayList(n.links().list())) {
            if (slot < 0 || l.slot() == slot) {
               CableNode m = node(lv, l.other());
               if (m != null && m.links().remove(l.otherSlot(), p, l.slot())) {
                  m.cablesChanged();
               }

               n.links().remove(l.slot(), l.other(), l.otherSlot());
               out.add(new ItemStack(item(l.type())));
            }
         }

         n.cablesChanged();
         return out;
      }
   }

   public static void broken(Level lv, BlockPos p, List<Link> links) {
      for(Link l : links) {
         CableNode m = node(lv, l.other());
         if (m != null && m.links().remove(l.otherSlot(), p, l.slot())) {
            m.cablesChanged();
         }
      }

      for(Link l : links) {
         lv.addFreshEntity(new ItemEntity(lv, (double)p.getX() + (double)0.5F, (double)p.getY() + (double)0.5F, (double)p.getZ() + (double)0.5F, new ItemStack(item(l.type()))));
      }

   }

   public static void checkStretch(Level lv, BlockPos p, CableNode n) {
      for(Link l : new ArrayList(n.links().list())) {
         CableNode m = node(lv, l.other());
         if (m != null) {
            double d = SableCompat.distance(lv, n.anchor(l.slot()), m.anchor(l.otherSlot()));
            if (!(d <= (double)l.type().maxLen * (double)1.25F)) {
               if (m.links().remove(l.otherSlot(), p, l.slot())) {
                  m.cablesChanged();
               }

               n.links().remove(l.slot(), l.other(), l.otherSlot());
               n.cablesChanged();
               lv.addFreshEntity(new ItemEntity(lv, (double)p.getX() + (double)0.5F, (double)p.getY() + (double)0.5F, (double)p.getZ() + (double)0.5F, new ItemStack(item(l.type()))));
               lv.playSound((Player)null, p, (SoundEvent)ModSounds.CABLE_DETACH.get(), SoundSource.BLOCKS, 1.0F, 0.7F);
            }
         }
      }

   }

   static Vec3 anchorOf(Level lv, NodeRef r) {
      CableNode n = node(lv, r.pos());
      return n == null ? Vec3.atCenterOf(r.pos()) : n.anchor(r.slot());
   }

   public static <T> List<Hit<T>> reach(Level lv, BlockPos start, Class<T> type) {
      List<Hit<T>> out = new ArrayList();
      CableNode s = node(lv, start);
      if (s == null) {
         return out;
      } else {
         Map<NodeRef, Seg> parent = new HashMap();
         Set<NodeRef> seen = new HashSet();
         Deque<NodeRef> q = new ArrayDeque();

         for(Link l : s.links().list()) {
            NodeRef far = l.far();
            if (seen.add(far)) {
               parent.put(far, new Seg(new NodeRef(start, l.slot()), far, l.type()));
               q.add(far);
            }
         }

         Set<BlockPos> found = new HashSet();

         while(!q.isEmpty() && seen.size() < 512) {
            NodeRef r = (NodeRef)q.poll();
            CableNode n = node(lv, r.pos());
            if (n != null && !r.pos().equals(start)) {
               if (type.isInstance(n) && found.add(r.pos())) {
                  LinkedList<Seg> path = new LinkedList();
                  NodeRef c = r;
                  int guard = 0;

                  while(parent.containsKey(c) && guard++ < 512) {
                     Seg sg = (Seg)parent.get(c);
                     path.addFirst(sg);
                     c = sg.from();
                     if (c.pos().equals(start)) {
                        break;
                     }
                  }

                  out.add(new Hit(type.cast(n), path));
               }

               if (n.junction(r.slot())) {
                  for(Link l : n.links().list()) {
                     if (l.slot() == r.slot()) {
                        NodeRef far = l.far();
                        if (seen.add(far)) {
                           parent.put(far, new Seg(r, far, l.type()));
                           q.add(far);
                        }
                     }
                  }
               }
            }
         }

         return out;
      }
   }

   public static <T> List<T> targets(List<Hit<T>> hits) {
      List<T> l = new ArrayList();

      for(Hit<T> h : hits) {
         l.add(h.target());
      }

      return l;
   }

   public static boolean isNode(BlockEntity be) {
      return be instanceof CableNode;
   }

   public static record Seg(NodeRef from, NodeRef to, CableType type) {
   }

   public static record Hit<T>(T target, List<Seg> path) {
      public float lineQuality(Level lv) {
         double loss = (double)0.0F;

         for(Seg s : this.path) {
            loss += s.type().loss * SableCompat.distance(lv, Cables.anchorOf(lv, s.from()), Cables.anchorOf(lv, s.to()));
         }

         return (float)Math.max((double)0.25F, (double)1.0F - loss);
      }
   }
}
