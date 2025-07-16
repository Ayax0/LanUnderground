package ch.vtt.lanunderground.item;

import ch.vtt.lanunderground.LanUnderground;
import ch.vtt.lanunderground.armor.BronzeOxygenTankArmorMaterial;
import ch.vtt.lanunderground.armor.ElectricOxygenTankArmorMaterial;
import ch.vtt.lanunderground.armor.SteelOxygenTankArmorMaterial;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item BRONZE_OXYGEN_TANK = registerItem("bronze_oxygen_tank", new ArmorItem(new BronzeOxygenTankArmorMaterial(), ArmorItem.Type.CHESTPLATE, new Item.Settings().maxDamage(300)));
    public static final Item STEEL_OXYGEN_TANK = registerItem("steel_oxygen_tank", new ArmorItem(new SteelOxygenTankArmorMaterial(), ArmorItem.Type.CHESTPLATE, new Item.Settings().maxDamage(600)));
    public static final Item ELECTRIC_OXYGEN_TANK = registerItem("electric_oxygen_tank", new ArmorItem(new ElectricOxygenTankArmorMaterial(), ArmorItem.Type.CHESTPLATE, new Item.Settings().maxDamage(1800)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(LanUnderground.MOD_ID, name), item);
    }

    public static void registerModItems() {
        LanUnderground.LOGGER.info("Registering Mod Items for " + LanUnderground.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(BRONZE_OXYGEN_TANK);
            entries.add(STEEL_OXYGEN_TANK);
            entries.add(ELECTRIC_OXYGEN_TANK);
        });
    }
}
