package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.Utils;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.colony.buildings.moduleviews.ColonyConnectionModuleView;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.WindowConstants.RESOURCE_ICON;

public class ConnectionModuleWindow extends AbstractModuleWindow
{
    /**
     * Special buttons
     */
    private static final String REQUEST_ALLY = "requestally";
    private static final String START_FEUD = "startfeud";
    private static final String SET_NEUTRAL = "setneutral";
    private static final String TELEPORT      = "tleport";
    private static final String LIST_COLONIES = "colonylist";

    /**
     * The matching module view to the window.
     */
    private final ColonyConnectionModuleView moduleView;


    /**
     * Scrollinglist of the guard towers.
     */
    private ScrollingList colonyList;

    /**
     * Constructor for the minimum stock window view.
     *
     * @param building class extending
     * @param moduleView the module view.
     */
    public ConnectionModuleWindow(final String res, final IBuildingView building, final ColonyConnectionModuleView moduleView)
    {
        super(building, res);
        this.moduleView = moduleView;

        colonyList = findPaneOfTypeByID(LIST_COLONIES, ScrollingList.class);

        registerButton(TELEPORT, this::teleportToColony);
        registerButton(REQUEST_ALLY, this::requestAlly);
        registerButton(START_FEUD, this::startFeud);
        registerButton(SET_NEUTRAL, this::setNeutral);

        //Neutral: [Set Ally] [Start Feud]
        //Ally: [Set Neutral]
        //Feud: [Set Neutral]
    }

    private void setNeutral(@NotNull final Button button)
    {
        final int row = colonyList.getListElementIndexByPane(button);

    }

    private void startFeud(@NotNull final Button button)
    {
        final int row = colonyList.getListElementIndexByPane(button);

    }

    private void requestAlly(@NotNull final Button button)
    {
        final int row = colonyList.getListElementIndexByPane(button);

    }

    private void teleportToColony(@NotNull final Button button)
    {
        final int row = colonyList.getListElementIndexByPane(button);

    }

    /**
     * Updates the colony list.
     */
    private void updateResourceList()
    {
        colonyList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return moduleView.getColony().getConnectionManager().getConnectedColonies().size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ItemStorage resource = allItems.get(index);
                final Text resourceLabel = rowPane.findPaneOfTypeByID("ressourceStackName", Text.class);
                final String name = resource.getItemStack().getHoverName().getString();
                resourceLabel.setText(Component.literal(name.substring(0, Math.min(17, name.length()))));
                final Text qtys = rowPane.findPaneOfTypeByID("quantities", Text.class);
                if (!Screen.hasShiftDown())
                {
                    qtys.setText(Component.literal(Utils.format(resource.getAmount())));
                }
                else
                {
                    qtys.setText(Component.literal(Integer.toString(resource.getAmount())));
                }
                final Item imagesrc = resource.getItemStack().getItem();
                final ItemStack image = new ItemStack(imagesrc, 1);
                image.setTag(resource.getItemStack().getTag());
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(image);
            }
        });
    }
}
