package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.inventory.ContainerGUICraftingFurnace;
import com.minecolonies.coremod.network.messages.AddRemoveRecipeMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Furnace crafting gui.
 */
public class WindowGuiFurnaceCrafting extends GuiContainer
{
    private static final ResourceLocation CRAFTING_FURNACE = new ResourceLocation(Constants.MOD_ID, "textures/gui/furnace.png");

    /**
     * X offset of the button.
     */
    private static final int BUTTON_X_OFFSET = 1;

    /**
     * Y offset of the button.
     */
    private static final int BUTTON_Y_POS = 170;

    /**
     * Button width.
     */
    private static final int BUTTON_WIDTH = 150;

    /**
     * Button height.
     */
    private static final int BUTTON_HEIGHT = 20;

    /**
     * Color of the gui description.
     */
    private static final int GUI_COLOR = 4_210_752;

    /**
     * X offset of the gui description.
     */
    private static final int X_OFFSET = 97;

    /**
     * Y offset of the gui description.
     */
    private static final int Y_OFFSET = 8;

    /**
     * The button to click done after finishing the recipe.
     */
    private GuiButton doneButton;

    /**
     * The building the window belongs to.
     */
    private final AbstractBuildingWorker.View building;

    /**
     * Create a crafting gui window.
     *
     * @param playerInv     the player.
     * @param worldIn       the world.
     * @param building      the building.
     */
    public WindowGuiFurnaceCrafting(final PlayerInventory playerInv, final World worldIn, final AbstractBuildingWorker.View building)
    {
        super(new ContainerGUICraftingFurnace(playerInv, worldIn));
        this.building = building;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.doneButton = this.addButton(new GuiButton(0, guiLeft + BUTTON_X_OFFSET, guiTop + BUTTON_Y_POS, BUTTON_WIDTH, BUTTON_HEIGHT, I18n.format("gui.done")));

        if(!building.canRecipeBeAdded())
        {
            this.doneButton.displayString = LanguageHandler.format("com.minecolonies.coremod.gui.recipe.full");
            this.doneButton.enabled = false;
        }
    }

    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (building.canRecipeBeAdded() && doneButton.isMouseOver())
        {
            final List<ItemStack> input = new ArrayList<>();
            input.add(inventorySlots.getInventory().get(0));
            final ItemStack primaryOutput =  inventorySlots.getSlot(1).getStack().copy();

            if(!ItemStackUtils.isEmpty(primaryOutput))
            {
                MineColonies.getNetwork().sendToServer(new AddRemoveRecipeMessage(input, 1, primaryOutput, building, false));
            }
        }
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY)
    {
        this.fontRenderer.drawString(I18n.format("container.furnace", new Object[0]), X_OFFSET, Y_OFFSET, GUI_COLOR);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(CRAFTING_FURNACE);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }
}
