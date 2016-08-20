package com.minecolonies.inventory;


import com.minecolonies.colony.Field;
import com.minecolonies.lib.Constants;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
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
     * Constructor of the GUI.
     * @param parInventoryPlayer the player inventory.
     * @param fieldInventory the field inventory.
     */
    protected GuiField(InventoryPlayer parInventoryPlayer, IInventory fieldInventory)
    {
        super(new Field((InventoryField) fieldInventory, parInventoryPlayer));
    }

    /**
     * Does draw the background of the GUI.
     * @param partialTicks the ticks delivered.
     * @param mouseX the mouseX position.
     * @param mouseY the mouseY position.
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        int marginHorizontal = (width - xSize) / 2;
        int marginVertical   = (height - ySize) / 2;
        drawTexturedModalRect(marginHorizontal, marginVertical, 0, 0, xSize, ySize);
    }
}
