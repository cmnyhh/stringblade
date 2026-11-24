package dev.neon.quiver;

import com.tacz.guns.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

@Mod("neonquiver")
public final class NeonQuiver {
    public NeonQuiver(IEventBus bus) {
        bus.addListener(NeonQuiver::registerPredicate);
        bus.addListener(NeonQuiver::registerCapability);
        bus.addListener(NeonQuiver::addCreative);
        NeoForge.EVENT_BUS.addListener(QuiverCharge::onStackedOn);
        NeoForge.EVENT_BUS.addListener(QuiverCharge::onRightClickItem);
    }

    private static void registerPredicate(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CuriosApi.registerCurioPredicate(
                ResourceLocation.parse("neonquiver:quiver"), result -> QuiverItems.isQuiver(result.stack())));
    }

    /** TaCZ only lists generic ammo boxes, so the assembled quiver needs its own tab entry. */
    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().equals(ResourceLocation.fromNamespaceAndPath("tacz", "ammo"))) {
            event.accept(QuiverItems.emptyQuiver());
        }
    }

    private static void registerCapability(RegisterCapabilitiesEvent event) {
        event.registerItem(CuriosCapability.ITEM, (stack, ignored) ->
                QuiverItems.isQuiver(stack) ? new Wearable(stack) : null, ModItems.AMMO_BOX.get());
        // FE lives on the stack, so a charger reaches it in the main inventory and in the Curios
        // "back" slot alike: native ammo boxes that are not our quiver keep returning null.
        event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, ignored) ->
                QuiverItems.isQuiver(stack) ? new QuiverEnergyStorage(stack) : null, ModItems.AMMO_BOX.get());
    }

    private record Wearable(ItemStack getStack) implements ICurio {
        @Override public boolean canEquip(SlotContext slot) { return slot.identifier().equals("back"); }
        @Override public boolean canEquipFromUse(SlotContext slot) { return canEquip(slot); }
    }
}
