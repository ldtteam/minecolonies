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

        Vec3 pos2 = Vec3.createVectorHelper(x, y, z);
        if(pos.squareDistanceTo(pos2) > 0)
        {
            String name = "SCAN_" + System.currentTimeMillis();
            Schematic.saveSchematic(world, pos, pos2, name, null);
            pos = null;
            LanguageHandler.sendPlayerLocalizedMessage(player, "item.scepterSteel.scan", name);
            return true;
        }
        LanguageHandler.sendPlayerLocalizedMessage(player, "item.scepterSteel.samePoint");
        return false;
    }
}
