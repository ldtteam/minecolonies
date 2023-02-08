package com.minecolonies.coremod.client.gui.huts;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractWindowModuleBuilding;
import com.minecolonies.coremod.client.gui.WindowsBarracksSpies;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBarracks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * BOWindow for the barracks building.
 */
public class WindowBarracksBuilding extends AbstractWindowModuleBuilding<BuildingBarracks.View>
{
    /**
     * Id of the positions list.
     */
    private static final String LIST_POSITIONS = "positions";

    /**
     * Id of the position label.
     */
    private static final String LABEL_POS      = "pos";
    /**
     * Id of the position label.
     */
    private static final String LABEL_CURRENNT = "current";

    /**
     * Suffix for the window.
     */
    private static final String HOME_BUILDING_RESOURCE_SUFFIX = ":gui/windowhutbarracks.xml";

    /**
     * Spies button id.
     */
    private static final String SPIES_BUTTON = "hireSpies";

    /**
     * The spies button icon id
     */
    private static final String SPIES_BUTTON_ICON = "hireSpiesIcon";

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
    private final IColonyView view;

    /**
     * Creates the BOWindow object.
     *
     * @param building View of the home building.
     */
    public WindowBarracksBuilding(final BuildingBarracks.View building)
    {
        super(building, Constants.MOD_ID + HOME_BUILDING_RESOURCE_SUFFIX);
        view = building.getColony();
        positionsList = findPaneOfTypeByID(LIST_POSITIONS, ScrollingList.class);
        findPaneOfTypeByID(SPIES_BUTTON_ICON, ItemIcon.class).setItem(Items.GOLD_INGOT.getDefaultInstance());
        registerButton(SPIES_BUTTON, this::hireSpiesClicked);

        if (building.getBuildingLevel() < 3)
        {
            findPaneOfTypeByID(SPIES_BUTTON, ButtonImage.class).setVisible(false);
            findPaneOfTypeByID(SPIES_BUTTON_ICON, ItemIcon.class).setVisible(false);
        }
    }

    /**
     * Open the spies gui when the button is clicked.
     *
     * @param button the clicked button.
     */
    private void hireSpiesClicked(final Button button)
    {
        @NotNull final WindowsBarracksSpies window = new WindowsBarracksSpies(this.building, this.building.getID());
        window.open();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        if (building.getBuildingLevel() >= BUILDING_LEVEL_FOR_LIST)
        {
            final List<BlockPos> spawnPoints = view.getLastSpawnPoints();
            if (spawnPoints.size() == 0)
            {
                return;
            }

            if (view.isRaiding())
            {
                findPaneOfTypeByID(LABEL_CURRENNT, Text.class).setText(mountDistanceString(spawnPoints.get(spawnPoints.size() - 1)));
            }
            positionsList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return spawnPoints.size() - (view.isRaiding() ? 1 : 0);
                }

                @Override
                public void updateElement(final int index, @NotNull final Pane rowPane)
                {
                    final BlockPos pos = spawnPoints.get(index);
                    if (!(view.isRaiding() && index == spawnPoints.size() - 1))
                    {
                        rowPane.findPaneOfTypeByID(LABEL_POS, Text.class).setText(Component.literal((index + 1) + ": " + mountDistanceString(pos)));
                    }
                }
            });
        }
    }

    /**
     * Mount the distance string for the barracks and the position.
     *
     * @param pos the position.
     * @return the component containing the nice human-readable string.
     */
    private Component mountDistanceString(final BlockPos pos)
    {
        final long distance = BlockPosUtil.getDistance2D(pos, building.getPosition());
        final String distanceDesc;
        if (distance < QUITE_CLOSE)
        {
            distanceDesc = QUITE_CLOSE_DESC;
        }
        else if (distance < QUITE_FAR)
        {
            distanceDesc = QUITE_FAR_DESC;
        }
        else
        {
            distanceDesc = REALLY_FAR_DESC;
        }
        final Component directionDest = BlockPosUtil.calcDirection(building.getPosition(), pos);
        return Component.translatable(distanceDesc)
                 .append(" ")
                 .append(directionDest);
    }
}
