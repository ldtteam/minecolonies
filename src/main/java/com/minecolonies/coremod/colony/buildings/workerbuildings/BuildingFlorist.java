package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableList;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.coremod.client.gui.WindowHutFlorist;
import com.minecolonies.coremod.colony.buildings.AbstractFilterableListBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractFilterableListsView;
import com.minecolonies.coremod.colony.jobs.JobFlorist;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.FLORIST_FLOWER_LIST;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * The florist building.
 */
public class BuildingFlorist extends AbstractFilterableListBuilding
{
    /**
     * Florist.
     */
    private static final String FLORIST = "florist";

    /**
     * Maximum building level
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Tag to store the plant ground position.
     */
    private static final String TAG_POS = "pos";

    /**
     * Tag to store the plant ground list.
     */
    private static final String TAG_PLANTGROUND = "plantGround";

    /**
     * Name to filter the list for.
     */
    private static final String FLOWER_NAME    = "flower";

    /**
     * The basic flower metadata for level 1.
     */
    private static final int BASIC_FLOWER_META = 0;

    /**
     * List of registered barrels.
     */
    private final List<BlockPos> plantGround = new ArrayList<>();

    /**
     * The constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingFlorist(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put((stack) -> stack.getItem() == ModItems.compost, new Tuple<>(STACKSIZE, true));
    }

    /**
     * Return a list of barrels assigned to this hut.
     *
     * @return copy of the list
     */
    public List<BlockPos> getPlantGround()
    {
        return ImmutableList.copyOf(plantGround);
    }

    @NotNull
    @Override
    public IJob createJob(final ICitizenData citizen)
    {
        return new JobFlorist(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return FLORIST;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Dexterity;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Agility;
    }

    @Override
    public String getSchematicName()
    {
        return FLORIST;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block == ModBlocks.blockCompostedDirt && !plantGround.contains(pos))
        {
            plantGround.add(pos);
        }
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        final ListNBT compostBinTagList = compound.getList(TAG_PLANTGROUND, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < compostBinTagList.size(); ++i)
        {
            plantGround.add(NBTUtil.readBlockPos(compostBinTagList.getCompound(i).getCompound(TAG_POS)));
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        @NotNull final ListNBT compostBinTagList = new ListNBT();
        for (@NotNull final BlockPos entry : plantGround)
        {
            @NotNull final CompoundNBT compostBinCompound = new CompoundNBT();
            compostBinCompound.put(TAG_POS, NBTUtil.writeBlockPos(entry));
            compostBinTagList.add(compostBinCompound);
        }
        compound.put(TAG_PLANTGROUND, compostBinTagList);

        return compound;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.florist;
    }

    /**
     * Remove a piece of plantable ground because invalid.
     * @param pos the pos to remove it at.
     */
    public void removePlantableGround(final BlockPos pos)
    {
        this.plantGround.remove(pos);
    }

    /**
     * Get a random flower to grow at the moment.
     * @return the flower to grow.
     */
    @Nullable
    public ItemStack getFlowerToGrow()
    {
        final List<ItemStorage> stacks = getPlantablesForBuildingLevel(getBuildingLevel()).stream()
                                           .filter(stack -> !isAllowedItem(FLORIST_FLOWER_LIST, stack))
                                           .collect(Collectors.toList());

        if (stacks.isEmpty())
        {
            return null;
        }

        Collections.shuffle(stacks);
        return stacks.get(0).getItemStack();
    }

    /**
     * Get the plantables from the compatibility manager the florist can build at the current level.
     * @param level the building level.
     * @return the restricted list.
     */
    public static List<ItemStorage> getPlantablesForBuildingLevel(final int level)
    {
        switch (level)
        {
            case 0:
            case 1:
                return IColonyManager.getInstance().getCompatibilityManager().getCopyOfPlantables().stream()
                         .filter(storage -> storage.getItem() == Items.POPPY || storage.getItem() == Items.DANDELION)
                         .filter(itemStorage -> itemStorage.getItem().isIn(ItemTags.SMALL_FLOWERS))
                         .collect(Collectors.toList());
            case 2:
                return IColonyManager.getInstance().getCompatibilityManager().getCopyOfPlantables().stream()
                         .filter(itemStorage -> itemStorage.getItem().isIn(ItemTags.SMALL_FLOWERS))
                         .collect(Collectors.toList());
            case 3:
            case 4:
            case 5:
            default:
                return IColonyManager.getInstance().getCompatibilityManager().getCopyOfPlantables();
        }
    }

    /**
     * The client side representation of the building.
     */
    public static class View extends AbstractFilterableListsView
    {
        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutFlorist(this);
        }
    }
}
