package com.minecolonies.items;

import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Schematic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ItemScanTool extends ItemMinecolonies
{
    Vec3 pos;
    Vec3 pos2;

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
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int face, float par8, float par9, float par10)
    {
        if(world.isRemote) return false;
        if(pos == null)
        {
            pos = Vec3.createVectorHelper(x, y, z);
            LanguageHandler.sendPlayerLocalizedMessage(player, "item.scepterSteel.point");
            return true;
        }
        else if(pos2 == null)
        {
            pos2 = Vec3.createVectorHelper(x, y, z);
            if(pos.squareDistanceTo(pos2) > 0)
            {
                LanguageHandler.sendPlayerLocalizedMessage(player, "item.scepterSteel.point2");
                return true;
            }
            pos2 = null;
            LanguageHandler.sendPlayerLocalizedMessage(player, "item.scepterSteel.samePoint");
            return false;
        }
        else
        {
            String result = Schematic.saveSchematic(world, pos, pos2);
            LanguageHandler.sendPlayerMessage(player, result);
            pos = null;
            pos2 = null;
            return true;
        }
    }
}
