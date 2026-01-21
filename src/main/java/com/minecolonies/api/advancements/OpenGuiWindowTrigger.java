package com.minecolonies.api.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Triggered when a blockui window is opened
 */
public class OpenGuiWindowTrigger extends SimpleCriterionTrigger<OpenGuiWindowTrigger.OpenGuiWindowTriggerInstance>
{
    /**
     * Triggers the listener checks if there are any listening in
     * 
     * @param player         the player the check regards
     * @param windowResource the window id that was just opened
     */
    public void trigger(final ServerPlayer player, final ResourceLocation windowResource)
    {
        trigger(player, trigger -> trigger.test(windowResource));
    }

    @Override
    @NotNull
    public Codec<OpenGuiWindowTriggerInstance> codec()
    {
        return OpenGuiWindowTriggerInstance.CODEC;
    }

    public record OpenGuiWindowTriggerInstance(Optional<ContextAwarePredicate> player, Optional<ResourceLocation> windowResource) implements SimpleInstance
    {
        public static final Codec<OpenGuiWindowTriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(OpenGuiWindowTriggerInstance::player),
                ResourceLocation.CODEC.optionalFieldOf("window_resource_location").forGetter(OpenGuiWindowTriggerInstance::windowResource))
            .apply(builder, OpenGuiWindowTriggerInstance::new));

        public static Criterion<OpenGuiWindowTriggerInstance> openGuiWindow()
        {
            return openGuiWindow(null);
        }

        /**
         * Construct the check with a single condition
         * 
         * @param windowResource the window that has to be opened to succeed
         */
        public static Criterion<OpenGuiWindowTriggerInstance> openGuiWindow(final ResourceLocation windowResource)
        {
            return AdvancementTriggers.OPEN_GUI_WINDOW.get()
                .createCriterion(new OpenGuiWindowTriggerInstance(Optional.empty(), Optional.ofNullable(windowResource)));
        }

        /**
         * Performs the check for the conditions
         * 
         * @param  windowResource the blockui window id that was just opened
         * @return                whether the check succeeded
         */
        public boolean test(final ResourceLocation windowResource)
        {
            return this.windowResource.map(resourceLocation -> resourceLocation.equals(windowResource)).orElse(true);
        }
    }
}
