package dev.neon.quiver.mixin;

import com.tacz.guns.item.AmmoBoxItem;
import dev.neon.quiver.QuiverCharge;
import dev.neon.quiver.QuiverItems;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * GUI half of the redstone gesture, the direct 1.20.1 replacement for NeoForge's
 * {@code ItemStackedOnOtherEvent} (which does not exist here).
 *
 * <p>{@code AbstractContainerMenu#slotClicked} asks the item <em>in the slot</em> first via
 * {@code Item#overrideOtherStackedOnMe}, handing it the carried stack, the menu's
 * {@link SlotAccess} and the player. TaCZ's ammo box overrides that method (SRG
 * {@code m_142305_}) and only delegates to {@code Item}, so injecting at its head covers every
 * container - the player inventory and the Curios container that owns the "back" slot alike.</p>
 *
 * <p>The second injection is the mirror image: {@code m_142207_} ({@code overrideStackedOnOther})
 * is what TaCZ runs when the <em>carried</em> stack is an ammo box, and its secondary-click branch
 * mints a real ammo item from the box's ammo id into an empty slot. A quiver pays in FE, so that
 * would fabricate the very ammo item this mod hides; the click is swallowed instead.</p>
 */
@Mixin(value = AmmoBoxItem.class, remap = false)
abstract class AmmoBoxChargeMixin {
    /** {@code Item#overrideOtherStackedOnMe} - carried stack on the quiver. */
    @Inject(method = "m_142305_", at = @At("HEAD"), cancellable = true)
    private void neonquiver$chargeFromSlot(ItemStack stack, ItemStack carried, Slot slot,
                                           ClickAction action, Player player, SlotAccess carriedAccess,
                                           CallbackInfoReturnable<Boolean> cir) {
        if (QuiverCharge.onStackedOnQuiver(stack, carried, action, player, carriedAccess)) {
            cir.setReturnValue(Boolean.TRUE);
        }
    }

    /** {@code Item#overrideStackedOnOther} - the quiver carried onto some slot. */
    @Inject(method = "m_142207_", at = @At("HEAD"), cancellable = true)
    private void neonquiver$noAmmoUnload(ItemStack stack, Slot slot, ClickAction action, Player player,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (action == ClickAction.SECONDARY && QuiverItems.isQuiver(stack)) {
            cir.setReturnValue(Boolean.TRUE);
        }
    }
}
