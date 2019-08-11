package com.minecolonies.coremod.inventory.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.inventory.container.ContainerRack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

@OnlyIn(Dist.CLIENT)
public class WindowRack extends ContainerScreen<ContainerRack>
{
    /**
     * The resource LOCATION of the texture.
     */
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/generic_108.png");

    /**
     * The LOCATION of the additional styles.
     */
    private static final String LOCATION = "textures/gui/gui%s.png";

    /**
     * Amount of slots each row.
     */
    private static final int SLOTS_EACH_ROW = 9;

    /**
     * Size of the custom texture.
     */
    private static final int TEXTURE_SIZE = 350;

    /**
     * Offset of each slot.
     */
    private static final int SLOT_OFFSET = 18;

    /**
     * Size at which the normal GUI texture still works.
     */
    private static final int GOOD_SIZE = 8;

    /**
     * Multiply the current size by this amount.
     */
    private static final int SIZE_MULTIPLIER = 3;

    /**
     * General y offset.
     */
    private static final int Y_OFFSET = 114;

    /**
     * Offet of the screen for the texture.
     */
    private static final int TEXTURE_HEIGHT = 96;

    /**
     * Offset inside the texture to use.
     */
    private static final int TEXTURE_OFFSET = 126 * 2 - 17;

    /**
     * Extra offset to move increase the texture if the inventory is huge.
     */
    private static final int EXTRA_OFFSET = 56;

    /**
     * Extra height to show the whole texture for big inventories.
     */
    private static final int EXTRA_HEIGHT = 50;

    /**
     * The upper chest inventory.
     */
    private final IItemHandler jointChestInventory;

    /**
     * Used to calculate the window height.
     */
    private final int inventoryRows;

    public WindowRack(final ContainerRack container, final PlayerInventory playerInventory, final ITextComponent iTextComponent)
    {
        super(container, playerInventory, iTextComponent);
        if (neighborRack != null)
        {
            if (tileEntity.isMain())
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
        this.ySize = Y_OFFSET + Math.min(SLOTS_EACH_ROW, this.inventoryRows) * SLOT_OFFSET;
        if (this.inventoryRows > SLOTS_EACH_ROW - 1)
        {
            this.xSize = this.xSize + (this.inventoryRows - SLOTS_EACH_ROW) * (SLOTS_EACH_ROW + 1);
        }
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY)
    {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(getCorrectTextureForSlots(inventoryRows));
        final int i = (this.width - this.xSize) / 2;
        final int j = (this.height - this.ySize) / 2;

        if (inventoryRows < SLOTS_EACH_ROW)
        {
            blit(i, j, 0, 0, this.xSize, this.inventoryRows * SLOT_OFFSET + SLOT_OFFSET - 1, TEXTURE_SIZE, TEXTURE_SIZE);
            blit(i, j + this.inventoryRows * SLOT_OFFSET + SLOT_OFFSET - 1, 0,
              TEXTURE_OFFSET, this.xSize, TEXTURE_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        else
        {
            final int textureOffset = TEXTURE_OFFSET - EXTRA_OFFSET;
            blit(i, j, 0, 0, (this.xSize * SIZE_MULTIPLIER) / 2, this.inventoryRows * SLOT_OFFSET + SLOT_OFFSET - 1, TEXTURE_SIZE, TEXTURE_SIZE);
            blit(i,
              j + Math.min(SLOTS_EACH_ROW, this.inventoryRows) * SLOT_OFFSET + SLOT_OFFSET - 1, 0,
              textureOffset, (this.xSize * SIZE_MULTIPLIER) / 2, TEXTURE_HEIGHT + EXTRA_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }

    /**
     * Get the correct resource LOCATION for this amount of rows.
     *
     * @param inventoryRows the amount of rows.
     * @return the correct LOCATION.
     */
    private static ResourceLocation getCorrectTextureForSlots(final int inventoryRows)
    {
        if (inventoryRows <= GOOD_SIZE)
        {
            return CHEST_GUI_TEXTURE;
        }
        else
        {
            return new ResourceLocation(Constants.MOD_ID, String.format(LOCATION, Integer.toString(inventoryRows * SLOTS_EACH_ROW)));
        }
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks)
    {
        super.draw(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
