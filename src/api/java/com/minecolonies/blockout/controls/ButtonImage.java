package com.ldtteam.blockout.controls;

import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.PaneParams;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import org.lwjgl.opengl.GL11;

/**
 * Clickable image.
 */
public class ButtonImage extends Button
{
    /**
     * Default size is a small square button.
     */
    private static final int   DEFAULT_BUTTON_SIZE = 20;
    private static final float HALF                = 0.5F;
    protected ResourceLocation image;
    protected ResourceLocation imageHighlight;
    protected ResourceLocation imageDisabled;
    protected int       imageOffsetX       = 0;
    protected int       imageOffsetY       = 0;
    protected int       imageWidth         = 0;
    protected int       imageHeight        = 0;
    protected int       imageMapWidth      = Image.MINECRAFT_DEFAULT_TEXTURE_MAP_SIZE;
    protected int       imageMapHeight     = Image.MINECRAFT_DEFAULT_TEXTURE_MAP_SIZE;
    protected int       highlightOffsetX   = 0;
    protected int       highlightOffsetY   = 0;
    protected int       highlightWidth     = 0;
    protected int       highlightHeight    = 0;
    protected int       highlightMapWidth  = Image.MINECRAFT_DEFAULT_TEXTURE_MAP_SIZE;
    protected int       highlightMapHeight = Image.MINECRAFT_DEFAULT_TEXTURE_MAP_SIZE;
    protected int       disabledOffsetX    = 0;
    protected int       disabledOffsetY    = 0;
    protected int       disabledWidth      = 0;
    protected int       disabledHeight     = 0;
    protected int       disabledMapWidth   = Image.MINECRAFT_DEFAULT_TEXTURE_MAP_SIZE;
    protected int       disabledMapHeight  = Image.MINECRAFT_DEFAULT_TEXTURE_MAP_SIZE;
    protected double    textScale          = 1.0;
    protected Alignment textAlignment      = Alignment.MIDDLE;
    protected int       textColor          = 0xffffff;
    protected int       textHoverColor     = 0xffffff;
    protected int       textDisabledColor  = 0xffffff;
    protected boolean   shadow             = false;
    protected int       textOffsetX        = 0;
    protected int       textOffsetY        = 0;

    /**
     * Default constructor. Makes a small square button.
     */
    public ButtonImage()
    {
        super();

        width = DEFAULT_BUTTON_SIZE;
        height = DEFAULT_BUTTON_SIZE;
    }

    /**
     * Constructor called by the xml loader.
     *
     * @param params PaneParams provided in the xml.
     */
    public ButtonImage(final PaneParams params)
    {
        super(params);

        loadImageInfo(params);
        loadHighlightInfo(params);
        loadDisabledInfo(params);

        loadTextInfo(params);
    }

