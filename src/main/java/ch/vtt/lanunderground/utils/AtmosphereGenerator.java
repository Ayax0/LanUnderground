package ch.vtt.lanunderground.utils;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import net.minecraft.registry.Registries;

public class AtmosphereGenerator {

    public static int getRange(MachineBlockEntity machine) {
        String id = Registries.BLOCK.getId(machine.getCachedState().getBlock()).toString();
        return switch (id) {
            case "modern_industrialization:bronze_atmosphere_generator" -> 16;
            case "modern_industrialization:steel_atmosphere_generator" -> 32;
            case "modern_industrialization:electric_atmosphere_generator" -> 64;
            default -> 0;
        };
    }
}
