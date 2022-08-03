package com.minecolonies.coremod.commands.generalcommands;

import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.minecolonies.coremod.util.BackUpHelper;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;

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
    public int onExecute(final CommandContext<CommandSource> context)
    {
        BackUpHelper.lastBackupTime = 0;
        if (BackUpHelper.backupColonyData())
        {
            context.getSource().sendSuccess(new TranslationTextComponent(COMMAND_BACKUP_SUCCESS), true);
        }
        else
        {
            context.getSource().sendSuccess(new TranslationTextComponent(COMMAND_BACKUP_FAILED), true);
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
