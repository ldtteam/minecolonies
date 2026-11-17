package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.Color;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.guardtype.registry.IGuardTypeRegistry;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.core.network.messages.server.RemoveFromRallyingListMessage;
import com.minecolonies.core.network.messages.server.ToggleBannerRallyGuardsMessage;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.core.items.ItemBannerRallyGuards.*;

/**
 * ClipBoard window.
 */
public class WindowBannerRallyGuards extends AbstractWindowSkeleton
{
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
        super(new ResourceLocation(Constants.MOD_ID, "gui/windowbannerrallyguards.xml"));
        this.banner = banner;

        registerButton(BUTTON_REMOVE, this::removeClicked);
        registerButton(BUTTON_RALLY, this::rallyClicked);
    }

    /**
     * Helper to assemble the guard information associated with the given military building.
     *
     * @param militaryView the military building view.
     * @return the assembled detail for the associated view.
     */
    private MutableComponent assembleGuardDetails(final AbstractBuildingGuards.View militaryView)
    {
        final Map<GuardType, Integer> assignedGuardTypes = new HashMap<>();

        for (final int citizenId : militaryView.getGuards())
        {
            final ICitizenDataView citizen = militaryView.getColony().getCitizen(citizenId);
            if (citizen == null || citizen.getJobView() == null)
            {
                continue;
            }

            // Ensure that only military units are listed on the banner.
            for (final GuardType guardType : IGuardTypeRegistry.getInstance().getValues())
            {
                if (guardType.getJobEntry().get().equals(citizen.getJobView().getEntry()))
                {
                    assignedGuardTypes.merge(guardType, 1, Integer::sum);
                    break;
                }
            }
        }

        if (assignedGuardTypes.isEmpty())
        {
            return Component.translatable(COM_MINECOLONIES_BANNER_RALLY_GUARDS_GUI_UNSTAFFED);
        }

        final MutableComponent guardDetails = Component.empty();
        for (final Map.Entry<GuardType, Integer> assignedGuardType : assignedGuardTypes.entrySet())
        {
            if (!guardDetails.getSiblings().isEmpty())
            {
                guardDetails.append(", ");
            }

            guardDetails.append(Component.translatable(assignedGuardType.getKey().getJobTranslationKey()));
            guardDetails.append(": " + assignedGuardType.getValue());
        }

        return guardDetails;
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

        guardTowerList.setDataProvider(() -> getGuardTowerViews(banner, mc.level).size(), (index, rowPane) ->
        {
            final List<Pair<ILocation, AbstractBuildingGuards.View>> guardTowers = getGuardTowerViews(banner, mc.level);

            if (index < 0 || index >= guardTowers.size())
            {
                return;
            }

            final Pair<ILocation, AbstractBuildingGuards.View> guardTower = guardTowers.get(index);

            final ItemIcon exampleStackDisplay = rowPane.findPaneOfTypeByID(ICON_GUARD, ItemIcon.class);
            final Text guardTypeLabel = rowPane.findPaneOfTypeByID(LABEL_GUARDTYPE, Text.class);
            final AbstractBuildingGuards.View guardTowerView = guardTower.getSecond();

            if (guardTowerView != null)
            {
                exampleStackDisplay.setItem(new ItemStack(Items.IRON_SWORD));
                final MutableComponent guardDetails = assembleGuardDetails(guardTowerView);
                guardTypeLabel.setText(guardDetails);
                guardTypeLabel.setColors(Color.rgbaToInt(0, 0, 0, 1));
                PaneBuilders.singleLineTooltip(guardDetails, guardTypeLabel);
                rowPane.findPaneOfTypeByID(LABEL_POSITION, Text.class).setText(Component.literal(guardTower.getFirst().toString()));
            }
            else
            {
                exampleStackDisplay.setItem(new ItemStack(Items.COOKIE));

                guardTypeLabel.setText(Component.translatable(COM_MINECOLONIES_BANNER_RALLY_GUARDS_GUI_TOWERMISSING));
                guardTypeLabel.setColors(Color.rgbaToInt(255, 0, 0, 1));
                guardTypeLabel.setHoverPane(null);
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

        final List<Pair<ILocation, AbstractBuildingGuards.View>> guardTowers = getGuardTowerViews(banner, mc.level);
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
