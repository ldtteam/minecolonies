package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.views.Box;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.ScrollingListContainer;
import com.ldtteam.structurize.client.gui.util.InputFilters;
import com.minecolonies.api.colony.buildings.modules.IBuildingModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.modules.RequestTreeWindowModule;
import com.minecolonies.core.client.gui.modules.TabsWindowModule;
import com.minecolonies.core.colony.buildings.workerbuildings.PostBox;
import com.minecolonies.core.network.messages.server.colony.OpenInventoryMessage;
import com.minecolonies.core.network.messages.server.colony.building.postbox.PostBoxRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.LABEL_MAIN_TAB_NAME;

/**
 * BOWindow for the request PostBox GUI.
 */
public class WindowPostBoxMain extends AbstractWindowSkeleton
{
    private static final int TABS_X_OFFSET = 34;

    /**
     * ID of the delivery available button inside the GUI.
     */
    private static final String TAG_BUTTON_DELIVER_AVAILABLE = "deliverAvailable";

    /**
     * String which displays full delivery.
     */
    private static final String RED_X = "§n§4X";

    /**
     * String which displays partial delivery.
     */
    private static final String APPROVE = "✓";

    /**
     * List of all item stacks in the game.
     */
    private List<ItemStack> allItems = new ArrayList<>();

    /**
     * Resource scrolling list.
     */
    private final ScrollingList stackList;

    /**
     * The post box view instance.
     */
    private final PostBox.View postBoxView;

    /**
     * The request tree window module.
     */
    private final PostBoxRequestTreeWindowModule requestTreeWindowModule;

    /**
     * The filter for the resource list.
     */
    private String filter = "";

    /**
     * Whether to deliver what's currently in the warehouse and then cancel order.
     */
    private boolean deliverAvailable;

    /**
     * Update delay.
     */
    private int tick;

    /**
     * Create the postBox GUI.
     *
     * @param postBoxView the building view.
     */
    public WindowPostBoxMain(final PostBox.View postBoxView)
    {
        super(new ResourceLocation(Constants.MOD_ID, "gui/windowpostboxrequest.xml"));
        this.postBoxView = postBoxView;
        this.requestTreeWindowModule = registerLayoutModule(PostBoxRequestTreeWindowModule::new, postBoxView, 261, 44);
        registerPostboxTabs(this, postBoxView);

        registerButton(BUTTON_INVENTORY, this::inventoryClicked);
        registerButton(BUTTON_REQUEST, this::requestClicked);
        registerButton(TAG_BUTTON_DELIVER_AVAILABLE, this::deliverPartialClicked);

        window.findPaneOfTypeByID(NAME_LABEL, TextField.class).setHandler(input -> {
            final String newFilter = input.getText();
            if (!newFilter.equals(filter))
            {
                filter = newFilter;
                this.tick = 10;
                for (final Pane child : ((ScrollingListContainer) findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class).getChildren().get(0)).getChildren())
                {
                    if (child instanceof Box box)
                    {
                        for (final Pane boxChild : box.getChildren())
                        {
                            if (boxChild.getID().equals(INPUT_QTY))
                            {
                                ((TextField) boxChild).setText("");
                            }
                        }
                    }
                }
            }
        });

