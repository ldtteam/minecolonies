package com.minecolonies.coremod.client.render;

import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.coremod.client.model.ModelEntityCitizenFemaleCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.BED_HEIGHT;

/**
 * Renderer for the citizens.
 */
public class RenderBipedCitizen<C extends EntityCitizen> extends RenderBiped<C>
{
    private static final ModelBiped defaultModelMale   = new ModelBiped();
    private static final ModelBiped defaultModelFemale = new ModelEntityCitizenFemaleCitizen();
    private static final double     SHADOW_SIZE        = 0.5F;
    private static final int        THREE_QUARTERS     = 270;

    /**
     * Renders model, see {@link RenderBiped}.
     *
     * @param renderManagerIn the RenderManager for this Renderer.
     */
    public RenderBipedCitizen(final RenderManager renderManagerIn)
    {
        super(renderManagerIn, defaultModelMale, (float) SHADOW_SIZE);
        super.addLayer(new LayerBipedArmor(this));
    }

    @Override
    public void doRender(@NotNull final C citizen, final double d, final double d1, final double d2, final float f, final float f1)
    {

        mainModel = citizen.isFemale()
                      ? IModelTypeRegistry.getInstance().getFemaleMap().get(citizen.getModelType())
                      : IModelTypeRegistry.getInstance().getMaleMap().get(citizen.getModelType());

        if (mainModel == null)
        {
            mainModel = citizen.isFemale() ? defaultModelFemale : defaultModelMale;
        }

        final ModelBiped citizenModel = (ModelBiped) mainModel;

        final ItemStack mainHandStack = citizen.getHeldItemMainhand();
        final ItemStack offHandStack = citizen.getHeldItemOffhand();
        ModelBiped.ArmPose armPoseMainHand = ModelBiped.ArmPose.EMPTY;
        ModelBiped.ArmPose armPoseOffHand = ModelBiped.ArmPose.EMPTY;

        final EnumAction enumActionMainHand;
        if (!mainHandStack.isEmpty())
        {
            armPoseMainHand = ModelBiped.ArmPose.ITEM;
            if (citizen.getItemInUseCount() > 0)
            {
                enumActionMainHand = mainHandStack.getItemUseAction();
                if (enumActionMainHand == EnumAction.BLOCK)
                {
                    armPoseMainHand = ModelBiped.ArmPose.BLOCK;
                }
                else if (enumActionMainHand == EnumAction.BOW)
                {
                    armPoseMainHand = ModelBiped.ArmPose.BOW_AND_ARROW;
                }
            }
        }

        final EnumAction enumActionOffHand;
        if (!offHandStack.isEmpty())
        {
            armPoseOffHand = ModelBiped.ArmPose.ITEM;
            if (citizen.getItemInUseCount() > 0)
            {
                enumActionOffHand = offHandStack.getItemUseAction();
                if (enumActionOffHand == EnumAction.BLOCK)
                {
                    armPoseOffHand = ModelBiped.ArmPose.BLOCK;
                }
                else if (enumActionOffHand == EnumAction.BOW)
                {
                    armPoseOffHand = ModelBiped.ArmPose.BOW_AND_ARROW;
                }
            }
        }

        if (citizen.getPrimaryHand() == EnumHandSide.RIGHT)
        {
            citizenModel.rightArmPose = armPoseMainHand;
            citizenModel.leftArmPose = armPoseOffHand;
        }
        else
        {
            citizenModel.rightArmPose = armPoseOffHand;
            citizenModel.leftArmPose = armPoseMainHand;
        }

        super.doRender(citizen, d, d1, d2, f, f1);
    }

    @Override
    protected void renderLivingAt(final C entityLivingBaseIn, final double x, final double y, final double z)
    {
        if (entityLivingBaseIn.isEntityAlive() && entityLivingBaseIn.getCitizenSleepHandler().isAsleep())
        {
            super.renderLivingAt(entityLivingBaseIn, x + (double)entityLivingBaseIn.getCitizenSleepHandler().getRenderOffsetX(), y + BED_HEIGHT, z + (double)entityLivingBaseIn.getCitizenSleepHandler().getRenderOffsetZ());
        }
        else
        {
            super.renderLivingAt(entityLivingBaseIn, x, y, z);
        }
    }

    @Override
    protected void applyRotations(final C entityLiving, final float rotationHead, final float rotationYaw, final float partialTicks)
    {
        if (entityLiving.isEntityAlive() && entityLiving.getCitizenSleepHandler().isAsleep())
        {
            GlStateManager.rotate(entityLiving.getCitizenSleepHandler().getBedOrientationInDegrees(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(this.getDeathMaxRotation(entityLiving), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(THREE_QUARTERS, 0.0F, 1.0F, 0.0F);
        }
        else
        {
            super.applyRotations(entityLiving, rotationHead, rotationYaw, partialTicks);
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(@NotNull final EntityCitizen entity)
    {
        return entity.getTexture();
    }
}
