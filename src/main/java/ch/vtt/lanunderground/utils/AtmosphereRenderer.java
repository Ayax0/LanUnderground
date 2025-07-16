package ch.vtt.lanunderground.utils;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class AtmosphereRenderer {

    public static List<Vec3d> getTerrainOutline(MachineBlockEntity block) {
        List<Vec3d> terrainOutline = new ArrayList<>();
        World world = block.getWorld();

        if(world == null) return terrainOutline;

        int y = block.getPos().getY();
        int range = AtmosphereGenerator.getRange(block);

        // Nordkante (West → Ost)
        for (int dx = -range; dx <= range; dx++) {
            int x = block.getPos().getX() + dx;
            int z = block.getPos().getZ() - range;
            terrainOutline.add(highestTerrain(world, new BlockPos(x, y, z), range));
        }

        // Ostkante (Nord → Süd)
        for (int dz = -range + 1; dz <= range; dz++) {
            int x = block.getPos().getX() + range;
            int z = block.getPos().getZ() + dz;
            terrainOutline.add(highestTerrain(world, new BlockPos(x, y, z), range));
        }

        // Südkante (Ost → West)
        for (int dx = range - 1; dx >= -range; dx--) {
            int x = block.getPos().getX() + dx;
            int z = block.getPos().getZ() + range;
            terrainOutline.add(highestTerrain(world, new BlockPos(x, y, z), range));
        }

        // Westkante (Süd → Nord)
        for (int dz = range - 1; dz > -range; dz--) {
            int x = block.getPos().getX() - range;
            int z = block.getPos().getZ() + dz;
            terrainOutline.add(highestTerrain(world, new BlockPos(x, y, z), range));
        }

        return terrainOutline;
    }

    public static Vec3d highestTerrain(World world, BlockPos origin, int threshold) {
        final boolean SEARCH_DOWNWARD = world.getBlockState(origin).isAir();

        for(int i = 0; i < threshold; i++) {
            if(SEARCH_DOWNWARD) {
                BlockPos pos = new BlockPos(origin.getX(), origin.getY() - i, origin.getZ());
                if(!world.getBlockState(pos).isAir())
                    return new Vec3d(origin.getX() + 0.5, origin.getY() - i + 1.01, origin.getZ() + 0.5);
            } else {
                BlockPos pos = new BlockPos(origin.getX(), origin.getY() + i, origin.getZ());
                if(world.getBlockState(pos).isAir())
                    return new Vec3d(origin.getX() + 0.5, origin.getY() + i + 0.01, origin.getZ() + 0.5);
            }
        }

        if(SEARCH_DOWNWARD)
            return origin.toCenterPos().add(0, -threshold, 0);
        else
            return origin.toCenterPos().add(0, threshold, 0);
    }

}
