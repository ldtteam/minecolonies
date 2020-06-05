package com.minecolonies.coremod.client.particles;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.particle.*;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * Custom particle for sleeping.
 */
public class SleepingParticle extends SpriteTexturedParticle
{

    /**
     * Spawn coords
     */
    private final double coordX;
    private final double coordY;
    private final double coordZ;

    /**
     * The light level of the particle
     */
    private static final int LIGHT_LEVEL = 15 << 20 | 15 << 4;

    /**
     * The resourcelocation for the sleeping image.
     */
    public static final ResourceLocation SLEEPING_TEXTURE = new ResourceLocation(Constants.MOD_ID, "particle/sleeping");

    public SleepingParticle(IAnimatedSprite spriteSet, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);

        //this.setSprite(Minecraft.getInstance().getTextureMap().getSprite(SLEEPING_TEXTURE));
        selectSpriteWithAge(spriteSet);
        this.motionX = xSpeedIn * 0.5;
        this.motionY = ySpeedIn;
        this.motionZ = zSpeedIn;
        this.coordX = xCoordIn;
        this.coordY = yCoordIn;
        this.coordZ = zCoordIn;
        this.prevPosX = xCoordIn;
        this.prevPosY = yCoordIn;
        this.prevPosZ = zCoordIn;
        this.posX = this.prevPosX;
        this.posY = this.prevPosY;
        this.posZ = this.prevPosZ;
        // Slight color variance
        float f = this.rand.nextFloat() * 0.6F + 0.4F;
        this.particleRed = 0.9F * f;
        this.particleGreen = 0.9F * f;
        this.particleBlue = f;
        // particles max age in ticks, random causes them to appear a bit more dynamic, as they get faster/slower with shorter/longer lifetime
        this.maxAge = (int) (Math.random() * 30.0D) + 40;
        // starting scale to fit
        this.particleScale = (float) ((0.8 * Math.sin(0) + 1.3) * 0.1);
    }

    /**
     * Updates the particles, setting new position/scale
     */

    public void tick()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        float f = (float) this.age / (float) this.maxAge;

        // Scale smaller/bigger in a similar rate to snoring
        particleScale = (float) ((0.8 * Math.sin(f * 4) + 1.3) * 0.1);

        // Moves the particle in relation to movespeed and age
        this.posX = this.coordX + this.motionX * f;
        this.posY = this.coordY + this.motionY * f;
        // TODO: potential bug, missing posZ

        if (this.age++ >= this.maxAge)
        {
            this.setExpired();
        }
    }

    @Override
    public int getBrightnessForRender(float partialTick)
    {
        return LIGHT_LEVEL;
    }

    @Override
    public IParticleRenderType getRenderType()
    {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements IParticleFactory<BasicParticleType>
    {
        private IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet)
        {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle makeParticle(BasicParticleType particleType, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            return new SleepingParticle(spriteSet, world, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}
