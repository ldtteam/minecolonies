package com.minecolonies.items;

import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.SchematicWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ItemScanTool extends AbstractItemMinecolonies
{
    public ItemScanTool()
    {
        super("scepterSteel");
        setMaxStackSize(1);
    }

    @Override
    public boolean onItemUse(@NotNull ItemStack stack, @NotNull EntityPlayer player, @NotNull World worldIn, @NotNull BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (!stack.hasTagCompound())
        {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound compound = stack.getTagCompound();

        if (!compound.hasKey("pos1"))
        {
            BlockPosUtil.writeToNBT(compound, "pos1", pos);
            if (worldIn.isRemote)
            {
                LanguageHandler.sendPlayerLocalizedMessage(player, "item.scepterSteel.point");
            }
            return true;
        }
        else if (!compound.hasKey("pos2"))
        {
            @NotNull BlockPos pos1 = BlockPosUtil.readFromNBT(compound, "pos1");
            @NotNull BlockPos pos2 = pos;
            if (pos2.distanceSq(pos1) > 0)
            {
                BlockPosUtil.writeToNBT(compound, "pos2", pos2);
                if (worldIn.isRemote)
                {
                    LanguageHandler.sendPlayerLocalizedMessage(player, "item.scepterSteel.point2");
                }
                return true;
            }
            if (worldIn.isRemote)
            {
                LanguageHandler.sendPlayerLocalizedMessage(player, "item.scepterSteel.samePoint");
            }
            return false;
        }
        else
        {
            @NotNull BlockPos pos1 = BlockPosUtil.readFromNBT(compound, "pos1");
            @NotNull BlockPos pos2 = BlockPosUtil.readFromNBT(compound, "pos2");
            if (worldIn.isRemote)
            {
                String result = SchematicWrapper.saveSchematic(worldIn, pos1, pos2);
                LanguageHandler.sendPlayerMessage(player, result);
            }
            compound.removeTag("pos1");
            compound.removeTag("pos2");
            return true;
        }
    }
}
