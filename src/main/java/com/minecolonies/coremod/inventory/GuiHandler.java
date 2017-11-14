package com.minecolonies.coremod.inventory;

import com.minecolonies.coremod.colony.*;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.citizen.farmer.Field;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.tileentities.TileEntityRack;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import java.util.List;

/**
 * Class which handles the GUI inventory.
 */
public class GuiHandler implements IGuiHandler
{
    public enum ID
    {
        DEFAULT,
        BUILDING_INVENTORY,
        CITIZEN_INVENTORY
    }

    @Override
    public Object getServerGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z)
    {
        if (id==ID.DEFAULT.ordinal())
        {
            final BlockPos pos = new BlockPos(x, y, z);
            final TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity instanceof ScarecrowTileEntity)
            {
                return new Field((ScarecrowTileEntity) tileEntity, player.inventory, world, pos);
            }
            else if(tileEntity instanceof TileEntityRack)
            {
                return new ContainerRack((TileEntityRack) tileEntity, ((TileEntityRack) tileEntity).getOtherChest(), player.inventory, pos);
            }
        } else if (id==ID.BUILDING_INVENTORY.ordinal())
        {
            TileEntity entity = world.getTileEntity(new BlockPos(x,y,z));
            if (entity instanceof TileEntityColonyBuilding)
            {
                final TileEntityColonyBuilding tileEntityColonyBuilding = (TileEntityColonyBuilding) entity;
                final Colony colony = ColonyManager.getClosestColony(world, tileEntityColonyBuilding.getPos());

                return new ContainerMinecoloniesBuildingInventory(player.inventory, tileEntityColonyBuilding, player, colony.getID(), tileEntityColonyBuilding.getPos());
            }
        } else if (id==ID.CITIZEN_INVENTORY.ordinal())
        {
            final BlockPos target = new BlockPos(x,y,z);
            final Colony colony = ColonyManager.getClosestColony(world, target);

            final CitizenData citizen = colony.getCitizens().values().stream().filter(citizenData -> {
                final BlockPos citizenPos = new BlockPos(citizenData.getCitizenEntity().getPosition().getX(), citizenData.getCitizenEntity().getPosition().getY(),citizenData.getCitizenEntity().getPosition().getZ());
                return citizenPos.equals(target);
            }).findFirst().orElse(null);

            if (citizen == null)
                return null;

            final AbstractBuilding building = citizen.getWorkBuilding();
            
            return new ContainerMinecoloniesCitizenInventory(player.inventory, citizen.getCitizenEntity().getInventoryCitizen(), player, colony.getID(), building.getID(), citizen.getId());
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z)
    {
        if (id == ID.DEFAULT.ordinal())
        {
            final BlockPos pos = new BlockPos(x, y, z);
            final TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity instanceof ScarecrowTileEntity)
            {
                return new GuiField(player.inventory, (ScarecrowTileEntity) tileEntity, world, pos);
            }
            else if(tileEntity instanceof TileEntityRack)
            {
                return new GuiRack(player.inventory, (TileEntityRack) tileEntity, ((TileEntityRack) tileEntity).getOtherChest(), world, pos);
            } 
        }
        else if (id==ID.BUILDING_INVENTORY.ordinal())
        {
            TileEntity entity = world.getTileEntity(new BlockPos(x,y,z));
            if (entity instanceof TileEntityColonyBuilding)
            {
                final TileEntityColonyBuilding tileEntityColonyBuilding = (TileEntityColonyBuilding) entity;
                return new GuiChest(tileEntityColonyBuilding, player.inventory);
            }
        }
        else if (id==ID.CITIZEN_INVENTORY.ordinal())
        {
            final BlockPos target = new BlockPos(x,y,z);
            final ColonyView colony = ColonyManager.getClosestColonyView(world, target);

            final List<EntityCitizen> entityCitizen = world.getEntitiesWithinAABB(EntityCitizen.class, new AxisAlignedBB(target));

            if (citizen == null)
                return null;

            final EntityCitizen entityCitizen = (EntityCitizen) world.getEntityByID(citizen.getEntityId());
            entityCitizen.getInventoryCitizen().setCustomName(citizen.getName());

            return new GuiChest(entityCitizen.getInventoryCitizen(), player.inventory);
        }

        return null;
    }
}
