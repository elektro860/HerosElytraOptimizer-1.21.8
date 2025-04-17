package hero.bane.mixin;

import hero.bane.HerosElytraOptimizer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    /**
     * @author HerobaneNair
     * @reason i wanna be upside down >:)
     */
    @Overwrite
    public static boolean shouldFlipUpsideDown(LivingEntity entity) {
        if (entity instanceof PlayerEntity || entity.hasCustomName()) {
            String string = Formatting.strip(entity.getName().getString());
            if ("Dinnerbone".equals(string) || "Grumm".equals(string) || ("HerobaneNair".equals(string) && HerosElytraOptimizer.shouldFlipHero)) {
                return !(entity instanceof PlayerEntity) || ((PlayerEntity)entity).isPartVisible(PlayerModelPart.CAPE);
            }
        }
        return false;
    }
}