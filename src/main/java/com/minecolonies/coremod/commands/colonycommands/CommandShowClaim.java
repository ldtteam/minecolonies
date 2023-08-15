package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
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

import java.util.Map;
import java.util.Set;

import static com.minecolonies.api.colony.IColony.CLOSE_COLONY_CAP;
import static com.minecolonies.coremod.commands.CommandArgumentNames.POS_ARG;

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
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
        if (cap == null)
        {
            context.getSource().sendFailure(Component.literal("No capability for chunk found!"));
            return 0;
        }

        final BlockPos finalPos = pos;
        context.getSource().sendSuccess(() -> buildClaimCommandResult(cap, finalPos, level), true);
        return 1;
    }

    /**
     * Creates the feedback text from the given cap
     *
     * @param cap
     * @param pos
     * @param level
     * @return
     */
    private MutableComponent buildClaimCommandResult(final IColonyTagCapability cap, final BlockPos pos, final ServerLevel level)
    {
        final MutableComponent text = Component.translatable("Claim data of chunk at: %sX %sZ\n", pos.getX(), pos.getZ()).withStyle(ChatFormatting.DARK_AQUA);

        if (!cap.getStaticClaimColonies().isEmpty())
        {
            text.append(Component.translatable("OwnerID:%s Direct colony claims:\n", cap.getOwningColony()).withStyle(ChatFormatting.GOLD));
            for (int colonyID : cap.getStaticClaimColonies())
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

        if (!cap.getAllClaimingBuildings().isEmpty())
        {
            text.append(Component.translatable("Building claims:\n").withStyle(ChatFormatting.GOLD));
            for (Map.Entry<Integer, Set<BlockPos>> entry : cap.getAllClaimingBuildings().entrySet())
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
