package hero.bane.mixin;

import hero.bane.HerosElytraOptimizer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Player Rendering Changes
 * Pivot
 * Offset
 * Instaglide
 * Superman
 */

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin
        extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityRenderState, PlayerEntityModel> {

    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx,
            PlayerEntityModel model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    // @Inject(method = "setupTransforms", at = @At(value = "INVOKE", target =
    // "Lnet/minecraft/client/render/entity/LivingEntityRenderer;setupTransforms(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/util/math/MatrixStack;FF)V",
    // shift = At.Shift.AFTER)) I don't know how to make this work
    @Inject(method = "setupTransforms(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;FF)V", at = @At("RETURN"))
    private void translateGlidingPlayer(PlayerEntityRenderState player, MatrixStack matrices,
            float bodyYaw, float scale, CallbackInfo ci) {
        float flightTicks = player.glidingTicks;
        if (flightTicks <= 0 || player.pose != net.minecraft.entity.EntityPose.GLIDING || !player.isGliding)
            return;
        if (HerosElytraOptimizer.instantGlide && flightTicks < 10) {
            player.glidingTicks = Math.max(10, player.glidingTicks);
            flightTicks = 10;
        }
        float pivot = HerosElytraOptimizer.pivot;
        float offset = HerosElytraOptimizer.offset;

        if (pivot == 0f && offset == 0f)
            return;

        float t = Math.min(flightTicks, 10f) / 10f;
        t *= t;
        double pitchRad = Math.toRadians(player.pitch);
        double y = Math.sin(pitchRad) * pivot * t;
        double y2 = Math.cos(pitchRad) * offset * t;
        double z = Math.cos(pitchRad) * pivot * t;

        matrices.translate(0, y + y2, z);
    }
}