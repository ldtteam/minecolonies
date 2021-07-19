package com.minecolonies.coremod.client.gui.modules;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.tileentities.TileEntityGrave;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.colony.buildings.moduleviews.GraveyardManagementModuleView;
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
public class GraveyardManagementWindow extends AbstractModuleWindow
{
    /**
     * Resource suffix of the GUI.
     */
    private static final String HUT_GRAVEYARD_RESOURCE_SUFFIX = ":gui/layouthuts/layoutgraveyard.xml";

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
     * The world.
     */
    private final ClientWorld world = Minecraft.getInstance().level;

    /**
     * The module view.
     */
    private final GraveyardManagementModuleView moduleView;

    /**
     * Constructor for the window of the graveyard.
     *
     * @param building {@link BuildingGraveyard.View}.
     */
    public GraveyardManagementWindow(final IBuildingView building, GraveyardManagementModuleView moduleView)
    {
        super(building, Constants.MOD_ID + HUT_GRAVEYARD_RESOURCE_SUFFIX);
        this.moduleView = moduleView;
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        /*
         * ScrollList with the graves.
         */
        final ScrollingList graveList = findPaneOfTypeByID(LIST_GRAVES, ScrollingList.class);
        graveList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return moduleView.getGraves().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final BlockPos grave = moduleView.getGraves().get(index);
                @NotNull final String distance = Integer.toString((int) Math.sqrt(BlockPosUtil.getDistanceSquared(grave, buildingView.getPosition())));
                final String direction = BlockPosUtil.calcDirection(buildingView.getPosition(), grave);
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

        /*
         * ScrollList with the resting citizen.
         */
        final ScrollingList ripList = findPaneOfTypeByID(LIST_CITIZEN, ScrollingList.class);
        ripList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return moduleView.getRestingCitizen().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final String citizenName = moduleView.getRestingCitizen().get(index);
                rowPane.findPaneOfTypeByID(TAG_CITIZEN_NAME, Text.class).setText(citizenName);
            }
        });
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        moduleView.cleanGraves();
    }
}
