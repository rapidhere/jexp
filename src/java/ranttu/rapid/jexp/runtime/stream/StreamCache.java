/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.stream;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.var;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * cache a stream
 *
 * @author rapid
 * @version : StreamCache.java, v 0.1 2020-11-10 8:36 PM rapid Exp $
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class StreamCache<T> {
    /**
     * create a cache
     */
    public static <S> StreamCache<S> of(Stream<S> stream) {
        return new StreamCache<>(stream.spliterator());
    }

    /**
     * delegated stream
     */
    private final Spliterator<T> delegated;

    /**
     * cached stream content
     */
    private final List<T> buff = new ArrayList<>();

    /**
     * whether source is consumed
     * TODO: thread safe
     */
    private boolean buffered = false;

    /**
     * create a stream
     */
    public Stream<T> stream() {
        return StreamSupport.stream(new CachedSpliterator(), false);
    }

    /**
     * NOTE: not thread safe
     */
    private class CachedSpliterator implements Spliterator<T> {
        /**
         * current index
         */
        private int index = 0;

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (!prepareBuff()) {
                return false;
            }

            action.accept(buff.get(index++));
            return true;
        }

        @Override
        public long estimateSize() {
            if (buffered) {
                return buff.size() - index;
            } else {
                var size = buff.size() - index + delegated.estimateSize();
                // overflowed
                return size < 0 ? Long.MAX_VALUE : size;
            }
        }

        @Override
        public int characteristics() {
            var characteristics = delegated.characteristics();

            // ORDERED because of cache
            characteristics |= ORDERED;

            // no CONCURRENT
            characteristics &= ~Spliterator.CONCURRENT;

            // when buffered, size and values are immutable
            if (buffered) {
                characteristics |= Spliterator.SIZED | Spliterator.IMMUTABLE;
            }

            return characteristics;
        }

        @Override
        public Spliterator<T> trySplit() {
            // not supported
            return null;
        }

        //~~~ impl
        private boolean prepareBuff() {
            while (!buffered && index >= buff.size()) {
                buffered = !delegated.tryAdvance(buff::add);
            }

            return index < buff.size();
        }
    }
}