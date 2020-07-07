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
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;

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
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getWorld().dimension.getType().getId());
        if (colony == null)
        {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.colonyidnotfound", colonyID), true);
            return 0;
        }

        if (!context.getSource().hasPermissionLevel(OP_PERM_LEVEL) && !MineColonies.getConfig().getCommon().canPlayerUseKillCitizensCommand.get())
        {
            context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.notenabledinconfig"), true);
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
        final BlockPos position = optionalEntityCitizen.get().getPosition();
        context.getSource().sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.citizeninfo.pos", position.getX(), position.getY(), position.getZ()), true);
        context.getSource()
          .sendFeedback(LanguageHandler.buildChatComponent("com.minecolonies.command.citizenkill.success", position.getX(), position.getY(), position.getZ()), true);

        optionalEntityCitizen.get().onDeath(CONSOLE_DAMAGE_SOURCE);

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
