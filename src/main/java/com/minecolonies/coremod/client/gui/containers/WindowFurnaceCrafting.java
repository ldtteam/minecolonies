package com.minecolonies.coremod.client.gui.containers;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.inventory.container.ContainerCraftingFurnace;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingSmelterCrafter;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.AddRemoveRecipeMessage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Furnace crafting gui.
 */
public class WindowFurnaceCrafting extends ContainerScreen<ContainerCraftingFurnace>
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
     * The building the window belongs to.
     */
    private final ContainerCraftingFurnace container;

    /**
     * The building assigned to this.
     */
    private final AbstractBuildingSmelterCrafter.View building;

    /**
     * Create a crafting gui window.
     * @param container the container.
     * @param playerInventory the player inv.
     * @param iTextComponent the display text component.
     */
    public WindowFurnaceCrafting(final ContainerCraftingFurnace container, final PlayerInventory playerInventory, final ITextComponent iTextComponent)
    {
        super(container, playerInventory, iTextComponent);
        this.container = container;
        this.building = (AbstractBuildingSmelterCrafter.View) IColonyManager.getInstance().getBuildingView(playerInventory.player.dimension.getId(), container.getPos());
    }

    @Override
    protected void init()
    {
        super.init();
        final String buttonDisplay = building.canRecipeBeAdded() ? I18n.format("gui.done") : LanguageHandler.format("com.minecolonies.coremod.gui.recipe.full");
        /*
         * The button to click done after finishing the recipe.
         */
        final Button doneButton = new Button(guiLeft + BUTTON_X_OFFSET, guiTop + BUTTON_Y_POS, BUTTON_WIDTH, BUTTON_HEIGHT, buttonDisplay, new OnButtonPress());
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
                final List<ItemStack> input = new ArrayList<>();
                input.add(container.inventorySlots.get(0).getStack());
                final ItemStack primaryOutput = container.inventorySlots.get(1).getStack().copy();

                if(!ItemStackUtils.isEmpty(primaryOutput))
                {
                    Network.getNetwork().sendToServer(new AddRemoveRecipeMessage(building, input, 1, primaryOutput, false));
                }
            }
        }
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY)
    {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(CRAFTING_FURNACE);
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
