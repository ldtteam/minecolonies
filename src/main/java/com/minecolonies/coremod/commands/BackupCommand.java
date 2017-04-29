package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.ColonyManager;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.MC_BACKUP;

public class BackupCommand extends AbstractSingleCommand
{
    public static final String DESC                   = "backup";
    public static final String NO_PERMISSION_MESSAGE  = "You do not have permission to backup colony data!";
    public static final String BACKUP_SUCCESS_MESSAGE = "Successfully backed up colony data!";
    public static final String BACKUP_FAILURE_MESSAGE = "Failed to back up colony data!";

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public BackupCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {

        if (isPlayerOpped(sender, String.valueOf(MC_BACKUP)))
        {
            server.addScheduledTask(() ->
            {
                if (ColonyManager.backupColonyData())
                {
                    sender.sendMessage(new TextComponentString(BACKUP_SUCCESS_MESSAGE));
                }
                else
                {
                    sender.sendMessage(new TextComponentString(BACKUP_FAILURE_MESSAGE));
                }
            });
        }
        else
        {
            sender.sendMessage(new TextComponentString(NO_PERMISSION_MESSAGE));
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
        return false;
    }
}
