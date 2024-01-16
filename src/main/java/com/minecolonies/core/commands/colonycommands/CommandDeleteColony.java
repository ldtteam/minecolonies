package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.util.BackUpHelper;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.concurrent.CompletableFuture;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.*;
import static com.minecolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

public class CommandDeleteColony implements IMCColonyOfficerCommand
{
    /**
     * Name for the command
     */
    private static final String COMMAND_NAME = "delete";

    /**
     * Key for the remove buildings argument
     */
    private static final String DELETE_BUILDINGS_ARG = "keep / delete buildings";

    /**
     * Key for the confirmation argument.
     */
    private static final String CONFIRM_ARG = "confirm";

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .executes(this::executeGuidedBuildingAsk)
                         .then(IMCCommand.newArgument(DELETE_BUILDINGS_ARG, DeleteBuildingsArgumentType.argument())
                                 .executes(this::executeGuidedConfirm)
                                 .then(IMCCommand.newArgument(CONFIRM_ARG, BoolArgumentType.bool())
                                         .executes(this::checkPreConditionAndExecute))));
    }

    /**
     * Generates a new click event, based on the original command, adding one additional boolean to the command.
     *
     * @param context the context of the command.
     * @param confirm whether to append true or false to the command.
     * @return the created click event.
     */
    private ClickEvent createClickEvent(final CommandContext<CommandSourceStack> context, final boolean confirm)
    {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + context.getInput() + " " + confirm);
    }

    /**
     * Modifies a button component with square braces.
     *
     * @param button the original input button.
     * @return the output component with braces.
     */
    private Component braceButtonComponent(final Component button)
    {
        return Component.literal("[").append(button).append("]");
    }

    /**
     * Execute the first step in the guided setup, asking whether the player wants to remove or destroy the buildings.
     *
     * @param context the context of the command.
     * @return 1 if successful and 0 if incomplete.
     */
    private int executeGuidedBuildingAsk(final CommandContext<CommandSourceStack> context)
    {
        if (!checkPreCondition(context))
        {
            return 0;
        }

        final Style keepButtonStyle = Style.EMPTY
                                        .withBold(true)
                                        .withColor(ChatFormatting.DARK_GREEN)
                                        .withClickEvent(createClickEvent(context, false));

        final Style deleteButtonStyle = Style.EMPTY
                                          .withBold(true)
                                          .withColor(ChatFormatting.DARK_RED)
                                          .withClickEvent(createClickEvent(context, true));

        final Component keepButton = braceButtonComponent(Component.translatable(COMMAND_COLONY_DELETE_CONFIRM_BUILDING_KEEP).setStyle(keepButtonStyle));
        final Component deleteButton = braceButtonComponent(Component.translatable(COMMAND_COLONY_DELETE_CONFIRM_BUILDING_DELETE).setStyle(deleteButtonStyle));

        final TranslatableContents contents = new TranslatableContents(COMMAND_COLONY_DELETE_CONFIRM_BUILDING, keepButton, deleteButton);
        context.getSource().sendSuccess(MutableComponent.create(contents).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)), true);
        return 1;
    }

    /**
     * Execute the second step in the guided setup, asking to confirm if the user really wants to delete their colony.
     *
     * @param context the context of the command.
     * @return 1 if successful and 0 if incomplete.
     */
    private int executeGuidedConfirm(final CommandContext<CommandSourceStack> context)
    {
        if (!checkPreCondition(context))
        {
            return 0;
        }

        final Style buttonStyle = Style.EMPTY
                                    .withBold(true)
                                    .withColor(ChatFormatting.DARK_RED)
                                    .withClickEvent(createClickEvent(context, true));
        final Component confirmButton = braceButtonComponent(Component.translatable(COMMAND_COLONY_DELETE_CONFIRM_FINAL_HERE).setStyle(buttonStyle));

        Component deleteBuildingsComponent = Component.empty();
        if (BoolArgumentType.getBool(context, DELETE_BUILDINGS_ARG))
        {
            deleteBuildingsComponent = Component.translatable(COMMAND_COLONY_DELETE_CONFIRM_FINAL_BUILDING).append(" ").setStyle(Style.EMPTY.withBold(true));
        }

        final TranslatableContents contents = new TranslatableContents(COMMAND_COLONY_DELETE_CONFIRM_FINAL, deleteBuildingsComponent, confirmButton);
        context.getSource().sendSuccess(MutableComponent.create(contents).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)), true);
        return 1;
    }

    @Override
    public boolean checkPreCondition(final CommandContext<CommandSourceStack> context)
    {
        if (!context.getSource().hasPermission(OP_PERM_LEVEL) && !MineColonies.getConfig().getServer().canPlayerUseDeleteColonyCommand.get())
        {
            context.getSource().sendSuccess(Component.translatable(CommandTranslationConstants.COMMAND_DISABLED_IN_CONFIG), true);
            return false;
        }

        return IMCColonyOfficerCommand.super.checkPreCondition(context);
    }

    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(Component.translatable(CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND, colonyID), true);
            return 0;
        }

        final boolean deleteBuildings = BoolArgumentType.getBool(context, DELETE_BUILDINGS_ARG);
        final boolean confirmation = BoolArgumentType.getBool(context, CONFIRM_ARG);

        if (!confirmation)
        {
            return 1;
        }

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

        @Override
        public Boolean parse(final StringReader reader) throws CommandSyntaxException
        {
            return BoolArgumentType.bool().parse(reader);
        }

        @Override
        public CompletableFuture<Suggestions> listSuggestions(final CommandContext context, final SuggestionsBuilder builder)
        {
            if ("true".startsWith(builder.getRemainingLowerCase()))
            {
                builder.suggest("true", Component.translatable(COMMAND_COLONY_DELETE_SUGGEST_DELETE));
            }
            if ("false".startsWith(builder.getRemainingLowerCase()))
            {
                builder.suggest("false", Component.translatable(COMMAND_COLONY_DELETE_SUGGEST_KEEP));
            }
            return builder.buildFuture();
        }
    }
}
