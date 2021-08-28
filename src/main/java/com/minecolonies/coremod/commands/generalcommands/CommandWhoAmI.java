package com.minecolonies.coremod.commands.generalcommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;

public class CommandWhoAmI implements IMCCommand
{
    /**
     * What happens when the command is executed
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final Entity sender = context.getSource().getEntity();
        if (!(sender instanceof Player))
        {
            return 0;
        }

        final IColony colony = IColonyManager.getInstance().getIColonyByOwner(sender.getCommandSenderWorld(), sender.getUUID());

        if (colony == null)
        {
            LanguageHandler.sendPlayerMessage((Player) sender, "com.minecolonies.command.whoami.nocolony");
            return 0;
        }

        final BlockPos pos = colony.getCenter();
        final String colonyName = colony.getName();
        final String playerName = sender.getDisplayName().getString();
        final String posString = "x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ();
        LanguageHandler.sendPlayerMessage((Player) sender, "com.minecolonies.command.whoami.hascolony", playerName, colonyName, colony.getID(), posString);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "whoami";
    }
}
