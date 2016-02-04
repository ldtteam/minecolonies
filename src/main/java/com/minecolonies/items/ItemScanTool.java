package com.minecolonies.items;

import com.minecolonies.util.ChunkCoordUtils;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Schematic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
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
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int face, float par8, float par9, float par10)
    {
        if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound compound = stack.getTagCompound();

        if(!compound.hasKey("pos1"))
        {
            ChunkCoordUtils.writeToNBT(compound, "pos1", new ChunkCoordinates(x, y, z));
            if(world.isRemote) LanguageHandler.sendPlayerLocalizedMessage(player, "item.scepterSteel.point");
            return true;
        }
        else if(!compound.hasKey("pos2"))
        {
            ChunkCoordinates pos1 = ChunkCoordUtils.readFromNBT(compound, "pos1");
            ChunkCoordinates pos2 = new ChunkCoordinates(x, y, z);
            if(pos2.getDistanceSquaredToChunkCoordinates(pos1) > 0)
            {
                ChunkCoordUtils.writeToNBT(compound, "pos2", pos2);
                if(world.isRemote) LanguageHandler.sendPlayerLocalizedMessage(player, "item.scepterSteel.point2");
                return true;
            }
            if (world.isRemote) LanguageHandler.sendPlayerLocalizedMessage(player, "item.scepterSteel.samePoint");
            return false;
        }
        else
        {
            ChunkCoordinates pos1 = ChunkCoordUtils.readFromNBT(compound, "pos1");
            ChunkCoordinates pos2 = ChunkCoordUtils.readFromNBT(compound, "pos2");
            if(world.isRemote)
            {
                String result = Schematic.saveSchematic(world, pos1, pos2);
                LanguageHandler.sendPlayerMessage(player, result);
            }
            compound.removeTag("pos1");
            compound.removeTag("pos2");
            return true;
        }
    }
}
