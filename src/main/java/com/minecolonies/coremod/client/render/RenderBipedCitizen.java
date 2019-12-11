package com.minecolonies.coremod.client.render;

import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.model.ModelEntityCitizenFemaleCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
    public static        boolean    isItGhostTime      = false;

    /**
     * The resource location for the blocking overlay.
     */
    private static final ResourceLocation BLOCKING_RESOURCE = new ResourceLocation(Constants.MOD_ID, "textures/icons/blocking.png");

    /**
     * The resource location for the pending overlay.
     */
    private static final ResourceLocation PENDING_RESOURCE = new ResourceLocation(Constants.MOD_ID, "textures/icons/warning.png");

    /**
     * Renders model.
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
        setupMainModelFrom(citizen);

        final ModelBiped citizenModel = (ModelBiped) mainModel;

        final ItemStack mainHandStack = citizen.getHeldItemMainhand();
        final ItemStack offHandStack = citizen.getHeldItemOffhand();
        final ModelBiped.ArmPose armPoseMainHand = getArmPoseFrom(citizen, mainHandStack, ModelBiped.ArmPose.EMPTY);
        final ModelBiped.ArmPose armPoseOffHand = getArmPoseFrom(citizen, offHandStack, ModelBiped.ArmPose.EMPTY);

        updateArmPose(citizen, citizenModel, armPoseMainHand, armPoseOffHand);

        if (isItGhostTime)
        {
            GlStateManager.enableBlend();

            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.3F);

            super.doRender(citizen, d, d1, d2, f, f1);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);
            
            GlStateManager.disableBlend();
        }
        else
        {
            super.doRender(citizen, d, d1, d2, f, f1);
        }
    }

    private void setupMainModelFrom(@NotNull final C citizen)
    {
        mainModel = citizen.isFemale()
                      ? IModelTypeRegistry.getInstance().getFemaleMap().get(citizen.getModelType())
                      : IModelTypeRegistry.getInstance().getMaleMap().get(citizen.getModelType());

        if (mainModel == null)
        {
            mainModel = citizen.isFemale() ? defaultModelFemale : defaultModelMale;
        }

        mainModel.isChild = citizen.isChild();
    }

    @Override
    protected void renderLivingLabel(final C entityIn, final String str, final double x, final double yIn, final double z, final int maxDistance)
    {
        super.renderLivingLabel(entityIn, str, x, yIn, z, maxDistance);
        double yOffset = mainModel.isChild ? -0.8 : 0;
        if (entityIn instanceof EntityCitizen && ((EntityCitizen) entityIn).getCitizenDataView() != null && ((EntityCitizen) entityIn).getCitizenDataView().hasPendingInteractions())
        {
            double distance = entityIn.getDistanceSq(this.renderManager.renderViewEntity);
            if (!(distance > (double) (maxDistance * maxDistance)))
            {
                boolean isSneaking = entityIn.isSneaking();
                double viewerYaw = this.renderManager.playerViewY;
                double viewerPitch = this.renderManager.playerViewX;
                double f2 = entityIn.getEyeHeight() + 0.5F - (isSneaking ? 0.25F : 0.0F);
                double y = yIn + f2 + 0.3 + yOffset;

                Minecraft.getMinecraft().getTextureManager().bindTexture(((EntityCitizen) entityIn).getCitizenDataView().hasBlockingInteractions()  ? BLOCKING_RESOURCE : PENDING_RESOURCE);

                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
                GlStateManager.rotate((float) -viewerYaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate((float) viewerPitch, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.scale(-0.025F, -0.025F, 0.025F);
                GlStateManager.disableLighting();
                GlStateManager.depthMask(false);
                if (!isSneaking)
                {
                    GlStateManager.disableDepth();
                }

                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                  GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                  GlStateManager.SourceFactor.ONE,
                  GlStateManager.DestFactor.ZERO);

                Tessellator tess = Tessellator.getInstance();
                BufferBuilder r = tess.getBuffer();
                r.begin(7, DefaultVertexFormats.POSITION_TEX);
                r.pos(0, 0, 0).tex(0, 0).endVertex();
                r.pos(0, 10, 0).tex(1, 0).endVertex();
                r.pos(10, 10, 0).tex(1, 1).endVertex();
                r.pos(10, 0, 0).tex(0, 1).endVertex();
                tess.draw();

                GlStateManager.enableTexture2D();
                if (!isSneaking)
                {
                    GlStateManager.enableDepth();
                }

                GlStateManager.depthMask(true);
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.popMatrix();
            }
        }
    }

    private ModelBiped.ArmPose getArmPoseFrom(@NotNull final C citizen, final ItemStack mainHandStack, ModelBiped.ArmPose armPoseMainHand)
    {
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
        return armPoseMainHand;
    }

    private void updateArmPose(
      @NotNull final C citizen,
      final ModelBiped citizenModel,
      final ModelBiped.ArmPose armPoseMainHand,
      final ModelBiped.ArmPose armPoseOffHand)
    {
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
