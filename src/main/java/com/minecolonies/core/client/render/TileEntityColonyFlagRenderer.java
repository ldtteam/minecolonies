package com.minecolonies.core.client.render;

import com.minecolonies.core.tileentities.TileEntityColonyFlag;
import com.minecolonies.core.blocks.decorative.BlockColonyFlagBanner;
import com.minecolonies.core.blocks.decorative.BlockColonyFlagWallBanner;
import com.minecolonies.core.client.render.state.ColonyFlagRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;

import net.minecraft.world.phys.Vec3;

/**
 * The custom renderer to render the colony flag patterns if they exist,
 * and a placeholder marker if in Creative mode.
 */
public class TileEntityColonyFlagRenderer implements BlockEntityRenderer<TileEntityColonyFlag, ColonyFlagRenderState>
{
    private final BannerRenderer bannerRenderer;
    private final ItemModelResolver itemModelResolver;

    public TileEntityColonyFlagRenderer(final BlockEntityRendererProvider.Context context)
    {
        super();
        this.bannerRenderer = new BannerRenderer(context.entityModelSet(), context.sprites());
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public ColonyFlagRenderState createRenderState()
    {
        return new ColonyFlagRenderState();
    }

    @Override
    public void extractRenderState(@NotNull final TileEntityColonyFlag flag,
                                   @NotNull final ColonyFlagRenderState state,
                                   final float partialTicks,
                                   @NotNull final Vec3 cameraPosition,
                                   final ModelFeatureRenderer.CrumblingOverlay breakProgress)
    {
        BlockEntityRenderer.super.extractRenderState(flag, state, partialTicks, cameraPosition, breakProgress);
        state.banner.baseColor = DyeColor.WHITE;
        state.banner.patterns = flag.getPatterns();
        state.banner.attachmentType = net.minecraft.world.level.block.BannerBlock.AttachmentType.GROUND;
        final BlockState blockState = flag.getBlockState();
        if (blockState.getBlock() instanceof BlockColonyFlagBanner)
        {
            state.banner.transformation =
                BannerRenderer.TRANSFORMATIONS.freeTransformations(blockState.getValue(BlockColonyFlagBanner.ROTATION));
        }
        else if (blockState.getBlock() instanceof BlockColonyFlagWallBanner)
        {
            state.banner.attachmentType = net.minecraft.world.level.block.BannerBlock.AttachmentType.WALL;
            state.banner.transformation =
                BannerRenderer.TRANSFORMATIONS.wallTransformation(blockState.getValue(BlockColonyFlagWallBanner.HORIZONTAL_FACING));
        }

        final long gameTime = flag.getLevel() == null ? 0L : flag.getLevel().getGameTime();
        final BlockPos pos = flag.getBlockPos();
        state.banner.phase = ((float) Math.floorMod(
            pos.getX() * 7 + pos.getY() * 9 + pos.getZ() * 13 + gameTime, 100L) + partialTicks) / 100.0F;

        final Minecraft minecraft = Minecraft.getInstance();
        state.creativePlaceholder = flag.getLevel() != null
            && minecraft.player != null
            && minecraft.player.getMainHandItem().getItem() instanceof BannerItem
            && minecraft.gameMode.getPlayerMode() == GameType.CREATIVE;
        if (state.creativePlaceholder)
        {
            itemModelResolver.updateForNonLiving(state.placeholder,
                new ItemStack(com.ldtteam.structurize.blocks.ModBlocks.blockSubstitution.get()),
                ItemDisplayContext.FIXED,
                minecraft.player);
        }
        else
        {
            state.placeholder.clear();
        }
    }

    @Override
    public void submit(@NotNull final ColonyFlagRenderState state,
                       @NotNull final PoseStack poseStack,
                       @NotNull final SubmitNodeCollector collector,
                       @NotNull final CameraRenderState camera)
    {
        bannerRenderer.submit(state.banner, poseStack, collector, camera);

        if (state.creativePlaceholder && !state.placeholder.isEmpty())
        {
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.5D, 0.0D);
            poseStack.scale(0.75F, 0.75F, 0.75F);
            state.placeholder.submit(poseStack, collector, state.lightCoords, 0, 0);
            poseStack.popPose();
        }
    }
}
