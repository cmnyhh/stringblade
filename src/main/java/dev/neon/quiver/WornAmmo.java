package dev.neon.quiver;

import com.tacz.guns.api.item.IAmmoBox;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/** A short-lived view owned by the shooter. TaCZ edits native ammo box components in place. */
public final class WornAmmo implements IItemHandler {
    private final IItemHandler inventory;
    private final IItemHandler back;
    private final int split;

    private WornAmmo(IItemHandler inventory, IItemHandler back) {
        this.inventory = inventory;
        this.back = back;
        this.split = inventory.getSlots();
    }

    private static IItemHandler back(LivingEntity shooter) {
        if (!(shooter instanceof Player)) return null;
        return CuriosApi.getCuriosInventory(shooter).flatMap(curios -> curios.getStacksHandler("back"))
                .map(ICurioStacksHandler::getStacks).orElse(null);
    }

    public static IItemHandler append(IItemHandler inventory, LivingEntity shooter) {
        if (inventory == null) return null;
        var back = back(shooter);
        return back == null ? inventory : new WornAmmo(inventory, back);
    }

    private static boolean supplies(ItemStack stack) {
        return QuiverItems.isQuiver(stack) && ((IAmmoBox) stack.getItem()).getAmmoCount(stack) > 0;
    }

    public static int addHudCount(int count, Player player, ItemStack gun) {
        var back = back(player);
        if (back == null) return count;
        for (int i = 0; i < back.getSlots(); i++) {
            ItemStack stack = back.getStackInSlot(i);
            if (!supplies(stack)) continue;
            IAmmoBox box = (IAmmoBox) stack.getItem();
            if (box.isAmmoBoxOfGun(gun, stack)) count += Math.min(9999 - count, box.getAmmoCount(stack));
        }
        return count;
    }

    @Override public int getSlots() { return split + back.getSlots(); }

    @Override public ItemStack getStackInSlot(int slot) {
        if (slot < split) return inventory.getStackInSlot(slot);
        ItemStack stack = back.getStackInSlot(slot - split);
        return supplies(stack) ? stack : ItemStack.EMPTY;
    }

    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return slot < split ? inventory.insertItem(slot, stack, simulate) : stack;
    }

    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return slot < split ? inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override public int getSlotLimit(int slot) { return slot < split ? inventory.getSlotLimit(slot) : 1; }
    @Override public boolean isItemValid(int slot, ItemStack stack) {
        return slot < split && inventory.isItemValid(slot, stack);
    }
}
