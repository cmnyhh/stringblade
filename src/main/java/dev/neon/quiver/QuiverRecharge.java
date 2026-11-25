package dev.neon.quiver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

/**
 * Keeps an Arc Quiver fed from the FE items the player is carrying.
 *
 * <p>A quiver only ever charges from a charging pad or a redstone dust, so a player who runs dry
 * mid-fight has to walk back to a dock. This pool fixes that: whenever the quiver has no shot left,
 * the emptiest FE item in the main inventory (and the Curios "back" slot) is drained into it.</p>
 *
 * <p>Design choices:</p>
 * <ul>
 *   <li>Only fires when the quiver is actually empty, so a mostly-full quiver never causes work 閳? *       the check is two tag reads and it runs once every {@value #CHECK_INTERVAL} ticks.</li>
 *   <li>At most {@link #BATCH} FE per cycle, so a 1M FE battery is never flattened in one tick.</li>
 *   <li>Emptiest items first: a nearly-dead cell empties before a full one, which is what players
 *       expect from "use up the dregs first".</li>
 *   <li>Quivers are never treated as a source (no quiver-to-quiver shuffling), and neither are the
 *       armour slots, so worn gear keeps its charge.</li>
 * </ul>
 */
public final class QuiverRecharge {
    /** How often a player is inspected (10 ticks = twice a second). */
    private static final int CHECK_INTERVAL = 10;
    /** Most FE a single cycle may pull: four shots. */
    private static final int BATCH = QuiverEnergy.PER_SHOT * 4;
    /** Vanilla player inventory: 36 main slots; armour and offhand are deliberately excluded. */
    private static final int MAIN_SLOTS = 36;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("neonquiver/recharge");

    private QuiverRecharge() {}

    /** Called from the player tick hook; costs one modulo and (usually) nothing else. */
    public static void tick(ServerPlayer player) {
        if (player.tickCount % CHECK_INTERVAL != 0) return;
        ItemStack quiver = findQuiver(player);
        if (quiver.isEmpty() || QuiverEnergy.getShots(quiver) > 0) return;
        int want = Math.min(BATCH, QuiverEnergy.CAPACITY - QuiverEnergy.getEnergy(quiver));
        if (want <= 0) return;
        int pulled = pull(player, quiver, want);
        if (pulled > 0) {
            QuiverEnergy.addEnergy(quiver, pulled);
            LOGGER.debug("Quiver recharged: +{} FE from carried items ({} / {} shots)", pulled,
                    QuiverEnergy.getShots(quiver), QuiverEnergy.SHOTS);
        } else if (player.tickCount % 200 == 0) {
            // Once per 10 s at most: a dry player with no FE items would otherwise log twice a second.
            LOGGER.debug("Quiver empty and no FE source found in inventory or back slot");
        }
    }

    /** The worn quiver when there is one, otherwise the first quiver in the main inventory. */
    private static ItemStack findQuiver(ServerPlayer player) {
        ItemStack worn = WornAmmo.wornQuiver(player);
        if (!worn.isEmpty()) return worn;
        var inventory = player.getInventory();
        for (int i = 0; i < MAIN_SLOTS && i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (QuiverItems.isQuiver(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    /** Drains the emptiest sources first and returns the FE actually moved into {@code quiver}. */
    private static int pull(ServerPlayer player, ItemStack quiver, int want) {
        List<Cell> cells = new ArrayList<>();
        var inventory = player.getInventory();
        for (int i = 0; i < MAIN_SLOTS && i < inventory.getContainerSize(); i++) {
            collect(cells, inventory.getItem(i), quiver);
        }
        IItemHandler back = WornAmmo.back(player);
        if (back != null) {
            for (int i = 0; i < back.getSlots(); i++) collect(cells, back.getStackInSlot(i), quiver);
        }
        cells.sort(Comparator.comparingInt(Cell::stored));
        int pulled = 0;
        for (Cell cell : cells) {
            if (pulled >= want) break;
            int moved = cell.storage().extractEnergy(want - pulled, false);
            if (moved > 0) pulled += moved;
        }
        return pulled;
    }

    private static void collect(List<Cell> cells, ItemStack stack, ItemStack quiver) {
        if (stack.isEmpty() || stack == quiver || QuiverItems.isQuiver(stack)) return;
        IEnergyStorage storage = stack.getCapability(ForgeCapabilities.ENERGY).resolve().orElse(null);
        if (storage == null || !storage.canExtract()) return;
        int stored = storage.getEnergyStored();
        if (stored <= 0) return;
        cells.add(new Cell(storage, stored));
    }

    private record Cell(IEnergyStorage storage, int stored) {}
}
