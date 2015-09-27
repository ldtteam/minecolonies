package com.minecolonies.proxy;

import com.github.lunatrius.schematica.world.SchematicWorld;
import com.minecolonies.colony.CitizenData;

public interface IProxy
{
    boolean isClient();

    void registerTileEntities();

    void registerKeybindings();

    void registerEvents();

    void registerEntities();

    void registerEntityRendering();

    void registerTileEntityRendering();

    void showCitizenWindow(CitizenData.View citizen);
    void openBuildToolWindow(int x, int y, int z);

    //Schematica
    void setActiveSchematic(SchematicWorld world);

    SchematicWorld getActiveSchematic();
}
