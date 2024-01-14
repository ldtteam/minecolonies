package com.minecolonies.api.entity.visitor;

public abstract class AbstractVisitorExtraData<S> implements IVisitorExtraData<S>
{
    private String key;

    private S value;

    public AbstractVisitorExtraData()
    {

    }

    @Override
    public final S getValue()
    {
        return value;
    }

    protected final void setValue(final S value)
    {
        this.value = value;
    }
}