    /**
     * Loads the parameters for the normal image.
     *
     * @param params PaneParams provided in the xml.
     */
    private void loadImageInfo(final PaneParams params)
    {
        final String path = params.getStringAttribute("source", null);
        if (path != null)
        {
            image = new ResourceLocation(path);
            loadImageDimensions();
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
     * Loads the parameters for the hover image.
     *
     * @param params PaneParams provided in the xml.
     */
    private void loadHighlightInfo(final PaneParams params)
    {
        final String path = params.getStringAttribute("highlight", null);
        if (path != null)
        {
            imageHighlight = new ResourceLocation(path);
            loadImageHighlightDimensions();
        }

        PaneParams.SizePair size = params.getSizePairAttribute("highlightoffset", null, null);
        if (size != null)
        {
            highlightOffsetX = size.getX();
            highlightOffsetY = size.getY();
        }

        size = params.getSizePairAttribute("highlightsize", null, null);
        if (size != null)
        {
            highlightWidth = size.getX();
            highlightHeight = size.getY();
        }
    }

    /**
     * Loads the parameters for the disabled image.
     *
     * @param params PaneParams provided in the xml.
     */
    private void loadDisabledInfo(final PaneParams params)
    {
        final String path = params.getStringAttribute("disabled", null);
        if (path != null)
        {
            imageDisabled = new ResourceLocation(path);
            loadImageDisabledDimensions();
        }

        PaneParams.SizePair size = params.getSizePairAttribute("disabledoffset", null, null);
        if (size != null)
        {
            disabledOffsetX = size.getX();
            disabledOffsetY = size.getY();
        }

        size = params.getSizePairAttribute("disabledsize", null, null);
        if (size != null)
        {
            disabledWidth = size.getX();
            disabledHeight = size.getY();
        }
    }

    /**
     * Loads the parameters for the button textContent.
     *
     * @param params PaneParams provided in the xml.
     */
    private void loadTextInfo(final PaneParams params)
    {
        textScale = params.getDoubleAttribute("textscale", textScale);
        textAlignment = params.getEnumAttribute("textalign", Alignment.class, textAlignment);
        textColor = params.getColorAttribute("textcolor", textColor);
        // match textColor by default
        textHoverColor = params.getColorAttribute("texthovercolor", textColor);
        // match textColor by default
        textDisabledColor = params.getColorAttribute("textdisabledcolor", textColor);
        shadow = params.getBooleanAttribute("shadow", shadow);

        final PaneParams.SizePair size = params.getSizePairAttribute("textoffset", null, null);
        if (size != null)
        {
            textOffsetX = size.getX();
            textOffsetY = size.getY();
        }
    }

    /**
     * Uses {@link Image#getImageDimensions(ResourceLocation)} to determine the dimensions of image texture.
     */
    private void loadImageDimensions()
    {
        final Tuple<Integer, Integer> dimensions = Image.getImageDimensions(image);
        imageMapWidth = dimensions.getA();
        imageMapHeight = dimensions.getB();
    }

    /**
     * Uses {@link Image#getImageDimensions(ResourceLocation)} to determine the dimensions of hover image texture.
     */
    private void loadImageHighlightDimensions()
    {
        final Tuple<Integer, Integer> dimensions = Image.getImageDimensions(imageHighlight);
        highlightMapWidth = dimensions.getA();
        highlightMapHeight = dimensions.getB();
    }

    /**
     * Uses {@link Image#getImageDimensions(ResourceLocation)} to determine the dimensions of disabled image texture.
     */
    private void loadImageDisabledDimensions()
    {
        final Tuple<Integer, Integer> dimensions = Image.getImageDimensions(imageDisabled);
        disabledMapWidth = dimensions.getA();
        disabledMapHeight = dimensions.getB();
    }

    /**
     * Set the default image.
     *
     * @param source String path.
     */
    public void setImage(final String source)
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
    public void setImage(final String source, final int offsetX, final int offsetY, final int w, final int h)
    {
        setImage(source != null ? new ResourceLocation(source) : null, offsetX, offsetY, w, h);
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
    public void setImage(final ResourceLocation loc, final int offsetX, final int offsetY, final int w, final int h)
    {
        image = loc;
        imageOffsetX = offsetX;
        imageOffsetY = offsetY;
        imageHeight = w;
        imageWidth = h;

        loadImageDimensions();
    }

    /**
     * Set the default image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImage(final ResourceLocation loc)
    {
        setImage(loc, 0, 0, 0, 0);
    }

    /**
     * Set the hover image.
     *
     * @param source String path.
     */
    public void setImageHighlight(final String source)
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
    public void setImageHighlight(final String source, final int offsetX, final int offsetY, final int w, final int h)
    {
        setImageHighlight(source != null ? new ResourceLocation(source) : null, offsetX, offsetY, w, h);
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
    public void setImageHighlight(final ResourceLocation loc, final int offsetX, final int offsetY, final int w, final int h)
    {
        imageHighlight = loc;
        highlightOffsetX = offsetX;
        highlightOffsetY = offsetY;
        highlightHeight = w;
        highlightWidth = h;

        loadImageHighlightDimensions();
    }

    /**
     * Set the hover image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImageHighlight(final ResourceLocation loc)
    {
        setImageHighlight(loc, 0, 0, 0, 0);
    }

    /**
     * Set the disabled image.
     *
     * @param source String path.
     */
    public void setImageDisabled(final String source)
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
    public void setImageDisabled(final String source, final int offsetX, final int offsetY, final int w, final int h)
    {
        setImageHighlight(source != null ? new ResourceLocation(source) : null, offsetX, offsetY, w, h);
    }

    /**
     * Set the disabled image.
     *
     * @param loc ResourceLocation for the image.
     */
    public void setImageDisabled(final ResourceLocation loc)
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
    public void setImageDisabled(final ResourceLocation loc, final int offsetX, final int offsetY, final int w, final int h)
    {
        imageDisabled = loc;
        disabledOffsetX = offsetX;
        disabledOffsetY = offsetY;
        disabledHeight = w;
        disabledWidth = h;

        loadImageDisabledDimensions();
    }

    /**
     * @return The standard textContent color.
     */
    public int getTextColor()
    {
        return textColor;
    }

    /**
     * Set the standard textContent color.
     *
     * @param c New textContent color.
     */
    public void setTextColor(final int c)
    {
        setTextColor(c, c, c);
    }

    /**
     * Set all textContent colors.
     *
     * @param c Standard textContent color.
     * @param d Disabled textContent color.
     * @param h Hover textContent color.
     */
    public void setTextColor(final int c, final int d, final int h)
    {
        textColor = c;
        textDisabledColor = d;
        textHoverColor = h;
    }

    /**
     * @return The textContent color when you hover the button.
     */
    public int getTextHoverColor()
    {
        return textHoverColor;
    }

    /**
     * @return The textContent color when the button is disabled.
     */
    public int getTextDisabledColor()
    {
        return textDisabledColor;
    }

    /**
     * @return true if the shadow is enabled.
     */
    public boolean hasShadow()
    {
        return shadow;
    }

    /**
     * Used to enabled or disable shadow.
     *
     * @param s true to enable shadow.
     */
    public void setShadow(final boolean s)
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
     * Set the textContent textContent {@link Alignment}.
     *
     * @param align textContent alignment.
     */
    public void setTextAlignment(final Alignment align)
    {
        textAlignment = align;
    }

    /**
     * @return The textContent scale.
     */
    public double getTextScale()
    {
        return textScale;
    }

    /**
     * Set the textContent scale.
     *
     * @param s New textContent scale.
     */
    public void setTextScale(final float s)
    {
        textScale = s;
    }

    /**
     * Draw the button.
     * Decide what image to use, and possibly draw textContent.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    public void drawSelf(final int mx, final int my)
    {
        final boolean mouseOver = isPointInPane(mx, my);

        drawImage(mouseOver);
        drawlabel(mouseOver);
    }

    /**
     * Draw the correct image.
     *
     * @param mouseOver Is the mouse hovering over the button.
     */
    private void drawImage(final boolean mouseOver)
    {
        ResourceLocation bind = image;
        int offsetX = imageOffsetX;
        int offsetY = imageOffsetY;
        int w = imageWidth;
        int h = imageHeight;
        int mapWidth = imageMapWidth;
        int mapHeight = imageMapHeight;

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
            else
            {
                return;
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

        setupOpenGL(bind);

        //Draw
        drawModalRectWithCustomSizedTexture(x, y, offsetX, offsetY, w, h, mapWidth, mapHeight);

        GlStateManager.disableBlend();
    }

    /**
     * Draw the textContent if there is one.
     *
     * @param mouseOver If the mouse hovering over the button.
     */
    private void drawlabel(final boolean mouseOver)
    {
        if (label != null)
        {
            final int color = enabled ? (mouseOver ? textHoverColor : textColor) : textDisabledColor;

            int offsetX = textOffsetX;
            int offsetY = textOffsetY;

            if (textAlignment.isRightAligned())
            {
                offsetX += (getWidth() - getStringWidth());
            }
            else if (textAlignment.isHorizontalCentered())
            {
                offsetX += (getWidth() - getStringWidth()) / 2;
            }

            if (textAlignment.isBottomAligned())
            {
                offsetY += (getHeight() - getTextHeight());
            }
            else if (textAlignment.isVerticalCentered())
            {
                offsetY += (getHeight() - getTextHeight()) / 2;
            }

            GlStateManager.pushMatrix();
            GlStateManager.scale((float) textScale, (float) textScale, (float) textScale);
            mc.fontRenderer.drawString(label, (float) (getX() + offsetX), (float) (getY() + offsetY), color, shadow);
            GlStateManager.popMatrix();
        }
    }

    /**
     * Bind texture, set color, and enable blending.
     *
     * @param texture The texture to bind.
     */
    private void setupOpenGL(final ResourceLocation texture)
    {
        this.mc.getTextureManager().bindTexture(texture);
        if (this.enabled || this.imageDisabled != null)
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        else
        {
            GlStateManager.color(HALF, HALF, HALF, 1.0F);
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * The textContent width is calculated by multiplying the normal string width by the textContent scale.
     *
     * @return The width of the textContent.
     */
    public int getStringWidth()
    {
        return (int) (mc.fontRenderer.getStringWidth(label) * textScale);
    }

    /**
     * Text height is calculated by multiplying FONT_HEIGHT and textContent scale.
     *
     * @return The textContent height.
     */
    public int getTextHeight()
    {
        return (int) (mc.fontRenderer.FONT_HEIGHT * textScale);
    }
}
