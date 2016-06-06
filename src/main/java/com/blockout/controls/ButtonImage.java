package com.blockout.controls;

import com.blockout.Alignment;
import com.blockout.PaneParams;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;

/**
 * Clickable image.
 */
public class ButtonImage extends Button
{
    protected ResourceLocation image;
    protected ResourceLocation imageHighlight;
    protected ResourceLocation imageDisabled;

    protected int imageOffsetX = 0;
    protected int imageOffsetY = 0;
    protected int imageWidth = 0;
    protected int imageHeight = 0;
    protected int imageMapWidth = Image.MINECRAFT_DEFAULT_TEXTURE_MAP_SIZE;
    protected int imageMapHeight = Image.MINECRAFT_DEFAULT_TEXTURE_MAP_SIZE;

    protected int highlightOffsetX = 0;
    protected int highlightOffsetY = 0;
    protected int highlightWidth = 0;
    protected int highlightHeight = 0;
    protected int highlightMapWidth = Image.MINECRAFT_DEFAULT_TEXTURE_MAP_SIZE;
    protected int highlightMapHeight = Image.MINECRAFT_DEFAULT_TEXTURE_MAP_SIZE;

    protected int disabledOffsetX = 0;
    protected int disabledOffsetY = 0;
    protected int disabledWidth = 0;
    protected int disabledHeight = 0;
    protected int disabledMapWidth = Image.MINECRAFT_DEFAULT_TEXTURE_MAP_SIZE;
    protected int disabledMapHeight = Image.MINECRAFT_DEFAULT_TEXTURE_MAP_SIZE;

    protected float textScale = 1.0F;
    protected Alignment textAlignment = Alignment.Middle;
    protected int textColor = 0xffffff;
    protected int textHoverColor = 0xffffff;
    protected int textDisabledColor = 0xffffff;
    protected boolean shadow = false;
    protected int textOffsetX = 0;
    protected int textOffsetY = 0;

    /**
     * Default constructor. Makes a small square button.
     */
    public ButtonImage()
    {
        super();
        setSize(20, 20);
    }

