package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.SwitchView;
import com.minecolonies.api.tileentities.TileEntityGrave;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingGraveyard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Window for the home building.
 */
public class WindowHutGraveyard extends AbstractWindowWorkerBuilding<BuildingGraveyard.View>
{
    /**
     * Tag of the pages view.
     */
    private static final String VIEW_PAGES = "pages";

    /**
     * Resource suffix of the GUI.
     */
    private static final String HUT_GRAVEYARD_RESOURCE_SUFFIX = ":gui/windowhutgraveyard.xml";

    /**
     * Id of the the graves page inside the GUI.
     */
    private static final String PAGE_GRAVES = "pageGraves";

    /**
     * Id of the the graves list inside the GUI.
     */
    private static final String LIST_GRAVES = "graves";

    /**
     * Id of the the name label inside the GUI.
     */
    private static final String TAG_NAME = "name";

    /**
     * Id of the the distance label inside the GUI.
     */
    private static final String TAG_DISTANCE = "dist";

    /**
     * Id of the the direction label inside the GUI.
     */
    private static final String TAG_DIRECTION = "dir";

    /**
     * Id of the icon inside the GUI.
     */
    private static final String TAG_ICON = "icon";

    /**
     * List of fields the building seeds.
     */
    private List<BlockPos> graves = new ArrayList<>();

    /**
     * ScrollList with the fields.
     */
    private ScrollingList graveList;

    /**
     * The world.
     */
    private final ClientWorld world = Minecraft.getInstance().world;

    /**
     * Constructor for the window of the graveyard.
     *
     * @param building {@link BuildingGraveyard.View}.
     */
    public WindowHutGraveyard(final BuildingGraveyard.View building)
    {
        super(building, Constants.MOD_ID + HUT_GRAVEYARD_RESOURCE_SUFFIX);
    }

     /**
     * Retrieve levels from the building to display in GUI.
     */
    private void pullInformationFromHut()
    {
        graves = building.getGraves();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        graveList = findPaneOfTypeByID(LIST_GRAVES, ScrollingList.class);
        graveList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return graves.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final BlockPos field = graves.get(index);
                @NotNull final String distance = Integer.toString((int) Math.sqrt(BlockPosUtil.getDistanceSquared(field, building.getPosition())));
                final String direction = BlockPosUtil.calcDirection(building.getPosition(), field);
                final TileEntity entity = world.getTileEntity(field);
                if (entity instanceof TileEntityGrave)
                {
                    rowPane.findPaneOfTypeByID(TAG_NAME, Text.class).setText("Grave of " + ((TileEntityGrave) entity).getSavedCitizenName());
                    rowPane.findPaneOfTypeByID(TAG_DISTANCE, Text.class).setText(distance + "m");
                    rowPane.findPaneOfTypeByID(TAG_DIRECTION, Text.class).setText(direction);
                }
            }
        });
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.graveyard";
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        final String currentPage = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView().getID();
        if (currentPage.equals(PAGE_GRAVES))
        {
            pullInformationFromHut();
            window.findPaneOfTypeByID(LIST_GRAVES, ScrollingList.class).refreshElementPanes();
        }
    }
}
