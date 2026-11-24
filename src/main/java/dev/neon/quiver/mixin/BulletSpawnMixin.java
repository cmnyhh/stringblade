package dev.neon.quiver.mixin;

import dev.neon.quiver.QuiverAim;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import org.joml.Vector2d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Re-anchors {@code neon:cyber_bow} rounds on the arrow line before the entity is added to the
 * world, and re-aims them once TaCZ has given them their (spread-rotated) velocity.
 *
 * <p>1.20.1 spawns bullets through this constructor from
 * {@code ModernKineticGunScriptAPI#shootOnce} (which this bow reaches via its Lua script), and the
 * launched velocity is written by TaCZ's own
 * {@code shootFromRotation(Entity, float, float, float, float, Vector2d)} - not by
 * {@code Projectile#shoot} - so that is where the zeroing is applied.</p>
 */
@Mixin(value = EntityKineticBullet.class, remap = false)
abstract class BulletSpawnMixin {
    /** TaCZ captures this at the eye during construction; the tracer is drawn from it. */
    @Shadow private Vec3 startPos;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;"
            + "Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;"
            + "Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/resources/ResourceLocation;"
            + "Lnet/minecraft/resources/ResourceLocation;Z"
            + "Lcom/tacz/guns/resource/pojo/data/gun/GunData;"
            + "Lcom/tacz/guns/resource/pojo/data/gun/BulletData;)V", at = @At("TAIL"))
    private void neonquiver$dropToArrowLine(EntityType<?> type, Level level, LivingEntity shooter,
                                            ItemStack gunItem, ResourceLocation ammoId,
                                            ResourceLocation gunId, ResourceLocation gunDisplayId,
                                            boolean isTracerAmmo, GunData gunData, BulletData bulletData,
                                            CallbackInfo ci) {
        if (!QuiverAim.NEON_BOW.equals(gunId)) return;
        Entity self = (Entity) (Object) this;
        Vec3 offset = QuiverAim.spawnOffset(shooter);
        self.setPos(self.getX() + offset.x, self.getY() + offset.y, self.getZ() + offset.z);
        this.startPos = self.position();
    }

    @Inject(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFLorg/joml/Vector2d;)V",
            at = @At("TAIL"))
    private void neonquiver$zeroAtRange(Entity shooter, float xRot, float yRot, float zRot,
                                        float velocity, Vector2d spread, CallbackInfo ci) {
        QuiverAim.zeroAim((Projectile) (Object) this);
    }
}
