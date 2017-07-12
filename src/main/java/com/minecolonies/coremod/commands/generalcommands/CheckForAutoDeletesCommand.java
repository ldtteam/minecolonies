package com.minecolonies.coremod.commands.generalcommands;

import com.minecolonies.api.colony.permissions.Rank;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CheckForAutoDeletesCommand extends AbstractSingleCommand
{

    public static final  String DESC              = "check";
    private static final String NO_COLONIES_FOUND = "There where no colonies found that could be auto-deleted";
    private static final String ABLE_TO_DELETE    = "We were able to delete some colonies!";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public CheckForAutoDeletesCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @Override
    public boolean canRankUseCommand(@NotNull final Colony colony, @NotNull final EntityPlayer player)
    {
        return colony.getPermissions().getRank(player).equals(Rank.OWNER);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        final List<Colony> colonies = ColonyManager.getColonies();

        boolean colonyWasDeleted = false;

        for (int index = 0; colonies.size() - 1 >= index; index++)
        {
            final Colony colony = colonies.get(index);

            if (colony.getCanBeAutoDeleted() && Configurations.autoDeleteColoniesInHours != 0 && colony.getLastContactInHours() >= Configurations.autoDeleteColoniesInHours)
            {
                sender.addChatMessage(new TextComponentString("The Inactive Colony: " + colony.getName() + " with the ID of: " + colony.getID() + " was deleted after: "
                                                                + colony.getLastContactInHours() + "hour/s of no contact"));
                server.addScheduledTask(() -> ColonyManager.deleteColony(colony.getID()));
                colonyWasDeleted = true;
            }
        }

        if (colonyWasDeleted)
        {
            sender.addChatMessage(new TextComponentString(ABLE_TO_DELETE));
        }
        else
        {
            sender.addChatMessage(new TextComponentString(NO_COLONIES_FOUND));
        }
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
        return index == 0
                 && args.length > 0
                 && !args[0].isEmpty()
                 && getIthArgument(args, 0, Integer.MAX_VALUE) == Integer.MAX_VALUE;
    }
}
