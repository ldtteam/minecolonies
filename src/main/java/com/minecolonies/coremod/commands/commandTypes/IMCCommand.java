package com.minecolonies.coremod.commands.commandTypes;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Interface for commands, uses Mojang's Brigadier command framework, @see <a href=https://github.com/Mojang/brigadier></a> .
 */
public interface IMCCommand
{
    /**
     * Builds the command, overwrite and add further arguments etc for nonsimple commands. When overwriting this make sure to check the preconditions.
     *
     * @return the built command.
     */
    default LiteralArgumentBuilder<CommandSource> build()
    {
        return newLiteral(getName()).executes(this::checkPreConditionAndExecute);
    }

    /**
     * Helper function for typing.
     *
     * @param name name of the new command.
     */
    static LiteralArgumentBuilder<CommandSource> newLiteral(final String name)
    {
        return LiteralArgumentBuilder.literal(name);
    }

    static <T> RequiredArgumentBuilder<CommandSource, T> newArgument(final String name, final ArgumentType<T> type)
    {
        return RequiredArgumentBuilder.argument(name, type);
    }

    /**
     * Executes pre-checks before issuing the command
     */
    default int checkPreConditionAndExecute(final CommandContext<CommandSource> context)
    {
        if (!checkPreCondition(context))
        {
            return 0;
        }

        return onExecute(context);
    }

    /**
     * Preconditions to check before executing
     */
    default boolean checkPreCondition(final CommandContext<CommandSource> context)
    {
        return context.getSource().getEntity() instanceof PlayerEntity;
    }

    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    int onExecute(final CommandContext<CommandSource> context);

    /**
     * Name string of the command.
     */
    String getName();

    static boolean isPlayerOped(final PlayerEntity player)
    {
        if (player.getServer() == null)
        {
            return false;
        }

        return player.getServer().getPlayerList().canSendCommands(player.getGameProfile());
    }
}
