package dev.neon.quiver;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Built-in charging that works without any energy mod: one redstone dust buys one shot (300 FE).
 *
 * <p>Two gestures reach the same code path, both of them server-authoritative:</p>
 * <ul>
 *   <li>Carry redstone dust on the mouse and right-click a slot holding the quiver. Every GUI
 *       routes this through {@link ItemStackedOnOtherEvent} - the player inventory and the Curios
 *       container (where the "back" slot lives) are both plain {@code AbstractContainerMenu}s, so
 *       the quiver can be charged where it actually sits while shooting.</li>
 *   <li>In the world: hold the quiver in one hand and redstone dust in the other and right-click.</li>
 * </ul>
 */
public final class QuiverCharge {
    private QuiverCharge() {}

    private static boolean isRedstone(ItemStack stack) {
        return !stack.isEmpty() && stack.is(Items.REDSTONE);
    }

    public static boolean canCharge(ItemStack quiver, ItemStack redstone) {
        return QuiverItems.isQuiver(quiver) && isRedstone(redstone) && !QuiverEnergy.isFull(quiver);
    }

    /**
     * Consumes one redstone dust (never in creative) and stores exactly one shot of FE.
     *
     * @param carried slot access of the mouse stack during a GUI click, or {@code null} when the
     *                redstone comes straight out of a hand
     * @return true when a shot worth of energy was added
     */
    public static boolean charge(Player player, ItemStack quiver, ItemStack redstone, @Nullable SlotAccess carried) {
        if (!canCharge(quiver, redstone) || player.level().isClientSide()) return false;

        if (!player.isCreative()) {
            boolean spent = false;
            if (carried != null) {
                ItemStack next = redstone.copy();
                next.shrink(1);
                spent = carried.set(next.isEmpty() ? ItemStack.EMPTY : next);
            }
            if (!spent) {
                redstone.shrink(1);
            }
        }
        QuiverEnergy.addEnergy(quiver, QuiverEnergy.PER_SHOT);
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.4F, 1.5F);
        return true;
    }

    /** Convenience overload for callers that only have the two stacks (tests, scripts). */
    public static boolean charge(Player player, ItemStack quiver, ItemStack redstone) {
        return charge(player, quiver, redstone, null);
    }

    public static void onStackedOn(ItemStackedOnOtherEvent event) {
        if (event.getClickAction() != ClickAction.SECONDARY) return;

        ItemStack carried = event.getCarriedItem();
        ItemStack target = event.getStackedOnItem();

        if (canCharge(target, carried)) {
            charge(event.getPlayer(), target, carried, event.getCarriedSlotAccess());
            event.setCanceled(true);
            return;
        }
        // A quiver no longer hands out arrow items: a shot costs 300 FE, so unloading it into a
        // slot must not mint loot (and must not desync AmmoCount behind the reservoir).
        if (QuiverItems.isQuiver(carried)) {
            event.setCanceled(true);
        }
    }

    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        ItemStack quiver;
        ItemStack redstone;
        if (QuiverItems.isQuiver(main)) {
            quiver = main;
            redstone = off;
        } else if (QuiverItems.isQuiver(off)) {
            quiver = off;
            redstone = main;
        } else {
            return;
        }
        if (!canCharge(quiver, redstone)) return;

        charge(player, quiver, redstone, null);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
