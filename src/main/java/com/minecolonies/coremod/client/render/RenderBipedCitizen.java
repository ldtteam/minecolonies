package com.minecolonies.coremod.client.render;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.model.ModelEntityFemaleCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.IVertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
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
import org.jetbrains.annotations.NotNull;

/**
 * Renderer for the citizens.
 */
public class RenderBipedCitizen extends MobRenderer<AbstractEntityCitizen, CitizenModel<AbstractEntityCitizen>>
{
    private static final double  SHADOW_SIZE    = 0.5F;
    private static final int     THREE_QUARTERS = 270;
    public static        boolean isItGhostTime  = false;

    /**
     * The resource location for the blocking overlay.
     */
    private static final ResourceLocation BLOCKING_RESOURCE = new ResourceLocation(Constants.MOD_ID, "textures/icons/blocking.png");

    /**
     * The resource location for the pending overlay.
     */
    private static final ResourceLocation PENDING_RESOURCE = new ResourceLocation(Constants.MOD_ID, "textures/icons/warning.png");

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

        final CitizenModel citizenModel = (CitizenModel) entityModel;

        final ItemStack mainHandStack = citizen.getHeldItemMainhand();
        final ItemStack offHandStack = citizen.getHeldItemOffhand();
        final BipedModel.ArmPose armPoseMainHand = getArmPoseFrom(citizen, mainHandStack, BipedModel.ArmPose.EMPTY);
        final BipedModel.ArmPose armPoseOffHand = getArmPoseFrom(citizen, offHandStack, BipedModel.ArmPose.EMPTY);

        // todo updateArmPose(citizen, citizenModel, armPoseMainHand, armPoseOffHand);
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
        entityModel = (citizen.isFemale()
                         ? IModelTypeRegistry.getInstance().getFemaleMap().get(citizen.getModelType())
                         : IModelTypeRegistry.getInstance().getMaleMap().get(citizen.getModelType()));
        if (entityModel == null)
        {
            entityModel = (citizen.isFemale() ? new ModelEntityFemaleCitizen() : new CitizenModel(0.0F));
        }

        entityModel.isChild = citizen.isChild();
        entityModel.isSitting = citizen.getRidingEntity() != null;
        entityModel.swingProgress = citizen.swingProgress;
    }

    @Override
    protected void renderLabelIfPresent(@NotNull final AbstractEntityCitizen entityIn, @NotNull final String str, @NotNull final MatrixStack matrixStack, @NotNull final IRenderTypeBuffer buffer, final int maxDistance)
    {
        super.renderLabelIfPresent(entityIn, str, matrixStack, buffer, maxDistance);

        if (entityIn instanceof EntityCitizen && ((EntityCitizen) entityIn).getCitizenDataView() != null && ((EntityCitizen) entityIn).getCitizenDataView().hasPendingInteractions())
        {
            double distance = this.renderManager.getSquaredDistanceToCamera(entityIn);
            if (distance <= 4096.0D)
            {
                double yOffset = entityModel.isChild ? -0.8 : 0;
                boolean isSneaking = entityIn.isSneaking();
                double height = entityIn.getHeight() + 0.5F - (isSneaking ? 0.25F : 0.0F);
                double y = height + 0.3 + yOffset;

                final ResourceLocation texture = ((EntityCitizen) entityIn).getCitizenDataView().hasBlockingInteractions()  ? BLOCKING_RESOURCE : PENDING_RESOURCE;

                matrixStack.push();
                matrixStack.translate(0, y, 0);
                matrixStack.multiply(renderManager.getRotation());
                matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(90));

                matrixStack.scale(-0.025F, -0.025F, 0.025F);

                final Matrix4f matrix = matrixStack.peek().getModel();
                final IVertexBuilder r = buffer.getBuffer(MRenderTypes.customTextRenderer(texture));

                r.vertex(matrix,0, 0, 0).texture(0, 0).light(250).endVertex();
                r.vertex(matrix,0, 10, 0).texture(1, 0).light(250).endVertex();
                r.vertex(matrix,10, 10, 0).texture(1, 1).light(250).endVertex();
                r.vertex(matrix,10, 0, 0).texture(0, 1).light(250).endVertex();
                matrixStack.pop();
            }
        }
    }

    private BipedModel.ArmPose getArmPoseFrom(@NotNull final AbstractEntityCitizen citizen, final ItemStack mainHandStack, BipedModel.ArmPose armPoseMainHand)
    {
        final UseAction enumActionMainHand;
        if (!mainHandStack.isEmpty())
        {
            armPoseMainHand = BipedModel.ArmPose.ITEM;
            if (citizen.getItemInUseCount() > 0)
            {
                enumActionMainHand = mainHandStack.getUseAction();
                if (enumActionMainHand == UseAction.BLOCK)
                {
                    armPoseMainHand = BipedModel.ArmPose.BLOCK;
                }
                else if (enumActionMainHand == UseAction.BOW)
                {
                    armPoseMainHand = BipedModel.ArmPose.BOW_AND_ARROW;
                }
            }
        }
        return armPoseMainHand;
    }

    private void updateArmPose(@NotNull final AbstractEntityCitizen citizen, final BipedModel citizenModel, final BipedModel.ArmPose armPoseMainHand, final BipedModel.ArmPose armPoseOffHand)
    {
        if (citizen.getPrimaryHand() == HandSide.RIGHT)
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

    /*@Override
    protected void renderLivingAt(final LivingEntity entity, final double x, final double y, final double z)
    {
        final AbstractEntityCitizen entityCitizen = (AbstractEntityCitizen) entity;
        if (entityCitizen.isAlive() && entityCitizen.getCitizenSleepHandler().isAsleep())
        {
            super.renderLivingAt(entity,
              x + (double) entityCitizen.getCitizenSleepHandler().getRenderOffsetX(),
              y + BED_HEIGHT,
              z + (double) entityCitizen.getCitizenSleepHandler().getRenderOffsetZ());
        }
        else
        {
            super.renderLivingAt(entity, x, y, z);
        }
    }

    @Override
    protected void applyRotations(final LivingEntity entityLiving, final float rotationHead, final float rotationYaw, final float partialTicks)
    {
        final AbstractEntityCitizen entityCitizen = (AbstractEntityCitizen) entityLiving;
        if (entityCitizen.isAlive() && entityCitizen.getCitizenSleepHandler().isAsleep())
        {
            RenderSystem.rotatef(entityCitizen.getCitizenSleepHandler().getBedOrientationInDegrees(), 0.0F, 1.0F, 0.0F);
            RenderSystem.rotatef(this.getDeathMaxRotation(entityLiving), 0.0F, 0.0F, 1.0F);
            RenderSystem.rotatef(THREE_QUARTERS, 0.0F, 1.0F, 0.0F);
        }
        else
        {
            super.applyRotations(entityLiving, rotationHead, rotationYaw, partialTicks);
        }
    }*/

    @NotNull
    @Override
    public ResourceLocation getEntityTexture(final AbstractEntityCitizen entity)
    {
        return entity.getTexture();
    }
}
