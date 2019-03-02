package org.uze.coherence;

import com.tangosol.net.GuardSupport;
import com.tangosol.net.Guardian;
import com.tangosol.net.cache.CacheMap;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.LiteMap;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.processor.ExtractorProcessor;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 02.03.2019
 */
public class BulkPreloadExtractorProcessor<K, V, T, E> extends ExtractorProcessor<K, V, T, E> {

    public BulkPreloadExtractorProcessor() {
    }

    public BulkPreloadExtractorProcessor(ValueExtractor<? super T, ? extends E> extractor) {
        super(extractor);
    }

    public BulkPreloadExtractorProcessor(String sMethod) {
        super(sMethod);
    }

    @Override
    public String toString() {
        return "BulkPreloadExtractorProcessor{" +
                "m_extractor=" + m_extractor +
                '}';
    }

    @Override
    public Map<K, E> processAll(Set<? extends InvocableMap.Entry<K, V>> entries) {
        final Map<K, E> mapResults = new LiteMap<>();
        final Guardian.GuardContext ctxGuard = GuardSupport.getThreadContext();
        final long cMillis = ctxGuard == null ? 0L : ctxGuard.getTimeoutMillis();
        final Iterator it = entries.iterator();
        while (it.hasNext()) {
            final InvocableMap.Entry<K, V> entry = (InvocableMap.Entry) it.next();
            if (entry.isPresent()) {
                mapResults.put(entry.getKey(), this.process(entry));
                it.remove();
            }
            if (ctxGuard != null) {
                ctxGuard.heartbeat(cMillis);
            }
        }
        final List<?> keys = entries.stream()
                .map(BinaryEntry.class::cast)
                .map(BinaryEntry::getBinaryKey)
                .collect(Collectors.toList());
        final Iterator it2 = entries.iterator();
        if (it2.hasNext()) {
            final InvocableMap.Entry<K, V> entry = (InvocableMap.Entry) it2.next();
            ((CacheMap) ((BinaryEntry) entry).getBackingMapContext().getBackingMap()).getAll(keys);
            if (ctxGuard != null) {
                ctxGuard.heartbeat(cMillis);
            }
            mapResults.put(entry.getKey(), this.process(entry));
            it2.remove();
        }
        while (it2.hasNext()) {
            final InvocableMap.Entry<K, V> entry = (InvocableMap.Entry) it2.next();
            mapResults.put(entry.getKey(), this.process(entry));
            if (ctxGuard != null) {
                ctxGuard.heartbeat(cMillis);
            }
        }
        return mapResults;
    }
}
