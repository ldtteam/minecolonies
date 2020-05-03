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
import net.minecraft.entity.Entity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

import static com.minecolonies.coremod.commands.CommandArgumentNames.CITIZENID_ARG;
import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;

/**
 * Displays information about a chosen citizen in a chosen colony.
 */
public class CommandCitizenInfo implements IMCColonyOfficerCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {

        final Entity sender = context.getSource().getEntity();
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, sender == null ? 0 : context.getSource().getWorld().dimension.getType().getId());
        if (colony == null)
        {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.colonyidnotfound", colonyID), true);
            return 0;
        }

        final ICitizenData citizenData = colony.getCitizenManager().getCitizen(IntegerArgumentType.getInteger(context, CITIZENID_ARG));

        if (citizenData == null)
        {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.notfound"), true);
            return 0;
        }

        final Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getCitizenEntity();

        if (!optionalEntityCitizen.isPresent())
        {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.notloaded"), true);
            return 0;
        }

        context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.desc", citizenData.getId(), citizenData.getName()), true);

        final AbstractEntityCitizen entityCitizen = optionalEntityCitizen.get();

        final BlockPos citizenPosition = entityCitizen.getPosition();
        context.getSource().sendFeedback(LanguageHandler.buildChatComponent(
          "com.minecolonies.command.citizeninfo.pos",
          citizenPosition.getX(),
          citizenPosition.getY(),
          citizenPosition.getZ()), true);
        final BlockPos homePosition = entityCitizen.getHomePosition();
        context.getSource()
          .sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.homepos", homePosition.getX(), homePosition.getY(), homePosition.getZ()), true);

        if (entityCitizen.getCitizenColonyHandler().getWorkBuilding() == null)
        {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.workposnull"), true);
        }
        else
        {
            final BlockPos workingPosition = entityCitizen.getCitizenColonyHandler().getWorkBuilding().getPosition();
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent(
              "com.minecolonies.command.citizeninfo.workpos",
              workingPosition.getX(),
              workingPosition.getY(),
              workingPosition.getZ()), true);
        }

        context.getSource().sendFeedback(LanguageHandler.buildChatComponent( "com.minecolonies.command.citizeninfo.health", entityCitizen.getHealth(), entityCitizen.getMaxHealth()), true);
        context.getSource().sendFeedback(LanguageHandler.buildChatComponent( "com.minecolonies.command.citizeninfo.skills",
          citizenData.getCitizenSkillHandler().getSkills().values().stream().map(Tuple::getA).toArray()), true);

        if (entityCitizen.getCitizenJobHandler().getColonyJob() == null)
        {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.jobnull"), true);
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.noactivity"), true);
        }
        else if (entityCitizen.getCitizenColonyHandler().getWorkBuilding() != null)
        {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent(
              "com.minecolonies.command.citizeninfo.job",
              entityCitizen.getCitizenColonyHandler().getWorkBuilding().getJobName()), true);
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent( "com.minecolonies.command.citizeninfo.activity",
              entityCitizen.getDesiredActivity(),
              entityCitizen.getCitizenJobHandler().getColonyJob().getNameTagDescription(),
              entityCitizen.goalSelector.getRunningGoals().findFirst().get().getGoal().toString()),
              true);
        }

        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "info";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(CITIZENID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute)));
    }
}
