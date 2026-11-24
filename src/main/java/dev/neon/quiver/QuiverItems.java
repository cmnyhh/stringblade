package dev.neon.quiver;

import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.init.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

public final class QuiverItems {
    /** Marker shared by the renderer, the Curios predicate and the creative entry. */
    public static final int MODEL_DATA = 765431;

    private QuiverItems() {}

    public static boolean isQuiver(ItemStack stack) {
        var model = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        return model != null && model.value() == MODEL_DATA && stack.is(ModItems.AMMO_BOX.get());
    }

    /** An empty, fully assembled quiver, as the creative tab and the crafting result provide it. */
    public static ItemStack emptyQuiver() {
        ItemStack stack = new ItemStack(ModItems.AMMO_BOX.get());
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(MODEL_DATA));
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("neon.quiver.name"));
        IAmmoBox box = (IAmmoBox) stack.getItem();
        box.setAmmoLevel(stack, 0);
        // Normalise the tags first: at zero shots the whole "empty" convention clears the ammo id.
        QuiverEnergy.setEnergy(stack, 0);
        // A fresh quiver is still the bow's quiver, so it keeps its type until it is charged.
        box.setAmmoId(stack, QuiverEnergy.AMMO_ID);
        return stack;
    }
}
