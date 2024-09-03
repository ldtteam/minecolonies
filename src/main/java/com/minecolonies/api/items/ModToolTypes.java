package com.minecolonies.api.items;

import com.minecolonies.api.items.registry.ToolTypeEntry;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class ModToolTypes {
    public static String NONE_ID = "none";
    public static String PICKAXE_ID = "pickaxe";
    public static String SHOVEL_ID = "shovel";
    public static String AXE_ID = "axe";
    public static String HOE_ID = "hoe";
    public static String SWORD_ID = "sword";
    public static String BOW_ID = "bow";
    public static String FISHING_ROD_ID = "rod";
    public static String SHEARS_ID = "shears";
    public static String SHIELD_ID = "shield";
    public static String HELMET_ID = "helmet";
    public static String LEGGINGS_ID = "leggings";
    public static String CHESTPLATE_ID = "chestplate";
    public static String BOOTS_ID = "boots";
    public static String FLINT_AND_STEEL_ID = "flintandsteel";

    public static RegistryObject<ToolTypeEntry> none;
    public static RegistryObject<ToolTypeEntry> pickaxe;
    public static RegistryObject<ToolTypeEntry> shovel;
    public static RegistryObject<ToolTypeEntry> axe;
    public static RegistryObject<ToolTypeEntry> hoe;
    public static RegistryObject<ToolTypeEntry> sword;
    public static RegistryObject<ToolTypeEntry> bow;
    public static RegistryObject<ToolTypeEntry> fishing_rod;
    public static RegistryObject<ToolTypeEntry> shears;
    public static RegistryObject<ToolTypeEntry> shield;
    public static RegistryObject<ToolTypeEntry> helmet;
    public static RegistryObject<ToolTypeEntry> leggings;
    public static RegistryObject<ToolTypeEntry> chestplate;
    public static RegistryObject<ToolTypeEntry> boots;
    public static RegistryObject<ToolTypeEntry> flint_and_steel;

    public static List<ResourceLocation> toolTypes = new ArrayList<>();

    public static ToolTypeEntry getToolType(ResourceLocation location) {
        RegistryObject<ToolTypeEntry> registryObject = RegistryObject.create(location, new ResourceLocation(Constants.MOD_ID, "tooltypes"), Constants.MOD_ID);
        return registryObject.get();
    }
}
