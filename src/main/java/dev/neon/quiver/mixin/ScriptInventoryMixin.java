package dev.neon.quiver.mixin;

import com.tacz.guns.item.ModernKineticGunScriptAPI;
import dev.neon.quiver.WornAmmo;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * The Lua script API of TaCZ reads ammo from the shooter's inventory capability in the same way;
 * wrap both call sites so a quiver worn on the back feeds scripted guns (this bow included).
 */
@Mixin(value = ModernKineticGunScriptAPI.class, remap = false)
abstract class ScriptInventoryMixin {
    @Redirect(method = {"consumeAmmoFromPlayer", "hasAmmoToConsume"}, require = 2,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getCapability("
                            + "Lnet/minecraftforge/common/capabilities/Capability;"
                            + "Lnet/minecraft/core/Direction;"
                            + ")Lnet/minecraftforge/common/util/LazyOptional;"))
    private LazyOptional<?> neonquiver$includeBack(LivingEntity shooter, Capability<?> capability,
                                                          Direction direction) {
        return WornAmmo.includeBack(shooter.getCapability(capability, direction), shooter);
    }
}
