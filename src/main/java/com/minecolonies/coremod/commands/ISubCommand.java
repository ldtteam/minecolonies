package com.minecolonies.coremod.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
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

    /**
     * Gets the usage string for the command.
     *
     * @param sender the CommandSender who gets to see the usage string.
     * @return this subcommands part of the usage string.
     */
    @NotNull
    String getCommandUsage(@NotNull final CommandSource sender);

    /**
     * Callback for when the command is executed.
     *
     * @param server the server this is executed on.
     * @param sender this commands executor.
     * @param args   leftover args stripped from parents.
     * @throws CommandException if something goes wrong (like wrong syntax).
     */
    void execute(@NotNull final MinecraftServer server, @NotNull final CommandSource sender, @NotNull final String... args) throws CommandException;

    /**
     * Return all possible autocomplete options.
     *
     * @param server the server this is executed on.
     * @param sender this commands executor.
     * @param args   leftover args stripped from parents.
     * @param pos    the block where this is called.
     * @return a list containing all positions.
     */
    @NotNull
    List<String> getTabCompletionOptions(@NotNull final MinecraftServer server, @NotNull final CommandSource sender, @NotNull final String[] args, @Nullable final BlockPos pos);

    /**
     * Return whether the specified command parameter index is a username
     * parameter.
     *
     * @param args  leftover args stripped from parents.
     * @param index the argument index offset by stripped parents.
     * @return true if this place is a username.
     */
    boolean isUsernameIndex(@NotNull final String[] args, final int index);
}
