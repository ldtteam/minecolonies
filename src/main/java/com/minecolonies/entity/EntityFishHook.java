package com.minecolonies.entity;

import com.minecolonies.util.MathUtils;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.util.EnumParticleTypes.*;

/**
 * Creates a custom fishHook for the Fisherman to throw
 * This class manages the entity
 */
public final class EntityFishHook extends Entity
{
    /**
     * Number of seconds to wait before removing a stuck hook
     */
    private static final int TTL = 360;

    /**
     * Entity size to scale it down
     */
    private static final float ENTITY_SIZE = 0.25F;

    /**
     * 180 degree used in trig. functions
     */
    private static final double HALF_CIRCLE = 180.0;

    /**
     * Used as a speed multiplicator for drifting movement
     */
    private static final double RANDOM_MOVEMENT_OFFSET = 0.007499999832361937;

    /**
     * Limits initial horizontal movement speed
     */
    private static final double INITIAL_MOVEMENT_LIMITER = 0.16;

    /**
     * The hook starts a bit lower
     */
    private static final double SUNKEN_OFFSET = 0.10000000149011612;

    /**
     * Multiplicator to get sum of edges
     */
    private static final double NUM_BOUNDING_BOX_EDGES = 4.0;

    /**
     * factor to scale up to distance
     */
    private static final double DISTANCE_FACTOR = 64.0;

    /**
     * Limits horizontal movement speed while bouncing
     */
    private static final double BOUNCE_MOVEMENT_LIMITER = 0.2;

    /**
     * Limits horizontal movement speed while in air
     */
    private static final float AIR_MOVEMENT_LIMITER = 0.92F;

    /**
     * Limits horizontal movement speed while on the ground
     */
    private static final double GROUND_MOVEMENT_LIMITER = 0.5;

    /**
     * Limits horizontal movement speed while in the water
     */
    private static final double WATER_MOVEMENT_LIMITER = 0.03999999910593033;

    /**
     * Chance to slow down fishing while the sky is not visible
     */
    private static final double NO_CLEAR_SKY_CHANCE = 0.5;

    /**
     * Chance to get rare drops while fishing. Higher value leads to a lower chance.
     */
    private static final double INCREASE_RARENESS_MODIFIER = 7.5;

    /**
     * The citizen who threw this rod
     */
    private EntityCitizen citizen;

    /**
     * The fishing speed enchantment level on the rod that threw this hook
     */
    private int fishingSpeedEnchantment;

    /**
     * The fishing loot enchantment level on the rod that threw this hook
     */
    private int fishingLootEnchantment;

    /**
     * If this hook is in the ground
     */
    private boolean inGround;

    /**
     * A counter for at what position in the shaking movement the hook is
     */
    private int    shake;
    private int    countdownNoFish;
    private int    countdownFishNear;
    private int    countdownFishBites;
    private double relativeRotation;

    /**
     * entity creation time.
     * Used to check it the hook got stuck.
     */
    private long creationTime;

    /**
     * When a fish is on the hook, this will be true
     */
    private boolean isFishCaugth = false;

