package com.minecolonies.coremod.commands.citizencommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

import static com.minecolonies.coremod.commands.CommandArgumentNames.*;

/**
 * Forces a citizen to walk to a chosen position..
 */
public class CommandCitizenTriggerWalkTo implements IMCColonyOfficerCommand {

    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context) {

        final Entity sender = context.getSource().getEntity();
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, sender == null ? 0 : context.getSource().getWorld().dimension.getType().getId());
        if (colony == null) {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.colonyidnotfound", colonyID), true);
            return 0;
        }

        final ICitizenData citizenData = colony.getCitizenManager().getCitizen(IntegerArgumentType.getInteger(context, CITIZENID_ARG));

        if (citizenData == null) {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.notfound"), true);
            return 0;
        }

        final Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getCitizenEntity();

        if (!optionalEntityCitizen.isPresent()) {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.notloaded"), true);
            return 0;
        }

        final AbstractEntityCitizen entityCitizen = optionalEntityCitizen.get();
        final ILocationArgument targetLocation = Vec3Argument.getLocation(context, POS_ARG);
        final BlockPos targetPos = targetLocation.getBlockPos(context.getSource());

        if (context.getSource().getWorld() == entityCitizen.world) {
            entityCitizen.getNavigator().tryMoveToXYZ(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1f);
        }

        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName() {
        return "walk";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build() {
        return IMCCommand.newLiteral(getName())
                .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                        .then(IMCCommand.newArgument(CITIZENID_ARG, IntegerArgumentType.integer(1))
                                .then(IMCCommand.newArgument(POS_ARG, Vec3Argument.vec3())
                                        .executes(executePreConditionCheck().then(this::onExecute)))));
    }
}
