package com.minecolonies.core.client.render;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.apiimp.initializer.ModModelTypeInitializer;
import com.minecolonies.core.client.render.worldevent.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import com.minecolonies.api.client.render.modeltype.CitizenRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Renderer for the citizens.
 */
public class RenderBipedCitizen extends MobRenderer<AbstractEntityCitizen, CitizenRenderState, CitizenModel<CitizenRenderState>>
{
    private static final double  SHADOW_SIZE   = 0.5F;
    public static        boolean isItGhostTime = false;
    private final CitizenModel<CitizenRenderState> defaultModel;

    /**
     * Renders model, see {@link MobRenderer}.
     *
     * @param context the context for this Renderer.
     */
    public RenderBipedCitizen(final EntityRendererProvider.Context context)
    {
        super(context, new CitizenModel<>(context.bakeLayer(ModelLayers.PLAYER)), (float) SHADOW_SIZE);
        this.defaultModel = this.model;
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getPlayerSkinRenderCache()));
        this.addLayer(new WingsLayer<>(this, context.getModelSet(), context.getEquipmentRenderer()));
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new CitizenArmorLayer(this,
            ModelLayers.PLAYER_ARMOR,
            context.getEquipmentRenderer(),
            context.getModelSet()));
        ModModelTypeInitializer.init(context);
    }

    @Override
    public CitizenRenderState createRenderState()
    {
        return new CitizenRenderState();
    }

    @Override
    public void extractRenderState(@NotNull final AbstractEntityCitizen citizen, @NotNull final CitizenRenderState state, final float partialTicks)
    {
        super.extractRenderState(citizen, state, partialTicks);
        HumanoidMobRenderer.extractHumanoidRenderState(citizen, state, partialTicks, this.itemModelResolver);
        state.setCitizen(citizen);
        state.rightArmPose = RenderUtils.getArmPose(citizen, InteractionHand.MAIN_HAND);
        state.leftArmPose = RenderUtils.getArmPose(citizen, InteractionHand.OFF_HAND);
        state.customHeadHidden = false;
        state.actualBodyRotation = 0.0F;
        if (citizen.getCitizenDataView() != null)
        {
            final ICitizenDataView citizenDataView = citizen.getCitizenDataView();
            state.headEquipment = firstNonEmpty(citizenDataView.getDisplayArmor(EquipmentSlot.HEAD), citizen.getItemBySlot(EquipmentSlot.HEAD));
            state.chestEquipment = firstNonEmpty(citizenDataView.getDisplayArmor(EquipmentSlot.CHEST), citizen.getItemBySlot(EquipmentSlot.CHEST));
            state.legsEquipment = firstNonEmpty(citizenDataView.getDisplayArmor(EquipmentSlot.LEGS), citizen.getItemBySlot(EquipmentSlot.LEGS));
            state.feetEquipment = firstNonEmpty(citizenDataView.getDisplayArmor(EquipmentSlot.FEET), citizen.getItemBySlot(EquipmentSlot.FEET));
        }
    }

    @Override
    public void submit(
      @NotNull final CitizenRenderState state,
      @NotNull final PoseStack poseStack,
      @NotNull final SubmitNodeCollector submitNodeCollector,
      @NotNull final CameraRenderState camera)
    {
        // Entity extraction is batched in 26.2, so choosing the model during extraction
        // leaks into the next entity. Select it immediately before this state is drawn.
        this.model = modelFor(state.getCitizen());
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    /**
     * Select the same gender/model-type-specific model that the pre-26.2 renderer used.
     * The state renderer keeps a single model reference, so this must happen immediately
     * before the state is submitted, after all visible entity states have been extracted.
     */
    private CitizenModel<CitizenRenderState> modelFor(final AbstractEntityCitizen citizen)
    {
        final IModelTypeRegistry registry = IModelTypeRegistry.getInstance();
        final ICitizenDataView citizenDataView = citizen.getCitizenDataView();
        final IModelType modelType = registry.getModelType(citizen.getModelType());
        if (citizenDataView != null && citizenDataView.getCustomTexture() != null)
        {
            final IModelType customType = registry.getModelType(ModModelTypes.CUSTOM_ID);
            if (customType != null && customType.getMaleModel() != null)
            {
                return customType.getMaleModel();
            }
        }
        if (modelType != null)
        {
            final CitizenModel<CitizenRenderState> selected = citizen.isFemale() ? modelType.getFemaleModel() : modelType.getMaleModel();
            if (selected != null)
            {
                return selected;
            }
        }
        return defaultModel;
    }

    private static ItemStack firstNonEmpty(final ItemStack preferred, final ItemStack fallback)
    {
        return preferred.isEmpty() ? fallback : preferred;
    }

    @Override
    protected void submitNameDisplay(
      @NotNull final CitizenRenderState state,
      @NotNull final PoseStack poseStack,
      @NotNull final SubmitNodeCollector submitNodeCollector,
      @NotNull final CameraRenderState camera)
    {
        super.submitNameDisplay(state, poseStack, submitNodeCollector, camera);

        final AbstractEntityCitizen citizen = state.getCitizen();
        if (citizen != null && citizen.getCitizenDataView() != null && citizen.getCitizenDataView().hasVisibleStatus())
        {
            submitNodeCollector.submitCustomGeometry(poseStack,
                RenderTypes.worldEntityIcon(citizen.getCitizenDataView().getStatusIcon()),
                (matrix, vertices) -> {
                    vertices.addVertex(matrix, -5, 0, 0).setUv(0, 0);
                    vertices.addVertex(matrix, -5, 10, 0).setUv(0, 1);
                    vertices.addVertex(matrix, 5, 10, 0).setUv(1, 1);
                    vertices.addVertex(matrix, 5, 0, 0).setUv(1, 0);
                });
        }
    }

    @NotNull
    @Override
    public Identifier getTextureLocation(final CitizenRenderState state)
    {
        final AbstractEntityCitizen citizen = state.getCitizen();
        if (citizen != null && citizen.getCitizenDataView() != null && citizen.getCitizenDataView().getCustomTexture() != null)
        {
            return citizen.getCitizenDataView().getCustomTexture();
        }
        return citizen == null ? Identifier.withDefaultNamespace("textures/entity/citizen/default/settlermale1_b.png") : citizen.getTexture();
    }
}