        stackList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        stackList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return allItems.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ItemStack resource = allItems.get(index);
                final Text resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class);
                resourceLabel.setText(resource.getHoverName());
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(resource);
                rowPane.findPaneOfTypeByID(INPUT_QTY, TextField.class).setFilter(InputFilters.ONLY_POSITIVE_NUMBERS_MAX1k);
            }
        });
    }

    /**
     * Action when a button opening an inventory is clicked.
     */
    private void inventoryClicked()
    {
        Network.getNetwork().sendToServer(new OpenInventoryMessage(postBoxView));
    }

    /**
     * Action executed when request is clicked.
     *
     * @param button the clicked button.
     */
    private void requestClicked(final Button button)
    {
        final int row = stackList.getListElementIndexByPane(button);
        final ItemStack stack = allItems.get(row);
        int qty = stack.getMaxStackSize();
        for (final Pane child : button.getParent().getChildren())
        {
            if (child.getID().equals(INPUT_QTY))
            {
                try
                {
                    qty = Integer.parseInt(((TextField) child).getText());
                }
                catch (final NumberFormatException ignored)
                {
                }
            }
        }

        Network.getNetwork().sendToServer(new PostBoxRequestMessage(postBoxView, stack.copy(), qty, deliverAvailable));
        requestTreeWindowModule.refreshOpenRequests();
    }

    private void deliverPartialClicked(@NotNull final Button button)
    {
        if (button.getTextAsString().equals(RED_X))
        {
            button.setText(Component.literal(APPROVE));
            this.deliverAvailable = true;
        }
        else
        {
            button.setText(Component.literal(RED_X));
            this.deliverAvailable = false;
        }
    }

    /**
     * Reusable method for the postbox to render the tabs.
     */
    public static void registerPostboxTabs(final AbstractWindowSkeleton window, final IBuildingView buildingView)
    {
        final TabsWindowModule tabsWindowModule = window.registerModule(TabsWindowModule::new, new Random(buildingView.getID().hashCode()));
        tabsWindowModule.setTabXOffset(TABS_X_OFFSET);

        int nextTabIndex = 0;

        tabsWindowModule.renderTabButton(nextTabIndex++,
            TabsWindowModule.TabImageSide.LEFT,
            new ResourceLocation(Constants.MOD_ID, "textures/gui/modules/main.png"),
            Component.translatable(LABEL_MAIN_TAB_NAME),
            button -> buildingView.getWindow().open());

        final List<IBuildingModuleView> allModuleViews = buildingView.getAllModuleViews();
        for (final IBuildingModuleView view : allModuleViews)
        {
            if (!view.isPageVisible())
            {
                continue;
            }

            tabsWindowModule.renderTabButton(nextTabIndex++,
                TabsWindowModule.TabImageSide.LEFT,
                view.getIconResourceLocation(),
                Optional.ofNullable(view.getDesc()).map(Component::copy).orElse(null),
                button -> {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
                    view.getWindow().open();
                });
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        findPaneOfTypeByID(TAG_BUTTON_DELIVER_AVAILABLE, Button.class).setText(Component.literal(RED_X));
        updateResources();
    }

    /**
     * Update the item list.
     */
    private void updateResources()
    {
        final List<ItemStack> allItems = ItemStackUtils.allItemsPlusInventory(Minecraft.getInstance().player)
            .stream()
            .filter(stack -> filter.isEmpty() || stack.getDescriptionId().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US)) || stack.getHoverName()
                .getString()
                .toLowerCase(Locale.US)
                .contains(filter.toLowerCase(Locale.US)) || (stack.getItem() instanceof EnchantedBookItem && EnchantedBookItem.getEnchantments(stack)
                .getCompound(0)
                .getString("id")
                .contains(filter.toLowerCase(Locale.US))))
            .sorted(Comparator.comparingInt(s1 -> StringUtils.getLevenshteinDistance(s1.getHoverName().getString(), filter)))
            .toList();

        this.allItems = allItems;
        this.stackList.refreshElementPanes(true);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (tick > 0 && --tick == 0)
        {
            updateResources();
        }
    }

    private static class PostBoxRequestTreeWindowModule extends RequestTreeWindowModule
    {
        @NotNull
        private final PostBox.View buildingView;

        /**
         * Constructor to initiate the window request tree windows.
         *
         * @param parent the parenting window
         */
        private PostBoxRequestTreeWindowModule(final AbstractWindowSkeleton parent, final PostBox.View buildingView)
        {
            super(parent, buildingView.getColony());
            this.buildingView = buildingView;
        }

        @Override
        protected Collection<IRequest<?>> getOpenRequests()
        {
            return buildingView.getOpenRequestsOfBuilding();
        }

        @Override
        protected void onCancel(final @NotNull IRequest<?> request)
        {
            buildingView.onRequestedRequestCancelled(buildingView.getColony().getRequestManager(), request);
        }
    }
}
