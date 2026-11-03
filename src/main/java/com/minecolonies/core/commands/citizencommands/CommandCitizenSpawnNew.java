package com.minecolonies.core.commands.citizencommands;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.eventbus.events.colony.citizens.CitizenAddedModEvent;
import com.minecolonies.core.commands.arguments.ColonyIdArgument;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_CITIZEN_SPAWN_SUCCESS;
import static com.minecolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

/**
 * Force-spawns a new citizen in the given colony.
 */
public class CommandCitizenSpawnNew implements IMCOPCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final IColony colony = ColonyIdArgument.getColony(context, COLONYID_ARG);
        final ICitizenData newCitizen = colony.getCitizenManager().spawnOrCreateCivilian(null, colony.getWorld(), new ArrayList<>(), true);
        context.getSource().sendSuccess(() -> Component.translatable(COMMAND_CITIZEN_SPAWN_SUCCESS, newCitizen.getName()), true);

        IMinecoloniesAPI.getInstance().getEventBus().post(new CitizenAddedModEvent(newCitizen, CitizenAddedModEvent.CitizenAddedSource.COMMANDS));
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "spawnNew";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, ColonyIdArgument.id()).executes(this::checkPreConditionAndExecute));
    }
}
