package ch.vtt.lanunderground.event;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.ElectricCraftingMachineBlockEntity;
import aztech.modern_industrialization.machines.components.IsActiveComponent;
import ch.vtt.lanunderground.armor.BronzeOxygenTankArmorMaterial;
import ch.vtt.lanunderground.armor.ElectricOxygenTankArmorMaterial;
import ch.vtt.lanunderground.armor.SteelOxygenTankArmorMaterial;
import ch.vtt.lanunderground.utils.AtmosphereGenerator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class AirManagerServer {

    ArrayList<MachineBlockEntity> blocks = new ArrayList<>();

    public void register() {
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((block, world) -> {
            if (!(block instanceof MachineBlockEntity machine)) return;

            String id = Registries.BLOCK.getId(machine.getCachedState().getBlock()).toString();
            if(
                id.equals("modern_industrialization:bronze_atmosphere_generator") ||
                id.equals("modern_industrialization:steel_atmosphere_generator") ||
                id.equals("modern_industrialization:electric_atmosphere_generator")
            ) {
                blocks.add(machine);
            }
        });

        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((block, world) -> {
            if (!(block instanceof MachineBlockEntity machine)) return;

            String id = Registries.BLOCK.getId(machine.getCachedState().getBlock()).toString();
            if(
                id.equals("modern_industrialization:bronze_atmosphere_generator") ||
                id.equals("modern_industrialization:steel_atmosphere_generator") ||
                id.equals("modern_industrialization:electric_atmosphere_generator")
            ) {
                blocks.remove(machine);
            }
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                int air = player.getAir();
                ItemStack chestItem = player.getEquippedStack(EquipmentSlot.CHEST);

                if(player.isSubmergedInWater()) {
                    if(chestItem.getItem() instanceof ArmorItem armor) {
                        if(
                            (
                                armor.getMaterial() instanceof BronzeOxygenTankArmorMaterial ||
                                armor.getMaterial() instanceof SteelOxygenTankArmorMaterial ||
                                armor.getMaterial() instanceof ElectricOxygenTankArmorMaterial
                            ) &&
                            chestItem.getDamage() < (chestItem.getMaxDamage() - 1)
                        ) {
                            if(air < player.getMaxAir())
                                player.setAir(air + 2);

                            if(server.getTicks() % 20 == 0)
                                chestItem.damage(
                                    1,
                                    player,
                                    p -> p.sendEquipmentBreakStatus(EquipmentSlot.CHEST)
                                );
                        }
                    }
                } else {
                    if(isAtmosphereNearby(player)) {
                        if(air < player.getMaxAir())
                            player.setAir(air + 1);
                    } else if(
                        chestItem.getItem() instanceof ArmorItem armor &&
                        (
                            armor.getMaterial() instanceof BronzeOxygenTankArmorMaterial ||
                            armor.getMaterial() instanceof SteelOxygenTankArmorMaterial ||
                            armor.getMaterial() instanceof ElectricOxygenTankArmorMaterial
                        ) &&
                        chestItem.getDamage() < (chestItem.getMaxDamage() - 1)
                    ) {
                        if(air < player.getMaxAir())
                            player.setAir(air + 1);

                        if(server.getTicks() % 20 == 0)
                            chestItem.damage(
                                1,
                                player,
                                p -> p.sendEquipmentBreakStatus(EquipmentSlot.CHEST)
                            );
                    } else {
                        if (air > 0) {
                            player.setAir(air - 1);
                        } else {
                            player.setAir(0);
                            if (server.getTicks() % 40 == 0)
                                player.damage(player.getDamageSources().drown(), 1.0F);
                        }
                    }
                }
            }

            for(MachineBlockEntity block : blocks) {
                AtomicBoolean isActive = new AtomicBoolean(false);
                block.forComponentType(IsActiveComponent.class, comp -> isActive.set(comp.isActive));
                if(!isActive.get()) continue;

                MIInventory inventory = block.getInventory();

                for(ConfigurableItemStack _stack : inventory.getItemStacks()) {
                    ItemStack stack = _stack.toStack();
                    if(stack.getItem() instanceof ArmorItem armor) {
                        if(
                            armor.getMaterial() instanceof BronzeOxygenTankArmorMaterial ||
                            armor.getMaterial() instanceof SteelOxygenTankArmorMaterial ||
                            armor.getMaterial() instanceof ElectricOxygenTankArmorMaterial
                        ) {
                            if (stack.getDamage() > 0) {
                                stack.setDamage(stack.getDamage() - 1);
                                _stack.setKey(ItemVariant.of(stack));

                                for (ConfigurableFluidStack fluid : inventory.getFluidStacks()) {
                                    if (fluid.getResource().getFluid().matchesType(MIFluids.OXYGEN.asFluid())) {
                                        fluid.decrement(100);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                if(block instanceof ElectricCraftingMachineBlockEntity machine) {
                    machine.getCrafterComponent().decreaseEfficiencyTicks();
                }
            }
        });
    }

    private boolean isAtmosphereNearby(ServerPlayerEntity player) {
        AtomicBoolean isActive = new AtomicBoolean(false);

        for(MachineBlockEntity machine : blocks) {
            int range = AtmosphereGenerator.getRange(machine);
            boolean inSquare =
                Math.abs(player.getPos().getX() - machine.getPos().toCenterPos().getX()) <= range &&
                Math.abs(player.getPos().getZ() - machine.getPos().toCenterPos().getZ()) <= range;

            if(!inSquare) continue;

            machine.forComponentType(IsActiveComponent.class, comp -> {
                if(comp.isActive) {
                    isActive.set(true);
                    return;
                }
            });
        }

        return isActive.get();
    }

}
