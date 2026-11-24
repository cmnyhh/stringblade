package dev.neon.quiver.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import dev.neon.quiver.WornAmmo;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ModernKineticGunScriptAPI.class, remap = false)
abstract class ScriptInventoryMixin {
    @WrapOperation(method = {"consumeAmmoFromPlayer", "hasAmmoToConsume"}, require = 2,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getCapability(Lnet/neoforged/neoforge/capabilities/EntityCapability;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object neonquiver$includeBack(LivingEntity shooter, EntityCapability<?, ?> capability,
                                          Object context, Operation<Object> original) {
        return WornAmmo.append((IItemHandler) original.call(shooter, capability, context), shooter);
    }
}
