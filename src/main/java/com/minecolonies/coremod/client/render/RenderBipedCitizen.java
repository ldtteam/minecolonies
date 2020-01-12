package com.minecolonies.coremod.client.render;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.model.ModelEntityFemaleCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
        super(renderManagerIn, new CitizenModel(0.0F), (float) SHADOW_SIZE);
        super.addLayer(new BipedArmorLayer<>(this, new CitizenModel(0.5F), new CitizenModel(1.0F)));
        super.addLayer(new HeldItemLayer(this));
    }

    @Override
    public void render(@NotNull final LivingEntity entity, final float limbSwing, final float partialTicks, @NotNull final MatrixStack matrixStack, @NotNull final IRenderTypeBuffer renderTypeBuffer, final int light)
    {
        final AbstractEntityCitizen citizen = (AbstractEntityCitizen) entity;
        setupMainModelFrom(citizen);

        final CitizenModel citizenModel = (CitizenModel) entityModel;

        final ItemStack mainHandStack = citizen.getHeldItemMainhand();
        final ItemStack offHandStack = citizen.getHeldItemOffhand();
        final BipedModel.ArmPose armPoseMainHand = getArmPoseFrom(citizen, mainHandStack, BipedModel.ArmPose.EMPTY);
        final BipedModel.ArmPose armPoseOffHand = getArmPoseFrom(citizen, offHandStack, BipedModel.ArmPose.EMPTY);

        updateArmPose(citizen, citizenModel, armPoseMainHand, armPoseOffHand);
        super.render((MobEntity) entity, limbSwing, partialTicks, matrixStack, renderTypeBuffer, light);
    }

    @Override
    public void render(
      final MobEntity entity,
      final float limbSwing,
      final float partialTicks,
      final MatrixStack matrixStack,
      final IRenderTypeBuffer renderTypeBuffer,
      final int light)
    {

        if (isItGhostTime)
        {
            GlStateManager.enableBlend();
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 0.3F);

            super.render(entity, limbSwing, partialTicks, matrixStack, renderTypeBuffer, light);

            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1F);

            GlStateManager.disableBlend();
        }
        else
        {
            super.render(entity, limbSwing, partialTicks, matrixStack, renderTypeBuffer, light);
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
    protected void renderLabelIfPresent(
      final Entity entityIn,
      final String str,
      final MatrixStack matrixStack,
      final IRenderTypeBuffer buffer,
      final int maxDistance)
    {
        super.renderLabelIfPresent(entityIn, str, matrixStack, buffer, maxDistance);

        if (entityIn instanceof EntityCitizen && ((EntityCitizen) entityIn).getCitizenDataView() != null && ((EntityCitizen) entityIn).getCitizenDataView().hasPendingInteractions())
        {
            double distance = entityIn.getDistanceSq(this.renderManager.info.getProjectedView());
            if (!(distance > (double) (maxDistance * maxDistance)))
            {
                double yOffset = entityModel.isChild ? -0.8 : 0;
                boolean isSneaking = entityIn.isSneaking();
                double viewerYaw = this.renderManager.info.getYaw();
                double viewerPitch = this.renderManager.info.getPitch();
                double f2 = entityIn.getHeight() + 0.5F - (isSneaking ? 0.25F : 0.0F);
                double y = entityIn.getY() + f2 + 0.3 + yOffset;

                Minecraft.getInstance().textureManager.bindTexture(((EntityCitizen) entityIn).getCitizenDataView().hasBlockingInteractions()  ? BLOCKING_RESOURCE : PENDING_RESOURCE);

                matrixStack.push();
                matrixStack.translate(entityIn.getX(), entityIn.getY(), entityIn.getZ());
                GlStateManager.normal3f(0.0F, 1.0F, 0.0F);

                final Matrix4f matrix = matrixStack.peek().getModel();

                RenderSystem.rotatef((float) -viewerYaw, 0.0F, 1.0F, 0.0F);
                RenderSystem.rotatef((float) viewerPitch, 1.0F, 0.0F, 0.0F);
                RenderSystem.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
                RenderSystem.scalef(-0.025F, -0.025F, 0.025F);
                RenderSystem.disableLighting();
                RenderSystem.depthMask(false);
                if (!isSneaking)
                {
                    GlStateManager.disableDepthTest();
                }

                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                  GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                  GlStateManager.SourceFactor.ONE,
                  GlStateManager.DestFactor.ZERO);

                Tessellator tess = Tessellator.getInstance();
                BufferBuilder r = tess.getBuffer();
                r.begin(7, DefaultVertexFormats.POSITION_TEX);
                r.vertex(0, 0, 0).texture(0, 0).endVertex();
                r.vertex(0, 10, 0).texture(1, 0).endVertex();
                r.vertex(10, 10, 0).texture(1, 1).endVertex();
                r.vertex(10, 0, 0).texture(0, 1).endVertex();
                tess.draw();

                RenderSystem.enableTexture();
                if (!isSneaking)
                {
                    GlStateManager.enableDepthTest();
                }

                RenderSystem.depthMask(true);
                RenderSystem.enableLighting();
                RenderSystem.disableBlend();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
        this.
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
    }

    @Nullable
    @Override
    public ResourceLocation getEntityTexture(final Entity entity)
    {
        return ((AbstractEntityCitizen) entity).getTexture();
    }
}
