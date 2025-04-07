package hero.bane.mixin;

import hero.bane.HerosElytraOptimizer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class ElytraHudMixin {
    @Unique
    private long keepHudTill = 0;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (HerosElytraOptimizer.hud == null || !HerosElytraOptimizer.showHud || HerosElytraOptimizer.client.player == null) return;
        PlayerEntity player = HerosElytraOptimizer.client.player;
        long currentTime = System.currentTimeMillis();

        boolean check = (
                player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) ||
                player.getMainHandStack().isOf(Items.MACE) ||
                player.getMainHandStack().isOf(Items.WIND_CHARGE)) &&
                (!player.isOnGround());

        if (check) {
            keepHudTill = currentTime + HerosElytraOptimizer.linger;
        }

        if (keepHudTill >= currentTime && !player.isOnGround()) {
            HerosElytraOptimizer.hud.draw(context, (int) (keepHudTill - currentTime), check);
        }
    }
}
