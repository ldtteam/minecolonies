package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.core.colony.buildings.modules.GraveyardManagementModule;
import com.minecolonies.core.tileentities.TileEntityGrave;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.EquipmentLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Class which handles the graveyard building.
 */
public class BuildingGraveyard extends AbstractBuilding
{
    /**
     * Descriptive string of the building.
     */
    private static final String GRAVEYARD = "graveyard";

    /**
     * The maximum building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * The last field tag.
     */
    private static final String TAG_CURRENT_GRAVE = "currentGRAVE";

    /**
     * The grave the undertaker is currently collecting.
     */
    @Nullable
    private BlockPos currentGrave;

    /**
     * Public constructor which instantiates the building.
     *
     * @param c the colony the building is in.
     * @param l the position it has been placed (it's id).
     */
    public BuildingGraveyard(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(itemStack -> ItemStackUtils.hasEquipmentLevel(itemStack, ModEquipmentTypes.shovel.get(), TOOL_LEVEL_WOOD_OR_GOLD, getMaxEquipmentLevel()), new net.minecraft.util.Tuple<>(1, true));
        keepX.put(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING, new net.minecraft.util.Tuple<>(2, true));
    }

    /**
     * Clear the current grave the undertaker is currently working on.
     */
    public void ClearCurrentGrave()
    {
        this.currentGrave = null;
    }

    /**
     * Retrieves a random grave to work on for the undertaker.
     *
     * @return a field to work on.
     */
    @Nullable
    public BlockPos getGraveToWorkOn()
    {
        if(currentGrave != null)
        {
            if (WorldUtil.isBlockLoaded(colony.getWorld(), currentGrave))
            {
                final BlockEntity tileEntity = getColony().getWorld().getBlockEntity(currentGrave);
                if (tileEntity instanceof TileEntityGrave)
                {
                    return currentGrave;
                }
            }

            colony.getGraveManager().unReserveGrave(currentGrave);
            currentGrave = null;
        }

        currentGrave = colony.getGraveManager().reserveNextFreeGrave();
        return currentGrave;
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);

        if (compound.contains(TAG_CURRENT_GRAVE))
        {
            currentGrave = BlockPosUtil.read(compound, TAG_CURRENT_GRAVE);
        }

        // Safely migrate grave positions from the building to the module
        getModule(GraveyardManagementModule.class).migrateLegacyGravePositions(compound);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();

        if (currentGrave != null)
        {
            BlockPosUtil.write(compound, TAG_CURRENT_GRAVE, currentGrave);
        }

        return compound;
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return GRAVEYARD;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public void registerBlockPosition(@NotNull final BlockState state, @NotNull final BlockPos pos, @NotNull final Level world)
    {
        super.registerBlockPosition(state, pos, world);
        getModule(GraveyardManagementModule.class).registerGravePosition(state, pos);
    }
}
