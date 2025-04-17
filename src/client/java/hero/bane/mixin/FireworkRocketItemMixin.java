package hero.bane.mixin;

import hero.bane.HerosElytraOptimizer;
import hero.bane.command.HerosElytraOptimizerCommand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
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

/**
 * Rocket Boost Optimization
 * rocket
 */

@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {

    @Unique
    private static final int BOOST_DURATION_TICKS = 10; //Hardcoded for now because users shouldn't need to specify that

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void modifyRocketUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if(HerosElytraOptimizer.client.getCurrentServerEntry() == null && !user.isFallFlying()) {
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
                if(HerosElytraOptimizer.debugging) {
                    HerosElytraOptimizerCommand.say("Boosting",0xFFFF55);
                }
                return;
            }
            case "delayed" -> {
                int ping = HerosElytraOptimizer.getPlayerPing();
                if (ping == 0) {
                    if(HerosElytraOptimizer.debugging) {
                        HerosElytraOptimizerCommand.say("Rocket Boost cancelled as ping could not be determined",0xFF5555);
                    }
                    cir.setReturnValue(TypedActionResult.fail(user.getStackInHand(hand)));
                    return;
                }
                HerosElytraOptimizer.executor.schedule(() -> {
                    if(HerosElytraOptimizer.debugging) {
                        HerosElytraOptimizerCommand.say("Rocket Boost applied after "+ ping +"ms", 0xFFFF55);
                    }
                    applyFireworkBoostLoop(user);
                }, ping, TimeUnit.MILLISECONDS);
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
        if(!player.isFallFlying() || player.isOnGround()) return;

        for (int i = 0; i < BOOST_DURATION_TICKS; i++) {
            HerosElytraOptimizer.executor.schedule(() -> {
                Vec3d vec3d = player.getRotationVector();
                Vec3d vec3d2 = player.getVelocity();
                //exact code is literally just setVelocity so I just copy pasted here
                player.setVelocity(vec3d2.add(
                        vec3d.x * 0.1 + (vec3d.x * 1.5F - vec3d2.x) * 0.5F,
                        vec3d.y * 0.1 + (vec3d.y * 1.5F - vec3d2.y) * 0.5F,
                        vec3d.z * 0.1 + (vec3d.z * 1.5F - vec3d2.z) * 0.5F));
                player.velocityModified = true;
            }, i * 50L, TimeUnit.MILLISECONDS);
        }
    }
}