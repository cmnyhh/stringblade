package dev.neon.quiver.mixin;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TaCZ spawns every projectile at the shooter's eye. On a gun the muzzle sits near the eye line, but
 * this bow draws its arrow at the middle of the riser, well below it, so shots appeared to leave from
 * above the bow. Rounds of this bow are re-anchored on the arrow line before the entity is added.
 */
@Mixin(value = EntityKineticBullet.class, remap = false)
abstract class BulletSpawnMixin {
    /** TaCZ captures this at the eye during construction; the tracer is drawn from it. */
    @Shadow private Vec3 startPos;

    private static final ResourceLocation NEON_BOW = ResourceLocation.parse("neon:cyber_bow");
    /**
     * How far below the eye the round leaves, in model units of 1/16 block.
     * The gap between the eye line (y 9.50) and the arrow (y 4.14) is 5.36 units; the first-person
     * model is drawn slightly smaller than camera space, so the visible origin sits a little higher.
     */
    private static final double DROP = 2.40 / 16.0;
    /** Nudged towards the screen-right so the light sits on the drawn arrow rather than left of it. */
    private static final double SIDE = 1.28 / 16.0;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;"
            + "Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;"
            + "Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/resources/ResourceLocation;"
            + "Lnet/minecraft/resources/ResourceLocation;Z"
            + "Lcom/tacz/guns/resource/pojo/data/gun/GunData;"
            + "Lcom/tacz/guns/resource/pojo/data/gun/BulletData;)V", at = @At("TAIL"))
    private void neonquiver$dropToArrowLine(EntityType<?> type, Level level, LivingEntity shooter,
                                            ItemStack gunItem, ResourceLocation ammoId, ResourceLocation gunId,
                                            ResourceLocation gunDisplayId, boolean isTracerAmmo,
                                            GunData gunData, BulletData bulletData, CallbackInfo ci) {
        if (!NEON_BOW.equals(gunId)) return;
        Entity self = (Entity) (Object) this;
        Vec3 look = shooter.getLookAngle();
        // Shooter-local "down": the bow rides the pitch, so a world-space offset would drift when aiming up.
        double rx = look.z, rz = -look.x;
        double rl = Math.sqrt(rx * rx + rz * rz);
        double dx, dy, dz;
        if (rl < 1.0E-4) {
            dx = 0.0; dy = -DROP; dz = 0.0;      // looking straight up or down
        } else {
            rx /= rl; rz /= rl;
            // right x look already points down for every yaw/pitch
            double ux = -rz * look.y;
            double uy = rz * look.x - rx * look.z;
            double uz = rx * look.y;
            // screenRight = (-look.z, 0, look.x)
            double sx = -look.z / Math.sqrt(look.x * look.x + look.z * look.z);
            double sz = look.x / Math.sqrt(look.x * look.x + look.z * look.z);
            dx = ux * DROP + sx * SIDE;
            dy = uy * DROP;
            dz = uz * DROP + sz * SIDE;
        }
        self.setPos(self.getX() + dx, self.getY() + dy, self.getZ() + dz);
        this.startPos = self.position();
    }
}
