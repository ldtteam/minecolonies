package com.blockout.controls;

import com.blockout.Pane;
import com.blockout.PaneParams;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Simple image element.
 */
public class Image extends Pane
{
    protected ResourceLocation image;
    protected int imageOffsetX = 0;
    protected int imageOffsetY = 0;
    protected int imageWidth = 0;
    protected int imageHeight = 0;
    protected int mapWidth = 256, mapHeight = 256;

    /**
     * Default Constructor.
     */
    public Image()
    {
        super();
    }

    /**
     * Constructor used by the xml loader.
     *
     * @param params PaneParams loaded from the xml.
     */
    public Image(PaneParams params)
    {
        super(params);
        String source = params.getStringAttribute("source", null);
        if (source != null)
        {
            setImage(source);
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

    /**
     * Set the image.
     *
     * @param source String path.
     */
    public void setImage(String source)
    {
        setImage(source, 0, 0, 0, 0);
    }

    /**
     * Set the image.
     *
     * @param source  String path.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImage(String source, int offsetX, int offsetY, int w, int h)
    {
        setImage((source != null) ? new ResourceLocation(source) : null, offsetX, offsetY, w, h);
    }

    /**
     * Set the image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImage(ResourceLocation loc)
    {
        setImage(loc, 0, 0, 0, 0);
    }

    /**
     * Set the image.
     *
     * @param loc     ResourceLocation for the image.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
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

    /**
     * Draw this image on the GUI.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    protected void drawSelf(int mx, int my)
    {
        // Some other texture must need to be ticked, I tried ticking the current one.
        // This fixes the problem, even if you put it after the draw call. So I guess I'll keep it.
        this.mc.getTextureManager().tick();
        this.mc.getTextureManager().bindTexture(image);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        //Draw
        drawModalRectWithCustomSizedTexture(x, y,
                imageOffsetX, imageOffsetY,
                imageWidth != 0 ? imageWidth : getWidth(),
                imageHeight != 0 ? imageHeight : getHeight(),
                mapWidth, mapHeight);
    }
}
