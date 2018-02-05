package com.minecolonies.structures.client;

import com.minecolonies.structures.helpers.Structure;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_LIGHTING_BIT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class StructureClientHandler
{

    private static final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

    public static void renderStructure(@NotNull final World world, @NotNull final Structure structure)
    {
        GlStateManager.pushMatrix();
        GL11.glPushAttrib(GL_LIGHTING_BIT);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();

        structure.getBlockInfoWithSettings(structure.getSettings()).forEach(blockInfo -> {
            renderStructureComponentInWorld(world, blockInfo);
        });
    }

    private static void renderStructureComponentInWorld(@NotNull final World world, @NotNull final Template.BlockInfo blockInfo)
    {
        double renderPosX = Minecraft.getMinecraft().getRenderManager().renderPosX;
        double renderPosY = Minecraft.getMinecraft().getRenderManager().renderPosY;
        double renderPosZ = Minecraft.getMinecraft().getRenderManager().renderPosZ;

        final IBlockState actualState = world.getBlockState(blockInfo.pos).getActualState(world, blockInfo.pos);
        if (actualState.equals(blockInfo.blockState))
        {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(-renderPosX, -renderPosY, -renderPosZ);
        GlStateManager.disableDepth();

        doRenderStructureComponentInWorld(blockInfo);

        GlStateManager.popMatrix();
    }

    private static void doRenderStructureComponentInWorld(@NotNull final Template.BlockInfo blockInfo)
    {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        final IBlockState state = blockInfo.blockState;

        BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
        GlStateManager.translate(blockInfo.pos.getX(), blockInfo.pos.getY(), blockInfo.pos.getZ() + 1);
        GlStateManager.color(1, 1, 1, 1);
        brd.renderBlockBrightness(state, 1.0F);

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }
}
