package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.colony.IColonyView;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.BannerTileEntityRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom rendered Screen (i.e. not BlockOut) that renders a picker for the banners,
 * similar to a loom. The resulting banner cannot be extracted.
 */
@OnlyIn(Dist.CLIENT)
public class WindowBannerPicker extends Screen
{
    /** The Y position of the layers */
    private static final int GUI_Y = 30;

    /** The side length for layer and palette buttons */
    private static final int SIDE = 20;

    /** The height of the pattern buttons */
    private static final int PATTERN_HEIGHT = 30;

    /** The width of the pattern buttons */
    private static final int PATTERN_WIDTH = PATTERN_HEIGHT / 2;

    /** The margin after each pattern button */
    private static final int PATTERN_MARGIN = 3;

    /** The number of columns the patterns are arrange in */
    private static final int PATTERN_COLUMNS = 8;

    /** The list of banner patterns, cached */
    private static final BannerPattern[] PATTERNS = BannerPattern.values();

    /** The final list of patterns and colors of the flag */
    private final List<Pair<BannerPattern, DyeColor>> layers;

    /** The colony this flag refers to */
    private final IColonyView colony;

    /** The town hall window that called this picker. Will be used to return to it. */
    private final WindowTownHall window;

    /** The assigned renderer for the banner models */
    private final ModelRenderer modelRender;

    /** The currently selected palette color. */
    private ColorPalette colors;

    /** The currently selected layer. Zero is the base. */
    private int activeLayer = 0;

    /**
     * @param colony the colony to make the flag for
     * @param hallWindow the calling town hall window to return to
     */
    public WindowBannerPicker(IColonyView colony, WindowTownHall hallWindow)
    {
        super(new StringTextComponent("Flag"));

        this.colony = colony;
        this.window = hallWindow;
        this.modelRender = BannerTileEntityRenderer.getModelRender();

        // Fetch the patterns as a List and not ListNBT
        this.layers = BannerTileEntity.func_230138_a_(DyeColor.WHITE, colony.getColonyFlag());
        // Remove the extra base layer created by the above function
        if (this.layers.size() > 1)
            this.layers.remove(0);
    }

    @Override
    protected void init()
    {
        int paletteX = center(this.width, PATTERN_COLUMNS, PATTERN_WIDTH, 0, 0) - 70;

        this.colors = new ColorPalette(paletteX, this.height/2, 2, this::addButton);
        colors.onchange = color -> setLayer(null, color);

        createLayerButtons();
        createPatternButtons();
        createCloseButtons();
    }

    /**
     * Creates the buttons for banner layer selection; Base, 1-6, and the remove button
     */
    protected void createLayerButtons()
    {
        for (int layer = 0; layer <= 6; layer++)
        {
            int posX = (this.width - SIDE * 6) / 2 + layer * SIDE;

            this.addButton(new LayerButton(posX, GUI_Y, SIDE, SIDE, layer));
        }

        this.addButton(new Button(
                center(this.width, 6, SIDE, 7, 0), GUI_Y,
                SIDE, SIDE,
                TextFormatting.RED + "X",
                pressed -> layers.remove(activeLayer))
        {
            @Override
            public void render(int mouseX, int mouseY, float partialTicks)
            {
                this.active = activeLayer < layers.size();
                super.render(mouseX, mouseY, partialTicks);
            }
        });
    }

    /**
     * Creates the buttons behind each pattern.
     */
    protected void createPatternButtons()
    {
        /* Draw each of the patterns. Omit the last 8, which includes gradients and special banners */
        for (int i = 0; i < PATTERNS.length - 8; i++)
        {
            int rows = (int) Math.ceil(PATTERNS.length / PATTERN_COLUMNS);
            int posX = center(this.width, PATTERN_COLUMNS, PATTERN_WIDTH, i % PATTERN_COLUMNS, PATTERN_MARGIN);
            int posY = center(this.height+30, rows, PATTERN_HEIGHT, Math.floorDiv(i, PATTERN_COLUMNS), PATTERN_MARGIN);

            this.addButton(new PatternButton(posX, posY, PATTERN_HEIGHT, PATTERNS[i]));
        }
    }

