package dev.neon.quiver.mixin;

import dev.neon.quiver.QuiverEnergy;
import dev.neon.quiver.QuiverItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import com.tacz.guns.item.AmmoBoxItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Lang-driven FE readout on the quiver tooltip; the HUD keeps showing the shot count from AmmoCount. */
@Mixin(value = AmmoBoxItem.class, remap = false)
abstract class AmmoTooltipMixin {
    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void neonquiver$energyTooltip(ItemStack stack, Item.TooltipContext context,
                                          List<Component> lines, TooltipFlag flag, CallbackInfo ci) {
        if (!QuiverItems.isQuiver(stack)) return;
        int energy = QuiverEnergy.getEnergy(stack);
        lines.add(Component.translatable("neon.quiver.energy.line", energy, QuiverEnergy.CAPACITY)
                .withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("neon.quiver.energy.shots", energy / QuiverEnergy.PER_SHOT, QuiverEnergy.SHOTS)
                .withStyle(ChatFormatting.GRAY));
        if (energy < QuiverEnergy.PER_SHOT) {
            lines.add(Component.translatable("neon.quiver.energy.empty").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
