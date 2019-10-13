package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutFlorist;
import com.minecolonies.coremod.colony.buildings.AbstractFilterableListBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractFilterableListsView;
import com.minecolonies.coremod.colony.jobs.JobFlorist;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
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

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * The florist building.
 */
public class BuildingFlorist extends AbstractFilterableListBuilding
{
    /**
     * Florist.
     */
    private static final String FLORIST = "Florist";

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
    public void deserializeNBT(final NBTTagCompound compound)
    {
        super.deserializeNBT(compound);
        final NBTTagList compostBinTagList = compound.getTagList(TAG_PLANTGROUND, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < compostBinTagList.tagCount(); ++i)
        {
            plantGround.add(NBTUtil.getPosFromTag(compostBinTagList.getCompoundTagAt(i).getCompoundTag(TAG_POS)));
        }
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        final NBTTagCompound compound = super.serializeNBT();
        @NotNull final NBTTagList compostBinTagList = new NBTTagList();
        for (@NotNull final BlockPos entry : plantGround)
        {
            @NotNull final NBTTagCompound compostBinCompound = new NBTTagCompound();
            compostBinCompound.setTag(TAG_POS, NBTUtil.createPosTag(entry));
            compostBinTagList.appendTag(compostBinCompound);
        }
        compound.setTag(TAG_PLANTGROUND, compostBinTagList);

        return compound;
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
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
        final List<ItemStorage> stacks = IColonyManager.getInstance().getCompatibilityManager()
                                           .getCopyOfPlantables().stream()
                                           .filter(stack -> !isAllowedItem("flowers", stack))
                                           .collect(Collectors.toList());

        if (stacks.isEmpty())
        {
            return null;
        }

        Collections.shuffle(stacks);
        return stacks.get(0).getItemStack();
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

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutFlorist(this);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.CHARISMA;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.INTELLIGENCE;
        }
    }
}
