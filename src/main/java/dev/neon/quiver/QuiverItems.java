package dev.neon.quiver;

import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.init.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Identity helpers for the Arc Quiver: a TaCZ ammo box marked with {@link #MODEL_DATA}.
 *
 * <p>1.20.1 has no data components, so the marker lives in the stack's NBT {@code CustomModelData}
 * tag (the same key the vanilla item model reads), and the display name is the plain
 * {@code display.Name} tag written by {@link ItemStack#setHoverName(Component)}.</p>
 */
public final class QuiverItems {
    /** Marker shared by the renderer, the Curios predicate and the creative entry. */
    public static final int MODEL_DATA = 765431;
    /** Vanilla's model-selection tag. */
    public static final String MODEL_DATA_TAG = "CustomModelData";

    private QuiverItems() {}

    public static boolean isQuiver(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IAmmoBox)) return false;
        // Read the existing tag only: probing must never mint NBT on unrelated ammo boxes.
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getInt(MODEL_DATA_TAG) == MODEL_DATA;
    }

    /** An empty, fully assembled quiver, as the creative tab and the crafting result provide it. */
    public static ItemStack emptyQuiver() {
        ItemStack stack = new ItemStack(ModItems.AMMO_BOX.get());
        stack.getOrCreateTag().putInt(MODEL_DATA_TAG, MODEL_DATA);
        stack.setHoverName(Component.translatable("neon.quiver.name"));
        if (stack.getItem() instanceof IAmmoBox box) {
            box.setAmmoLevel(stack, 0);
            // Normalise the tags first: at zero shots the whole "empty" convention clears the ammo id.
            QuiverEnergy.setEnergy(stack, 0);
            // A fresh quiver is still the bow's quiver, so it keeps its type until it is charged.
            box.setAmmoId(stack, QuiverEnergy.AMMO_ID);
        }
        return stack;
    }
}
