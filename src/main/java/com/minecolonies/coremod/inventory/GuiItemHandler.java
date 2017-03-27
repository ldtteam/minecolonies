package com.minecolonies.coremod.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

/**
 * Gui for a ItemHandler.
 */
@SideOnly(Side.CLIENT)
public class GuiItemHandler extends GuiContainer
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private final IInteractiveItemHandler handler;
    private final int                     inventoryRows;

    private static final int CONSTANT_HANDLERNAMELEFTOFFSET      = 8;
    private static final int CONSTANT_HANDLERNAMETOPOFFSET       = 6;
    private static final int CONSTANT_HANDLERNAMEFOREGROUNDCOLOR = 4_210_752;

    private static final int CONSTANT_PLAYERCOLUMNCOUNT = 9;

    private static final int CONSTANT_INVENTORYOFFSET       = 114;
    private static final int CONSTANT_INVENTORYSLOTSYOFFSET = 126;
    private static final int CONSTANT_INVENTORYHEIGHT       = 96;
    private static final int CONSTANT_SLOTSIZE              = 18;

    private static final int CONSTANT_SLOTXOFFSET = 17;

    /**
     * Constructor to create a GUI for a ItemHandler.
     *
     * @param handler The handler to create a gui for.
     */
    public GuiItemHandler(IInteractiveItemHandler handler)
    {
        super(new ContainerItemHandler(Minecraft.getMinecraft().thePlayer.inventory, handler));
        this.handler = handler;
        this.allowUserInput = false;
        this.inventoryRows = handler.getSlots() / CONSTANT_PLAYERCOLUMNCOUNT;
        this.ySize = CONSTANT_INVENTORYOFFSET + this.inventoryRows * CONSTANT_SLOTSIZE;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items).
     * @param mouseX The x-Coord of the mouse.
     * @param mouseY The y-Coord of the mouse.
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(new TextComponentTranslation(this.handler.getName()).getUnformattedText(),
          CONSTANT_HANDLERNAMELEFTOFFSET,
          CONSTANT_HANDLERNAMETOPOFFSET,
          CONSTANT_HANDLERNAMEFOREGROUNDCOLOR);
    }

    /**
     * Draws the background layer of this container (behind the items).
     * @param partialTicks The relative progression of time between two ticks.
     * @param mouseX The x-Coord of the mouse.
     * @param mouseY The y-Coord of the mouse.
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), Color.WHITE.getAlpha());
        this.mc.getTextureManager().bindTexture(TEXTURE);
        final int i = (this.width - this.xSize) / 2;
        final int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.inventoryRows * CONSTANT_SLOTSIZE + CONSTANT_SLOTXOFFSET);
        this.drawTexturedModalRect(i, j + this.inventoryRows * CONSTANT_SLOTSIZE + CONSTANT_SLOTXOFFSET, 0, CONSTANT_INVENTORYSLOTSYOFFSET, this.xSize, CONSTANT_INVENTORYHEIGHT);
    }
}