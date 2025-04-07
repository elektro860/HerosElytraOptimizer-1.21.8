package hero.bane.render;

import hero.bane.HerosElytraOptimizer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class ElytraOptimizerHud {

    public void draw(DrawContext context, int lingerLeft, boolean shouldntFade) {
        if (!HerosElytraOptimizer.showHud) return;

        PlayerEntity player = HerosElytraOptimizer.client.player;
        if (player == null) return;

        TextRenderer textRenderer = HerosElytraOptimizer.client.textRenderer;

        Vec3d pos = player.getPos();
        double dx = pos.x - player.prevX;
        double dy = pos.y - player.prevY;
        double dz = pos.z - player.prevZ;

        double totalSpeed = Math.sqrt(dx * dx + dy * dy + dz * dz);
        float pitch = player.getPitch();

        String line1 = String.format("Total Speed: %.2f blocks/s", totalSpeed / 0.05);
        String line2 = String.format("Fall Distance: %.2f", player.fallDistance);
        String line3 = String.format("Pitch: %.2f°", pitch);

        int x = HerosElytraOptimizer.hudX;
        int y = HerosElytraOptimizer.hudY;


        float alphaFraction = Math.max(0f, Math.min(1f, (lingerLeft) / (float) HerosElytraOptimizer.linger));
        int alpha = shouldntFade ? 255 : (int) (0.6f * 255 + 0.4f * 255 * alphaFraction);

        int color = (alpha << 24) | 0xFFFFFF;

        int baseColor2 = 0xFFFFFF;
        if (player.fallDistance > 2 && player.fallDistance < 7) {
            baseColor2 = 0xAAFFFF;
        } else if (player.fallDistance >= 7) {
            baseColor2 = 0xAAFFAA;
        }
        int color2 = (alpha << 24) | baseColor2;

        context.drawTextWithShadow(textRenderer, line1, x, y, color);
        context.drawTextWithShadow(textRenderer, line2, x, y + 10, color2);
        context.drawTextWithShadow(textRenderer, line3, x, y + 20, color);
    }
}
