package com.minecolonies.core.commands.citizencommands;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.ai.statemachine.AIOneTimeEventTarget;
import com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.core.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAIBasic;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Optional;

import static com.minecolonies.core.commands.CommandArgumentNames.*;

/**
 * Forces a citizen to walk to a chosen position..
 */
public class CommandCitizenTriggerWalkTo implements IMCColonyOfficerCommand
{

    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {

        final Entity sender = context.getSource().getEntity();
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, sender == null ? Level.OVERWORLD : context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(() -> Component.translatableEscape(CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND, colonyID), true);
            return 0;
        }

        final ICitizenData citizenData = colony.getCitizenManager().getCivilian(IntegerArgumentType.getInteger(context, CITIZENID_ARG));

        if (citizenData == null)
        {
            context.getSource().sendSuccess(() -> Component.translatableEscape(CommandTranslationConstants.COMMAND_CITIZEN_NOT_FOUND), true);
            return 0;
        }

        final Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getEntity();

        if (!optionalEntityCitizen.isPresent())
        {
            context.getSource().sendSuccess(() -> Component.translatableEscape(CommandTranslationConstants.COMMAND_CITIZEN_NOT_LOADED), true);
            return 0;
        }

        final AbstractEntityCitizen entityCitizen = optionalEntityCitizen.get();
        final Coordinates targetLocation = Vec3Argument.getCoordinates(context, POS_ARG);
        final BlockPos targetPos = targetLocation.getBlockPos(context.getSource());

        if (context.getSource().getLevel() == entityCitizen.level())
        {
            if (entityCitizen instanceof EntityCitizen && entityCitizen.getCitizenJobHandler().getColonyJob() != null)
            {
                final AbstractEntityAIBasic basic = ((AbstractEntityAIBasic) entityCitizen.getCitizenJobHandler().getColonyJob().getWorkerAI());
                basic.setWalkTo(targetPos);
                basic.registerTarget(new AIOneTimeEventTarget(AIWorkerState.WALK_TO));
            }
            else
            {
                entityCitizen.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1f);
            }
        }

        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "walk";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(CITIZENID_ARG, IntegerArgumentType.integer(1))
                                 .then(IMCCommand.newArgument(POS_ARG, Vec3Argument.vec3())
                                         .executes(this::checkPreConditionAndExecute))));
    }
}
