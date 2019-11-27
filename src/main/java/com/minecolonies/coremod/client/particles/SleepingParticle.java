package com.minecolonies.coremod.client.particles;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SleepingParticle extends Particle
{
    private final double coordX;
    private final double coordY;
    private final double coordZ;

    public static final ResourceLocation SLEEPING_TEXTURE = new ResourceLocation(Constants.MOD_ID, "entity/sleeping");

    public SleepingParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);

        this.setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(SLEEPING_TEXTURE.toString()));

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
        float f = this.rand.nextFloat() * 0.6F + 0.4F;
        this.particleRed = 0.9F * f;
        this.particleGreen = 0.9F * f;
        this.particleBlue = f;
        this.particleMaxAge = (int) (Math.random() * 30.0D) + 40;
        this.particleScale = (float) ((0.8 * Math.sin(0) + 1.3));
        // this.setParticleTextureIndex((int)(Math.random() * 26.0D + 1.0D + 224.0D));
    }

    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;


        // goes from 0.x to 1. Example: 0.4.
        float f = (float) this.particleAge / (float) this.particleMaxAge;

        particleScale = (float) ((0.8 * Math.sin(f * 4) + 1.3));

        // Used for Y motion -> curve? Example: 0.4
        float f1 = f;
        // Example: 0.16
        f1 *= f1;
        // Example: 0.0256
        f1 *= f1;

        // Horizontal motion Example: 0.6
        this.posX = this.coordX + this.motionX * f;
        // vertical motion Example:
        this.posY = this.coordY + this.motionY * f;
        // Horizontal motion Example: 0.6
        //this.posZ = this.coordZ + this.motionZ * (double)f;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setExpired();
        }
    }

    @Override
    public int getBrightnessForRender(float partialTick)
    {
        return 15 << 20 | 15 << 4;
    }

    public int getFXLayer()
    {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements IParticleFactory
    {
        public Factory()
        {
        }

        public Particle createParticle(
          int particleID,
          World worldIn,
          double xCoordIn,
          double yCoordIn,
          double zCoordIn,
          double xSpeedIn,
          double ySpeedIn,
          double zSpeedIn,
          int... p_178902_15_)
        {
            return (new SleepingParticle(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn));
        }
    }
}
