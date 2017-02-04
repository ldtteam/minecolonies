package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.permissions.Permissions;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;
import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.ADDOFFICER;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * List all colonies.
 */
public class AddOfficerCommand extends AbstractSingleCommand
{

    public static final  String       DESC                            = "addOfficer";
    private static final String       SUCCESS_MESSAGE                 = "Succesfully added Player %s to colony %d";
    private static final String       COLONY_NULL                     = "Couldn't find colony %d.";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public AddOfficerCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "<ColonyId> <(Optional)Player>";
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {

       /* check if sender is permitted to do this :: OFFICER or MAYOR */
        boolean chkPlayer = canCommandSenderUseCommand(ADDOFFICER);
            /* this checks config to see if player is allowed to use the command and if they are mayor or office of the Colony */
        if (!chkPlayer)
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString("Not happenin bro!!, You are not permitted to do that!"));
            return;
        }
                /* here we see if they have colony rank to do this command */
        EntityPlayer player = (EntityPlayer)sender;
        final int colonyId = getIthArgument(args, 0, -1);

        Colony chkColony = ColonyManager.getColony(colonyId);
        if (!chkColony.getPermissions().getRank(player).equals(Permissions.Rank.OWNER))
        {
            sender.getCommandSenderEntity().addChatMessage(new TextComponentString("Not happenin bro!!, You are not permitted to do that!"));
            return;
        }
        Colony colony = ColonyManager.getColony(colonyId);



        String playerName = null;

        if (args.length >= 2)
        {
            playerName = args[1];
        }

        if(playerName == null || playerName.isEmpty())
        {
            playerName = sender.getName();
        }

        /*Colony colony = ColonyManager.getColony(colonyId);*/

        //No citizen or citizen defined.
        if (colony.equals(null))
        {
            sender.addChatMessage(new TextComponentString(String.format(COLONY_NULL, colonyId, colonyId)));
            return;
        }

        colony.getPermissions().addPlayer(playerName, Permissions.Rank.OFFICER, colony.getWorld());
        sender.addChatMessage(new TextComponentString(String.format(SUCCESS_MESSAGE, playerName, colonyId)));
    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
                                                 @NotNull final MinecraftServer server,
                                                 @NotNull final ICommandSender sender,
                                                 @NotNull final String[] args,
                                                 @Nullable final BlockPos pos)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        return false;
    }
}
