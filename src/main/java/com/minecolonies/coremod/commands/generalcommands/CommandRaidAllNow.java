package com.minecolonies.coremod.commands.generalcommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class CommandRaidAllNow implements IMCOPCommand
{
    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        final Entity sender = context.getSource().getEntity();

        for (final IColony colony : IColonyManager.getInstance().getAllColonies())
        {
            colony.getRaiderManager().raiderEvent();
        }

        LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.raidtonight");
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "raid-All-now";
    }
}
