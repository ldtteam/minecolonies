package com.minecolonies.coremod.commands.generalcommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class CommandWhoAmI implements IMCCommand
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

        final IColony colony = IColonyManager.getInstance().getIColonyByOwner(sender.getEntityWorld(), sender.getUniqueID());

        if (colony == null)
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.whoami.nocolony");
            return 0;
        }

        final BlockPos pos = colony.getCenter();
        final String colonyName = colony.getName();
        final String playerName = sender.getDisplayName().getFormattedText();
        final String posString = "x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ();
        LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.whoami.hascolony", playerName, colonyName, colony.getID() ,posString);
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
