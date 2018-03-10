package com.minecolonies.coremod.commands.generalcommands;

import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionArgument;
import com.minecolonies.coremod.commands.IActionCommand;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by asie on 2/16/17.
 */
public class BackupCommand extends AbstractSingleCommand implements IActionCommand
{
    public static final String DESC                   = "backup";
    public static final String NO_PERMISSION_MESSAGE  = "You do not have permission to backup colony data!";
    public static final String BACKUP_SUCCESS_MESSAGE = "Successfully backed up colony data!";
    public static final String BACKUP_FAILURE_MESSAGE = "Failed to back up colony data!";

    public BackupCommand()
    {
    }
    
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
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final List<ActionArgument> actionArgumentList,
            @NotNull final Map<String, Object> argumentValueByActionArgumentNameMap) throws CommandException
    {
        executeShared(server, sender);
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        executeShared(server, sender);
    }

    private void executeShared(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender) throws CommandException
    {

        if (isPlayerOpped(sender))
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
