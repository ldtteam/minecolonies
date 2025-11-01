package com.minecolonies.core.entity.mobs.raider.pirates;

import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirateRaider;
import com.minecolonies.api.entity.mobs.pirates.ICaptainPirateEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import static com.minecolonies.api.entity.mobs.RaiderMobUtils.MOB_ATTACK_DAMAGE;
import static com.minecolonies.api.util.constant.RaiderConstants.BASE_ENV_DAMAGE_RESIST;

/**
 * Class for the Chief Pirate entity.
 */
public class EntityCaptainPirateRaider extends AbstractEntityPirateRaider implements ICaptainPirateEntity
{

    /**
     * Constructor of the entity.
     *
     * @param type    the entity type.
     * @param worldIn world to construct it in.
     */
    public EntityCaptainPirateRaider(final EntityType<? extends EntityCaptainPirateRaider> type, final Level worldIn)
    {
        super(type, worldIn);
    }

    @Override
    public void initStatsFor(final double baseHealth, final double difficulty, final double baseDamage)
    {
        super.initStatsFor(baseHealth, difficulty, baseDamage);
        this.getAttribute(Attributes.ARMOR).setBaseValue(-1);
        this.getAttribute(MOB_ATTACK_DAMAGE.get()).setBaseValue(baseDamage + 2.0);
        this.setEnvDamageInterval((int) (BASE_ENV_DAMAGE_RESIST * 2 * difficulty));
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHealth * 1.3);
        this.setHealth(this.getMaxHealth());
    }
}
