package com.minecolonies.coremod.inventory;

import com.minecolonies.coremod.client.gui.WindowGuiCrafting;
import com.minecolonies.coremod.colony.*;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import com.minecolonies.coremod.tileentities.TileEntityRack;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Class which handles the GUI inventory.
 */
public class GuiHandler implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(final int id, final EntityPlayer player, final World world, final int x, final int y, final int z)
    {
        if (id == ID.DEFAULT.ordinal())
        {
            final BlockPos pos = new BlockPos(x, y, z);
            final TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof ScarecrowTileEntity)
            {
                return new ContainerField((ScarecrowTileEntity) tileEntity, player.inventory, world, pos);
            }
            else if (tileEntity instanceof TileEntityRack)
            {
                return new ContainerRack((TileEntityRack) tileEntity, ((TileEntityRack) tileEntity).getOtherChest(), player.inventory);
            }
            else
            {
                @Nullable final AbstractBuildingView building = ColonyManager.getBuildingView(new BlockPos(x,y,z));
                if (building != null)
                {
                    return new CraftingGUIBuilding(player.inventory, world);
                }
                return null;
            }
        }
        else if (id == ID.BUILDING_INVENTORY.ordinal())
        {
            final TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
            if (entity instanceof TileEntityColonyBuilding)
            {
                final TileEntityColonyBuilding tileEntityColonyBuilding = (TileEntityColonyBuilding) entity;
                final Colony colony = ColonyManager.getClosestColony(world, tileEntityColonyBuilding.getPos());

                return new ContainerMinecoloniesBuildingInventory(player.inventory, tileEntityColonyBuilding, colony.getID(), tileEntityColonyBuilding.getPos());
            }
        }
        else if (id == ID.CITIZEN_INVENTORY.ordinal())
        {
            final Colony colony = ColonyManager.getColony(x);
            final CitizenData citizen = colony.getCitizenManager().getCitizen(y);
            final AbstractBuilding building = citizen.getWorkBuilding() == null ? null : citizen.getWorkBuilding();

            return new ContainerMinecoloniesCitizenInventory(player.inventory,
                                                              citizen.getCitizenEntity().getInventoryCitizen(),
                                                              colony.getID(),
                                                              building == null ? null : building.getID(),
                                                              citizen.getId());
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
            if (tileEntity instanceof ScarecrowTileEntity)
            {
                return new GuiField(player.inventory, (ScarecrowTileEntity) tileEntity, world, pos);
            }
            else if (tileEntity instanceof TileEntityRack)
            {
                return new GuiRack(player.inventory, (TileEntityRack) tileEntity, ((TileEntityRack) tileEntity).getOtherChest(), world, pos);
            }
            else
            {
                @Nullable final AbstractBuildingView building = ColonyManager.getBuildingView(new BlockPos(x,y,z));
                if (building instanceof AbstractBuildingWorker.View)
                {
                    return new WindowGuiCrafting(player.inventory, world, (AbstractBuildingWorker.View) building);
                }
            }
        }
        else if (id == ID.BUILDING_INVENTORY.ordinal())
        {
            final TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
            if (entity instanceof TileEntityColonyBuilding)
            {
                final TileEntityColonyBuilding tileEntityColonyBuilding = (TileEntityColonyBuilding) entity;
                return new GuiChest(player.inventory, tileEntityColonyBuilding);
            }
        }
        else if (id == ID.CITIZEN_INVENTORY.ordinal())
        {
            final ColonyView view = ColonyManager.getColonyView(x);
            final CitizenDataView citizenDataView = view.getCitizen(y);

            return new GuiChest(player.inventory, citizenDataView.getInventory());
        }
        return null;
    }

    public enum ID
    {
        DEFAULT,
        BUILDING_INVENTORY,
        CITIZEN_INVENTORY
    }
}
