package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.coremod.util.BarbarianUtils;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created by Asher on 19/6/17.
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
        BarbarianUtils.setBarbarianAITasks(this);
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        BarbarianUtils.setBarbarianAttributes(this);
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable()
    {
        return BarbarianUtils.getBarbarianLootTables(this);
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(final DifficultyInstance difficulty, @Nullable final IEntityLivingData livingdata)
    {
        BarbarianUtils.setBarbarianEquipment(this);
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
