package com.minecolonies.structures.client;

import com.minecolonies.blockout.Log;
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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import static org.lwjgl.opengl.GL11.*;

public class TemplateTessellator
{

    private static final int VERTEX_COMPONENT_SIZE = 3;
    private static final  int   COLOR_COMPONENT_SIZE              = 4;
    private static final  int   TEX_COORD_COMPONENT_SIZE          = 2;
    private static final  int   LIGHT_TEX_COORD_COMPONENT_SIZE    = TEX_COORD_COMPONENT_SIZE;
    private static final  int   VERTEX_SIZE                       = 28;
    private static final  int   VERTEX_COMPONENT_OFFSET           = 0;
    private static final  int   COLOR_COMPONENT_OFFSET            = 12;
    private static final  int   TEX_COORD_COMPONENT_OFFSET        = 16;
    private static final  int   LIGHT_TEXT_COORD_COMPONENT_OFFSET = 24;
    private static final float HALF_PERCENT_SHRINK               = 0.995F;
    private static final int DEFAULT_BUFFER_SIZE = 2097152;

    private final BufferBuilder builder;
    private final VertexBuffer         buffer      = new VertexBuffer(DefaultVertexFormats.BLOCK);
    private final VertexBufferUploader vboUploader = new VertexBufferUploader();
    private       boolean              isReadOnly  = false;

    public TemplateTessellator()
    {
        this.builder = new BufferBuilder(DEFAULT_BUFFER_SIZE);
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

        final int currentShader = preTemplateDraw();

        this.buffer.drawArrays(GL_QUADS);

        postTemplateDraw(currentShader);

        this.buffer.unbindBuffer();

        postTemplateBufferUnbinding();
    }

    private static void preTemplateBufferBinding(final Rotation rotation, final Mirror mirror, final Vector3d drawingOffset, final BlockPos inTemplateOffset)
    {
        final ITextureObject textureObject = Minecraft.getMinecraft().getTextureMapBlocks();
        GlStateManager.bindTexture(textureObject.getGlTextureId());

        GlStateManager.pushMatrix();
        GlStateManager.translate(drawingOffset.x, drawingOffset.y, drawingOffset.z);

        final BlockPos rotateInTemplateOffset = inTemplateOffset.rotate(rotation);
        GlStateManager.translate(-rotateInTemplateOffset.getX(), -rotateInTemplateOffset.getY(), -rotateInTemplateOffset.getZ());

        RenderUtil.applyRotationToYAxis(rotation);
        RenderUtil.applyMirror(mirror, inTemplateOffset);

        GlStateManager.scale(HALF_PERCENT_SHRINK, HALF_PERCENT_SHRINK, HALF_PERCENT_SHRINK);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.resetColor();
        GlStateManager.pushMatrix();
    }

    private static int preTemplateDraw()
    {
        GlStateManager.glEnableClientState(GL_VERTEX_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL_COLOR_ARRAY);

        GlStateManager.glVertexPointer(VERTEX_COMPONENT_SIZE, GL_FLOAT, VERTEX_SIZE, VERTEX_COMPONENT_OFFSET);
        GlStateManager.glColorPointer(COLOR_COMPONENT_SIZE, GL_UNSIGNED_BYTE, VERTEX_SIZE, COLOR_COMPONENT_OFFSET);
        GlStateManager.glTexCoordPointer(TEX_COORD_COMPONENT_SIZE, GL_FLOAT, VERTEX_SIZE, TEX_COORD_COMPONENT_OFFSET);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glTexCoordPointer(LIGHT_TEX_COORD_COMPONENT_SIZE, GL_SHORT, VERTEX_SIZE, LIGHT_TEXT_COORD_COMPONENT_OFFSET);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);

        GlStateManager.disableCull();

        final int currentProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        Log.getLogger().info(String.format("Disabled Shader Programm: %d", currentProgram));
        GL20.glUseProgram(0);

        return currentProgram;
    }

    private void postTemplateDraw(final int shaderProgramm)
    {
        GlStateManager.enableCull();

        for (final VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements())
        {
            final VertexFormatElement.EnumUsage vfeUsage = vertexformatelement.getUsage();
            final int formatIndex = vertexformatelement.getIndex();

            switch (vfeUsage)
            {
                case POSITION:
                    GlStateManager.glDisableClientState(GL_VERTEX_ARRAY);
                    break;
                case UV:
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + formatIndex);
                    GlStateManager.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                    break;
                case COLOR:
                    GlStateManager.glDisableClientState(GL_COLOR_ARRAY);
                    GlStateManager.resetColor();
                    break;
                default:
                    //NOOP
                    break;
            }
        }

        GL20.glUseProgram(shaderProgramm);
        Log.getLogger().info(String.format("Enabled Shader programm: %d", shaderProgramm));
    }

    private void postTemplateBufferUnbinding()
    {
        GlStateManager.popMatrix();
        GlStateManager.resetColor();
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
