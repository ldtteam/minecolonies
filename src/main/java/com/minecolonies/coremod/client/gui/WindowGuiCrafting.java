package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.ItemStackUtils;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.inventory.CraftingGUIBuilding;
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
import java.util.LinkedList;
import java.util.List;

/**
 * AbstractCrafting gui.
 */
public class WindowGuiCrafting extends GuiContainer
{
    private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation(Constants.MOD_ID, "textures/gui/crafting2x2.png");

    private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES3X3 = new ResourceLocation(Constants.MOD_ID, "textures/gui/crafting3x3.png");

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
     * Size of the crafting grid.
     */
    private static final int CRAFTING_GRID_SIZE = 4;

    /**
     * Size of the crafting grid.
     */
    private static final int MAX_CRAFTING_GRID_SIZE = 9;

    /**
     * The button to click done after finishing the recipe.
     */
    private GuiButton doneButton;

    /**
     * The building the window belongs to.
     */
    private final AbstractBuildingWorker.View building;

    /**
     * Check if the GUI should display for 9 or 4 slots.
     */
    private final boolean completeCrafting;

    /**
     * Create a crafting gui window.
     *
     * @param playerInv     the player.
     * @param worldIn       the world.
     * @param building      the building.
     */
    public WindowGuiCrafting(final PlayerInventory playerInv, final World worldIn, final AbstractBuildingWorker.View building)
    {
        super(new CraftingGUIBuilding(playerInv, worldIn, building.canCraftComplexRecipes()));
        this.building = building;
        completeCrafting = building.canCraftComplexRecipes();
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
            final List<ItemStack> input = new LinkedList<>();

            for(int i = 1; i <= (completeCrafting ? MAX_CRAFTING_GRID_SIZE : CRAFTING_GRID_SIZE); i++)
            {
                final ItemStack stack = inventorySlots.getInventory().get(i);
                final ItemStack copy = stack.copy();
                ItemStackUtils.setSize(copy, 1);

                input.add(copy);
            }

            final ItemStack primaryOutput =  inventorySlots.getSlot(0).getStack().copy();

            if(!ItemStackUtils.isEmpty(primaryOutput))
            {
                Network.getNetwork().sendToServer(new AddRemoveRecipeMessage(input, completeCrafting ? 3 : 2, primaryOutput, building, false));
            }
        }
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY)
    {
        this.fontRenderer.drawString(I18n.format("container.crafting"), X_OFFSET, Y_OFFSET, GUI_COLOR);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if(completeCrafting)
        {
            this.mc.getTextureManager().bindTexture(CRAFTING_TABLE_GUI_TEXTURES3X3);
        }
        else
        {
            this.mc.getTextureManager().bindTexture(CRAFTING_TABLE_GUI_TEXTURES);
        }
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }
}
