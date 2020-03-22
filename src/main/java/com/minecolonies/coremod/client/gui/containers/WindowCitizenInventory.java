package com.minecolonies.coremod.client.gui.containers;

import com.minecolonies.api.inventory.container.ContainerCitizenInventory;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * ------------ Class not Documented ------------
 */
public class WindowCitizenInventory extends ContainerScreen<ContainerCitizenInventory>
{
    /**
     * The ResourceLocation containing the chest GUI texture.
     */
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    /**
     * window height is calculated with these values; the more rows, the heigher
     */
    private final int        inventoryRows;

    public WindowCitizenInventory(final ContainerCitizenInventory container, final PlayerInventory playerInventory, final ITextComponent iTextComponent)
    {
        super(container, playerInventory, iTextComponent);
        this.inventoryRows = container.getInventory().size()/ 9;
        this.ySize = 114 + this.inventoryRows * 18;
    }

    public void render(int x, int y, float z)
    {
        this.renderBackground();
        super.render(x, y, z);
        this.renderHoveredToolTip(x, y);
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY)
    {
        this.font.drawString(this.container.getDisplayName(), 8, 6, 4210752);
        this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8, this.ySize - 168 + 2, 4210752);
    }

     /**
     * Draws the background layer of this container (behind the items).
     */
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(i, j, 0, 0, this.xSize, 3 * 18 + 17);
        this.blit(i, j + 3 * 18 + 17, 0, 126, this.xSize, 96);
    }
}