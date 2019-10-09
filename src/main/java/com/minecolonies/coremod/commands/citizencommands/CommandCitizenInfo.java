package com.minecolonies.coremod.commands.citizencommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
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
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, sender.dimension.getId());
        if (colony == null)
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.colonyidnotfound", colonyID);
            return 0;
        }

        if (!MineColonies.getConfig().getCommon().canPlayerUseCitizenInfoCommand.get())
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.notenabledinconfig");
            return 0;
        }

        final ICitizenData citizenData = colony.getCitizenManager().getCitizen(IntegerArgumentType.getInteger(context, CITIZENID_ARG));

        if (citizenData == null)
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.citizeninfo.notfound");
            return 0;
        }

        final Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getCitizenEntity();

        if (!optionalEntityCitizen.isPresent())
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.citizeninfo.notloaded");
            return 0;
        }

        LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.citizeninfo.desc", citizenData.getId(), citizenData.getName());

        final AbstractEntityCitizen entityCitizen = optionalEntityCitizen.get();

        final BlockPos citizenPosition = entityCitizen.getPosition();
        LanguageHandler.sendPlayerMessage((PlayerEntity) sender,
          "com.minecolonies.command.citizeninfo.pos",
          citizenPosition.getX(),
          citizenPosition.getY(),
          citizenPosition.getZ());
        final BlockPos homePosition = entityCitizen.getHomePosition();
        LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.citizeninfo.homepos", homePosition.getX(), homePosition.getY(), homePosition.getZ());

        if (entityCitizen.getCitizenColonyHandler().getWorkBuilding() == null)
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.citizeninfo.workposnull");
        }
        else
        {
            final BlockPos workingPosition = entityCitizen.getCitizenColonyHandler().getWorkBuilding().getPosition();
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender,
              "com.minecolonies.command.citizeninfo.workpos",
              workingPosition.getX(),
              workingPosition.getY(),
              workingPosition.getZ());
        }

        LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.citizeninfo.health", entityCitizen.getHealth(), entityCitizen.getMaxHealth());
        LanguageHandler.sendPlayerMessage((PlayerEntity) sender,
          "com.minecolonies.command.citizeninfo.levelandage",
          entityCitizen.getCitizenExperienceHandler().getLevel(),
          entityCitizen.getGrowingAge(),
          citizenData.getLevel());
        LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.citizeninfo.skills",
          citizenData.getCharisma(),
          citizenData.getDexterity(),
          citizenData.getEndurance(),
          citizenData.getIntelligence(),
          citizenData.getStrength());

        if (entityCitizen.getCitizenJobHandler().getColonyJob() == null)
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.citizeninfo.jobnull");
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.citizeninfo.noactivity");
        }
        else if (entityCitizen.getCitizenColonyHandler().getWorkBuilding() != null)
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender,
              "com.minecolonies.command.citizeninfo.job",
              entityCitizen.getCitizenColonyHandler().getWorkBuilding().getJobName());
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.citizeninfo.activity",
              entityCitizen.getDesiredActivity(),
              entityCitizen.getCitizenJobHandler().getColonyJob().getNameTagDescription());
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
