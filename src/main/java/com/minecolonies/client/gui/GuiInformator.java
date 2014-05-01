package com.minecolonies.client.gui;

import com.minecolonies.inventory.CraftingInventoryInformator;
import com.minecolonies.lib.Constants;
import com.minecolonies.tilentities.TileEntityTownHall;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiInformator extends GuiContainer
{
    private final ResourceLocation background = new ResourceLocation(Constants.MODID + ":" + "textures/gui/guiInformatorBackground.png");
    private InventoryPlayer    inventoryPlayer;
    private TileEntityTownHall tileEntityTownHall;

    public GuiInformator(InventoryPlayer inventory, TileEntityTownHall tileEntity)
    {
        super(new CraftingInventoryInformator(inventory, tileEntity)); //TODO
        xSize = 171;
        ySize = 247;
        this.inventoryPlayer = inventory;
        this.tileEntityTownHall = tileEntity;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float unknown, int xMouse, int zMouse)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(background);
        int xCoord = (width - xSize) / 2;
        int yCoord = (height - ySize - 10) / 2;
        drawTexturedModalRect(xCoord, yCoord, 0, 0, xSize, ySize);
    }
}