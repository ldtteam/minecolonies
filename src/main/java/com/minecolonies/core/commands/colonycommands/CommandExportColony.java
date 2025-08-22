package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.commands.arguments.ColonyIdArgument;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.minecolonies.core.util.BackUpHelper;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_EXPORT_SUCCESS;
import static com.minecolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

/**
 * Command to export a colony from a world save, exports region and backup file.
 */
public class CommandExportColony implements IMCOPCommand
{
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final IColony colony = ColonyIdArgument.getColony(context, COLONYID_ARG);
        BackUpHelper.backupColonyData();

        final String filename = BackUpHelper.exportColony(colony);
        context.getSource().sendSuccess(() -> Component.translatable(COMMAND_COLONY_EXPORT_SUCCESS, filename), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "export";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, ColonyIdArgument.id()).executes(this::checkPreConditionAndExecute));
    }
}
