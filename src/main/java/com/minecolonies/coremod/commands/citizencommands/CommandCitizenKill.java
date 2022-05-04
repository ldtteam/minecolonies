package com.minecolonies.coremod.commands.citizencommands;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Optional;

import static com.minecolonies.coremod.commands.CommandArgumentNames.CITIZENID_ARG;
import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;

/**
 * Kills the given citizen.
 */
public class CommandCitizenKill implements IMCColonyOfficerCommand
{

    /**
     * The damage source used to kill citizens.
     */
    private static final DamageSource CONSOLE_DAMAGE_SOURCE = new DamageSource("Console");

    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(new TranslationTextComponent(CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND, colonyID), true);
            return 0;
        }

        if (!context.getSource().hasPermission(OP_PERM_LEVEL) && !MineColonies.getConfig().getServer().canPlayerUseKillCitizensCommand.get())
        {
            context.getSource().sendSuccess(new TranslationTextComponent(CommandTranslationConstants.COMMAND_DISABLED_IN_CONFIG), true);
            return 0;
        }

        final ICitizenData citizenData = colony.getCitizenManager().getCivilian(IntegerArgumentType.getInteger(context, CITIZENID_ARG));

        if (citizenData == null)
        {
            context.getSource().sendSuccess(new TranslationTextComponent(CommandTranslationConstants.COMMAND_CITIZEN_NOT_FOUND), true);
            return 0;
        }

        final Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getEntity();

        if (!optionalEntityCitizen.isPresent())
        {
            context.getSource().sendSuccess(new TranslationTextComponent(CommandTranslationConstants.COMMAND_CITIZEN_NOT_LOADED), true);
            return 0;
        }

        context.getSource().sendSuccess(new TranslationTextComponent(CommandTranslationConstants.COMMAND_CITIZEN_INFO, citizenData.getId(), citizenData.getName()), true);
        final BlockPos position = optionalEntityCitizen.get().blockPosition();
        context.getSource().sendSuccess(new TranslationTextComponent(CommandTranslationConstants.COMMAND_CITIZEN_INFO_POSITION, position.getX(), position.getY(), position.getZ()), true);
        context.getSource()
          .sendSuccess(new TranslationTextComponent(CommandTranslationConstants.COMMAND_CITIZEN_KILL_SUCCESS, position.getX(), position.getY(), position.getZ()), true);

        optionalEntityCitizen.get().die(CONSOLE_DAMAGE_SOURCE);

        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "kill";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(CITIZENID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute)));
    }
}
