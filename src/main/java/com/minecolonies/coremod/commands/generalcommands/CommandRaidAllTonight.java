package com.minecolonies.coremod.commands.generalcommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;

public class CommandRaidAllTonight implements IMCOPCommand
{
    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        for (final IColony colony : IColonyManager.getInstance().getAllColonies())
        {
            colony.getRaiderManager().setWillRaidTonight(true);
        }

        context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.raidtonight"), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "raid-All-tonight";
    }
}
