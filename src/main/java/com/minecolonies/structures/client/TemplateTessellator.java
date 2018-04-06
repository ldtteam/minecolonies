package com.minecolonies.structures.client;

import com.minecolonies.structures.lib.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class TemplateTessellator
{

    private final BufferBuilder builder;
    private final VertexBuffer         buffer      = new VertexBuffer(DefaultVertexFormats.BLOCK);
    private final VertexBufferUploader vboUploader = new VertexBufferUploader();
    private       boolean              isReadOnly  = false;

    public TemplateTessellator()
    {
        this.builder = new BufferBuilder(2097152);
        this.vboUploader.setVertexBuffer(buffer);
    }

    /**
     * Draws the data set up in this tessellator and resets the state to prepare for new drawing.
     */
    public void draw(final Rotation rotation, final Mirror mirror, final Vector3d drawingOffset, final BlockPos inTemplateOffset)
    {
        if (!isReadOnly)
        {
            this.builder.finishDrawing();
            this.vboUploader.draw(this.builder);
            this.isReadOnly = true;
        }

        preTemplateBufferBinding(rotation, mirror, drawingOffset, inTemplateOffset);

        this.buffer.bindBuffer();

        preTemplateDraw();

        this.buffer.drawArrays(7);

        postTemplateDraw();

        this.buffer.unbindBuffer();

        postTemplateBufferUnbinding();
    }

    private static void preTemplateBufferBinding(final Rotation rotation, final Mirror mirror, final Vector3d drawingOffset, final BlockPos inTemplateOffset)
    {
        final ITextureObject textureObject = Minecraft.getMinecraft().getTextureMapBlocks();
        GlStateManager.bindTexture(textureObject.getGlTextureId());

        GlStateManager.pushMatrix();
        GlStateManager.translate(drawingOffset.x, drawingOffset.y, drawingOffset.z);

        BlockPos rotateInTemplateOffset = inTemplateOffset.rotate(rotation);
        GlStateManager.translate(-rotateInTemplateOffset.getX(), -rotateInTemplateOffset.getY(), -rotateInTemplateOffset.getZ());

        RenderUtil.applyRotationToYAxis(rotation);
        RenderUtil.applyMirror(mirror, inTemplateOffset);

        GlStateManager.scale(0.995f, 0.995f, 0.995f);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.pushMatrix();
    }

    private static void preTemplateDraw()
    {
        GlStateManager.glEnableClientState(32884);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(32888);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glEnableClientState(32888);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(32886);

        GlStateManager.glVertexPointer(3, 5126, 28, 0);
        GlStateManager.glColorPointer(4, 5121, 28, 12);
        GlStateManager.glTexCoordPointer(2, 5126, 28, 16);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glTexCoordPointer(2, 5122, 28, 24);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);

        GlStateManager.disableCull();
    }

    private void postTemplateDraw()
    {
        GlStateManager.enableCull();

        for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements())
        {
            VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
            int k1 = vertexformatelement.getIndex();

            switch (vertexformatelement$enumusage)
            {
                case POSITION:
                    GlStateManager.glDisableClientState(32884);
                    break;
                case UV:
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + k1);
                    GlStateManager.glDisableClientState(32888);
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                    break;
                case COLOR:
                    GlStateManager.glDisableClientState(32886);
                    GlStateManager.resetColor();
            }
        }
    }

    private void postTemplateBufferUnbinding()
    {
        GlStateManager.popMatrix();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public BufferBuilder getBuilder()
    {
        if (isReadOnly)
        {
            throw new IllegalStateException("Cannot retrieve BufferBuilder when Tessellator is in readonly.");
        }

        return this.builder;
    }

    public VertexBuffer getBuffer()
    {
        return buffer;
    }
}
