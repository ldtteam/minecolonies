package com.minecolonies.coremod.client.gui.containers;

import com.minecolonies.api.blocks.AbstractBlockMinecoloniesRack;
import com.minecolonies.api.blocks.types.RackType;
import com.minecolonies.api.inventory.container.ContainerRack;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

@OnlyIn(Dist.CLIENT)
public class WindowRack extends AbstractContainerScreen<ContainerRack>
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
     * Size of the custom texture.
     */
    private static final int TEXTURE_SIZE = 350;

    /**
     * Size at which the normal GUI texture still works.
     */
    private static final int GOOD_SIZE = 8;

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
     * The upper chest inventory.
     */
    private final IItemHandler jointChestInventory;

    /**
     * Used to calculate the window height.
     */
    private final int inventoryRows;

    public WindowRack(final ContainerRack container, final Inventory playerInventory, final Component iTextComponent)
    {
        super(container, playerInventory, iTextComponent);
        if (container.neighborRack != null)
        {
            if (container.rack.getBlockState().getValue(AbstractBlockMinecoloniesRack.VARIANT) != RackType.EMPTYAIR)
            {
                this.jointChestInventory = new CombinedInvWrapper(container.rack.getInventory(), container.neighborRack.getInventory());
            }
            else
            {
                this.jointChestInventory = new CombinedInvWrapper(container.neighborRack.getInventory(), container.rack.getInventory());
            }
        }
        else
        {
            this.jointChestInventory = container.rack.getInventory();
        }

        final int size = jointChestInventory.getSlots();
        this.inventoryRows = size / INVENTORY_COLUMNS;
        final int rows = Math.min(this.inventoryRows, INVENTORY_BAR_SIZE);
        final int columns = this.inventoryRows <= INVENTORY_BAR_SIZE ? INVENTORY_COLUMNS : ((size / INVENTORY_BAR_SIZE) + 1);

        this.imageHeight = Y_OFFSET + rows * PLAYER_INVENTORY_OFFSET_EACH;
        if (columns > INVENTORY_COLUMNS)
        {
            this.imageWidth += (columns - INVENTORY_COLUMNS) * PLAYER_INVENTORY_OFFSET_EACH;
        }
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void renderLabels(@NotNull final GuiGraphics stack, int mouseX, int mouseY)
    {
        stack.drawString(this.font, this.title.getString(), 8, 6, 4210752);
        stack.drawString(this.font, this.playerInventoryTitle.getString(), 8, (this.imageHeight - 94), 4210752);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    @Override
    protected void renderBg(@NotNull final GuiGraphics stack, final float partialTicks, final int mouseX, final int mouseY)
    {
        final ResourceLocation loc = getCorrectTextureForSlots(inventoryRows);

        if (inventoryRows <= GOOD_SIZE)
        {
            final int rowsHeight = this.inventoryRows * PLAYER_INVENTORY_OFFSET_EACH + PLAYER_INVENTORY_OFFSET_EACH - 1;
            stack.blit(loc, this.leftPos, this.topPos, 0, 0, this.imageWidth, rowsHeight, TEXTURE_SIZE, TEXTURE_SIZE);
            stack.blit(loc, this.leftPos, this.topPos + rowsHeight, 0,
              TEXTURE_OFFSET, this.imageWidth, TEXTURE_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        else
        {
            stack.blit(loc, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, TEXTURE_SIZE, TEXTURE_SIZE);
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
            return new ResourceLocation(Constants.MOD_ID, String.format(LOCATION, inventoryRows * INVENTORY_COLUMNS));
        }
    }

    @Override
    public void render(@NotNull final GuiGraphics stack, int x, int y, float z)
    {
        this.renderBackground(stack);
        super.render(stack, x, y, z);
        this.renderTooltip(stack, x, y);
    }
}
