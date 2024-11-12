package com.minecolonies.core.client.gui.containers;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IConcreteDeliverable;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.inventory.container.ContainerCraftingFurnace;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.modules.WindowSelectRequest;
import com.minecolonies.core.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
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
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_MAXIMUM_NUMBER_RECIPES;
import static com.minecolonies.api.util.constant.WindowConstants.CRAFTING_SWITCH_SIZE;
import static com.minecolonies.api.util.constant.WindowConstants.CRAFTING_SWITCH_TEXTURE;
import static com.minecolonies.api.util.constant.translation.BaseGameTranslationConstants.BASE_GUI_DONE;

/**
 * Furnace crafting gui.
 */
public class WindowFurnaceCrafting extends AbstractContainerScreen<ContainerCraftingFurnace>
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
     * Request list button location.
     */
    private static final int REQUEST_X_OFFSET = 90 - (CRAFTING_SWITCH_SIZE.width / 2);
    private static final int REQUEST_Y_OFFSET = 70 - CRAFTING_SWITCH_SIZE.height;

    /**
     * The building the window belongs to.
     */
    private final ContainerCraftingFurnace container;

    /**
     * The building assigned to this.
     */
    private final AbstractBuildingView building;

    /**
     * The module this crafting window is for.
     */
    private final CraftingModuleView module;

    private final Map<IRequest<?>, ItemStack> requestables = new HashMap<>();

    /**
     * Create a crafting gui window.
     *
     * @param container       the container.
     * @param playerInventory the player inv.
     * @param iTextComponent  the display text component.
     */
    public WindowFurnaceCrafting(final ContainerCraftingFurnace container, final Inventory playerInventory, final Component iTextComponent)
    {
        super(container, playerInventory, iTextComponent);
        this.container = container;
        this.building = (AbstractBuildingView) IColonyManager.getInstance().getBuildingView(playerInventory.player.level.dimension(), container.getPos());
        this.module =(CraftingModuleView) building.getModuleView(container.getModuleId());
    }

    @NotNull
    public AbstractBuildingView getBuildingView()
    {
        return building;
    }

    @Override
    protected void init()
    {
        super.init();
        final Component buttonDisplay = Component.translatable(module.canLearn(ModCraftingTypes.SMELTING.get()) ? BASE_GUI_DONE : WARNING_MAXIMUM_NUMBER_RECIPES);
        /*
         * The button to click done after finishing the recipe.
         */
        final Button doneButton = new Button.Builder(buttonDisplay, new OnButtonPress()).pos(leftPos + BUTTON_X_OFFSET, topPos + BUTTON_Y_POS).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(doneButton);
        if (!module.canLearn(ModCraftingTypes.SMELTING.get()))
        {
            doneButton.active = false;
        }

        final ImageButton requestsButton = new ImageButton(leftPos + REQUEST_X_OFFSET, topPos + REQUEST_Y_OFFSET, CRAFTING_SWITCH_SIZE.width, CRAFTING_SWITCH_SIZE.height,
                CRAFTING_SWITCH_SIZE.width + 1, 0, CRAFTING_SWITCH_SIZE.height + 1, CRAFTING_SWITCH_TEXTURE, btn ->
        {
            requestables.clear();
            new WindowSelectRequest(this.building, this::matchingRequest, this::reopenWithRequest).open();
        });
        this.addRenderableWidget(requestsButton);
    }

    public class OnButtonPress implements Button.OnPress
    {
        @Override
        public void onPress(@NotNull final Button button)
        {
            if (module.canLearn(ModCraftingTypes.SMELTING.get()))
            {
                final List<ItemStorage> input = new ArrayList<>();
                input.add(new ItemStorage(container.slots.get(0).getItem()));
                final ItemStack primaryOutput = container.slots.get(1).getItem().copy();

                if (!ItemStackUtils.isEmpty(primaryOutput))
                {
                    Network.getNetwork().sendToServer(new AddRemoveRecipeMessage(building, input, 1, primaryOutput, false, Blocks.FURNACE, module.getProducer().getRuntimeID()));
                }
            }
        }
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
        if (!stack.isEmpty() && WindowCrafting.JEI_REQUEST_HOOK != null)
        {
            WindowCrafting.JEI_REQUEST_HOOK.accept(stack);
        }
    }

    /**
     * Draws the background layer of this container (behind the items).
     */
    @Override
    protected void renderBg(@NotNull final GuiGraphics stack, final float partialTicks, final int mouseX, final int mouseY)
    {
        stack.blit(CRAFTING_FURNACE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(@NotNull final GuiGraphics stack, int x, int y, float z)
    {
        this.renderBackground(stack);
        super.render(stack, x, y, z);
        this.renderTooltip(stack, x, y);
    }
}
