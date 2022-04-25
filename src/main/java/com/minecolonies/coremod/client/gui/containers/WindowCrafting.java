package com.minecolonies.coremod.client.gui.containers;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.AddRemoveRecipeMessage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

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
        this.building = (AbstractBuildingView) IColonyManager.getInstance().getBuildingView(playerInventory.player.level.dimension(), container.getPos());
        this.module = building.getModuleViewMatching(CraftingModuleView.class, v -> v.getId().equals(container.getModuleId()));
        completeCrafting = module.canLearn(ModCraftingTypes.LARGE_CRAFTING);
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
        final String buttonDisplay = module.canLearn(ModCraftingTypes.SMALL_CRAFTING) ? I18n.get("gui.done") : new TranslatableComponent("com.minecolonies.coremod.gui.recipe.full").getString();
        /*
         * The button to click done after finishing the recipe.
         */
        final Button
          doneButton = new Button(leftPos + BUTTON_X_OFFSET, topPos + BUTTON_Y_POS, BUTTON_WIDTH, BUTTON_HEIGHT, new TextComponent(buttonDisplay), new WindowCrafting.OnButtonPress());
        this.addRenderableWidget(doneButton);
        if (!module.canLearn(ModCraftingTypes.SMALL_CRAFTING))
        {
            doneButton.active = false;
        }

    }

    public class OnButtonPress implements Button.OnPress
    {
        @Override
        public void onPress(final Button button)
        {
            if (module.canLearn(ModCraftingTypes.SMALL_CRAFTING))
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
                    Network.getNetwork().sendToServer(new AddRemoveRecipeMessage(building, input, completeCrafting ? 3 : 2, primaryOutput, secondaryOutputs, false, module.getId()));
                }
            }
        }
    }

    /**
     * Draw the foreground layer for the GuiContainer (everything in front of the items)
     */
    @Override
    protected void renderLabels(@NotNull final PoseStack stack, final int mouseX, final int mouseY)
    {
        this.font.draw(stack, I18n.get("container.crafting"), X_OFFSET, Y_OFFSET, GUI_COLOR);
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    @Override
    protected void renderBg(@NotNull final PoseStack stack, final float partialTicks, final int mouseX, final int mouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (completeCrafting)
        {
            RenderSystem.setShaderTexture(0, CRAFTING_TABLE_GUI_TEXTURES3X3);
        }
        else
        {
            RenderSystem.setShaderTexture(0, CRAFTING_TABLE_GUI_TEXTURES);
        }
        this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(@NotNull final PoseStack stack, int x, int y, float z)
    {
        this.renderBackground(stack);
        super.render(stack, x, y, z);
        this.renderTooltip(stack, x, y);
    }
}
