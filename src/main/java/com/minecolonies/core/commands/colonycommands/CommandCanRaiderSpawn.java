package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.core.commands.arguments.ColonyIdArgument;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_CAN_RAIDER_SPAWN_SUCCESS;
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
        final IColony colony = ColonyIdArgument.getColony(context, COLONYID_ARG);
        final boolean canHaveBarbEvents = BoolArgumentType.getBool(context, CANSPAWN_ARG);

        colony.getRaiderManager().setCanHaveRaiderEvents(canHaveBarbEvents);
        colony.markDirty();
        context.getSource().sendSuccess(() -> Component.translatable(COMMAND_CAN_RAIDER_SPAWN_SUCCESS, colony.getName(), canHaveBarbEvents), true);
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
                 .then(IMCCommand.newArgument(COLONYID_ARG, ColonyIdArgument.id())
                         .then(IMCCommand.newArgument(CANSPAWN_ARG, BoolArgumentType.bool()).executes(this::checkPreConditionAndExecute)));
    }
}
