package dev.neon.quiver;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge-bus wiring: per-stack capabilities, the FE tooltip and the redstone charging gesture.
 */
@Mod.EventBusSubscriber(modid = NeonQuiver.MOD_ID)
public final class QuiverGameEvents {
    private static final ResourceLocation ENERGY_AND_CURIO =
            new ResourceLocation(NeonQuiver.MOD_ID, "quiver_capabilities");

    private QuiverGameEvents() {}

    /**
     * 1.20.1 attaches item capabilities per stack. The energy reservoir reaches a charger in the
     * main inventory and in the Curios "back" slot alike, and the Curio capability is what lets the
     * back slot accept the quiver (and only the quiver) in the first place.
     */
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (!QuiverItems.isQuiver(stack)) return;
        event.addCapability(ENERGY_AND_CURIO, new QuiverStackCapability(stack));
    }

    /**
     * Lang-driven FE readout on the quiver tooltip; the HUD keeps showing the shot count from
     * AmmoCount. 1.20.1 has no hook on TaCZ's item class that does not need a mixin, and
     * {@code ItemTooltipEvent} fires after every {@code appendHoverText}, so it lands last.
     */
    @SubscribeEvent
    public static void addTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!QuiverItems.isQuiver(stack)) return;

        int energy = QuiverEnergy.getEnergy(stack);
        List<Component> lines = event.getToolTip();
        lines.add(Component.translatable("neon.quiver.energy.line", energy, QuiverEnergy.CAPACITY)
                .withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("neon.quiver.energy.shots", energy / QuiverEnergy.PER_SHOT,
                        QuiverEnergy.SHOTS)
                .withStyle(ChatFormatting.GRAY));
        if (energy < QuiverEnergy.PER_SHOT) {
            lines.add(Component.translatable("neon.quiver.energy.empty").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        QuiverCharge.onRightClickItem(event);
    }

    /**
     * Field recharge: an empty quiver tops itself up from the FE items the player carries, so a dry
     * quiver no longer means walking back to a charging pad (see {@link QuiverRecharge}).
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player instanceof ServerPlayer server) QuiverRecharge.tick(server);
    }
}
