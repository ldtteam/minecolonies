/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package com.minecolonies.api.entity.ai.statemachine.tickratestatemachine;

import java.io.Serializable;

/**
 * Serializeable version of a supplier for AI states, used for name generation
 *
 * @param <T>
 */
@FunctionalInterface
public interface IStateSupplier<T> extends Serializable
{
    /**
     * Gets a result.
     *
     * @return a result
     */
    T get();
}
