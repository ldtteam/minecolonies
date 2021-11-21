package com.minecolonies.coremod.client.gui.townhall;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.controls.TextField;
import com.ldtteam.blockout.views.DropDownList;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.SwitchView;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.IPermissions;
import com.minecolonies.api.colony.permissions.Player;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.network.messages.PermissionsMessage;
import com.minecolonies.coremod.network.messages.server.colony.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the town hall.
 */
public class WindowPermissionsPage extends AbstractWindowTownHall
{
    /**
     * List of added users.
     */
    @NotNull
    private final List<Player> users = new ArrayList<>();

    /**
     * The ScrollingList of the users.
     */
    private ScrollingList userList;

    /**
     * The ScrollingList of the users.
     */
    private ScrollingList actionsList;

    /**
     * The ScrollingList of the users.
     */
    private ScrollingList freeBlocksList;

    /**
     * The ScrollingList of all rank buttons
     */
    private final ScrollingList rankButtonList;

    /**
     * A list of ranks (excluding owner)
     */
    private final List<Rank> rankList = new LinkedList<>();
    private final List<Rank> allRankList = new LinkedList<>();

    /**
     * The currently selected rank to edit or delete
     */
    private Rank actionsRank;

    /**
     * A filtered list of actions
     */
    private List<Action> actions = new ArrayList<>();

    /**
     * A list of available rank types
     */
    private Map<Integer, String> rankTypes = new HashMap<>();

    /**
     * Constructor for the town hall window.
     *
     * @param building {@link BuildingTownHall.View}.
     */
    public WindowPermissionsPage(final BuildingTownHall.View building)
    {
        super(building, "layoutpermissions.xml");
        // ToDo: remove these actions in 1.17
        for (Action action : Action.values())
        {
            if (action != Action.CAN_DEMOTE && action != Action.CAN_PROMOTE && action != Action.SEND_MESSAGES)
            {
                actions.add(action);
            }
        }

        rankTypes.put(0, RANKTYPE_COLONY_MANAGER);
        rankTypes.put(1, RANKTYPE_HOSTILE);
        rankTypes.put(2, RANKTYPE_NONE);

        actionsRank = building.getColony().getPermissions().getRankOfficer();
        findPaneOfTypeByID(BUTTON_REMOVE_RANK, Button.class).setEnabled(false);

        rankButtonList = findPaneOfTypeByID(TOWNHALL_RANK_BUTTON_LIST, ScrollingList.class);
        actionsList = findPaneOfTypeByID(TOWNHALL_RANK_LIST, ScrollingList.class);

        updateUsers();

        registerButton(BUTTON_ADD_PLAYER, this::addPlayerCLicked);
        registerButton(BUTTON_REMOVE_PLAYER, this::removePlayerClicked);

        registerButton(BUTTON_TRIGGER, this::trigger);
        registerButton(BUTTON_ADD_BLOCK, this::addBlock);
        registerButton(BUTTON_REMOVE_BLOCK, this::removeBlock);
        registerButton(BUTTON_ADD_RANK, this::addRank);
        registerButton(TOWNHALL_RANK_BUTTON, this::onRankButtonClicked);
        registerButton(BUTTON_REMOVE_RANK, this::onRemoveRankButtonClicked);
        registerButton(TOWNHALL_PERM_MODE_TOGGLE, this::togglePermMode);
        registerButton(TOWNHALL_BUTTON_SUBSCRIBER, this::setSubscriber);
    }

    /**
     * Toggle the subscriber flag on client
     * Send message to change it on server
     * @param button the button clicked
     */
    private void setSubscriber(Button button)
    {
        Network.getNetwork().sendToServer(new PermissionsMessage.SetSubscriber(building.getColony(), actionsRank, !actionsRank.isSubscriber()));
        actionsRank.setSubscriber(!actionsRank.isSubscriber());
        button.setText(LanguageHandler.format(actionsRank.isSubscriber() ? COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON : COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF));
    }

    /**
     * Send message to the server to change the rank type
     * @param dropdown the index of the type
     */
    private void changeRankMode(DropDownList dropdown)
    {
        Network.getNetwork().sendToServer(new PermissionsMessage.EditRankType(building.getColony(), actionsRank, dropdown.getSelectedIndex()));
    }

