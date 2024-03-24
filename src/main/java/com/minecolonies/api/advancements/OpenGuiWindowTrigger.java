package com.minecolonies.api.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger.SimpleInstance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

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
    public void trigger(final ServerPlayer player, final String windowResource)
    {
        trigger(player, trigger -> trigger.test(windowResource));
    }

    @Override
    public Codec<OpenGuiWindowTriggerInstance> codec()
    {
        return OpenGuiWindowTriggerInstance.CODEC;
    }

    public static record OpenGuiWindowTriggerInstance(Optional<ContextAwarePredicate> player, Optional<String> windowResource) implements SimpleInstance
    {
        public static final Codec<OpenGuiWindowTriggerInstance> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(OpenGuiWindowTriggerInstance::player),
                ExtraCodecs.strictOptionalField(Codec.STRING, "window_resource_location").forGetter(OpenGuiWindowTriggerInstance::windowResource))
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
        public static Criterion<OpenGuiWindowTriggerInstance> openGuiWindow(final String windowResource)
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
        public boolean test(final String windowResource)
        {
            if (this.windowResource.isPresent())
            {
                return this.windowResource.get().equalsIgnoreCase(windowResource);
            }
            return true;
        }
    }
}
