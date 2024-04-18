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
    public static ResourceKey<DamageType> GUARD = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "guard"));
    public static ResourceKey<DamageType> GUARD_PVP = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "guardpvp"));
    public static ResourceKey<DamageType> NORSEMENCHIEF = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "norsemenchief"));
    public static ResourceKey<DamageType> NORSEMENARCHER = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "norsemenarcher"));
    public static ResourceKey<DamageType> SHIELDMAIDEN = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "shieldmaiden"));
    public static ResourceKey<DamageType> BARBARIAN = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "barbarian"));
    public static ResourceKey<DamageType> CHIEFBARBARIAN = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "chiefbarbarian"));
    public static ResourceKey<DamageType> ARCHERBARBARIAN = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "archerbarbarian"));
    public static ResourceKey<DamageType> PIRATE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "pirate"));
    public static ResourceKey<DamageType> CHIEFPIRATE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "chiefpirate"));
    public static ResourceKey<DamageType> ARCHERPIRATE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "archerpirate"));
    public static ResourceKey<DamageType> MERCENARY = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "mercenary"));
    public static ResourceKey<DamageType> MUMMY = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "mummy"));
    public static ResourceKey<DamageType> PHARAO = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "pharao"));
    public static ResourceKey<DamageType> ARCHERMUMMY = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "archermummy"));
    public static ResourceKey<DamageType> AMAZON = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "amazon"));
    public static ResourceKey<DamageType> AMAZONSPEARMAN = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "amazonspearman"));
    public static ResourceKey<DamageType> AMAZONCHIEF = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "amazonchief"));
    public static ResourceKey<DamageType> DROWNED_PIRATE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "drownedpirate"));
    public static ResourceKey<DamageType> DROWNED_CHIEFPIRATE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "drownedchiefpirate"));
    public static ResourceKey<DamageType> DROWNED_ARCHERPIRATE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Constants.MOD_ID, "drownedarcherpirate"));

}
