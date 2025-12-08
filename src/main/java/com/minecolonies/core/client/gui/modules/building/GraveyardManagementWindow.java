package com.minecolonies.core.client.gui.modules.building;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.colony.buildings.moduleviews.GraveyardManagementModuleView;
import com.minecolonies.core.tileentities.TileEntityGrave;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

/**
 * BOWindow for the Graveyard building.
 */
public class GraveyardManagementWindow extends AbstractModuleWindow<GraveyardManagementModuleView>
{
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
    private final ClientLevel world = Minecraft.getInstance().level;

    /**
     * Constructor for the window of the graveyard.
     *
     * @param moduleView {@link GraveyardManagementModuleView}.
     */
    public GraveyardManagementWindow(final GraveyardManagementModuleView moduleView)
    {
        super(moduleView, new ResourceLocation(Constants.MOD_ID, "gui/layouthuts/layoutgraveyard.xml"));
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
                final Component direction = BlockPosUtil.calcDirection(buildingView.getPosition(), grave).getLongText();
                final BlockEntity entity = world.getBlockEntity(grave);
                if (entity instanceof TileEntityGrave)
                {
                    rowPane.findPaneOfTypeByID(TAG_NAME, Text.class).setText(Component.literal("Grave of " +
                            ((((TileEntityGrave) entity).getGraveData() != null) ?
                             ((TileEntityGrave) entity).getGraveData().getCitizenName() :
                             "Unknown Citizen")));
                    rowPane.findPaneOfTypeByID(TAG_DISTANCE, Text.class).setText(Component.literal(distance + "m"));
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
                rowPane.findPaneOfTypeByID(TAG_CITIZEN_NAME, Text.class).setText(Component.literal(citizenName));
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
