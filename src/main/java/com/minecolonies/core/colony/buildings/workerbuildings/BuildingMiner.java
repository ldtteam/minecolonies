package com.minecolonies.core.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableList;
import com.ldtteam.structurize.api.RotationMirror;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.core.colony.buildings.modules.WorkerBuildingModule;
import com.minecolonies.core.colony.buildings.modules.settings.BlockSetting;
import com.minecolonies.core.colony.buildings.modules.settings.IntSetting;
import com.minecolonies.core.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.core.colony.jobs.JobMiner;
import com.minecolonies.core.colony.workorders.WorkOrderMiner;
import com.minecolonies.core.entity.ai.workers.util.MineNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.BuildingConstants.*;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.EquipmentLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

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
     * Max depth the miner is going for.
     */
    public static final ISettingKey<IntSetting> MAX_DEPTH = new SettingKey<>(IntSetting.class, new ResourceLocation(Constants.MOD_ID, "maxdepth"));

    /**
     * Mine height levels:
     * 48: Copper
     * 16: Iron
     * -16: Gold
     * -100: Diamond
     */
    private static final List<Integer> MINING_LEVELS = ImmutableList.copyOf(new Integer[] {48, 16, -16, -100});

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

        keepX.put(stack -> ItemStack.isSameItem(stackLadder, stack), new Tuple<>(STACKSIZE, true));
        keepX.put(stack -> ItemStack.isSameItem(stackTorch, stack), new Tuple<>(STACKSIZE, true));
        keepX.put(stack -> ItemStack.isSameItem(stackCobble, stack), new Tuple<>(STACKSIZE, true));

        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.pickaxe.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxEquipmentLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.shovel.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxEquipmentLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.axe.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxEquipmentLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.shears.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxEquipmentLevel()), new Tuple<>(1, true));

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
        if (getModuleMatching(WorkerBuildingModule.class, m -> m.getJobEntry() == ModJobs.quarrier.get()).getAssignedCitizen().isEmpty())
        {
            //Ask for 4x the resources if possible
            return 4;
        }
        return 1;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag compound)
    {
        super.deserializeNBT(provider, compound);

        ladderLocation = BlockPosUtil.readOrNull(compound, TAG_LLOCATION);
        cobbleLocation = BlockPosUtil.readOrNull(compound, TAG_CLOCATION);
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider)
    {
        final CompoundTag compound = super.serializeNBT(provider);

        BlockPosUtil.writeOptional(compound, TAG_CLOCATION, cobbleLocation);
        BlockPosUtil.writeOptional(compound, TAG_LLOCATION, ladderLocation);

        return compound;
    }

    /**
     * Returns the depth limit. Limitted by building level.
     * <pre>
     * - Level 1: 50
     * - Level 2: 20
     * - Level 3: 0
     * </pre>
     *
     * @return Depth limit.
     */
    public int getDepthLimit(final Level level)
    {
        int buildingY = this.getLadderLocation().getY() - 5;

        int buildingLevels = getBuildingLevel();
        int yLevel = 0;
        for (final Integer miningLevel : MINING_LEVELS)
        {
            if (miningLevel < buildingY)
            {
                yLevel = miningLevel;
                buildingLevels--;
            }

            if (buildingLevels == 0)
            {
                break;
            }
        }

        return normalizeMaxDepth(yLevel, level);
    }

    /**
     * Normalize the maximum depth.
     * Make sure that the returned depth respects the world limits and follows the building setting..
     * @param max the max depth of the given building level.
     * @param level the world.
     * @return the max.
     */
    public int normalizeMaxDepth(final int max, final Level level)
    {
        final int worldMaxDepth = level.getMinBuildHeight() + 5;
        final IntSetting maxDepth = getSetting(MAX_DEPTH);
        if (maxDepth.getValue() == maxDepth.getDefault())
        {
            return Math.max(worldMaxDepth, max);
        }
        return Math.max(worldMaxDepth, Math.max(max, maxDepth.getValue()));
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
        final Map<String, Set<BlockPos>> map = getTileEntity().getWorldTagNamePosMap();
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
                getColony().getWorkManager().setDirty(true);
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
    public static void initStructure(final MineNode mineNode, final BlockPos structurePos, final BuildingMiner buildingMiner, final Level world, final JobMiner job)
    {
        final String structurePack = buildingMiner.getStructurePack();
        RotationMirror rotMir;
        final String style;

        if (mineNode == null)
        {
            rotMir = getRotationFromVector(buildingMiner);
            style = MineNode.NodeType.SHAFT.getSchematicName();
        }
        else
        {
            rotMir = mineNode.getRotationMirror().orElse(RotationMirror.NONE);
            style = mineNode.getStyle().getSchematicName();
        }

        if (job == null || job.getWorkOrder() == null)
        {
            final WorkOrderMiner wo = new WorkOrderMiner(structurePack, style + ".blueprint", style, rotMir, structurePos, false, buildingMiner.getPosition());
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
    private static RotationMirror getRotationFromVector(final BuildingMiner buildingMiner)
    {
        final BlockPos vector = buildingMiner.getLadderLocation().subtract(buildingMiner.getCobbleLocation());

        if (vector.getX() == 1)
        {
            return RotationMirror.R90;
        }
        else if (vector.getZ() == 1)
        {
            return RotationMirror.R180;
        }
        else if (vector.getX() == -1)
        {
            return RotationMirror.R270;
        }
        else if (vector.getZ() == -1)
        {
            return RotationMirror.NONE;
        }
        return RotationMirror.NONE;
    }
}
