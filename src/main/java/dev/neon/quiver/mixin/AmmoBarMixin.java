package dev.neon.quiver.mixin;

import com.tacz.guns.item.AmmoBoxItem;
import dev.neon.quiver.QuiverEnergy;
import dev.neon.quiver.QuiverItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Draws the quiver's durability bar from its FE reservoir instead of TaCZ's ammo-count capacity.
 *
 * <p>TaCZ sizes an ammo box bar against a full vanilla box:
 * {@code ammoStackSize * AMMO_BOX_STACK_SIZE * (ammoLevel + 1)}, which for this pack is
 * {@code 32 * 3 * 1 = 96} rounds. The quiver only ever carries {@link QuiverEnergy#SHOTS} shots, so a
 * completely full quiver rendered as a half-full bar. Here the bar is the charge: empty is 0 and a
 * full reservoir is 13, the widest vanilla bar.</p>
 *
 * <p>NeoForge 1.21.1 runs with official names, so the target is plain {@code getBarWidth}.
 * {@code require = 0} keeps a future TaCZ build that renames the method from crashing the client.</p>
 */
@Mixin(value = AmmoBoxItem.class, remap = false)
abstract class AmmoBarMixin {
    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true, require = 0)
    private void neonquiver$energyBar(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (!QuiverItems.isQuiver(stack)) return;
        int energy = QuiverEnergy.getEnergy(stack);
        if (energy <= 0) {
            cir.setReturnValue(0);
            return;
        }
        cir.setReturnValue((int) Math.min(1 + 12.0 * energy / QuiverEnergy.CAPACITY, 13));
    }
}
