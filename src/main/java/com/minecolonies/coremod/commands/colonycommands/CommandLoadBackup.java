package com.minecolonies.coremod.commands.colonycommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.minecolonies.coremod.util.BackUpHelper;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;

/**
 * Loads a colony by id from the backup file.
 */
public class CommandLoadBackup implements IMCOPCommand
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

        final int colonyId = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        BackUpHelper.loadColonyBackup(colonyId, sender.dimension.getId());
        LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.loadbackup.success");
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "loadBackup";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute));
    }
}
