package hero.bane.mixin;

import hero.bane.HerosElytraOptimizer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> extends AnimalModel<T> implements ModelWithArms, ModelWithHead {

    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;

    @Inject(method = "positionRightArm", at = @At("HEAD"), cancellable = true)
    private void modifyRightArmPose(T entity, CallbackInfo ci) {
        if (entity.isFallFlying() && HerosElytraOptimizer.superman) {
            this.rightArm.pitch = (float) Math.toRadians(180-HerosElytraOptimizer.armAngle);
            this.rightArm.yaw = 0;
            this.rightArm.roll = 0;
            ci.cancel();
        }
    }

    @Inject(method = "positionLeftArm", at = @At("HEAD"), cancellable = true)
    private void modifyLeftArmPose(T entity, CallbackInfo ci) {
        if (entity.isFallFlying() && HerosElytraOptimizer.superman) {
            this.leftArm.pitch = (float) Math.toRadians(180-HerosElytraOptimizer.armAngle);
            this.leftArm.yaw = 0;
            this.leftArm.roll = 0;
            ci.cancel();
        }
    }
}