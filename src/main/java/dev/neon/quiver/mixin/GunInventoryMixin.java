package dev.neon.quiver.mixin;

import com.tacz.guns.api.item.gun.AbstractGunItem;
import dev.neon.quiver.WornAmmo;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * TaCZ only ever looks for loose ammo in the shooter's inventory capability. Both the "can I
 * reload" and the "do I have ammo" checks are wrapped so the Curios back slot joins that search.
 *
 * <p>1.20.1 hands the handler out as a {@code LazyOptional<ItemHandler>} found through
 * {@code LivingEntity#getCapability(Capability, Direction)} (NeoForge's two-argument
 * {@code getCapability(EntityCapability, Object)} does not exist here), so the extra slots are
 * folded into the lazy result.</p>
 */
@Mixin(value = AbstractGunItem.class, remap = false)
abstract class GunInventoryMixin {
    @Redirect(method = {"hasInventoryAmmo", "canReload"}, require = 2,
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
