package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.commandTypes.IMCColonyOfficerCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND;
import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_OWNER_CHANGE_SUCCESS;
import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;

public class CommandSetAbandoned implements IMCColonyOfficerCommand
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

        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, context.getSource().getLevel().dimension());
        if (colony == null)
        {
            context.getSource().sendSuccess(new TranslationTextComponent(COMMAND_COLONY_ID_NOT_FOUND, colonyID), true);
            return 0;
        }

        boolean addOfficer = false;
        if (sender != null && (colony.getPermissions().getRank((PlayerEntity) sender).isColonyManager()))
        {
            addOfficer = true;
        }

        colony.getPermissions().setOwnerAbandoned();

        if (addOfficer)
        {
            colony.getPermissions().addPlayer(((PlayerEntity) sender).getGameProfile(), colony.getPermissions().getRankOfficer());
        }

        context.getSource().sendSuccess(new TranslationTextComponent(COMMAND_OWNER_CHANGE_SUCCESS, "[abandoned]", colony.getName()), true);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "setAbandoned";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1)).executes(this::checkPreConditionAndExecute));
    }
}
