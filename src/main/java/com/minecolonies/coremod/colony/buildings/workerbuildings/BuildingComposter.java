package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.blockout.Log;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.client.gui.WindowHutComposter;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobComposter;
import com.minecolonies.coremod.tileentities.TileEntityBarrel;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BuildingComposter extends AbstractBuildingWorker
{

    /**
     * Description of the job for this building
     */
    private static final String COMPOSTER         = "Composter";

    /**
     * Maxiimum building level
     */
    private static final int MAX_BUILDING_LEVEL   = 5;

    /**
     * Name of the building
     */
    private static final String HUT_NAME          = "composterHut";

    /**
     * Tag to store the barrel position.
     */
    private static final String TAG_POS = "pos";

    /**
     * Tag to store the barrel list.
     */
    private static final String TAG_BARRELS = "barrels";

    /**
     * List of registered barrels.
     */
    private final List<BlockPos> barrels = new ArrayList<>();

    /**
     * Tag to store the item list
     */
    private static final String TAG_ITEMLIST = "itemList";

    /**
     * Tag to store the item
     */
    private static final String TAG_ITEM = "item";

    /**
     * List of allowed items to compost
     */
    private List<ItemStorage> itemsAllowed = new ArrayList<>();

    /**
     * The constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingComposter(@NotNull final Colony c, final BlockPos l)
    {
        super(c, l);
        keepX.put((stack) -> TileEntityBarrel.checkCorrectItem(stack)
          , Integer.MAX_VALUE);

    }

    /**
     * Return a list of barrels assigned to this hut.
     *
     * @return copy of the list
     */
    public List<BlockPos> getBarrels()
    {
        return new ArrayList<>(barrels);
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobComposter(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return COMPOSTER;
    }

    @Override
    public String getSchematicName()
    {
        return COMPOSTER;
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
        if(block == ModBlocks.blockBarrel && !barrels.contains(pos))
        {
            barrels.add(pos);
        }
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        @NotNull final NBTTagList furnacesTagList = new NBTTagList();
        for (@NotNull final BlockPos entry : barrels)
        {
            @NotNull final NBTTagCompound furnaceCompound = new NBTTagCompound();
            furnaceCompound.setTag(TAG_POS, NBTUtil.createPosTag(entry));
            furnacesTagList.appendTag(furnaceCompound);
        }
        compound.setTag(TAG_BARRELS, furnacesTagList);

        @NotNull final NBTTagList itemsToCompost = new NBTTagList();
        for(@NotNull final ItemStorage entry : itemsAllowed)
        {
            @NotNull final NBTTagCompound itemCompound = new NBTTagCompound();
            entry.getItemStack().writeToNBT(itemCompound);
        }
        compound.setTag(TAG_ITEMLIST, itemsToCompost);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        final NBTTagList furnaceTagList = compound.getTagList(TAG_BARRELS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < furnaceTagList.tagCount(); ++i)
        {
            barrels.add(NBTUtil.getPosFromTag(furnaceTagList.getCompoundTagAt(i).getCompoundTag(TAG_POS)));
        }
        final NBTTagList itemsToCompost = compound.getTagList(TAG_ITEMLIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < itemsToCompost.tagCount(); ++i)
        {
            try
            {
                itemsAllowed.add(new ItemStorage(
                  new ItemStack(itemsToCompost.getCompoundTagAt(i).getCompoundTag(TAG_ITEM))
                ));
            }catch (Exception e)
            {
                Log.getLogger().info("Removing incompatible stack");
            }
        }
    }

    public void addCompostableItem(ItemStorage item)
    {
        if(!itemsAllowed.contains(item))
        {
            itemsAllowed.add(item);
        }
    }

    public boolean isAllowedItem(ItemStorage item)
    {
        return itemsAllowed.contains(item);
    }

    public void removeCompostableItem(ItemStorage item)
    {
        if(itemsAllowed.contains(item))
        {
            itemsAllowed.remove(item);
        }
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeInt(itemsAllowed.size());
        for (ItemStorage item : itemsAllowed)
        {
            ByteBufUtils.writeItemStack(buf, item.getItemStack());
        }
    }

    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Instantiates the view of the building.
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutComposter(this, HUT_NAME);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.DEXTERITY;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.STRENGTH;
        }
    }

}
