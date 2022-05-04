package com.minecolonies.coremod.commands.colonycommands.requestsystem;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;

import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;

public class CommandRSReset implements IMCCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(new TranslationTextComponent(CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND, colonyID), true);
            return 0;
        }

        if (!context.getSource().hasPermission(OP_PERM_LEVEL) && !MineColonies.getConfig().getServer().canPlayerUseResetCommand.get())
        {
            context.getSource().sendSuccess(new TranslationTextComponent(CommandTranslationConstants.COMMAND_DISABLED_IN_CONFIG), true);
            return 0;
        }

        colony.getRequestManager().reset();
        context.getSource().sendSuccess(new TranslationTextComponent(CommandTranslationConstants.COMMAND_REQUEST_SYSTEM_RESET_SUCCESS, colony.getName()), true);

        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "requestsystem-reset";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute));
    }
}
