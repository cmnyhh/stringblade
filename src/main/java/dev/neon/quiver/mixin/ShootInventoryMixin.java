package dev.neon.quiver.mixin;

import com.tacz.guns.entity.shooter.LivingEntityShoot;
import dev.neon.quiver.WornAmmo;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Same wrapping for the per-tick shooter, which draws rounds while firing. */
@Mixin(value = LivingEntityShoot.class, remap = false)
abstract class ShootInventoryMixin {
    @Redirect(method = {"consumeAmmoFromPlayer"}, require = 1,
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
