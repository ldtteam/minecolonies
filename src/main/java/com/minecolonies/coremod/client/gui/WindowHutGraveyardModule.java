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
 * Window for the Graveyard building.
 */
public class WindowHutGraveyardModule extends AbstractWindowWorkerModuleBuilding<BuildingGraveyard.View>
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
     * Id of the the RIP page inside the GUI.
     */
    private static final String PAGE_RIP = "pageRIP";

    /**
     * Id of the graves list inside the GUI.
     */
    private static final String LIST_GRAVES = "graves";

    /**
     * Id of the citizen list inside the GUI.
     */
    private static final String LIST_CITIZEN = "citizen";

    /**
     * Id of the the name label inside the GUI.
     */
    private static final String TAG_NAME = "name";

    /**
     * Id of the the citizenName label inside the GUI.
     */
    private static final String TAG_CITIZEN_NAME = "citizenName";

    /**
     * Id of the the distance label inside the GUI.
     */
    private static final String TAG_DISTANCE = "dist";

    /**
     * Id of the the direction label inside the GUI.
     */
    private static final String TAG_DIRECTION = "dir";

    /**
     * List of graves to collect
     */
    private List<BlockPos> graves = new ArrayList<>();

    /**
     * List of resting citizen in this building
     */
    private List<String> ripCitizen = new ArrayList<>();

    /**
     * ScrollList with the graves.
     */
    private ScrollingList graveList;

    /**
     * ScrollList with the resting citizen.
     */
    private ScrollingList ripList;

    /**
     * The world.
     */
    private final ClientWorld world = Minecraft.getInstance().level;

    /**
     * Constructor for the window of the graveyard.
     *
     * @param building {@link BuildingGraveyard.View}.
     */
    public WindowHutGraveyardModule(final BuildingGraveyard.View building)
    {
        super(building, Constants.MOD_ID + HUT_GRAVEYARD_RESOURCE_SUFFIX);
    }

     /**
     * Retrieve levels from the building to display in GUI.
     */
    private void pullInformationFromHut()
    {
        graves = building.getGraves();
        ripCitizen = building.getRestingCitizen();
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
                final BlockPos grave = graves.get(index);
                @NotNull final String distance = Integer.toString((int) Math.sqrt(BlockPosUtil.getDistanceSquared(grave, building.getPosition())));
                final String direction = BlockPosUtil.calcDirection(building.getPosition(), grave);
                final TileEntity entity = world.getBlockEntity(grave);
                if (entity instanceof TileEntityGrave)
                {
                    rowPane.findPaneOfTypeByID(TAG_NAME, Text.class).setText("Grave of " +
                            ((((TileEntityGrave) entity).getGraveData() != null) ?
                             ((TileEntityGrave) entity).getGraveData().getCitizenName() :
                             "Unknown Citizen"));
                    rowPane.findPaneOfTypeByID(TAG_DISTANCE, Text.class).setText(distance + "m");
                    rowPane.findPaneOfTypeByID(TAG_DIRECTION, Text.class).setText(direction);
                }
            }
        });

        ripList = findPaneOfTypeByID(LIST_CITIZEN, ScrollingList.class);
        ripList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return ripCitizen.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final String citizenName = ripCitizen.get(index);
                rowPane.findPaneOfTypeByID(TAG_CITIZEN_NAME, Text.class).setText(citizenName);
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
