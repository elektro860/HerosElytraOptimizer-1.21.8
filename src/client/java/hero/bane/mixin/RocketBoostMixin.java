package hero.bane.mixin;

import hero.bane.HerosElytraOptimizer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.TimeUnit;

@Mixin(FireworkRocketItem.class)
public class RocketBoostMixin {

    @Unique
    private static final int BOOST_DURATION_TICKS = 10; //Hardcoded for now because users won't need to specify that

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void modifyRocketUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!user.isFallFlying()) {
            cir.setReturnValue(TypedActionResult.pass(user.getStackInHand(hand)));
            return;
        }

        String[] config = HerosElytraOptimizer.getCurrentServerConfig();
        String rocketMode = config[1]; //off,delayed,on

        switch (rocketMode) {
            case "off" -> {
                return;
            }
            case "on" -> {
                applyFireworkBoostLoop(user);
                return;
            }
            case "delayed" -> {
                int latency = HerosElytraOptimizer.getPlayerPing();
                if (latency == 0) {
                    user.sendMessage(Text.literal("Rocket Boost cancelled as ping could not be determined").styled(style -> style.withColor(0xAAAAAA)), false);
                    cir.setReturnValue(TypedActionResult.fail(user.getStackInHand(hand)));
                    return;
                }
                HerosElytraOptimizer.executor.schedule(() -> applyFireworkBoostLoop(user), latency, TimeUnit.MILLISECONDS);
            }
        }

        ItemStack itemStack = user.getStackInHand(hand);
        if (world.isClient) {
            itemStack.decrementUnlessCreative(1, user);
            user.incrementStat(Stats.USED.getOrCreateStat((FireworkRocketItem) (Object) this));
        }

        cir.setReturnValue(TypedActionResult.success(user.getStackInHand(hand), world.isClient()));
    }

    @Unique
    private void applyFireworkBoostLoop(PlayerEntity player) {
        for (int i = 0; i < BOOST_DURATION_TICKS; i++) {
            HerosElytraOptimizer.executor.schedule(() -> applyFireworkBoost(player), i * 50L, TimeUnit.MILLISECONDS);
        }
    }

    @Unique
    private void applyFireworkBoost(PlayerEntity player) {
        Vec3d vec3d = player.getRotationVector();
        Vec3d vec3d2 = player.getVelocity();
        player.setVelocity(vec3d2.add(vec3d.x * 0.1 + (vec3d.x * 1.5F - vec3d2.x) * 0.5F, vec3d.y * 0.1 + (vec3d.y * 1.5F - vec3d2.y) * 0.5F, vec3d.z * 0.1 + (vec3d.z * 1.5F - vec3d2.z) * 0.5F));
        player.velocityModified = true;
    }
}
