package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.claims.ClaimInfo;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.commands.arguments.ColonyIdArgument;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import static com.minecolonies.core.commands.CommandArgumentNames.COLONYID_ARG;

/**
 * Debug command reporting a whole colony's claim status at once: every claimed chunk, how many buildings claim it ([CIB]),
 * whether it's force-claimed ([CIF]) or part of the center claim ([CIC]), whether it has enough claiming buildings to
 * qualify for force-loading ([F]), whether it currently holds a force-load ticket ([T]), and whether it's actually loaded
 * right now ([L]).
 */
public class CommandColonyClaimStatus implements IMCOPCommand
{
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final ServerLevel level = context.getSource().getLevel();
        final IColony colony = ColonyIdArgument.getColony(context, COLONYID_ARG);

        sendClaimStatus(context, colony, level);
        return 1;
    }

    /**
     * Sends the claim status report as one chat message per line, so it doesn't get squashed into a single oversized entry
     * in the client's chat history (which is capped by message count, not by how many lines a single message wraps to).
     *
     * @param context the command context, used to send the report.
     * @param colony  the colony to report on.
     * @param level   the level.
     */
    private void sendClaimStatus(final CommandContext<CommandSourceStack> context, final IColony colony, final ServerLevel level)
    {
        final var claimedChunks = colony.getClaimedChunks();
        final var ticketedChunks = colony.getTicketedChunks();
        final int strictness = MineColonies.getConfig().getServer().colonyLoadStrictness.get();

        send(context, Component.literal("Claim status for colony " + colony.getID() + " (" + colony.getName() + "):").withStyle(ChatFormatting.DARK_AQUA));
        send(context, Component.literal("Claimed chunks: " + claimedChunks.size() + ", ticketed chunks: " + ticketedChunks.size()).withStyle(ChatFormatting.GOLD));

        final Integer forceLoadTimer = colony.getForceLoadTimer();
        if (forceLoadTimer != null)
        {
            send(context, Component.literal("Force-load timer: " + (forceLoadTimer / 20) + "s remaining").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        else
        {
            send(context, Component.literal("Force-load timer: not active").withStyle(ChatFormatting.GRAY));
        }

        send(context, Component.literal(
            "[CIB=N] = claiming building count, [CIF] = force-claimed, [CIC] = part of the center claim, [F] = qualified for force-loading, [T] = currently force-loaded (has a ticket), [L] = actually loaded right now")
          .withStyle(ChatFormatting.GRAY));

        if (claimedChunks.isEmpty())
        {
            return;
        }

        int forcedCount = 0;
        int centerCount = 0;
        int shouldForceLoadCount = 0;
        int ticketedCount = 0;
        int loadedCount = 0;

        for (final long chunkPos : claimedChunks)
        {
            final ChunkPos chunk = new ChunkPos(chunkPos);
            final ClaimInfo claimInfo = IColonyManager.getInstance().getClaimInfo(level, chunkPos, colony);

            final boolean forced = claimInfo != null && claimInfo.isForced();
            final boolean center = claimInfo != null && claimInfo.isCenter();
            final int claimingBuildingCount = claimInfo == null ? 0 : claimInfo.getClaimingBuildings().size();
            final boolean shouldForceLoad = claimingBuildingCount >= strictness;
            final boolean ticketed = ticketedChunks.contains(chunkPos);
            final boolean loaded = WorldUtil.isChunkLoaded(level, chunk);

            final MutableComponent line = Component.literal(chunk + " ");
            line.append(Component.literal("[CIB=" + claimingBuildingCount + "]").withStyle(ChatFormatting.WHITE));
            if (forced)
            {
                line.append(Component.literal("[CIF]").withStyle(ChatFormatting.YELLOW));
                forcedCount++;
            }
            if (center)
            {
                line.append(Component.literal("[CIC]").withStyle(ChatFormatting.GOLD));
                centerCount++;
            }
            if (shouldForceLoad)
            {
                line.append(Component.literal("[F]").withStyle(ChatFormatting.AQUA));
                shouldForceLoadCount++;
            }
            if (ticketed)
            {
                line.append(Component.literal("[T]").withStyle(ChatFormatting.LIGHT_PURPLE));
                ticketedCount++;
            }
            if (loaded)
            {
                line.append(Component.literal("[L]").withStyle(ChatFormatting.GREEN));
                loadedCount++;
            }
            send(context, line);
        }

        send(context, Component.literal(
            "Totals: [CIF]=" + forcedCount + " [CIC]=" + centerCount + " [F]=" + shouldForceLoadCount + " [T]=" + ticketedCount + " [L]=" + loadedCount)
          .withStyle(ChatFormatting.GOLD));
    }

    /**
     * Sends one line of the report as its own chat message.
     *
     * @param context the command context.
     * @param line    the line to send.
     */
    private void send(final CommandContext<CommandSourceStack> context, final MutableComponent line)
    {
        context.getSource().sendSuccess(() -> line, true);
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "claimstatus";
    }

    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
          .then(IMCCommand.newArgument(COLONYID_ARG, ColonyIdArgument.id()).executes(this::checkPreConditionAndExecute));
    }
}