    /**
     * Constructor called by the xml loader.
     *
     * @param params PaneParams provided in the xml.
     */
    public ButtonImage(PaneParams params)
    {
        super(params);

        String path = params.getStringAttribute("source", null);
        if (path != null)
        {
            image = new ResourceLocation(path);
            loadImageDimensions();
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

        path = params.getStringAttribute("highlight", null);
        if (path != null)
        {
            imageHighlight = new ResourceLocation(path);
            loadImageHighlightDimensions();
        }

        size = params.getSizePairAttribute("highlightoffset", null, null);
        if (size != null)
        {
            highlightOffsetX = size.x;
            highlightOffsetY = size.y;
        }

        size = params.getSizePairAttribute("highlightsize", null, null);
        if (size != null)
        {
            highlightWidth = size.x;
            highlightHeight = size.y;
        }

        path = params.getStringAttribute("disabled", null);
        if (path != null)
        {
            imageDisabled = new ResourceLocation(path);
            loadImageDisabledDimensions();
        }

        size = params.getSizePairAttribute("disabledoffset", null, null);
        if (size != null)
        {
            disabledOffsetX = size.x;
            disabledOffsetY = size.y;
        }

        size = params.getSizePairAttribute("disabledsize", null, null);
        if (size != null)
        {
            disabledWidth = size.x;
            disabledHeight = size.y;
        }

        textScale = params.getFloatAttribute("scale", textScale);
        textAlignment = params.getEnumAttribute("textalign", textAlignment);
        textColor = params.getColorAttribute("textcolor", textColor);
        // match textColor by default
        textHoverColor = params.getColorAttribute("texthovercolor", textColor);
        // match textColor by default
        textDisabledColor = params.getColorAttribute("textdisabledcolor", textColor);
        shadow = params.getBooleanAttribute("shadow", shadow);

        size = params.getSizePairAttribute("textoffset", null, null);
        if (size != null)
        {
            textOffsetX = size.x;
            textOffsetY = size.y;
        }
    }

    /**
     * Set the default image.
     *
     * @param source String path.
     */
    public void setImage(String source)
    {
        setImage(source, 0, 0, 0, 0);
    }

    /**
     * Set the default image.
     *
     * @param source  String path.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImage(String source, int offsetX, int offsetY, int w, int h)
    {
        setImage(source != null ? new ResourceLocation(source) : null, offsetX, offsetY, w, h);
    }

    /**
     * Set the default image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImage(ResourceLocation loc)
    {
        setImage(loc, 0, 0, 0, 0);
    }

    /**
     * Set the default image.
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
        imageHeight = w;
        imageWidth = h;

        loadImageDimensions();
    }

    private void loadImageDimensions()
    {
        Tuple<Integer, Integer> dimensions = Image.getImageDimensions(image);
        imageMapWidth = dimensions.getFirst();
        imageMapHeight = dimensions.getSecond();
    }

    /**
     * Set the hover image.
     *
     * @param source String path.
     */
    public void setImageHighlight(String source)
    {
        setImageHighlight(source, 0, 0, 0, 0);
    }

    /**
     * Set the hover image.
     *
     * @param source  String path.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImageHighlight(String source, int offsetX, int offsetY, int w, int h)
    {
        setImageHighlight(source != null ? new ResourceLocation(source) : null, offsetX, offsetY, w, h);
    }

    /**
     * Set the hover image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImageHighlight(ResourceLocation loc)
    {
        setImageHighlight(loc, 0, 0, 0, 0);
    }

    /**
     * Set the hover image.
     *
     * @param loc     ResourceLocation for the image.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImageHighlight(ResourceLocation loc, int offsetX, int offsetY, int w, int h)
    {
        imageHighlight = loc;
        highlightOffsetX = offsetX;
        highlightOffsetY = offsetY;
        highlightHeight = w;
        highlightWidth = h;

        loadImageHighlightDimensions();
    }

    private void loadImageHighlightDimensions()
    {
        Tuple<Integer, Integer> dimensions = Image.getImageDimensions(imageHighlight);
        highlightMapWidth = dimensions.getFirst();
        highlightMapHeight = dimensions.getSecond();
    }

    /**
     * Set the disabled image.
     *
     * @param source String path.
     */
    public void setImageDisabled(String source)
    {
        setImageHighlight(source, 0, 0, 0, 0);
    }

    /**
     * Set the disabled image.
     *
     * @param source  String path.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImageDisabled(String source, int offsetX, int offsetY, int w, int h)
    {
        setImageHighlight(source != null ? new ResourceLocation(source) : null, offsetX, offsetY, w, h);
    }

    /**
     * Set the disabled image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImageDisabled(ResourceLocation loc)
    {
        setImageHighlight(loc, 0, 0, 0, 0);
    }

    /**
     * Set the disabled image.
     *
     * @param loc     ResourceLocation for the image.
     * @param offsetX image x offset.
     * @param offsetY image y offset.
     * @param w       image width.
     * @param h       image height.
     */
    public void setImageDisabled(ResourceLocation loc, int offsetX, int offsetY, int w, int h)
    {
        imageDisabled = loc;
        disabledOffsetX = offsetX;
        disabledOffsetY = offsetY;
        disabledHeight = w;
        disabledWidth = h;

        //Get file dimension
        Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix("png");
        if (it.hasNext())
        {
            ImageReader reader = it.next();
            try (ImageInputStream stream = ImageIO.createImageInputStream(Minecraft.getMinecraft().getResourceManager().getResource(image).getInputStream()))
            {
                reader.setInput(stream);
                disabledMapWidth = reader.getWidth(reader.getMinIndex());
                disabledMapHeight = reader.getHeight(reader.getMinIndex());
            } catch (IOException e)
            {
                e.printStackTrace();
            } finally
            {
                reader.dispose();
            }
        }
    }

    private void loadImageDisabledDimensions()
    {
        Tuple<Integer, Integer> dimensions = Image.getImageDimensions(imageDisabled);
        disabledMapWidth = dimensions.getFirst();
        disabledMapHeight = dimensions.getSecond();
    }

    /**
     * @return The standard text color.
     */
    public int getTextColor()
    {
        return textColor;
    }

    /**
     * @return The text color when you hover the button.
     */
    public int getTextHoverColor()
    {
        return textHoverColor;
    }

    /**
     * @return The text color when the button is disabled.
     */
    public int getTextDisabledColor()
    {
        return textDisabledColor;
    }

    /**
     * Set the standard text color.
     *
     * @param c New text color.
     */
    public void setTextColor(int c)
    {
        setTextColor(c, c, c);
    }

    /**
     * Set all text colors.
     *
     * @param c Standard text color.
     * @param d Disabled text color.
     * @param h Hover text color.
     */
    public void setTextColor(int c, int d, int h)
    {
        textColor = c;
        textDisabledColor = d;
        textHoverColor = h;
    }

    /**
     * @return true if the shadow is enabled.
     */
    public boolean getShadow()
    {
        return shadow;
    }

    /**
     * Used to enabled or disable shadow.
     *
     * @param s true to enable shadow.
     */
    public void setShadow(boolean s)
    {
        shadow = s;
    }

    /**
     * @return the Text {@link Alignment}.
     */
    public Alignment getTextAlignment()
    {
        return textAlignment;
    }

    /**
     * Set the label text {@link Alignment}.
     *
     * @param align text alignment.
     */
    public void setTextAlignment(Alignment align)
    {
        textAlignment = align;
    }

    /**
     * @return The text scale.
     */
    public float getTextScale()
    {
        return textScale;
    }

    /**
     * Set the text scale.
     *
     * @param s New text scale.
     */
    public void setTextScale(float s)
    {
        textScale = s;
    }

    /**
     * Text height is calculated by multiplying FONT_HEIGHT and text scale.
     *
     * @return The text height.
     */
    public int getTextHeight()
    {
        return BigDecimal.valueOf(mc.fontRendererObj.FONT_HEIGHT).multiply(BigDecimal.valueOf(textScale)).intValue();
    }

    /**
     * The label width is calculated by multiplying the normal string width by the text scale.
     *
     * @return The width of the label.
     */
    public int getStringWidth()
    {
        return BigDecimal.valueOf(mc.fontRendererObj.getStringWidth(label)).multiply(BigDecimal.valueOf(textScale)).intValue();
    }

    /**
     * Draw the button. Decide what image to use, and possibly draw label.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    protected void drawSelf(int mx, int my)
    {
        ResourceLocation bind = image;
        int offsetX = imageOffsetX;
        int offsetY = imageOffsetY;
        int w = imageWidth;
        int h = imageHeight;
        int mapWidth = imageMapWidth;
        int mapHeight = imageMapHeight;

        boolean mouseOver = isPointInPane(mx, my);

        if (!enabled)
        {
            if (imageDisabled != null)
            {
                bind = imageDisabled;
                offsetX = disabledOffsetX;
                offsetY = disabledOffsetY;
                w = disabledWidth;
                h = disabledHeight;
                mapWidth = disabledMapWidth;
                mapHeight = disabledMapHeight;
            }
        }
        else if (mouseOver && imageHighlight != null)
        {
            bind = imageHighlight;
            offsetX = highlightOffsetX;
            offsetY = highlightOffsetY;
            w = highlightWidth;
            h = highlightHeight;
            mapWidth = highlightMapWidth;
            mapHeight = highlightMapHeight;
        }

        if (w == 0 || w > getWidth())
        {
            w = getWidth();
        }
        if (h == 0 || h > getHeight())
        {
            h = getHeight();
        }

        mc.renderEngine.bindTexture(bind);
        if (enabled || imageDisabled != null)
        {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
        else
        {
            GL11.glColor4f(0.5F, 0.5F, 0.5F, 1.0F);
        }

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        //Draw
        drawModalRectWithCustomSizedTexture(x, y, offsetX, offsetY, w, h, mapWidth, mapHeight);

        //  Label, if any
        if (label != null)
        {
            int color = enabled ? (mouseOver ? textHoverColor : textColor) : textDisabledColor;

            offsetX = textOffsetX;
            offsetY = textOffsetY;

            if (textAlignment.rightAligned)
            {
                offsetX += (getWidth() - getStringWidth());
            }
            else if (textAlignment.horizontalCentered)
            {
                offsetX += (getWidth() - getStringWidth()) / 2;
            }

            if (textAlignment.bottomAligned)
            {
                offsetY += (getHeight() - getTextHeight());
            }
            else if (textAlignment.verticalCentered)
            {
                offsetY += (getHeight() - getTextHeight()) / 2;
            }

            GL11.glPushMatrix();
            GL11.glTranslatef(textScale, textScale, textScale);
            mc.fontRendererObj.drawString(label, getX() + offsetX, getY() + offsetY, color, shadow);
            GL11.glPopMatrix();
        }
    }
}
