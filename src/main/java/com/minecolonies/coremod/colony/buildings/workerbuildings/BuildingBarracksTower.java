package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.advancements.AdvancementTriggers;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.util.AdvancementUtils;
import com.minecolonies.coremod.util.ChunkDataHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Building class for the Barracks Tower.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class BuildingBarracksTower extends AbstractBuildingGuards
{
    ////// --------------------------- NBTConstants --------------------------- \\\\\\
    private static final String TAG_POS = "pos";
    ////// --------------------------- NBTConstants --------------------------- \\\\\\

    /**
     * Our constants. The Schematic names, Defence bonus, and Offence bonus.
     */
    private static final String SCHEMATIC_NAME = "barrackstower";

    /**
     * Position of the barracks for this tower.
     */
    private BlockPos barracks = null;


    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingBarracksTower(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return SCHEMATIC_NAME;
    }

    @SuppressWarnings("squid:S109")
    @Override
    public int getMaxBuildingLevel()
    {
        return 5;
    }

    @Override
    public void requestUpgrade(final PlayerEntity player, final BlockPos builder)
    {
        final int buildingLevel = getBuildingLevel();
        final IBuilding building = getColony().getBuildingManager().getBuilding(barracks);
        if (building != null && buildingLevel < getMaxBuildingLevel() && buildingLevel < building.getBuildingLevel())
        {
            requestWorkOrder(WorkOrderType.UPGRADE, builder);
        }
        else
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.worker.needbarracks"), player.getUUID());
        }
    }

    @Override
    public boolean canDeconstruct()
    {
        return false;
    }

    @Override
    public int getClaimRadius(final int newLevel)
    {
        return 0;
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
        final IBuilding barrack = colony.getBuildingManager().getBuilding(barracks);
        if (barrack == null)
        {
            return;
        }
        ChunkDataHelper.claimColonyChunks(colony, true, barracks, barrack.getClaimRadius(newLevel));

        if (newLevel == barrack.getMaxBuildingLevel())
        {
            boolean allUpgraded = true;
            for (BlockPos tower : ((BuildingBarracks) barrack).getTowers())
            {
                Log.getLogger().info("Upgraded: " + allUpgraded);
                if (colony.getBuildingManager().getBuilding(tower).getBuildingLevel() != barrack.getMaxBuildingLevel())
                    allUpgraded = false;
            }

            if (allUpgraded)
                AdvancementUtils.TriggerAdvancementPlayersForColony(colony, AdvancementTriggers.ALL_TOWERS::trigger);
        }
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        barracks = NBTUtil.readBlockPos(compound.getCompound(TAG_POS));
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        if (barracks != null)
        {
            compound.put(TAG_POS, NBTUtil.writeBlockPos(barracks));
        }

        return compound;
    }

    /**
     * Adds the position of the main barracks.
     *
     * @param pos the BlockPos.
     */
    public void addBarracks(final BlockPos pos)
    {
        barracks = pos;
    }
}
