package com.minecolonies.coremod.client.gui.containers;

import com.minecolonies.api.inventory.container.ContainerBuildingInventory;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

public class WindowBuildingInventory extends ContainerScreen<ContainerBuildingInventory>
{
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private final        int              inventoryRows;

    public WindowBuildingInventory(final ContainerBuildingInventory container, final PlayerInventory playerInventory, final ITextComponent component)
    {
        super(container, playerInventory, component);
        this.passEvents = false;
        this.inventoryRows = container.getSize();
        this.ySize = 114 + this.inventoryRows * 18;
    }

    @Override
    public void render(@NotNull MatrixStack matrixStack, int x, int y, float z)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, x, y, z);
        this.renderHoveredTooltip(matrixStack, x, y);
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(@NotNull final MatrixStack stack, int mouseX, int mouseY)
    {
        this.font.drawString(stack, this.title.getString(), 8.0F, 6.0F, 4210752);
        this.font.drawString(stack, this.playerInventory.getDisplayName().getString(), 8.0F, (float) (this.ySize - 96 + 2), 4210752);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(@NotNull MatrixStack stack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.blit(stack, x, y, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
        this.blit(stack, x, y + this.inventoryRows * 18 + 17, 0, 126, this.xSize, 96);
    }
}
