package org.uze.coherence;

import com.tangosol.net.GuardSupport;
import com.tangosol.net.Guardian;
import com.tangosol.net.cache.CacheMap;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.LiteMap;
import com.tangosol.util.processor.AbstractProcessor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 02.03.2019
 */
public final class BulkPreloadProcessor<K, V, R> extends AbstractProcessor<K, V, R> {

    private static final long serialVersionUID = -5863917087215621551L;

    private InvocableMap.EntryProcessor<K, V, R> delegate;

    private int batchSize;

    public BulkPreloadProcessor() {
    }

    public BulkPreloadProcessor(InvocableMap.EntryProcessor<K, V, R> delegate, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batch size should be greater than zero!");
        }
        this.delegate = Objects.requireNonNull(delegate);
        this.batchSize = batchSize;
    }

    @Override
    public String toString() {
        return "BulkPreloadProcessor{" +
                "delegate=" + delegate +
                ", batchSize=" + batchSize +
                '}';
    }

    @Override
    public R process(InvocableMap.Entry<K, V> entry) {
        return delegate.process(entry);
    }

    @Override
    public Map<K, R> processAll(Set<? extends InvocableMap.Entry<K, V>> entries) {
        final Map<K, R> mapResults = new LiteMap<>();
        final Guardian.GuardContext ctxGuard = GuardSupport.getThreadContext();
        final long cMillis = ctxGuard == null ? 0L : ctxGuard.getTimeoutMillis();
        final Runnable heartbeat = ctxGuard != null
                ? () -> ctxGuard.heartbeat(cMillis)
                : () -> {
        };
        consumeAllPresentEntries(entries, mapResults, heartbeat);
        final Iterator<? extends InvocableMap.Entry<K, V>> it = entries.iterator();
        final Set<InvocableMap.Entry<K, V>> batch = new HashSet<>(batchSize);
        while (it.hasNext()) {
            while (it.hasNext() && batch.size() < batchSize) {
                batch.add(it.next());
            }
            if (!batch.isEmpty()) {
                preload((Collection) batch);
                heartbeat.run();
                consumeAllPresentEntries(batch, mapResults, heartbeat);
                if (!batch.isEmpty()) {
                    err(batch.size() + " entries was left behind (unable to preload)!");
                    batch.clear();
                }
            }
        }
        entries.clear();
        getLog().println("Processed " + mapResults.size() + " entries");
        return mapResults;
    }

    private void consumeAllPresentEntries(Set<? extends InvocableMap.Entry<K, V>> entries, Map<K, R> results, Runnable heartbeat) {
        final Iterator it = entries.iterator();
        while (it.hasNext()) {
            final InvocableMap.Entry<K, V> entry = (InvocableMap.Entry) it.next();
            if (entry.isPresent()) {
                results.put(entry.getKey(), this.process(entry));
                it.remove();
            }
            heartbeat.run();
        }
    }

    private void preload(Collection<BinaryEntry> entries) {
        final Iterator<BinaryEntry> it = entries.iterator();
        if (it.hasNext()) {
            final BinaryEntry entry = it.next();
            ((CacheMap) entry.getBackingMapContext().getBackingMap()).getAll(
                    entries.stream()
                            .filter(b -> !b.isPresent())
                            .map(BinaryEntry::getBinaryKey)
                            .collect(Collectors.toList())
            );
        }
    }

}
