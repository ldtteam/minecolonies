package com.minecolonies.coremod.inventory;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.tileentities.TileEntityRack;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.lwjgl.input.Mouse;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiRack extends GuiContainer
{
    /**
     * The resource location of the texture.
     */
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/generic_108.png");

    /**
     * Amount of slots each row.
     */
    private static final int SLOTS_EACH_ROW = 9;

    /**
     * Offset of each slot.
     */
    private static final int SLOT_OFFSET    = 18;

    /**
     * General y offset.
     */
    private static final int Y_OFFSET       = 114;

    /**
     * Offet of the screen for the texture.
     */
    private static final int TEXTURE_HEIGHT = 96;

    /**
     * Offset inside the texture to use.
     */
    private static final int TEXTURE_OFFSET = 126*2-17;

    /**
     * The upper chest inventory.
     */
    private final IItemHandler jointChestInventory;

    /**
     * Used to calculate the window height.
     */
    private final int inventoryRows;

    public GuiRack(final InventoryPlayer parInventoryPlayer, final TileEntityRack tileEntity, final TileEntityRack neighborRack, final World world, final BlockPos location)
    {
        super(new ContainerRack(tileEntity, neighborRack, parInventoryPlayer, world, location));

        if(neighborRack != null)
        {
            if(tileEntity.isMain())
            {
                this.jointChestInventory = new CombinedInvWrapper(tileEntity.getInventory(), neighborRack.getInventory());
            }
            else
            {
                this.jointChestInventory = new CombinedInvWrapper(neighborRack.getInventory(), tileEntity.getInventory());
            }
        }
        else
        {
            this.jointChestInventory = tileEntity.getInventory();
        }

        this.inventoryRows = jointChestInventory.getSlots() / SLOTS_EACH_ROW;

        this.allowUserInput = false;
        this.ySize = Y_OFFSET + this.inventoryRows * SLOT_OFFSET;
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        final int i = (this.width - this.xSize) / 2;
        final int j = (this.height - this.ySize) / 2;

        this.drawModalRectWithCustomSizedTexture(i, j, 0, 0, this.xSize, this.inventoryRows * SLOT_OFFSET + SLOT_OFFSET - 1, 350, 350);
        this.drawModalRectWithCustomSizedTexture(i, j + this.inventoryRows * SLOT_OFFSET + SLOT_OFFSET - 1, 0, TEXTURE_OFFSET, this.xSize, TEXTURE_HEIGHT, 350, 350);

    }
}
