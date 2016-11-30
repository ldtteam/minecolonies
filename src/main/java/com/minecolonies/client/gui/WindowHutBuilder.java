package com.minecolonies.client.gui;

import com.blockout.Pane;
import com.blockout.controls.Button;
import com.blockout.controls.Label;
import com.blockout.views.ScrollingList;
import com.blockout.views.SwitchView;
import com.minecolonies.colony.buildings.BuildingBuilder;
import com.minecolonies.lib.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Window for the builder hut.
 */
public class WindowHutBuilder extends AbstractWindowWorkerBuilding<BuildingBuilder.View>
{
    /**
     * The builders gui file.
     */
    private static final String HUT_BUILDER_RESOURCE_SUFFIX = ":gui/windowHutBuilder.xml";
    private static final String LIST_RESOURCES              = "resources";
    private static final String PAGE_RESOURCES              = "resourceActions";
    private static final String BUTTON_PREVPAGE             = "prevPage";
    private static final String BUTTON_NEXTPAGE             = "nextPage";

    private static final String VIEW_PAGES                = "pages";

    private Button             buttonPrevPage;
    private Button             buttonNextPage;

    private Map<String, Integer> resources;
    private final BuildingBuilder.View     builder;

    /**
     * Constructor for window builder hut.
     *
     * @param building {@link com.minecolonies.colony.buildings.BuildingBuilder.View}.
     */
    public WindowHutBuilder(final BuildingBuilder.View building)
    {
        super(building, Constants.MOD_ID + HUT_BUILDER_RESOURCE_SUFFIX);
        this.builder = building;
        pullResourcesFromHut();
    }

    /**
     * Retrieve resources from the building to display in GUI.
     */
    private void pullResourcesFromHut()
    {
        if (builder.getColony().getBuilding(builder.getID()) != null)
        {
            resources = builder.getNeededResources();
        }
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.gui.workerHuts.buildersHut";
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class).setEnabled(false);

        buttonNextPage = findPaneOfTypeByID(BUTTON_NEXTPAGE, Button.class);
        buttonPrevPage = findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class);
        pullResourcesFromHut();

        final Object[] entries = resources.entrySet().toArray();

        final ScrollingList resourceList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        resourceList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return resources.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final Object obj = entries[index];

                if (obj instanceof Map.Entry && ((Map.Entry) obj).getKey() instanceof String && ((Map.Entry) obj).getValue() instanceof Integer)
                {
                    @NotNull final String key = (String) ((Map.Entry) obj).getKey();
                    final int value = (Integer) ((Map.Entry) obj).getValue();
                    rowPane.findPaneOfTypeByID("resource", Label.class).setLabelText(key);
                    rowPane.findPaneOfTypeByID("amount", Label.class).setLabelText(Integer.toString(value));
                }
            }
        });
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        switch (button.getID())
        {
            case BUTTON_PREVPAGE:
                findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).previousView();
                buttonPrevPage.setEnabled(false);
                buttonNextPage.setEnabled(true);
                break;
            case BUTTON_NEXTPAGE:
                findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).nextView();
                buttonPrevPage.setEnabled(true);
                buttonNextPage.setEnabled(false);
                break;
            default:
                super.onButtonClicked(button);
                break;
        }
    }

    @Override
    public void onUpdate()
    {
        final String currentPage = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView().getID();
        if (currentPage.equals(PAGE_RESOURCES))
        {
            pullResourcesFromHut();
            window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class).refreshElementPanes();
        }
    }
}
