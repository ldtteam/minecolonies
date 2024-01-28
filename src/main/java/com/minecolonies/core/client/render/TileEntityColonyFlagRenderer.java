package com.minecolonies.core.client.render;

import com.ldtteam.structurize.blocks.ModBlocks;
import com.minecolonies.core.tileentities.TileEntityColonyFlag;
import com.minecolonies.core.blocks.decorative.BlockColonyFlagBanner;
import com.minecolonies.core.blocks.decorative.BlockColonyFlagWallBanner;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;

import java.util.List;

/**
 * The custom renderer to render the colony flag patterns if they exist,
 * and a placeholder marker if in Creative mode.
 */
public class TileEntityColonyFlagRenderer implements BlockEntityRenderer<TileEntityColonyFlag>
{
    private final ModelPart cloth;
    private final ModelPart standPost;
    private final ModelPart crossbar;

    public TileEntityColonyFlagRenderer(final BlockEntityRendererProvider.Context context)
    {
        super();
        ModelPart modelpart = context.bakeLayer(ModelLayers.BANNER);
        this.cloth = modelpart.getChild("flag");
        this.standPost = modelpart.getChild("pole");
        this.crossbar = modelpart.getChild("bar");
    }

    @Override
    public void render(TileEntityColonyFlag flagIn, float partialTicks, PoseStack transform, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        List<Pair<Holder<BannerPattern>, DyeColor>> list = flagIn.getPatternList();

        boolean noWorld = flagIn.getLevel() == null;
        transform.pushPose();
        long i;
        if (noWorld)
        {
            i = 0L;
            transform.translate(0.5D, 0.5D, 0.5D);
            this.standPost.visible = true;
        }
        else
        {

            i = flagIn.getLevel().getGameTime();
            BlockState blockstate = flagIn.getBlockState();
            if (blockstate.getBlock() instanceof BlockColonyFlagBanner)
            {
                transform.translate(0.5D, 0.5D, 0.5D);
                float f1 = (float)(-blockstate.getValue(BlockColonyFlagBanner.ROTATION) * 360) / 16.0F;
                transform.mulPose(Axis.YP.rotationDegrees(f1));
                this.standPost.visible = true;
            }
            else if (blockstate.getBlock() instanceof BlockColonyFlagWallBanner)
            {
                transform.translate(0.5D, -0.16666667F, 0.5D);
                float f3 = -blockstate.getValue(BlockColonyFlagWallBanner.HORIZONTAL_FACING).toYRot();
                transform.mulPose(Axis.YP.rotationDegrees(f3));
                transform.translate(0.0D, -0.3125D, -0.4375D);
                this.standPost.visible = false;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc.player.getMainHandItem().getItem() instanceof BannerItem
             && mc.gameMode.getPlayerMode() == GameType.CREATIVE)
            {
                transform.pushPose();
                ItemStack placeholder = new ItemStack(ModBlocks.blockSubstitution.get());

                transform.translate(0.0D, 0.5D, 0.0D);
                transform.scale(0.75F, 0.75F, 0.75F);
                Minecraft.getInstance().getItemRenderer().renderStatic(placeholder, ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn, transform, bufferIn, mc.level, OverlayTexture.NO_OVERLAY);
                transform.popPose();
            }
        }

        transform.pushPose();
        transform.scale(2/3F, -2/3F, -2/3F);
        VertexConsumer ivertexbuilder = ModelBakery.BANNER_BASE.buffer(bufferIn, RenderType::entitySolid);
        this.standPost.render(transform, ivertexbuilder, combinedLightIn, combinedOverlayIn);
        this.crossbar.render(transform, ivertexbuilder, combinedLightIn, combinedOverlayIn);
        BlockPos blockpos = flagIn.getBlockPos();
        float f2 = ((float)Math.floorMod((long)(blockpos.getX() * 7 + blockpos.getY() * 9 + blockpos.getZ() * 13) + i, 100L) + partialTicks) / 100.0F;
        this.cloth.xRot = (-0.0125F + 0.01F * Mth.cos(((float)Math.PI * 2F) * f2)) * (float)Math.PI;
        this.cloth.y = -32.0F;
        BannerRenderer.renderPatterns(transform, bufferIn, combinedLightIn, combinedOverlayIn, this.cloth, ModelBakery.BANNER_BASE, true, list);
        transform.popPose();
        transform.popPose();
    }
}
