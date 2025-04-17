package hero.bane.render;

import hero.bane.HerosElytraOptimizer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class ElytraOptimizerHud {

    private double lastSpeed = -1;
    private int lastSpeedColor = 0xFFFFFF;

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

        if (lastSpeed >= 0) {
            if (totalSpeed > lastSpeed) {
                lastSpeedColor = 0xAAFFAA;
            } else if (totalSpeed < lastSpeed) {
                lastSpeedColor = 0xFFAAAA;
            }
        }
        lastSpeed = totalSpeed;

        String line_speed = String.format("%.2f bl/s", totalSpeed / 0.05);
        String line_pitch = String.format("%.2f°", player.getPitch());
        String line_fall = String.format("%.2f blocks", player.fallDistance);

        int textWidth = Math.max(
                Math.max(textRenderer.getWidth(line_speed), textRenderer.getWidth(line_pitch)),
                textRenderer.getWidth(line_fall)
        );

        int x = HerosElytraOptimizer.hudX;
        switch (HerosElytraOptimizer.indentMode) {
            case CENTERED -> x -= textWidth / 2;
            case RIGHT -> x -= textWidth;
        }
        int y = HerosElytraOptimizer.hudY;

        float alphaFraction = Math.max(0f, Math.min(1f, lingerLeft / (float) HerosElytraOptimizer.linger));
        int alpha = shouldntFade ? 255 : (int) (0.5f * 255 + 0.5f * 255 * alphaFraction);

        int colorSpeed = (alpha << 24) | lastSpeedColor;
        int colorPitch = (alpha << 24) | 0xFFFFFF;

        int baseColor2 = 0xFFFFFF;
        if (player.fallDistance > 2 && player.fallDistance < 7) {
            baseColor2 = 0xAAFFFF;
        } else if (player.fallDistance >= 7 && player.fallDistance < 20) {
            baseColor2 = 0xAAFFAA;
        } else if (player.fallDistance >= 20) {
            baseColor2 = 0xFFAAAA;
        }
        int colorFall = (alpha << 24) | baseColor2;

        context.drawTextWithShadow(textRenderer, line_speed, x, y, colorSpeed);
        context.drawTextWithShadow(textRenderer, line_pitch, x, y + 10, colorPitch);
        context.drawTextWithShadow(textRenderer, line_fall, x, y + 20, colorFall);
    }
}