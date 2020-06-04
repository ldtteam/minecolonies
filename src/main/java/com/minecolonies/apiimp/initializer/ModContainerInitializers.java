package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.inventory.container.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.containers.*;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContainerInitializers
{
    @SubscribeEvent
    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
    {
        ModContainers.craftingFurnace = IForgeContainerType.create(ContainerCraftingFurnace::new);
        ModContainers.craftingFurnace.setRegistryName("crafting_furnace");

        ModContainers.buildingInv = IForgeContainerType.create(ContainerBuildingInventory::new);
        ModContainers.buildingInv.setRegistryName("bulding_inv");

        ModContainers.citizenInv = IForgeContainerType.create(ContainerCitizenInventory::new);
        ModContainers.citizenInv.setRegistryName("citizen_inv");

        ModContainers.rackInv = IForgeContainerType.create(ContainerRack::new);
        ModContainers.rackInv.setRegistryName("rack_inv");

        ModContainers.craftingGrid = IForgeContainerType.create(ContainerCrafting::new);
        ModContainers.craftingGrid.setRegistryName("crafting_building");

        ModContainers.field = IForgeContainerType.create(ContainerField::new);
        ModContainers.field.setRegistryName("field");

        event.getRegistry()
            .registerAll(ModContainers.craftingFurnace,
                ModContainers.buildingInv,
                ModContainers.citizenInv,
                ModContainers.rackInv,
                ModContainers.craftingGrid,
                ModContainers.field);
    }

    @SubscribeEvent
    public static void doClientStuff(final FMLClientSetupEvent event)
    {
        ScreenManager.registerFactory(ModContainers.craftingFurnace, WindowFurnaceCrafting::new);
        ScreenManager.registerFactory(ModContainers.craftingGrid, WindowCrafting::new);

        ScreenManager.registerFactory(ModContainers.buildingInv, WindowBuildingInventory::new);
        ScreenManager.registerFactory(ModContainers.citizenInv, WindowCitizenInventory::new);
        ScreenManager.registerFactory(ModContainers.rackInv, WindowRack::new);
        ScreenManager.registerFactory(ModContainers.field, WindowField::new);
    }
}
