package com.minecolonies.api.entity.ai;

/**
 * AI using our states
 */
public interface ITickingStateAI
{
    public void tick();
    public void onRemoval();
    public void resetAI();
}
