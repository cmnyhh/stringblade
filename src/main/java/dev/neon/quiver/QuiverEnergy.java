package dev.neon.quiver;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.IAmmoBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * Forge Energy carried by a single Arc Quiver stack.
 *
 * <p>TaCZ keeps reading {@code AmmoCount} for its HUD, reload and empty-state logic, so that tag
 * stays the single source of truth for "how many shots are left". Energy is the reservoir and
 * {@code AmmoCount} is rewritten as {@code floor(energy / PER_SHOT)} at every write boundary:
 * FE charging, TaCZ extracting a round, and when a legacy (arrow-era) stack is first read.</p>
 *
 * <p>1.20.1 has no {@code DataComponents}: both keys are plain ints in the stack's tag compound,
 * exactly the way {@code AmmoBoxItemDataAccessor} reads and writes {@code AmmoCount} itself.</p>
 */
public final class QuiverEnergy {
    /** 12000 FE at 300 FE per shot = 40 shots. */
    public static final int CAPACITY = 12_000;
    public static final int PER_SHOT = 300;
    public static final int SHOTS = CAPACITY / PER_SHOT;

    /** Stored next to TaCZ's own {@code AmmoCount}. */
    public static final String ENERGY_TAG = "NeonEnergy";
    /** TaCZ's own key; never renamed, TaCZ reads it directly. */
    public static final String AMMO_COUNT_TAG = "AmmoCount";

    public static final ResourceLocation AMMO_ID = new ResourceLocation("neon", "cyber_quiver");

    private QuiverEnergy() {}

    /** Live tag; read-only, exactly the way TaCZ reads its own keys. */
    private static CompoundTag data(ItemStack stack) {
        return stack.getOrCreateTag();
    }

    /** Stored FE, or the legacy equivalent when the stack predates the FE conversion. */
    public static int getEnergy(ItemStack stack) {
        CompoundTag tag = data(stack);
        if (tag.contains(ENERGY_TAG, Tag.TAG_INT)) {
            return Mth.clamp(tag.getInt(ENERGY_TAG), 0, CAPACITY);
        }
        // Arrow-era stacks (and gun-smith-table results) only know AmmoCount: one arrow = one shot.
        return Mth.clamp(tag.getInt(AMMO_COUNT_TAG), 0, SHOTS) * PER_SHOT;
    }

    public static int getShots(ItemStack stack) {
        return getEnergy(stack) / PER_SHOT;
    }

    public static boolean isFull(ItemStack stack) {
        return getEnergy(stack) > CAPACITY - PER_SHOT;
    }

    /** Writes the reservoir, then mirrors it into TaCZ's AmmoCount / AmmoId. */
    public static void setEnergy(ItemStack stack, int energy) {
        int clamped = Mth.clamp(energy, 0, CAPACITY);
        data(stack).putInt(ENERGY_TAG, clamped);
        writeAmmoFields(stack, clamped / PER_SHOT);
    }

    /** @return FE actually accepted (clamped to capacity). */
    public static int addEnergy(ItemStack stack, int delta) {
        if (delta == 0) return 0;
        int before = getEnergy(stack);
        setEnergy(stack, before + delta);
        return getEnergy(stack) - before;
    }

    /**
     * TaCZ just wrote a new AmmoCount (it extracts rounds straight into the tag). Keep the exact
     * per-shot price: only the shots that actually changed move FE, so partial charge is not lost.
     */
    public static void afterAmmoCountWrite(ItemStack stack, int energyBefore, int newCount) {
        setEnergy(stack, energyBefore + (newCount - energyBefore / PER_SHOT) * PER_SHOT);
    }

    /**
     * Keeps the two TaCZ fields coherent with the reservoir: an empty quiver drops its AmmoId
     * (TaCZ's own "box no longer feeds this gun" convention, so the gun reports empty and the item
     * survives), a charged quiver re-arms it so TaCZ counts the shots again.
     */
    private static void writeAmmoFields(ItemStack stack, int shots) {
        if (!(stack.getItem() instanceof IAmmoBox box)) return;
        // Creative / all-type boxes are TaCZ's infinite-ammo items; never touch them.
        if (box.isCreative(stack) || box.isAllTypeCreative(stack)) return;

        if (box.getAmmoCount(stack) != shots) {
            box.setAmmoCount(stack, shots);
        }
        if (shots > 0) {
            if (!AMMO_ID.equals(box.getAmmoId(stack))) {
                box.setAmmoId(stack, AMMO_ID);
            }
        } else if (!DefaultAssets.EMPTY_AMMO_ID.equals(box.getAmmoId(stack))) {
            box.setAmmoId(stack, DefaultAssets.EMPTY_AMMO_ID);
        }
    }
}
