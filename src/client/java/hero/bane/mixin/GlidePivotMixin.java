package hero.bane.mixin;

import hero.bane.HerosElytraOptimizer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class GlidePivotMixin {

    @Inject(
            method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFFF)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;setupTransforms(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/util/math/MatrixStack;FFFF)V",
                    shift = At.Shift.AFTER)
    )
    private void translateGlidingPlayer(AbstractClientPlayerEntity player, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta, float scale, CallbackInfo ci) {
        int flightTicks = player.getFallFlyingTicks();
        if (flightTicks <= 0 || player.getPose() != net.minecraft.entity.EntityPose.FALL_FLYING || !player.isFallFlying()) return;
        if (HerosElytraOptimizer.instantGlide && flightTicks < 10) {
            ((LivingEntityAccessor) player).setFallFlyingTicks(10);
            flightTicks = 10;
        }
        float pivot = HerosElytraOptimizer.pivot;
        float offset = HerosElytraOptimizer.offset;

        if (pivot == 0f && offset == 0f) return;

        float t = Math.min(flightTicks + tickDelta, 10f) / 10f;
        t *= t;
        double pitchRad = Math.toRadians(player.getPitch(tickDelta));
        double y = Math.sin(pitchRad) * pivot * t;
        double y2 = Math.cos(pitchRad) * offset * t;
        double z = Math.cos(pitchRad) * pivot * t;

        matrices.translate(0, y + y2, z);
    }
}
