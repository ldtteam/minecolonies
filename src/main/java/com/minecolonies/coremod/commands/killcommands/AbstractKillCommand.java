package com.minecolonies.coremod.commands.killcommands;

import com.minecolonies.coremod.commands.AbstractSingleCommand;
import com.minecolonies.coremod.commands.DeleteCommand;
import com.minecolonies.coremod.commands.MinecoloniesCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Abstract command for killing all entities of Type T on map.
 */
public abstract class AbstractKillCommand<T extends Entity> extends AbstractSingleCommand
{

    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param desc the command name.
     */
    public AbstractKillCommand(@NotNull final String desc)
    {
        super(MinecoloniesCommand.DESC, DeleteCommand.DESC, desc);
    }

    /**
     * Gets the DESC string of the command (Entity name usually).
     *
     * @return The {@link String} of the Desc.
     */
    public abstract String getDesc();

    /**
     * Gets the class of the entity we're killing.
     *
     * @return The {@link Class} of the Entity.
     */
    public abstract Class<T> getEntityClass();

    @Override
    public void execute(
                         @NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        if (sender instanceof EntityPlayer && !isPlayerOpped(sender))
        {
            sender.sendMessage(new TextComponentString("Must be OP to use command"));
            return;
        }

        int entitiesKilled = 0;
        for (final Entity entity : server.getEntityWorld().getEntities(getEntityClass(), entity -> true))
        {
            entity.setDead();
            entitiesKilled++;
        }
        sender.sendMessage(new TextComponentString(entitiesKilled + " entities killed"));

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
