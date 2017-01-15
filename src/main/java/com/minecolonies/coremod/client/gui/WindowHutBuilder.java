package com.minecolonies.coremod.client.gui;

import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.SwitchView;
import com.minecolonies.coremod.colony.buildings.BuildingBuilder;
import com.minecolonies.coremod.lib.Constants;
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
    private static final String VIEW_PAGES = "pages";
    private final BuildingBuilder.View builder;
    private       Map<String, Integer> resources;

    /**
     * Constructor for window builder hut.
     *
     * @param building {@link com.minecolonies.coremod.colony.buildings.BuildingBuilder.View}.
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
        return "com.minecolonies.coremod.gui.workerHuts.buildersHut";
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
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
