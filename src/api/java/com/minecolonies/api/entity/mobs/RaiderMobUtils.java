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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.RaiderConstants.*;

/**
 * Util class for raider mobs/spawning
 */
public final class RaiderMobUtils
{

    /**
     * Distances in which spawns are spread
     */
    public static double MOB_SPAWN_DEVIATION_STEPS = 0.3;

    /**
     * Mob attribute, used for custom attack damage
     */
    public final static Attribute MOB_ATTACK_DAMAGE = new RangedAttribute( "mc_mob_damage", 2.0, 1.0, 20);

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
    public static void setMobAttributes(final AbstractEntityMinecoloniesMob mob, final IColony colony)
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
      final World world,
      final IColony colony,
      final int eventID)
    {
        if (spawnLocation != null && entityToSpawn != null && world != null && numberOfSpawns > 0)
        {
            final int x = spawnLocation.getX();
            final int y = BlockPosUtil.getFloor(spawnLocation, world).getY();
            final int z = spawnLocation.getZ();
            double spawnDeviationX = 0;
            double spawnDeviationZ = 0;

            for (int i = 0; i < numberOfSpawns; i++)
            {
                final AbstractEntityMinecoloniesMob entity = (AbstractEntityMinecoloniesMob) entityToSpawn.create(world);

                if (entity != null)
                {
                    entity.absMoveTo(x + spawnDeviationX, y + 1.0, z + spawnDeviationZ, (float) MathHelper.wrapDegrees(world.random.nextDouble() * WHOLE_CIRCLE), 0.0F);
                    CompatibilityUtils.addEntity(world, entity);
                    entity.setColony(colony);
                    entity.setEventID(eventID);
                    entity.registerWithColony();
                    spawnDeviationZ += MOB_SPAWN_DEVIATION_STEPS;

                    if (spawnDeviationZ > 2)
                    {
                        spawnDeviationZ = 0;
                        spawnDeviationX += MOB_SPAWN_DEVIATION_STEPS;
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
    public static void setEquipment(final AbstractEntityMinecoloniesMob mob)
    {
        if (mob instanceof IMeleeBarbarianEntity || mob instanceof IMeleeNorsemenEntity || mob instanceof INorsemenChiefEntity)
        {
            mob.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.STONE_AXE));
        }
        else if (mob instanceof IPharaoEntity)
        {
            mob.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(ModItems.pharaoscepter));
        }
        else if (mob instanceof IArcherMobEntity)
        {
            mob.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.BOW));
        }
        else if (mob instanceof ISpearmanMobEntity) {
            mob.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(ModItems.spear));
        }
        else if (mob instanceof IChiefBarbarianEntity)
        {
            mob.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(ModItems.chiefSword));
            mob.setItemSlot(EquipmentSlotType.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
            mob.setItemSlot(EquipmentSlotType.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            mob.setItemSlot(EquipmentSlotType.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
            mob.setItemSlot(EquipmentSlotType.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
        }
        else if (mob instanceof IPirateEntity)
        {
            mob.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(ModItems.scimitar));
            if (mob instanceof ICaptainPirateEntity)
            {
                if (new Random().nextBoolean())
                {
                    mob.setItemSlot(EquipmentSlotType.HEAD, new ItemStack(ModItems.pirateHelmet_1));
                    mob.setItemSlot(EquipmentSlotType.CHEST, new ItemStack(ModItems.pirateChest_1));
                    mob.setItemSlot(EquipmentSlotType.LEGS, new ItemStack(ModItems.pirateLegs_1));
                    mob.setItemSlot(EquipmentSlotType.FEET, new ItemStack(ModItems.pirateBoots_1));
                }
                else
                {
                    mob.setItemSlot(EquipmentSlotType.HEAD, new ItemStack(ModItems.pirateHelmet_2));
                    mob.setItemSlot(EquipmentSlotType.CHEST, new ItemStack(ModItems.pirateChest_2));
                    mob.setItemSlot(EquipmentSlotType.LEGS, new ItemStack(ModItems.pirateLegs_2));
                    mob.setItemSlot(EquipmentSlotType.FEET, new ItemStack(ModItems.pirateBoots_2));
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
    public static List<AbstractEntityMinecoloniesMob> getBarbariansCloseToEntity(final Entity entity, final double distanceFromEntity)
    {
        return CompatibilityUtils.getWorldFromEntity(entity).getLoadedEntitiesOfClass(
          AbstractEntityMinecoloniesMob.class,
          entity.getBoundingBox().expandTowards(
            distanceFromEntity,
            3.0D,
            distanceFromEntity),
          Entity::isAlive);
    }
}
