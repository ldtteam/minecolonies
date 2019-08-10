package com.minecolonies.coremod.inventory;

import com.ldtteam.structurize.api.util.constant.Constants;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MinecoloniesContainers
{
    @ObjectHolder("crafting_furnace")
    public static ContainerType<?> craftingFurnace;

    @ObjectHolder("crafting_building")
    public static ContainerType<?> craftingGrid;

    @ObjectHolder("field")
    public static ContainerType<ContainerField> field;

    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
    {
        craftingFurnace = IForgeContainerType.create(ContainerGUICraftingFurnace::new).setRegistryName("crafting_furnace");
        craftingGrid = IForgeContainerType.create(CraftingGUIBuilding::new).setRegistryName("crafting_building");
        field = IForgeContainerType.create(GuiField::new).setRegistryName("field");
    }
}
