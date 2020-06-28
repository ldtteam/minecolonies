package com.minecolonies.coremod.client.gui.containers;

import com.minecolonies.api.inventory.container.ContainerField;
import com.minecolonies.api.tileentities.AbstractScarescrowTileEntity;
import com.minecolonies.api.util.constant.Constants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
    private final AbstractScarescrowTileEntity tileEntity;

    /**
     * Create the field GUI.
     * @param container the container.
     * @param playerInventory the player inv.
     * @param iTextComponent the display text component.
     */
    public WindowField(final ContainerField container, final PlayerInventory playerInventory, final ITextComponent iTextComponent)
    {
        super(container, playerInventory, iTextComponent);
        this.tileEntity = container.getTileEntity();
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
        this.font.drawString(tileEntity.getDesc(), X_OFFSET, Y_OFFSET, TEXT_COLOR);
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
}
