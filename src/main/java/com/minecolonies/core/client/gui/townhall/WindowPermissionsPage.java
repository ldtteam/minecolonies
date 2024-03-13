package com.minecolonies.core.client.gui.townhall;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.AbstractTextBuilder;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.blockui.views.DropDownList;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.permissions.*;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.core.network.messages.PermissionsMessage;
import com.minecolonies.core.network.messages.server.colony.ChangeFreeToInteractBlockMessage;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for the town hall.
 */
public class WindowPermissionsPage extends AbstractWindowTownHall
{
    /**
     * List of added users.
     */
    @NotNull
    private final List<ColonyPlayer> users = new ArrayList<>();

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
    private final List<Rank> rankList    = new LinkedList<>();
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
     * The ScrollingList of the events.
     */
    private ScrollingList eventList;

    /**
     * Constructor for the town hall window.
     *
     * @param building {@link BuildingTownHall.View}.
     */
    public WindowPermissionsPage(final BuildingTownHall.View building)
    {
        super(building, "layoutpermissions.xml");
        actions.addAll(Arrays.asList(Action.values()));

        rankTypes.put(0, RANK_TYPE_COLONY_MANAGER);
        rankTypes.put(1, RANK_TYPE_HOSTILE);
        rankTypes.put(2, RANK_TYPE_NONE);

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
        registerButton(TOWNHALL_BUTTON_SUBSCRIBER, this::setSubscriber);
        registerButton(BUTTON_ADD_PLAYER_OR_FAKEPLAYER, this::addPlayerToColonyClicked);

        fillEventsList();
    }

    /**
     * Action performed when remove player button is clicked.
     *
     * @param button Button that holds the user clicked on.
     */
    private void addPlayerToColonyClicked(@NotNull final Button button)
    {
        final int row = eventList.getListElementIndexByPane(button);
        if (row >= 0 && row < building.getPermissionEvents().size())
        {
            final PermissionEvent user = building.getPermissionEvents().get(row);
            new PermissionsMessage.AddPlayerOrFakePlayer(building.getColony(), user.getName(), user.getId()).sendToServer();
        }
    }

    /**
     * Toggle the subscriber flag on client
     * Send message to change it on server
     *
     * @param button the button clicked
     */
    private void setSubscriber(Button button)
    {
        new PermissionsMessage.SetSubscriber(building.getColony(), actionsRank, !actionsRank.isSubscriber()).sendToServer();
        actionsRank.setSubscriber(!actionsRank.isSubscriber());
        button.setText(Component.translatableEscape(actionsRank.isSubscriber()
                                                   ? COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON
                                                   : COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF));
    }

    /**
     * Send message to the server to change the rank type
     *
     * @param dropdown the index of the type
     */
    private void changeRankMode(DropDownList dropdown)
    {
        new PermissionsMessage.EditRankType(building.getColony(), actionsRank, dropdown.getSelectedIndex()).sendToServer();
    }

    /**
     * Read the text input with the name of the rank to be added
     * If the chosen name is valid, send a message to the server, hide the error label and empty the input
     * else show the error label
     */
    private void addRank()
    {
        final TextField input = findPaneOfTypeByID(INPUT_ADDRANK_NAME, TextField.class);
        if (isValidRankname(input.getText()))
        {
            new PermissionsMessage.AddRank(building.getColony(), input.getText()).sendToServer();
            input.setText("");
            SoundUtils.playSuccessSound(Minecraft.getInstance().player, Minecraft.getInstance().player.blockPosition());
        }
        else
        {
            SoundUtils.playErrorSound(Minecraft.getInstance().player, Minecraft.getInstance().player.blockPosition());
        }
    }

