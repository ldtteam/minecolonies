package com.minecolonies.coremod.compatibility.jade;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.coremod.blocks.BlockDecorationController;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import org.jetbrains.annotations.NotNull;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade plugin entry point.
 */
@WailaPlugin
public class JadePlugin implements IWailaPlugin
{
    @Override
    public void register(@NotNull final IWailaCommonRegistration registration)
    {
        registration.registerBlockDataProvider(BuildingNameComponentProvider.getInstance(), AbstractTileEntityColonyBuilding.class);
        registration.registerBlockDataProvider(BuildingPackComponentProvider.getInstance(), AbstractTileEntityColonyBuilding.class);
        registration.registerBlockDataProvider(BuildingNameComponentProvider.getInstance(), TileEntityDecorationController.class);
        registration.registerBlockDataProvider(BuildingPackComponentProvider.getInstance(), TileEntityDecorationController.class);
    }

    @Override
    public void registerClient(@NotNull final IWailaClientRegistration registration)
    {
        registration.registerBlockComponent(BuildingNameComponentProvider.getInstance(), AbstractBlockHut.class);
        registration.registerBlockComponent(BuildingPackComponentProvider.getInstance(), AbstractBlockHut.class);
        registration.registerBlockComponent(BuildingNameComponentProvider.getInstance(), BlockDecorationController.class);
        registration.registerBlockComponent(BuildingPackComponentProvider.getInstance(), BlockDecorationController.class);
        registration.registerBlockComponent(CitizenListComponentProvider.getInstance(), AbstractBlockHut.class);

        registration.registerEntityComponent(ColonistComponentProvider.getInstance(), AbstractEntityCitizen.class);
        registration.registerEntityComponent(VisitorRecruitmentComponentProvider.getInstance(), AbstractEntityCitizen.class);
    }
}
