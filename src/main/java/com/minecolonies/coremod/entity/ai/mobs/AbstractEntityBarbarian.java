package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.coremod.util.BarbarianSpawnUtils;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Abstract Class for barbarians
 */
public abstract class AbstractEntityBarbarian extends EntityMob
{
    /**
     * Constructor method for our abstract class
     * @param worldIn The world that the Barbarian is in
     */
    AbstractEntityBarbarian(final World worldIn)
    {
        super(worldIn);
    }

    @Override
    protected void initEntityAI()
    {
        BarbarianSpawnUtils.setBarbarianAITasks(this);
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        BarbarianSpawnUtils.setBarbarianAttributes(this);
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable()
    {
        return BarbarianSpawnUtils.getBarbarianLootTables(this);
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(final DifficultyInstance difficulty, @Nullable final IEntityLivingData livingdata)
    {
        BarbarianSpawnUtils.setBarbarianEquipment(this);
        return super.onInitialSpawn(difficulty, livingdata);
    }

    /**
     * We have loot_tables for a reason, this is done to disable equipped items dropping on death.
     * @param wasRecentlyHit Was the barbarian recently hit?
     * @param lootingModifier Was the barbarian hit with a sword with looting Enchantment?
     */
    @Override
    protected void dropEquipment(final boolean wasRecentlyHit, final int lootingModifier)
    {
    }
}