    /**
     * Validates whether the given name is a valid rank name
     * If name is empty or already in use for a rank within this colony, it is invalid
     *
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
     *
     * @param button the clicked button
     */
    private void onRemoveRankButtonClicked(Button button)
    {
        if (actionsRank != null)
        {
            new PermissionsMessage.RemoveRank(building.getColony(), actionsRank).sendToServer();
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
        users.sort(Comparator.comparing(ColonyPlayer::getRank, Rank::compareTo));
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

        LocalPlayer player = Minecraft.getInstance().player;

        final Button addPlayerButton = findPaneOfTypeByID(BUTTON_ADD_PLAYER, Button.class);
        final TextField playerNameField = findPaneOfTypeByID(INPUT_ADDPLAYER_NAME, TextField.class);
        final TextField rankNameField = findPaneOfTypeByID(INPUT_ADDRANK_NAME, TextField.class);
        final Button addRankButton = findPaneOfTypeByID(BUTTON_ADD_RANK, Button.class);


        if (building.getColony().getPermissions().hasPermission(player, Action.EDIT_PERMISSIONS))
        {
            addPlayerButton.setEnabled(true);
            playerNameField.setEnabled(true);
            rankNameField.setEnabled(true);
            addRankButton.setEnabled(true);
        }
        else
        {
            AbstractTextBuilder.TooltipBuilder hoverText = PaneBuilders.tooltipBuilder().hoverPane(playerNameField);
            hoverText.append(Component.translatableEscape("com.minecolonies.coremod.gui.townhall.player_permission_error")).paragraphBreak();
            hoverText.build();

            AbstractTextBuilder.TooltipBuilder hoverText2 = PaneBuilders.tooltipBuilder().hoverPane(rankNameField);
            hoverText2.append(Component.translatableEscape("com.minecolonies.core.gui.townhall.rank_permission_error")).paragraphBreak();
            hoverText2.build();

            rankNameField.setEnabled(false);
            addPlayerButton.setEnabled(false);
            playerNameField.setEnabled(false);
            addRankButton.setEnabled(false);
        }

        findPaneOfTypeByID(TOWNHALL_RANK_TYPE_PICKER, DropDownList.class).setSelectedIndex(actionsRank.isColonyManager() ? 0 : (actionsRank.isHostile() ? 1 : 2));
        findPaneOfTypeByID(TOWNHALL_BUTTON_SUBSCRIBER, Button.class).setText(Component.translatableEscape(actionsRank.isSubscriber()
                                                                                                      ? COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON
                                                                                                      : COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF));
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
        rankButtonList.setDataProvider(new ScrollingList.DataProvider()
        {
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
                button.setText(Component.literal(rank.getName()));
                button.setEnabled(!rank.equals(actionsRank));
                pane.findPaneOfTypeByID("rankId", Text.class).setText(Component.literal(Integer.toString(rank.getId())));
            }
        });

        DropDownList dropdown = findPaneOfTypeByID(TOWNHALL_RANK_TYPE_PICKER, DropDownList.class);
        dropdown.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return rankTypes.size();
            }

