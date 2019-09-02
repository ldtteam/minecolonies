package com.minecolonies.coremod.commands.colonycommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IChunkmanagerCapability;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.minecolonies.coremod.util.ChunkDataHelper;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import static com.minecolonies.api.util.constant.ColonyManagerConstants.UNABLE_TO_FIND_WORLD_CAP_TEXT;
import static com.minecolonies.api.util.constant.CommandConstants.CHUNKS_TO_CLAM_THRESHOLD;
import static com.minecolonies.coremod.MineColonies.CHUNK_STORAGE_UPDATE_CAP;
import static com.minecolonies.coremod.commands.CommandArgumentNames.ADD_ARG;
import static com.minecolonies.coremod.commands.CommandArgumentNames.COLONYID_ARG;
import static com.minecolonies.coremod.commands.CommandArgumentNames.RANGE_ARG;

public class CommandClaimChunks implements IMCOPCommand
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

        // Range
        final int range = IntegerArgumentType.getInteger(context, RANGE_ARG);
        if (range > MineColonies.getConfig().getCommon().workingRangeTownHallChunks.get() * 2)
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.claim.toolarge", colonyID);
            return 0;
        }

        // Added/removed
        final boolean add = BoolArgumentType.getBool(context, ADD_ARG);

        final IChunkmanagerCapability chunkManager = sender.world.getCapability(CHUNK_STORAGE_UPDATE_CAP, null).orElse(null);
        if (chunkManager == null)
        {
            Log.getLogger().error(UNABLE_TO_FIND_WORLD_CAP_TEXT);
            return 0;
        }

        if (chunkManager.getAllChunkStorages().size() > CHUNKS_TO_CLAM_THRESHOLD)
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.claim.maxchunks");
            return 0;
        }

        ChunkDataHelper.claimChunksInRange(colonyID, sender.dimension.getId(), add, sender.getPosition(), range, 0, sender.world);
        LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.claim.success");
        return 1;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "claim";
    }

    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(RANGE_ARG, IntegerArgumentType.integer(1))
                                 .then(IMCCommand.newArgument(ADD_ARG, BoolArgumentType.bool()).executes(this::checkPreConditionAndExecute))));
    }
}
