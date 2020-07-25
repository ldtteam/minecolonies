package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.particles.SleepingParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Initializes the particle type.
 */
public class ModParticleTypesInitializer
{
    /**
     * Particle type
     */
    public static final BasicParticleType SLEEPINGPARTICLE_TYPE = new BasicParticleType(true);
    public static final ResourceLocation  SLEEPING_TEXTURE      = new ResourceLocation(Constants.MOD_ID, "particle/sleeping");

    /**
     * Register the particle
     */
    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class CommonRegistration
    {
        @SubscribeEvent
        public static void registerParticles(final RegistryEvent.Register<ParticleType<?>> event)
        {
            event.getRegistry().register(SLEEPINGPARTICLE_TYPE.setRegistryName(SLEEPING_TEXTURE));
        }
    }

    /**
     * Register the client side factory
     */
    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientRegistration
    {
        @SubscribeEvent
        public static void registerParticleFactories(ParticleFactoryRegisterEvent event)
        {
            Minecraft.getInstance().particles.registerFactory(SLEEPINGPARTICLE_TYPE, SleepingParticle.Factory::new);
        }
    }
}
