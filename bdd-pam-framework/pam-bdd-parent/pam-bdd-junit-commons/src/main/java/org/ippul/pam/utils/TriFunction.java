package org.ippul.pam.utils;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface TriFunction<F,S,T,R> {

    R apply(F f, S s, T t);

    default <V> TriFunction<F, S, T, V> andThen(
            Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (F f, S s, T t) -> after.apply(apply(f, s, t));
    }
}