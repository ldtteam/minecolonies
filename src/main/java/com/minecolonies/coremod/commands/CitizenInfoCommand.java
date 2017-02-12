package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.CITIZENINFO;

/**
 * Show info of one citizen.
 */
public class CitizenInfoCommand extends AbstractCitizensCommands
{
    public static final  String       DESC                            = "citizenInfo";
    private static final String       CITIZEN_DESCRIPTION             = "§2ID: §f %d §2 Name: §f %s";
    private static final String       CITIZEN_LEVEL_AND_AGE           = "§2Level: §f%s §2Age: §f%s §2Experience: §f%s";
    private static final String       CITIZEN_SKILLS                  = "§2Charisma: §f%s §2Dexterity: §f%s §2Endurance: §f%s\n§2Intelligence: §f%s §2Strength: §f%s";
    private static final String       CITIZEN_JOB                     = "§2Job: §f%s";
    private static final String       CITIZEN_JOB_NULL                = "§2Job: §fUnemployed";
    private static final String       CITIZEN_HEALTH                  = "§2Health: §f%s §2Max Health: §f%s";
    private static final String       CITIZEN_DESIRED_ACTIVITY        = "§2Desired activity: §f%s §2Current Activity: §f%s";
    private static final String       CITIZEN_HOME_POSITION           = "§2Home position: §4x=§f%s §4y=§f%s §4z=§f%s";
    private static final String       CITIZEN_WORK_POSITION      = "§2Work position: §4x=§f%s §4y=§f%s §4z=§f%s";
    private static final String       CITIZEN_POSITION           = "§2Citizen position: §4x=§f%s §4y=§f%s §4z=§f%s";
    private static final String       CITIZEN_WORK_POSITION_NULL = "§2Work position: §4No work position found!";
    private static final String       CITIZEN_NO_ACTIVITY        = "§4No activity is being desired or executed!";
    private static final String       CITIZEN_NOT_LOADED         = "Citizen entity not loaded!";

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
    void executeSpecializedCode(@NotNull final MinecraftServer server, final ICommandSender sender, final Colony colony, final int citizenId)
    {
        final CitizenData citizenData = colony.getCitizen(citizenId);
        final EntityCitizen entityCitizen = citizenData.getCitizenEntity();
        sender.sendMessage(new TextComponentString(String.format(CITIZEN_DESCRIPTION,
                citizenData.getId(),
                citizenData.getName())));
        if (entityCitizen == null)
        {
            sender.sendMessage(new TextComponentTranslation(CITIZEN_NOT_LOADED));
            return;
        }

        final BlockPos citizenPosition = entityCitizen.getPosition();
        sender.sendMessage(new TextComponentString(String.format(CITIZEN_POSITION,
                citizenPosition.getX(),
                citizenPosition.getY(),
                citizenPosition.getZ())));
        final BlockPos homePosition = entityCitizen.getHomePosition();
        sender.sendMessage(new TextComponentString(String.format(CITIZEN_HOME_POSITION,
                homePosition.getX(),
                homePosition.getY(),
                homePosition.getZ())));
        if (entityCitizen.getWorkBuilding() == null)
        {
            sender.sendMessage(new TextComponentString(String.format(CITIZEN_WORK_POSITION_NULL)));
        }
        else
        {
            final BlockPos workingPosition = entityCitizen.getWorkBuilding().getLocation();
            sender.sendMessage(new TextComponentString(String.format(CITIZEN_WORK_POSITION,
                    workingPosition.getX(),
                    workingPosition.getY(),
                    workingPosition.getZ())));
        }

        sender.sendMessage(new TextComponentString(String.format(CITIZEN_HEALTH,
                entityCitizen.getHealth(),
                entityCitizen.getMaxHealth())));
        sender.sendMessage(new TextComponentString(String.format(CITIZEN_LEVEL_AND_AGE,
                entityCitizen.getLevel(),
                entityCitizen.getAge(),
                entityCitizen.getExperienceLevel())));
        sender.sendMessage(new TextComponentString(String.format(CITIZEN_SKILLS,
                entityCitizen.getCharisma(),
                entityCitizen.getDexterity(),
                entityCitizen.getEndurance(),
                entityCitizen.getIntelligence(),
                entityCitizen.getStrength())));
        if (entityCitizen.getColonyJob() == null)
        {
            sender.sendMessage(new TextComponentString(String.format(CITIZEN_JOB_NULL)));
            sender.sendMessage(new TextComponentString(String.format(CITIZEN_NO_ACTIVITY)));
        }
        else if(entityCitizen.getWorkBuilding() != null)
        {
            sender.sendMessage(new TextComponentString(String.format(CITIZEN_JOB, entityCitizen.getWorkBuilding().getJobName())));
            sender.sendMessage(new TextComponentString(String.format(CITIZEN_DESIRED_ACTIVITY,
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

    @Override
    public Commands getCommand()
    {
        return CITIZENINFO;
    }
}

