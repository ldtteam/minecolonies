package com.blockout.controls;

import com.blockout.Pane;
import com.blockout.PaneParams;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Iterator;

public class Image extends Pane
{
    protected ResourceLocation image;
    protected int imageOffsetX = 0;
    protected int imageOffsetY = 0;
    protected int imageWidth = 0;
    protected int imageHeight = 0;
    protected int mapWidth = 256, mapHeight = 256;

    public Image() { super(); }
    public Image(PaneParams params)
    {
        super(params);
        String source = params.getStringAttribute("source", null);
        if (source != null)
        {
            image = new ResourceLocation(source);
        }

        PaneParams.SizePair size = params.getSizePairAttribute("imageoffset", null, null);
        if (size != null)
        {
            imageOffsetX = size.x;
            imageOffsetY = size.y;
        }

        size = params.getSizePairAttribute("imagesize", null, null);
        if (size != null)
        {
            imageWidth = size.x;
            imageHeight = size.y;
        }
    }

    public void setImage(String source)
    {
        setImage(source, 0, 0, 0, 0);
    }

    public void setImage(String source, int offsetX, int offsetY, int w, int h)
    {
        setImage((source != null) ? new ResourceLocation(source) : null, offsetX, offsetY, w, h);
    }

    public void setImage(ResourceLocation loc)
    {
        setImage(loc, 0, 0, 0, 0);
    }

    public void setImage(ResourceLocation loc, int offsetX, int offsetY, int w, int h)
    {
        image = loc;
        imageOffsetX = offsetX;
        imageOffsetY = offsetY;
        imageWidth = w;
        imageHeight = h;

        //Get file dimension
        Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix("png");
        if (it.hasNext())
        {
            ImageReader reader = it.next();
            try (ImageInputStream stream = ImageIO.createImageInputStream(Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream()))
            {
                reader.setInput(stream);
                mapWidth = reader.getWidth(reader.getMinIndex());
                mapHeight = reader.getHeight(reader.getMinIndex());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                reader.dispose();
            }
        }
    }

    @Override
    protected void drawSelf(int mx, int my)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(image);

        //Draw
        drawModalRectWithCustomSizedTexture(x, y,
                imageOffsetX, imageOffsetY,
                imageWidth != 0 ? imageWidth : getWidth(),
                imageHeight != 0 ? imageHeight : getHeight(),
                mapWidth, mapHeight);
    }
}
