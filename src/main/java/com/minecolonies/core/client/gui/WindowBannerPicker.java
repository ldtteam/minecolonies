package com.minecolonies.core.client.gui;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.townhall.AbstractWindowTownHall;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.util.Mth;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.translation.BaseGameTranslationConstants.BASE_GUI_DONE;
import static net.minecraft.client.gui.components.Button.DEFAULT_NARRATION;

/**
 * A custom rendered Screen (i.e. not blockui) that renders a picker for the banners,
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

    /** The number of columns the patterns are arranged in */
    private static final int PATTERN_COLUMNS = 8;

    /** The number of rows the patterns are arranged in */
    private static final int PATTERN_ROWS = 4;


    /**
     * The list of patterns that usually require charges, or are to be made more valuable
     * by excluding them from lower TH levels. Sorted by the TH level they are first introduced at
     */
    private static final ResourceKey[][] EXCLUSION = {
            {    // 1
                BannerPatterns.GRADIENT,
                BannerPatterns.GRADIENT_UP
            }, { // 2
                BannerPatterns.BRICKS,
                BannerPatterns.FLOWER
            }, { // 3
                BannerPatterns.SKULL,
                BannerPatterns.CREEPER
            }, { // 4
                BannerPatterns.GLOBE,
                BannerPatterns.PIGLIN
            }, { // 5
                BannerPatterns.MOJANG
            }, { // Excluded completely
                BannerPatterns.BASE
            }
    };

    /** The list of banner patterns, to be excluded and cached */
    private final List<Holder<BannerPattern>> patterns;

    /** The final list of patterns and colors of the flag */
    private final List<Pair<Holder<BannerPattern>, DyeColor>> layers;

    /** The colony this flag refers to */
    private final IColonyView colony;

    /** The town hall window that called this picker. Will be used to return to it. */
    private final AbstractWindowTownHall window;

    /** The assigned renderer for the banner models */
    private final ModelPart modelRender;

    /** The currently selected palette color. */
    private ColorPalette colors;

    /** The currently selected layer. Zero is the base. */
    private int activeLayer = 0;

    /** Whether or not the player is dragging the scrollbar */
    private boolean scrolling = false;

    /** The number of rows scrolled past */
    private int scrollRow = 0;

    /**
     * @param colony            the colony to make the flag for
     * @param hallWindow        the calling town hall window to return to
     * @param isFeatureUnlocked
     */
    public WindowBannerPicker(IColonyView colony, AbstractWindowTownHall hallWindow, final AtomicBoolean isFeatureUnlocked)
    {
        super(Component.literal("Flag"));

        this.colony = colony;
        this.window = hallWindow;
        this.modelRender = Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BANNER).getChild("flag");

        /* Get all patterns, then remove excluded and item-required patterns */
        List<Holder<BannerPattern>> exclusion = new ArrayList<>();
        for (int i = hallWindow.building.getBuildingLevel(); i <= hallWindow.building.getBuildingMaxLevel(); i++)
        {
            for (final ResourceKey key : EXCLUSION[i])
            {
                exclusion.add((Holder<BannerPattern>) BuiltInRegistries.BANNER_PATTERN.getHolder(key).get());
            }
        }

        this.patterns = BuiltInRegistries.BANNER_PATTERN.holders().collect(Collectors.toCollection(LinkedList::new));
        this.patterns.removeAll(exclusion);
        if (!isFeatureUnlocked.get())
        {
            this.patterns.removeIf(key -> key.unwrapKey().get().location().getNamespace().equals(Constants.MOD_ID));
        }

        // Fetch the patterns as a List and not ListNBT
        this.layers = BannerBlockEntity.createPatterns(DyeColor.WHITE, colony.getColonyFlag());
        // Remove the extra base layer created by the above function
        if (this.layers.size() > 1)
            this.layers.remove(0);
    }

    @Override
    protected void init()
    {
        int paletteX = center(this.width, PATTERN_COLUMNS, PATTERN_WIDTH, 0, 0) - 70;

        this.colors = new ColorPalette(paletteX, this.height/2, 2, this::addRenderableWidget);
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

            this.addRenderableWidget(new LayerButton(posX, GUI_Y, SIDE, SIDE, layer));
        }

        this.addRenderableWidget(new Button(
                center(this.width, 6, SIDE, 7, 0), GUI_Y,
                SIDE, SIDE,
                Component.literal(ChatFormatting.RED + "X"),
                pressed -> layers.remove(activeLayer), DEFAULT_NARRATION)
        {
            @Override
            public void renderWidget(final GuiGraphics stack, int mouseX, int mouseY, float partialTicks)
            {
                this.active = activeLayer < layers.size() && activeLayer != 0; // TODO: port this last vital condition
                super.renderWidget(stack, mouseX, mouseY, partialTicks);
            }
        });
    }

    /**
     * Creates the buttons behind each pattern.
     */
    protected void createPatternButtons()
    {
        for (int i = 0; i < patterns.size(); i++)
        {
            int posX = center(this.width, PATTERN_COLUMNS, PATTERN_WIDTH, i % PATTERN_COLUMNS, PATTERN_MARGIN);
            int posY = center(this.height+30, PATTERN_ROWS, PATTERN_HEIGHT, Math.floorDiv(i, PATTERN_COLUMNS), PATTERN_MARGIN);

            this.addRenderableWidget(new PatternButton(posX, posY, PATTERN_HEIGHT, patterns.get(i)));
        }
    }

    /**
     * Creates the Done and Cancel buttons, to return to the town hall window and save the banner or not, respectively.
     */
    protected void createCloseButtons()
    {
        this.addRenderableWidget(new Button(
                center(this.width, 2, 80, 1, 10),
                this.height - 40,
                80, SIDE,
                Component.translatable(BASE_GUI_DONE),
                pressed -> {
                    BannerPattern.Builder builder = new BannerPattern.Builder();
                    for (Pair<Holder<BannerPattern>, DyeColor> pair : layers)
                        builder.addPattern(pair.getFirst(), pair.getSecond());

                    colony.setColonyFlag(builder.toListTag());
                    window.open();
                }, DEFAULT_NARRATION
        ));
        this.addRenderableWidget(new Button(
                center(this.width, 2, 80, 0, 10),
                this.height - 40,
                80, SIDE,
                Component.translatable("gui.cancel"),
                pressed -> window.open(), DEFAULT_NARRATION
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
    public void setLayer(@Nullable Holder<BannerPattern> pattern, DyeColor color)
    {
        if (pattern == null)
        {
            // Drop out if only the color was selected.
            if (activeLayer == layers.size()) return;
            else if (activeLayer == 0) pattern = BuiltInRegistries.BANNER_PATTERN.getHolderOrThrow(BannerPatterns.BASE);
            else pattern = layers.get(activeLayer).getFirst();
        }

        if (activeLayer == layers.size())
            layers.add(new Pair<>(pattern, color));
        else
            layers.set(activeLayer, new Pair<>(pattern, color));
    }

    @Override
    public void render(final GuiGraphics stack, int mouseX, int mouseY, float partialTicks)
    {
        super.render(stack, mouseX, mouseY, partialTicks);
        drawFlag();

        // Draw the scrollbar
        int scrollRows = (int) (Math.ceil(this.patterns.size() / (float) PATTERN_COLUMNS) - PATTERN_ROWS);
        if (scrollRows > 0 && activeLayer > 0)
        {
            int trackHeight = (PATTERN_HEIGHT + PATTERN_MARGIN) * PATTERN_ROWS;
            double barHeight = trackHeight * (PATTERN_ROWS / (float)(scrollRows + PATTERN_ROWS));
            int trackX = center(this.width, PATTERN_COLUMNS, PATTERN_WIDTH, PATTERN_COLUMNS, PATTERN_MARGIN);
            int trackY = (int) (center(this.height, PATTERN_ROWS, PATTERN_HEIGHT, 0, PATTERN_MARGIN)
                                + this.scrollRow * (trackHeight / (float)(scrollRows + PATTERN_ROWS)));

            stack.fill(trackX+2, trackY, trackX+6, trackY+ (int) barHeight, 0xBBFFFFFF);
        }


        // Render the instructions
        stack.drawCenteredString(this.font,
                Component.translatable("com.minecolonies.coremod.gui.flag.choose").getString(),
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
        Lighting.setupForFlatItems();
        double posX = (this.width + PATTERN_HEIGHT/2.0 * PATTERN_COLUMNS) / 2 + SIDE *2;
        double posY = (this.height) / 2.0;

        PoseStack transform = new PoseStack();
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
    private void drawBannerPattern(Holder<BannerPattern> pattern, int x, int y)
    {
        Lighting.setupForFlatItems();

        List<Pair<Holder<BannerPattern>, DyeColor>> list = new ArrayList<>();
        list.add(new Pair<>(BuiltInRegistries.BANNER_PATTERN.getHolder(BannerPatterns.BASE).get(), DyeColor.GRAY));
        list.add(new Pair<>(pattern, DyeColor.WHITE));

        PoseStack transform = new PoseStack();
        transform.pushPose();
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
    public void renderBanner(PoseStack transform, List<Pair<Holder<BannerPattern>, DyeColor>> layers)
    {
        this.modelRender.xRot= 0.0F;
        this.modelRender.y = -32.0F;

        // TODO: move to guigraphics buffer
        MultiBufferSource.BufferSource source = this.minecraft.renderBuffers().bufferSource();
        BannerRenderer.renderPatterns(
                transform,
                source, 15728880,
                OverlayTexture.NO_OVERLAY,
                this.modelRender,
                ModelBakery.BANNER_BASE,
                true,
                layers
        );
        transform.popPose();
        source.endBatch();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        if (activeLayer > 0) {
            this.scrollRow = (int) Mth.clamp(
                    this.scrollRow - scrollY,
                    0,
                    Math.ceil(this.patterns.size() / PATTERN_COLUMNS) - PATTERN_ROWS + 1 // Extra 1 so it is inclusive
            );
        }

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int p_231044_5_)
    {
        this.scrolling = false;

        int trackX = center(this.width, PATTERN_COLUMNS, PATTERN_WIDTH, PATTERN_COLUMNS, PATTERN_MARGIN);
        int trackY = center(this.height, PATTERN_ROWS, PATTERN_HEIGHT, 0, PATTERN_MARGIN);
        int trackEnd = trackY + PATTERN_ROWS*(PATTERN_HEIGHT + PATTERN_MARGIN);
        if (mouseX > trackX + 2 && mouseX < trackX + 8 && mouseY > trackY && mouseY < trackEnd)
            this.scrolling = true;
        
        return super.mouseClicked(mouseX, mouseY, p_231044_5_);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        if (this.scrolling && this.activeLayer > 0) {

            int trackStart = center(this.height, PATTERN_ROWS, PATTERN_HEIGHT, 0, PATTERN_MARGIN);
            int trackLength = PATTERN_ROWS*(PATTERN_HEIGHT + PATTERN_MARGIN);

            double scrollRatio = Mth.clamp(
                    (mouseY - trackStart) / trackLength,
                    0, 1
            );
            this.scrollRow = (int) Math.round(scrollRatio * (Math.ceil(this.patterns.size() / PATTERN_COLUMNS) - PATTERN_ROWS + 1));

            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
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
                            ? Component.translatable("com.minecolonies.coremod.gui.flag.base_layer")
                            : Component.literal(String.valueOf(layer)),
                    pressed -> {},
                    DEFAULT_NARRATION
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
        public void renderWidget(final GuiGraphics stack, int p_render_1_, int p_render_2_, float p_render_3_)
        {
            this.active = this.layer <= layers.size();
            super.renderWidget(stack, p_render_1_, p_render_2_, p_render_3_);

            if (activeLayer == this.layer)
                stack.fill(this.getX(), this.getY(), this.getX()+this.width, this.getY()+this.height, 0x66DD99FF);
        }
    }

    /**
     * A custom button for each pattern, to override click and render logic
     */
    public class PatternButton extends Button
    {
        private final Holder<BannerPattern> pattern;
        private int index = -1;

        /**
         * @param x the left x position of the button
         * @param y the top y position of the button
         * @param height the height of the button. Twice the width, always
         * @param pattern the pattern this button represents
         */
        public PatternButton(int x, int y, int height, Holder<BannerPattern> pattern)
        {
            super(x, y, height/2, height, Component.literal(""), btn -> {}, DEFAULT_NARRATION);
            this.pattern = pattern;
            int tempIndex = 0;
            for (final Holder<BannerPattern> pat : WindowBannerPicker.this.patterns)
            {
                if (pat.value().getHashname().equals(pattern.value().getHashname()))
                {
                    this.index = tempIndex;
                    break;
                }
                tempIndex++;
            }
        }

        @Override
        public void onPress() { setLayer(this.pattern, colors.getSelected()); }

        @Override
        public void renderWidget(final GuiGraphics stack, int mx, int my, float p_renderButton_3_)
        {
            this.visible = scrollRow * PATTERN_COLUMNS <= this.index && this.index < PATTERN_COLUMNS * (scrollRow + PATTERN_ROWS);
            this.active = activeLayer != 0;

            if (!this.active || !this.visible) return;

            int position = Math.floorDiv(this.index - scrollRow*PATTERN_COLUMNS, PATTERN_COLUMNS);
            this.setY(center(WindowBannerPicker.this.height, PATTERN_ROWS, PATTERN_HEIGHT, position, PATTERN_MARGIN));
            this.isHovered = mx >= this.getX() && my >= this.getY() && mx < this.getX() + this.width && my < this.getY() + this.height;

            super.renderWidget(stack, mx, my, p_renderButton_3_);

            if (this.visible)
            {
                if (this.isHovered && this.active)
                    stack.fill(this.getX(), this.getY(), this.getX()+this.width, this.getY()+this.height, 0xDDFFFFFF);

                if (activeLayer < layers.size() && layers.get(activeLayer).getFirst() == this.pattern)
                    stack.fill(this.getX(), this.getY(), this.getX()+this.width, this.getY()+this.height, 0xFFDD88FF);

                else
                    stack.fill(this.getX(), this.getY(), this.getX()+this.width, this.getY()+this.height, 0x33888888);
            }

            try
            {
                drawBannerPattern(this.pattern, this.getX(), this.getY());
            }
            catch (final Exception ex)
            {
                Log.getLogger().warn(pattern.value().getHashname());
                Log.getLogger().error(ex);
            }
        }
    }
}
