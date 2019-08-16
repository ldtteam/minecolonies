package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.util.BlockPosUtil;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBarracks;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Window for the barracks building.
 */
public class WindowBarracksBuilding extends AbstractWindowBuilding<BuildingBarracks.View>
{
    /**
     * Id of the positions list.
     */
    private static final String LIST_POSITIONS = "positions";

    /**
     * Id of the position label.
     */
    private static final String LABEL_POS = "pos";
    /**
     * Id of the position label.
     */
    private static final String LABEL_CURRENNT = "current";

    /**
     * Suffix for the window.
     */
    private static final String HOME_BUILDING_RESOURCE_SUFFIX = ":gui/windowhutbarracks.xml";

    /**
     * Required building level to see the barbarian spawnpoints in the GUI.
     */
    private static final int BUILDING_LEVEL_FOR_LIST = 3;

    /**
     * Distance when the barbs spawned quite close.
     */
    private static final long QUITE_CLOSE = 50;

    /**
     * Distance when the barbs spawned quite far.
     */
    private static final long QUITE_FAR = 100;

    /**
     * List of the positions.
     */
    private final ScrollingList positionsList;

    /**
     * Colony View of the colony.
     */
    private final IColonyView iColonyView;

    /**
     * Creates the Window object.
     *
     * @param building View of the home building.
     */
    public WindowBarracksBuilding(final BuildingBarracks.View building)
    {
        super(building, Constants.MOD_ID + HOME_BUILDING_RESOURCE_SUFFIX);
        iColonyView = building.getColony();
        positionsList = findPaneOfTypeByID(LIST_POSITIONS, ScrollingList.class);
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
        return "com.minecolonies.coremod.gui.workerHuts.buildBarracks";
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        if (building.getBuildingLevel() >= BUILDING_LEVEL_FOR_LIST)
        {
            final List<BlockPos> spawnPoints = iColonyView.getLastSpawnPoints();
            if(iColonyView.isRaiding())
            {
                findPaneOfTypeByID(LABEL_CURRENNT, Label.class).setLabelText(mountDistanceString(spawnPoints.get(spawnPoints.size()-1)));
            }
            positionsList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return spawnPoints.size() - (iColonyView.isRaiding() ? 1 : 0);
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {
                    final BlockPos pos = spawnPoints.get(index);
                    if(!(iColonyView.isRaiding() && index == spawnPoints.size()-1))
                    {
                        rowPane.findPaneOfTypeByID(LABEL_POS, Label.class).setLabelText((index + 1) + ": " + mountDistanceString(pos));
                    }
                }
            });
        }
    }

    /**
     * Mount the distance string for the barracks and the position.
     * @param pos the position.
     * @return the nice human readable string.
     */
    private String mountDistanceString(final BlockPos pos)
    {
        final long distance = BlockPosUtil.getDistance2D(pos, building.getPosition());
        final String distanceDesc;
        if (distance < QUITE_CLOSE)
        {
            distanceDesc = LanguageHandler.format(QUITE_CLOSE_DESC);
        }
        else if (distance < QUITE_FAR)
        {
            distanceDesc = LanguageHandler.format(QUITE_FAR_DESC);
        }
        else
        {
            distanceDesc = LanguageHandler.format(REALLY_FAR_DESC);
        }
        final String directionDest = BlockPosUtil.calcDirection(building.getPosition(), pos);
        return distanceDesc + " " + directionDest;
    }
}
