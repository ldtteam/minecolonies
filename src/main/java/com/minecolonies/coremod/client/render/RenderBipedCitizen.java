package com.minecolonies.coremod.client.render;

import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.client.model.ModelEntityCitizenFemaleCitizen;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.BED_HEIGHT;

/**
 * Renderer for the citizens.
 */
public class RenderBipedCitizen extends MobRenderer<AbstractEntityCitizen, CitizenModel>
{
    private static final CitizenModel defaultModelMale   = new CitizenModel();
    private static final CitizenModel defaultModelFemale = new ModelEntityCitizenFemaleCitizen();
    private static final double     SHADOW_SIZE        = 0.5F;
    private static final int        THREE_QUARTERS     = 270;

    /**
     * Renders model, see {@link BipedRenderer}.
     *
     * @param renderManagerIn the RenderManager for this Renderer.
     */
    public RenderBipedCitizen(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, defaultModelMale, (float) SHADOW_SIZE);
        super.addLayer(new BipedArmorLayer(this, defaultModelMale, defaultModelMale));
    }

    @Override
    protected void renderModel(
      final AbstractEntityCitizen citizen,
      final float limbSwing,
      final float limbSwingAmount,
      final float ageInTicks,
      final float netHeadYaw,
      final float headPitch,
      final float scaleFactor)
    {
        setupMainModelFrom(citizen);

        final CitizenModel citizenModel = entityModel;

        final ItemStack mainHandStack = citizen.getHeldItemMainhand();
        final ItemStack offHandStack = citizen.getHeldItemOffhand();
        final BipedModel.ArmPose armPoseMainHand = getArmPoseFrom(citizen, mainHandStack, BipedModel.ArmPose.EMPTY);
        final BipedModel.ArmPose armPoseOffHand = getArmPoseFrom(citizen, offHandStack, BipedModel.ArmPose.EMPTY);

        updateArmPose(citizen, citizenModel, armPoseMainHand, armPoseOffHand);
        super.renderModel(citizen, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
    }

    private void setupMainModelFrom(@NotNull final AbstractEntityCitizen citizen)
    {
        entityModel = citizen.isFemale()
                      ? IModelTypeRegistry.getInstance().getFemaleMap().get(citizen.getModelType())
                      : IModelTypeRegistry.getInstance().getMaleMap().get(citizen.getModelType());

        if (entityModel == null)
        {
            entityModel = citizen.isFemale() ? defaultModelFemale : defaultModelMale;
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

    private void updateArmPose(
      @NotNull final AbstractEntityCitizen citizen,
      final BipedModel citizenModel,
      final BipedModel.ArmPose armPoseMainHand,
      final BipedModel.ArmPose armPoseOffHand)
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

    @Override
    protected void renderLivingAt(final AbstractEntityCitizen LivingEntityIn, final double x, final double y, final double z)
    {
        if (LivingEntityIn.isAlive() && LivingEntityIn.getCitizenSleepHandler().isAsleep())
        {
            super.renderLivingAt(LivingEntityIn, x + (double)LivingEntityIn.getCitizenSleepHandler().getRenderOffsetX(), y + BED_HEIGHT, z + (double)LivingEntityIn.getCitizenSleepHandler().getRenderOffsetZ());
        }
        else
        {
            super.renderLivingAt(LivingEntityIn, x, y, z);
        }
    }

    @Override
    protected void applyRotations(final AbstractEntityCitizen entityLiving, final float rotationHead, final float rotationYaw, final float partialTicks)
    {
        if (entityLiving.isAlive() && entityLiving.getCitizenSleepHandler().isAsleep())
        {
            GlStateManager.rotated(entityLiving.getCitizenSleepHandler().getBedOrientationInDegrees(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotated(this.getDeathMaxRotation(entityLiving), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotated(THREE_QUARTERS, 0.0F, 1.0F, 0.0F);
        }
        else
        {
            super.applyRotations(entityLiving, rotationHead, rotationYaw, partialTicks);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(@NotNull final AbstractEntityCitizen entity)
    {
        return entity.getTexture();
    }
}
