package com.airwaves.be;

import com.airwaves.cable.CableType;
import com.airwaves.cable.Link;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;

public final class LinkSet {
   private final List<Link> list = new ArrayList();

   public List<Link> list() {
      return this.list;
   }

   public boolean has(int slot, BlockPos other, int otherSlot) {
      for(Link l : this.list) {
         if (l.slot() == slot && l.other().equals(other) && l.otherSlot() == otherSlot) {
            return true;
         }
      }

      return false;
   }

   public boolean linkedTo(BlockPos other) {
      for(Link l : this.list) {
         if (l.other().equals(other)) {
            return true;
         }
      }

      return false;
   }

   public int count(int slot) {
      int n = 0;

      for(Link l : this.list) {
         if (l.slot() == slot) {
            ++n;
         }
      }

      return n;
   }

   public void add(Link l) {
      if (!this.has(l.slot(), l.other(), l.otherSlot())) {
         this.list.add(new Link(l.slot(), l.other().immutable(), l.otherSlot(), l.type()));
      }

   }

   public boolean remove(int slot, BlockPos other, int otherSlot) {
      return this.list.removeIf((l) -> l.slot() == slot && l.other().equals(other) && l.otherSlot() == otherSlot);
   }

   public boolean removeAllTo(BlockPos other) {
      return this.list.removeIf((l) -> l.other().equals(other));
   }

   public int size() {
      return this.list.size();
   }

   public boolean isEmpty() {
      return this.list.isEmpty();
   }

   public void save(CompoundTag tag) {
      ListTag lt = new ListTag();

      for(Link l : this.list) {
         CompoundTag c = new CompoundTag();
         c.putInt("s", l.slot());
         c.putLong("o", l.other().asLong());
         c.putInt("os", l.otherSlot());
         c.putString("t", l.type().id);
         lt.add(c);
      }

      tag.put("links", lt);
   }

   public void load(CompoundTag tag) {
      this.list.clear();

      for(Tag t : tag.getList("links", 10)) {
         CompoundTag c = (CompoundTag)t;
         this.list.add(new Link(c.getInt("s"), BlockPos.of(c.getLong("o")), c.getInt("os"), CableType.byId(c.getString("t"))));
      }

      if (tag.contains("cables", 9)) {
         for(Tag t : tag.getList("cables", 4)) {
            this.list.add(new Link(0, BlockPos.of(((LongTag)t).getAsLong()), 0, CableType.COPPER));
         }
      }

   }
}
