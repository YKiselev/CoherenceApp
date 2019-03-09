package org.uze.coherence;

import com.google.common.base.Stopwatch;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.aggregator.DistinctValues;
import com.tangosol.util.extractor.KeyExtractor;
import com.tangosol.util.extractor.MultiExtractor;
import com.tangosol.util.processor.ExtractorProcessor;
import com.tangosol.util.stream.RemoteCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uze.coherence.model.Item;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class ClientAggregatorApp {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ValueExtractor extractor = new KeyExtractor();

    public static void main(String[] args) throws Exception {
        new ClientAggregatorApp().run();
    }

    private void run() throws Exception {
        logger.info("Starting...");
        final NamedCache items = CacheFactory.getCache("Items");

        final List<Object> keys = LongStream.range(1, 10_000).boxed().collect(Collectors.toList());

        logger.info("Cache size is {} items", items.size());

        final Stopwatch sw = Stopwatch.createStarted();
        final MultiExtractor multiExtractor = new MultiExtractor("getId,getPayload");

        if (true) {
            logger.info("Invoking warming up processor");
            //sw.reset().start();
            //Map result2 = items.invokeAll(keys, new CompositeProcessor(new InvocableMap.EntryProcessor[]{PreloadRequest.INSTANCE, new ExtractorProcessor(multiExtractor)}));
            //Map result2 = items.invokeAll(keys, new ConditionalProcessor(NeverFilter.INSTANCE, PreloadRequest.INSTANCE));
            //logger.info("Got {} items in {}", result2.size(), sw);
            sw.reset().start();
            //Map bulkPreloadResult = items.invokeAll(keys, new BulkPreloadExtractorProcessor(multiExtractor));
            Map bulkPreloadResult = items.invokeAll(keys, new BulkPreloadProcessor(new ExtractorProcessor(multiExtractor), 5_000));
            logger.info("Bulk preload complete in {}: got {} items", sw, bulkPreloadResult.size());
        }

        if (true) {
            logger.info("Aggregating");
            sw.reset().start();
            //final Map<Long, List<Object>> result = (Map) items.aggregate(keys, new ReducerAggregator(multiExtractor));
            final Collection<List<Object>> result = (Collection) items.aggregate(keys, new DistinctValues(multiExtractor));
            logger.info("Got {} items in {}", result.size(), sw);
        }

        if (false) {
            sw.reset().start();
            List streamResult = (List) items.stream(keys, multiExtractor).collect(RemoteCollectors.toList());
            logger.info("Got {} items in {}", streamResult.size(), sw);
        }

        if (false) {
            logger.info("Getting all");
            sw.reset().start();
            Map result4 = items.getAll(keys);
            logger.info("Got {} items in {}", result4.size(), sw);
        }

        logger.info("Done!");
    }

    private Map<Object, Item> generate(int count) {
        final long seq = System.currentTimeMillis();
        final Map<Object, Item> items = new HashMap<>();
        for (int i = 0; i < count; i++) {
            items.put(
                    seq + i,
                    new Item("key#" + i, Collections.singletonList(123L))
            );
        }
        return items;
    }
}
