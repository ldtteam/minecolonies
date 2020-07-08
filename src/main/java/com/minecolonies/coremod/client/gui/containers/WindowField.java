package com.minecolonies.coremod.client.gui.containers;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.inventory.container.ContainerField;
import com.minecolonies.api.tileentities.AbstractScarecrowTileEntity;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;

/**
 * Class which creates the GUI of our field inventory.
 */
@OnlyIn(Dist.CLIENT)
public class WindowField extends ContainerScreen<ContainerField>
{
    /**
     * The resource location of the GUI background.
     */
    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/scarecrow.png");

    /**
     * The X-OFFSET of the string in the GUI.
     */
    private static final int X_OFFSET = 8;

    /**
     * Y-OFFSET of the string in the GUI.
     */
    private static final int Y_OFFSET = 6;

    /**
     * The text color of the string in the GUI.
     */
    private static final int TEXT_COLOR = 0x404040;

    /**
     * Tile entity of the scarecrow.
     */
    private final AbstractScarecrowTileEntity tileEntity;

    /**
     * Create the field GUI.
     *
     * @param container       the container.
     * @param playerInventory the player inv.
     * @param iTextComponent  the display text component.
     */
    public WindowField(final ContainerField container, final PlayerInventory playerInventory, final ITextComponent iTextComponent)
    {
        super(container, playerInventory, iTextComponent);
        this.tileEntity = container.getTileEntity();


    }

    @Override
    protected void init () {
        super.init();

        final int offset = 24;
        final int centerX = this.guiLeft + this.xSize / 2 + 1;
        final int centerY = this.guiTop  + this.ySize / 2;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int xFromPolar = (int) Math.sin(Math.PI*(4-dir.getHorizontalIndex())/2) * (offset);
            int yFromPolar = (int) Math.cos(Math.PI*(4-dir.getHorizontalIndex())/2) * (offset);

            DirectionalButton db = new DirectionalButton(
                    centerX + xFromPolar - 12,
                    centerY - 40 + yFromPolar - 12,
                    24,
                    24,
                    String.valueOf(tileEntity.getRadiusInDirection(dir)),
                    new DirectionalButtonPress(),
                    dir
            );
            this.addButton(db);
        }
    }

    /**
     * Method called to draw the foreground of the GUI.
     *
     * @param layer1 the first layer.
     * @param layer2 the second layer.
     */
    @Override
    protected void drawGuiContainerForegroundLayer(final int layer1, final int layer2)
    {
        this.font.drawString(tileEntity.getDesc(), X_OFFSET, -Y_OFFSET*2, 16777215 /* WHITE */);
        this.font.drawString(
                LanguageHandler.format("block.minecolonies.blockhutfield"),
                X_OFFSET, Y_OFFSET,
                TEXT_COLOR

        );
    }

    /**
     * Does draw the background of the GUI.
     *
     * @param partialTicks the ticks delivered.
     * @param mouseX       the mouseX position.
     * @param mouseY       the mouseY position.
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        minecraft.getTextureManager().bindTexture(TEXTURE);
        final int marginHorizontal = (width - xSize) / 2;
        final int marginVertical = (height - ySize) / 2;
        blit(marginHorizontal, marginVertical, 0, 0, xSize, ySize);
    }

    @Override
    public void render(int x, int y, float z)
    {
        this.renderBackground();
        super.render(x, y, z);
        this.renderHoveredToolTip(x, y);
    }

    protected class DirectionalButton extends Button
    {
        public Direction direction;
        public DirectionalButtonPress onPressDirectional;
        public int textureX = 176;
        public int textureY = 0;
        public int columns = 2;

        public DirectionalButton(int widthIn, int heightIn, int width, int height, String text, IPressable onPress, Direction direction)
        {
            super(widthIn, heightIn, width, height, text, onPress);
            this.direction = direction;
            this.onPressDirectional = new DirectionalButtonPress();
            LogManager.getLogger().info(this.getTextureYOffset());

        }

        @Override
        public void onPress () { this.onPressDirectional.onPress(this); }

        public int getTextureXOffset()
        {
            return this.textureX + 24*Math.floorDiv(this.direction.getHorizontalIndex(), this.columns);
        }

        public int getTextureYOffset()
        {
            return this.textureY + 72*(this.direction.getHorizontalIndex() % this.columns);
        }

        public int getTextOffset (Direction.Axis axis)
        {
            switch (this.direction)
            {
                case NORTH: return axis == Direction.Axis.X? 0 : +2;
                case EAST:  return axis == Direction.Axis.X? -2 : 0;
                case SOUTH: return axis == Direction.Axis.X? 0 : -2;
                case WEST:  return axis == Direction.Axis.X? +2 : 0;
            }
            return 0;
        }

        @Override
        public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_)
        {
            Minecraft minecraft = Minecraft.getInstance();
            FontRenderer fontrenderer = minecraft.fontRenderer;
            minecraft.getTextureManager().bindTexture(WindowField.TEXTURE);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
            int i = this.getYImage(this.isHovered());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.blit(this.x, this.y, getTextureXOffset(), getTextureYOffset() + i * 24, this.width, this.height);
            this.renderBg(minecraft, p_renderButton_1_, p_renderButton_2_);
            int j = getFGColor();
            this.drawCenteredString(
                    fontrenderer, this.getMessage(),
                    this.x + this.width / 2 + getTextOffset(Direction.Axis.X),
                    this.y + (this.height - 8) / 2 + getTextOffset(Direction.Axis.Y),
                    j | MathHelper.ceil(this.alpha * 255.0F) << 24
            );
        }
    }

    protected class DirectionalButtonPress implements Button.IPressable
    {
        @Override
        public void onPress(Button pressed) {}

        public void onPress(DirectionalButton pressed)
        {
            pressed.setMessage(String.valueOf(tileEntity.setRadiusInDirection(pressed.direction)));
        }
    }
}
