package com.minecolonies.core.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.ColonyUtils;
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
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

        final LevelChunk chunk = (LevelChunk) level.getChunk(pos);
        final BlockPos finalPos = pos;
        context.getSource().sendSuccess(() -> buildClaimCommandResult(chunk, finalPos, level), true);
        return 1;
    }

    /**
     * Creates the feedback text from the given cap
     *
     * @param chunk
     * @param pos
     * @param level
     * @return
     */
    private MutableComponent buildClaimCommandResult(final LevelChunk chunk, final BlockPos pos, final ServerLevel level)
    {
        final MutableComponent text = Component.translatable("Claim data of chunk at: %sX %sZ\n", pos.getX(), pos.getZ()).withStyle(ChatFormatting.DARK_AQUA);

        final List<Integer> staticColonyClaims = ColonyUtils.getStaticClaims(chunk);
        final int owningColony = ColonyUtils.getOwningColony(chunk);
        if (!staticColonyClaims.isEmpty())
        {
            text.append(Component.translatable("OwnerID:%s Direct colony claims:\n", owningColony).withStyle(ChatFormatting.GOLD));
            for (int colonyID : staticColonyClaims)
            {
                final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, level.dimension());
                if (colony == null)
                {
                    text.append(Component.translatable("ID: %s Name: Unkown Colony\n", colonyID));
                }
                else
                {
                    text.append(Component.translatable("ID: %s Name: %s\n", colonyID, colony.getName()));
                }
            }
        }

        final Map<Integer, Set<BlockPos>> buildingClaims = ColonyUtils.getAllClaimingBuildings(chunk);
        if (!buildingClaims.isEmpty())
        {
            text.append(Component.translatable("Building claims:\n").withStyle(ChatFormatting.GOLD));
            for (Map.Entry<Integer, Set<BlockPos>> entry : buildingClaims.entrySet())
            {
                final IColony colony = IColonyManager.getInstance().getColonyByDimension(entry.getKey(), level.dimension());
                for (final BlockPos buildingPos : entry.getValue())
                {
                    if (colony != null)
                    {
                        final IBuilding building = colony.getBuildingManager().getBuilding(buildingPos);
                        if (building != null)
                        {
                            text.append(Component.translatable("ID: %s Building: %s Pos: %s\n",
                              entry.getKey(),
                              Component.translatable(building.getBuildingDisplayName()),
                              buildingPos));
                        }
                        else
                        {
                            text.append(Component.translatable("ID: %s Building: Unknown pos: %s\n", entry.getKey(), buildingPos));
                        }
                    }
                    else
                    {
                        text.append(Component.translatable("ID: %s Building: Unknown Pos: %s\n", entry.getKey(), buildingPos));
                    }
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
