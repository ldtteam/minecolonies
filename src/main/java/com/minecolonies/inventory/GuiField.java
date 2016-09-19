package com.minecolonies.inventory;

import com.minecolonies.entity.ai.citizen.farmer.Field;
import com.minecolonies.lib.Constants;
import com.minecolonies.tileentities.ScarecrowTileEntity;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Class which creates the GUI of our field inventory.
 */
@SideOnly(Side.CLIENT)
public class GuiField extends GuiContainer
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
    private final ScarecrowTileEntity tileEntity;

    /**
     * Constructor of the GUI.
     *
     * @param parInventoryPlayer the player inventory.
     * @param tileEntity         the tileEntity of the field, contains the inventory.
     * @param world              the world the field is in.
     * @param location           the location the field is at.
     */
    protected GuiField(InventoryPlayer parInventoryPlayer, ScarecrowTileEntity tileEntity, World world, BlockPos location)
    {
        super(new Field(tileEntity, parInventoryPlayer, world, location));
        this.tileEntity = tileEntity;
    }

    /**
     * Method called to draw the foreground of the GUI.
     *
     * @param layer1 the first layer.
     * @param layer2 the second layer.
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int layer1, int layer2)
    {
        this.fontRendererObj.drawString(tileEntity.getDesc(), X_OFFSET, Y_OFFSET, TEXT_COLOR);
    }

    /**
     * Does draw the background of the GUI.
     *
     * @param partialTicks the ticks delivered.
     * @param mouseX       the mouseX position.
     * @param mouseY       the mouseY position.
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        final int marginHorizontal = (width - xSize) / 2;
        final int marginVertical = (height - ySize) / 2;
        drawTexturedModalRect(marginHorizontal, marginVertical, 0, 0, xSize, ySize);
    }
}
