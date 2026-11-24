package dev.neon.quiver.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gameplay.ClientAmmoCount;
import dev.neon.quiver.WornAmmo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ClientAmmoCount.class, remap = false)
abstract class ClientAmmoCountMixin {
    @ModifyReturnValue(method = "get", at = @At("TAIL"))
    private static int neonquiver$addEquipped(int count, LocalPlayer player, ItemStack gun, IGun unused) {
        return WornAmmo.addHudCount(count, player, gun);
    }
}