    /**
     * Constructor for throwing out a hook.
     *
     * @param world   the world the hook lives in
     * @param citizen the citizen throwing the hook
     */
    public EntityFishHook(World world, @NotNull EntityCitizen citizen)
    {
        this(world);
        this.citizen = citizen;
        this.setLocationAndAngles(citizen.posX,
          citizen.posY + 1.62 - citizen.getYOffset(),
          citizen.posZ,
          citizen.rotationYaw,
          citizen.rotationPitch);
        this.posX -= Math.cos(this.rotationYaw / HALF_CIRCLE * Math.PI) * INITIAL_MOVEMENT_LIMITER;
        this.posY -= SUNKEN_OFFSET;
        this.posZ -= Math.sin(this.rotationYaw / HALF_CIRCLE * Math.PI) * INITIAL_MOVEMENT_LIMITER;
        this.setPosition(this.posX, this.posY, this.posZ);
        double f = 0.4;
        this.motionX = -Math.sin(this.rotationYaw / HALF_CIRCLE * Math.PI) * Math.cos(this.rotationPitch / HALF_CIRCLE * Math.PI) * f;
        this.motionZ = Math.cos(this.rotationYaw / HALF_CIRCLE * Math.PI) * Math.cos(this.rotationPitch / HALF_CIRCLE * Math.PI) * f;
        this.motionY = -Math.sin(this.rotationPitch / HALF_CIRCLE * Math.PI) * f;
        this.setPosition(this.motionX, this.motionY, this.motionZ, 1.5, 1.0);
        fishingSpeedEnchantment = EnchantmentHelper.getLureModifier(citizen);
        fishingLootEnchantment = EnchantmentHelper.getLuckOfSeaModifier(citizen);
    }

    /**
     * Lowest denominator constructor
     * Used by other constructors to do general stuff
     *
     * @param world the world this entity lives in
     */
    public EntityFishHook(World world)
    {
        super(world);
        this.setSize(ENTITY_SIZE, ENTITY_SIZE);
        this.ignoreFrustumCheck = true;
        this.creationTime = System.nanoTime();
        fishingLootEnchantment = 0;
        fishingSpeedEnchantment = 0;
    }

