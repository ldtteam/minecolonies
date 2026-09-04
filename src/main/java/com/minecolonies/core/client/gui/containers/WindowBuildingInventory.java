package com.minecolonies.core.client.gui.containers;

import com.minecolonies.api.inventory.container.ContainerBuildingInventory;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class WindowBuildingInventory extends AbstractContainerScreen<ContainerBuildingInventory>
{
    /**
     * Texture res loc.
     */
    private static final Identifier TEXT = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/generic_108.png");

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
        super(container, playerInventory, component, 176, 114 + container.getSize() * 18);
        this.inventoryRows = container.getSize();
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor matrixStack, int x, int y, float z)
    {
        super.extractRenderState(matrixStack, x, y, z);
        // tooltip extraction is handled by AbstractContainerScreen;
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void extractLabels(@NotNull final GuiGraphicsExtractor stack, int mouseX, int mouseY)
    {
        stack.text(this.font, this.title.getString(), 8, 6, 0xFF404040, false);
        stack.text(this.font, this.playerInventoryTitle.getString(), 8, (this.imageHeight - 96 + 2), 0xFF404040, false);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    @Override
    public void extractBackground(@NotNull final GuiGraphicsExtractor stack, final int mouseX, final int mouseY, final float partialTicks)
    {
        super.extractBackground(stack, mouseX, mouseY, partialTicks);
        final int i = this.leftPos;
        final int j = this.topPos;
        stack.blit(TEXT, i, j, 0, 0, this.imageWidth, this.inventoryRows * SLOT_OFFSET + SLOT_OFFSET - 1, TEXTURE_SIZE, TEXTURE_SIZE);
        stack.blit(TEXT, i, j + this.inventoryRows * SLOT_OFFSET + SLOT_OFFSET - 1, 0,
          TEXTURE_OFFSET, this.imageWidth, TEXTURE_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
    }
}
