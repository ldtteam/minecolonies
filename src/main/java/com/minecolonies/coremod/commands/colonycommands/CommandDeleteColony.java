package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.util.BackUpHelper;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;

public class CommandDeleteColony implements IMCColonyOfficerCommand
{
    /**
     * Name for the command
     */
    private static final String COMMAND_NAME = "delete";

    /**
     * Literal for the word buildings.
     */
    private static final String BUILDINGS_LITERAL = "buildings";

    /**
     * Key to use when keeping the buildings.
     */
    private static final String KEEP_BUILDING_OPTION = "keep";

    /**
     * Key to use when deleting the buildings.
     */
    private static final String DELETE_BUILDING_OPTION = "delete";

    /**
     * Key for the remove buildings argument
     */
    private static final String KEEP_BUILDINGS_ARG = KEEP_BUILDING_OPTION + " / " + DELETE_BUILDING_OPTION;

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(KEEP_BUILDINGS_ARG, DeleteBuildingsArgumentType.argument())
                                 .then(IMCCommand.newLiteral(BUILDINGS_LITERAL)
                                         .executes(this::checkPreConditionAndExecute))));
    }

    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        if (!context.getSource().hasPermission(OP_PERM_LEVEL) && !MineColonies.getConfig().getServer().canPlayerUseDeleteColonyCommand.get())
        {
            context.getSource().sendSuccess(Component.translatable(CommandTranslationConstants.COMMAND_DISABLED_IN_CONFIG), true);
            return 0;
        }

        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(Component.translatable(CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND, colonyID), true);
            return 0;
        }

        final boolean deleteBuildings = DeleteBuildingsArgumentType.getValue(context, KEEP_BUILDINGS_ARG);

        BackUpHelper.backupColonyData();
        IColonyManager.getInstance().deleteColonyByDimension(colonyID, deleteBuildings, context.getSource().getLevel().dimension());
        context.getSource().sendSuccess(Component.translatable(CommandTranslationConstants.COMMAND_COLONY_DELETE_SUCCESS, colony.getName()), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return COMMAND_NAME;
    }

    public static class DeleteBuildingsArgumentType implements ArgumentType<Boolean>
    {
        static
        {
            ArgumentTypeInfos.registerByClass(CommandDeleteColony.DeleteBuildingsArgumentType.class,
              SingletonArgumentInfo.contextFree(CommandDeleteColony.DeleteBuildingsArgumentType::argument));
        }
        public static DeleteBuildingsArgumentType argument()
        {
            return new DeleteBuildingsArgumentType();
        }

        public static boolean getValue(final CommandContext<?> context, final String name)
        {
            return context.getArgument(name, Boolean.class);
        }

        @Override
        public Boolean parse(final StringReader reader) throws CommandSyntaxException
        {
            String arg = reader.readUnquotedString();
            if (arg.equals(KEEP_BUILDING_OPTION))
            {
                return false;
            }
            else if (arg.equals(DELETE_BUILDING_OPTION))
            {
                return true;
            }
            else
            {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
            }
        }

        @Override
        public CompletableFuture<Suggestions> listSuggestions(final CommandContext context, final SuggestionsBuilder builder)
        {
            if (KEEP_BUILDING_OPTION.startsWith(builder.getRemainingLowerCase()))
            {
                builder.suggest(KEEP_BUILDING_OPTION);
            }
            if (DELETE_BUILDING_OPTION.startsWith(builder.getRemainingLowerCase()))
            {
                builder.suggest(DELETE_BUILDING_OPTION);
            }
            return builder.buildFuture();
        }
    }
}
