package com.minecolonies.items;

import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Schematic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemScanTool extends ItemMinecolonies
{
    public ItemScanTool()
    {
        super();
        setMaxStackSize(1);
    }

    @Override
    public String getName()
    {
        return "scepterSteel";
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound compound = stack.getTagCompound();

        if(!compound.hasKey("pos1"))
        {
            BlockPosUtil.writeToNBT(compound, "pos1", pos);
            if(worldIn.isRemote) LanguageHandler.sendPlayerLocalizedMessage(player, "item.scepterSteel.point");
            return true;
        }
        else if(!compound.hasKey("pos2"))
        {
            BlockPos pos1 = BlockPosUtil.readFromNBT(compound, "pos1");
            BlockPos pos2 = pos;
            if(pos2.distanceSq(pos1) > 0)
            {
                BlockPosUtil.writeToNBT(compound, "pos2", pos2);
                if(worldIn.isRemote) LanguageHandler.sendPlayerLocalizedMessage(player, "item.scepterSteel.point2");
                return true;
            }
            if (worldIn.isRemote) LanguageHandler.sendPlayerLocalizedMessage(player, "item.scepterSteel.samePoint");
            return false;
        }
        else
        {
            BlockPos pos1 = BlockPosUtil.readFromNBT(compound, "pos1");
            BlockPos pos2 = BlockPosUtil.readFromNBT(compound, "pos2");
            if(worldIn.isRemote)
            {
                String result = Schematic.saveSchematic(worldIn, pos1, pos2);
                LanguageHandler.sendPlayerMessage(player, result);
            }
            compound.removeTag("pos1");
            compound.removeTag("pos2");
            return true;
        }
    }
}
