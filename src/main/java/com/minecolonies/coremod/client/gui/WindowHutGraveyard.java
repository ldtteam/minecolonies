package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.SwitchView;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFarmer;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingGraveyard;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Window for the home building.
 */
public class WindowHutGraveyard extends AbstractWindowBuilding<BuildingGraveyard.View>
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
     * Id of the the worker label inside the GUI.
     */
    private static final String TAG_WORKER = "worker";

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
     * Constructor for the window of the farmer.
     *
     * @param building {@link BuildingFarmer.View}.
     */
    public WindowHutGraveyard(final BuildingGraveyard.View building)
    {
        super(building, Constants.MOD_ID + HUT_GRAVEYARD_RESOURCE_SUFFIX);
    }

     /**
     * Retrieve levels from the building to display in GUI.
     */
    private void pullLevelsFromHut()
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
                if (entity instanceof ScarecrowTileEntity) //TODO TG change from scarecrow
                {

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
            pullLevelsFromHut();
            window.findPaneOfTypeByID(PAGE_GRAVES, ScrollingList.class).refreshElementPanes();
        }
    }
}
