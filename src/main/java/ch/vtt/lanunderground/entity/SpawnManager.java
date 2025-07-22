package ch.vtt.lanunderground.entity;

import ch.vtt.lanunderground.LanUnderground;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;

import java.util.List;

public class SpawnManager {

    public static final List<EntityType<? extends MobEntity>> OVERWORLD_MONSTERS = List.of(
        EntityType.CREEPER,
        EntityType.SKELETON,
        EntityType.SPIDER,
        EntityType.ZOMBIE,
        EntityType.SLIME,
        EntityType.ENDERMAN,
        EntityType.DROWNED,
        EntityType.PHANTOM,
        EntityType.HUSK,
        EntityType.STRAY,
        EntityType.PILLAGER,
        EntityType.ZOMBIE_VILLAGER
    );

    public static final List<EntityType<? extends MobEntity>> OVERWORLD_ANIMALS = List.of(
        EntityType.COW,
        EntityType.SHEEP,
        EntityType.PIG,
        EntityType.CHICKEN,
        EntityType.RABBIT,
        EntityType.HORSE,
        EntityType.DONKEY,
        EntityType.MULE,
        EntityType.LLAMA,
        EntityType.MOOSHROOM,
        EntityType.WOLF,
        EntityType.FOX,
        EntityType.OCELOT,
        EntityType.PANDA,
        EntityType.TURTLE
    );

    public static void registerSpawnRules() {
        OVERWORLD_ANIMALS.forEach(entity -> {
            try {
                SpawnRestriction.register(
                    entity,
                    SpawnRestriction.Location.ON_GROUND,
                    Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                    (type, world, reason, pos, random) -> {
                        return true;
                    }
                );
            } catch(IllegalStateException e) {
                LanUnderground.LOGGER.info("skip animal registration: " + entity.getName());
            }
        });

        OVERWORLD_MONSTERS.forEach(entity -> {
            try {
                SpawnRestriction.register(
                    entity,
                    SpawnRestriction.Location.ON_GROUND,
                    Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                    (type, world, reason, pos, random) -> {
                        if(
                                reason != SpawnReason.CHUNK_GENERATION &&
                                        reason != SpawnReason.NATURAL
                        ) return true;

                        int y = pos.getY();
                        final int MAX_Y = 20;
                        final int MIN_Y = world.getDimension().minY();

                        if (y >= MAX_Y) return false;

                        float probability = (float)(MAX_Y - y) / (MAX_Y - MIN_Y);
                        probability = MathHelper.clamp(probability, 0.0f, 1.0f);

                        return random.nextFloat() < probability;
                    }
                );
            } catch(IllegalStateException e) {
                LanUnderground.LOGGER.info("skip monster registration: " + entity.getName());
            }
        });
    }
}
