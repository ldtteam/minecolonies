package com.minecolonies.core.client.render;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.model.SpearModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class SpearSpecialRenderer implements NoDataSpecialModelRenderer
{
    public static final Identifier TEXTURE =
        Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/entity/spear.png");
    public static final SpearSpecialRenderer.Unbaked UNBAKED = new SpearSpecialRenderer.Unbaked();
    public static final MapCodec<SpearSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(UNBAKED);

    private final SpearModel model;

    public SpearSpecialRenderer(final SpearModel model)
    {
        this.model = model;
    }

    @Override
    public void submit(final PoseStack poseStack,
                       final SubmitNodeCollector collector,
                       final int lightCoords,
                       final int overlayCoords,
                       final boolean hasFoil,
                       final int outlineColor)
    {
        collector.order(0).submitModel(this.model, Unit.INSTANCE, poseStack, TEXTURE,
            lightCoords, overlayCoords, outlineColor, null);
        if (hasFoil)
        {
            collector.order(1).submitModel(this.model, Unit.INSTANCE, poseStack, RenderTypes.entityGlint(),
                lightCoords, overlayCoords, outlineColor, null);
        }
    }

    @Override
    public void getExtents(final Consumer<Vector3fc> output)
    {
        final PoseStack poseStack = new PoseStack();
        this.model.root().getExtentsForGui(poseStack, output);
    }

    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked
    {
        @Override
        public MapCodec<SpearSpecialRenderer.Unbaked> type()
        {
            return MAP_CODEC;
        }

        @Override
        public SpearSpecialRenderer bake(final SpecialModelRenderer.BakingContext context)
        {
            return new SpearSpecialRenderer(new SpearModel(context.entityModelSet().bakeLayer(ModelLayers.TRIDENT)));
        }
    }
}