    /**
     * Creates the Done and Cancel buttons, to return to the town hall window and save the banner or not, respectively.
     */
    protected void createCloseButtons()
    {
        this.addButton(new Button(
                center(this.width, 2, 80, 1, 10),
                this.height - 40,
                80, SIDE,
                I18n.format("gui.done"),
                pressed -> {
                    BannerPattern.Builder builder = new BannerPattern.Builder();
                    for (Pair<BannerPattern, DyeColor> pair : layers)
                        builder.setPatternWithColor(pair.getFirst(), pair.getSecond());

                    colony.setColonyFlag(builder.func_222476_a());
                    window.open();
                }
        ));
        this.addButton(new Button(
                center(this.width, 2, 80, 0, 10),
                this.height - 40,
                80, SIDE,
                I18n.format("gui.cancel"),
                pressed -> window.open()
        ));
    }

    /**
     * Positions a button within a grid based on the center coordinates of that grid.
     * This method is Axis agnostic.
     * @param length the length of the grid
     * @param count the number of items along that length
     * @param side the side length of the items in the relevant axis
     * @param n the nth item we are positioning
     * @param margin the gap between elements, half of this gap length borders the hole grid
     * @return the coordinate along the relevant axis
     */
    public static int center(int length, int count, int side, int n, int margin)
    {
        return (length - count * (side + margin)) / 2 + n * (side + margin) + margin / 2;
    }

    /**
     * Tries to set the layer in the banner pattern list with the given information
     * @param pattern the pattern to set in the layer. Uses the existing or BASE if null
     * @param color the associated color for the pattern
     */
    public void setLayer(@Nullable BannerPattern pattern, DyeColor color)
    {
        if (pattern == null)
        {
            // Drop out if only the color was selected.
            if (activeLayer == layers.size()) return;
            else if (activeLayer == 0) pattern = BannerPattern.BASE;
            else pattern = layers.get(activeLayer).getFirst();
        }

        if (activeLayer == layers.size())
            layers.add(new Pair<>(pattern, color));
        else
            layers.set(activeLayer, new Pair<>(pattern, color));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        drawFlag();

        // Render the instructions
        this.drawCenteredString(
                this.font,
                I18n.format("com.minecolonies.coremod.gui.flag.choose"),
                this.width /2,
                16,
                0xFFFFFF /* white */
        );
    }

    /**
     * Sets the large final preview of the banner for rendering
     */
    private void drawFlag()
    {
        RenderHelper.setupGuiFlatDiffuseLighting();
        double posX = (this.width + PATTERN_HEIGHT/2.0 * PATTERN_COLUMNS) / 2 + SIDE *2;
        double posY = (this.height) / 2.0;

        MatrixStack transform = new MatrixStack();
        transform.translate(posX, posY + 40, 0.0D);
        transform.scale(40.0F, -40.0F, 1.0F);
        transform.translate(0.5D, 0.5D, 0.5D);
        transform.scale(1F, -1F, -1F);

        renderBanner(transform, this.layers);
    }

    /**
     * Sets a specific banner pattern in place to be rendered
     * @param pattern the banner pattern to render
     * @param x the left x position of the banner
     * @param y the top y position of the banner
     */
    private void drawBannerPattern(BannerPattern pattern, int x, int y)
    {
        RenderHelper.setupGuiFlatDiffuseLighting();

        List<Pair<BannerPattern, DyeColor>> list = new ArrayList<>();
        list.add(new Pair<>(BannerPattern.BASE, DyeColor.GRAY));
        list.add(new Pair<>(pattern, DyeColor.WHITE));

        MatrixStack transform = new MatrixStack();
        transform.push();
        transform.translate(x+2.5, y + 29, 0.0D);
        transform.scale(10.0F, -11.0F, 1.0F);
        transform.translate(0.5D, 0.5D, 0.5D);
        transform.scale(1F, -1F, -1F);

        renderBanner(transform, list);
    }

