package dev.neon.quiver.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.neon.quiver.QuiverItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * Draws the worn Arc Quiver on the player's back.
 *
 * <p>Renders the stack's own baked item model — the exact model the inventory icon already uses —
 * instead of a separately registered standalone model. A standalone {@code #standalone} MRL is baked
 * lazily by model-pipeline mods (ModernFix's dynamic resources, for one) and falls back to blockstate
 * lookup, which fails with {@code Unknown blockstate property: 'standalone'} and leaves the missing
 * model on the player's back (the purple cube).</p>
 */
@Mod.EventBusSubscriber(modid = "neonquiver", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class QuiverBackLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private QuiverBackLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new QuiverBackLayer(playerRenderer));
            }
        }
    }

    private static ItemStack visibleQuiver(ICurioStacksHandler back) {
        var equipped = back.getStacks();
        var cosmetics = back.getCosmeticStacks();
        var renders = back.getRenders();
        for (int i = 0; i < equipped.getSlots(); i++) {
            ItemStack stack = cosmetics.getStackInSlot(i);
            if (stack.isEmpty()) {
                if (i >= renders.size() || !renders.get(i)) continue;
                stack = equipped.getStackInSlot(i);
            }
            if (QuiverItems.isQuiver(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void render(PoseStack poses, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                       float limbSwing, float limbAmount, float partialTick, float age, float yaw, float pitch) {
        if (player.isInvisible() || player.isSpectator() || !getParentModel().body.visible) return;
        ItemStack stack = CuriosApi.getCuriosInventory(player)
                .resolve()
                .flatMap(handler -> handler.getStacksHandler("back"))
                .map(QuiverBackLayer::visibleQuiver)
                .orElse(ItemStack.EMPTY);
        if (stack.isEmpty()) return;

        poses.pushPose();
        try {
            // Body includes the animated crouch pivot and tilt. Coordinates below are in player-model pixels.
            getParentModel().body.translateAndRotate(poses);
            float backDepth = player.getItemBySlot(EquipmentSlot.CHEST).isEmpty() ? 3.65F : 4.35F;
            poses.translate(0, 5.5F / 16, backDepth / 16);
            // The source model's feed is at low Y. Player-model Y increases downward,
            // so low item Y places the feed over the shoulder and the closed cap by the hip.
            poses.mulPose(Axis.ZP.rotationDegrees(-32));
            poses.mulPose(Axis.YP.rotationDegrees(90));  // Textured broad face points toward the rear.
            poses.scale(1.25F, 1.25F, 1.25F);
            var client = Minecraft.getInstance();
            // The stack's own model (CustomModelData override included), resolved exactly like the
            // inventory icon, so no standalone-MRL registration can go missing.
            var model = client.getItemRenderer().getModel(stack, player.level(), player, 0);
            client.getItemRenderer().render(stack, ItemDisplayContext.NONE, false, poses, buffers,
                    light, OverlayTexture.NO_OVERLAY, model);
        } finally {
            poses.popPose();
        }
    }
}
