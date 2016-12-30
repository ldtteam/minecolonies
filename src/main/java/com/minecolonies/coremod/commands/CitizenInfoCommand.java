package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Show info of one citizen.
 */
public class CitizenInfoCommand extends AbstractSingleCommand
{
    public static final  String       DESC                            = "citizenInfo";
    private static final String       CITIZEN_DESCRIPTION             = "§2ID: §f %d §2 Name: §f %s";
    private static final String       NO_COLONY_CITIZEN_FOUND_MESSAGE = "No citizen %d found in colony %d.";
    private static final String       CITIZEN_DATA_NULL               = "Couldn't find citizen client side representation of %d in %d";
    private static final String       ENTITY_CITIZEN_NULL             = "Couldn't find entity of %d in %d";
    private static final String       COLONY_NULL                     = "Couldn't find colony %d";
    private static final String       CITIZEN_LEVEL_AND_AGE           = "§2Level: §f%s §2Age: §f%s §2Experience: §f%s";
    private static final String       CITIZEN_SKILLS                  = "§2Charisma: §f%s §2Dexterity: §f%s §2Endurance: §f%s\n§2Intelligence: §f%s §2Strength: §f%s";
    private static final String       CITIZEN_JOB                     = "§2Job: §f%s";
    private static final String       CITIZEN_JOB_NULL                = "§2Job: §fUnemployed";
    private static final String       CITIZEN_HEALTH                  = "§2Health: §f%s §2Max Health: §f%s";
    private static final String       CITIZEN_DESIRED_ACTIVITY        = "§2Desired activity: §f%s §2Current Activity: §f%s";
    private static final String       CITIZEN_HOME_POSITION           = "§2Home position: §4x=§f%s §4y=§f%s §4z=§f%s";
    private static final String       CITIZEN_WORK_POSITION           = "§2Work position: §4x=§f%s §4y=§f%s §4z=§f%s";
    private static final String       CITIZEN_POSITION                = "§2Citizen position: §4x=§f%s §4y=§f%s §4z=§f%s";
    private static final String       CITIZEN_WORK_POSITION_NULL      = "§2Work position: §4No work position found!";
    private static final String       CITIZEN_NO_ACTIVITY             = "§4No activity is being desired or executed!";



    /**
     * Initialize this SubCommand with it's parents.
     *
     * @param parents an array of all the parents.
     */
    public CitizenInfoCommand(@NotNull final String... parents)
    {
        super(parents);
    }

    @NotNull
    @Override
    public String getCommandUsage(@NotNull final ICommandSender sender)
    {
        return super.getCommandUsage(sender) + "<ColonyId> <CitizenId>";
    }

    @Override
    public void execute(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender, @NotNull final String... args) throws CommandException
    {
        final int colonyId = GetColonyAndCitizen.getColonyId(sender, args);
        final int citizenId = GetColonyAndCitizen.getCitizenId(sender, args);
        if (colonyId == -1 && citizenId == -1)
        {
            sender.addChatMessage(new TextComponentString(String.format(NO_COLONY_CITIZEN_FOUND_MESSAGE,
              citizenId,
              colonyId)));
            return;
        }
        //Wasn't able to get the citizen from the colony.
        final Colony colony = ColonyManager.getColony(colonyId);
        if (colony == null)
        {
            sender.addChatMessage(new TextComponentString(String.format(COLONY_NULL, colonyId)));
            return;
        }
        final CitizenData citizenData = colony.getCitizen(citizenId);
        if (citizenData == null)
        {
            sender.addChatMessage(new TextComponentString(String.format(CITIZEN_DATA_NULL,
              citizenId,
              colonyId)));
            return;
        }
        //Wasn't able to get the entity from the citizenData.
        final EntityCitizen entityCitizen = citizenData.getCitizenEntity();
        if (entityCitizen == null)
        {
            sender.addChatMessage(new TextComponentString(String.format(ENTITY_CITIZEN_NULL,
              citizenId,
              colonyId)));
            return;
        }
        sender.addChatMessage(new TextComponentString(String.format(CITIZEN_DESCRIPTION,
          entityCitizen.getEntityId(),
          entityCitizen.getName())));
        final BlockPos citizenPosition = entityCitizen.getPosition();
        sender.addChatMessage(new TextComponentString(String.format(CITIZEN_POSITION,
          citizenPosition.getX(),
          citizenPosition.getY(),
          citizenPosition.getZ())));
        final BlockPos homePosition = entityCitizen.getHomePosition();
        sender.addChatMessage(new TextComponentString(String.format(CITIZEN_HOME_POSITION,
          homePosition.getX(),
          homePosition.getY(),
          homePosition.getZ())));
        if (entityCitizen.getWorkBuilding() == null)
        {
            sender.addChatMessage(new TextComponentString(String.format(CITIZEN_WORK_POSITION_NULL)));
        }
        else
        {
            final BlockPos workingPosition = entityCitizen.getWorkBuilding().getLocation();
            sender.addChatMessage(new TextComponentString(String.format(CITIZEN_WORK_POSITION,
              workingPosition.getX(),
              workingPosition.getY(),
              workingPosition.getZ())));
        }
        sender.addChatMessage(new TextComponentString(String.format(CITIZEN_HEALTH,
          entityCitizen.getHealth(),
          entityCitizen.getMaxHealth())));
        sender.addChatMessage(new TextComponentString(String.format(CITIZEN_LEVEL_AND_AGE,
          entityCitizen.getLevel(),
          entityCitizen.getAge(),
          entityCitizen.getExperienceLevel())));
        sender.addChatMessage(new TextComponentString(String.format(CITIZEN_SKILLS,
          entityCitizen.getCharisma(),
          entityCitizen.getDexterity(),
          entityCitizen.getEndurance(),
          entityCitizen.getIntelligence(),
          entityCitizen.getStrength())));
        if (entityCitizen.getColonyJob() == null)
        {
            sender.addChatMessage(new TextComponentString(String.format(CITIZEN_JOB_NULL)));
            sender.addChatMessage(new TextComponentString(String.format(CITIZEN_NO_ACTIVITY)));
        }
        else
        {
            sender.addChatMessage(new TextComponentString(String.format(CITIZEN_JOB, entityCitizen.getWorkBuilding().getJobName())));
            sender.addChatMessage(new TextComponentString(String.format(CITIZEN_DESIRED_ACTIVITY,
              entityCitizen.getDesiredActivity(),
              entityCitizen.getColonyJob().getNameTagDescription())));
        }
    }

    @NotNull
    @Override
    public List<String> getTabCompletionOptions(
                                                 @NotNull final MinecraftServer server,
                                                 @NotNull final ICommandSender sender,
                                                 @NotNull final String[] args,
                                                 @Nullable final BlockPos pos)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(@NotNull final String[] args, final int index)
    {
        return false;
    }
}

