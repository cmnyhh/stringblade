package dev.neon.quiver;

import com.tacz.guns.api.item.IAmmoBox;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * A short-lived view of "shooter inventory + the Curios back slot", owned by the shooter.
 * TaCZ edits native ammo box NBT in place, and a curio stack is the live stack, so a read-only
 * window onto it is enough for both counting and extraction.
 */
public final class WornAmmo implements IItemHandler {
    private final IItemHandler inventory;
    private final IItemHandler back;
    private final int split;

    private WornAmmo(IItemHandler inventory, IItemHandler back) {
        this.inventory = inventory;
        this.back = back;
        this.split = inventory.getSlots();
    }

    /** The Curios "back" handler of a player, or {@code null} when Curios has none. */
    public static IItemHandler back(LivingEntity shooter) {
        if (!(shooter instanceof Player)) return null;
        return CuriosApi.getCuriosInventory(shooter)
                .resolve()
                .flatMap(handler -> handler.getStacksHandler("back"))
                .<IItemHandler>map(ICurioStacksHandler::getStacks)
                .orElse(null);
    }

    /** The first quiver worn in the back slot, or an empty stack. */
    public static ItemStack wornQuiver(LivingEntity shooter) {
        IItemHandler back = back(shooter);
        if (back == null) return ItemStack.EMPTY;
        for (int i = 0; i < back.getSlots(); i++) {
            ItemStack stack = back.getStackInSlot(i);
            if (QuiverItems.isQuiver(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    public static IItemHandler append(IItemHandler inventory, LivingEntity shooter) {
        if (inventory == null) return null;
        IItemHandler back = back(shooter);
        return back == null ? inventory : new WornAmmo(inventory, back);
    }

    /**
     * 1.20.1 hands item handlers out as a {@link LazyOptional}, so the Curios window is folded into
     * the lazy result instead of replacing it: absent capabilities stay absent.
     */
    public static LazyOptional<?> includeBack(LazyOptional<?> inventory, LivingEntity shooter) {
        if (inventory == null) return null;
        // LazyOptional#map yields a plain Optional here; lazyMap keeps the capability contract.
        return inventory.lazyMap(handler -> append((IItemHandler) handler, shooter));
    }

    private static boolean supplies(ItemStack stack) {
        return QuiverItems.isQuiver(stack) && ((IAmmoBox) stack.getItem()).getAmmoCount(stack) > 0;
    }

    public static int addHudCount(int count, Player player, ItemStack gun) {
        IItemHandler back = back(player);
        if (back == null) return count;
        for (int i = 0; i < back.getSlots(); i++) {
            ItemStack stack = back.getStackInSlot(i);
            if (!supplies(stack)) continue;
            IAmmoBox box = (IAmmoBox) stack.getItem();
            if (box.isAmmoBoxOfGun(gun, stack)) count += Math.min(9999 - count, box.getAmmoCount(stack));
        }
        return count;
    }

    @Override
    public int getSlots() {
        return split + back.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < split) return inventory.getStackInSlot(slot);
        ItemStack stack = back.getStackInSlot(slot - split);
        return supplies(stack) ? stack : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return slot < split ? inventory.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return slot < split ? inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot < split ? inventory.getSlotLimit(slot) : 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot < split && inventory.isItemValid(slot, stack);
    }
}
