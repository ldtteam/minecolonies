package com.minecolonies.coremod.commands.colonycommands;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IChunkmanagerCapability;
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
import net.minecraft.util.math.BlockPos;

import static com.minecolonies.api.util.constant.ColonyManagerConstants.UNABLE_TO_FIND_WORLD_CAP_TEXT;
import static com.minecolonies.api.util.constant.Constants.CHUNKS_TO_CLAIM_THRESHOLD;
import static com.minecolonies.coremod.MineColonies.CHUNK_STORAGE_UPDATE_CAP;
import static com.minecolonies.coremod.commands.CommandArgumentNames.*;

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

        if (!(sender instanceof PlayerEntity))
        {
            return 0;
        }

        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);

        // Range
        final int range = IntegerArgumentType.getInteger(context, RANGE_ARG);
        if (range > MineColonies.getConfig().getServer().maxColonySize.get())
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.claim.toolarge", colonyID);
            return 0;
        }

        // Added/removed
        final boolean add = BoolArgumentType.getBool(context, ADD_ARG);

        final IChunkmanagerCapability chunkManager = sender.level.getCapability(CHUNK_STORAGE_UPDATE_CAP, null).resolve().orElse(null);
        if (chunkManager == null)
        {
            Log.getLogger().error(UNABLE_TO_FIND_WORLD_CAP_TEXT, new Exception());
            return 0;
        }

        if (chunkManager.getAllChunkStorages().size() > CHUNKS_TO_CLAIM_THRESHOLD)
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.claim.maxchunks");
            return 0;
        }

        ChunkDataHelper.claimChunksInRange(colonyID, context.getSource().getLevel().dimension(), add, new BlockPos(sender.position()), range, 0, sender.level);
        if(add)
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.claim.success");
        }
        else
        {
            LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.command.claim.unclaim");
        }
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
                         .then(IMCCommand.newArgument(RANGE_ARG, IntegerArgumentType.integer(0, 10))
                                 .then(IMCCommand.newArgument(ADD_ARG, BoolArgumentType.bool()).executes(this::checkPreConditionAndExecute))));
    }
}