    private void setPosition(double x, double y, double z, double yaw, double pitch)
    {
        double squareRootXYZ = MathHelper.sqrt_double(x * x + y * y + z * z);
        double newX = x / squareRootXYZ;
        double newY = y / squareRootXYZ;
        double newZ = z / squareRootXYZ;
        newX += this.rand.nextGaussian() * RANDOM_MOVEMENT_OFFSET * pitch;
        newY += this.rand.nextGaussian() * RANDOM_MOVEMENT_OFFSET * pitch;
        newZ += this.rand.nextGaussian() * RANDOM_MOVEMENT_OFFSET * pitch;
        newX *= yaw;
        newY *= yaw;
        newZ *= yaw;
        this.motionX = newX;
        this.motionY = newY;
        this.motionZ = newZ;
        this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(newX, newZ) * HALF_CIRCLE / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(newY, Math.sqrt(newX * newX + newZ * newZ)) * HALF_CIRCLE / Math.PI);
    }

    /**
     * Returns the citizen throwing the hook.
     *
     * @return a citizen
     */
    public EntityCitizen getCitizen()
    {
        return citizen;
    }

    /**
     * Minecraft may call this method
     */
    @Override
    protected void entityInit()
    {
        /**
         * No need to use this method.
         * It will be ignored.
         */
    }

    @Override
    public boolean equals(@Nullable Object o)
    {
        return !(o == null || getClass() != o.getClass() || !super.equals(o));
    }

    @Override
    public int hashCode()
    {
        return getEntityId();
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (fishHookIsOverTimeToLive())
        {
            this.setDead();
        }
        if (bounceFromGround() || this.inGround)
        {
            return;
        }

        moveSomeStuff();
    }

    /**
     * Checks if the entity is in range to render by using the past in distance and comparing it to its average edge
     * length * 64 * renderDistanceWeight Args: distance
     *
     * @param range the real range
     * @return true or false
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double range)
    {
        double maxLength = this.getEntityBoundingBox().getAverageEdgeLength() * NUM_BOUNDING_BOX_EDGES;
        maxLength *= DISTANCE_FACTOR;
        return range < maxLength * maxLength;
    }

    /**
     * If a hook gets loaded, kill it immediately.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound unused)
    {
        this.setDead();
    }

    /**
     * No need to write anything to NBT.
     * A hook does not need to be saved.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound unused)
    {
        //We don't save this
    }

    /**
     * Sets the velocity to the args.
     *
     * @param vectorX directionX
     * @param vectorY directionY
     * @param vectorZ directionZ
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(double vectorX, double vectorY, double vectorZ)
    {
        this.motionX = vectorX;
        this.motionY = vectorY;
        this.motionZ = vectorZ;
    }

    /**
     * Check if a fishhook is there for too long
     * and became bugged.
     * After 360 seconds remove the hook.
     *
     * @return true if the time to live is over
     */
    public boolean fishHookIsOverTimeToLive()
    {
        return MathUtils.nanoSecondsToSeconds(System.nanoTime() - creationTime) > TTL;
    }

    /**
     * Update some movement things for the hook.
     * Detect if the hook is on ground and maybe bounce.
     * Also count how long the hook is laying on the ground or in water.
     *
     * @return true if the hook is killed.
     */
    private boolean bounceFromGround()
    {
        if (this.shake > 0)
        {
            --this.shake;
        }

        if (!this.inGround)
        {
            return false;
        }

        this.inGround = false;
        this.motionX *= (this.rand.nextDouble() * BOUNCE_MOVEMENT_LIMITER);
        this.motionY *= (this.rand.nextDouble() * BOUNCE_MOVEMENT_LIMITER);
        this.motionZ *= (this.rand.nextDouble() * BOUNCE_MOVEMENT_LIMITER);

        return false;
    }

    /**
     * Main update method thingie
     * hopefully I'm able to reduce it somewhat...
     */
    private void moveSomeStuff()
    {
        updateMotionAndRotation();
        double movementLimiter = AIR_MOVEMENT_LIMITER;

        if (this.onGround || this.isCollidedHorizontally)
        {
            movementLimiter = GROUND_MOVEMENT_LIMITER;
        }

        byte numSteps = 5;
        double waterDensity = 0.0;

        //Check how much water is around the hook
        for (int j = 0; j < numSteps; ++j)
        {
            double d3 = this.getEntityBoundingBox().minY + (this.getEntityBoundingBox().maxY - this.getEntityBoundingBox().minY) * j / numSteps;
            double d4 = this.getEntityBoundingBox().minY + (this.getEntityBoundingBox().maxY - this.getEntityBoundingBox().minY) * (j + 1) / numSteps;

            @NotNull AxisAlignedBB axisAlignedBB1 = new AxisAlignedBB(
                                                                       this.getEntityBoundingBox().minX,
                                                                       d3,
                                                                       this.getEntityBoundingBox().minZ,
                                                                       this.getEntityBoundingBox().maxX,
                                                                       d4,
                                                                       this.getEntityBoundingBox().maxZ);

            //If the hook is swimming
            if (this.worldObj.isAABBInMaterial(axisAlignedBB1, Material.WATER))
            {
                waterDensity += 1.0 / numSteps;
            }
        }

        checkIfFishBites(waterDensity);

        double currentDistance = waterDensity * 2.0D - 1.0;
        this.motionY += WATER_MOVEMENT_LIMITER * currentDistance;

        if (waterDensity > 0.0)
        {
            movementLimiter *= 0.9;
            this.motionY *= 0.8;
        }

        this.motionX *= movementLimiter;
        this.motionY *= movementLimiter;
        this.motionZ *= movementLimiter;
        this.setPosition(this.posX, this.posY, this.posZ);
    }

    /**
     * Update the fishing hooks motion
     * and its rotation.
     */
    private void updateMotionAndRotation()
    {
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        double motion = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float) (Math.atan2(this.motionY, this.motionZ) * HALF_CIRCLE / Math.PI);
        this.rotationPitch = (float) (Math.atan2(this.motionY, motion) * HALF_CIRCLE / Math.PI);
        while ((double) this.rotationPitch - (double) this.prevRotationPitch < -HALF_CIRCLE)
        {
            this.prevRotationPitch -= 360.0;
        }

        while ((double) this.rotationPitch - (double) this.prevRotationPitch >= HALF_CIRCLE)
        {
            this.prevRotationPitch += 360.0;
        }

        while ((double) this.rotationYaw - (double) this.prevRotationYaw < -HALF_CIRCLE)
        {
            this.prevRotationYaw -= 360.0;
        }

        while ((double) this.rotationYaw - (double) this.prevRotationYaw >= HALF_CIRCLE)
        {
            this.prevRotationYaw += 360.0;
        }

        this.rotationPitch = (float) ((double) this.prevRotationPitch + ((double) this.rotationPitch - (double) this.prevRotationPitch) * 0.2D);
        this.rotationYaw = (float) ((double) this.prevRotationYaw + ((double) this.rotationYaw - (double) this.prevRotationYaw) * 0.2D);
    }

    /**
     * Server side method to do
     * some animation and movement stuff
     * when the hook swims in water
     * <p>
     * will set isFishCaught if a fish bites
     *
     * @param waterDensity the amount of water around
     */
    private void checkIfFishBites(double waterDensity)
    {
        if (!this.worldObj.isRemote && waterDensity > 0.0)
        {
            int fishingProgressStep = 1;

            if (this.rand.nextDouble() < NO_CLEAR_SKY_CHANCE
                  && !this.worldObj.canBlockSeeSky(new BlockPos(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY) + 1, MathHelper.floor_double(this.posZ))))
            {
                --fishingProgressStep;
            }

            if (this.countdownNoFish > 0)
            {
                updateNoFishCounter();
                return;
            }

            @NotNull WorldServer worldServer = (WorldServer) this.worldObj;

            if (this.countdownFishNear > 0)
            {
                renderBubble(fishingProgressStep, worldServer);
                return;
            }

            if (this.countdownFishBites > 0)
            {
                this.countdownFishBites -= fishingProgressStep;

                renderFishBiteOrSwim(worldServer);
            }
            else
            {
                this.countdownFishNear = MathHelper.getRandomIntegerInRange(this.rand, 100, 900);
                this.countdownFishNear -= fishingSpeedEnchantment * 20 * 5;
            }
        }
    }

    private void updateNoFishCounter()
    {
        --this.countdownNoFish;

        if (this.countdownNoFish <= 0)
        {
            this.countdownFishNear = 0;
            this.countdownFishBites = 0;
        }
        else
        {
            this.motionY -= (this.rand.nextDouble() * this.rand.nextDouble() * this.rand.nextDouble()) * BOUNCE_MOVEMENT_LIMITER;
        }
    }

    private void renderBubble(int fishingProgressStep, @NotNull WorldServer worldServer)
    {
        this.countdownFishNear -= fishingProgressStep;

        double bubbleY = 0.15;

        if (this.countdownFishNear < 20)
        {
            bubbleY = bubbleY + (double) (20 - this.countdownFishNear) * 0.05;
        }
        else if (this.countdownFishNear < 40)
        {
            bubbleY = bubbleY + (double) (40 - this.countdownFishNear) * 0.02;
        }
        else if (this.countdownFishNear < 60)
        {
            bubbleY = bubbleY + (double) (60 - this.countdownFishNear) * 0.01;
        }

        if (this.rand.nextDouble() < bubbleY)
        {
            renderLittleSplash(worldServer);
        }

        if (this.countdownFishNear <= 0)
        {
            this.relativeRotation = MathHelper.randomFloatClamp(this.rand, 0.0F, 360.0F);
            this.countdownFishBites = MathHelper.getRandomIntegerInRange(this.rand, 20, 80);
        }
    }

    private void renderFishBiteOrSwim(@NotNull WorldServer worldServer)
    {
        if (this.countdownFishBites <= 0)
        {
            //Show fish bite animation
            showFishBiteAnimation(worldServer);
        }
        else
        {
            //Show fish swim animation
            showFishSwimmingTowardsHookAnimation(worldServer);
        }
    }

    /**
     * Render little splashes around the fishing hook
     * simulating fish movement
     *
     * @param worldServer the server side world
     */
    private void renderLittleSplash(@NotNull WorldServer worldServer)
    {
        double sinYPosition = (double) MathHelper.randomFloatClamp(this.rand, 0.0F, 360.0F) * 0.017453292D;
        double cosYPosition = MathHelper.randomFloatClamp(this.rand, 25.0F, 60.0F);
        double bubbleX = this.posX + (Math.sin(sinYPosition) * cosYPosition * 0.1);
        double increasedYPosition = Math.floor(this.getEntityBoundingBox().minY) + 1.0;
        double bubbleZ = this.posZ + (Math.cos(sinYPosition) * cosYPosition * 0.1);
        worldServer.spawnParticle(WATER_SPLASH,
          bubbleX,
          increasedYPosition,
          bubbleZ,
          2 + this.rand.nextInt(2),
          SUNKEN_OFFSET,
          0.0,
          SUNKEN_OFFSET,
          0.0);
    }

    /**
     * Show bubbles towards the hook.
     * Let the hook sink in a bit.
     * Play a sound to signal to the player,
     * that a fish bit
     *
     * @param worldServer the server side world
     */
    private void showFishBiteAnimation(@NotNull final WorldServer worldServer)
    {
        this.motionY -= 0.20000000298023224D;
        this.playSound(SoundEvents.ENTITY_BOBBER_SPLASH, ENTITY_SIZE, (float) (1.0D + this.rand.nextGaussian() * 0.4D));
        double bubbleY = Math.floor(this.getEntityBoundingBox().minY);
        worldServer.spawnParticle(WATER_BUBBLE,
          this.posX,
          bubbleY + 1.0,
          this.posZ,
          (int) (1.0 + this.width * 20.0),
          (double) this.width,
          0.0,
          (double) this.width,
          0.20000000298023224);
        worldServer.spawnParticle(WATER_WAKE,
          this.posX,
          bubbleY + 1.0,
          this.posZ,
          (int) (1.0 + this.width * 20.0),
          (double) this.width,
          0.0,
          (double) this.width,
          0.20000000298023224);
        this.countdownNoFish = MathHelper.getRandomIntegerInRange(this.rand, 10, 30);
        isFishCaugth = true;
    }

    /**
     * Show bubbles moving towards the hook
     * make it look like a fish will bite soon
     *
     * @param worldServer the server side world
     */
    private void showFishSwimmingTowardsHookAnimation(@NotNull WorldServer worldServer)
    {
        this.relativeRotation = this.relativeRotation + this.rand.nextGaussian() * NUM_BOUNDING_BOX_EDGES;
        double bubbleY = this.relativeRotation * 0.017453292;
        double sinYPosition = Math.sin(bubbleY);
        double cosYPosition = Math.cos(bubbleY);
        double bubbleX = this.posX + (sinYPosition * this.countdownFishBites * 0.1);
        double increasedYPosition = Math.floor(this.getEntityBoundingBox().minY) + 1.0;
        double bubbleZ = this.posZ + (cosYPosition * this.countdownFishBites * 0.1);

        if (this.rand.nextDouble() < 0.15)
        {
            worldServer.spawnParticle(WATER_BUBBLE, bubbleX, increasedYPosition - SUNKEN_OFFSET, bubbleZ, 1, sinYPosition, 0.1D, cosYPosition, 0.0);
        }

        double f3 = sinYPosition * 0.04;
        double f4 = cosYPosition * 0.04;
        worldServer.spawnParticle(WATER_WAKE, bubbleX, increasedYPosition, bubbleZ, 0, f4, 0.01, -f3, 1.0);
        worldServer.spawnParticle(WATER_WAKE, bubbleX, increasedYPosition, bubbleZ, 0, -f4, 0.01, f3, 1.0);
    }

    /**
     * Returns a damage value by how much the fishingRod should be damaged.
     * Also spawns loot and exp and destroys the hook.
     *
     * @param citizen the fisherman fishing
     * @return the number of damage points to be deducted.
     */
    public int getDamage(@NotNull final EntityCitizen citizen)
    {
        if (this.worldObj.isRemote)
        {
            this.setDead();
            return 0;
        }
        byte itemDamage = 0;
        if (isFishCaugth)
        {
            if (this.countdownNoFish > 0)
            {
                spawnLootAndExp(citizen);
                itemDamage = 1;
            }

            if (this.inGround)
            {
                itemDamage = 0;
            }
        }
        this.setDead();
        return itemDamage;
    }

    /**
     * Spawns a random loot from the loot table
     * and some exp orbs.
     * Should be called when retrieving a hook.
     * todo: Perhaps streamline this and directly add the items?
     *
     * @param citizen the fisherman getting the loot
     */
    private void spawnLootAndExp(@NotNull final EntityCitizen citizen)
    {
        double citizenPosX = citizen.posX;
        double citizenPosY = citizen.posY;
        double citizenPosZ = citizen.posZ;
        @NotNull EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, this.getFishingLoot(citizen));
        double distanceX = citizenPosX - this.posX;
        double distanceY = citizenPosY - this.posY;
        double distanceZ = citizenPosZ - this.posZ;

        entityitem.motionX = distanceX * 0.1;
        entityitem.motionY = distanceY * 0.1 + Math.sqrt(Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ)) * 0.08;
        entityitem.motionZ = distanceZ * 0.1;
        this.worldObj.spawnEntityInWorld(entityitem);
        citizen.worldObj.spawnEntityInWorld(new EntityXPOrb(citizen.worldObj,
                                                             citizenPosX,
                                                             citizenPosY + 0.D,
                                                             citizenPosZ + 0.5,
                                                             this.rand.nextInt(6) + 1));
    }

    /**
     * Determines which loot table should be used.
     * <p>
     * The selection is somewhat random and depends on enchantments
     * and the level of the fisherman hut.
     *
     * @param citizen the fisherman getting the loot
     * @return an ItemStack randomly from the loot table
     */
    private ItemStack getFishingLoot(final EntityCitizen citizen)
    {
        //Reduce random to get more fish drops
        double random = this.worldObj.rand.nextDouble() / INCREASE_RARENESS_MODIFIER;
        double speedBonus = 0.1 - fishingSpeedEnchantment * 0.025 - fishingLootEnchantment * 0.01;
        double lootBonus = 0.05 + fishingSpeedEnchantment * 0.01 - fishingLootEnchantment * 0.01;
        //clamp_float gives the values an upper limit
        speedBonus = MathHelper.clamp_float((float) speedBonus, 0.0F, 1.0F);
        lootBonus = MathHelper.clamp_float((float) lootBonus, 0.0F, 1.0F);
        int buildingLevel = citizen.getWorkBuilding().getBuildingLevel();

        if (random < speedBonus || buildingLevel == 1)
        {
            LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer) this.worldObj);
            for (ItemStack itemstack : this.worldObj.getLootTableManager()
                                         .getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING)
                                         .generateLootForPools(this.rand, lootcontext$builder.build()))
            {
                return itemstack;
            }
        }
        else
        {
            random -= speedBonus;

            if (random < lootBonus || buildingLevel == 2)
            {
                LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer) this.worldObj);
                for (ItemStack itemstack : this.worldObj.getLootTableManager()
                                             .getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING_JUNK)
                                             .generateLootForPools(this.rand, lootcontext$builder.build()))
                {
                    return itemstack;
                }
            }
            else
            {
                LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer) this.worldObj);
                for (ItemStack itemstack : this.worldObj.getLootTableManager()
                                             .getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING_TREASURE)
                                             .generateLootForPools(this.rand, lootcontext$builder.build()))
                {
                    return itemstack;
                }
            }
        }
        return null;
    }

    /**
     * returns true if a fish was caught.
     *
     * @return true | false
     */
    public boolean caughtFish()
    {
        return isFishCaugth;
    }
}
