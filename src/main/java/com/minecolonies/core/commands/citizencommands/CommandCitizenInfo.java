package com.minecolonies.core.commands.citizencommands;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.Optional;

import static com.minecolonies.core.commands.CommandArgumentNames.CITIZENID_ARG;
import static com.minecolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

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
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {

        final Entity sender = context.getSource().getEntity();
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, sender == null ? Level.OVERWORLD : context.getSource().getLevel().dimension());
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

        final Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getEntity();

        if (optionalEntityCitizen.isPresent())
        {
            final AbstractEntityCitizen entityCitizen = optionalEntityCitizen.get();

            final BlockPos citizenPosition = entityCitizen.blockPosition();
            context.getSource()
              .sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_INFO_POSITION,
                citizenPosition.getX(),
                citizenPosition.getY(),
                citizenPosition.getZ()), true);
            final BlockPos homePosition = citizenData.getHomePosition();
            context.getSource()
              .sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_INFO_HOME_POSITION,
                homePosition.getX(),
                homePosition.getY(),
                homePosition.getZ()), true);

            context.getSource()
              .sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_INFO_HEALTH, entityCitizen.getHealth(), entityCitizen.getMaxHealth()), true);
        }
        else
        {
            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_INFO_POSITION,
              citizenData.getLastPosition().getX(),
              citizenData.getLastPosition().getY(),
              citizenData.getLastPosition().getZ()), true);

            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_NOT_LOADED), true);
        }

        context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_INFO, citizenData.getId(), citizenData.getName()), true);

        final AbstractEntityCitizen entityCitizen = optionalEntityCitizen.get();

        final BlockPos citizenPosition = entityCitizen.blockPosition();
        context.getSource()
          .sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_INFO_POSITION,
            citizenPosition.getX(),
            citizenPosition.getY(),
            citizenPosition.getZ()), true);
        final BlockPos homePosition = citizenData.getHomePosition();
        context.getSource()
          .sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_INFO_HOME_POSITION, homePosition.getX(), homePosition.getY(), homePosition.getZ()),
            true);

        if (citizenData.getWorkBuilding() == null)
        {
            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_INFO_NO_WORKING_POSITION), true);
        }
        else
        {
            final BlockPos workingPosition = citizenData.getWorkBuilding().getPosition();
            context.getSource()
              .sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_INFO_WORKING_POSITION,
                workingPosition.getX(),
                workingPosition.getY(),
                workingPosition.getZ()), true);
        }


        Object[] skills =
          new Object[] {citizenData.getCitizenSkillHandler().getSkills().get(Skill.Athletics).getLevel(),
            citizenData.getCitizenSkillHandler().getSkills().get(Skill.Dexterity).getLevel(),
            citizenData.getCitizenSkillHandler().getSkills().get(Skill.Strength).getLevel(),
            citizenData.getCitizenSkillHandler().getSkills().get(Skill.Agility).getLevel(),
            citizenData.getCitizenSkillHandler().getSkills().get(Skill.Stamina).getLevel(),
            citizenData.getCitizenSkillHandler().getSkills().get(Skill.Mana).getLevel(),
            citizenData.getCitizenSkillHandler().getSkills().get(Skill.Adaptability).getLevel(),
            citizenData.getCitizenSkillHandler().getSkills().get(Skill.Focus).getLevel(),
            citizenData.getCitizenSkillHandler().getSkills().get(Skill.Creativity).getLevel(),
            citizenData.getCitizenSkillHandler().getSkills().get(Skill.Knowledge).getLevel(),
            citizenData.getCitizenSkillHandler().getSkills().get(Skill.Intelligence).getLevel()};
        context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_INFO_SKILLS, skills), true);

        if (citizenData.getJob() == null)
        {
            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_INFO_NO_JOB), true);
            context.getSource().sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_INFO_NO_ACTIVITY), true);
        }
        else if (citizenData.getWorkBuilding() != null && citizenData.getWorkBuilding().hasModule(WorkerBuildingModule.class))
        {
            context.getSource()
              .sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_INFO_JOB,
                citizenData.getWorkBuilding().getFirstModuleOccurance(WorkerBuildingModule.class).getJobEntry().getTranslationKey()), true);

            context.getSource()
              .sendSuccess(() -> Component.translatable(CommandTranslationConstants.COMMAND_CITIZEN_INFO_ACTIVITY,
                ((EntityCitizen) entityCitizen).getCitizenAI().getState(),
                entityCitizen.getCitizenJobHandler().getColonyJob().getNameTagDescription(),
                entityCitizen.getCitizenJobHandler().getWorkAI().getState()), true);
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
    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(CITIZENID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute)));
    }
}
