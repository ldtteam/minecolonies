package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.colony.ColonyFlagChangeMessage;
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
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class WindowBannerPicker extends Screen
{
    /** The number of columns the color palette is arranged in */
    private static final int COLUMNS = 2;

    /** The Y position of the layers */
    private static final int GUI_Y = 30;

    /** The side length for layer and palette buttons */
    private static final int SIDE_LENGTH = 20;

    /** The height of the Pattern Buttons */
    private static final int PATTERN_HEIGHT = 30;

    /** The margin after each pattern button */
    private static final int PATTERN_MARGIN = 3;

    /** The number of columns the patterns are arrange in */
    private static final int PATTERN_COLUMNS = 8;

    /** The list of banner patterns, cached */
    private static final BannerPattern[] PATTERNS = BannerPattern.values();

    /** The list of banenr colors, cached **/
    private static final DyeColor[] DYES = DyeColor.values();

    /** The currently selected layer. Zero is the base. */
    private int activeLayer = 0;

    /** The currently selected palette color. */
    private DyeColor selectedColor = DyeColor.WHITE;

    /** The final list of patterns and colors of the flag */
    private final List<Pair<BannerPattern, DyeColor>> layers;

    private IColonyView colony;

    private final ModelRenderer modelRender;

    public WindowBannerPicker(IColonyView colony) {
        super(new StringTextComponent("Flag"));

        this.colony = colony;
        this.modelRender = BannerTileEntityRenderer.getModelRender();

        LogManager.getLogger().info(colony.getColonyFlag());
        this.layers = BannerTileEntity.func_230138_a_(DyeColor.WHITE, colony.getColonyFlag());
        // Remove the extra base layer created by the above function
        if (this.layers.size() > 1)
            this.layers.remove(0);
    }

    @Override
    protected void init()
    {
        int paletteX = (int) ((this.width - PATTERN_HEIGHT/2 * PATTERN_COLUMNS) / 2 - SIDE_LENGTH * 2.0);
        int paletteY = GUI_Y + SIDE_LENGTH; // Account for the "Base" button above it.

        /* Draw the colour palette */
        for (DyeColor color : DYES)
        {
            int posX = paletteX + (color.getId() % COLUMNS) * SIDE_LENGTH;
            int posY = paletteY + SIDE_LENGTH * Math.floorDiv(color.getId(), COLUMNS);

            this.addButton(new PaletteButton(posX, posY, SIDE_LENGTH, color));
        }

        /* Draw the layer selection */
        for (int layer = 0; layer <= 6; layer++)
        {
            int posX = (this.width - SIDE_LENGTH * 6) / 2 + layer * SIDE_LENGTH;

            this.addButton(new LayerButton(posX, GUI_Y, SIDE_LENGTH, SIDE_LENGTH, layer));
        }

        /* Draw each of the patterns. Omit the last 8, which includes gradients and special banners */
        for (int i = 0; i < PATTERNS.length - 8; i++)
        {
            int posX = paletteX + 2*SIDE_LENGTH + (PATTERN_HEIGHT/2 + PATTERN_MARGIN) * (i % PATTERN_COLUMNS);
            int posY = paletteY + Math.floorDiv(i, PATTERN_COLUMNS) * (PATTERN_HEIGHT + PATTERN_MARGIN);

            this.addButton(new PatternButton(posX, posY, PATTERN_HEIGHT, PATTERNS[i]));
        }

        int x = (this.width - SIDE_LENGTH * 6) / 2 + 7 * SIDE_LENGTH;
        Button removeLayerButton = new Button(
                x, GUI_Y, SIDE_LENGTH, SIDE_LENGTH,
                TextFormatting.RED + "X",
                pressed -> layers.remove(activeLayer))
        {
            @Override
            public void render(int mouseX, int mouseY, float partialTicks)
            {
                this.active = activeLayer < layers.size();
                super.render(mouseX, mouseY, partialTicks);
            }
        };
        this.addButton(removeLayerButton);

        this.addButton(new Button(
                (int) ((this.width + PATTERN_HEIGHT/2.0 * PATTERN_COLUMNS) / 2 + SIDE_LENGTH*2),
                GUI_Y + SIDE_LENGTH * 9,
                80, SIDE_LENGTH,
                I18n.format("gui.done"),
                pressed -> {}
        ) {
            @Override
            public void onPress()
            {
                BannerPattern.Builder builder = new BannerPattern.Builder();
                for (Pair<BannerPattern, DyeColor> pair : layers)
                    builder.setPatternWithColor(pair.getFirst(), pair.getSecond());

                colony.setColonyFlag(builder.func_222476_a());
                onClose();
            }
        });
        this.addButton(new Button(
                (int) ((this.width + PATTERN_HEIGHT/2.0 * PATTERN_COLUMNS) / 2 + SIDE_LENGTH*2 - 90),
                GUI_Y + SIDE_LENGTH * 9,
                80, SIDE_LENGTH,
                I18n.format("gui.cancel"),
                pressed -> onClose()
        ));
    }

    protected List<Pair<BannerPattern, DyeColor>> getPatterns () { return this.layers; }

    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();

        RenderHelper.setupGuiFlatDiffuseLighting();
        drawFlag();

        super.render(mouseX, mouseY, partialTicks);

        // Render the instructions
        this.drawCenteredString(
                this.font,
                "Choose a colored pattern for up to 6 layers on your flag.",
                this.width /2,
                16,
                0xFFFFFF
        );
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

    }

    private void drawFlag()
    {
        double posX = (this.width + PATTERN_HEIGHT/2.0 * PATTERN_COLUMNS) / 2 + SIDE_LENGTH*2;
        double posY = (this.height) / 2.0;

        MatrixStack transform = new MatrixStack();
        transform.translate(posX, posY + 40, 0.0D);
        transform.scale(40.0F, -40.0F, 1.0F);
        transform.translate(0.5D, 0.5D, 0.5D);
        transform.scale(1F, -1F, -1F);
        this.modelRender.rotateAngleX = 0.0F;
        this.modelRender.rotationPointY = -32.0F;

        drawBanner(transform, this.layers);
    }

    private void drawBannerPattern(BannerPattern pattern, int x, int y) {
        RenderHelper.setupGuiFlatDiffuseLighting();

        List<Pair<BannerPattern, DyeColor>> list = new ArrayList<>();
        list.add(new Pair<>(
                BannerPattern.BASE,
                this.selectedColor.equals(DyeColor.WHITE) ? DyeColor.LIGHT_GRAY : DyeColor.WHITE)
        );
        list.add(new Pair<>(
                pattern,
                this.selectedColor
        ));

        MatrixStack transform = new MatrixStack();
        transform.push();
        transform.translate(x+2.5, y + 29, 0.0D);
        transform.scale(10.0F, -11.0F, 1.0F);
        transform.translate(0.5D, 0.5D, 0.5D);
        transform.scale(1F, -1F, -1F);

        this.modelRender.rotateAngleX = 0.0F;
        this.modelRender.rotationPointY = -32.0F;

        drawBanner(transform, list);
    }

    public void drawBanner(MatrixStack transform, List<Pair<BannerPattern, DyeColor>> layers)
    {
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


    public class PaletteButton extends Button
    {
        private final DyeColor color;

        public PaletteButton(int posX, int posY, int sideLength, DyeColor color)
        {
            super(posX, posY, sideLength, sideLength, "", pressed -> {});
            this.color = color;
        }

        @Override
        public void render(int mouseX, int mouseY, float partialTicks)
        {
            this.active = WindowBannerPicker.this.selectedColor != this.color;

            super.render(mouseX, mouseY, partialTicks);
        }

        @Override
        public void renderButton(int mouseX, int mouseY, float partialTicks)
        {
            super.renderButton(mouseX, mouseY, partialTicks);
            fill(this.x, this.y, this.x+this.width, this.y+this.height, this.color.getColorValue() + (128 << 24));
            fill(this.x+2, this.y+2, this.x+this.width-2, this.y+this.height-2, this.color.getColorValue() + (255 << 24));

            if (selectedColor == this.color)
                fill(this.x+4, this.y+4, this.x+this.width-4, this.y+this.height-4, 0x55DDDDDD);
        }

        @Override
        public void onPress ()
        {
            WindowBannerPicker.this.selectedColor = this.color;
            if (activeLayer == 0)
                layers.set(0, new Pair<>(BannerPattern.BASE, this.color));

            if (layers.size() > activeLayer)
                layers.set(activeLayer, new Pair<>(layers.get(activeLayer).getFirst(), this.color));
        }
    }

    public class LayerButton extends Button
    {
        private final int layer;

        public LayerButton(int x, int y, int width, int height, int layer)
        {
            super(
                    x - (layer == 0 ? width*2 : 0), y,
                    width * (layer == 0 ? 3 : 1), height,
                    layer == 0? "Base" : String.valueOf(layer),
                    pressed -> {}
            );
            this.layer = layer;
        }

        @Override
        public void onPress()
        {
            activeLayer = this.layer;

            if (this.layer >= layers.size())
                selectedColor = layers.get(0).getSecond().equals(DyeColor.BLACK) ? DyeColor.WHITE : DyeColor.BLACK;

            else
                selectedColor = layers.get(activeLayer).getSecond();
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

    public class PatternButton extends Button
    {
        private final BannerPattern pattern;

        public PatternButton(int x, int y, int height, BannerPattern pattern)
        {
            super(x, y, height/2, height, "", pressed -> {});
            this.pattern = pattern;
        }

        @Override
        public void onPress()
        {
            if (activeLayer == layers.size())
                layers.add(new Pair<>(this.pattern, selectedColor));

            else if (activeLayer != 0)
                layers.set(activeLayer, new Pair<>(this.pattern, selectedColor));
        }

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