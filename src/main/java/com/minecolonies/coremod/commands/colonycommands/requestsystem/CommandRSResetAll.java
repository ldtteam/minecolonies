package com.minecolonies.coremod.commands.colonycommands.requestsystem;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_REQUEST_SYSTEM_RESET_ALL_SUCCESS;

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
        context.getSource().sendSuccess(new TranslationTextComponent(COMMAND_REQUEST_SYSTEM_RESET_ALL_SUCCESS), true);

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
