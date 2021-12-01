package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.inventory.container.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.containers.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContainerInitializers
{
    @SubscribeEvent
    public static void registerContainers(final RegistryEvent.Register<MenuType<?>> event)
    {
        ModContainers.craftingFurnace = (MenuType<ContainerCraftingFurnace>) IForgeMenuType.create(ContainerCraftingFurnace::fromFriendlyByteBuf).setRegistryName("crafting_furnace");
        ModContainers.buildingInv = (MenuType<ContainerBuildingInventory>) IForgeMenuType.create(ContainerBuildingInventory::fromFriendlyByteBuf).setRegistryName("bulding_inv");
        ModContainers.citizenInv = (MenuType<ContainerCitizenInventory>) IForgeMenuType.create(ContainerCitizenInventory::fromFriendlyByteBuf).setRegistryName("citizen_inv");
        ModContainers.craftingGrid = (MenuType<ContainerCrafting>) IForgeMenuType.create(ContainerCrafting::fromFriendlyByteBuf).setRegistryName("crafting_building");
        ModContainers.rackInv = (MenuType<ContainerRack>) IForgeMenuType.create(ContainerRack::fromFriendlyByteBuf).setRegistryName("rack_inv");
        ModContainers.graveInv = (MenuType<ContainerGrave>) IForgeMenuType.create(ContainerGrave::fromFriendlyByteBuf).setRegistryName("grave_inv");
        ModContainers.field = (MenuType<ContainerField>) IForgeMenuType.create(ContainerField::fromFriendlyByteBuf).setRegistryName("field");

        event.getRegistry()
          .registerAll(ModContainers.craftingFurnace, ModContainers.buildingInv, ModContainers.citizenInv, ModContainers.rackInv, ModContainers.graveInv, ModContainers.craftingGrid, ModContainers.field);
    }

    @SubscribeEvent
    public static void doClientStuff(final FMLClientSetupEvent event)
    {
        MenuScreens.register(ModContainers.craftingFurnace, WindowFurnaceCrafting::new);
        MenuScreens.register(ModContainers.craftingGrid, WindowCrafting::new);

        MenuScreens.register(ModContainers.buildingInv, WindowBuildingInventory::new);
        MenuScreens.register(ModContainers.citizenInv, WindowCitizenInventory::new);
        MenuScreens.register(ModContainers.rackInv, WindowRack::new);
        MenuScreens.register(ModContainers.graveInv, WindowGrave::new);
        MenuScreens.register(ModContainers.field, WindowField::new);
    }
}
