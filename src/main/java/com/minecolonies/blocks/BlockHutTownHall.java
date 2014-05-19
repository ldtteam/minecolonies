package com.minecolonies.blocks;

import com.minecolonies.MineColonies;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.PlayerProperties;
import com.minecolonies.lib.Constants;
import com.minecolonies.tileentities.TileEntityTownHall;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

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
    public String getName()
    {
        return name;
    }

    //TODO Check that huts are within the range of the townhall and aren't already bound to an existing townhall.
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack)
    {
        if(world.isRemote) return;

        if(entityLivingBase instanceof EntityPlayer)
        {
            EntityPlayer entityPlayer = (EntityPlayer) entityLivingBase;

            if (!world.provider.isSurfaceWorld())
            {
                world.setBlockToAir(x, y, z);
                LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "tile.blockHutTownhall.messageInvalidWorld");
                removedByPlayer(world, entityPlayer, x, y, z);
                return;
            }

            TileEntityTownHall closestTownHall = Utils.getClosestTownHall(world, x, y, z, true);
            if(closestTownHall != null && closestTownHall.getDistanceFrom(x, y, z) < 200)
            {
                world.setBlockToAir(x, y, z);
                LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "tile.blockHutTownhall.messageTooClose");
                removedByPlayer(world, entityPlayer, x, y, z);
                return;
            }

            PlayerProperties playerProperties = PlayerProperties.get(entityPlayer);
            if(playerProperties.hasPlacedTownHall())
            {
                world.setBlockToAir(x, y, z);
                LanguageHandler.sendPlayerLocalizedMessage(entityPlayer, "tile.blockHutTownhall.messagePlacedAlready");
                removedByPlayer(world, entityPlayer, x, y, z);
                return;
            }

            TileEntityTownHall tileEntityTownHall = (TileEntityTownHall) world.getTileEntity(x, y, z);
            tileEntityTownHall.onBlockAdded();
            tileEntityTownHall.setInfo(world, entityPlayer.getUniqueID(), x, z);
            tileEntityTownHall.setCityName(LanguageHandler.format("com.minecolonies.gui.townhall.defaultName", entityPlayer.getDisplayName()));
            playerProperties.placeTownhall(x, y, z);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityTownHall();
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z)
    {
        if(world.isRemote) return false;

        if(this.canPlayerDestroy(world, x, y, z, player))
        {
            if(world.getTileEntity(x, y, z) instanceof TileEntityTownHall)
            {
                /*
                Note, not enhanced yet
                 */
                TileEntityTownHall tileEntityTownHall = (TileEntityTownHall) world.getTileEntity(x, y, z);
                List<Entity> loadedEntities = world.getLoadedEntityList();
                List<UUID> townhallList = tileEntityTownHall.getCitizens();
                for(Entity entity : loadedEntities)
                    for(UUID uuid : townhallList)
                        if(entity.getPersistentID().equals(uuid)) entity.setDead();
                PlayerProperties.get(player).removeTownhall();
            }
            return super.removedByPlayer(world, player, x, y, z);
        }
        return false;
    }

// //TODO Delete this to open GUI again, atm used for testing entities
// @Override
// public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
// {
//     if(world.isRemote) return false;
//
//     TileEntityTownHall tileEntityTownHall = (TileEntityTownHall) world.getTileEntity(x, y, z);
//     if(tileEntityTownHall.getMaxCitizens() > tileEntityTownHall.getCitizens().size()) //TODO Change to be checked when spawned.
//     {
//         EntityCitizen entityCitizen = new EntityCitizen(world);
//         entityCitizen.setLocationAndAngles(x, y, z, 1f, 1f);
//         world.spawnEntityInWorld(entityCitizen);
//         tileEntityTownHall.addCitizenToTownhall(entityCitizen);
//         return true;
//     }
//     return false;
// }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
    {
        entityPlayer.openGui(MineColonies.instance, Constants.Gui.TownHall.ordinal(), world, x, y, z);
        return true;
    }

    public boolean canPlayerDestroy(World world, int x, int y, int z, Entity entity)
    {
        EntityPlayer entityPlayer = (EntityPlayer) entity;
        TileEntityTownHall tileEntityTownHall = (TileEntityTownHall) world.getTileEntity(x, y, z);
        if(tileEntityTownHall == null) return true;
        if(tileEntityTownHall.getOwners().size() == 0) return true;
        for(int i = 0; i < tileEntityTownHall.getOwners().size(); i++)
        {
            if(tileEntityTownHall.getOwners().get(i).equals(entityPlayer.getUniqueID()))
            {
                return true;
            }
        }
        return false;
    }
}
