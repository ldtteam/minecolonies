package com.minecolonies.coremod.client.render;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.client.model.ModelEntityFemaleCitizen;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer for the citizens.
 */
public class RenderBipedCitizen extends MobRenderer<AbstractEntityCitizen, CitizenModel<AbstractEntityCitizen>>
{
    private static final double  SHADOW_SIZE   = 0.5F;
    public static        boolean isItGhostTime = false;

    /**
     * Renders model, see {@link BipedRenderer}.
     *
     * @param renderManagerIn the RenderManager for this Renderer.
     */
    public RenderBipedCitizen(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new CitizenModel<>(0.0F), (float) SHADOW_SIZE);
        super.addLayer(new BipedArmorLayer<>(this, new CitizenModel<>(0.5F), new CitizenModel<>(1.0F)));
        super.addLayer(new HeldItemLayer<>(this));
    }

    @Override
    public void render(
      @NotNull final AbstractEntityCitizen citizen,
      final float limbSwing,
      final float partialTicks,
      @NotNull final MatrixStack matrixStack,
      @NotNull final IRenderTypeBuffer renderTypeBuffer,
      final int light)
    {
        setupMainModelFrom(citizen);

        final CitizenModel<AbstractEntityCitizen> citizenModel = model;

        final ItemStack mainHandStack = citizen.getMainHandItem();
        final ItemStack offHandStack = citizen.getOffhandItem();
        final BipedModel.ArmPose armPoseMainHand = getArmPoseFrom(citizen, mainHandStack);
        final BipedModel.ArmPose armPoseOffHand = getArmPoseFrom(citizen, offHandStack);

        updateArmPose(citizen, citizenModel, armPoseMainHand, armPoseOffHand);

        if (isItGhostTime)
        {
            RenderSystem.enableBlend();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.3F);

            super.render(citizen, limbSwing, partialTicks, matrixStack, renderTypeBuffer, light);

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1F);

            RenderSystem.disableBlend();
        }
        else
        {
            super.render(citizen, limbSwing, partialTicks, matrixStack, renderTypeBuffer, light);
        }
    }

    private void setupMainModelFrom(@NotNull final AbstractEntityCitizen citizen)
    {
        model = citizen.isFemale() ? citizen.getModelType().getFemaleModel() : citizen.getModelType().getMaleModel();
        if (model == null)
        {
            model = (citizen.isFemale() ? new ModelEntityFemaleCitizen() : new CitizenModel<>(0.0F));
        }

        if (citizen.getCitizenDataView() != null && citizen.getCitizenDataView().getCustomTexture() != null)
        {
            model = new CitizenModel<>(false);
        }

        model.young = citizen.isBaby();
        model.riding = citizen.getVehicle() != null;
        model.attackTime = citizen.attackAnim;
    }

    @Override
    protected void renderNameTag(
      @NotNull final AbstractEntityCitizen entityIn,
      @NotNull final ITextComponent str,
      @NotNull final MatrixStack matrixStack,
      @NotNull final IRenderTypeBuffer buffer,
      final int maxDistance)
    {
        super.renderNameTag(entityIn, str, matrixStack, buffer, maxDistance);

        if (entityIn.getCitizenDataView() != null && entityIn.getCitizenDataView().hasVisibleInteractions())
        {
            double distance = this.entityRenderDispatcher.distanceToSqr(entityIn.getX(), entityIn.getY(), entityIn.getZ());
            if (distance <= 4096.0D)
            {
                double yOffset = model.young ? -0.8 : 0;
                boolean isSneaking = entityIn.isShiftKeyDown();
                double height = entityIn.getBbHeight() + 0.5F - (isSneaking ? 0.25F : 0.0F);
                double y = height + 0.3 + yOffset;

                final ResourceLocation texture = entityIn.getCitizenDataView().getInteractionIcon();

                matrixStack.pushPose();
                matrixStack.translate(0, y, 0);
                matrixStack.mulPose(entityRenderDispatcher.cameraOrientation());
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(90));

                matrixStack.scale(-0.025F, -0.025F, 0.025F);

                final Matrix4f matrix = matrixStack.last().pose();
                final IVertexBuilder r = buffer.getBuffer(MRenderTypes.customTextRenderer(texture));

                r.vertex(matrix, 0, 0, 0).uv(0, 0).uv2(250).endVertex();
                r.vertex(matrix, 0, 10, 0).uv(1, 0).uv2(250).endVertex();
                r.vertex(matrix, 10, 10, 0).uv(1, 1).uv2(250).endVertex();
                r.vertex(matrix, 10, 0, 0).uv(0, 1).uv2(250).endVertex();
                matrixStack.popPose();
            }
        }
    }

    private BipedModel.ArmPose getArmPoseFrom(@NotNull final AbstractEntityCitizen citizen, final ItemStack mainHandStack)
    {
        final UseAction enumActionMainHand;
        BipedModel.ArmPose pose = BipedModel.ArmPose.EMPTY;
        if (!mainHandStack.isEmpty())
        {
            pose = BipedModel.ArmPose.ITEM;
            if (citizen.getUseItemRemainingTicks() > 0)
            {
                enumActionMainHand = mainHandStack.getUseAnimation();
                if (enumActionMainHand == UseAction.BLOCK)
                {
                    pose = BipedModel.ArmPose.BLOCK;
                }
                else if (enumActionMainHand == UseAction.BOW)
                {
                    pose = BipedModel.ArmPose.BOW_AND_ARROW;
                }
            }
        }
        return pose;
    }

    private void updateArmPose(
      @NotNull final AbstractEntityCitizen citizen,
      final BipedModel<AbstractEntityCitizen> citizenModel,
      final BipedModel.ArmPose armPoseMainHand,
      final BipedModel.ArmPose armPoseOffHand)
    {
        if (citizen.getMainArm() == HandSide.RIGHT)
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
