package com.minecolonies.structures.client;

import com.minecolonies.structures.helpers.Settings;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class TemplateTessellator {

    private final BufferBuilder builder;
    private final VertexBuffer buffer = new VertexBuffer(DefaultVertexFormats.BLOCK);
    private final VertexBufferUploader vboUploader = new VertexBufferUploader();
    private final FloatBuffer modelviewMatrix = GLAllocation.createDirectFloatBuffer(16);
    private boolean isReadOnly = false;

    public TemplateTessellator()
    {
        this.builder = new BufferBuilder(2097152);
        this.vboUploader.setVertexBuffer(buffer);

        this.initModelviewMatrix();
    }

    private void initModelviewMatrix()
    {
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        float f = 1.000001F;
        GlStateManager.translate(-8.0F, -8.0F, -8.0F);
        GlStateManager.scale(1.000001F, 1.000001F, 1.000001F);
        GlStateManager.translate(8.0F, 8.0F, 8.0F);
        GlStateManager.getFloat(2982, this.modelviewMatrix);
        GlStateManager.popMatrix();
    }

    public void multModelviewMatrix()
    {
        GlStateManager.multMatrix(this.modelviewMatrix);
    }

    /**
     * Draws the data set up in this tessellator and resets the state to prepare for new drawing.
     */
    public void draw(double viewEntityX, double viewEntityY, double viewEntityZ)
    {
        if (!isReadOnly)
        {
            this.builder.finishDrawing();
            this.vboUploader.draw(this.builder);
            this.isReadOnly = true;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate((float)((double) Settings.instance.getPosition().getX() - viewEntityX), (float)((double)Settings.instance.getPosition().getX() - viewEntityY), (float)((double)Settings.instance.getPosition().getX() - viewEntityZ));
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1F,1F,1F,1F);
        GlStateManager.pushMatrix();

        this.multModelviewMatrix();

        this.buffer.bindBuffer();

        GlStateManager.glVertexPointer(3, 5126, 28, 0);
        GlStateManager.glColorPointer(4, 5121, 28, 12);
        GlStateManager.glTexCoordPointer(2, 5126, 28, 16);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glTexCoordPointer(2, 5122, 28, 24);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);

        this.buffer.drawArrays(7);

        this.buffer.unbindBuffer();

        GlStateManager.popMatrix();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.disableDepth();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    public BufferBuilder getBuilder()
    {
        if (isReadOnly) {
            throw new IllegalStateException("Cannot retrieve BufferBuilder when Tessellator is in readonly.");
        }

        return this.builder;
    }

    public VertexBuffer getBuffer() {
        return buffer;
    }
}
