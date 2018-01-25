package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.inventory.CraftingGUIBuilding;
import com.minecolonies.coremod.network.messages.AddRemoveRecipeMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Crafting gui.
 */
public class WindowGuiCrafting extends GuiContainer
{
    private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation(Constants.MOD_ID, "textures/gui/crafting2x2.png");

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
    public WindowGuiCrafting(final InventoryPlayer playerInv, final World worldIn, final AbstractBuildingWorker.View building)
    {
        super(new CraftingGUIBuilding(playerInv, worldIn));
        this.building = building;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.doneButton = this.addButton(new GuiButton(0, guiLeft + BUTTON_X_OFFSET, guiTop + BUTTON_Y_POS, BUTTON_WIDTH, BUTTON_HEIGHT, I18n.format("gui.done")));

        if(Math.pow(2, building.getBuildingLevel()) < (building.getRecipes().size() + 1))
        {
            this.doneButton.displayString = LanguageHandler.format("com.minecolonies.coremod.gui.recipe.full");
            this.doneButton.enabled = false;
        }
    }

    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (doneButton.isMouseOver())
        {
            final List<ItemStack> input = new ArrayList<>();
            final List<ItemStack> secondaryOutput = new ArrayList<>();

            for(int i = 1; i <= CRAFTING_GRID_SIZE; i++)
            {
                final ItemStack stack = inventorySlots.getInventory().get(i);
                if(ItemStackUtils.isEmpty(stack))
                {
                    continue;
                }
                final ItemStack copy = stack.copy();
                ItemStackUtils.setSize(copy, 1);

                input.add(copy);

                if(copy.getItem().hasContainerItem(copy))
                {
                    secondaryOutput.add(copy.getItem().getContainerItem(copy));
                }
            }

            final ItemStack primaryOutput =  inventorySlots.getSlot(0).getStack().copy();

            if(!ItemStackUtils.isEmpty(primaryOutput))
            {
                MineColonies.getNetwork().sendToServer(new AddRemoveRecipeMessage(input, 2, primaryOutput, secondaryOutput, building, false));
                LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, "com.minecolonies.coremod.gui.recipe.done");
            }
        }
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY)
    {
        this.fontRenderer.drawString(I18n.format("container.crafting", new Object[0]), X_OFFSET, Y_OFFSET, GUI_COLOR);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(CRAFTING_TABLE_GUI_TEXTURES);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }
}
