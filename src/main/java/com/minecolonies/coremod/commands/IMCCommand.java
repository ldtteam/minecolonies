package com.minecolonies.coremod.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;

/**
 * Interface for commands, uses Mojang's Brigadier command framework, @see <a href=https://github.com/Mojang/brigadier></a> .
 */
public interface IMCCommand
{
    /**
     * Builds the command, overwrite and add further arguments etc for nonsimple commands.
     * @return the built command.
     */
    default LiteralArgumentBuilder<CommandSource> build()
    {
        return newLiteral(getName()).executes(this::onExecute);
    }

    /**
     * Helper function for typing.
     * @param name name of the new command.
     * @return
     */
    static LiteralArgumentBuilder<CommandSource> newLiteral(final String name)
    {
        return LiteralArgumentBuilder.literal(name);
    }

    /**
     * What happens when the command is executed
     * @param context the context of the command execution
     * @return
     */
    int onExecute(final CommandContext<CommandSource> context);

    /**
     * Name string of the command.
     * @return
     */
    String getName();
}
