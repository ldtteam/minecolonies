package com.minecolonies.coremod.commands.commandTypes;

import com.minecolonies.api.util.MessageUtils;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_REQUIRES_OP;

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

        if (!(context.getSource().getEntity() instanceof final Player sender))
        {
            return false;
        }

        if (!IMCCommand.isPlayerOped(sender))
        {
            MessageUtils.format(COMMAND_REQUIRES_OP).sendTo(sender);
            return false;
        }
        return true;
    }
}
