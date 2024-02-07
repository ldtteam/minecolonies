package com.minecolonies.core.client.gui.containers;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.network.messages.server.colony.building.worker.AddRemoveRecipeMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_MAXIMUM_NUMBER_RECIPES;
import static com.minecolonies.api.util.constant.translation.BaseGameTranslationConstants.BASE_GUI_DONE;

/**
 * AbstractCrafting gui.
 */
public class WindowCrafting extends AbstractContainerScreen<ContainerCrafting>
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
    private final AbstractBuildingView building;

    /**
     * Check if the GUI should display for 9 or 4 slots.
     */
    private final boolean completeCrafting;

    /**
     * The module view.
     */
    private final CraftingModuleView module;

    /**
     * Create a crafting gui window.
     *
     * @param container       the container.
     * @param playerInventory the player inv.
     * @param iTextComponent  the display text component.
     */
    public WindowCrafting(final ContainerCrafting container, final Inventory playerInventory, final Component iTextComponent)
    {
        super(container, playerInventory, iTextComponent);
        this.building = (AbstractBuildingView) IColonyManager.getInstance().getBuildingView(playerInventory.player.level().dimension(), container.getPos());
        this.module = (CraftingModuleView) building.getModuleView(container.getModuleId());
        completeCrafting = module.canLearn(ModCraftingTypes.LARGE_CRAFTING.get());
    }

    @NotNull
    public AbstractBuildingView getBuildingView()
    {
        return building;
    }

    public boolean isCompleteCrafting()
    {
        return completeCrafting;
    }

    @Override
    protected void init()
    {
        super.init();
        final Component buttonDisplay = Component.translatable(module.canLearn(ModCraftingTypes.SMALL_CRAFTING.get()) ? BASE_GUI_DONE : WARNING_MAXIMUM_NUMBER_RECIPES);
        /*
         * The button to click done after finishing the recipe.
         */
        final Button doneButton = new Button.Builder(buttonDisplay, new WindowCrafting.OnButtonPress()).pos(leftPos + BUTTON_X_OFFSET, topPos + BUTTON_Y_POS).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(doneButton);
        if (!module.canLearn(ModCraftingTypes.SMALL_CRAFTING.get()))
        {
            doneButton.active = false;
        }

    }

    public class OnButtonPress implements Button.OnPress
    {
        @Override
        public void onPress(final Button button)
        {
            if (module.canLearn(ModCraftingTypes.SMALL_CRAFTING.get()))
            {
                final List<ItemStorage> input = new LinkedList<>();

                for (int i = 0; i < (completeCrafting ? MAX_CRAFTING_GRID_SIZE : CRAFTING_GRID_SIZE); i++)
                {
                    final ItemStack stack = menu.craftMatrix.getItem(i);
                    final ItemStack copy = stack.copy();
                    ItemStackUtils.setSize(copy, 1);

                    input.add(new ItemStorage(copy));
                }

                final ItemStack primaryOutput = menu.craftResult.getItem(0).copy();
                final List<ItemStack> secondaryOutputs = menu.getRemainingItems();

                if (!ItemStackUtils.isEmpty(primaryOutput))
                {
                    new AddRemoveRecipeMessage(building, input, completeCrafting ? 3 : 2, primaryOutput, secondaryOutputs, false, module.getProducer().getRuntimeID()).sendToServer();
                }
            }
        }
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void renderLabels(@NotNull final GuiGraphics stack, final int mouseX, final int mouseY)
    {
        stack.drawString(this.font, Component.translatable("container.crafting").getString(), X_OFFSET, Y_OFFSET, GUI_COLOR, false);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    @Override
    protected void renderBg(@NotNull final GuiGraphics stack, final float partialTicks, final int mouseX, final int mouseY)
    {
        final ResourceLocation texture;
        if (completeCrafting)
        {
            texture = CRAFTING_TABLE_GUI_TEXTURES3X3;
        }
        else
        {
            texture = CRAFTING_TABLE_GUI_TEXTURES;
        }
        stack.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(@NotNull final GuiGraphics stack, int x, int y, float z)
    {
        super.render(stack, x, y, z);
        this.renderTooltip(stack, x, y);
    }
}
