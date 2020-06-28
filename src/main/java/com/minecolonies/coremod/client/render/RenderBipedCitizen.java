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
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector3f;
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

        final CitizenModel<AbstractEntityCitizen> citizenModel = entityModel;

        final ItemStack mainHandStack = citizen.getHeldItemMainhand();
        final ItemStack offHandStack = citizen.getHeldItemOffhand();
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
        entityModel = (citizen.isFemale()
                         ? IModelTypeRegistry.getInstance().getFemaleMap().get(citizen.getModelType())
                         : IModelTypeRegistry.getInstance().getMaleMap().get(citizen.getModelType()));
        if (entityModel == null)
        {
            entityModel = (citizen.isFemale() ? new ModelEntityFemaleCitizen() : new CitizenModel<>(0.0F));
        }

        entityModel.isChild = citizen.isChild();
        entityModel.isSitting = citizen.getRidingEntity() != null;
        entityModel.swingProgress = citizen.swingProgress;
    }

    @Override
    protected void renderName(@NotNull final AbstractEntityCitizen entityIn, @NotNull final String str, @NotNull final MatrixStack matrixStack, @NotNull final IRenderTypeBuffer buffer, final int maxDistance)
    {
        super.renderName(entityIn, str, matrixStack, buffer, maxDistance);

        if (entityIn instanceof EntityCitizen && ((EntityCitizen) entityIn).getCitizenDataView() != null && ((EntityCitizen) entityIn).getCitizenDataView().hasPendingInteractions())
        {
            double distance = this.renderManager.getDistanceToCamera(entityIn.posX, entityIn.posY, entityIn.posZ);
            if (distance <= 4096.0D)
            {
                double yOffset = entityModel.isChild ? -0.8 : 0;
                boolean isSneaking = entityIn.isSneaking();
                double height = entityIn.getHeight() + 0.5F - (isSneaking ? 0.25F : 0.0F);
                double y = height + 0.3 + yOffset;

                final ResourceLocation texture = ((EntityCitizen) entityIn).getCitizenDataView().hasBlockingInteractions()  ? BLOCKING_RESOURCE : PENDING_RESOURCE;

                matrixStack.push();
                matrixStack.translate(0, y, 0);
                matrixStack.rotate(renderManager.getCameraOrientation());
                matrixStack.rotate(Vector3f.ZP.rotationDegrees(90));

                matrixStack.scale(-0.025F, -0.025F, 0.025F);

                final Matrix4f matrix = matrixStack.getLast().getMatrix();
                final IVertexBuilder r = buffer.getBuffer(MRenderTypes.customTextRenderer(texture));

                r.pos(matrix,0, 0, 0).tex(0, 0).lightmap(250).endVertex();
                r.pos(matrix,0, 10, 0).tex(1, 0).lightmap(250).endVertex();
                r.pos(matrix,10, 10, 0).tex(1, 1).lightmap(250).endVertex();
                r.pos(matrix,10, 0, 0).tex(0, 1).lightmap(250).endVertex();
                matrixStack.pop();
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
            if (citizen.getItemInUseCount() > 0)
            {
                enumActionMainHand = mainHandStack.getUseAction();
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

    private void updateArmPose(@NotNull final AbstractEntityCitizen citizen, final BipedModel<AbstractEntityCitizen> citizenModel, final BipedModel.ArmPose armPoseMainHand, final BipedModel.ArmPose armPoseOffHand)
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

    @NotNull
    @Override
    public ResourceLocation getEntityTexture(final AbstractEntityCitizen entity)
    {
        return entity.getTexture();
    }
}
