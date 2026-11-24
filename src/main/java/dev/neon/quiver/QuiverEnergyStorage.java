package dev.neon.quiver;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * The {@code ForgeCapabilities.ENERGY} view of one Arc Quiver stack.
 *
 * <p>Every transfer goes through {@link QuiverEnergy}, so a standard FE charger, battery or pipe
 * can top the quiver up and TaCZ's {@code AmmoCount} follows immediately. Extraction is allowed
 * because this is a real storage item, and it drains shots the same way a shot does.</p>
 */
public final class QuiverEnergyStorage implements IEnergyStorage {
    private final ItemStack stack;

    public QuiverEnergyStorage(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        if (toReceive <= 0) return 0;
        int stored = QuiverEnergy.getEnergy(stack);
        int accepted = Math.min(toReceive, QuiverEnergy.CAPACITY - stored);
        if (accepted <= 0) return 0;
        if (!simulate) QuiverEnergy.setEnergy(stack, stored + accepted);
        return accepted;
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        if (toExtract <= 0) return 0;
        int stored = QuiverEnergy.getEnergy(stack);
        int drained = Math.min(toExtract, stored);
        if (drained <= 0) return 0;
        if (!simulate) QuiverEnergy.setEnergy(stack, stored - drained);
        return drained;
    }

    @Override
    public int getEnergyStored() {
        return QuiverEnergy.getEnergy(stack);
    }

    @Override
    public int getMaxEnergyStored() {
        return QuiverEnergy.CAPACITY;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
