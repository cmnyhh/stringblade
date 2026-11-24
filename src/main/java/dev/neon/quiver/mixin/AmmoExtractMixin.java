package dev.neon.quiver.mixin;

import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import dev.neon.quiver.QuiverEnergy;
import dev.neon.quiver.QuiverItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * TaCZ extracts a round by decrementing the ammo box's {@code AmmoCount} in place
 * ({@code LivingEntityShoot.consumeAmmoFromPlayer}, {@code ModernKineticGunScriptAPI
 * .consumeAmmoFromPlayer} and the Lua API all funnel through here). Mirror that write into the FE
 * reservoir so one shot always costs exactly {@link QuiverEnergy#PER_SHOT} FE.
 *
 * <p>1.20.1 signature: {@code findAndExtractInventoryAmmo(net.minecraftforge.items.IItemHandler,
 * ItemStack, int)} - the handler is the first argument, not the last.</p>
 */
@Mixin(value = AbstractGunItem.class, remap = false)
abstract class AmmoExtractMixin {
    @Redirect(method = "findAndExtractInventoryAmmo", require = 1,
            at = @At(value = "INVOKE",
                    target = "Lcom/tacz/guns/api/item/IAmmoBox;setAmmoCount("
                            + "Lnet/minecraft/world/item/ItemStack;I)V"))
    private void neonquiver$drainEnergy(IAmmoBox box, ItemStack stack, int count) {
        boolean quiver = QuiverItems.isQuiver(stack);
        int energyBefore = quiver ? QuiverEnergy.getEnergy(stack) : 0;
        box.setAmmoCount(stack, count);
        if (quiver) {
            QuiverEnergy.afterAmmoCountWrite(stack, energyBefore, count);
        }
    }
}
