package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.minecolonies.core.util.BackUpHelper;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_LOAD_BACKUP_SUCCESS;
import static com.minecolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

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
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final int colonyId = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        BackUpHelper.loadColonyBackup(colonyId, context.getSource().getLevel().dimension(), true, true);
        context.getSource().sendSuccess(Component.translatable(COMMAND_COLONY_LOAD_BACKUP_SUCCESS), true);
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
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute));
    }
}
