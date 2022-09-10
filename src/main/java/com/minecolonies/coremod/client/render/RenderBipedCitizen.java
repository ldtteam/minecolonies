package com.minecolonies.coremod.client.render;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.apiimp.initializer.ModModelTypeInitializer;
import com.minecolonies.coremod.client.render.worldevent.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Renderer for the citizens.
 */
public class RenderBipedCitizen extends MobRenderer<AbstractEntityCitizen, CitizenModel<AbstractEntityCitizen>>
{
    private static final double  SHADOW_SIZE   = 0.5F;
    public static        boolean isItGhostTime = false;

    /**
     * Renders model, see {@link MobRenderer}.
     *
     * @param context the context for this Renderer.
     */
    public RenderBipedCitizen(final EntityRendererProvider.Context context)
    {
        super(context, new CitizenModel<>(context.bakeLayer(ModelLayers.PLAYER)), (float) SHADOW_SIZE);
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR))));
        super.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        ModModelTypeInitializer.init(context);
    }

    @Override
    public void render(
      @NotNull final AbstractEntityCitizen citizen,
      final float limbSwing,
      final float partialTicks,
      @NotNull final PoseStack matrixStack,
      @NotNull final MultiBufferSource renderTypeBuffer,
      final int light)
    {

        setupMainModelFrom(citizen);

        final CitizenModel<AbstractEntityCitizen> citizenModel = model;

        final ItemStack mainHandStack = citizen.getMainHandItem();
        final ItemStack offHandStack = citizen.getOffhandItem();
        final HumanoidModel.ArmPose armPoseMainHand = getArmPoseFrom(citizen, mainHandStack);
        final HumanoidModel.ArmPose armPoseOffHand = getArmPoseFrom(citizen, offHandStack);

        updateArmPose(citizen, citizenModel, armPoseMainHand, armPoseOffHand);

        if (isItGhostTime)
        {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.3F);

            super.render(citizen, limbSwing, partialTicks, matrixStack, renderTypeBuffer, light);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            RenderSystem.disableBlend();
        }
        else
        {
            super.render(citizen, limbSwing, partialTicks, matrixStack, renderTypeBuffer, light);
        }
    }

    private void setupMainModelFrom(@NotNull final AbstractEntityCitizen citizen)
    {
        final @Nullable IModelType modelType = IModelTypeRegistry.getInstance().getModelType(citizen.getModelType());
        model = citizen.isFemale() ? modelType.getFemaleModel() : modelType.getMaleModel();
        if (model == null)
        {
            //no if base, or the next condition, get player model!
            model = citizen.isFemale() ? modelType.getFemaleModel() : modelType.getMaleModel();
        }

        if (citizen.getCitizenDataView() != null && citizen.getCitizenDataView().getCustomTexture() != null)
        {
            model = IModelTypeRegistry.getInstance().getModelType(ModModelTypes.CUSTOM_ID).getMaleModel();
        }

        model.young = citizen.isBaby();
        model.riding = citizen.getVehicle() != null;
        model.attackTime = citizen.attackAnim;
    }

    @Override
    protected void renderNameTag(
      @NotNull final AbstractEntityCitizen entityIn,
      @NotNull final Component str,
      @NotNull final PoseStack matrixStack,
      @NotNull final MultiBufferSource buffer,
      final int packedLight)
    {
        super.renderNameTag(entityIn, str, matrixStack, buffer, packedLight);

        if (entityIn.getCitizenDataView() != null && entityIn.getCitizenDataView().hasVisibleInteractions())
        {
            double distance = this.entityRenderDispatcher.distanceToSqr(entityIn.getX(), entityIn.getY(), entityIn.getZ());
            if (distance <= 4096.0D)
            {
                double yOffset = model.young ? -0.3 : 0;
                boolean isSneaking = entityIn.isShiftKeyDown();
                double height = entityIn.getBbHeight() + 0.5F - (isSneaking ? 0.25F : 0.0F);
                double y = height + 0.3 + yOffset;

                final ResourceLocation texture = entityIn.getCitizenDataView().getInteractionIcon();

                matrixStack.pushPose();
                matrixStack.translate(0, y, 0);
                matrixStack.mulPose(entityRenderDispatcher.cameraOrientation());
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(90));

                matrixStack.scale(-0.025F, -0.025F, 0.025F);

                VertexConsumer r = buffer.getBuffer(RenderTypes.getEntityCutoutFront(texture));

                final Matrix4f pose = matrixStack.last().pose();
                final Matrix3f norm = matrixStack.last().normal();
                
                r.vertex(pose, 0, 0, 0).color(255, 255, 255, 255).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(norm, 0, 1, 0).endVertex();
                r.vertex(pose, 0, 10, 0).color(255, 255, 255, 255).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(norm, 0, 1, 0).endVertex();
                r.vertex(pose, 10, 10, 0).color(255, 255, 255, 255).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(norm, 0, 1, 0).endVertex();
                r.vertex(pose, 10, 0, 0).color(255, 255, 255, 255).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(norm, 0, 1, 0).endVertex();
                
                matrixStack.popPose();
            }
        }
    }

    private HumanoidModel.ArmPose getArmPoseFrom(@NotNull final AbstractEntityCitizen citizen, final ItemStack mainHandStack)
    {
        final UseAnim enumActionMainHand;
        HumanoidModel.ArmPose pose = HumanoidModel.ArmPose.EMPTY;
        if (!mainHandStack.isEmpty())
        {
            pose = HumanoidModel.ArmPose.ITEM;
            if (citizen.getUseItemRemainingTicks() > 0)
            {
                enumActionMainHand = mainHandStack.getUseAnimation();
                if (enumActionMainHand == UseAnim.BLOCK)
                {
                    pose = HumanoidModel.ArmPose.BLOCK;
                }
                else if (enumActionMainHand == UseAnim.BOW)
                {
                    pose = HumanoidModel.ArmPose.BOW_AND_ARROW;
                }
            }
        }
        return pose;
    }

    private void updateArmPose(
      @NotNull final AbstractEntityCitizen citizen,
      final HumanoidModel<AbstractEntityCitizen> citizenModel,
      final HumanoidModel.ArmPose armPoseMainHand,
      final HumanoidModel.ArmPose armPoseOffHand)
    {
        if (citizen.getMainArm() == HumanoidArm.RIGHT)
        {
            citizenModel.rightArmPose = armPoseMainHand;
            citizenModel.leftArmPose = armPoseOffHand;
        }
        else
        {
            citizenModel.rightArmPose = armPoseOffHand;
            citizenModel.leftArmPose = armPoseMainHand;
        }
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(final AbstractEntityCitizen entity)
    {
        if (entity.getCitizenDataView() != null && entity.getCitizenDataView().getCustomTexture() != null)
        {
            return entity.getCitizenDataView().getCustomTexture();
        }
        return entity.getTexture();
    }
}
