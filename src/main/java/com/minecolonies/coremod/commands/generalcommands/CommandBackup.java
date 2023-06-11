package com.minecolonies.coremod.commands.generalcommands;

import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.minecolonies.coremod.util.BackUpHelper;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_BACKUP_FAILED;
import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_BACKUP_SUCCESS;

public class CommandBackup implements IMCOPCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        BackUpHelper.lastBackupTime = 0;
        if (BackUpHelper.backupColonyData())
        {
            context.getSource().sendSuccess(() -> Component.translatable(COMMAND_BACKUP_SUCCESS), true);
        }
        else
        {
            context.getSource().sendSuccess(() -> Component.translatable(COMMAND_BACKUP_FAILED), true);
        }
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "backup";
    }
}
