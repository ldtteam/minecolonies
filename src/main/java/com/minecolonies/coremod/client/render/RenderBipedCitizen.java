package com.minecolonies.coremod.client.render;

import com.minecolonies.api.client.render.BipedModelType;
import com.minecolonies.coremod.client.model.*;
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

import java.util.EnumMap;
import java.util.Map;

import static com.minecolonies.api.util.constant.Constants.BED_HEIGHT;

/**
 * Renderer for the citizens.
 */
public class RenderBipedCitizen<C extends EntityCitizen> extends RenderBiped<C>
{
    private static final ModelBiped                      defaultModelMale   = new ModelBiped();
    private static final ModelBiped                      defaultModelFemale = new ModelEntityCitizenFemaleCitizen();
    private static final Map<BipedModelType, ModelBiped> idToMaleModelMap   = new EnumMap<>(BipedModelType.class);
    private static final Map<BipedModelType, ModelBiped> idToFemaleModelMap = new EnumMap<>(BipedModelType.class);
    private static final double                          SHADOW_SIZE        = 0.5F;
    private static final int                             THREE_QUARTERS     = 270;

    static
    {
        idToMaleModelMap.put(BipedModelType.DELIVERYMAN, new ModelEntityDeliverymanMale());
        idToMaleModelMap.put(BipedModelType.LUMBERJACK, new ModelEntityLumberjackMale());
        idToMaleModelMap.put(BipedModelType.FARMER, new ModelEntityFarmerMale());
        idToMaleModelMap.put(BipedModelType.FISHERMAN, new ModelEntityFishermanMale());
        idToMaleModelMap.put(BipedModelType.BAKER, new ModelEntityBakerMale());
        idToMaleModelMap.put(BipedModelType.COMPOSTER, new ModelEntityComposterMale());
        idToMaleModelMap.put(BipedModelType.COOK, new ModelEntityCookMale());
        idToMaleModelMap.put(BipedModelType.CHICKEN_FARMER, new ModelEntityChickenFarmerMale());
        idToMaleModelMap.put(BipedModelType.SHEEP_FARMER, new ModelEntitySheepFarmerMale());
        idToMaleModelMap.put(BipedModelType.PIG_FARMER, new ModelEntityPigFarmerMale());
        idToMaleModelMap.put(BipedModelType.COW_FARMER, new ModelEntityCowFarmerMale());
        idToMaleModelMap.put(BipedModelType.SMELTER, new ModelEntitySmelterMale());
        idToMaleModelMap.put(BipedModelType.STUDENT, new ModelEntityStudentMale());
        idToMaleModelMap.put(BipedModelType.CRAFTER, new ModelEntityCrafterMale());
        idToMaleModelMap.put(BipedModelType.BLACKSMITH, new ModelEntityBlacksmithMale());

        idToFemaleModelMap.put(BipedModelType.NOBLE, new ModelEntityCitizenFemaleNoble());
        idToFemaleModelMap.put(BipedModelType.ARISTOCRAT, new ModelEntityCitizenFemaleAristocrat());
        idToFemaleModelMap.put(BipedModelType.BUILDER, new ModelEntityBuilderFemale());
        idToFemaleModelMap.put(BipedModelType.DELIVERYMAN, new ModelEntityDeliverymanFemale());
        idToFemaleModelMap.put(BipedModelType.MINER, new ModelEntityMinerFemale());
        idToFemaleModelMap.put(BipedModelType.LUMBERJACK, new ModelEntityLumberjackFemale());
        idToFemaleModelMap.put(BipedModelType.FARMER, new ModelEntityFarmerFemale());
        idToFemaleModelMap.put(BipedModelType.FISHERMAN, new ModelEntityFishermanFemale());
        idToFemaleModelMap.put(BipedModelType.ARCHER_GUARD, new ModelBiped());
        idToFemaleModelMap.put(BipedModelType.KNIGHT_GUARD, new ModelBiped());
        idToFemaleModelMap.put(BipedModelType.BAKER, new ModelEntityBakerFemale());
        idToFemaleModelMap.put(BipedModelType.COMPOSTER, new ModelEntityComposterFemale());
        idToFemaleModelMap.put(BipedModelType.COOK, new ModelEntityCookFemale());
        idToFemaleModelMap.put(BipedModelType.CHICKEN_FARMER, new ModelEntityChickenFarmerFemale());
        idToFemaleModelMap.put(BipedModelType.COW_FARMER, new ModelEntityCowFarmerFemale());
        idToFemaleModelMap.put(BipedModelType.PIG_FARMER, new ModelEntityPigFarmerFemale());
        idToFemaleModelMap.put(BipedModelType.SHEEP_FARMER, new ModelEntitySheepFarmerFemale());
        idToFemaleModelMap.put(BipedModelType.SMELTER, new ModelEntitySmelterFemale());
        idToFemaleModelMap.put(BipedModelType.STUDENT, new ModelEntityStudentFemale());
        idToFemaleModelMap.put(BipedModelType.CRAFTER, new ModelEntityCrafterFemale());
        idToFemaleModelMap.put(BipedModelType.BLACKSMITH, new ModelEntityBlacksmithFemale());

    }

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
                      ? idToFemaleModelMap.get(citizen.getModelID())
                      : idToMaleModelMap.get(citizen.getModelID());

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
