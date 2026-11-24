package dev.neon.quiver;

import com.tacz.guns.api.item.IGun;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Ballistics fixes for {@code neon:cyber_bow}, kept out of the mixins so the two injection points
 * (spawn and launch) share one implementation.
 *
 * <ul>
 *   <li>{@link #spawnOffset(LivingEntity)}: TaCZ spawns every projectile at the shooter's eye. On a
 *       gun the muzzle sits near the eye line, but this bow draws its arrow at the middle of the
 *       riser, well below it, so shots appeared to leave from above the bow.</li>
 *   <li>{@link #zeroAim(Projectile)}: the round now starts on the arrow line, below the sight, so
 *       firing it straight along the look vector would leave the whole trajectory that far under
 *       the crosshair. A real sight is zeroed at a range instead, so the round is re-aimed at the
 *       point the crosshair covers at {@link #ZERO_DISTANCE}.</li>
 * </ul>
 */
public final class QuiverAim {
    public static final ResourceLocation NEON_BOW = new ResourceLocation("neon", "cyber_bow");

    /**
     * How far below the eye the round leaves, in model units of 1/16 block.
     * The gap between the eye line (y 9.50) and the arrow (y 4.14) is 5.36 units; the first-person
     * model is drawn slightly smaller than camera space, so the visible origin sits a little higher.
     */
    private static final double DROP = 2.40 / 16.0;
    /** Nudged towards the screen-right so the light sits on the drawn arrow rather than left of it. */
    private static final double SIDE = 1.28 / 16.0;

    /** Rounds cross the sight line here; inside this range they sit slightly low, beyond it slightly high. */
    private static final double ZERO_DISTANCE = 16.0;

    private QuiverAim() {}

    /**
     * Offset from the eye to the arrow, expressed in the shooter's own basis: a world-space offset
     * would drift when aiming up, because the bow rides the pitch.
     */
    public static Vec3 spawnOffset(LivingEntity shooter) {
        Vec3 look = shooter.getLookAngle();
        // Shooter-local "down": right x look.
        double rx = look.z;
        double rz = -look.x;
        double rl = Math.sqrt(rx * rx + rz * rz);
        if (rl < 1.0E-4) {
            // Looking straight up or down: the basis is degenerate, so only the plain drop applies.
            return new Vec3(0.0, -DROP, 0.0);
        }
        rx /= rl;
        rz /= rl;
        // right x look already points down for every yaw/pitch
        double ux = -rz * look.y;
        double uy = rz * look.x - rx * look.z;
        double uz = rx * look.y;
        // screenRight = (-look.z, 0, look.x)
        double sx = -look.z / Math.sqrt(look.x * look.x + look.z * look.z);
        double sz = look.x / Math.sqrt(look.x * look.x + look.z * look.z);
        return new Vec3(ux * DROP + sx * SIDE, uy * DROP, uz * DROP + sz * SIDE);
    }

    /**
     * Re-aims the round already in flight at the crosshair's {@link #ZERO_DISTANCE}-block point.
     *
     * <p>The existing velocity is <em>rotated</em> rather than replaced, so TaCZ's inaccuracy spread
     * and the charged speed survive untouched.</p>
     */
    public static void zeroAim(Projectile bullet) {
        Entity owner = bullet.getOwner();
        if (!(owner instanceof LivingEntity shooter)) return;
        ItemStack held = shooter.getMainHandItem();
        if (!(held.getItem() instanceof IGun gun) || !NEON_BOW.equals(gun.getGunId(held))) return;

        Vec3 velocityNow = bullet.getDeltaMovement();
        if (velocityNow.lengthSqr() < 1.0E-9) return;
        Vec3 aimPoint = shooter.getEyePosition().add(shooter.getLookAngle().scale(ZERO_DISTANCE));
        Vec3 wanted = aimPoint.subtract(bullet.position());
        if (wanted.lengthSqr() < 1.0E-9) return;

        Vec3 from = velocityNow.normalize();
        Vec3 to = wanted.normalize();
        Quaternionf turn = new Quaternionf().rotationTo(
                (float) from.x, (float) from.y, (float) from.z,
                (float) to.x, (float) to.y, (float) to.z);
        Vector3f corrected = turn.transform(new Vector3f(
                (float) velocityNow.x, (float) velocityNow.y, (float) velocityNow.z));
        bullet.setDeltaMovement(corrected.x, corrected.y, corrected.z);
    }
}
