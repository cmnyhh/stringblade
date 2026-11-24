package dev.neon.quiver.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import dev.neon.quiver.QuiverEnergy;
import dev.neon.quiver.QuiverItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * TaCZ extracts a round by decrementing the ammo box's {@code AmmoCount} in place
 * ({@code LivingEntityShoot.consumeAmmoFromPlayer}, {@code ModernKineticGunScriptAPI
 * .consumeAmmoFromPlayer} and the Lua API all funnel through here). Mirror that write into the FE
 * reservoir so one shot always costs exactly {@link QuiverEnergy#PER_SHOT} FE.
 */
@Mixin(value = AbstractGunItem.class, remap = false)
abstract class AmmoExtractMixin {
    @WrapOperation(method = "findAndExtractInventoryAmmo", require = 1,
            at = @At(value = "INVOKE",
                    target = "Lcom/tacz/guns/api/item/IAmmoBox;setAmmoCount(Lnet/minecraft/world/item/ItemStack;I)V"))
    private void neonquiver$drainEnergy(IAmmoBox box, ItemStack stack, int count, Operation<Void> original) {
        boolean quiver = QuiverItems.isQuiver(stack);
        int energyBefore = quiver ? QuiverEnergy.getEnergy(stack) : 0;
        original.call(box, stack, count);
        if (quiver) {
            QuiverEnergy.afterAmmoCountWrite(stack, energyBefore, count);
        }
    }
}
