package com.minecolonies.coremod.client.render;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.client.model.ModelEntityCitizenFemaleCitizen;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.minecolonies.api.util.constant.Constants.BED_HEIGHT;

/**
 * Renderer for the citizens.
 */
public class RenderBipedCitizen<T extends AbstractEntityCitizen, M extends CitizenModel> extends MobRenderer
{
    private static final double  SHADOW_SIZE    = 0.5F;
    private static final int     THREE_QUARTERS = 270;
    public static        boolean isItGhostTime  = false;

    /**
     * Renders model, see {@link BipedRenderer}.
     *
     * @param renderManagerIn the RenderManager for this Renderer.
     */
    public RenderBipedCitizen(final EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn, new CitizenModel(0.0F), (float) SHADOW_SIZE);
        super.addLayer(new BipedArmorLayer<>(this, new CitizenModel(0.5F), new CitizenModel(1.0F)));
        super.addLayer(new HeldItemLayer(this));
    }

    @Override
    protected void renderModel(
      @NotNull final LivingEntity entity,
      final float limbSwing,
      final float limbSwingAmount,
      final float ageInTicks,
      final float netHeadYaw,
      final float headPitch,
      final float scaleFactor)
    {
        final AbstractEntityCitizen citizen = (AbstractEntityCitizen) entity;
        setupMainModelFrom(citizen);

        final CitizenModel citizenModel = (CitizenModel) entityModel;

        final ItemStack mainHandStack = citizen.getHeldItemMainhand();
        final ItemStack offHandStack = citizen.getHeldItemOffhand();
        final BipedModel.ArmPose armPoseMainHand = getArmPoseFrom(citizen, mainHandStack, BipedModel.ArmPose.EMPTY);
        final BipedModel.ArmPose armPoseOffHand = getArmPoseFrom(citizen, offHandStack, BipedModel.ArmPose.EMPTY);

        updateArmPose(citizen, citizenModel, armPoseMainHand, armPoseOffHand);
        super.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
    }

    @Override
    public void doRender(MobEntity entity, double x, double y, double z, float f, float partialTicks)
    {
        if (isItGhostTime)
        {
            GlStateManager.enableBlend();
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 0.3F);

            super.doRender(entity, x, y, z, f, partialTicks);

            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1F);

            GlStateManager.disableBlend();
        }
        else
        {
            super.doRender(entity, x, y, z, f, partialTicks);
        }
    }

    private void setupMainModelFrom(@NotNull final AbstractEntityCitizen citizen)
    {
        entityModel = (citizen.isFemale()
                         ? IModelTypeRegistry.getInstance().getFemaleMap().get(citizen.getModelType())
                         : IModelTypeRegistry.getInstance().getMaleMap().get(citizen.getModelType()));

        if (entityModel == null)
        {
            entityModel = (citizen.isFemale() ? new ModelEntityCitizenFemaleCitizen() : new CitizenModel(0.0F));
        }

        entityModel.isChild = citizen.isChild();
        entityModel.swingProgress = citizen.swingProgress;

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
            GlStateManager.rotated(entityCitizen.getCitizenSleepHandler().getBedOrientationInDegrees(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotated(this.getDeathMaxRotation(entityLiving), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotated(THREE_QUARTERS, 0.0F, 1.0F, 0.0F);
        }
        else
        {
            super.applyRotations(entityLiving, rotationHead, rotationYaw, partialTicks);
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(final Entity entity)
    {
        return ((AbstractEntityCitizen) entity).getTexture();
    }
}
