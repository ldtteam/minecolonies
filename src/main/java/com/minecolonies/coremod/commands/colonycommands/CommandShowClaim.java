package com.minecolonies.coremod.commands.colonycommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;

import java.util.Map;
import java.util.Set;

import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;
import static com.minecolonies.coremod.commands.CommandArgumentNames.POS_ARG;

public class CommandShowClaim implements IMCOPCommand
{
    /**
     * What happens when the command is executed after preConditions are successful.
     *
     * @param context the context of the command execution
     */
    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        final ServerWorld level = context.getSource().getLevel();

        // Colony
        BlockPos pos = new BlockPos(context.getSource().getPosition());
        try
        {
            pos = BlockPosArgument.getLoadedBlockPos(context, POS_ARG);
        }
        catch (Exception e)
        {

        }

        final Chunk chunk = (Chunk) level.getChunk(pos);
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP, null).resolve().orElse(null);
        if (cap == null)
        {
            context.getSource().sendFailure(new StringTextComponent("No capability for chunk found!"));
            return 0;
        }

        context.getSource().sendSuccess(buildClaimCommandResult(cap, pos, level), true);
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
    private IFormattableTextComponent buildClaimCommandResult(final IColonyTagCapability cap, final BlockPos pos, final ServerWorld level)
    {
        final IFormattableTextComponent text = new TranslationTextComponent("Claim data of chunk at: %sX %sZ\n", pos.getX(), pos.getZ()).withStyle(TextFormatting.DARK_AQUA);

        if (!cap.getStaticClaimColonies().isEmpty())
        {
            text.append(new TranslationTextComponent("OwnerID:%s Direct colony claims:\n", cap.getOwningColony()).withStyle(TextFormatting.GOLD));
            for (int colonyID : cap.getStaticClaimColonies())
            {
                final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyID, level.dimension());
                if (colony == null)
                {
                    text.append(new TranslationTextComponent("ID: %s Name: Unkown Colony\n", colonyID));
                }
                else
                {
                    text.append(new TranslationTextComponent("ID: %s Name: %s\n", colonyID, colony.getName()));
                }
            }
        }

        if (!cap.getAllClaimingBuildings().isEmpty())
        {
            text.append(new TranslationTextComponent("Building claims:\n").withStyle(TextFormatting.GOLD));
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
                            text.append(new TranslationTextComponent("ID: %s Building: %s Pos: %s\n",
                              entry.getKey(),
                              new TranslationTextComponent(building.getBuildingDisplayName()),
                              buildingPos));
                        }
                        else
                        {
                            text.append(new TranslationTextComponent("ID: %s Building: Unknown pos: %s\n", entry.getKey(), buildingPos));
                        }
                    }
                    else
                    {
                        text.append(new TranslationTextComponent("ID: %s Building: Unknown Pos: %s\n", entry.getKey(), buildingPos));
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

    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName())
          .then(IMCCommand.newArgument(POS_ARG, BlockPosArgument.blockPos()).executes(this::checkPreConditionAndExecute))
          .executes(this::checkPreConditionAndExecute);
    }
}
