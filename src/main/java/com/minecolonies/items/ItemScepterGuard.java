package com.minecolonies.items;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.Log;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Guard Scepter Item class. Used to give tasks to guards.
 */
public class ItemScepterGuard extends AbstractItemMinecolonies
{
    public enum Action
    {
        PATROL,
        GUARD
    }

    /**
     * Caliper constructor. Sets max stack to 1, like other tools.
     */
    public ItemScepterGuard()
    {
        super("scepterGuard");
        this.setMaxDamage(2);

        maxStackSize = 1;
    }



    @NotNull
    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        //todo check in colony
        //todo if guard -> right click one block to mark it as guard position
        //todo if patrol + manual -> "Right click blocks to mark them as patrol targets left click to terminate.

        //todo send message to server with task
        //todo at server in the tool recognize which task he should follow, following that message.

        // if client world, do nothing
        if (worldIn.isRemote)
        {
            return EnumActionResult.FAIL;
        }

        ItemStack scepter = new ItemStack(ModItems.scepterGuard);
        if (!scepter.hasTagCompound())
        {
            scepter.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound compound = scepter.getTagCompound();

        //Should never happen.
        if (compound == null)
        {
            return EnumActionResult.FAIL;
        }

        int task = compound.getInteger("task");
        BlockPos guardTower = BlockPosUtil.readFromNBT(compound, "pos");

        Log.getLogger().info(task + " " + guardTower.toString());


        //todo get guardTower from building to add the positions to the correct task.
        Colony colony = ColonyManager.getColony(worldIn, pos);


        return EnumActionResult.SUCCESS;
    }

}
