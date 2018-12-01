package org.uze.coherence;

import com.oracle.common.util.Duration;
import com.tangosol.coherence.dslquery.CoherenceQueryLanguage;
import com.tangosol.coherence.dslquery.ExecutionContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.QueryHelper;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.KeyExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Set;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class CohQueryLanguageApp {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ValueExtractor extractor = new KeyExtractor();

    public static void main(String[] args) throws Exception {
        new CohQueryLanguageApp().run();
    }

    private void run() throws Exception {
        logger.info("Starting...");
        final NamedCache items = CacheFactory.getCache("Items");

        items.addIndex(extractor, false, null);

        Filter filter = QueryHelper.createFilter("key()>100l");

        Set set = items.keySet(filter);
        logger.info("Got {} keys", set.size());

        Set entrySet = items.entrySet(filter);
        logger.info("Got {} entries", entrySet.size());

        ExecutionContext context = new ExecutionContext();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter writer = new PrintWriter(System.out);

        context.setTimeout(new Duration(30, Duration.Magnitude.SECOND));
        context.setTraceEnabled(false);
        context.setSanityCheckingEnabled(true);
        context.setExtendedLanguage(true);
        context.setWriter(writer);
        context.setCoherenceQueryLanguage(new CoherenceQueryLanguage());
        context.setReader(reader);

        for (; ; ) {
            System.out.print("CohQL:");
            String raw = reader.readLine();
            if (raw == null) {
                break;
            }
            if (raw.isEmpty()) {
                continue;
            }
            if ("quit".equals(raw)) {
                System.out.println("Bye!");
                break;
            }
            context.getStatementExecutor()
                    .execute(new StringReader(raw.trim()), context);
        }

        logger.info("Done!");
    }
}
