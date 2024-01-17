package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.particles.SleepingParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Initializes the particle type.
 */
public class ModParticleTypesInitializer
{
    /**
     * Particle type
     */
    public static final SimpleParticleType SLEEPINGPARTICLE_TYPE = new SimpleParticleType(true);
    public static final ResourceLocation  SLEEPING_TEXTURE      = new ResourceLocation(Constants.MOD_ID, "particle/sleeping");

    /**
     * Register the particle
     */
    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class CommonRegistration
    {
        @SubscribeEvent
        public static void registerParticles(final RegisterEvent event)
        {
            if (event.getRegistryKey().equals(ForgeRegistries.Keys.PARTICLE_TYPES))
            {
                event.getForgeRegistry().register(SLEEPING_TEXTURE, SLEEPINGPARTICLE_TYPE);
            }
        }
    }

    /**
     * Register the client side factory
     */
    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientRegistration
    {
        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event)
        {
            Minecraft.getInstance().particleEngine.register(SLEEPINGPARTICLE_TYPE, SleepingParticle.Factory::new);
        }
    }
}
