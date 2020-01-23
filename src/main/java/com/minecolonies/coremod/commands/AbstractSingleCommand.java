package com.minecolonies.coremod.commands;

import com.google.common.primitives.Ints;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.configuration.Configurations;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

/**
 * A command that has children. Is a single one-word command.
 */
public abstract class AbstractSingleCommand implements ISubCommand
{
    public static final String NOT_PERMITTED = "You are not allowed to do that!";
    private final String[] parents;

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public AbstractSingleCommand(@NotNull final String... parents)
    {
        this.parents = parents.clone();
    }

    /**
     * Get the ith argument (An Integer).
     *
     * @param i    the argument from the list you want.
     * @param args the list of arguments.
     * @param def  the default value.
     * @return the argument.
     */
    public static int getIthArgument(final String[] args, final int i, final int def)
    {
        if (args.length <= i)
        {
            return def;
        }

        try
        {
            return Integer.parseInt(args[i]);
        }
        catch (final NumberFormatException e)
        {
            return def;
        }
    }
    
    /**
     * Get the colony id from the ith argument (two integers separated by a pipe char)
     * @param args the list of arguments
     * @param i the index of the argument you want
     * @param def the default value
     * @return the colony id
     */
    public static int getColonyIdFromArg(final String[] args, final int i, final int def)
    {
    	if (args.length < i + 1)
    	{
    		return def;
    	}
    	else
    	{
    		String[] split = args[i].split(Pattern.quote("|"));
    		if (split.length == 2)
    		{
    			Integer result1 = Ints.tryParse(split[1]);
    			if (null != result1) return result1.intValue();
    		}
    		else if (split.length == 1)
    		{
    			Integer result = Ints.tryParse(split[0]);
    			if (null != result) return result.intValue();
    		}
    	}
    	return def;
    }
    
    public static int getDimensionIdFromArg(final String[] args, final int i, final int def)
    {
    	if (args.length > i)
    	{
    		String[] split = args[i].split(Pattern.quote("|"));
    		if (split.length == 2)
    		{
    			Integer result0 = Ints.tryParse(split[0]);
    			if (null != result0) return result0.intValue();
    		}
    	}
    	return def;
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        final StringBuilder sb = new StringBuilder().append('/');
        for (final String parent : parents)
        {
            sb.append(parent).append(' ');
        }
        return sb.toString();
    }

    /**
     * Will check the config file to see if players are allowed to use the command that is sent here.
     * and will verify that they are of correct rank to do so.
     *
     * @param player     the players/senders name.
     * @param theCommand which command to check if the player can use it.
     * @param colonyId   the id of the colony.
     * @return boolean.
     */

    public boolean canPlayerUseCommand(final EntityPlayer player, final Commands theCommand, final int colonyId)
    {
        if (isPlayerOpped(player))
        {
            return true;
        }

        final IColony chkColony = IColonyManager.getInstance().getColonyByWorld(colonyId, FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0));
        if (chkColony == null)
        {
            return false;
        }
        return canCommandSenderUseCommand(theCommand)
                 && canRankUseCommand(chkColony, player);
    }

    /**
     * Will check to see if play is Opped for the given command name.
     *
     * @param sender to check the player using the command.
     * @return boolean
     */
    @NotNull
    public static boolean isPlayerOpped(@NotNull final ICommandSender sender)
    {
        if (sender instanceof EntityPlayer)
        {
            return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList()
                     .canSendCommands(((EntityPlayer) sender).getGameProfile());
        }
        return true;
    }

    /**
     * Will check the config file to see if players are allowed to use the command that is sent here.
     *
     * @param theCommand which command to check if the player can use it
     * @return boolean
     */
    public static boolean canCommandSenderUseCommand(final Commands theCommand)
    {
        switch (theCommand)
        {
            case HOMETP:
                return Configurations.gameplay.canPlayerUseHomeTPCommand;
            case COLONYTP:
                return Configurations.gameplay.canPlayerUseColonyTPCommand;
            case RTP:
                return Configurations.gameplay.canPlayerUseRTPCommand;
            case KILLCITIZENS:
                return Configurations.gameplay.canPlayerUseKillCitizensCommand;
            case CITIZENINFO:
                return Configurations.gameplay.canPlayerUseCitizenInfoCommand;
            case LISTCITIZENS:
                return Configurations.gameplay.canPlayerUseListCitizensCommand;
            case RESPAWNCITIZENS:
                return Configurations.gameplay.canPlayerRespawnCitizensCommand;
            case SHOWCOLONYINFO:
                return Configurations.gameplay.canPlayerUseShowColonyInfoCommand;
            case ADDOFFICER:
                return Configurations.gameplay.canPlayerUseAddOfficerCommand;
            case DELETECOLONY:
                return Configurations.gameplay.canPlayerUseDeleteColonyCommand;
            case REFRESH_COLONY:
                return Configurations.gameplay.canPlayerUseRefreshColonyCommand;
            case MC_BACKUP:
                return Configurations.gameplay.canPlayerUseBackupCommand;
            case RSRESET:
                return Configurations.requestSystem.canPlayerUseResetCommand;
            default:
                return false;
        }
    }

    /**
     * Checks if the player has the permission to use the command.
     * By default officer and owner, overwrite this if other required.
     *
     * @param colony the colony.
     * @param player the player.
     * @return true if so.
     */
    public boolean canRankUseCommand(@NotNull final IColony colony, @NotNull final EntityPlayer player)
    {
        return colony.getPermissions().getRank(player).equals(Rank.OFFICER) || colony.getPermissions().getRank(player).equals(Rank.OWNER);
    }

    public enum Commands
    {
        CITIZENINFO,
        COLONYTP,
        RTP,
        DELETECOLONY,
        SPAWNCITZENS,
        KILLCITIZENS,
        LISTCITIZENS,
        RESPAWNCITIZENS,
        SHOWCOLONYINFO,
        ADDOFFICER,
        CHANGE_COLONY_OWNER,
        REFRESH_COLONY,
        HOMETP,
        MC_BACKUP,
        RSRESET
    }
}
