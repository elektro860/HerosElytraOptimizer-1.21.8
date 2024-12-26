package hero.bane.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class FallFlyingMixin {

    /**
     * Injects into the flag that sets the player into the gliding animation
     * Stops fall flying if no Elytra is equipped in the chest slot
     */
    @Inject(method = "tickFallFlying", at = @At("HEAD"), cancellable = true)
    private void addElytraRequirement(CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity entity) {
            ItemStack chestSlot = entity.getEquippedStack(EquipmentSlot.CHEST);
            boolean hasElytra = chestSlot.isOf(Items.ELYTRA);
            //Wanted to add a check whenever they hit the ground but is fun to be able to glide like that :)
            //boolean onGround = entity.fallDistance == 0.0f;
            if (!hasElytra) {
                entity.stopFallFlying();
                ci.cancel();
            }
        }
    }
}
