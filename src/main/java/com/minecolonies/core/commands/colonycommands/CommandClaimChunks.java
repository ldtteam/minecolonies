package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.api.colony.IChunkmanagerCapability;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.minecolonies.core.util.ChunkDataHelper;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import static com.minecolonies.api.util.constant.ColonyManagerConstants.UNABLE_TO_FIND_WORLD_CAP_TEXT;
import static com.minecolonies.api.util.constant.Constants.CHUNKS_TO_CLAIM_THRESHOLD;
import static com.minecolonies.core.MineColonies.CHUNK_STORAGE_UPDATE_CAP;
import static com.minecolonies.core.commands.CommandArgumentNames.*;

public class CommandClaimChunks implements IMCOPCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final Entity sender = context.getSource().getEntity();

        if (!(sender instanceof Player))
        {
            return 0;
        }

        // Colony
        final int colonyID = IntegerArgumentType.getInteger(context, COLONYID_ARG);

        // Range
        final int range = IntegerArgumentType.getInteger(context, RANGE_ARG);
        if (range > MineColonies.getConfig().getServer().maxColonySize.get())
        {
            MessageUtils.format(CommandTranslationConstants.COMMAND_CLAIM_TOO_LARGE, colonyID).sendTo((Player) sender);
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
            MessageUtils.format(CommandTranslationConstants.COMMAND_CLAIM_MAX_CHUNKS).sendTo((Player) sender);
            return 0;
        }

        ChunkDataHelper.staticClaimInRange(colonyID, add, sender.blockPosition(), range, sender.level, true);
        if (add)
        {
            MessageUtils.format(CommandTranslationConstants.COMMAND_CLAIM_SUCCESS).sendTo((Player) sender);
        }
        else
        {
            MessageUtils.format(CommandTranslationConstants.COMMAND_CLAIM_REMOVE_CLAIM).sendTo((Player) sender);
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

    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COLONYID_ARG, IntegerArgumentType.integer(1))
                         .then(IMCCommand.newArgument(RANGE_ARG, IntegerArgumentType.integer(0, 10))
                                 .then(IMCCommand.newArgument(ADD_ARG, BoolArgumentType.bool()).executes(this::checkPreConditionAndExecute))));
    }
}
