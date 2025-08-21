package hero.bane.mixin;

import hero.bane.HerosElytraOptimizer;
import hero.bane.command.HerosElytraOptimizerCommand;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.TimeUnit;

/**
 * Gliding Optimization
 * glide
 */

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "tickGliding", at = @At("HEAD"), cancellable = true)
    private void enforceGlideBehavior(CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity player) {
            String[] config = HerosElytraOptimizer.getCurrentServerConfig();
            String glideMode = config[0]; // off,delayed,on
            if (!player.isGliding()) {
                return;
            }
            switch (glideMode) {
                case "off":
                    return;
                case "on":
                    stopGliding(player, "Stopped Gliding");
                    ci.cancel();
                    break;
                case "delayed":
                    int ping = HerosElytraOptimizer.getPlayerPing();
                    if (ping == 0) {
                        if (HerosElytraOptimizer.debugging
                                && !player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
                            HerosElytraOptimizerCommand
                                    .say("Gliding Optimization cancelled as ping could not be determined", 0xFF5555);
                        }
                        break;
                    }
                    HerosElytraOptimizer.executor.schedule(() -> {
                        if (player.isGliding()) {
                            stopGliding(player, "Gliding Optimization applied after " + ping + "ms");
                        }
                    }, ping, TimeUnit.MILLISECONDS);
                    break;
                default:
                    break;
            }
        }
    }

    @Unique
    private static void stopGliding(PlayerEntity player, String debugMessage) {
        ItemStack chestSlot = player.getEquippedStack(EquipmentSlot.CHEST);
        if (!chestSlot.isOf(Items.ELYTRA)) {
            player.isGliding();
            if (HerosElytraOptimizer.debugging) {
                HerosElytraOptimizerCommand.say(debugMessage, 0xFFFF55);
            }
        }
    }
}
