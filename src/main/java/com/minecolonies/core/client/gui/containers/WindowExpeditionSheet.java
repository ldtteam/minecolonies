package com.minecolonies.core.client.gui.containers;

import com.minecolonies.api.inventory.container.ContainerExpeditionSheet;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.InventoryConstants.*;

@OnlyIn(Dist.CLIENT)
public class WindowExpeditionSheet extends AbstractContainerScreen<ContainerExpeditionSheet>
{
    /**
     * The resource LOCATION of the texture.
     */
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/generic_108.png");

    /**
     * Size of the custom texture.
     */
    private static final int TEXTURE_SIZE = 350;

    /**
     * General y offset.
     */
    private static final int Y_OFFSET = 114;

    /**
     * Offset of the screen for the texture.
     */
    private static final int TEXTURE_HEIGHT = 96;

    /**
     * Offset inside the texture to use.
     */
    private static final int TEXTURE_OFFSET = 126 * 2 - 17;

    /**
     * Used to calculate the window height.
     */
    private final int inventoryRows;

    public WindowExpeditionSheet(final ContainerExpeditionSheet container, final Inventory playerInventory, final Component component)
    {
        super(container, playerInventory, component);
        this.inventoryRows = (container.getItems().size() - 36) / 9;

        this.imageHeight = Y_OFFSET + Math.min(INVENTORY_COLUMNS, this.inventoryRows) * INVENTORY_OFFSET_EACH;
        this.imageWidth = 245;
    }

    @Override
    public void render(@NotNull final GuiGraphics stack, int x, int y, float z)
    {
        this.renderBackground(stack);
        super.render(stack, x, y, z);
        this.renderTooltip(stack, x, y);
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void renderLabels(@NotNull final GuiGraphics stack, int mouseX, int mouseY)
    {
        stack.drawString(this.font, this.title.getString(), 8, 6, 4210752, false);
        stack.drawString(this.font, this.playerInventoryTitle.getString(), 8, (this.imageHeight - 94), 4210752, false);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    @Override
    protected void renderBg(@NotNull final GuiGraphics stack, final float partialTicks, final int mouseX, final int mouseY)
    {
        final int rowsHeight = this.inventoryRows * PLAYER_INVENTORY_OFFSET_EACH + PLAYER_INVENTORY_OFFSET_EACH - 1;
        stack.blit(CHEST_GUI_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, rowsHeight, TEXTURE_SIZE, TEXTURE_SIZE);
        stack.blit(CHEST_GUI_TEXTURE, this.leftPos, this.topPos + rowsHeight, 0,
          TEXTURE_OFFSET, this.imageWidth, TEXTURE_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
    }
}
