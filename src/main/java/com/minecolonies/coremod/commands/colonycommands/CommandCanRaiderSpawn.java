package com.minecolonies.coremod.commands.colonycommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;

public class CommandCanRaiderSpawn implements IMCOPCommand
{
    private static final String CANSPAWN_ARG = "canSpawn";

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

        final boolean canHaveBarbEvents = BoolArgumentType.getBool(context,CANSPAWN_ARG);

        colony.getRaiderManager().setCanHaveRaiderEvents(canHaveBarbEvents);
        colony.markDirty();
        LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.canspawnraider.success", colony.getName(),canHaveBarbEvents);
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "canSpawnRaiders";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(CANSPAWN_ARG, BoolArgumentType.bool()).executes(this::checkPreConditionAndExecute)));
    }
}