    /**
     * Renders the provided banner using the given transformations
     * @param transform the transformation matrix stack to render with
     * @param layers the pattern-color pairs that form the banner
     */
    public void renderBanner(MatrixStack transform, List<Pair<BannerPattern, DyeColor>> layers)
    {
        this.modelRender.rotateAngleX = 0.0F;
        this.modelRender.rotationPointY = -32.0F;

        IRenderTypeBuffer.Impl source = this.minecraft.getRenderTypeBuffers().getBufferSource();
        BannerTileEntityRenderer.func_230180_a_(
                transform,
                source, 15728880,
                OverlayTexture.NO_OVERLAY,
                this.modelRender,
                ModelBakery.LOCATION_BANNER_BASE,
                true,
                layers
        );
        transform.pop();
        source.finish();
    }

    /**
     * A custom button for each layer, to override click and render logic
     */
    public class LayerButton extends Button
    {
        private final int layer;

        /**
         * @param x the left x position of the button
         * @param y the top y position of the button
         * @param width the width of the button. Probably 20. Overridden if layer is 0.
         * @param height the height of the button. Probably 20.
         * @param layer the layer this button represents.
         */
        public LayerButton(int x, int y, int width, int height, int layer)
        {
            super(
                    x - (layer == 0 ? width*2 : 0), y,
                    width * (layer == 0 ? 3 : 1), height,
                    layer == 0
                            ? I18n.format("com.minecolonies.coremod.gui.flag.base_layer")
                            : String.valueOf(layer),
                    pressed -> {}
            );
            this.layer = layer;
        }

        @Override
        public void onPress()
        {
            activeLayer = this.layer;

            if (this.layer >= layers.size())
                colors.setSelected(layers.get(0).getSecond().equals(DyeColor.BLACK) ? DyeColor.WHITE : DyeColor.BLACK);
            else
                colors.setSelected(layers.get(activeLayer).getSecond());
        }

        @Override
        public void render(int p_render_1_, int p_render_2_, float p_render_3_)
        {
            this.active = this.layer <= layers.size();
            super.render(p_render_1_, p_render_2_, p_render_3_);

            if (activeLayer == this.layer)
                fill(this.x, this.y, this.x+this.width, this.y+this.height, 0x66DD99FF);
        }
    }

    /**
     * A custom button for each pattern, to override click and render logic
     */
    public class PatternButton extends Button
    {
        private final BannerPattern pattern;

        /**
         * @param x the left x position of the button
         * @param y the top y position of the button
         * @param height the height of the button. Twice the width, always
         * @param pattern the pattern this button represents
         */
        public PatternButton(int x, int y, int height, BannerPattern pattern)
        {
            super(x, y, height/2, height, "", btn -> {});
            this.pattern = pattern;
        }

        @Override
        public void onPress() { setLayer(this.pattern, colors.getSelected()); }

        @Override
        public void render(int p_render_1_, int p_render_2_, float p_render_3_)
        {
            this.active = activeLayer != 0;

            if (!this.active) return;

            super.render(p_render_1_, p_render_2_, p_render_3_);

            drawBannerPattern(this.pattern, this.x, this.y);
        }

        @Override
        public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_)
        {
            if (this.visible)
            {
                if (this.isHovered() && this.active)
                    fill(this.x, this.y, this.x+this.width, this.y+this.height, 0xDDFFFFFF);

                if (activeLayer < layers.size() && layers.get(activeLayer).getFirst() == this.pattern)
                    fill(this.x, this.y, this.x+this.width, this.y+this.height, 0xFFDD88FF);

                else
                    fill(this.x, this.y, this.x+this.width, this.y+this.height, 0x33888888);
            }
        }
    }
}
