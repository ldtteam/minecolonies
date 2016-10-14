package com.minecolonies.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A command that has children.
 */
public abstract class SingleCommand implements ISubCommand
{

    private final String[] parents;

    public SingleCommand(@NotNull String[] parents)
    {
        this.parents = parents;
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        final StringBuilder sb = new StringBuilder().append('/');
        for (String parent : parents)
        {
            sb.append(parent).append(' ');
        }
        return sb.toString();
    }

}
