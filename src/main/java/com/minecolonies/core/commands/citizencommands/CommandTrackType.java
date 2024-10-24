package com.minecolonies.core.commands.citizencommands;

import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

/**
 * Displays information about a chosen citizen in a chosen colony.
 */
public class CommandTrackType implements IMCOPCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        if (!context.getSource().isPlayer())
        {
            return 1;
        }

        final String className = StringArgumentType.getString(context, "pathjobname");
        if (className.equals("clear"))
        {
            PathfindingUtils.trackByType.entrySet().removeIf(entry -> entry.getValue().equals(context.getSource().getPlayer().getUUID()));
            context.getSource().sendSystemMessage(Component.literal("Removed tracking for player"));
            return 1;
        }

        PathfindingUtils.trackByType.put(className, context.getSource().getPlayer().getUUID());
        context.getSource().sendSystemMessage(Component.literal("Tracking enabled for pathjobs containing: " + className));
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "trackPathType";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument("pathjobname", StringArgumentType.word()).suggests((context, builder) -> {
                     builder.suggest("clear");
                     return builder.buildFuture();
                 }).executes(this::checkPreConditionAndExecute));
    }
}
