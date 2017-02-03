package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.configuration.Configurations;
import com.minecolonies.coremod.util.ServerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;

/**
 * A command that has children. Is a single one-word command.
 */
public abstract class AbstractSingleCommand implements ISubCommand
{

    private final String[] parents;
    enum Commands
    {
        CITIZENSINFO, COLONYTP, DELETECOLONY, KILLCITIZENS, LISTCITIZENS, RESPAWNCITIZENS, SHOWCOLONYINFO, ADDOFFICER
    }
    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public AbstractSingleCommand(@NotNull final String... parents)
    {
        this.parents = parents;
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
     * Will check the config file to see if players are allowed to use the command that is sent here
     * and will verify that they are of correct rank to do so
     *@param player the players/senders name
     * @param theCommand which command to check if the player can use it
     * @return boolean
     */
    public boolean canPlayerUseCommand(EntityPlayer player, Commands theCommand,@NotNull final String... args)
    {
        int colonyId;

        colonyId = GetColonyAndCitizen.getColonyId(player.getCommandSenderEntity().getUniqueID(), player.getEntityWorld(), args);
        Colony chkColony = ColonyManager.getColony(colonyId);
        return canCommandSenderUseCommand(theCommand) && !chkColony.getPermissions().getRank(player).equals(Permissions.Rank.OFFICER) && !chkColony.getPermissions().getRank(player).equals(Permissions.Rank.OWNER);
    }
    /**
     * Will check the config file to see if players are allowed to use the command that is sent here
     *
     * @param theCommand which command to check if the player can use it
     * @return boolean
     */
    public boolean canCommandSenderUseCommand(Commands theCommand)
    {
        switch (theCommand)
        {
            case CITIZENSINFO: return Configurations.canPlayerUseCitizensInfoCommand;
            case COLONYTP: return Configurations.canPlayerUseCTPCommand;
            case KILLCITIZENS: return Configurations.canPlayerUseKillCitizensCommand;
            case LISTCITIZENS: return Configurations.canPlayerUseListCitizensCommand;
            case RESPAWNCITIZENS: return Configurations.canPlayerRespawnCitizensCommand;
            case SHOWCOLONYINFO: return Configurations.canPlayerUseShowColonyInfoCommand;
            case ADDOFFICER: return Configurations.canPlayerUseAddOfficerCommand;
            case DELETECOLONY: return Configurations.canPlayerUseDeleteColonyCommand;
        }
        return false;
    }

    /**
     * Will check to see if play is Opped for the given command name
     *
     * @param sender to check the player using the command
     * @param cmdName the name of the command to be checked
     * @return boolean
     */
    @NotNull
    public boolean isPlayerOpped(@NotNull final ICommandSender sender, String cmdName)
    {
        boolean checkedDone;
        checkedDone= FMLCommonHandler.instance().getMinecraftServerInstance().canCommandSenderUseCommand(3,cmdName);
        return checkedDone;
    }
}
