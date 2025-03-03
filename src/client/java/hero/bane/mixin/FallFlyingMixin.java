package hero.bane.mixin;

import hero.bane.HerosElytraOptimizer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.TimeUnit;

@Mixin(LivingEntity.class)
public abstract class FallFlyingMixin {

    @Inject(method = "tickFallFlying", at = @At("HEAD"), cancellable = true)
    private void enforceGlideBehavior(CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity entity) {
            String[] config = HerosElytraOptimizer.getCurrentServerConfig();
            String glideMode = config[0]; //off,delayed,on

            if (!entity.isFallFlying()) {
                return;
            }

            switch (glideMode) {
                case "off":
                    return;
                case "on":
                    entity.stopFallFlying();
                    ci.cancel();
                    break;
                case "delayed":
                    int ping = HerosElytraOptimizer.getPlayerPing();
                    if (ping > 0) {
                        HerosElytraOptimizer.executor.schedule(() -> {
                            if (entity.isFallFlying()) {
                                entity.stopFallFlying();
                            }
                        }, ping, TimeUnit.MILLISECONDS);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
