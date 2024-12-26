package hero.bane.mixin;

import hero.bane.HerosElytraOptimizerClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mixin(FireworkRocketItem.class)
public class RocketBoostMixin {

    @Unique
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Unique
    private static final int BOOST_DURATION_TICKS = 10;

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void modifyRocketUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (user.isFallFlying()) {
            long timeInWorld = HerosElytraOptimizerClient.getTimeInWorld();
            if (timeInWorld < 30000) {
                user.sendMessage(Text.literal("Rocket boost not available yet. Wait 30 seconds after joining the world").styled(style -> style.withColor(0xAAAAAA)), false);
                cir.setReturnValue(TypedActionResult.fail(user.getStackInHand(hand)));
                return;
            }

            ItemStack itemStack = user.getStackInHand(hand);

            if (world.isClient) {
                int latency = getPlayerPing();
                if (latency == 0) {
                    user.sendMessage(Text.literal("Rocket boost canceled as could not get ping correctly").styled(style -> style.withColor(0xAAAAAA)), false);
                    cir.setReturnValue(TypedActionResult.fail(user.getStackInHand(hand)));
                    return;
                }

                executor.schedule(() -> {
                    applyFireworkBoostLoop(user);
                    user.sendMessage(Text.literal("Firework boost applied after " + latency + " ms!"), false);
                }, latency, TimeUnit.MILLISECONDS);

                itemStack.decrementUnlessCreative(1, user);
                user.incrementStat(Stats.USED.getOrCreateStat((FireworkRocketItem) (Object) this));
            }

            cir.setReturnValue(TypedActionResult.success(user.getStackInHand(hand), world.isClient()));
        } else {
            cir.setReturnValue(TypedActionResult.pass(user.getStackInHand(hand)));
        }
    }

    @Unique
    private void applyFireworkBoostLoop(PlayerEntity player) {
        ItemStack chestSlot = player.getEquippedStack(EquipmentSlot.CHEST);
        boolean hasElytra = chestSlot.isOf(Items.ELYTRA);
        for (int i = 0; i < BOOST_DURATION_TICKS; i++) {
            executor.schedule(() -> {
                if (hasElytra) {
                    applyFireworkBoost(player);
                }
            }, i * 50L, TimeUnit.MILLISECONDS);
        }
    }

    @Unique
    private void applyFireworkBoost(PlayerEntity player) {

        Vec3d vec3d = player.getRotationVector();
        Vec3d vec3d2 = player.getVelocity();

        player.setVelocity(vec3d2.add(vec3d.x * 0.1 + (vec3d.x * (double)1.5F - vec3d2.x) * (double)0.5F, vec3d.y * 0.1 + (vec3d.y * (double)1.5F - vec3d2.y) * (double)0.5F, vec3d.z * 0.1 + (vec3d.z * (double)1.5F - vec3d2.z) * (double)0.5F));
        player.velocityModified = true;
    }

    @Unique
    private int getPlayerPing() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null && client.player != null) {
            PlayerListEntry playerEntry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (playerEntry != null) {
                return playerEntry.getLatency();
            }
        }
        return 50;
    }
}
