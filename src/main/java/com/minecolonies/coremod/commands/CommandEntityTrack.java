package com.minecolonies.coremod.commands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.AbstractPathJob;
import com.minecolonies.coremod.network.messages.client.SyncPathMessage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

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
    public int onExecute(final CommandContext<CommandSource> context)
    {
        final Entity sender = context.getSource().getEntity();
        if (!(sender instanceof PlayerEntity))
        {
            return 1;
        }

        try
        {
            final Collection<? extends Entity> entities = EntityArgument.getEntities(context, "entity");
            if (entities.isEmpty())
            {
                context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.noentityfound"), true);
                return 0;
            }

            final Entity entity = entities.iterator().next();
            if (AbstractPathJob.trackingMap.getOrDefault((PlayerEntity) sender, UUID.randomUUID()).equals(entity.getUUID()))
            {
                context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.citizentrack.success.disable"), true);
                AbstractPathJob.trackingMap.remove((PlayerEntity) sender);
                Network.getNetwork().sendToPlayer(new SyncPathMessage(new HashSet<>(), new HashSet<>(), new HashSet<>()), (ServerPlayerEntity) sender);
            }
            else
            {
                context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.citizentrack.success.enable"), true);
                AbstractPathJob.trackingMap.put((PlayerEntity) sender, entity.getUUID());
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
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName()).then(IMCCommand.newArgument("entity", EntityArgument.entities()).executes(this::checkPreConditionAndExecute));
    }
}
