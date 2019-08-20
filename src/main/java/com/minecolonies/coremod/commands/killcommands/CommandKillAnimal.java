package com.minecolonies.coremod.commands.killcommands;

import com.minecolonies.coremod.commands.IMCCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;

public class CommandKillAnimal implements IMCCommand
{
    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "animals";
    }
}
