package de.codehat.mobcapture.util;

@FunctionalInterface
public interface TriConsumer<V, T, U> {
    void accept(V v, T t, U u);
}
