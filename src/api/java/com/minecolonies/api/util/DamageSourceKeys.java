package com.minecolonies.api.util;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public class DamageSourceKeys
{
    public static ResourceKey<DamageType> DESPAWN = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "despawn"));
    public static ResourceKey<DamageType> STUCK_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "stuckdamage"));
    public static ResourceKey<DamageType> SPEAR = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "spear"));
    public static ResourceKey<DamageType> NETHER = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "nether"));
    public static ResourceKey<DamageType> CONSOLE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "console"));
    public static ResourceKey<DamageType> SLAP = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "slap"));
    public static ResourceKey<DamageType> DEFAULT = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "default"));
    public static ResourceKey<DamageType> VISITOR = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "visitor"));
    public static ResourceKey<DamageType> WAKEY = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "wakeywakey"));
    public static ResourceKey<DamageType> TRAINING = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "training"));

}
