package com.minecolonies.coremod.client.gui.containers;

import com.minecolonies.api.inventory.container.ContainerBuildingInventory;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class WindowBuildingInventory extends AbstractContainerScreen<ContainerBuildingInventory>
{
    /**
     * Texture res loc.
     */
    private static final ResourceLocation TEXT = new ResourceLocation(Constants.MOD_ID, "textures/gui/generic_108.png");

    /**
     * Offset inside the texture to use.
     */
    private static final int TEXTURE_OFFSET = 126 * 2 - 17;

    /**
     * Offset of each slot.
     */
    private static final int SLOT_OFFSET = 18;

    /**
     * Size of the custom texture.
     */
    private static final int TEXTURE_SIZE = 350;

    /**
     * Offet of the screen for the texture.
     */
    private static final int TEXTURE_HEIGHT = 96;

    /**
     * In rows total.
     */
    private final int inventoryRows;

    public WindowBuildingInventory(final ContainerBuildingInventory container, final Inventory playerInventory, final Component component)
    {
        super(container, playerInventory, component);
        this.inventoryRows = container.getSize();
        this.imageHeight = 114 + this.inventoryRows * 18;
    }

    @Override
    public void render(@NotNull GuiGraphics matrixStack, int x, int y, float z)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, x, y, z);
        this.renderTooltip(matrixStack, x, y);
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void renderLabels(@NotNull final GuiGraphics stack, int mouseX, int mouseY)
    {
        stack.drawString(this.font, this.title.getString(), 8, 6, 4210752);
        stack.drawString(this.font, this.playerInventoryTitle.getString(), 8, (this.imageHeight - 96 + 2), 4210752);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    @Override
    protected void renderBg(@NotNull GuiGraphics stack, float partialTicks, int mouseX, int mouseY)
    {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        stack.blit(TEXT, i, j, 0, 0, this.imageWidth, this.inventoryRows * SLOT_OFFSET + SLOT_OFFSET - 1, TEXTURE_SIZE, TEXTURE_SIZE);
        stack.blit(TEXT, i, j + this.inventoryRows * SLOT_OFFSET + SLOT_OFFSET - 1, 0,
          TEXTURE_OFFSET, this.imageWidth, TEXTURE_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
    }
}
