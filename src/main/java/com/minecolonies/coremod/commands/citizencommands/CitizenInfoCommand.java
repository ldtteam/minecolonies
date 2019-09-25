package com.minecolonies.coremod.commands.citizencommands;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.minecolonies.coremod.commands.AbstractSingleCommand.Commands.CITIZENINFO;

/**
 * Show info of one citizen.
 */
public class CitizenInfoCommand extends AbstractCitizensCommands
{
    public static final  String DESC                       = "info";
    private static final String CITIZEN_DESCRIPTION        = "§2ID: §f %d §2 Name: §f %s";
    private static final String CITIZEN_LEVEL_AND_AGE      = "§2Level: §f%s §2Age: §f%s §2Experience: §f%s";
    private static final String CITIZEN_SKILLS             = "§2Charisma: §f%s §2Dexterity: §f%s §2Endurance: §f%s\n§2Intelligence: §f%s §2Strength: §f%s";
    private static final String CITIZEN_JOB                = "§2Job: §f%s";
    private static final String CITIZEN_JOB_NULL           = "§2Job: §fUnemployed";
    private static final String CITIZEN_HEALTH             = "§2Health: §f%s §2Max Health: §f%s";
    private static final String CITIZEN_DESIRED_ACTIVITY   = "§2Desired activity: §f%s §2Current Activity: §f%s";
    private static final String CITIZEN_HOME_POSITION      = "§2Home position: §4x=§f%s §4y=§f%s §4z=§f%s";
    private static final String CITIZEN_WORK_POSITION      = "§2Work position: §4x=§f%s §4y=§f%s §4z=§f%s";
    private static final String CITIZEN_POSITION           = "§2Citizen position: §4x=§f%s §4y=§f%s §4z=§f%s";
    private static final String CITIZEN_WORK_POSITION_NULL = "§2Work position: §4No work position found!";
    private static final String CITIZEN_NO_ACTIVITY        = "§4No activity is being desired or executed!";
    private static final String CITIZEN_NOT_LOADED         = "Citizen entity not loaded!";

    /**
     * no-args constructor called by new CommandEntryPoint executer.
     */
    public CitizenInfoCommand()
    {
        super();
    }

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
    public void executeSpecializedCode(@NotNull final MinecraftServer server, final ICommandSender sender, final IColony colony, final int citizenId)
    {
        final ICitizenData citizenData = colony.getCitizenManager().getCitizen(citizenId);
        sender.sendMessage(new TextComponentString(String.format(CITIZEN_DESCRIPTION,
          citizenData.getId(),
          citizenData.getName())));

        final Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getCitizenEntity();

        if (!optionalEntityCitizen.isPresent())
        {
            sender.sendMessage(new TextComponentTranslation(CITIZEN_NOT_LOADED));
            return;
        }


        final AbstractEntityCitizen entityCitizen = optionalEntityCitizen.get();

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
        if (entityCitizen.getCitizenColonyHandler().getWorkBuilding() == null)
        {
            sender.sendMessage(new TextComponentString(CITIZEN_WORK_POSITION_NULL));
        }
        else
        {
            final BlockPos workingPosition = entityCitizen.getCitizenColonyHandler().getWorkBuilding().getPosition();
            sender.sendMessage(new TextComponentString(String.format(CITIZEN_WORK_POSITION,
              workingPosition.getX(),
              workingPosition.getY(),
              workingPosition.getZ())));
        }

        sender.sendMessage(new TextComponentString(String.format(CITIZEN_HEALTH,
          entityCitizen.getHealth(),
          entityCitizen.getMaxHealth())));

        sender.sendMessage(new TextComponentString(String.format(CITIZEN_LEVEL_AND_AGE,
                entityCitizen.getCitizenExperienceHandler().getLevel(),
                entityCitizen.getGrowingAge(),
                citizenData.getLevel())));
        sender.sendMessage(new TextComponentString(String.format(CITIZEN_SKILLS,
                citizenData.getCharisma(),
                citizenData.getDexterity(),
                citizenData.getEndurance(),
                citizenData.getIntelligence(),
                citizenData.getStrength())));
        if (entityCitizen.getCitizenJobHandler().getColonyJob() == null)
        {
            sender.sendMessage(new TextComponentString(CITIZEN_JOB_NULL));
            sender.sendMessage(new TextComponentString(CITIZEN_NO_ACTIVITY));
        }
        else if (entityCitizen.getCitizenColonyHandler().getWorkBuilding() != null)
        {
            sender.sendMessage(new TextComponentString(String.format(CITIZEN_JOB, entityCitizen.getCitizenColonyHandler().getWorkBuilding().getJobName())));
            sender.sendMessage(new TextComponentString(String.format(CITIZEN_DESIRED_ACTIVITY,
              entityCitizen.getDesiredActivity(),
              entityCitizen.getCitizenJobHandler().getColonyJob().getNameTagDescription())));
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

