package ch.vtt.lanunderground.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "getNextAirOnLand", at = @At("HEAD"), cancellable = true)
    private void disableLandAirRegen(int air, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(air); // Behalte aktuelle Luftmenge bei
    }
}