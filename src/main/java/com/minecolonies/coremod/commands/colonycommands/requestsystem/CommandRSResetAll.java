package com.minecolonies.coremod.commands.colonycommands.requestsystem;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;

public class CommandRSResetAll implements IMCOPCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        for (final IColony colony : IColonyManager.getInstance().getAllColonies())
        {
            colony.getRequestManager().reset();
        }
        context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.rsreset.success"), true);

        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "requestsystem-reset-all";
    }
}
