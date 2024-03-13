package com.minecolonies.core.commands;

import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.core.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.entity.pathfinding.pathjobs.AbstractPathJob;
import com.minecolonies.core.network.messages.client.SyncPathMessage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

/**
 * Displays information about a chosen citizen in a chosen colony.
 */
public class CommandEntityTrack implements IMCColonyOfficerCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final Entity sender = context.getSource().getEntity();
        if (!(sender instanceof Player))
        {
            return 1;
        }

        try
        {
            final Collection<? extends Entity> entities = EntityArgument.getEntities(context, "entity");
            if (entities.isEmpty())
            {
                context.getSource().sendSuccess(() -> Component.translatableEscape(CommandTranslationConstants.COMMAND_ENTITY_NOT_FOUND), true);
                return 0;
            }

            final Entity entity = entities.iterator().next();
            if (AbstractPathJob.trackingMap.getOrDefault((Player) sender, UUID.randomUUID()).equals(entity.getUUID()))
            {
                context.getSource().sendSuccess(() -> Component.translatableEscape(CommandTranslationConstants.COMMAND_ENTITY_TRACK_DISABLED), true);
                AbstractPathJob.trackingMap.remove((Player) sender);
                new SyncPathMessage(new HashSet<>(), new HashSet<>(), new HashSet<>()).sendToPlayer((ServerPlayer) sender);
            }
            else
            {
                context.getSource().sendSuccess(() -> Component.translatableEscape(CommandTranslationConstants.COMMAND_ENTITY_TRACK_ENABLED), true);
                AbstractPathJob.trackingMap.put((Player) sender, entity.getUUID());
            }
            return 1;
        }
        catch (CommandSyntaxException e)
        {
            Log.getLogger().error("Error attemting to track entity", e);
        }
        return 0;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "trackPath";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName()).then(IMCCommand.newArgument("entity", EntityArgument.entities()).executes(this::checkPreConditionAndExecute));
    }
}
