package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.blocks.AbstractBlockMinecoloniesNamedGrave;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.workerbuildings.IBuildingPublicCrafter;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.tileentities.TileEntityGrave;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingWorkerView;
import com.minecolonies.coremod.colony.jobs.JobUndertaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;

/**
 * Class which handles the graveyard building.
 */
public class BuildingGraveyard extends AbstractBuildingWorker implements IBuildingPublicCrafter
{
    /**
     * Descriptive string of the building.
     */
    private static final String GRAVEYARD = "graveyard";

    /**
     * Descriptive string of the profession.
     */
    private static final String UNDERTAKER = "undertaker";

    /**
     * The maximum building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * NBTTag to store the visual grave positions.
     */
    private static final String TAG_VISUAL_GRAVES = "visualgraves";

    /**
     * NBTTag to store the visual grave positions blockpos.
     */
    private static final String TAG_VISUAL_GRAVES_BLOCKPOS = "visualgravesblockpos";

    /**
     * NBTTag to store the visual grave facing.
     */
    private static final String TAG_VISUAL_GRAVES_FACING = "visualgravesfacing";

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
     * Grave positions
     */
    private Set<Tuple<BlockPos, Direction>> visualGravePositions = new HashSet<>();

    /**
     * Public constructor which instantiates the building.
     *
     * @param c the colony the building is in.
     * @param l the position it has been placed (it's id).
     */
    public BuildingGraveyard(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.SHOVEL, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new net.minecraft.util.Tuple<>(1, true));
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

    @NotNull
    @Override
    public IJob<?> createJob(@Nullable final ICitizenData citizen)
    {
        return new JobUndertaker(citizen);
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);

        if (compound.getAllKeys().contains(TAG_CURRENT_GRAVE))
        {
            currentGrave = BlockPosUtil.read(compound, TAG_CURRENT_GRAVE);
        }

        visualGravePositions.clear();
        final ListTag visualGraveTagList = compound.getList(TAG_VISUAL_GRAVES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < visualGraveTagList.size(); ++i)
        {
            final CompoundTag graveCompound = visualGraveTagList.getCompound(i);
            final BlockPos graveLocation = BlockPosUtil.read(graveCompound, TAG_VISUAL_GRAVES_BLOCKPOS);
            final Direction graveFacing = Direction.byName(graveCompound.getString(TAG_VISUAL_GRAVES_FACING));
            visualGravePositions.add(new Tuple<>(graveLocation, graveFacing));
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();

        if (currentGrave != null)
        {
            BlockPosUtil.write(compound, TAG_CURRENT_GRAVE, currentGrave);
        }

        @NotNull final ListTag visualGraveTagList = new ListTag();
        for (@NotNull final Tuple<BlockPos, Direction> vgp : visualGravePositions)
        {
            @NotNull final CompoundTag graveCompound = new CompoundTag();
            BlockPosUtil.write(graveCompound, TAG_VISUAL_GRAVES_BLOCKPOS, vgp.getA());
            graveCompound.putString(TAG_VISUAL_GRAVES_FACING, vgp.getB().getName());
            visualGraveTagList.add(graveCompound);
        }
        compound.put(TAG_VISUAL_GRAVES, visualGraveTagList);
        return compound;
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return UNDERTAKER;
}

    @NotNull
    @Override
    public Skill getPrimarySkill() { return Skill.Strength; }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Mana;
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
        if (state.getBlock() == ModBlocks.blockNamedGrave)
        {
            visualGravePositions.add(new Tuple<>(pos, state.getValue(AbstractBlockMinecoloniesNamedGrave.FACING)));
        }
    }

    /**
     * Return a random free visual grave position
     */
    public Tuple<BlockPos, Direction> getRandomFreeVisualGravePos()
    {
        if (visualGravePositions.isEmpty())
        {
            return null;
        }

        final List<Tuple<BlockPos, Direction>> availablePos = new ArrayList<Tuple<BlockPos, Direction>>();
        for(final Tuple<BlockPos, Direction> tuple : visualGravePositions)
        {
            if(getColony().getWorld().getBlockState(tuple.getA()).isAir())
            {
                availablePos.add(tuple);
            }
        }

        if (availablePos.isEmpty())
        {
            return null;
        }

        Collections.shuffle(availablePos);
        return availablePos.get(0);
    }

    /**
     * Provides a view of the farmer building class.
     */
    public static class View extends AbstractBuildingWorkerView
    {
        /**
         * Public constructor of the view, creates an instance of it.
         *
         * @param c the colony.
         * @param l the position.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @Override
        @NotNull
        public BOWindow getWindow()
        {
            return new WindowHutWorkerModulePlaceholder<>(this, GRAVEYARD);
        }
    }
}
