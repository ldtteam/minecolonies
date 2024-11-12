package com.minecolonies.core.client.gui.containers;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IConcreteDeliverable;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.modules.WindowSelectRequest;
import com.minecolonies.core.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.network.messages.server.SwitchRecipeCraftingTeachingMessage;
import com.minecolonies.core.network.messages.server.colony.building.worker.AddRemoveRecipeMessage;
import com.minecolonies.core.util.DomumOrnamentumUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_MAXIMUM_NUMBER_RECIPES;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.api.util.constant.translation.BaseGameTranslationConstants.BASE_GUI_DONE;

/**
 * AbstractCrafting gui.
 */
public class WindowCrafting extends AbstractContainerScreen<ContainerCrafting>
{
    private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES = new ResourceLocation(MOD_ID, "textures/gui/crafting2x2.png");

    private static final ResourceLocation CRAFTING_TABLE_GUI_TEXTURES3X3 = new ResourceLocation(MOD_ID, "textures/gui/crafting3x3.png");

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
     * Switch button horizontal location.
     */
    private static final int SWITCH_X_OFFSET = 148;

    /**
     * Switch button vertical location.
     */
    private static final int SWITCH_Y_OFFSET = 43 - (CRAFTING_SWITCH_SIZE.height / 2);

    /**
     * Request list button location.
     */
    private static final int REQUEST_X_OFFSET = 100 - (CRAFTING_SWITCH_SIZE.width / 2);
    private static final int REQUEST_Y_OFFSET = 70 - CRAFTING_SWITCH_SIZE.height;

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
     * The recipe switch button.
     */
    private ImageButton switchButton;

    @Nullable public static Consumer<ItemStack> JEI_REQUEST_HOOK;
    private final Map<IRequest<?>, ItemStack> requestables = new HashMap<>();

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
        this.module = (CraftingModuleView) building.getModuleView(container.getModuleId());
        this.completeCrafting = module.canLearn(ModCraftingTypes.LARGE_CRAFTING.get());
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
        final Button doneButton = Button.builder(buttonDisplay, this::onDoneClicked)
                .pos(leftPos + BUTTON_X_OFFSET, topPos + BUTTON_Y_POS)
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(doneButton);
        if (!module.canLearn(ModCraftingTypes.SMALL_CRAFTING.get()))
        {
            doneButton.active = false;
        }

        this.switchButton = new ImageButton(leftPos + SWITCH_X_OFFSET, topPos + SWITCH_Y_OFFSET, CRAFTING_SWITCH_SIZE.width, CRAFTING_SWITCH_SIZE.height,
                0, 0, CRAFTING_SWITCH_SIZE.height + 1, CRAFTING_SWITCH_TEXTURE, btn ->
        {
            Network.getNetwork().sendToServer(new SwitchRecipeCraftingTeachingMessage());
        });
        this.switchButton.visible = false;
        this.addRenderableWidget(this.switchButton);

        if (completeCrafting)
        {
            final ImageButton requestsButton = new ImageButton(leftPos + REQUEST_X_OFFSET, topPos + REQUEST_Y_OFFSET, CRAFTING_SWITCH_SIZE.width, CRAFTING_SWITCH_SIZE.height,
                    CRAFTING_SWITCH_SIZE.width + 1, 0, CRAFTING_SWITCH_SIZE.height + 1, CRAFTING_SWITCH_TEXTURE, btn ->
            {
                requestables.clear();
                new WindowSelectRequest(this.building, this::matchingRequest, this::reopenWithRequest).open();
            });
            this.addRenderableWidget(requestsButton);
        }
    }

    @Override
    protected void containerTick()
    {
        super.containerTick();

        this.switchButton.visible = this.menu.canSwitchRecipes();
    }

    private boolean matchingRequest(@NotNull final IRequest<?> request)
    {
        if (!DomumOrnamentumUtils.getRequestedStack(request).isEmpty()) return false;

        if (request.getRequest() instanceof IConcreteDeliverable deliverable)
        {
            for (final ItemStack stack : deliverable.getRequestedItems())
            {
                // todo filter?
                requestables.put(request, stack);
                return true;
            }
        }
        return false;
    }

    private void reopenWithRequest(@Nullable final IRequest<?> request)
    {
        minecraft.setScreen(this);

        final ItemStack stack = requestables.getOrDefault(request, ItemStack.EMPTY);
        if (!stack.isEmpty() && JEI_REQUEST_HOOK != null)
        {
            JEI_REQUEST_HOOK.accept(stack);
        }
    }

    private void onDoneClicked(final Button button)
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
                Network.getNetwork()
                  .sendToServer(new AddRemoveRecipeMessage(building, input, completeCrafting ? 3 : 2, primaryOutput, secondaryOutputs, false, module.getProducer().getRuntimeID()));
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
        this.renderBackground(stack);
        super.render(stack, x, y, z);
        this.renderTooltip(stack, x, y);
    }
}
