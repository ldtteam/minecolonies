package com.minecolonies.coremod.commands.generalcommands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.minecolonies.api.util.LanguageHandler;
import org.jetbrains.annotations.NotNull;

import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.ActionMenu;
import com.minecolonies.coremod.commands.IActionCommand;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

/**
 * Created by asie on 2/16/17.
 */
public class BackupCommand extends AbstractSingleCommand implements IActionCommand
{
    public static final String DESC = "backup";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public BackupCommand()
    {
        super();
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
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenu actionMenu) throws CommandException
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
                    sender.sendMessage(new TextComponentString(LanguageHandler.format(COLONIES_BACKUP_SUCCESS)));
                }
                else
                {
                    sender.sendMessage(new TextComponentString(LanguageHandler.format(COLONIES_BACKUP_FAIL)));
                }
            });
        }
        else
        {
            sender.sendMessage(new TextComponentString(LanguageHandler.format(COLONIES_BACKUP_NOPERMS)));
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
