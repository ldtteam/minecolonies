package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.modules.IEntityListModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static org.jline.utils.AttributedStyle.WHITE;

/**
 * BOWindow for all the filterable entity lists.
 */
public class EntityListModuleWindow extends AbstractModuleWindow
{
    /**
     * Resource scrolling list.
     */
    private final ScrollingList resourceList;

    /**
     * The building this belongs to.
     */
    protected final IBuildingView building;

    /**
     * The filter for the resource list.
     */
    private String filter = "";

    /**
     * Check for inversion of the list.
     */
    private final boolean isInverted;

    /**
     * Grouped list that can be further filtered.
     */
    private List<ResourceLocation> groupedItemList;

    /**
     * Grouped list after applying the current temporary filter.
     */
    private final List<ResourceLocation> currentDisplayedList = new ArrayList<>();

    /**
     * Update delay.
     */
    private int tick;

    /**
     * @param building   the building it belongs to.
     * @param res   the building res id.
     * @param moduleView   the assigned module view.
     */
    public EntityListModuleWindow(
      final String res,
      final IBuildingView building,
      final IEntityListModuleView moduleView)
    {
        super(building, res);

        resourceList = window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        window.findPaneOfTypeByID(DESC_LABEL, Text.class).setText(Component.translatable(moduleView.getDesc().toLowerCase(Locale.US)));
        this.building = building;
        this.isInverted = moduleView.isInverted();
        this.id = moduleView.getId();

        groupedItemList = new ArrayList<>(IColonyManager.getInstance().getCompatibilityManager().getAllMonsters());

        window.findPaneOfTypeByID(INPUT_FILTER, TextField.class).setHandler(input -> {
            final String newFilter = input.getText();
            if (!newFilter.equals(filter))
            {
                filter = newFilter;
                this.tick = 10;
            }
        });
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        super.onButtonClicked(button);
        if (Objects.equals(button.getID(), BUTTON_SWITCH))
        {
            switchClicked(button);
        }
        else if (Objects.equals(button.getID(), BUTTON_RESET_DEFAULT))
        {
            reset();
        }
    }

    @Override
    public void onOpened()
    {
        updateResources();
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

    /**
     * Fired when assign has been clicked in the field list.
     *
     * @param button clicked button.
     */
    private void switchClicked(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);
        final ResourceLocation item = currentDisplayedList.get(row);
        final boolean on = button.getText().equals(Component.translatable(ON));
        final boolean add = (on && isInverted) || (!on && !isInverted);
        final IEntityListModuleView module = building.getModuleViewMatching(IEntityListModuleView.class, view -> view.getId().equals(id));

        if (add)
        {
            module.addEntity(item);
        }
        else
        {
            module.removeEntity(item);
        }

        resourceList.refreshElementPanes();
    }

    /**
     * Fired when reset to default has been clicked.
     */
    private void reset()
    {
        final IEntityListModuleView module = building.getModuleViewMatching(IEntityListModuleView.class, view -> view.getId().equals(id));
        module.clearEntities();
        resourceList.refreshElementPanes();
    }

    /**
     * Update the item list.
     */
    private void updateResources()
    {
        final Predicate<ResourceLocation> filterPredicate = res -> filter.isEmpty() || BuiltInRegistries.ENTITY_TYPE.get(res).getDescription().getString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US)) || res.toString().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US));
        currentDisplayedList.clear();
        for (final ResourceLocation storage : groupedItemList)
        {
            if (filterPredicate.test(storage))
            {
                currentDisplayedList.add(storage);
            }
        }

        currentDisplayedList.sort((o1, o2) -> {

            boolean o1Allowed = building.getModuleViewMatching(IEntityListModuleView.class, view -> view.getId().equals(id)).isAllowedEntity(o1);

            boolean o2Allowed = building.getModuleViewMatching(IEntityListModuleView.class, view -> view.getId().equals(id)).isAllowedEntity(o2);

            if(!o1Allowed && o2Allowed)
            {
                return isInverted ? -1 : 1;
            }
            else if(o1Allowed && !o2Allowed)
            {
                return isInverted ? 1 : -1;
            }
            else
            {
                return 0;
            }
        });

        updateResourceList();
    }

    /**
     * Updates the resource list in the GUI with the info we need.
     */
    private void updateResourceList()
    {
        resourceList.enable();
        resourceList.show();

        //Creates a dataProvider for the unemployed resourceList.
        resourceList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return currentDisplayedList.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ResourceLocation resource = currentDisplayedList.get(index);
                final Text resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class);
                resourceLabel.setText(BuiltInRegistries.ENTITY_TYPE.get(resource).getDescription());
                resourceLabel.setColors(WHITE);
                final boolean isAllowedItem  = building.getModuleViewMatching(IEntityListModuleView.class, view -> view.getId().equals(id)).isAllowedEntity(resource);
                final Button switchButton = rowPane.findPaneOfTypeByID(BUTTON_SWITCH, Button.class);

                if ((isInverted && !isAllowedItem) || (!isInverted && isAllowedItem))
                {
                    switchButton.setText(Component.translatable(ON));
                }
                else
                {
                    switchButton.setText(Component.translatable(OFF));
                }
            }
        });
    }
}
