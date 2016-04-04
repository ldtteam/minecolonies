package com.minecolonies.entity;

import com.minecolonies.entity.ai.EntityAIWorkFisherman;
import com.minecolonies.util.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomFishable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Arrays;
import java.util.List;

/**
 * Creates a custom fishHook for the Fisherman to throw
 * This class manages the entity
 */
public final class EntityFishHook extends Entity
{
    private static final int  TTL             = 360;
    private static final List possibleDrops_1 = Arrays.asList((new WeightedRandomFishable(new ItemStack(Items.leather_boots), 10)).func_150709_a(0.9F),
                                                              new WeightedRandomFishable(new ItemStack(Items.leather), 10),
                                                              new WeightedRandomFishable(new ItemStack(Items.bone), 10),
                                                              new WeightedRandomFishable(new ItemStack(Items.potionitem), 10),
                                                              new WeightedRandomFishable(new ItemStack(Items.string), 5),
                                                              (new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 2)).func_150709_a(0.9F),
                                                              new WeightedRandomFishable(new ItemStack(Items.bowl), 10),
                                                              new WeightedRandomFishable(new ItemStack(Items.stick), 5),
                                                              new WeightedRandomFishable(new ItemStack(Items.dye, 10, 0), 1),
                                                              new WeightedRandomFishable(new ItemStack(Blocks.tripwire_hook), 10),
                                                              new WeightedRandomFishable(new ItemStack(Items.rotten_flesh), 10));
    private static final List possibleDrops_2 = Arrays.asList(new WeightedRandomFishable(new ItemStack(Blocks.waterlily), 1),
                                                              new WeightedRandomFishable(new ItemStack(Items.name_tag), 1),
                                                              new WeightedRandomFishable(new ItemStack(Items.saddle), 1),
                                                              (new WeightedRandomFishable(new ItemStack(Items.bow), 1)).func_150709_a(0.25F).func_150707_a(),
                                                              (new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 1)).func_150709_a(0.25F).func_150707_a(),
                                                              (new WeightedRandomFishable(new ItemStack(Items.book), 1)).func_150707_a());
    private static final List possibleDrops_3 = Arrays.asList(new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.COD.func_150976_a()), 60),
                                                              new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.SALMON.func_150976_a()), 25),
                                                              new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.CLOWNFISH.func_150976_a()), 2),
                                                              new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.PUFFERFISH.func_150976_a()), 13));
    public  EntityCitizen citizen;
    private int           fishingSpeedEnchantment;
    private int           fishingLootEnchantment;
    private boolean       inGround;
    private int           shake;
    private int           movedOnX;
    private int           movedOnY;
    private int           movedOnZ;
    private double        relativeRotation;
    //Time at which the entity has been created
    private long          creationTime;
    //Will be set true when the citizen caught a fish (to reset the fisherman)
    private boolean isFishCaugth = false;

    /**
     * Constructor for throwing out a hook.
     *
     * @param world     the world the hook lives in
     * @param fisherman the fisherman throwing the hook
     */
    public EntityFishHook(World world, EntityAIWorkFisherman fisherman)
    {
        this(world);
        this.citizen = fisherman.getCitizen();
        this.setLocationAndAngles(fisherman.getCitizen().posX,
                                  fisherman.getCitizen().posY + 1.62 - (double) fisherman.getCitizen().yOffset,
                                  fisherman.getCitizen().posZ,
                                  fisherman.getCitizen().rotationYaw,
                                  fisherman.getCitizen().rotationPitch);
        this.posX -= Math.cos(this.rotationYaw / 180.0 * Math.PI) * 0.16;
        this.posY -= 0.10000000149011612;
        this.posZ -= Math.sin(this.rotationYaw / 180.0 * Math.PI) * 0.16;
        this.setPosition(this.posX, this.posY, this.posZ);
        this.yOffset = 0.0F;
        double f = 0.4;
        this.motionX = -Math.sin(this.rotationYaw / 180.0 * Math.PI) * Math.cos(this.rotationPitch / 180.0 * Math.PI) * f;
        this.motionZ = Math.cos(this.rotationYaw / 180.0 * Math.PI) * Math.cos(this.rotationPitch / 180.0 * Math.PI) * f;
        this.motionY = -Math.sin(this.rotationPitch / 180.0 * Math.PI) * f;
        this.setPosition(this.motionX, this.motionY, this.motionZ, 1.5, 1.0);
        fishingSpeedEnchantment = EnchantmentHelper.func_151386_g(fisherman.getCitizen());
        fishingLootEnchantment = EnchantmentHelper.func_151387_h(fisherman.getCitizen());
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
        this.setSize(0.25F, 0.25F);
        this.ignoreFrustumCheck = true;
        this.creationTime = System.nanoTime();
        fishingLootEnchantment = 0;
        fishingSpeedEnchantment = 0;
    }

    private void setPosition(double x, double y, double z, double yaw, double pitch)
    {
        double squareRootXYZ = MathHelper.sqrt_double(x * x + y * y + z * z);
        double newX          = x / squareRootXYZ;
        double newY          = y / squareRootXYZ;
        double newZ          = z / squareRootXYZ;
        newX += this.rand.nextGaussian() * 0.007499999832361937 * pitch;
        newY += this.rand.nextGaussian() * 0.007499999832361937 * pitch;
        newZ += this.rand.nextGaussian() * 0.007499999832361937 * pitch;
        newX *= yaw;
        newY *= yaw;
        newZ *= yaw;
        this.motionX = newX;
        this.motionY = newY;
        this.motionZ = newZ;
        this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(newX, newZ) * 180.0 / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(newY, Math.sqrt(newX * newX + newZ * newZ)) * 180.0 / Math.PI);
    }

    /**
     * Minecraft may call this method
     */
    @Override
    protected void entityInit(){}

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
        double maxLength = this.boundingBox.getAverageEdgeLength() * 4.0;
        maxLength *= 64.0;
        return range < maxLength * maxLength;
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
        this.motionX *= (this.rand.nextDouble() * 0.2);
        this.motionY *= (this.rand.nextDouble() * 0.2);
        this.motionZ *= (this.rand.nextDouble() * 0.2);

        return false;
    }

    /**
     * Main update method thingie
     * hopefully I'm able to reduce it somewhat...
     */
    private void moveSomeStuff()
    {
        updateMotionAndRotation();
        double f6 = 0.92F;

        if (this.onGround || this.isCollidedHorizontally)
        {
            f6 = 0.5;
        }

        byte   b0  = 5;
        double d10 = 0.0;

        //Check how much water is around the hook
        for (int j = 0; j < b0; ++j)
        {
            double        d3             = this.boundingBox.minY + (this.boundingBox.maxY - this.boundingBox.minY) * j / b0 - 0.125 + 0.125;
            double        d4             = this.boundingBox.minY + (this.boundingBox.maxY - this.boundingBox.minY) * (j + 1) / b0 - 0.125 + 0.125;
            AxisAlignedBB axisAlignedBB1 = AxisAlignedBB.getBoundingBox(this.boundingBox.minX, d3, this.boundingBox.minZ, this.boundingBox.maxX, d4, this.boundingBox.maxZ);

            //If the hook is swimming
            if (this.worldObj.isAABBInMaterial(axisAlignedBB1, Material.water))
            {
                d10 += 1.0 / b0;
            }
        }

        checkIfFishBites(d10);

        double currentDistance = d10 * 2.0D - 1.0;
        this.motionY += 0.03999999910593033 * currentDistance;

        if (d10 > 0.0)
        {
            f6 = f6 * 0.9;
            this.motionY *= 0.8;
        }

        this.motionX *= f6;
        this.motionY *= f6;
        this.motionZ *= f6;
        this.setPosition(this.posX, this.posY, this.posZ);
    }

    /**
     * Server side method to do
     * some animation and movement stuff
     * when the hook swims in water
     * <p>
     * will set isFishCaught if a fish bites
     *
     * @param d10 the amount of water around
     */
    private void checkIfFishBites(double d10)
    {
        if (!this.worldObj.isRemote && d10 > 0.0)
        {

            WorldServer worldServer = (WorldServer) this.worldObj;
            int         k           = 1;

            if (this.rand.nextDouble() < 0.25
                && this.worldObj.canLightningStrikeAt(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY) + 1, MathHelper.floor_double(this.posZ)))
            {
                k = 2;
            }

            if (this.rand.nextDouble() < 0.5
                && !this.worldObj.canBlockSeeTheSky(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY) + 1, MathHelper.floor_double(this.posZ)))
            {
                --k;
            }

            if (this.movedOnX > 0)
            {
                --this.movedOnX;

                if (this.movedOnX <= 0)
                {
                    this.movedOnY = 0;
                    this.movedOnZ = 0;
                }
            }
            else
            {
                double bubbleY;
                double bubbleZ;
                double bubbleX;

                double cosYPosition;
                double increasedYPosition;
                double sinYPosition;

                if (this.movedOnZ > 0)
                {
                    this.movedOnZ -= k;

                    if (this.movedOnZ <= 0)
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
                else if (this.movedOnY > 0)
                {
                    this.movedOnY -= k;
                    bubbleY = 0.15;

                    if (this.movedOnY < 20)
                    {
                        bubbleY = bubbleY + (double) (20 - this.movedOnY) * 0.05;
                    }
                    else if (this.movedOnY < 40)
                    {
                        bubbleY = bubbleY + (double) (40 - this.movedOnY) * 0.02;
                    }
                    else if (this.movedOnY < 60)
                    {
                        bubbleY = bubbleY + (double) (60 - this.movedOnY) * 0.01;
                    }

                    if (this.rand.nextDouble() < bubbleY)
                    {
                        renderLittleSplash(worldServer);

                    }

                    if (this.movedOnY <= 0)
                    {
                        this.relativeRotation = MathHelper.randomFloatClamp(this.rand, 0.0F, 360.0F);
                        this.movedOnZ = MathHelper.getRandomIntegerInRange(this.rand, 20, 80);
                    }
                }
                else
                {
                    this.movedOnY = MathHelper.getRandomIntegerInRange(this.rand, 100, 900);
                    this.movedOnY -= fishingSpeedEnchantment * 20 * 5;
                }
            }

            if (this.movedOnX > 0)
            {
                this.motionY -= (this.rand.nextDouble() * this.rand.nextDouble() * this.rand.nextDouble()) * 0.2;
            }
        }
    }

    /**
     * Render little splashes around the fishing hook
     * simulating fish movement
     *
     * @param worldServer the server side world
     */
    private void renderLittleSplash(WorldServer worldServer)
    {
        double sinYPosition       = (double) MathHelper.randomFloatClamp(this.rand, 0.0F, 360.0F) * 0.017453292D;
        double cosYPosition       = MathHelper.randomFloatClamp(this.rand, 25.0F, 60.0F);
        double bubbleX            = this.posX + (Math.sin(sinYPosition) * cosYPosition * 0.1);
        double increasedYPosition = Math.floor(this.boundingBox.minY) + 1.0;
        double bubbleZ            = this.posZ + (Math.cos(sinYPosition) * cosYPosition * 0.1);
        worldServer.func_147487_a("splash",
                                  bubbleX,
                                  increasedYPosition,
                                  bubbleZ,
                                  2 + this.rand.nextInt(2),
                                  0.10000000149011612,
                                  0.0,
                                  0.10000000149011612,
                                  0.0);
    }

    /**
     * Show bubbles moving towards the hook
     * make it look like a fish will bite soon
     *
     * @param worldServer the server side world
     */
    private void showFishSwimmingTowardsHookAnimation(WorldServer worldServer)
    {
        this.relativeRotation = this.relativeRotation + this.rand.nextGaussian() * 4.0;
        double bubbleY            = this.relativeRotation * 0.017453292;
        double sinYPosition       = Math.sin(bubbleY);
        double cosYPosition       = Math.cos(bubbleY);
        double bubbleX            = this.posX + (sinYPosition * this.movedOnZ * 0.1);
        double increasedYPosition = Math.floor(this.boundingBox.minY) + 1.0;
        double bubbleZ            = this.posZ + (cosYPosition * this.movedOnZ * 0.1);

        if (this.rand.nextDouble() < 0.15)
        {
            worldServer.func_147487_a("bubble", bubbleX, increasedYPosition - 0.10000000149011612, bubbleZ, 1, sinYPosition, 0.1D, cosYPosition, 0.0);
        }

        double f3 = sinYPosition * 0.04;
        double f4 = cosYPosition * 0.04;
        worldServer.func_147487_a("wake", bubbleX, increasedYPosition, bubbleZ, 0, f4, 0.01, (-f3), 1.0);
        worldServer.func_147487_a("wake", bubbleX, increasedYPosition, bubbleZ, 0, (-f4), 0.01, f3, 1.0);
    }

    /**
     * Show bubbles towards the hook.
     * Let the hook sink in a bit.
     * Play a sound to signal to the player,
     * that a fish bit
     *
     * @param worldServer the server side world
     */
    private void showFishBiteAnimation(final WorldServer worldServer)
    {
        this.motionY -= 0.20000000298023224D;
        this.playSound("random.splash", 0.25F, (float) (1.0D + (this.rand.nextDouble() - this.rand.nextDouble()) * 0.4D));
        double bubbleY = Math.floor(this.boundingBox.minY);
        worldServer.func_147487_a("bubble",
                                  this.posX,
                                  (bubbleY + 1.0),
                                  this.posZ,
                                  (int) (1.0 + this.width * 20.0),
                                  (double) this.width,
                                  0.0,
                                  (double) this.width,
                                  0.20000000298023224);
        worldServer.func_147487_a("wake",
                                  this.posX,
                                  (bubbleY + 1.0),
                                  this.posZ,
                                  (int) (1.0 + this.width * 20.0),
                                  (double) this.width,
                                  0.0,
                                  (double) this.width,
                                  0.20000000298023224);
        this.movedOnX = MathHelper.getRandomIntegerInRange(this.rand, 10, 30);
        isFishCaugth = true;
    }

    /**
     * Update the fishing hooks motion
     * and its rotation.
     */
    private void updateMotionAndRotation()
    {
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        double motion = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float) (Math.atan2(this.motionY, this.motionZ) * 180.0 / Math.PI);
        this.rotationPitch = (float) (Math.atan2(this.motionY, motion) * 180.0 / Math.PI);
        while ((double) this.rotationPitch - (double) this.prevRotationPitch < -180.0)
        {
            this.prevRotationPitch -= 360.0;
        }

        while ((double) this.rotationPitch - (double) this.prevRotationPitch >= 180.0)
        {
            this.prevRotationPitch += 360.0;
        }

        while ((double) this.rotationYaw - (double) this.prevRotationYaw < -180.0)
        {
            this.prevRotationYaw -= 360.0;
        }

        while ((double) this.rotationYaw - (double) this.prevRotationYaw >= 180.0)
        {
            this.prevRotationYaw += 360.0;
        }

        this.rotationPitch = (float) ((double) this.prevRotationPitch + ((double) this.rotationPitch - (double) this.prevRotationPitch) * 0.2D);
        this.rotationYaw = (float) ((double) this.prevRotationYaw + ((double) this.rotationYaw - (double) this.prevRotationYaw) * 0.2D);
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
        return Utils.nanoSecondsToSeconds(System.nanoTime() - creationTime) > TTL;
    }

    /**
     * No need to write anything to NBT.
     * A hook does not need to be saved.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound unused)
    {
    }

    /**
     * If a hook gets loaded, kill it immediately.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound unused)
    {
        this.setDead();
    }

    @Override
    public boolean equals(Object o)
    {
        return !(o == null || getClass() != o.getClass() || !super.equals(o));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getShadowSize()
    {
        return 0.0F;
    }

    /**
     * Returns a damage value by how much the fishingRod should be damaged.
     * Also spawns loot and exp and destroys the hook.
     *
     * @param entityAIWorkFisherman the fisherman fishing
     * @return the number of damage points to be deducted.
     */
    public int getDamage(final EntityAIWorkFisherman entityAIWorkFisherman)
    {
        if (this.worldObj.isRemote)
        {
            this.setDead();
            return 0;
        }
        byte itemDamage = 0;
        if (isFishCaugth)
        {
            if (this.movedOnX > 0)
            {
                spawnLootAndExp(entityAIWorkFisherman);
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
     * Spawns a random loot from the loottable
     * and some exp orbs.
     * Should be calles when retrieving a hook.
     * todo: Perhaps streamline this and directly add the items?
     *
     * @param entityAIWorkFisherman the fisherman getting the loot
     */
    private void spawnLootAndExp(final EntityAIWorkFisherman entityAIWorkFisherman)
    {
        double     citizenPosX = entityAIWorkFisherman.getCitizen().posX;
        double     citizenPosY = entityAIWorkFisherman.getCitizen().posY;
        double     citizenPosZ = entityAIWorkFisherman.getCitizen().posZ;
        EntityItem entityitem  = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, this.getFishingLoot(entityAIWorkFisherman));
        double     distanceX   = citizenPosX - this.posX;
        double     distanceY   = citizenPosY - this.posY;
        double     distanceZ   = citizenPosZ - this.posZ;

        entityitem.motionX = distanceX * 0.1;
        entityitem.motionY = distanceY * 0.1 + Math.sqrt(Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ)) * 0.08;
        entityitem.motionZ = distanceZ * 0.1;
        this.worldObj.spawnEntityInWorld(entityitem);
        entityAIWorkFisherman.getCitizen().worldObj.spawnEntityInWorld(new EntityXPOrb(entityAIWorkFisherman.getCitizen().worldObj,
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
     * @param entityAIWorkFisherman the fisherman getting the loot
     * @return an ItemStack randomly from the loot table
     */
    private ItemStack getFishingLoot(final EntityAIWorkFisherman entityAIWorkFisherman)
    {
        double random     = this.worldObj.rand.nextDouble();
        double speedBonus = 0.1 - fishingSpeedEnchantment * 0.025 - fishingLootEnchantment * 0.01;
        double lootBonus  = 0.05 + fishingSpeedEnchantment * 0.01 - fishingLootEnchantment * 0.01;
        //clamp_float gives the values an upper limit
        speedBonus = MathHelper.clamp_float((float) speedBonus, 0.0F, 1.0F);
        lootBonus = MathHelper.clamp_float((float) lootBonus, 0.0F, 1.0F);
        int buildingLevel = entityAIWorkFisherman.getCitizen().getWorkBuilding().getBuildingLevel();

        if (random < speedBonus || buildingLevel == 1)
        {
            return ((WeightedRandomFishable) WeightedRandom.getRandomItem(this.rand, possibleDrops_3)).func_150708_a(this.rand);
        }
        else
        {
            random -= speedBonus;

            if (random < lootBonus || buildingLevel == 2)
            {
                return ((WeightedRandomFishable) WeightedRandom.getRandomItem(this.rand, possibleDrops_1)).func_150708_a(this.rand);
            }
            else
            {
                return ((WeightedRandomFishable) WeightedRandom.getRandomItem(this.rand, possibleDrops_2)).func_150708_a(this.rand);
            }
        }
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
