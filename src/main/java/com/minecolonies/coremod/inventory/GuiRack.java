package com.minecolonies.coremod.inventory;

import com.minecolonies.coremod.tileentities.TileEntityRack;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

@SideOnly(Side.CLIENT)
public class GuiRack extends GuiContainer
{
    /**
     * The resource location of the texture.
     */
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    /**
     * The upper chest inventory.
     */
    private final IItemHandler upperChestInventory;

    /**
     * The lower chest inventory.
     */
    private final IItemHandler   lowerChestInventory;

    /**
     * Used to calculate the window height.
     */
    private int inventoryRows;

    public GuiRack(final InventoryPlayer parInventoryPlayer, final TileEntityRack tileEntity, final TileEntityRack neighborRack, final World world, final BlockPos location)
    {
        super(new ContainerRack(tileEntity, neighborRack, parInventoryPlayer, world, location));
        this.upperChestInventory = tileEntity.getInventory();
        this.inventoryRows = upperChestInventory.getSlots() / 9;
        if(neighborRack != null)
        {
            this.lowerChestInventory = neighborRack.getInventory();
            this.inventoryRows += lowerChestInventory.getSlots() / 9;
        }
        else
        {
            this.lowerChestInventory = null;
        }
        this.allowUserInput = false;
        this.ySize = 114 + this.inventoryRows * 18;
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
        this.drawTexturedModalRect(i, j + this.inventoryRows * 18 + 17, 0, 126, this.xSize, 96);
    }
}
