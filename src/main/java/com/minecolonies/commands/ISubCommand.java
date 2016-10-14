package com.minecolonies.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Override this to specify a sub command.
 */
public interface ISubCommand
{

    @NotNull
    String getCommandUsage(@NotNull final ICommandSender sender);

    void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException;

    @NotNull
    List<String> getTabCompletionOptions(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String[] args, @Nullable final BlockPos pos);

    boolean isUsernameIndex(@NotNull final String[] args, final int index);
}
