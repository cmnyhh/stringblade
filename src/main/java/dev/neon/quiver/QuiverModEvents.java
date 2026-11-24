package dev.neon.quiver;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;

/** Mod-bus wiring: the Curios slot predicate and the creative entry. */
@Mod.EventBusSubscriber(modid = NeonQuiver.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class QuiverModEvents {
    /** Referenced by {@code data/neonquiver/curios/slots/back.json} as a validator. */
    public static final ResourceLocation QUIVER_PREDICATE = new ResourceLocation(NeonQuiver.MOD_ID, "quiver");

    private QuiverModEvents() {}

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CuriosApi.registerCurioPredicate(QUIVER_PREDICATE,
                result -> QuiverItems.isQuiver(result.stack())));
    }

    /** TaCZ only lists generic ammo boxes, so the assembled quiver needs its own tab entry. */
    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().equals(new ResourceLocation("tacz", "ammo"))) {
            event.accept(QuiverItems.emptyQuiver());
        }
    }
}
