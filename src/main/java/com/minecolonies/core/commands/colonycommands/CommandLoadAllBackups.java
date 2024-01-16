package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.minecolonies.core.util.BackUpHelper;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_LOAD_BACKUP_SUCCESS;

public class CommandLoadAllBackups implements IMCOPCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        BackUpHelper.loadAllBackups();
        context.getSource().sendSuccess(Component.translatable(COMMAND_COLONY_LOAD_BACKUP_SUCCESS), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "loadAllColoniesFromBackup";
    }
}
