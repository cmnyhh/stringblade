package dev.neon.quiver;

import net.minecraftforge.fml.common.Mod;

/**
 * Stringblade / Arc Quiver for Forge 1.20.1.
 *
 * <p>The mod is almost entirely event driven ({@link QuiverModEvents} on the mod bus,
 * {@link QuiverGameEvents} on the forge bus) plus a handful of TaCZ mixins
 * ({@code dev.neon.quiver.mixin}). This class only exists so Forge has a {@code @Mod} entry point.</p>
 */
@Mod(NeonQuiver.MOD_ID)
public final class NeonQuiver {
    public static final String MOD_ID = "neonquiver";

    public NeonQuiver() {
        // Nothing to wire up by hand: both listener classes are @Mod.EventBusSubscriber.
    }
}
