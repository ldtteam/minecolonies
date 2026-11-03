package com.minecolonies.core.client.gui.map;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.network.messages.client.colony.ColonyListMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * UI to display colony prestige ranking server-wide.
 */
public class WindowColonyPrestigeRanking extends AbstractWindowSkeleton
{
    /**
     * Constructor for prestige window.
     * @param atTownHall if player is seeing that at townhall or in town map item.
     * @param building the townhall building view.
     */
    public WindowColonyPrestigeRanking(final boolean atTownHall, final IBuildingView building)
    {
        super(new ResourceLocation(Constants.MOD_ID, "gui/map/windowcolonyprestigeranking.xml"));

        if (atTownHall)
        {
            registerButton(BUTTON_EXIT, () -> building.openGui(false));
        }
        else
        {
            registerButton(BUTTON_EXIT, this::close);
        }
        registerButton(BUTTON_MAP, () -> new WindowColonyMap(atTownHall, building).open());
        registerButton(BUTTON_MAP_ICON, () -> new WindowColonyMap(atTownHall, building).open());

        final List<ColonyListMessage.ColonyInfo> colonies = new ArrayList<>(WindowColonyMap.colonies);
        colonies.sort(Comparator.comparing(ColonyListMessage.ColonyInfo::getPrestige));

        final ScrollingList buildingList = findPaneOfTypeByID("colonies", ScrollingList.class);
        buildingList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return colonies.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                rowPane.findPaneOfTypeByID("desc", Text.class).setText(Component.literal(colonies.get(index).getName() + ": " + colonies.get(index).getPrestige()));
            }
        });
    }
}
