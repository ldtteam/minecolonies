package com.minecolonies.coremod.client.gui.containers;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.AddRemoveRecipeMessage;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.LinkedList;
import java.util.List;

/**
 * AbstractCrafting gui.
 */
public class WindowCrafting extends ContainerScreen<ContainerCrafting>
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
     * The building the window belongs to.
     */
    private final AbstractBuildingWorker.View building;

    /**
     * Check if the GUI should display for 9 or 4 slots.
     */
    private final boolean completeCrafting;

    /**
     * Create a crafting gui window.
     * @param container the container.
     * @param playerInventory the player inv.
     * @param iTextComponent the display text component.
     */
    public WindowCrafting(final ContainerCrafting container, final PlayerInventory playerInventory, final ITextComponent iTextComponent)
    {
        super(container, playerInventory, iTextComponent);
        this.building = (AbstractBuildingWorker.View) IColonyManager.getInstance().getBuildingView(playerInventory.player.dimension.getId(), container.getPos());
        completeCrafting = building.canCraftComplexRecipes();
    }

    @Override
    protected void init()
    {
        super.init();
        final String buttonDisplay = building.canRecipeBeAdded() ? I18n.format("gui.done") : LanguageHandler.format("com.minecolonies.coremod.gui.recipe.full");
        /*
         * The button to click done after finishing the recipe.
         */
        final Button
          doneButton = new Button(guiLeft + BUTTON_X_OFFSET, guiTop + BUTTON_Y_POS, BUTTON_WIDTH, BUTTON_HEIGHT, buttonDisplay, new WindowCrafting.OnButtonPress());
        this.addButton(doneButton);
        if(!building.canRecipeBeAdded())
        {
            doneButton.active = false;
        }
    }

    public class OnButtonPress implements Button.IPressable
    {
        @Override
        public void onPress(final Button button)
        {
            if (building.canRecipeBeAdded())
            {
                final List<ItemStack> input = new LinkedList<>();

                for(int i = 0; i < (completeCrafting ? MAX_CRAFTING_GRID_SIZE : CRAFTING_GRID_SIZE); i++)
                {
                    final ItemStack stack = container.craftMatrix.getStackInSlot(i);
                    final ItemStack copy = stack.copy();
                    ItemStackUtils.setSize(copy, 1);

                    input.add(copy);
                }

                final ItemStack primaryOutput =  container.craftResult.getStackInSlot(0).getStack().copy();

                if(!ItemStackUtils.isEmpty(primaryOutput))
                {
                    Network.getNetwork().sendToServer(new AddRemoveRecipeMessage(building, input, completeCrafting ? 3 : 2, primaryOutput, false));
                }
            }
            onClose();
        }
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY)
    {
        this.font.drawString(I18n.format("container.crafting"), X_OFFSET, Y_OFFSET, GUI_COLOR);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if(completeCrafting)
        {
            this.minecraft.getTextureManager().bindTexture(CRAFTING_TABLE_GUI_TEXTURES3X3);
        }
        else
        {
            this.minecraft.getTextureManager().bindTexture(CRAFTING_TABLE_GUI_TEXTURES);
        }
        this.blit(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public void render(int x, int y, float z)
    {
        this.renderBackground();
        super.render(x, y, z);
        this.renderHoveredToolTip(x, y);
    }
}
