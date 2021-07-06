package com.minecolonies.coremod.commands.colonycommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.util.BackUpHelper;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;

import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;

public class CommandDeleteColony implements IMCColonyOfficerCommand
{
    /**
     * Formatable string to use the command
     */
    private static final String DELETE_BUILDNGS_ARG = "delete Buildings?";

    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        if (!context.getSource().hasPermission(OP_PERM_LEVEL) && !MineColonies.getConfig().getServer().canPlayerUseDeleteColonyCommand.get())
        {
            context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.notenabledinconfig"), true);
            return 0;
        }

        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.colonyidnotfound", colonyID), true);
            return 0;
        }

        final boolean deleteBuildings = BoolArgumentType.getBool(context, DELETE_BUILDNGS_ARG);

        BackUpHelper.backupColonyData();
        IColonyManager.getInstance().deleteColonyByDimension(colonyID, deleteBuildings, context.getSource().getLevel().dimension());
        context.getSource().sendSuccess(LanguageHandler.buildChatComponent("com.minecolonies.command.delete.success", colony.getName()), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "delete";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        String[] s = new String[2];
        s[0] = "true delete buildings";
        s[1] = "false keep buildings";

        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(DELETE_BUILDNGS_ARG, BoolArgumentType.bool())
                                 .suggests((ctx, builder) -> ISuggestionProvider.suggest(s, builder))
                                 .then(IMCCommand.newArgument("", StringArgumentType.string())
                                         .then(IMCCommand.newArgument("", StringArgumentType.string())
                                                 .executes(this::checkPreConditionAndExecute)))));
    }
}
