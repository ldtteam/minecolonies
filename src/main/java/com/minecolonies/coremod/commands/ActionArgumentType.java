package com.minecolonies.coremod.commands;

import com.google.common.primitives.Ints;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ICitizenData;
import com.minecolonies.coremod.colony.managers.interfaces.ICitizenManager;
import com.minecolonies.coremod.commands.AbstractCommandParser.ModuleContext;
import com.minecolonies.coremod.commands.CommandEntryPointNew.MineColonyDataProvider;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public enum ActionArgumentType
{
    ONLINE_PLAYER("online-player-expression", 0),
    PLAYER("player-expression", 0),
    COLONY("colony-id", 0),
    CITIZEN("citizen-id or full-name", 9),
    COORDINATE_X("x-coordinate", 0),
    COORDINATE_Y("y-coordinate", 0),
    COORDINATE_Z("z-coordinate", 0),
    BOOLEAN("boolean", 0),
    INTEGER("integer", 0),
    STRING("string", 0),
    DOUBLE("double", 0)
    ;

    private static final String ABANDONED_FAKE_PLAYER_NAME = "[abandoned]";

    public enum Is
    {
        REQUIRED,
        OPTIONAL
    }

    @Nonnull private final String usageValue;
    private final int allowedSpaceCount;

    ActionArgumentType(@Nonnull final String usageValue, final int allowedSpaceCount)
    {
        this.usageValue = usageValue;
        this.allowedSpaceCount = allowedSpaceCount;
    }

    public String getUsageValue()
    {
        return usageValue;
    }

    public int allowedSpaceCount()
    {
        return allowedSpaceCount;
    }

    @NotNull
    private static List<String> getOnlinePlayerNames(@NotNull final MinecraftServer server)
    {
        final String[] onlinePlayerNames = server.getOnlinePlayerNames();
        return Arrays.asList(onlinePlayerNames);
    }

    @NotNull
    private static List<String> getAllPlayerNames(@NotNull final MinecraftServer server)
    {
        final PlayerList playerList = server.getPlayerList();
        final List<EntityPlayerMP> allPlayersList = playerList.getPlayers();
        final List<String> playerNames = new ArrayList<>(allPlayersList.size());
        for (final EntityPlayerMP entityPlayerMP : allPlayersList)
        {
            final String playerName = entityPlayerMP.getName();
            if (!playerNames.contains(playerName))
            {
                playerNames.add(playerName);
            }
        }
        return playerNames;
    }

    @NotNull
    private static List<String> getColonyIdStrings(@NotNull final MineColonyDataProvider mineColonyDataProvider)
    {
        final List<IColony> colonyList = mineColonyDataProvider.getColonies();
        final List<String> colonyIdList = new ArrayList<>(colonyList.size());
        for (final IColony colony : colonyList)
        {
            colonyIdList.add(String.valueOf(colony.getID()));
        }
        return colonyIdList;
    }

    private static List<String> getCitizenNames(@NotNull final MineColonyDataProvider mineColonyDataProvider, @Nullable final IColony colonyToUse)
    {
        final List<IColony> colonyList;
        if (null != colonyToUse)
        {
            colonyList = Collections.singletonList(colonyToUse);
        }
        else
        {
            colonyList = mineColonyDataProvider.getColonies();
        }
        final List<String> citizenNameList = new ArrayList<>();
        for (final IColony colony : colonyList)
        {
            final List<ICitizenData> citizenDataList = colony.getCitizenManager().getCitizens();
            for (final ICitizenData citizenData : citizenDataList)
            {
                citizenNameList.add(citizenData.getName());
            }
        }
        return citizenNameList;
    }

    @NotNull
    private static List<String> getCitizenIds(@NotNull final MineColonyDataProvider mineColonyDataProvider, @Nullable final IColony colonyToUse)
    {
        final List<IColony> colonyList;
        if (null != colonyToUse)
        {
            colonyList = Collections.singletonList(colonyToUse);
        }
        else
        {
            colonyList = mineColonyDataProvider.getColonies();
        }
        final List<String> citizenNameList = new ArrayList<>();
        for (final IColony colony : colonyList)
        {
            final List<ICitizenData> citizenDataList = colony.getCitizenManager().getCitizens();
            for (final ICitizenData citizenData : citizenDataList)
            {
                citizenNameList.add(String.valueOf(citizenData.getId()));
            }
        }
        return citizenNameList;
    }

    @NotNull
    public List<String> getTabCompletions(@NotNull final MinecraftServer server,
            @Nullable final BlockPos pos, @NotNull final ModuleContext moduleContext,
            @NotNull final ActionMenuState actionMenuState, final String potentialArgumentValue)
    {
        final MineColonyDataProvider mineColonyDataProvider = moduleContext.get(MineColonyDataProvider.class);

        switch (this)
        {
            case INTEGER:
            case STRING:
                return Collections.emptyList();
            case BOOLEAN:
                return getBooleanTabCompletions(potentialArgumentValue);
            case COORDINATE_X:
            case COORDINATE_Y:
            case COORDINATE_Z:
                return getCoordinateTabCompletions(pos, potentialArgumentValue);
            case ONLINE_PLAYER:
                final List<String> onlinePlayerNameStrings = getOnlinePlayerNames(server);
                return onlinePlayerNameStrings.stream().filter(k -> k.startsWith(potentialArgumentValue)).collect(Collectors.toList());
            case PLAYER:
                final List<String> allPlayerNameStrings = getAllPlayerNames(server);
                return allPlayerNameStrings.stream().filter(k -> k.startsWith(potentialArgumentValue)).collect(Collectors.toList());
            case COLONY:
                return getColonyTabCompletions(mineColonyDataProvider, potentialArgumentValue);
            case CITIZEN:
                return getCitizenTabCompletions(mineColonyDataProvider, actionMenuState, potentialArgumentValue);
            default:
                throw new IllegalStateException("Unimplemented ActionArgumentType tab completion");
        }
    }

    @NotNull
    private List<String> getCoordinateTabCompletions(@Nullable final BlockPos pos, final String potentialArgumentValue)
    {
        if (null == pos)
        {
            return Collections.emptyList();
        }
        if (potentialArgumentValue.isEmpty())
        {
            switch (this)
            {
                case COORDINATE_X:
                    return Collections.singletonList(String.valueOf(pos.getX()));
                case COORDINATE_Y:
                    return Collections.singletonList(String.valueOf(pos.getY()));
                case COORDINATE_Z:
                    return Collections.singletonList(String.valueOf(pos.getZ()));
                default:
                    // We will never reach here.
                    break;
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    @NotNull
    private List<String> getColonyTabCompletions(@NotNull final MineColonyDataProvider mineColonyDataProvider,
            final String potentialArgumentValue)
    {
        // TODO: use the colony we are in as the default tab completion.
        final List<String> colonyNumberStrings = getColonyIdStrings(mineColonyDataProvider);
        if (potentialArgumentValue.isEmpty())
        {
            return colonyNumberStrings;
        }
        final Integer result = Ints.tryParse(potentialArgumentValue);
        if (null != result)
        {
            return colonyNumberStrings.stream().filter(k -> k.startsWith(potentialArgumentValue)).collect(Collectors.toList());
        }
        else
        {
            return Collections.emptyList();
        }
    }

    @NotNull
    private List<String> getCitizenTabCompletions(@NotNull final MineColonyDataProvider mineColonyDataProvider,
            @NotNull final ActionMenuState actionMenuState, final String potentialArgumentValue)
    {
        // TODO: see if we can figure out what citizen we are looking at as the default tab completion.
        @Nullable Colony colony = null;
        // Try to find a valid colony value.
        // TODO: doesn't check subarguments but should only check arguments for parents of this argument.
        // TODO: Also no guarantee that we've grabbed the right colony argument if the command has more than one.
        final ActionMenu actionMenu = actionMenuState.getActionMenu();
        for (final ActionArgument actionArgument : actionMenu.getActionArgumentList())
        {
            if (ActionArgumentType.COLONY == actionArgument.getType())
            {
                colony = (Colony) actionMenuState.getValue(actionArgument);
                if (null != colony)
                {
                    break;
                }
            }
        }

        final List<String> citizenNameStrings = getCitizenNames(mineColonyDataProvider, colony);
        final List<String> citizenNumberStrings = getCitizenIds(mineColonyDataProvider, colony);
        final String[] potentiaCitizenNameParts = potentialArgumentValue.split(" ", -1);
        final int currentWordIndex = potentiaCitizenNameParts.length - 1;
        if (potentialArgumentValue.isEmpty())
        {
            final Set<String> nameSet = new HashSet<>();
            for (final String citizenName : citizenNameStrings)
            {
                final String[] citizenNameParts = citizenName.split(" ");
                nameSet.add(citizenNameParts[currentWordIndex]);
            }

            final List<String> result = new ArrayList<>(nameSet);
            if (0 == currentWordIndex)
            {
                result.addAll(citizenNumberStrings);
            }
            return result;
        }
        if (0 == currentWordIndex)
        {
            final Integer result = Ints.tryParse(potentialArgumentValue);
            if (null != result)
            {
                return citizenNumberStrings.stream().filter(k -> k.startsWith(potentialArgumentValue)).collect(Collectors.toList());
            }
        }

        final Set<String> nameAtIndexStringSet = new HashSet<>();
        for (final String citizenName : citizenNameStrings)
        {
            final String[] citizenNameParts = citizenName.split(" ");
            if (citizenNameParts.length > currentWordIndex)
            {
                int i = 0;
                while (i < currentWordIndex)
                {
                    if (!potentiaCitizenNameParts[i].equals(citizenNameParts[i]))
                    {
                        break;
                    }
                    ++i;
                }
                if (i == currentWordIndex)
                {
                    nameAtIndexStringSet.add(citizenNameParts[currentWordIndex]);
                }
            }
        }
        if (!nameAtIndexStringSet.isEmpty())
        {
            return nameAtIndexStringSet.stream().filter(k -> k.startsWith(potentiaCitizenNameParts[currentWordIndex])).collect(Collectors.toList());
        }
        else
        {
            return Collections.emptyList();
        }
    }

    @Nullable
    public Object parse(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @Nullable final BlockPos pos, 
            @NotNull final ModuleContext moduleContext,
            @NotNull final ActionMenuState actionMenuState,
            final String potentialArgumentValue)
    {
        final MineColonyDataProvider mineColonyDataProvider = moduleContext.get(MineColonyDataProvider.class);

        // TODO: selector support, such as used by CommandKill to find player
        // Entity entity = <net.minecraft.command.CommandBase>.getEntity(server, sender, args[0]);

        switch (this)
        {
            case INTEGER:
            case COORDINATE_X:
            case COORDINATE_Y:
            case COORDINATE_Z:
                return Ints.tryParse(potentialArgumentValue);
            case BOOLEAN:
                return parseBoolean(potentialArgumentValue);
            case ONLINE_PLAYER:
                return parseOnlinePlayerValue(server, potentialArgumentValue);
            case PLAYER:
                return parseAnyPlayerValue(server, potentialArgumentValue);
            case COLONY:
                return parseColonyValue(sender, mineColonyDataProvider, potentialArgumentValue);
            case CITIZEN:
                return parseCitizenDataValue(mineColonyDataProvider, actionMenuState, potentialArgumentValue);
            case STRING:
                return potentialArgumentValue.isEmpty() ? null : potentialArgumentValue;
            case DOUBLE:
                return potentialArgumentValue.isEmpty() ? 0D : Double.parseDouble(potentialArgumentValue);
            default:
                throw new IllegalStateException("Unimplemented ActionArgumentType parsing");
        }
    }

    /*
     * Suppressing Sonar Rule squid:S2447
     * This rule complains about returning null for a Boolean method.
     * But in this case the rule does not apply because
     * We are returning null to indicate that no boolean value could be parsed.
     */
    @SuppressWarnings({"squid:S2447"})
    @Nullable
    private static Boolean parseBoolean(final String potentialArgumentValue)
    {
        if ("true".equalsIgnoreCase(potentialArgumentValue))
        {
            return Boolean.TRUE;
        }
        if ("t".equalsIgnoreCase(potentialArgumentValue))
        {
            return Boolean.TRUE;
        }
        if ("yes".equalsIgnoreCase(potentialArgumentValue))
        {
            return Boolean.TRUE;
        }
        if ("y".equalsIgnoreCase(potentialArgumentValue))
        {
            return Boolean.TRUE;
        }
        if ("1".equalsIgnoreCase(potentialArgumentValue))
        {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(potentialArgumentValue))
        {
            return Boolean.FALSE;
        }
        if ("f".equalsIgnoreCase(potentialArgumentValue))
        {
            return Boolean.FALSE;
        }
        if ("no".equalsIgnoreCase(potentialArgumentValue))
        {
            return Boolean.FALSE;
        }
        if ("n".equalsIgnoreCase(potentialArgumentValue))
        {
            return Boolean.FALSE;
        }
        if ("0".equalsIgnoreCase(potentialArgumentValue))
        {
            return Boolean.FALSE;
        }
        return null;
    }

    private static List<String> getBooleanTabCompletions(final String potentialArgumentValue)
    {
        if (potentialArgumentValue.isEmpty())
        {
            return Arrays.asList(new String[] {"true", "false" });
        }
        if (("true".startsWith(potentialArgumentValue))
            || ("yes".startsWith(potentialArgumentValue))
            || ("1".startsWith(potentialArgumentValue)))
        {
            return Collections.singletonList("true");
        }
        if (("false".startsWith(potentialArgumentValue))
            || ("no".startsWith(potentialArgumentValue))
            || ("0".startsWith(potentialArgumentValue)))
        {
            return Collections.singletonList("false");
        }
        
        return Collections.emptyList();
    }

    @Nullable
    private EntityPlayerMP parseOnlinePlayerValue(@NotNull final MinecraftServer server, final String potentialArgumentValue)
    {
        final List<String> playerNameStrings = getOnlinePlayerNames(server);
        if (playerNameStrings.contains(potentialArgumentValue))
        {
            return server.getPlayerList().getPlayerByUsername(potentialArgumentValue);
        }
        else
        {
            if (ABANDONED_FAKE_PLAYER_NAME.equals(potentialArgumentValue))
            {
                return new FakePlayer(server.getWorld(0), new GameProfile(UUID.randomUUID(), ABANDONED_FAKE_PLAYER_NAME));
            }
            return null;
        }
    }

    @Nullable
    private EntityPlayerMP parseAnyPlayerValue(@NotNull final MinecraftServer server, final String potentialArgumentValue)
    {
        final List<String> playerNameStrings = getAllPlayerNames(server);
        if (playerNameStrings.contains(potentialArgumentValue))
        {
            return server.getPlayerList().getPlayerByUsername(potentialArgumentValue);
        }
        else
        {
            if (ABANDONED_FAKE_PLAYER_NAME.equals(potentialArgumentValue))
            {
                return new FakePlayer(server.getWorld(0), new GameProfile(UUID.randomUUID(), ABANDONED_FAKE_PLAYER_NAME));
            }
            return null;
        }
    }

    @Nullable
    private IColony parseColonyValue(@NotNull final ICommandSender sender, @NotNull final MineColonyDataProvider mineColonyDataProvider, final String potentialArgumentValue)
    {
        final List<String> colonyNumberStrings = getColonyIdStrings(mineColonyDataProvider);
        final Integer result = Ints.tryParse(potentialArgumentValue);
        if (null != result)
        {
            int colonyNumber = result.intValue();
            if (sender instanceof EntityPlayer && colonyNumber == -1)
            {
                final IColony icolony = mineColonyDataProvider.getIColonyByOwner(sender.getEntityWorld(), (EntityPlayer) sender);
                if (icolony != null)
                {
                    colonyNumber = icolony.getID();
                }
            }
            if (colonyNumberStrings.contains(String.valueOf(colonyNumber)))
            {
                return mineColonyDataProvider.getColony(colonyNumber, sender.getEntityWorld().provider.getDimension());
            }
            return null;
        }
        else
        {
            return null;
        }
    }

    @Nullable
    private ICitizenData parseCitizenDataValue(@NotNull final MineColonyDataProvider mineColonyDataProvider,
            @NotNull final ActionMenuState actionMenuState, final String potentialArgumentValue)
    {
        // Try to find a non-null colony value set
        IColony colony = null;
        final List<ActionArgument> actionArgumentList = actionMenuState.getActionMenu().getActionArgumentList();
        for (final ActionArgument actionArgument : actionArgumentList)
        {
            if ((actionArgument.getType() == COLONY) && actionMenuState.isValueSet(actionArgument))
            {
                colony = actionMenuState.getColonyForArgument(actionArgument.getName());
                break;
            }
        }

        if (null != colony)
        {
            final ICitizenManager citizenManager = colony.getCitizenManager(); 
            return findCitizenForCitizenManager(citizenManager, potentialArgumentValue);
        }
        else
        {
            // no colony specified for citizen
            final List<IColony> colonyList = mineColonyDataProvider.getColonies();
            for (final IColony someColony : colonyList)
            {
                final ICitizenManager citizenManager = someColony.getCitizenManager();
                final ICitizenData citizen = findCitizenForCitizenManager(citizenManager, potentialArgumentValue);
                if (null != citizen)
                {
                    return citizen;
                }
            }
            return null;
        }
    }

    @Nullable
    private ICitizenData findCitizenForCitizenManager(@NotNull final ICitizenManager citizenManager, final String potentialArgumentValue)
    {
        final List<ICitizenData> citizenDataList = citizenManager.getCitizens();
        for (final ICitizenData citizenData : citizenDataList)
        {
            if (citizenData.getName().equalsIgnoreCase(potentialArgumentValue))
            {
                return citizenData;
            }
        }

        final Integer result = Ints.tryParse(potentialArgumentValue);
        if (null != result)
        {
            final int citizenId = result.intValue();
            return citizenManager.getCitizen(citizenId);
        }
        else
        {
            return null;
        }
    }
}

