package com.minecolonies.coremod.commands.commandTypes;

import com.ldtteam.structurize.util.LanguageHandler;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Interface for commands requiring OP rights to execute.
 */
public interface IMCOPCommand extends IMCCommand
{
    /**
     * Executes pre-checks before issuing the command. Checks for the senders type and OP rights.
     */
    @Override
    default boolean checkPreCondition(final CommandContext<CommandSourceStack> context)
    {
        if (context.getSource().hasPermission(OP_PERM_LEVEL))
        {
            return true;
        }

        final Entity sender = context.getSource().getEntity();
        if (!(sender instanceof Player))
        {
            return false;
        }

        if (!IMCCommand.isPlayerOped((Player) sender))
        {
            LanguageHandler.sendPlayerMessage((Player) sender, "com.minecolonies.command.notop");
            return false;
        }
        return true;
    }
}
