package com.minecolonies.coremod.commands.killcommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.coremod.commands.ActionMenuState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;

/**
 * Command for killing all Barbarians on map
 */
public class RaiderKillCommand extends AbstractKillCommand<AbstractEntityMinecoloniesMob>
{
    public static final String DESC = "raiders";

    /**
     * Initialize this SubCommand with it's parents.
     */
    public RaiderKillCommand()
    {
        super(DESC);
    }

    @Override
    public String getDesc()
    {
        return DESC;
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final ActionMenuState actionMenuState) throws CommandException
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
        if (sender instanceof EntityPlayer && !isPlayerOpped(sender))
        {
            sender.sendMessage(new TextComponentString("Must be OP to use command"));
            return;
        }

        int entitiesKilled = 0;
        for (final AbstractEntityMinecoloniesMob entity : server.getEntityWorld().getEntities(getEntityClass(), entity -> true))
        {
            final IColony colony = entity.getColony();
            if (colony != null && entity.getEventID() > 0)
            {
                final IColonyEvent event = colony.getEventManager().getEventByID(entity.getEventID());
                event.onEntityDeath(entity);
                event.setStatus(EventStatus.DONE);
            }
            entity.setDead();
            entitiesKilled++;
        }
        sender.sendMessage(new TextComponentString(entitiesKilled + " entities killed"));
    }

    @Override
    public Class<AbstractEntityMinecoloniesMob> getEntityClass()
    {
        return AbstractEntityMinecoloniesMob.class;
    }
}
