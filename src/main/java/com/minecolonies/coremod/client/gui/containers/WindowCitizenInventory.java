package com.minecolonies.coremod.client.gui.containers;

import com.minecolonies.api.inventory.container.ContainerCitizenInventory;
import com.minecolonies.api.util.constant.Constants;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * ------------ Class not Documented ------------
 */
public class WindowCitizenInventory extends ContainerScreen<ContainerCitizenInventory>
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
     * General y offset.
     */
    private static final int Y_OFFSET = 114;

    /**
     * Amount of slots each row.
     */
    private static final int SLOTS_EACH_ROW = 9;

    /**
     * window height is calculated with these values; the more rows, the heigher
     */
    private final int inventoryRows;

    public WindowCitizenInventory(final ContainerCitizenInventory container, final PlayerInventory playerInventory, final ITextComponent iTextComponent)
    {
        super(container, playerInventory, iTextComponent);
        this.inventoryRows = (container.getItems().size() - 36) / 9;

        this.imageHeight = Y_OFFSET + Math.min(SLOTS_EACH_ROW, this.inventoryRows) * SLOT_OFFSET;
        if (this.inventoryRows > SLOTS_EACH_ROW - 1)
        {
            this.imageWidth = this.imageWidth + (this.inventoryRows - SLOTS_EACH_ROW) * (SLOTS_EACH_ROW + 1);
        }
    }

    @Override
    public void render(@NotNull final MatrixStack stack, int x, int y, float z)
    {
        this.renderBackground(stack);
        super.render(stack, x, y, z);
        this.renderTooltip(stack, x, y);
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    protected void renderLabels(@NotNull final MatrixStack stack, final int mouseX, final int mouseY)
    {
        this.font.draw(stack, this.menu.getDisplayName(), 8, 6, 4210752);
        this.font.draw(stack, this.inventory.getDisplayName().getString(), 8, 20 + this.inventoryRows * SLOT_OFFSET, 4210752);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    protected void renderBg(@NotNull final MatrixStack stack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.minecraft.getTextureManager().bind(TEXT);

        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        blit(stack, i, j, 0, 0, this.imageWidth, this.inventoryRows * SLOT_OFFSET + SLOT_OFFSET - 1, TEXTURE_SIZE, TEXTURE_SIZE);
        blit(stack, i, j + this.inventoryRows * SLOT_OFFSET + SLOT_OFFSET - 1, 0,
          TEXTURE_OFFSET, this.imageWidth, TEXTURE_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
    }
}
