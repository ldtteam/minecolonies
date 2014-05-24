package com.minecolonies.tileentities;

import com.minecolonies.lib.Constants;

public class TileEntityHutBuilder extends TileEntityHutWorker
{
    private int checkInterfall;
    public TileEntityHutBuilder()
    {
        setMaxInhabitants(1);
        setHutName("hutBuilder");
        checkInterfall = Constants.BUILDERCHECKINTERFALL * 20;
    }



    @Override
    public void updateEntity()
    {
        super.updateEntity();
        checkInterfall--;
        if(checkInterfall == 0 && !getTownHall().getBuilderRequired().isEmpty())
        {
            int[] coord = {0,0,0};
            for(int[] coords : getTownHall().getBuilderRequired()) //TODO Not sure if the first index can be empty. This is for safety.
            {
                if(coords == null)
                {
                    continue;
                }
                coord = coords;
                break;
            }
            TileEntityBuildable tileEntityBuildable = (TileEntityHutBuilder) worldObj.getTileEntity(coord[0], coord[1], coord[2]);
            if(tileEntityBuildable != null)
            {
                startBuilding(tileEntityBuildable);
                checkInterfall = Constants.BUILDERCHECKINTERFALL * 20;
            }//TODO when do we remove the coord from the getBuilderRequired?
        }
    }

    private void startBuilding(TileEntityBuildable tileEntityBuildable)
    {

    }
}
