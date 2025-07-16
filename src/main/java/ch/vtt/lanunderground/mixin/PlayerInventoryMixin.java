package ch.vtt.lanunderground.mixin;

import ch.vtt.lanunderground.armor.BronzeOxygenTankArmorMaterial;
import ch.vtt.lanunderground.armor.ElectricOxygenTankArmorMaterial;
import ch.vtt.lanunderground.armor.SteelOxygenTankArmorMaterial;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {

    @Shadow public abstract ItemStack getArmorStack(int slot);
    @Shadow public PlayerEntity player;

    /**
     * @author Ayax0
     * @reason prevent damage to OxygenTank
     * Overwrites the PlayerInventory#damageArmor method to prevent durability loss
     * on OxygenTankItem. All other armor behaves normally.
     */
    @Overwrite
    public void damageArmor(DamageSource damageSource, float amount, int[] slots) {
        if (!(amount <= 0.0F)) {
            amount /= 4.0F;
            if (amount < 1.0F) {
                amount = 1.0F;
            }

            for(int i : slots) {
                ItemStack itemStack = this.getArmorStack(i);
                if(itemStack.getItem() instanceof ArmorItem armor) {
                    if(armor.getMaterial() instanceof BronzeOxygenTankArmorMaterial)
                        continue;
                    if(armor.getMaterial() instanceof SteelOxygenTankArmorMaterial)
                        continue;
                    if(armor.getMaterial() instanceof ElectricOxygenTankArmorMaterial)
                        continue;
                }

                if ((!damageSource.isIn(DamageTypeTags.IS_FIRE) || !itemStack.getItem().isFireproof()) && itemStack.getItem() instanceof ArmorItem) {
                    itemStack.damage((int)amount, this.player, (player) -> player.sendEquipmentBreakStatus(EquipmentSlot.fromTypeIndex(EquipmentSlot.Type.ARMOR, i)));
                }
            }

        }
    }
}
