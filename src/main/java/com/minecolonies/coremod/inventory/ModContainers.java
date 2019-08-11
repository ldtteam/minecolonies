package com.minecolonies.coremod.inventory;

import com.ldtteam.structurize.api.util.constant.Constants;
import com.minecolonies.coremod.inventory.container.*;
import com.minecolonies.coremod.inventory.gui.*;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContainers
{
    @ObjectHolder("crafting_furnace")
    public static ContainerType<ContainerCraftingFurnace> craftingFurnace;

    @ObjectHolder("bulding_inv")
    public static ContainerType<ContainerBuildingInventory> buildingInv;

    @ObjectHolder("citizen_inv")
    public static ContainerType<ContainerCitizenInventory> citizenInv;

    @ObjectHolder("rack_inv")
    public static ContainerType<ContainerRack> rackInv;

    @ObjectHolder("crafting_building")
    public static ContainerType<ContainerCrafting> craftingGrid;

    @ObjectHolder("field")
    public static ContainerType<ContainerField> field;

    @SubscribeEvent
    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
    {
        craftingFurnace = (ContainerType<ContainerCraftingFurnace>) IForgeContainerType.create(ContainerCraftingFurnace::new).setRegistryName("crafting_furnace");
        buildingInv = (ContainerType<ContainerBuildingInventory>) IForgeContainerType.create(ContainerBuildingInventory::new).setRegistryName("bulding_inv");
        citizenInv = IForgeContainerType.create(ContainerCitizenInventory::new).setRegistryName("citizen_inv");
        rackInv = (ContainerType<ContainerRack>) IForgeContainerType.create(ContainerRack::new).setRegistryName("rack_inv");
        craftingGrid = (ContainerType<ContainerCrafting>) IForgeContainerType.create(ContainerCrafting::new).setRegistryName("crafting_building");
        field = (ContainerType<ContainerField>) IForgeContainerType.create(ContainerField::new).setRegistryName("field");

        event.getRegistry().registerAll(craftingFurnace, buildingInv, citizenInv, rackInv, craftingGrid, field);
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