    /**
     * Switch the view on the rank view (between permissions and settings)
     * @param button the button clicked
     */
    private void togglePermMode(Button button)
    {
        SwitchView permSwitch = findPaneOfTypeByID(TOWNHALL_PERM_MANAGEMENT, SwitchView.class);
        permSwitch.setView(permSwitch.getCurrentView() != null && permSwitch.getCurrentView().getID().equals(TOWNHALL_PERM_LIST) ? TOWNHALL_PERM_SETTINGS : TOWNHALL_PERM_LIST);
        if (permSwitch.getCurrentView().getID().equals(TOWNHALL_PERM_SETTINGS))
        {
            DropDownList dropdown = findPaneOfTypeByID(TOWNHALL_RANK_TYPE_PICKER, DropDownList.class);
            dropdown.setDataProvider(new DropDownList.DataProvider() {
                @Override
                public int getElementCount()
                {
                    return rankTypes.size();
                }

                @Override
                public String getLabel(final int i)
                {
                    return LanguageHandler.format(rankTypes.get(i));
                }
            });
            dropdown.setHandler(this::changeRankMode);
            dropdown.setSelectedIndex(actionsRank.isColonyManager() ? 0 : (actionsRank.isHostile() ? 1 : 2));
            findPaneOfTypeByID(TOWNHALL_BUTTON_SUBSCRIBER, Button.class).setText(LanguageHandler.format(actionsRank.isSubscriber() ? COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON : COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF));
        }
    }

    /**
     * Read the text input with the name of the rank to be added
     * If the chosen name is valid, send a message to the server, hide the error label and empty the input
     * else show the error label
     */
    private void addRank()
    {
        final Text label = findPaneOfTypeByID(TOWNHALL_ADD_RANK_ERROR, Text.class);
        final TextField input = findPaneOfTypeByID(INPUT_ADDRANK_NAME, TextField.class);
        if (isValidRankname(input.getText()))
        {
            Network.getNetwork().sendToServer(new PermissionsMessage.AddRank(building.getColony(), input.getText()));
            input.setText("");
            label.hide();
        }
        else
        {
            label.show();
        }
    }

