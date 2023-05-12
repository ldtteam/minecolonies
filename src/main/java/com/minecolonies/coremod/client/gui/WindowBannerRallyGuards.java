package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockui.Color;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.network.messages.server.RemoveFromRallyingListMessage;
import com.minecolonies.coremod.network.messages.server.ToggleBannerRallyGuardsMessage;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.items.ItemBannerRallyGuards.*;

/**
 * ClipBoard window.
 */
public class WindowBannerRallyGuards extends AbstractWindowSkeleton
{
    /**
     * Resource suffix.
     */
    private static final String BUILD_TOOL_RESOURCE_SUFFIX = ":gui/windowbannerrallyguards.xml";

    /**
     * Requests list id.
     */
    private static final String LIST_GUARDTOWERS = "guardtowers";

    /**
     * Requestst stack id.
     */
    private static final String ICON_GUARD = "guardicon";

    /**
     * Id of the resource add button.
     */
    private static final String BUTTON_REMOVE = "remove";

    /**
     * Id of the resource add button.
     */
    private static final String BUTTON_RALLY = "rally";

    /**
     * Id of the detail button.
     */
    private static final String LABEL_GUARDTYPE = "guardtype";

    /**
     * Id of the short detail label.
     */
    private static final String LABEL_POSITION = "position";

    /**
     * Scrollinglist of the guard towers.
     */
    private ScrollingList guardTowerList;

    /**
     * Banner for which the window is opened
     */
    private ItemStack banner = null;

    /**
     * Constructor of the rally banner window
     *
     * @param banner The banner to be displayed
     */
    public WindowBannerRallyGuards(final ItemStack banner)
    {
        super(Constants.MOD_ID + BUILD_TOOL_RESOURCE_SUFFIX);
        this.banner = banner;

        registerButton(BUTTON_REMOVE, this::removeClicked);
        registerButton(BUTTON_RALLY, this::rallyClicked);
    }

    @Override
    public void onOpened()
    {
        guardTowerList = findPaneOfTypeByID(LIST_GUARDTOWERS, ScrollingList.class);

        if (isActive(banner))
        {
            findPaneOfTypeByID(BUTTON_RALLY, ButtonImage.class).setText(Component.translatable(COM_MINECOLONIES_BANNER_RALLY_GUARDS_GUI_DISMISS));
        }
        else
        {
            findPaneOfTypeByID(BUTTON_RALLY, ButtonImage.class).setText(Component.translatable(COM_MINECOLONIES_BANNER_RALLY_GUARDS_GUI_RALLY));
        }

        guardTowerList.setDataProvider(() -> getGuardTowerViews(banner).size(), (index, rowPane) ->
        {
            final List<Pair<ILocation, AbstractBuildingGuards.View>> guardTowers = getGuardTowerViews(banner);

            if (index < 0 || index >= guardTowers.size())
            {
                return;
            }

            final Pair<ILocation, AbstractBuildingGuards.View> guardTower = guardTowers.get(index);

            //todo we probably want to display the exact mix.
            final ItemIcon exampleStackDisplay = rowPane.findPaneOfTypeByID(ICON_GUARD, ItemIcon.class);
            final AbstractBuildingGuards.View guardTowerView = guardTower.getSecond();

            if (guardTowerView != null)
            {
                exampleStackDisplay.setItem(new ItemStack(Items.IRON_SWORD));
                rowPane.findPaneOfTypeByID(LABEL_GUARDTYPE, Text.class).setText(Component.translatable(ModGuardTypes.knight.get().getJobTranslationKey())
                  .append("|")
                  .append(Component.translatable(ModGuardTypes.ranger.get().getJobTranslationKey()))
                  .append(": ")
                  .append(String.valueOf(guardTowerView.getGuards().size())));
                rowPane.findPaneOfTypeByID(LABEL_POSITION, Text.class).setText(Component.literal(guardTower.getFirst().toString()));
            }
            else
            {
                exampleStackDisplay.setItem(new ItemStack(Items.COOKIE));

                rowPane.findPaneOfTypeByID(LABEL_GUARDTYPE, Text.class)
                  .setText(Component.translatable(COM_MINECOLONIES_BANNER_RALLY_GUARDS_GUI_TOWERMISSING));
                rowPane.findPaneOfTypeByID(LABEL_GUARDTYPE, Text.class).setColors(Color.rgbaToInt(255, 0, 0, 1));
                rowPane.findPaneOfTypeByID(LABEL_POSITION, Text.class).setText(Component.literal(guardTower.getFirst().toString()));
            }
        });
    }

    /**
     * Handles removal of towers from the rallying list.
     *
     * @param button The button used to remove the tower.
     */
    private void removeClicked(@NotNull final Button button)
    {
        final int row = guardTowerList.getListElementIndexByPane(button);

        final List<Pair<ILocation, AbstractBuildingGuards.View>> guardTowers = getGuardTowerViews(banner);
        if (guardTowers.size() > row && row >= 0)
        {
            final ILocation locationToRemove = guardTowers.get(row).getFirst();
            // Server side removal
            Network.getNetwork().sendToServer(new RemoveFromRallyingListMessage(banner, locationToRemove));

            // Client side removal
            removeGuardTowerAtLocation(banner, locationToRemove);
        }
    }

    /**
     * Handles toggle of banner.
     *
     * @param button The button used to toggle the banner.
     */
    private void rallyClicked(@NotNull final Button button)
    {
        Network.getNetwork().sendToServer(new ToggleBannerRallyGuardsMessage(banner));
        this.close();
    }
}
