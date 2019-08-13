package com.minecolonies.api.entity;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.EntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
public class ModEntities
{
    @ObjectHolder("citizen")
    public static EntityType<?> CITIZEN;

    @ObjectHolder("fishhook")
    public static EntityType<?> FISHHOOK;

    @ObjectHolder("barbarian")
    public static EntityType<?> BARBARIAN;

    @ObjectHolder("mercenary")
    public static EntityType<?> MERCENARY;

    @ObjectHolder("archerbarbarian")
    public static EntityType<?> ARCHERBARBARIAN;

    @ObjectHolder("chiefbarbarian")
    public static EntityType<?> CHIEFBARBARIAN;

    @ObjectHolder("pirate")
    public static EntityType<?> PIRATE;

    @ObjectHolder("chiefpirate")
    public static EntityType<?> CHIEFPIRATE;

    @ObjectHolder("archerpirate")
    public static EntityType<?> ARCHERPIRATE;
}
