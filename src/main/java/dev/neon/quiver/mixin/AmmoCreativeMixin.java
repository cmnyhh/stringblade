package dev.neon.quiver.mixin;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.item.AmmoItem;
import dev.neon.quiver.QuiverEnergy;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The bow still names an ammo id, because TaCZ resolves the quiver's ammo box against it, but rounds
 * are paid for with FE. Listing the matching ammo item in the creative tab would advertise a
 * consumable that no longer exists, so it is dropped from that enumeration.
 */
@Mixin(value = AmmoItem.class, remap = false)
abstract class AmmoCreativeMixin {
    @Inject(method = "fillItemCategory", at = @At("RETURN"))
    private static void neonquiver$hideEnergyAmmo(CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
        cir.getReturnValue().removeIf(stack -> stack.getItem() instanceof IAmmo ammo
                && QuiverEnergy.AMMO_ID.equals(ammo.getAmmoId(stack)));
    }
}
