package com.minecolonies.coremod.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
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
     * @param sender the entity that executed this command.
     * @param actionMenuState contains argument values
     * @throws CommandException if something goes wrong (like wrong syntax).
     */
    void execute(@NotNull MinecraftServer server, @NotNull CommandSource sender, @NotNull ActionMenuState actionMenuState) throws CommandException;
}
