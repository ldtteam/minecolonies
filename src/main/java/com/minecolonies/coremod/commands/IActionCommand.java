package com.minecolonies.coremod.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import org.jetbrains.annotations.NotNull;

/**
 * Override this to specify a sub command.
 */
public interface IActionCommand
{
    /**
     * Callback for when the command is executed.
     *
     * @param server the server this is executed on.
     * @param sender this commands executor.
     * @param actionMenu actionMenu for this execution containing arguments
     * @throws CommandException if something goes wrong (like wrong syntax).
     */
    void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, @NotNull ActionMenu actionMenu) throws CommandException;
}
