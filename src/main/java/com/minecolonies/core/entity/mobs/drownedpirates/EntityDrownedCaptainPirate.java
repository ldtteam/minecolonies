package com.minecolonies.core.entity.mobs.drownedpirates;

import com.minecolonies.api.entity.mobs.drownedpirate.AbstractDrownedEntityPirate;
import com.minecolonies.api.entity.mobs.pirates.ICaptainPirateEntity;
import com.minecolonies.api.util.MathUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import static com.minecolonies.api.entity.mobs.RaiderMobUtils.MOB_ATTACK_DAMAGE;
import static com.minecolonies.api.util.constant.RaiderConstants.BASE_ENV_DAMAGE_RESIST;

/**
 * Class for the Chief Pirate entity.
 */
public class EntityDrownedCaptainPirate extends AbstractDrownedEntityPirate implements ICaptainPirateEntity
{
    /**
     * Constructor of the entity.
     *
     * @param type    the entity type.
     * @param worldIn world to construct it in.
     */
    public EntityDrownedCaptainPirate(final EntityType<? extends EntityDrownedCaptainPirate> type, final Level worldIn)
    {
        super(type, worldIn);
    }

    @Override
    public void initStatsFor(final double baseHealth, final double difficulty, final double baseDamage)
    {
        super.initStatsFor(baseHealth, difficulty, baseDamage);
        this.getAttribute(Attributes.ARMOR).setBaseValue(-1);
        this.getAttribute(MOB_ATTACK_DAMAGE.get()).setBaseValue(baseDamage);
        this.setEnvDamageInterval((int) (BASE_ENV_DAMAGE_RESIST * 2 * difficulty));
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHealth * 2.0);
        this.setHealth(this.getMaxHealth());
        if (MathUtils.RANDOM.nextInt(100) < 2)
        {
            setCustomName(Component.literal("Davy Jones"));
        }
    }
}
