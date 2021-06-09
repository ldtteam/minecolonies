package com.minecolonies.coremod.client.render;

import com.ldtteam.structurize.blocks.ModBlocks;
import com.minecolonies.api.tileentities.TileEntityColonyFlag;
import com.minecolonies.coremod.blocks.decorative.BlockColonyFlagBanner;
import com.minecolonies.coremod.blocks.decorative.BlockColonyFlagWallBanner;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.BannerTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BannerItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.GameType;

import java.util.List;

/**
 * The custom renderer to render the colony flag patterns if they exist,
 * and a placeholder marker if in Creative mode.
 */
public class TileEntityColonyFlagRenderer extends TileEntityRenderer<TileEntityColonyFlag>
{
    private final ModelRenderer cloth     = BannerTileEntityRenderer.makeFlag();
    private final ModelRenderer standPost = new ModelRenderer(64, 64, 44, 0);
    private final ModelRenderer crossbar  = new ModelRenderer(64, 64, 0, 42);;

    public TileEntityColonyFlagRenderer(TileEntityRendererDispatcher dispatcher)
    {
        super(dispatcher);
        standPost.addBox(-1.0F, -30.0F, -1.0F, 2.0F, 42.0F, 2.0F, 0.0F);
        crossbar.addBox(-10.0F, -32.0F, -1.0F, 20.0F, 2.0F, 2.0F, 0.0F);
    }

    @Override
    public void render(TileEntityColonyFlag flagIn, float partialTicks, MatrixStack transform, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        List<Pair<BannerPattern, DyeColor>> list = flagIn.getPatternList();

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
                transform.mulPose(Vector3f.YP.rotationDegrees(f1));
                this.standPost.visible = true;
            }
            else if (blockstate.getBlock() instanceof BlockColonyFlagWallBanner)
            {
                transform.translate(0.5D, -0.16666667F, 0.5D);
                float f3 = -blockstate.getValue(BlockColonyFlagWallBanner.HORIZONTAL_FACING).toYRot();
                transform.mulPose(Vector3f.YP.rotationDegrees(f3));
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
                Minecraft.getInstance().getItemRenderer().renderStatic(placeholder, ItemCameraTransforms.TransformType.FIXED, combinedLightIn, OverlayTexture.NO_OVERLAY, transform, bufferIn);
                transform.popPose();
            }
        }

        transform.pushPose();
        transform.scale(2/3F, -2/3F, -2/3F);
        IVertexBuilder ivertexbuilder = ModelBakery.BANNER_BASE.buffer(bufferIn, RenderType::entitySolid);
        this.standPost.render(transform, ivertexbuilder, combinedLightIn, combinedOverlayIn);
        this.crossbar.render(transform, ivertexbuilder, combinedLightIn, combinedOverlayIn);
        BlockPos blockpos = flagIn.getBlockPos();
        float f2 = ((float)Math.floorMod((long)(blockpos.getX() * 7 + blockpos.getY() * 9 + blockpos.getZ() * 13) + i, 100L) + partialTicks) / 100.0F;
        this.cloth.xRot = (-0.0125F + 0.01F * MathHelper.cos(((float)Math.PI * 2F) * f2)) * (float)Math.PI;
        this.cloth.y = -32.0F;
        BannerTileEntityRenderer.renderPatterns(transform, bufferIn, combinedLightIn, combinedOverlayIn, this.cloth, ModelBakery.BANNER_BASE, true, list);
        transform.popPose();
        transform.popPose();
    }
}
