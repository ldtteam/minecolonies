package com.minecolonies.coremod.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.google.common.primitives.Ints;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.managers.ICitizenManager;
import com.mojang.authlib.GameProfile;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.FakePlayer;

public enum ActionArgumentType
{
    PLAYER("player-expression", 0),
    COLONY("colony-id", 0),
    CITIZEN("citizen-id or full-name", 2),
    COORDINATE_X("x-coordinate", 0),
    COORDINATE_Y("y-coordinate", 0),
    COORDINATE_Z("z-coordinate", 0),
    BOOLEAN("boolean", 0),
    INTEGER("integer", 0)
    ;

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

    private List<String> getOnlinePlayerNames(@NotNull final MinecraftServer server)
    {
        final String[] onlinePlayerNames = server.getOnlinePlayerNames();
        return Arrays.asList(onlinePlayerNames);
    }

    private List<String> getColonyIdStrings()
    {
        final List<Colony> colonyList = ColonyManager.getColonies();
        final List<String> colonyIdList = new ArrayList<>(colonyList.size());
        for (final Colony colony : colonyList)
        {
            colonyIdList.add(String.valueOf(colony.getID()));
        }
        return colonyIdList;
    }

    private List<String> getCitizenNames()
    {
        final List<Colony> colonyList = ColonyManager.getColonies();
        final List<String> citizenNameList = new ArrayList<>();
        for (final Colony colony : colonyList)
        {
            final List<CitizenData> citizenDataList = colony.getCitizenManager().getCitizens();
            for (final CitizenData citizenData : citizenDataList)
            {
                citizenNameList.add(citizenData.getName());
            }
        }
        return citizenNameList;
    }

    public List<String> getTabCompletions(@NotNull final MinecraftServer server,
            @Nullable final BlockPos pos,
            final String potentialArgumentValue)
    {
        switch (this)
        {
            case INTEGER:
                return Collections.emptyList();
            case BOOLEAN:
                return Arrays.asList(new String[] {"true", "false"});
            case COORDINATE_X:
            case COORDINATE_Y:
            case COORDINATE_Z:
                return getCoordinateTabCompletions(pos, potentialArgumentValue);
            case PLAYER:
                final List<String> playerNameStrings = getOnlinePlayerNames(server);
                return playerNameStrings.stream().filter(k -> k.startsWith(potentialArgumentValue)).collect(Collectors.toList());
            case COLONY:
                return getColonyTabCompletions(potentialArgumentValue);
            case CITIZEN:
                return getCitizenTabCompletions(potentialArgumentValue);
            default:
                throw new IllegalStateException("Unimplemented ActionArgumentType tab completion");
        }
    }

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

    private List<String> getColonyTabCompletions(final String potentialArgumentValue)
    {
        // TODO: use the colony we are in as the default tab completion.
        final List<String> colonyNumberStrings = getColonyIdStrings();
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

    private List<String> getCitizenTabCompletions(final String potentialArgumentValue)
    {
        // TODO: see if we can figure out what citizen we are looking at as the default tab completion.
        final List<String> citizenNameStrings = getCitizenNames();
        final List<String> citizenNumberStrings = getColonyIdStrings();
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
            else
            {
                final Set<String> firstNameSet = new HashSet<>();
                for (final String citizenName : citizenNameStrings)
                {
                    final String[] citizenNameParts = citizenName.split(" ");
                    firstNameSet.add(citizenNameParts[0]);
                }
                final List<String> firstNameMatches = firstNameSet.stream().filter(k -> k.startsWith(potentialArgumentValue)).collect(Collectors.toList());
                if (!firstNameMatches.isEmpty())
                {
                    return firstNameMatches;
                }
            }
        }
        else
        {
            if (1 == currentWordIndex)
            {
                final Set<String> middleNameSet = new HashSet<>();
                for (final String citizenName : citizenNameStrings)
                {
                    final String[] citizenNameParts = citizenName.split(" ");
                    if (potentiaCitizenNameParts[0].equals(citizenNameParts[0]))
                    {
                        middleNameSet.add(citizenNameParts[1]);
                    }
                }
                return middleNameSet.stream().filter(k -> k.startsWith(potentiaCitizenNameParts[1])).collect(Collectors.toList());
            }
            else
            {
                final Set<String> lastNameSet = new HashSet<>();
                for (final String citizenName : citizenNameStrings)
                {
                    final String[] citizenNameParts = citizenName.split(" ");
                    if ((potentiaCitizenNameParts[0].equals(citizenNameParts[0])) && (potentiaCitizenNameParts[1].equals(citizenNameParts[1])))
                    {
                        lastNameSet.add(citizenNameParts[2]);
                    }
                }
                return lastNameSet.stream().filter(k -> k.startsWith(potentiaCitizenNameParts[2])).collect(Collectors.toList());
           }
        }
        return Collections.emptyList();
    }

