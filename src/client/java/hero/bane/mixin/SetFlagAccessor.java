package hero.bane.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface SetFlagAccessor {
    @Invoker("setFlag")
    void callSetFlag(int flag, boolean value);
}
