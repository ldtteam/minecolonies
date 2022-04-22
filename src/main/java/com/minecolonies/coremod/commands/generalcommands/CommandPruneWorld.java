package com.minecolonies.coremod.commands.generalcommands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.coremod.colony.IColonyManagerCapability;
import com.minecolonies.coremod.commands.commandTypes.IMCCommand;
import com.minecolonies.coremod.commands.commandTypes.IMCOPCommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.storage.FolderName;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.translation.CommandTranslationConstants.COMMAND_PRUNE_WORLD_WARNING;
import static com.minecolonies.coremod.MineColonies.COLONY_MANAGER_CAP;

/**
 * Command for pruning world region files to colonies
 */
public class CommandPruneWorld implements IMCOPCommand
{
    /**
     * Command radius arg, for giving the protection range around buildings
     */
    private static final String RADIUS_ARG = "additional block protection radius";

    /**
     * Command stage arg, for execution repeats
     */
    private static final String COMMAND_STAGE = "stage";

    /**
     * Base name of region data folders
     */
    private static final String REGION_FOLDER = "region";

    @Override
    public int onExecute(final CommandContext<CommandSource> context)
    {
        return tryPrune(context, 0);
    }

    private int executeWithPage(final CommandContext<CommandSource> context)
    {
        if (!checkPreCondition(context))
        {
            return 0;
        }

        return tryPrune(context, IntegerArgumentType.getInteger(context, COMMAND_STAGE));
    }

    /**
     * Tries to prune the world, protects colony buildings in this world by the given radius(min 100)
     *
     * @param context command context
     * @param arg     progress arg
     * @return command return
     */
    private int tryPrune(final CommandContext<CommandSource> context, final int arg)
    {
        if (arg < 3)
        {
            context.getSource().sendSuccess(new TranslationTextComponent(COMMAND_PRUNE_WORLD_WARNING, arg + 1), true);
            return 0;
        }

        final int radius = IntegerArgumentType.getInteger(context, RADIUS_ARG);

        int deleteCount = 0;

        final World world = context.getSource().getLevel();

        // Local save folder for this word
        final File saveDir = new File(DimensionType.getStorageFolder(world.dimension(), world.getServer().getWorldPath(FolderName.ROOT).toFile()), REGION_FOLDER);

        // Colony list for this world
        List<IColony> colonies = new ArrayList<>();
        final IColonyManagerCapability cap = world.getCapability(COLONY_MANAGER_CAP, null).orElseGet(null);
        if (cap != null)
        {
            colonies = cap.getColonies();
        }
        else
        {
            return 0;
        }

        // Loop the region data files
        for (final File currentRegion : saveDir.listFiles())
        {
            if (currentRegion != null && currentRegion.getName().contains(".mca"))
            {
                final String[] split = currentRegion.getName().split("\\.");
                if (split.length != 4)
                {
                    continue;
                }

                // Current region file X/Z positions
                final int regionX = Integer.parseInt(split[1]);
                final int regionZ = Integer.parseInt(split[2]);

                if (isFarEnoughFromColonies(regionX, regionZ, radius, colonies))
                {
                    if (!currentRegion.delete())
                    {
                        context.getSource().sendSuccess(new StringTextComponent("Could not delete file:" + currentRegion.getPath()), true);
                    }
                    else
                    {
                        deleteCount++;
                        context.getSource().sendSuccess(new StringTextComponent("Deleted file:" + currentRegion.getPath()), true);
                    }
                }
            }
        }

        context.getSource().sendSuccess(new StringTextComponent("Successfully deleted " + deleteCount + " regions!"), true);
        return 0;
    }

    /**
     * Returns whether the given region coords are far enough away from all colony buildings
     *
     * @param regionX     X
     * @param regionZ     Z
     * @param blockRadius additional protected block radius
     * @param colonies    colonies to check
     * @return true if far enough
     */
    private boolean isFarEnoughFromColonies(final int regionX, final int regionZ, final int blockRadius, List<IColony> colonies)
    {
        for (final IColony colony : colonies)
        {
            for (final BlockPos buildingPos : colony.getBuildingManager().getBuildings().keySet())
            {
                // Calculate region corners for the building pos + additionally protected radius
                final int maxX = (buildingPos.getX() + blockRadius) >> 9;
                final int minX = (buildingPos.getX() - blockRadius) >> 9;
                final int maxZ = (buildingPos.getZ() + blockRadius) >> 9;
                final int minZ = (buildingPos.getZ() - blockRadius) >> 9;

                if (regionX <= maxX && regionX >= minX && regionZ <= maxZ && regionZ >= minZ)
                {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String getName()
    {
        return "prune-world-now";
    }

    @Override
    public LiteralArgumentBuilder<CommandSource> build()
    {
        return IMCCommand.newLiteral(getName())
                 .then(IMCCommand.newArgument(COMMAND_STAGE, IntegerArgumentType.integer(1))
                         .executes(this::executeWithPage)
                         .then(IMCCommand.newArgument(RADIUS_ARG, IntegerArgumentType.integer(100, 5000)).executes(this::executeWithPage)))
                 .executes(this::checkPreConditionAndExecute);
    }
}