    /**
     * Validates whether the given name is a valid rank name
     * If name is empty or already in use for a rank within this colony, it is invalid
     * @param name the name
     * @return true if name is valid
     */
    private boolean isValidRankname(String name)
    {
        if (name.equals(""))
        {
            return false;
        }
        for (Rank rank : rankList)
        {
            if (rank.getName().equals(name))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Send message to server to remove the currently selected rank
     * Remove rank from view
     * Set currently selected rank to officer and disable button
     * @param button the clicked button
     */
    private void onRemoveRankButtonClicked(Button button)
    {
        if (actionsRank != null)
        {
            Network.getNetwork().sendToServer(new PermissionsMessage.RemoveRank(building.getColony(), actionsRank));
            building.getColony().getPermissions().removeRank(actionsRank);
            actionsRank = building.getColony().getPermissions().getRankOfficer();
            button.setEnabled(false);
        }
    }

    /**
     * Clears and resets all users.
     */
    private void updateUsers()
    {
        users.clear();
        users.addAll(building.getColony().getPlayers().values());
        users.sort(Comparator.comparing(Player::getRank, Rank::compareTo));
    }

    /**
     * Executed when <code>WindowTownHall</code> is opened. Does tasks like setting buttons.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();

        fillUserList();
        fillFreeBlockList();
        fillRanks();
        fillPermissionList();

        PlayerEntity player = Minecraft.getInstance().player;
        Text label = findPaneOfTypeByID(TOWNHALL_PERMISSION_ERROR, Text.class);
        Button button = findPaneOfTypeByID(BUTTON_ADD_PLAYER, Button.class);
        if (building.getColony().getPermissions().hasPermission(player, Action.EDIT_PERMISSIONS))
        {
            label.hide();
            button.setEnabled(true);
        }
        else
        {
            label.show();
            button.setEnabled(false);
        }
    }

    /**
     * Clears and resets all ranks
     */
    private void updateRanks()
    {
        rankList.clear();
        for (final Rank rank : building.getColony().getPermissions().getRanks().values())
        {
            if (!rank.equals(building.getColony().getPermissions().getRankOwner()))
            {
                rankList.add(rank);
            }
        }
        allRankList.clear();
        allRankList.addAll(building.getColony().getPermissions().getRanks().values());
    }

    /**
     * Fill the rank button list in the GUI
     */
    private void fillRanks()
    {
        rankButtonList.setDataProvider(new ScrollingList.DataProvider() {
            @Override
            public int getElementCount()
            {
                return allRankList.size();
            }

            @Override
            public void updateElement(final int i, final Pane pane)
            {
                final Rank rank = allRankList.get(i);
                final Button button = pane.findPaneOfTypeByID(TOWNHALL_RANK_BUTTON, Button.class);
                button.setText(rank.getName());
                button.setEnabled(!rank.equals(actionsRank));
                pane.findPaneOfTypeByID("rankId", Text.class).setText(Integer.toString(rank.getId()));
            }
        });
    }

    /**
     * Change to currently selected rank to the one belonging to the clicked button
     * @param button the clicked button
     */
    private void onRankButtonClicked(@NotNull final Button button)
    {
        final int rankId = rankButtonList.getListElementIndexByPane(button);
        final Rank rank = allRankList.get(rankId);
        if (rank != null)
        {
            actionsRank = rank;
            button.setEnabled(false);
            findPaneOfTypeByID(BUTTON_REMOVE_RANK, Button.class).setEnabled(!actionsRank.isInitial());
            findPaneOfTypeByID(TOWNHALL_PERM_MANAGEMENT, SwitchView.class).setView(TOWNHALL_PERM_LIST);
        }
    }

    private void removeBlock(final Button button)
    {
        final int row = freeBlocksList.getListElementIndexByPane(button);
        if (row >= 0)
        {
            @NotNull final List<Block> freeBlocks = building.getColony().getFreeBlocks();
            @NotNull final List<BlockPos> freePositions = building.getColony().getFreePositions();

            if (row < freeBlocks.size())
            {
                Network.getNetwork().sendToServer(
                  new ChangeFreeToInteractBlockMessage(building.getColony(), freeBlocks.get(row), ChangeFreeToInteractBlockMessage.MessageType.REMOVE_BLOCK));
                building.getColony().removeFreeBlock(freeBlocks.get(row));
            }
            else if (row < freeBlocks.size() + freePositions.size())
            {
                final BlockPos freePos = freePositions.get(row - freeBlocks.size());
                Network.getNetwork().sendToServer(
                  new ChangeFreeToInteractBlockMessage(building.getColony(), freePos, ChangeFreeToInteractBlockMessage.MessageType.REMOVE_BLOCK));
                building.getColony().removeFreePosition(freePos);
            }
            fillFreeBlockList();
        }
    }

    /**
     * Fills the free blocks list in the GUI.
     */
    private void fillFreeBlockList()
    {
        @NotNull final List<Block> freeBlocks = building.getColony().getFreeBlocks();
        @NotNull final List<BlockPos> freePositions = building.getColony().getFreePositions();

        freeBlocksList = findPaneOfTypeByID(LIST_FREE_BLOCKS, ScrollingList.class);
        freeBlocksList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return freeBlocks.size() + freePositions.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                if (index < freeBlocks.size())
                {
                    rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(freeBlocks.get(index).getRegistryName().toString());
                }
                else
                {
                    final BlockPos pos = freePositions.get(index - freeBlocks.size());
                    rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(pos.getX() + " " + pos.getY() + " " + pos.getZ());
                }
            }
        });
    }

    /**
     * Called when the "addBlock" button has been triggered. Tries to add the content of the input field as block or position to the colony.
     */
    private void addBlock()
    {
        final TextField input = findPaneOfTypeByID(INPUT_BLOCK_NAME, TextField.class);
        final String inputText = input.getText();

        try
        {
            final Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(inputText));

            if (block != null)
            {
                building.getColony().addFreeBlock(block);
                Network.getNetwork().sendToServer(new ChangeFreeToInteractBlockMessage(building.getColony(), block, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK));
            }
        }
        catch (final ResourceLocationException e)
        {
            // Do nothing.
        }

        final BlockPos pos = BlockPosUtil.getBlockPosOfString(inputText);

        if (pos != null)
        {
            Network.getNetwork().sendToServer(new ChangeFreeToInteractBlockMessage(building.getColony(), pos, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK));
            building.getColony().addFreePosition(pos);
        }

        fillFreeBlockList();
        input.setText("");
    }

    /**
     * Called when the permission button has been triggered.
     *
     * @param button the triggered button.
     */
    private void trigger(@NotNull final Button button)
    {
        final int index = actionsList.getListElementIndexByPane(button);
        final Action action = actions.get(index);

        final IPermissions permissions = building.getColony().getPermissions();
        final PlayerEntity playerEntity = Minecraft.getInstance().player;
        if (!permissions.hasPermission(playerEntity, Action.EDIT_PERMISSIONS) || !permissions.canAlterPermission(permissions.getRank(playerEntity), actionsRank, action))
        {
            return;
        }

        final boolean trigger = LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON).equals(button.getTextAsString());
        Network.getNetwork().sendToServer(new PermissionsMessage.Permission(building.getColony(), PermissionsMessage.MessageType.TOGGLE_PERMISSION, actionsRank, action));
        building.getColony().getPermissions().togglePermission(permissions.getRank(playerEntity), actionsRank, action);

        if (trigger)
        {
            button.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF));
        }
        else
        {
            button.setText(LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON));
        }
    }

    /**
     * Fills the permission list in the GUI.
     */
    private void fillPermissionList()
    {
        actionsList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return actions.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final Action action = actions.get(index);
                final String name = LanguageHandler.format(KEY_TO_PERMISSIONS + action.toString().toLowerCase(Locale.US));

                if (name.contains(KEY_TO_PERMISSIONS))
                {
                    Log.getLogger().warn("Didn't work for:" + name);
                    return;
                }

                rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(name);
                final boolean isTriggered = building.getColony().getPermissions().hasPermission(actionsRank, action);
                rowPane.findPaneOfTypeByID("trigger", Button.class)
                  .setText(isTriggered ? LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON)
                              : LanguageHandler.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF));
                rowPane.findPaneOfTypeByID("index", Text.class).setText(Integer.toString(index));
            }
        });
    }

    /**
     * Fills the userList in the GUI.
     */
    private void fillUserList()
    {
        userList = findPaneOfTypeByID(LIST_USERS, ScrollingList.class);
        userList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return users.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final Player player = users.get(index);
                Rank rank = player.getRank();
                rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(player.getName());
                DropDownList dropdown = rowPane.findPaneOfTypeByID(TOWNHALL_RANK_PICKER, DropDownList.class);
                if (rank.getId() == building.getColony().getPermissions().OWNER_RANK_ID)
                {
                    rowPane.findPaneOfTypeByID(BUTTON_REMOVE_PLAYER, Button.class).setEnabled(false);
                    rowPane.findPaneOfTypeByID("rank", Text.class).setText(rank.getName());
                    dropdown.setEnabled(false);
                }
                else
                {
                    dropdown.setDataProvider(new DropDownList.DataProvider() {
                        @Override
                        public int getElementCount()
                        {
                            return rankList.size();
                        }

                        @Override
                        public String getLabel(final int i)
                        {
                            Rank rank = rankList.get(i);
                            return rank.getName();
                        }
                    });
                    dropdown.setSelectedIndex(rankList.indexOf(rank));
                    dropdown.setHandler(WindowPermissionsPage.this::onRankSelected);
                }
            }
        });
    }

    /**
     * When the selected index in the rank dropdown is updated,
     * check if the rank is different to the current one
     * if so, change the rank client side and send a message to the server
     * @param dropdown the rank dropdown
     */
    private void onRankSelected(final DropDownList dropdown)
    {
        final int index = dropdown.getSelectedIndex();
        final Player player = users.get(userList.getListElementIndexByPane(dropdown));
        final Rank rank = rankList.get(index);
        if (rank != player.getRank())
        {
            player.setRank(rank);
            Network.getNetwork().sendToServer(new PermissionsMessage.ChangePlayerRank(building.getColony(), player.getID(), rank));
        }
    }



    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateUsers();
        updateRanks();
    }

    /**
     * Action performed when add player button is clicked.
     */
    private void addPlayerCLicked()
    {
        final TextField input = findPaneOfTypeByID(INPUT_ADDPLAYER_NAME, TextField.class);
        Network.getNetwork().sendToServer(new PermissionsMessage.AddPlayer(building.getColony(), input.getText()));
        input.setText("");
    }

    /**
     * Action performed when remove player button is clicked.
     *
     * @param button Button that holds the user clicked on.
     */
    private void removePlayerClicked(final Button button)
    {
        final int row = userList.getListElementIndexByPane(button);
        if (row >= 0 && row < users.size())
        {
            final Player user = users.get(row);
            if (user.getRank().getId() != IPermissions.OWNER_RANK_ID)
            {
                users.remove(user);
                userList.removeChild(button.getParent());
                Network.getNetwork().sendToServer(new PermissionsMessage.RemovePlayer(building.getColony(), user.getID()));
            }
        }
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_PERMISSIONS;
    }
}
