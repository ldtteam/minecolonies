package com.minecolonies.core.commands.citizencommands;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static com.minecolonies.api.colony.ICitizenData.MAX_SATURATION;
import static com.minecolonies.core.commands.CommandArgumentNames.CITIZENID_ARG;
import static com.minecolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

/**
 * Allows modifying citizen data for test purposes.
 */
public class CommandCitizenModify implements IMCColonyOfficerCommand
{
    private static final String VALUE_ARG = "value";

    @Override
    public int onExecute(@NotNull final CommandContext<CommandSourceStack> context)
    {
        return 0;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "modify";
    }

    @NotNull
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                        .then(IMCCommand.newArgument(CITIZENID_ARG, IntegerArgumentType.integer(1))
                                .then(IMCCommand.newLiteral("saturation")
                                        .then(IMCCommand.newLiteral("=")
                                                .then(IMCCommand.newArgument(VALUE_ARG, DoubleArgumentType.doubleArg(0, MAX_SATURATION))
                                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(List.of("0.0", String.valueOf(MAX_SATURATION)), builder))
                                                        .executes(ctx -> adjust(ctx, citizen -> citizen.setSaturation(DoubleArgumentType.getDouble(ctx, VALUE_ARG)),
                                                                citizen -> String.valueOf(citizen.getSaturation())))))
                                        .then(IMCCommand.newLiteral("+")
                                                .then(IMCCommand.newArgument(VALUE_ARG, DoubleArgumentType.doubleArg(0, MAX_SATURATION))
                                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(List.of("1.0"), builder))
                                                        .executes(ctx -> adjust(ctx, citizen -> citizen.increaseSaturation(DoubleArgumentType.getDouble(ctx, VALUE_ARG)),
                                                                citizen -> String.valueOf(citizen.getSaturation())))))
                                        .then(IMCCommand.newLiteral("-")
                                                .then(IMCCommand.newArgument(VALUE_ARG, DoubleArgumentType.doubleArg(0, MAX_SATURATION))
                                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(List.of("1.0"), builder))
                                                        .executes(ctx -> adjust(ctx, citizen -> citizen.decreaseSaturation(DoubleArgumentType.getDouble(ctx, VALUE_ARG)),
                                                                citizen -> String.valueOf(citizen.getSaturation())))))
                                )));
    }

    private int adjust(@NotNull final CommandContext<CommandSourceStack> context,
                       @NotNull final Consumer<ICitizenData> action,
                       @NotNull final Function<ICitizenData, String> valueProvider)
    {
        return execute(context, citizen ->
        {
            action.accept(citizen);

            final String type = context.getNodes().get(5).getNode().getName();
            final String value = valueProvider.apply(citizen);
            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_MODIFY_SUCCESS, type, citizen.getName(), value), true);
            return 0;
        });
    }

    private int execute(@NotNull final CommandContext<CommandSourceStack> context,
                        @NotNull final ToIntFunction<ICitizenData> action)
    {
        try
        {
            if (!checkPreCondition(context))
            {
                return 0;
            }

            if (!context.getSource().hasPermission(OP_PERM_LEVEL) && !MineColonies.getConfig().getServer().canPlayerUseModifyCitizensCommand.get())
            {
                context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_DISABLED_IN_CONFIG), true);
                return 0;
            }

            if (context.getSource().source instanceof MinecraftServer)
            {
                // server console allowed
            }
            else if (context.getSource().isPlayer() && context.getSource().getPlayer().isCreative())
            {
                // creative mode allowed (if passed above checks)
            }
            else
            {
                context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_REQUIRES_CREATIVE), true);
                return 0;
            }

            final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
            if (colony == null)
            {
                context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND, colonyID), true);
                return 0;
            }

            final ICitizenData citizenData = colony.getCitizenManager().getCivilian(IntegerArgumentType.getInteger(context, CITIZENID_ARG));
            if (citizenData == null)
            {
                context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_NOT_FOUND), true);
                return 0;
            }

            return action.applyAsInt(citizenData);
        }
        catch (Throwable e)
        {
            Log.getLogger().warn("Error during running command:", e);
            return 0;
        }
    }
}
