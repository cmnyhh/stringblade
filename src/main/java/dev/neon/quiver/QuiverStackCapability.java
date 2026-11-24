package dev.neon.quiver;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * Both stack capabilities of one Arc Quiver, attached by {@code AttachCapabilitiesEvent<ItemStack>}.
 *
 * <p>1.20.1 Forge has no {@code RegisterCapabilitiesEvent#registerItem}: item capabilities are
 * attached per stack through {@link net.minecraftforge.event.AttachCapabilitiesEvent}. Attaching
 * here (instead of probing {@code stack.getItem()} in a shared {@code Item#initCapabilities}) keeps
 * every other TaCZ ammo box capability-free, which is what makes the Curios "back" slot reject
 * them without touching TaCZ's item class.</p>
 */
public final class QuiverStackCapability implements ICapabilityProvider {
    private final ItemStack stack;
    private final IEnergyStorage energy;
    private final LazyOptional<IEnergyStorage> energyHolder;
    private final ICurio curio;
    private final LazyOptional<ICurio> curioHolder;

    public QuiverStackCapability(ItemStack stack) {
        this.stack = stack;
        this.energy = new QuiverEnergyStorage(stack);
        this.energyHolder = LazyOptional.of(() -> this.energy);
        this.curio = new BackOnlyCurio(stack);
        this.curioHolder = LazyOptional.of(() -> this.curio);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
                                                      @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) return energyHolder.cast();
        if (capability == CuriosCapability.ITEM) return curioHolder.cast();
        return LazyOptional.empty();
    }

    /** Wearable, but the quiver only hangs on the back. */
    private record BackOnlyCurio(ItemStack getStack) implements ICurio {
        @Override
        public boolean canEquip(SlotContext slotContext) {
            return "back".equals(slotContext.identifier());
        }

        @Override
        public boolean canEquipFromUse(SlotContext slotContext) {
            return canEquip(slotContext);
        }
    }
}
