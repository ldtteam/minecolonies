package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.inventory.ModContainers;
import com.minecolonies.api.inventory.container.*;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.containers.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModContainerInitializers
{
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, Constants.MOD_ID);
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
    public static void doClientStuff(final RegisterMenuScreensEvent event)
    {
        event.register(ModContainers.craftingFurnace.get(), WindowFurnaceCrafting::new);
        event.register(ModContainers.craftingGrid.get(), WindowCrafting::new);
        event.register(ModContainers.craftingBrewingstand.get(), WindowBrewingstandCrafting::new);

        event.register(ModContainers.buildingInv.get(), WindowBuildingInventory::new);
        event.register(ModContainers.citizenInv.get(), WindowCitizenInventory::new);
        event.register(ModContainers.rackInv.get(), WindowRack::new);
        event.register(ModContainers.graveInv.get(), WindowGrave::new);
    }
}
