package com.minecolonies.coremod.client.gui.containers;

import com.google.common.collect.Lists;
import com.minecolonies.api.inventory.container.ContainerField;
import com.minecolonies.api.tileentities.AbstractScarecrowTileEntity;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.FieldPlotResizeMessage;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Class which creates the GUI of our field inventory.
 */
@OnlyIn(Dist.CLIENT)
public class WindowField extends AbstractContainerScreen<ContainerField>
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
     * The width and height of the DirectionalButtons (they're square)
     */
    private static final int BUTTON_SIDE_LENGTH = 24;

    /**
     * Tile entity of the scarecrow.
     */
    private final AbstractScarecrowTileEntity tileEntity;

    /**
     * The values to render on each directional button, indicating the size of the field
     * S, W, N, E.
     */
    private final int[] radii = new int[4];

    /**
     * Create the field GUI.
     *
     * @param container       the container.
     * @param playerInventory the player inv.
     * @param iTextComponent  the display text component.
     */
    public WindowField(final ContainerField container, final Inventory playerInventory, final Component iTextComponent)
    {
        super(container, playerInventory, iTextComponent);
        this.tileEntity = container.getTileEntity();
    }

    @Override
    protected void init()
    {
        super.init();

        final int centerX = this.leftPos + this.imageWidth / 2 + 1;
        final int centerY = this.topPos + this.imageHeight / 2;
        for (Direction dir : Direction.Plane.HORIZONTAL)
        {
            int xFromPolar = (int) Math.sin(Math.PI * (4 - dir.get2DDataValue()) / 2) * (BUTTON_SIDE_LENGTH);
            int yFromPolar = (int) Math.cos(Math.PI * (4 - dir.get2DDataValue()) / 2) * (BUTTON_SIDE_LENGTH);
            this.radii[dir.get2DDataValue()] = tileEntity.getRadius(dir);

            // Some magic numbering to get the offsets right
            DirectionalButton db = new DirectionalButton(
              centerX + xFromPolar - 12,
              centerY - 40 + yFromPolar - 12,
              BUTTON_SIDE_LENGTH,
              BUTTON_SIDE_LENGTH,
              Component.literal(String.valueOf(this.radii[dir.get2DDataValue()])),
              dir
            );
            this.addRenderableWidget(db);
        }
    }

    @Override
    protected void renderLabels(@NotNull final GuiGraphics stack, final int mouseX, final int mouseY)
    {
        if (!tileEntity.getOwner().isEmpty())
        {
            stack.drawString(this.font, Component.translatable(WORKER_FIELD, tileEntity.getOwner()), X_OFFSET, -Y_OFFSET * 2, 16777215 /* WHITE */);
        }

        stack.drawString(this.font, Component.translatable(BLOCK_HUT_FIELD), X_OFFSET, Y_OFFSET, TEXT_COLOR);

        for (Renderable widget : this.renderables)
        {
            if (widget instanceof DirectionalButton)
            {
                if (((AbstractWidget) widget).isMouseOver(mouseX, mouseY))
                {
                    // Don't render while they are dragging a stack around
                    if (!menu.getCarried().isEmpty())
                    {
                        return;
                    }
                    List<FormattedText> lines = Lists.newArrayList(
                      Component.translatable(PARTIAL_BLOCK_HUT_FIELD_DIRECTION_ABSOLUTE + ((DirectionalButton) widget).direction.getSerializedName()),
                      Component.translatable(((DirectionalButton) widget).getDirectionalTranslationKey())
                        .setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY))
                    );

                    stack.renderTooltip(this.font, Language.getInstance().getVisualOrder(lines), mouseX-100, mouseY-20);
                    break;
                }
            }
        }
    }

    /**
     * Does draw the background of the GUI.
     *
     * @param partialTicks the ticks delivered.
     * @param mouseX       the mouseX position.
     * @param mouseY       the mouseY position.
     */
    @Override
    protected void renderBg(@NotNull final GuiGraphics stack, final float partialTicks, final int mouseX, final int mouseY)
    {
        final int marginHorizontal = (width - imageWidth) / 2;
        final int marginVertical = (height - imageHeight) / 2;
        stack.blit(TEXTURE, marginHorizontal, marginVertical, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(@NotNull final GuiGraphics stack, int x, int y, float z)
    {
        this.renderBackground(stack);
        super.render(stack, x, y, z);
        this.renderTooltip(stack, x, y);
    }

    /**
     * Buttons with a direction. Textures are assigned based on direction specified.
     */
    protected class DirectionalButton extends Button
    {
        public Direction direction;
        public int       textureX = 176;
        public int       textureY = 0;

        /**
         * The arrangement of the button's direction textures in the image.
         */
        public int columns = 2;

        /**
         * Construct a directional button. Arguments based on a basic Button widget
         *
         * @param x         the x position from screen top left
         * @param y         the y position from screen top left
         * @param width     the width of the button in the gui
         * @param height    the height of the button in the gui
         * @param text      the label on the button
         * @param direction the direction this button faces. Adjusts texture coordinates.
         */
        public DirectionalButton(int x, int y, int width, int height, Component text, Direction direction)
        {
            super(x, y, width, height, text, button -> {}, DEFAULT_NARRATION);
            this.direction = direction;
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button)
        {
            if (this.clicked(mouseX, mouseY))
            {
                int index = this.direction.get2DDataValue();
                int delta = this.isValidClickButton(button) ? 1 : -1;

                // Perform the cycle
                radii[index] = (radii[index] + delta) % (ScarecrowTileEntity.getMaxRange() + 1);
                if (radii[index] < 0) radii[index] = ScarecrowTileEntity.getMaxRange();

                this.setMessage(Component.literal(String.valueOf(radii[index])));
                Network.getNetwork().sendToServer(new FieldPlotResizeMessage(radii[index], this.direction, tileEntity.getBlockPos()));

                return true;
            }

            return false;
        }

        /**
         * Retrieves the texture offset depending on the direction of the button
         *
         * @return the X offset for the image texture
         */
        public int getTextureXOffset()
        {
            return this.textureX + 24 * Math.floorDiv(this.direction.get2DDataValue(), this.columns);
        }

        /**
         * Retrieves the texture offset depending on the direction of the button
         *
         * @return the Y offset for the image texture
         */
        public int getTextureYOffset()
        {
            return this.textureY + 72 * (this.direction.get2DDataValue() % this.columns);
        }

        /**
         * Neatens the buttons by offsetting the text towards the main area of the texture
         *
         * @param axis the Axis of the direction that the button represents
         * @return the render offset
         */
        public int getTextOffset(Direction.Axis axis)
        {
            switch (this.direction)
            {
                case NORTH:
                    return axis == Direction.Axis.X ? 0 : +2;
                case EAST:
                    return axis == Direction.Axis.X ? -2 : 0;
                case SOUTH:
                    return axis == Direction.Axis.X ? 0 : -2;
                case WEST:
                    return axis == Direction.Axis.X ? +2 : 0;
            }
            return 0;
        }

        @Override
        public void renderWidget(@NotNull final GuiGraphics stack, int mouseX, int mouseY, float partialTicks)
        {
            Minecraft minecraft = Minecraft.getInstance();
            Font fontrenderer = minecraft.font;

            int i = this.getYImage(this.isHovered);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            stack.blit(TEXTURE, this.getX(), this.getY(), getTextureXOffset(), getTextureYOffset() + i * 24, this.width, this.height);
            int j = getFGColor();
            stack.drawCenteredString(minecraft.font, this.getMessage(),
              this.getX() + this.width / 2 + getTextOffset(Direction.Axis.X),
              this.getY() + (this.height - 8) / 2 + getTextOffset(Direction.Axis.Y),
              j | Mth.ceil(this.alpha * 255.0F) << 24
            );
        }

        /**
         * Copy older from old system.
         * @param isHovered if hovered.
         * @return get the y img.
         */
        protected int getYImage(boolean isHovered)
        {
            int i = 1;
            if (!this.active)
            {
                i = 0;
            }
            else if (isHovered)
            {
                i = 2;
            }

            return i;
        }

        /**
         * Calculates where the player is and the appropriate relative direction
         *
         * @return the translation key
         */
        public String getDirectionalTranslationKey()
        {
            Direction[] looks = Direction.orderedByNearest(Minecraft.getInstance().player);
            Direction facing = looks[0].getAxis() == Direction.Axis.Y ? looks[1] : looks[0];

            switch (facing.getOpposite().get2DDataValue() - this.direction.get2DDataValue())
            {
                case 1:
                case -3:
                    return BLOCK_HUT_FIELD_DIRECTION_RELATIVE_TO_RIGHT;
                case 2:
                case -2:
                    return BLOCK_HUT_FIELD_DIRECTION_RELATIVE_OPPOSITE;
                case 3:
                case -1:
                    return BLOCK_HUT_FIELD_DIRECTION_RELATIVE_TO_LEFT;
                default:
                    return BLOCK_HUT_FIELD_DIRECTION_RELATIVE_NEAREST;
            }
        }
    }
}
