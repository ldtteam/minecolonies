package com.minecolonies.coremod.inventory;

import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.coremod.client.gui.WindowGuiCrafting;
import com.minecolonies.coremod.client.gui.WindowGuiFurnaceCrafting;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingSmelterCrafter;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.tileentities.TileEntityScarecrow;
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
            if (tileEntity instanceof TileEntityScarecrow)
            {
                return new ContainerField((TileEntityScarecrow) tileEntity, player.inventory, world, pos);
            }
            else if (tileEntity instanceof TileEntityColonyBuilding)
            {
                @Nullable final IBuilding building = IColonyManager.getInstance().getBuilding(world, new BlockPos(x,y,z));
                if (building instanceof AbstractBuildingSmelterCrafter)
                {
                    return new ContainerGUICraftingFurnace(player.inventory, world);
                }
                else if (building instanceof AbstractBuildingWorker)
                {
                    return new CraftingGUIBuilding(player.inventory, world, ((IBuildingWorker) building).canCraftComplexRecipes());
                }
                return null;
            }
            else if (tileEntity instanceof TileEntityRack)
            {
                return new ContainerRack((AbstractTileEntityRack) tileEntity, ((AbstractTileEntityRack) tileEntity).getOtherChest(), player.inventory);
            }
            return null;
        }
        else if (id == ID.BUILDING_INVENTORY.ordinal())
        {
            final TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
            if (entity instanceof TileEntityColonyBuilding)
            {
                final TileEntityColonyBuilding tileEntityColonyBuilding = (TileEntityColonyBuilding) entity;
                final IColony colony = IColonyManager.getInstance().getClosestColony(world, tileEntityColonyBuilding.getPos());

                return new ContainerMinecoloniesBuildingInventory(player.inventory,
                  tileEntityColonyBuilding.getInventory(),
                  colony.getID(),
                  tileEntityColonyBuilding.getPos(),
                  world);
            }
        }
        else if (id == ID.CITIZEN_INVENTORY.ordinal())
        {
            final IColony colony = IColonyManager.getInstance().getColonyByWorld(x, world);
            final ICitizenData citizen = colony.getCitizenManager().getCitizen(y);
            final IBuilding building = citizen.getWorkBuilding();

            return new ContainerMinecoloniesCitizenInventory(player.inventory,
                                                              citizen.getInventory(),
                                                              colony.getID(),
                                                              building == null ? null : building.getID(),
                                                              citizen.getId(), world);
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
            if (tileEntity instanceof TileEntityScarecrow)
            {
                return new GuiField(player.inventory, (TileEntityScarecrow) tileEntity, world, pos);
            }
            else if (tileEntity instanceof TileEntityColonyBuilding)
            {
                @Nullable final IBuildingView building = IColonyManager.getInstance().getBuildingView(player.world.provider.getDimension(), new BlockPos(x,y,z));
                if (building instanceof AbstractBuildingSmelterCrafter.View)
                {
                    return new WindowGuiFurnaceCrafting(player.inventory, world, (AbstractBuildingSmelterCrafter.View) building);
                }
                else if (building instanceof AbstractBuildingWorker.View)
                {
                    return new WindowGuiCrafting(player.inventory, world, (AbstractBuildingWorker.View) building);
                }
            }
            else if (tileEntity instanceof TileEntityRack)
            {
                return new GuiRack(player.inventory, (AbstractTileEntityRack) tileEntity, ((AbstractTileEntityRack) tileEntity).getOtherChest(), world, pos);
            }
        }
        else if (id == ID.BUILDING_INVENTORY.ordinal())
        {
            final TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
            if (entity instanceof TileEntityColonyBuilding)
            {
                final AbstractTileEntityColonyBuilding ITileEntityColonyBuilding = (AbstractTileEntityColonyBuilding) entity;
                return new GuiRack(player.inventory, ITileEntityColonyBuilding, ITileEntityColonyBuilding.getOtherChest(), world, ITileEntityColonyBuilding.getPosition());
            }
        }
        else if (id == ID.CITIZEN_INVENTORY.ordinal())
        {
            final IColonyView view = IColonyManager.getInstance().getColonyView(x, player.world.provider.getDimension());
            final ICitizenDataView citizenDataView = view.getCitizen(y);

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
