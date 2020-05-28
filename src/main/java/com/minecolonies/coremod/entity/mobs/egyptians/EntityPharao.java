package com.minecolonies.coremod.entity.mobs.egyptians;

import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import com.minecolonies.api.entity.mobs.egyptians.IPharaoEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.world.World;

import static com.minecolonies.api.entity.mobs.RaiderMobUtils.MOB_ATTACK_DAMAGE;
import static com.minecolonies.api.util.constant.RaiderConstants.BASE_ENV_DAMAGE_RESIST;
import static com.minecolonies.api.util.constant.RaiderConstants.CHIEF_BONUS_ARMOR;

/**
 * Class for the Pharao entity.
 */
public class EntityPharao extends AbstractEntityEgyptian implements IPharaoEntity
{

    /**
     * Constructor of the entity.
     * @param type the entity type.
     * @param worldIn world to construct it in.
     */
    public EntityPharao(final EntityType type, final World worldIn)
    {
        super(type, worldIn);
    }

    @Override
    public void initStatsFor(final double baseHealth, final double difficulty, final double baseDamage)
    {
        super.initStatsFor(baseHealth, difficulty, baseDamage);
        final double chiefArmor = difficulty * CHIEF_BONUS_ARMOR;
        this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(chiefArmor);
        this.getAttribute(MOB_ATTACK_DAMAGE).setBaseValue(baseDamage + 1.0);
        this.setEnvDamageInterval((int) (BASE_ENV_DAMAGE_RESIST * 2 * difficulty));
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(baseHealth * 4.5);
        this.setHealth(this.getMaxHealth());
    }
}
