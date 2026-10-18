package com.airwaves.logic;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ShapeUtil {
   private ShapeUtil() {
   }

   public static Map<Direction, VoxelShape> make(double x1, double y1, double z1, double x2, double y2, double z2) {
      Map<Direction, VoxelShape> m = new EnumMap(Direction.class);
      m.put(Direction.NORTH, Block.box(x1, y1, z1, x2, y2, z2));
      m.put(Direction.SOUTH, Block.box((double)16.0F - x2, y1, (double)16.0F - z2, (double)16.0F - x1, y2, (double)16.0F - z1));
      m.put(Direction.EAST, Block.box((double)16.0F - z2, y1, x1, (double)16.0F - z1, y2, x2));
      m.put(Direction.WEST, Block.box(z1, y1, (double)16.0F - x2, z2, y2, (double)16.0F - x1));
      return m;
   }
}
