package com.minecolonies.blocks;

import com.minecolonies.MineColonies;
import com.minecolonies.lib.Constants;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutDeliveryman extends BlockInformator {
    public final String name = "blockHutDeliveryman";

    protected BlockHutDeliveryman() {
        super(Material.wood);
        setBlockName(getName());
        GameRegistry.registerBlock(this, getName());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        //TODO
        return null;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9) {
        entityPlayer.openGui(MineColonies.instance, Constants.Gui.HutDeliveryman.ordinal(), world, x, y, z);
        return true;
    }
}