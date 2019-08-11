package com.minecolonies.coremod.inventory;

import com.ldtteam.structurize.api.util.constant.Constants;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContainers
{
    @ObjectHolder("crafting_furnace")
    public static ContainerType<ContainerGUICraftingFurnace> craftingFurnace;

    @ObjectHolder("bulding_inv")
    public static ContainerType<ContainerMinecoloniesBuildingInventory> buildingInv;

    @ObjectHolder("citizen_inv")
    public static ContainerType<ContainerMinecoloniesCitizenInventory> citizenInv;

    @ObjectHolder("rack_inv")
    public static ContainerType<ContainerRack> rackInv;

    @ObjectHolder("crafting_building")
    public static ContainerType<ContainerGUICrafting> craftingGrid;

    @ObjectHolder("field")
    public static ContainerType<ContainerField> field;

    @SubscribeEvent
    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
    {
        craftingFurnace = IForgeContainerType.create(ContainerGUICraftingFurnace::new).setRegistryName("crafting_furnace");
        buildingInv = IForgeContainerType.create(ContainerMinecoloniesBuildingInventory::new).setRegistryName("bulding_inv");
        citizenInv = IForgeContainerType.create(ContainerMinecoloniesCitizenInventory::new).setRegistryName("citizen_inv");
        rackInv = IForgeContainerType.create(ContainerRack::new).setRegistryName("rack_inv");
        craftingGrid = IForgeContainerType.create(ContainerGUICrafting::new).setRegistryName("crafting_building");
        field = IForgeContainerType.create(GuiField::new).setRegistryName("field");

        event.getRegistry().registerAll(craftingFurnace, buildingInv, citizenInv, rackInv, craftingGrid, field);
    }
}
