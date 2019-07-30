package com.minecolonies.coremod.util;

public class ModifyableLambdaWrapper<T>
{

    private T value;

    public ModifyableLambdaWrapper(final T value)
    {
        this.value = value;
    }

    public ModifyableLambdaWrapper()
    {
    }

    public T getValue()
    {
        return value;
    }

    public void setValue(final T value)
    {
        this.value = value;
    }
}
