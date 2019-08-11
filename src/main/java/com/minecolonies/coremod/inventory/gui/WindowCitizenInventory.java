package com.minecolonies.coremod.inventory.gui;

import com.minecolonies.coremod.inventory.container.ContainerCitizenInventory;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

/**
 * ------------ Class not Documented ------------
 */
public class WindowCitizenInventory extends ContainerScreen
{
    /**
     * The ResourceLocation containing the chest GUI texture.
     */
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private final IInventory upperChestInventory;
    private final IInventory lowerChestInventory;
    /**
     * window height is calculated with these values; the more rows, the heigher
     */
    private final int        inventoryRows;

    public WindowCitizenInventory(final ContainerCitizenInventory containerMinecoloniesBuildingInventory)
    {
        super(containerMinecoloniesBuildingInventory);
        this.upperChestInventory = containerMinecoloniesBuildingInventory.getPlayerInventory();
        this.lowerChestInventory = containerMinecoloniesBuildingInventory.getLowerChestInventory();
        this.allowUserInput = false;
        this.inventoryRows = containerMinecoloniesBuildingInventory.getLowerChestInventory().getSizeInventory() / 9;
        this.ySize = 114 + this.inventoryRows * 18;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY)
    {
        this.font.drawString(this.lowerChestInventory.getDisplayName().getUnformattedText(), 8, 6, 4210752);
        this.font.drawString(this.upperChestInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        final int i = (this.width - this.xSize) / 2;
        final int j = (this.height - this.ySize) / 2;
        this.blit(i, j, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
        this.blit(i, j + this.inventoryRows * 18 + 17, 0, 126, this.xSize, 96);
    }
}