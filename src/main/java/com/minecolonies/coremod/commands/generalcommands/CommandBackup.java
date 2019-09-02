package com.minecolonies.coremod.commands.generalcommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.minecolonies.coremod.util.BackUpHelper;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

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
        final Entity sender = context.getSource().getEntity();
        if (BackUpHelper.backupColonyData())
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.backup.success");
        }
        else
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.backup.failed");
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
