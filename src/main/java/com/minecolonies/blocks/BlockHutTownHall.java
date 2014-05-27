package com.minecolonies.blocks;

import com.minecolonies.MineColonies;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.PlayerProperties;
import com.minecolonies.lib.EnumGUI;
import com.minecolonies.tileentities.TileEntityTownHall;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockHutTownHall extends BlockHut
{
    protected BlockHutTownHall()
    {
        super();
        this.workingRange = Configurations.workingRangeTownhall;
    }

    @Override
    public String getName()
    {
        return "blockHutTownhall";
    }

    //TODO Check that huts are within the range of the townhall and aren't already bound to an existing townhall.
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack)
    {
        if(world.isRemote) return;

        if(entityLivingBase instanceof EntityPlayer)
        {
            EntityPlayer entityPlayer = (EntityPlayer) entityLivingBase;

            if(!world.provider.isSurfaceWorld())
            {
                cancelBlockPlacing(world, entityPlayer, x, y, z);
                LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "tile.blockHutTownhall.messageInvalidWorld");
                return;
            }

            TileEntityTownHall closestTownHall = Utils.getClosestTownHall(world, x, y, z);
            if(closestTownHall != null && closestTownHall.getDistanceFrom(x, y, z) < Math.pow(2 * workingRange + Configurations.townhallPadding, 2))
            {
                cancelBlockPlacing(world, entityPlayer, x, y, z);
                LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "tile.blockHutTownhall.messageTooClose");
                return;
            }

            PlayerProperties playerProperties = PlayerProperties.get(entityPlayer);
            if(playerProperties.hasPlacedTownHall())
            {
                cancelBlockPlacing(world, entityPlayer, x, y, z);
                LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "tile.blockHutTownhall.messagePlacedAlready");
                return;
            }

            TileEntityTownHall tileEntityTownHall = (TileEntityTownHall) world.getTileEntity(x, y, z);
            tileEntityTownHall.onBlockAdded();
            tileEntityTownHall.setInfo(world, entityPlayer.getUniqueID(), x, z);
            tileEntityTownHall.setCityName(LanguageHandler.format("com.minecolonies.gui.townhall.defaultName", entityPlayer.getDisplayName()));
            playerProperties.placeTownhall(x, y, z);
        }
    }

    private void cancelBlockPlacing(World world, EntityPlayer player, int x, int y, int z)
    {
        world.setBlockToAir(x, y, z);
        removedByPlayer(world, player, x, y, z);
        player.inventory.addItemStackToInventory(new ItemStack(ModBlocks.blockHutTownhall));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEntityTownHall();
    }

    @Override//TODO create a way for this to be in BlockHut
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
    {
        TileEntityTownHall tileEntityTownHall = (TileEntityTownHall) world.getTileEntity(x, y, z);
        if(tileEntityTownHall != null && tileEntityTownHall.isPlayerOwner(entityPlayer))
        {
            entityPlayer.openGui(MineColonies.instance, EnumGUI.TOWNHALL.getID(), world, x, y, z);
            return true;
        }
        return false;
    }
}
