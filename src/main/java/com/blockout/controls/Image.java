package com.blockout.controls;

import com.blockout.Pane;
import com.blockout.PaneParams;
import com.minecolonies.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;

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
    public static final int MINECRAFT_DEFAULT_TEXTURE_MAP_SIZE = 256;

    protected ResourceLocation image;
    protected int imageOffsetX = 0;
    protected int imageOffsetY = 0;
    protected int imageWidth = 0;
    protected int imageHeight = 0;
    protected int mapWidth = MINECRAFT_DEFAULT_TEXTURE_MAP_SIZE;
    protected int mapHeight = MINECRAFT_DEFAULT_TEXTURE_MAP_SIZE;

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
            image = new ResourceLocation(source);
            loadMapDimensions();
        }

        PaneParams.SizePair size = params.getSizePairAttribute("imageoffset", null, null);
        if (size != null)
        {
            imageOffsetX = size.getX();
            imageOffsetY = size.getY();
        }

        size = params.getSizePairAttribute("imagesize", null, null);
        if (size != null)
        {
            imageWidth = size.getX();
            imageHeight = size.getY();
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

        loadMapDimensions();
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

    private void loadMapDimensions()
    {
        Tuple<Integer, Integer> dimensions = getImageDimensions(image);
        mapWidth = dimensions.getFirst();
        mapHeight = dimensions.getSecond();
    }

    /**
     * Load and image from a {@link ResourceLocation} and return a {@link Tuple} containing its width and height.
     *
     * @param resourceLocation The {@link ResourceLocation} pointing to the image.
     * @return Width and height.
     */
    public static Tuple<Integer, Integer> getImageDimensions(ResourceLocation resourceLocation)
    {
        int width = 0;
        int height = 0;

        Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix("png");
        if (it.hasNext())
        {
            ImageReader reader = it.next();
            try (ImageInputStream stream = ImageIO.createImageInputStream(Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream()))
            {
                reader.setInput(stream);
                width = reader.getWidth(reader.getMinIndex());
                height = reader.getHeight(reader.getMinIndex());
            }
            catch (IOException e)
            {
                Log.logger.error(e);
            }
            finally
            {
                reader.dispose();
            }
        }

        return new Tuple<>(width, height);
    }
}