    public Object parse(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @Nullable final BlockPos pos,
            @NotNull final List<ActionMenuHolder> parsedHolders,
            final String potentialArgumentValue)
    {
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
                return Boolean.parseBoolean(potentialArgumentValue);
            case PLAYER:
                return parsePlayerValue(server, potentialArgumentValue);
            case COLONY:
                return parseColonyValue(sender, potentialArgumentValue);
            case CITIZEN:
                return parseCitizenDataValue(parsedHolders, potentialArgumentValue);
            default:
                throw new IllegalStateException("Unimplemented ActionArgumentType parsing");
        }
    }

    private EntityPlayerMP parsePlayerValue(@NotNull final MinecraftServer server, final String potentialArgumentValue)
    {
        final List<String> playerNameStrings = getOnlinePlayerNames(server);
        if (playerNameStrings.contains(potentialArgumentValue))
        {
            return server.getPlayerList().getPlayerByUsername(potentialArgumentValue);
        }
        else
        {
            if ("[abandoned]".equals(potentialArgumentValue))
            {
                return new FakePlayer(server.getWorld(0), new GameProfile(UUID.randomUUID(), "[abandoned]"));
            }
            return null;
        }
    }

    private Colony parseColonyValue(@NotNull final ICommandSender sender, final String potentialArgumentValue)
    {
        final List<String> colonyNumberStrings = getColonyIdStrings();
        final Integer result = Ints.tryParse(potentialArgumentValue);
        if (null != result)
        {
            int colonyNumber = result.intValue();
            if (sender instanceof EntityPlayer)
            {
                if (colonyNumber == -1)
                {
                    final IColony icolony = ColonyManager.getIColonyByOwner(sender.getEntityWorld(), (EntityPlayer) sender);
                    if (icolony != null)
                    {
                        colonyNumber = icolony.getID();
                    }
                }
            }
            if (colonyNumberStrings.contains(String.valueOf(colonyNumber)))
            {
                return ColonyManager.getColony(colonyNumber);
            }
            return null;
        }
        else
        {
            return null;
        }
    }

    private CitizenData parseCitizenDataValue(@NotNull final List<ActionMenuHolder> parsedHolders, final String potentialArgumentValue)
    {
        final ArrayList<ActionMenuHolder> reversedParsedHolderList = new ArrayList<>(parsedHolders);
        Collections.reverse(reversedParsedHolderList);
        Colony colony = null;
        for (final ActionMenuHolder actionMenuHolder : reversedParsedHolderList)
        {
            final ActionArgument actionArgument = actionMenuHolder.getActionArgument();
            if (actionArgument.getType() == COLONY)
            {
                colony = (Colony) actionMenuHolder.getValue();
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
            final List<Colony> colonyList = ColonyManager.getColonies();
            for (final Colony someColony : colonyList)
            {
                final ICitizenManager citizenManager = someColony.getCitizenManager();
                final CitizenData citizen = findCitizenForCitizenManager(citizenManager, potentialArgumentValue);
                if (null != citizen)
                {
                    return citizen;
                }
            }
            return null;
        }
    }

    private CitizenData findCitizenForCitizenManager(@NotNull final ICitizenManager citizenManager, final String potentialArgumentValue)
    {
        final List<CitizenData> citizenDataList = citizenManager.getCitizens();
        for (final CitizenData citizenData : citizenDataList)
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

