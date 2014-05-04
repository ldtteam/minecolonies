package com.minecolonies.blocks;

import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.PlayerProperties;
import com.minecolonies.lib.Constants;
import com.minecolonies.tilentities.TileEntityTownHall;
import com.minecolonies.util.Utils;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.Random;

public class BlockHutTownHall extends BlockInformator
{
    public final String name = "blockHutTownhall";

    protected BlockHutTownHall()
    {
        super(Material.wood);
        this.workingRange = Configurations.workingRangeTownhall;
        setBlockName(getName());
        GameRegistry.registerBlock(this, getName());
    }

    @Override
    public int getRenderType()
    {
        return 31;
    }

    @Override
    public String getName()
    {
        return name;
    }

    //TODO Check that huts are within the range of the townhall and aren't already bound to an existing townhall.
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack)
    {
        if(world.isRemote) return;
        PlayerProperties playerProperties = (PlayerProperties)entityLivingBase.getExtendedProperties(Constants.PlayerPropertyName);
        if(playerProperties.hasPlacedTownHall())
        {
            world.setBlockToAir(x,y,z);
            FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("You have placed a Town Hall already"));
            removedByPlayer(world, (EntityPlayer)entityLivingBase, x, y, z);
            return;
        }

        super.onBlockPlacedBy(world, x, y, z, entityLivingBase, itemStack);

        TileEntityTownHall tileEntityTownHall = (TileEntityTownHall) world.getTileEntity(x, y, z);
        if(entityLivingBase instanceof EntityPlayer)
        {
            tileEntityTownHall.setInfo(world, entityLivingBase.getUniqueID(), x, z);
            tileEntityTownHall.markDirty();
            playerProperties.setHasPlacedTownHall(true);
        }
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z)
    {
        if(world.isRemote) return;
        super.onBlockAdded(world, x, y, z);

        TileEntityTownHall tileEntityTownHall = (TileEntityTownHall) world.getTileEntity(x, y, z);
        Random rand = new Random();
        tileEntityTownHall.setCityName(Configurations.cityNames[rand.nextInt(Configurations.cityNames.length)]);
        tileEntityTownHall.onBlockAdded();
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z)
    {
        if(world.provider.dimensionId == 0)
        {
            TileEntityTownHall tileEntityTownHall = Utils.getClosestTownHall(world, x, y, z);
            if(tileEntityTownHall != null && tileEntityTownHall.getDistanceFrom(x, y, z) < 200)
            {
                FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("Too close to existing townhall"));

                return false;
            }
        }
        return super.canPlaceBlockAt(world, x, y, z);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityTownHall();
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z)
    {
        if(world.isRemote) return super.removedByPlayer(world, player, x, y, z);

        if(super.removedByPlayer(world, player, x, y, z))
        {
            PlayerProperties.get(player).setHasPlacedTownHall(false);
            return true;
        }
        return false;
    }
}
