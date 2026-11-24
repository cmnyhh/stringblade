package dev.neon.quiver.mixin;

import com.tacz.guns.api.item.IGun;
import dev.neon.quiver.WornAmmo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The client-side HUD counter must also see the rounds sitting in the back slot. */
@Pseudo
@Mixin(targets = "com.tacz.guns.client.gameplay.ClientAmmoCount", remap = false)
abstract class ClientAmmoCountMixin {
    @Inject(method = "get", at = @At("RETURN"), cancellable = true)
    private static void neonquiver$addEquipped(LocalPlayer player, ItemStack gun, IGun gunItem,
                                               CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(WornAmmo.addHudCount(cir.getReturnValue(), player, gun));
    }
}
