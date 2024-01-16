package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_CAN_RAIDER_SPAWN_SUCCESS;
import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND;
import static com.minecolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

public class CommandCanRaiderSpawn implements IMCOPCommand
{
    private static final String CANSPAWN_ARG = "canSpawn";

    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(Component.translatable(COMMAND_COLONY_ID_NOT_FOUND, colonyID), true);
            return 0;
        }

        final boolean canHaveBarbEvents = BoolArgumentType.getBool(context, CANSPAWN_ARG);

        colony.getRaiderManager().setCanHaveRaiderEvents(canHaveBarbEvents);
        colony.markDirty();
        context.getSource().sendSuccess(Component.translatable(COMMAND_CAN_RAIDER_SPAWN_SUCCESS, colony.getName(), canHaveBarbEvents), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "canSpawnRaiders";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(CANSPAWN_ARG, BoolArgumentType.bool()).executes(this::checkPreConditionAndExecute)));
    }
}
