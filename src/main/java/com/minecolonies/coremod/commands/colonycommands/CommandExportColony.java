package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.minecolonies.coremod.util.BackUpHelper;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_EXPORT_SUCCESS;
import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND;
import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;

/**
 * Command to export a colony from a world save, exports region and backup file.
 */
public class CommandExportColony implements IMCOPCommand
{
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        final int colonyId = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        BackUpHelper.backupColonyData();
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(new TranslationTextComponent(COMMAND_COLONY_ID_NOT_FOUND, colonyId), true);
            return 0;
        }

        context.getSource().sendSuccess(new TranslationTextComponent(COMMAND_COLONY_EXPORT_SUCCESS, BackUpHelper.exportColony(colony)), true);
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
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute));
    }
}
