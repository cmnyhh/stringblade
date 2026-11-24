package dev.neon.quiver.mixin;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The bow's rounds start on the arrow line, below the sight, so firing them straight along the look
 * vector would leave the whole trajectory that far under the crosshair. A real sight is zeroed at a
 * range instead, so the round is re-aimed at the point the crosshair covers at {@link #ZERO_DISTANCE}.
 *
 * <p>The existing velocity is <em>rotated</em> rather than replaced, so TaCZ's inaccuracy spread and
 * the charged speed survive untouched.</p>
 */
@Mixin(Projectile.class)
abstract class ProjectileZeroMixin {
    private static final ResourceLocation NEON_BOW = ResourceLocation.parse("neon:cyber_bow");
    /** Rounds cross the sight line here; inside this range they sit slightly low, beyond it slightly high. */
    private static final double ZERO_DISTANCE = 16.0;

    @Inject(method = "shoot(DDDFF)V", at = @At("TAIL"))
    private void neonquiver$zeroAtRange(double x, double y, double z, float velocity, float inaccuracy,
                                        CallbackInfo ci) {
        if (!((Object) this instanceof EntityKineticBullet bullet)) return;
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
