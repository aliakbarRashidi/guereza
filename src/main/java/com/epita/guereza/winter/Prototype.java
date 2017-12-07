package com.epita.guereza.winter;

import java.util.function.Function;

public class Prototype<BEAN_TYPE> implements Provider<BEAN_TYPE> {
    private final Function<Scope, BEAN_TYPE> initiator;

    public Prototype(Function<Scope, BEAN_TYPE> initiator) {
        this.initiator = initiator;
    }

    @Override
    public BEAN_TYPE getInstance(final Scope scope) {
        return initiator.apply(scope);
    }
}
