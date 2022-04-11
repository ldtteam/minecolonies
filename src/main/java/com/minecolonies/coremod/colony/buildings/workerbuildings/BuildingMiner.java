package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.settings.BlockSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.coremod.colony.jobs.JobMiner;
import com.minecolonies.coremod.colony.workorders.WorkOrderMiner;
import com.minecolonies.coremod.entity.ai.citizen.miner.Node;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.BuildingConstants.*;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * The miners building.
 */
public class BuildingMiner extends AbstractBuildingStructureBuilder
{
    /**
     * Setting for solid filling block.
     */
    public static final ISettingKey<BlockSetting> FILL_BLOCK = new SettingKey<>(BlockSetting.class, new ResourceLocation(Constants.MOD_ID, "fillblock"));

    /**
     * The job description.
     */
    private static final String MINER = "miner";

    /**
     * The location of the topmost cobblestone the ladder starts at.
     */
    private BlockPos cobbleLocation;

    /**
     * The location of the topmost ladder in the shaft.
     */
    private BlockPos ladderLocation;

    /**
     * Required constructor.
     *
     * @param c colony containing the building.
     * @param l location of the building.
     */
    public BuildingMiner(final IColony c, final BlockPos l)
    {
        super(c, l);

        final ItemStack stackLadder = new ItemStack(Blocks.LADDER);
        final ItemStack stackTorch = new ItemStack(Blocks.TORCH);
        final ItemStack stackCobble = new ItemStack(Blocks.COBBLESTONE);

        keepX.put(stackLadder::sameItem, new Tuple<>(STACKSIZE, true));
        keepX.put(stackTorch::sameItem, new Tuple<>(STACKSIZE, true));
        keepX.put(stackCobble::sameItem, new Tuple<>(STACKSIZE, true));

        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.PICKAXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.SHOVEL, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));

        keepX.put(itemStack -> ItemStackUtils.compareItemStacksIgnoreStackSize(itemStack, new ItemStack(getSetting(FILL_BLOCK).getValue())), new Tuple<>(STACKSIZE, true));
    }

    /**
     * Getter of the structure name.
     *
     * @return the structure name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return MINER;
    }

    /**
     * Getter of the max building level.
     *
     * @return the integer.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    /**
     * The Miner wants to get multiple nodes/levels worth of stuff when requesting.
     */
    @Override
    public int getResourceBatchMultiplier() 
    {
        if (getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry() == ModJobs.quarrier).getAssignedCitizen().isEmpty())
        {
            //Ask for 10x the resources if possible
            return 10;
        }
        return 1;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);

        ladderLocation = BlockPosUtil.readOrNull(compound, TAG_LLOCATION);
        cobbleLocation = BlockPosUtil.readOrNull(compound, TAG_CLOCATION);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();

        BlockPosUtil.writeOptional(compound, TAG_CLOCATION, cobbleLocation);
        BlockPosUtil.writeOptional(compound, TAG_LLOCATION, ladderLocation);

        return compound;
    }

    /**
     * Returns the depth limit. Limitted by building level.
     * <pre>
     * - Level 1: 50
     * - Level 2: 30
     * - Level 3: 5
     * </pre>
     *
     * @return Depth limit.
     */
    public int getDepthLimit()
    {
        if (this.getBuildingLevel() == 1)
        {
            return MAX_DEPTH_LEVEL_1;
        }
        else if (this.getBuildingLevel() == 2)
        {
            return MAX_DEPTH_LEVEL_2;
        }
        else if (this.getBuildingLevel() >= 3)
        {
            return MAX_DEPTH_LEVEL_3;
        }

        return MAX_DEPTH_LEVEL_0;
    }

    /**
     * Getter of the ladderLocation.
     *
     * @return the ladder location.
     */
    public BlockPos getLadderLocation()
    {
        if (ladderLocation == null)
        {
            loadLadderPos();
        }

        return ladderLocation;
    }

    /**
     * Getter of the cobbleLocation.
     *
     * @return the location.
     */
    public BlockPos getCobbleLocation()
    {
        if (cobbleLocation == null)
        {
            loadLadderPos();
        }

        return cobbleLocation;
    }

    private void loadLadderPos()
    {
        final Map<String, Set<BlockPos>> map = tileEntity.getWorldTagNamePosMap();
        final Set<BlockPos> cobblePos = map.getOrDefault("cobble", new HashSet<>());
        final Set<BlockPos> ladderPos = map.getOrDefault("ladder", new HashSet<>());
        if (cobblePos.isEmpty() || ladderPos.isEmpty())
        {
            return;
        }
        cobbleLocation = cobblePos.iterator().next();
        ladderLocation = ladderPos.iterator().next();
    }

    @Override
    public void searchWorkOrder()
    {
        final ICitizenData citizen = getFirstModuleOccurance(WorkerBuildingModule.class).getFirstCitizen();
        if (citizen == null)
        {
            return;
        }

        final List<WorkOrderMiner> list = getColony().getWorkManager().getOrderedList(WorkOrderMiner.class, getPosition());

        for (final WorkOrderMiner wo : list)
        {
            if (this.getID().equals(wo.getMinerBuilding()))
            {
                citizen.getJob(JobMiner.class).setWorkOrder(wo);
                wo.setClaimedBy(citizen);
                return;
            }
        }
    }

    /**
     * Initiates structure loading.
     *
     * @param mineNode     the node to load it for.
     * @param rotateTimes  The amount of time to rotate the structure.
     * @param structurePos The position of the structure.
     */
    public static void initStructure(final Node mineNode, final int rotateTimes, final BlockPos structurePos, final BuildingMiner buildingMiner, final World world, final JobMiner job)
    {
        final String style = buildingMiner.getStyle();
        String requiredName = null;
        int rotateCount = 0;

        if (mineNode == null)
        {
            rotateCount = getRotationFromVector(buildingMiner);
            requiredName = getCorrectStyleLocation(style, Node.NodeType.SHAFT.getSchematicName(), world, buildingMiner);
        }

        else
        {
            rotateCount = rotateTimes;
            requiredName = getCorrectStyleLocation(style, mineNode.getStyle().getSchematicName(), world, buildingMiner);
        }

        if (requiredName != null &&  (job == null || job.getWorkOrder() == null))
        {
            final WorkOrderMiner wo = new WorkOrderMiner(requiredName, requiredName, rotateCount, structurePos, false, buildingMiner.getPosition());
            wo.setClaimedBy(buildingMiner.getPosition());
            buildingMiner.getColony().getWorkManager().addWorkOrder(wo, false);
            if (job != null)
            {
                job.setWorkOrder(wo);
            }
            else
            {
                wo.setClaimedBy(buildingMiner.getPosition());
            }
        }
        buildingMiner.markDirty();
    }

    /**
     * Return number of rotation for our building, for the main shaft.
     *
     * @return the rotation.
     */
    private static int getRotationFromVector(final BuildingMiner buildingMiner)
    {
        final BlockPos vector = buildingMiner.getLadderLocation().subtract(buildingMiner.getCobbleLocation());

        if (vector.getX() == 1)
        {
            return 1;
        }
        else if (vector.getZ() == 1)
        {
            return 2;
        }
        else if (vector.getX() == -1)
        {
            return 3;
        }
        else if (vector.getZ() == -1)
        {
            return 4;
        }
        return 0;
    }

    /**
     * Get the correct style for the shaft. Return default back.
     *
     * @param style the style to check.
     * @param shaft the shaft.
     * @return the correct location.
     */
    private static String getCorrectStyleLocation(final String style, final String shaft, final World world, final BuildingMiner buildingMiner)
    {
        final LoadOnlyStructureHandler
          wrapper = new LoadOnlyStructureHandler(world, buildingMiner.getPosition(), Structures.SCHEMATICS_PREFIX + "/" + style + shaft, new PlacementSettings(), true);
        if (wrapper.hasBluePrint())
        {
            return Structures.SCHEMATICS_PREFIX + "/" + style + shaft;
        }
        else
        {
            return Structures.SCHEMATICS_PREFIX + shaft;
        }
    }
}
