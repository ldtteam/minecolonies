package com.minecolonies.coremod.commands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.configuration.Configurations;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.common.FMLCommonHandler;
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

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final CommandSource sender)
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

    public boolean canPlayerUseCommand(final PlayerEntity player, final Commands theCommand, final int colonyId)
    {
        if (isPlayerOpped(player))
        {
            return true;
        }

        final IColony chkColony = IColonyManager.getInstance().getColonyByWorld(colonyId, ServerLifecycleHooks.getCurrentServer().getWorld(0));
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
    public static boolean isPlayerOpped(@NotNull final CommandSource sender)
    {
        if (sender instanceof PlayerEntity)
        {
            return ServerLifecycleHooks.getCurrentServer().getPlayerList()
                     .canSendCommands(((PlayerEntity) sender).getGameProfile());
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
                return MineColonies.getConfig().getCommon().gameplay.canPlayerUseHomeTPCommand;
            case COLONYTP:
                return MineColonies.getConfig().getCommon().gameplay.canPlayerUseColonyTPCommand;
            case RTP:
                return MineColonies.getConfig().getCommon().gameplay.canPlayerUseRTPCommand;
            case KILLCITIZENS:
                return MineColonies.getConfig().getCommon().gameplay.canPlayerUseKillCitizensCommand;
            case CITIZENINFO:
                return MineColonies.getConfig().getCommon().gameplay.canPlayerUseCitizenInfoCommand;
            case LISTCITIZENS:
                return MineColonies.getConfig().getCommon().gameplay.canPlayerUseListCitizensCommand;
            case RESPAWNCITIZENS:
                return MineColonies.getConfig().getCommon().gameplay.canPlayerRespawnCitizensCommand;
            case SHOWCOLONYINFO:
                return MineColonies.getConfig().getCommon().gameplay.canPlayerUseShowColonyInfoCommand;
            case ADDOFFICER:
                return MineColonies.getConfig().getCommon().gameplay.canPlayerUseAddOfficerCommand;
            case DELETECOLONY:
                return MineColonies.getConfig().getCommon().gameplay.canPlayerUseDeleteColonyCommand;
            case REFRESH_COLONY:
                return MineColonies.getConfig().getCommon().gameplay.canPlayerUseRefreshColonyCommand;
            case MC_BACKUP:
                return MineColonies.getConfig().getCommon().gameplay.canPlayerUseBackupCommand;
            case RSRESET:
                return MineColonies.getConfig().getCommon().requestSystem.canPlayerUseResetCommand;
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
    public boolean canRankUseCommand(@NotNull final IColony colony, @NotNull final PlayerEntity player)
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
