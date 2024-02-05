package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.inventory.container.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.containers.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContainerInitializers
{
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Constants.MOD_ID);
    static
    {
        ModContainers.craftingFurnace = CONTAINERS.register("crafting_furnace", () -> IMenuTypeExtension.create(ContainerCraftingFurnace::fromFriendlyByteBuf));
        ModContainers.buildingInv = CONTAINERS.register("building_inv", () -> IMenuTypeExtension.create(ContainerBuildingInventory::fromFriendlyByteBuf));
        ModContainers.citizenInv = CONTAINERS.register("citizen_inv", () -> IMenuTypeExtension.create(ContainerCitizenInventory::fromFriendlyByteBuf));
        ModContainers.craftingGrid = CONTAINERS.register("crafting_building", () -> IMenuTypeExtension.create(ContainerCrafting::fromFriendlyByteBuf));
        ModContainers.rackInv = CONTAINERS.register("rack_inv", () -> IMenuTypeExtension.create(ContainerRack::fromFriendlyByteBuf));
        ModContainers.graveInv = CONTAINERS.register("grave_inv", () -> IMenuTypeExtension.create(ContainerGrave::fromFriendlyByteBuf));
        ModContainers.craftingBrewingstand = CONTAINERS.register("crafting_brewingstand", () -> IMenuTypeExtension.create(ContainerCraftingBrewingstand::fromFriendlyByteBuf));
    }
    @SubscribeEvent
    public static void doClientStuff(final FMLClientSetupEvent event)
    {
        MenuScreens.register(ModContainers.craftingFurnace.get(), WindowFurnaceCrafting::new);
        MenuScreens.register(ModContainers.craftingGrid.get(), WindowCrafting::new);
        MenuScreens.register(ModContainers.craftingBrewingstand.get(), WindowBrewingstandCrafting::new);

        MenuScreens.register(ModContainers.buildingInv.get(), WindowBuildingInventory::new);
        MenuScreens.register(ModContainers.citizenInv.get(), WindowCitizenInventory::new);
        MenuScreens.register(ModContainers.rackInv.get(), WindowRack::new);
        MenuScreens.register(ModContainers.graveInv.get(), WindowGrave::new);
    }
}
