package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.claims.ClaimInfo;
import com.minecolonies.core.commands.commandTypes.IMCCommand;
import com.minecolonies.core.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import static com.minecolonies.core.commands.CommandArgumentNames.POS_ARG;

public class CommandShowClaim implements IMCOPCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSourceStack> context)
    {
        final ServerLevel level = context.getSource().getLevel();

        // Colony
        BlockPos pos = BlockPos.containing(context.getSource().getPosition());
        try
        {
            pos = BlockPosArgument.getBlockPos(context, POS_ARG);
        }
        catch (Exception e)
        {

        }

        final BlockPos finalPos = pos;
        final long chunkPos = new ChunkPos(pos).toLong();
        context.getSource().sendSuccess(() -> buildClaimCommandResult(chunkPos, finalPos, level), true);
        return 1;
    }

    /**
     * Creates the feedback text describing the claim state of a chunk.
     *
     * @param chunkPos the chunk position, as {@code ChunkPos.asLong(x, z)}.
     * @param pos      the block position within the chunk, for display purposes.
     * @param level    the level.
     * @return the feedback text.
     */
    private MutableComponent buildClaimCommandResult(final long chunkPos, final BlockPos pos, final ServerLevel level)
    {
        final MutableComponent text = Component.literal("Claim data of chunk at: " + pos.getX() + "X " + pos.getZ() + "Z\n").withStyle(ChatFormatting.DARK_AQUA);

        final IColony owningColony = IColonyManager.getInstance().getOwningColony(level, chunkPos);
        if (owningColony == null)
        {
            text.append(Component.literal("Unclaimed\n").withStyle(ChatFormatting.GOLD));
            return text;
        }

        text.append(Component.literal("OwnerID:" + owningColony.getID() + " Name: " + owningColony.getName() + "\n").withStyle(ChatFormatting.GOLD));

        final ClaimInfo claimInfo = IColonyManager.getInstance().getClaimInfo(level, chunkPos, owningColony);
        if (claimInfo == null)
        {
            return text;
        }

        if (claimInfo.isForced())
        {
            text.append(Component.literal("Force-claimed\n").withStyle(ChatFormatting.GOLD));
        }

        if (!claimInfo.getClaimingBuildings().isEmpty())
        {
            text.append(Component.literal("Claiming buildings:\n").withStyle(ChatFormatting.GOLD));
            for (final BlockPos buildingPos : claimInfo.getClaimingBuildings())
            {
                final IBuilding building = owningColony.getServerBuildingManager().getBuilding(buildingPos);
                if (building != null)
                {
                    text.append(Component.literal("Building: ").append(Component.translatable(building.getBuildingDisplayName())).append(" Pos: " + buildingPos + "\n"));
                }
                else
                {
                    text.append(Component.literal("Building: Unknown Pos: " + buildingPos + "\n"));
                }
            }
        }

        return text;
    }

    /**
     * Name string of the command.
     */
    @Override
    public String getName()
    {
        return "claiminfo";
    }

    public LiteralArgumentBuilder<CommandSourceStack> build()
    {
        return IMCCommand.newLiteral(getName())
          .then(IMCCommand.newArgument(POS_ARG, BlockPosArgument.blockPos()).executes(this::checkPreConditionAndExecute))
          .executes(this::checkPreConditionAndExecute);
    }
}
