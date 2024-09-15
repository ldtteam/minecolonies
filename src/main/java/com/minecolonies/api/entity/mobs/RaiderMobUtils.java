package com.minecolonies.api.entity.mobs;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.mobs.barbarians.IChiefBarbarianEntity;
import com.minecolonies.api.entity.mobs.barbarians.IMeleeBarbarianEntity;
import com.minecolonies.api.entity.mobs.egyptians.IPharaoEntity;
import com.minecolonies.api.entity.mobs.pirates.ICaptainPirateEntity;
import com.minecolonies.api.entity.mobs.pirates.IPirateEntity;
import com.minecolonies.api.entity.mobs.vikings.IMeleeNorsemenEntity;
import com.minecolonies.api.entity.mobs.vikings.INorsemenChiefEntity;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.RaiderConstants.*;

/**
 * Util class for raider mobs/spawning
 */
public final class RaiderMobUtils
{
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, Constants.MOD_ID);

    /**
     * Mob attribute, used for custom attack damage
     */
    public final static DeferredHolder<Attribute, RangedAttribute> MOB_ATTACK_DAMAGE = ATTRIBUTES.register("mc_mob_damage", () -> new RangedAttribute( "mc_mob_damage", 2.0, 1.0, 20));

    /**
     * Damage increased by 1 for every 200 raid level difficulty
     */
    public static int DAMAGE_PER_X_RAID_LEVEL = 400;

    /**
     * Max damage from raidlevels
     */
    public static int MAX_RAID_LEVEL_DAMAGE = 3;

    private RaiderMobUtils()
    {
        throw new IllegalStateException("Tried to initialize: MobSpawnUtils but this is a Utility class.");
    }

    /**
     * Set mob attributes.
     *
     * @param mob    The mob to set the attributes on.
     * @param colony The colony that the mob is attacking.
     */
    public static void setMobAttributes(final AbstractEntityRaiderMob mob, final IColony colony)
    {
        final double difficultyModifier = colony.getRaiderManager().getRaidDifficultyModifier();
        mob.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(FOLLOW_RANGE * 2);
        mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(difficultyModifier < 2.4 ? MOVEMENT_SPEED : MOVEMENT_SPEED * 1.2);
        final int raidLevel = colony.getRaiderManager().getColonyRaidLevel();

        // Base damage
        final double attackDamage =
          ATTACK_DAMAGE +
            difficultyModifier *
              Math.min(raidLevel / DAMAGE_PER_X_RAID_LEVEL, MAX_RAID_LEVEL_DAMAGE);

        // Base health
        final double baseHealth = getHealthBasedOnRaidLevel(raidLevel) * difficultyModifier;

        mob.initStatsFor(baseHealth, difficultyModifier, attackDamage);
    }

    /**
     * Sets the entity's health based on the raidLevel
     *
     * @param raidLevel the raid level.
     * @return returns the health in the form of a double
     */
    public static double getHealthBasedOnRaidLevel(final int raidLevel)
    {
        return Math.max(BARBARIAN_BASE_HEALTH, (BARBARIAN_BASE_HEALTH + raidLevel * BARBARIAN_HEALTH_MULTIPLIER));
    }

    /**
     * Sets up and spawns the Barbarian entities of choice
     *
     * @param entityToSpawn  The entity which should be spawned
     * @param numberOfSpawns The number of times the entity should be spawned
     * @param spawnLocation  the location at which to spawn the entity
     * @param world          the world in which the colony and entity are
     * @param colony         the colony to spawn them close to.
     * @param eventID        the event id.
     */
    public static void spawn(
      final EntityType<?> entityToSpawn,
      final int numberOfSpawns,
      final BlockPos spawnLocation,
      final Level world,
      final IColony colony,
      final int eventID)
    {
        if (spawnLocation != null && entityToSpawn != null && world != null && numberOfSpawns > 0)
        {
            int spawnDeviationX = 0;
            int spawnDeviationZ = 0;

            for (int i = 0; i < numberOfSpawns; i++)
            {
                final AbstractEntityRaiderMob entity = (AbstractEntityRaiderMob) entityToSpawn.create(world);

                if (entity != null)
                {
                    BlockPos spawnpos = BlockPosUtil.findAround(world, spawnLocation.offset(spawnDeviationX, 0, spawnDeviationZ), 5, 5, BlockPosUtil.SOLID_AIR_POS_SELECTOR);
                    if (spawnpos == null)
                    {
                        spawnpos = spawnLocation.above();
                    }

                    entity.absMoveTo(spawnpos.getX(), spawnpos.getY(), spawnpos.getZ(), (float) Mth.wrapDegrees(world.random.nextDouble() * WHOLE_CIRCLE), 0.0F);
                    CompatibilityUtils.addEntity(world, entity);
                    entity.setColony(colony);
                    entity.setEventID(eventID);
                    entity.registerWithColony();
                    spawnDeviationZ += 1;

                    if (spawnDeviationZ > 5)
                    {
                        spawnDeviationZ = 0;
                        spawnDeviationX += 1;
                    }
                }
            }
        }
    }

    /**
     * Set the equipment of a certain mob.
     *
     * @param mob the equipment to set up.
     */
    public static void setEquipment(final AbstractEntityRaiderMob mob)
    {
        if (mob instanceof IMeleeBarbarianEntity || mob instanceof IMeleeNorsemenEntity || mob instanceof INorsemenChiefEntity)
        {
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
        }
        else if (mob instanceof IPharaoEntity)
        {
            mob.setItemSlot(EquipmentSlot.MAINHAND, ModItems.pharaoscepter.toStack());
        }
        else if (mob instanceof IArcherMobEntity)
        {
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        }
        else if (mob instanceof ISpearmanMobEntity)
        {
            mob.setItemSlot(EquipmentSlot.MAINHAND, ModItems.spear.toStack());
        }
        else if (mob instanceof IChiefBarbarianEntity)
        {
            mob.setItemSlot(EquipmentSlot.MAINHAND, ModItems.chiefSword.toStack());
            mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
            mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
            mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
        }
        else if (mob instanceof IPirateEntity)
        {
            mob.setItemSlot(EquipmentSlot.MAINHAND, ModItems.scimitar.toStack());
            if (mob instanceof ICaptainPirateEntity)
            {
                if (new Random().nextBoolean())
                {
                    mob.setItemSlot(EquipmentSlot.HEAD, ModItems.pirateHelmet_1.toStack());
                    mob.setItemSlot(EquipmentSlot.CHEST, ModItems.pirateChest_1.toStack());
                    mob.setItemSlot(EquipmentSlot.LEGS, ModItems.pirateLegs_1.toStack());
                    mob.setItemSlot(EquipmentSlot.FEET, ModItems.pirateBoots_1.toStack());
                }
                else
                {
                    mob.setItemSlot(EquipmentSlot.HEAD, ModItems.pirateHelmet_2.toStack());
                    mob.setItemSlot(EquipmentSlot.CHEST, ModItems.pirateChest_2.toStack());
                    mob.setItemSlot(EquipmentSlot.LEGS, ModItems.pirateLegs_2.toStack());
                    mob.setItemSlot(EquipmentSlot.FEET, ModItems.pirateBoots_2.toStack());
                }
            }
        }
    }

    /**
     * Returns the barbarians close to an entity.
     *
     * @param entity             The entity to test against
     * @param distanceFromEntity The distance to check for
     * @return the barbarians (if any) that is nearest
     */
    public static List<AbstractEntityRaiderMob> getBarbariansCloseToEntity(final Entity entity, final double distanceFromEntity)
    {
        return CompatibilityUtils.getWorldFromEntity(entity).getEntitiesOfClass(
          AbstractEntityRaiderMob.class,
          entity.getBoundingBox().expandTowards(
            distanceFromEntity,
            3.0D,
            distanceFromEntity),
          Entity::isAlive);
    }
}