            @Override
            public MutableComponent getLabel(final int i)
            {
                return Component.translatableEscape(rankTypes.get(i));
            }
        });
        dropdown.setHandler(this::changeRankMode);
    }

    /**
     * Change to currently selected rank to the one belonging to the clicked button
     *
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

            findPaneOfTypeByID(TOWNHALL_RANK_TYPE_PICKER, DropDownList.class).setSelectedIndex(actionsRank.isColonyManager() ? 0 : (actionsRank.isHostile() ? 1 : 2));
            findPaneOfTypeByID(TOWNHALL_BUTTON_SUBSCRIBER, Button.class).setText(Component.translatableEscape(actionsRank.isSubscriber()
                                                                                                          ? COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON
                                                                                                          : COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF));
        }
    }

    private void fillEventsList()
    {
        eventList = findPaneOfTypeByID(EVENTS_LIST, ScrollingList.class);
        eventList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return building.getPermissionEvents().size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final Text nameLabel = rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class);
                final Text actionLabel = rowPane.findPaneOfTypeByID(ACTION_LABEL, Text.class);

                    final List<PermissionEvent> permissionEvents = building.getPermissionEvents();
                    Collections.reverse(permissionEvents);
                    final PermissionEvent event = permissionEvents.get(index);

                    nameLabel.setText(Component.literal(event.getName() + (event.getId() == null ? " <fake>" : "")));
                    rowPane.findPaneOfTypeByID(POS_LABEL, Text.class).setText(Component.literal(event.getPosition().getX() + " " + event.getPosition().getY() + " " + event.getPosition().getZ()));

                    rowPane.findPaneOfTypeByID(BUTTON_ADD_PLAYER_OR_FAKEPLAYER, Button.class).setVisible(event.getId() != null);

                    actionLabel.setText(Component.translatableEscape(KEY_TO_PERMISSIONS + event.getAction().toString().toLowerCase(Locale.US)));
            }
        });
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
                new ChangeFreeToInteractBlockMessage(building.getColony(), freeBlocks.get(row), ChangeFreeToInteractBlockMessage.MessageType.REMOVE_BLOCK).sendToServer();
                building.getColony().removeFreeBlock(freeBlocks.get(row));
            }
            else if (row < freeBlocks.size() + freePositions.size())
            {
                final BlockPos freePos = freePositions.get(row - freeBlocks.size());
                new ChangeFreeToInteractBlockMessage(building.getColony(), freePos, ChangeFreeToInteractBlockMessage.MessageType.REMOVE_BLOCK).sendToServer();
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
                    rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(Component.literal(BuiltInRegistries.BLOCK.getKey(freeBlocks.get(index)).toString()));
                }
                else
                {
                    final BlockPos pos = freePositions.get(index - freeBlocks.size());
                    rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(Component.literal(pos.getX() + " " + pos.getY() + " " + pos.getZ()));
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
            final Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(inputText));

            if (block != null)
            {
                building.getColony().addFreeBlock(block);
                new ChangeFreeToInteractBlockMessage(building.getColony(), block, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK).sendToServer();
            }
        }
        catch (final ResourceLocationException e)
        {
            // Do nothing.
        }

        final BlockPos pos = BlockPosUtil.getBlockPosOfString(inputText);

        if (pos != null)
        {
            new ChangeFreeToInteractBlockMessage(building.getColony(), pos, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK).sendToServer();
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
        final Player playerEntity = Minecraft.getInstance().player;
        
        String key = button.getText().getContents() instanceof TranslatableContents contents ? contents.getKey() : button.getTextAsString();

        final boolean enable = !COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON.equals(key);
        button.disable();
        if (!permissions.alterPermission(permissions.getRank(playerEntity), actionsRank, action, enable))
        {
            return;
        }
        new PermissionsMessage.Permission(building.getColony(), enable, actionsRank, action).sendToServer();

        if (!enable)
        {
            button.setText(Component.translatableEscape(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF));
        }
        else
        {
            button.setText(Component.translatableEscape(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON));
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
                final Component name =Component.translatableEscape(KEY_TO_PERMISSIONS + action.toString().toLowerCase(Locale.US));
                rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(name);

                final boolean isTriggered = building.getColony().getPermissions().hasPermission(actionsRank, action);
                final Button onOffButton = rowPane.findPaneOfTypeByID("trigger", Button.class);
                onOffButton.setText(isTriggered ? Component.translatableEscape(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_ON)
                                      : Component.translatableEscape(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_RETRIEVE_OFF));
                rowPane.findPaneOfTypeByID("index", Text.class).setText(Component.literal(Integer.toString(index)));

                if (!building.getColony().getPermissions().canAlterPermission(building.getColony().getPermissions().getRank(Minecraft.getInstance().player), actionsRank, action))
                {
                    onOffButton.disable();
                }
                else
                {
                    onOffButton.enable();
                }
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
                final ColonyPlayer player = users.get(index);
                Rank rank = player.getRank();
                rowPane.findPaneOfTypeByID(NAME_LABEL, Text.class).setText(Component.literal(player.getName()));
                DropDownList dropdown = rowPane.findPaneOfTypeByID(TOWNHALL_RANK_PICKER, DropDownList.class);
                if (rank.getId() == building.getColony().getPermissions().OWNER_RANK_ID)
                {
                    rowPane.findPaneOfTypeByID(BUTTON_REMOVE_PLAYER, Button.class).setEnabled(false);
                    rowPane.findPaneOfTypeByID("rank", Text.class).setText(Component.literal(rank.getName()));
                    dropdown.setEnabled(false);
                }
                else
                {
                    dropdown.setDataProvider(new DropDownList.DataProvider()
                    {
                        @Override
                        public int getElementCount()
                        {
                            return rankList.size();
                        }

                        @Override
                        public MutableComponent getLabel(final int i)
                        {
                            Rank rank = rankList.get(i);
                            return Component.literal(rank.getName());
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
     *
     * @param dropdown the rank dropdown
     */
    private void onRankSelected(final DropDownList dropdown)
    {
        final int index = dropdown.getSelectedIndex();
        final ColonyPlayer player = users.get(userList.getListElementIndexByPane(dropdown));
        final Rank rank = rankList.get(index);
        if (rank != player.getRank())
        {
            player.setRank(rank);
            new PermissionsMessage.ChangePlayerRank(building.getColony(), player.getID(), rank).sendToServer();
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
        new PermissionsMessage.AddPlayer(building.getColony(), input.getText()).sendToServer();
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
            final ColonyPlayer user = users.get(row);
            if (user.getRank().getId() != IPermissions.OWNER_RANK_ID)
            {
                users.remove(user);
                userList.removeChild(button.getParent());
                new PermissionsMessage.RemovePlayer(building.getColony(), user.getID()).sendToServer();
            }
        }
    }

    @Override
    protected String getWindowId()
    {
        return BUTTON_PERMISSIONS;
    }
}
