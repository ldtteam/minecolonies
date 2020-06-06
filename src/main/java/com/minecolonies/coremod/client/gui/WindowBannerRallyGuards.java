package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableList;
import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.colony.guardtype.GuardType;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.research.ILocalResearch;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.RemoveFromRallyingListMessage;
import com.minecolonies.coremod.network.messages.server.ToggleBannerRallyGuardsMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.lumberjack.LumberjackScepterMessage;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
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
     * Id of the requester label.
     */
    private ItemStack banner = null;

    private PlayerEntity playerIn = null;

    private List<Pair<ILocation, IGuardBuilding>> guardTowers = new LinkedList<>();

    /**
     * Constructor of the rally banner window
     *
     * @param banner  The banner to be displayed
     * @param playerIn The player that opens the window
     */
    public WindowBannerRallyGuards(final ItemStack banner, final PlayerEntity playerIn)
    {
        super(Constants.MOD_ID + BUILD_TOOL_RESOURCE_SUFFIX);
        this.banner = banner;
        this.playerIn = playerIn;

        final ImmutableList<ILocation> guardTowerPositions = getGuardTowerLocations(banner);
        for (final ILocation guardTowerLocation : guardTowerPositions)
        {
            final World world = playerIn.getEntityWorld().getServer().getWorld(DimensionType.getById(guardTowerLocation.getDimension()));
            final TileEntity entity = world.getTileEntity(guardTowerLocation.getInDimensionLocation());
            // Note that getGuardBuildingFromTileEntity will perform the null-check for entity
            final IGuardBuilding building = getGuardBuildingFromTileEntity(entity);
            guardTowers.add(new Pair<>(guardTowerLocation, building));
        }

        registerButton(BUTTON_REMOVE, this::removeClicked);
        registerButton(BUTTON_RALLY, this::rallyClicked);
    }

    /**
     * Called when the window is opened.
     * Sets up the buttons for either hut mode or decoration mode.
     */
    @Override
    public void onOpened()
    {
        guardTowerList = findPaneOfTypeByID(LIST_GUARDTOWERS, ScrollingList.class);

        if (isActive(banner))
        {
            findPaneOfTypeByID(BUTTON_RALLY, ButtonImage.class).setLabel(LanguageHandler.format(COM_MINECOLONIES_BANNER_RALLY_GUARDS_GUI_DISMISS));
        }
        else
        {
            findPaneOfTypeByID(BUTTON_RALLY, ButtonImage.class).setLabel(LanguageHandler.format(COM_MINECOLONIES_BANNER_RALLY_GUARDS_GUI_RALLY, guardTowers.size()));
        }

        guardTowerList.setDataProvider(() -> guardTowers.size(), (index, rowPane) ->
        {
            if (index < 0 || index >= guardTowers.size())
            {
                return;
            }

            final Pair<ILocation, IGuardBuilding> guardTower = guardTowers.get(index);

            final ItemIcon exampleStackDisplay = rowPane.findPaneOfTypeByID(ICON_GUARD, ItemIcon.class);


            if (guardTower.getSecond() != null)
            {
                final GuardType guardType = guardTower.getSecond().getGuardType();
                if (ModGuardTypes.knight.equals(guardType))
                {
                    exampleStackDisplay.setItem(new ItemStack(Items.IRON_SWORD));
                }
                else if (ModGuardTypes.ranger.equals(guardType))
                {
                    exampleStackDisplay.setItem(new ItemStack(Items.BOW));
                }

                rowPane.findPaneOfTypeByID(LABEL_GUARDTYPE, Label.class)
                  .setLabelText(LanguageHandler.format(guardTower.getSecond().getGuardType().getJobTranslationKey()) + ": " + guardTower.getSecond().getAssignedCitizen().size());

                rowPane.findPaneOfTypeByID(LABEL_POSITION, Label.class)
                  .setLabelText(guardTower.getFirst().toString());
            }
            else
            {
                exampleStackDisplay.setItem(new ItemStack(Items.COOKIE));

                rowPane.findPaneOfTypeByID(LABEL_GUARDTYPE, Label.class)
                  .setLabelText(LanguageHandler.format(COM_MINECOLONIES_BANNER_RALLY_GUARDS_GUI_TOWERMISSING));
                rowPane.findPaneOfTypeByID(LABEL_GUARDTYPE, Label.class).setColor(Color.rgbaToInt(255, 0, 0, 1));
                rowPane.findPaneOfTypeByID(LABEL_POSITION, Label.class)
                  .setLabelText(guardTower.getFirst().toString());
            }
        });
    }

    private void removeClicked(@NotNull final Button button)
    {
        final int row = guardTowerList.getListElementIndexByPane(button);

        if (guardTowers.size() > row && row >= 0)
        {
            Network.getNetwork().sendToServer(new RemoveFromRallyingListMessage(banner, guardTowers.get(row).getFirst()));
        }
    }

    private void rallyClicked(@NotNull final Button button)
    {
        Network.getNetwork().sendToServer(new ToggleBannerRallyGuardsMessage(banner));
        this.close();
    }
}
