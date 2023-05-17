package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler {
    private final Clock clock;
    private final Duration timeout;
    private final int popularWordCount;
    private final int maxDepth;
    private final List<Pattern> ignoredUrls;
    private final ForkJoinPool pool;
    private final PageParserFactory parserFactory;


    @Inject
    ParallelWebCrawler(
            Clock clock,
            PageParserFactory parserFactory,
            @Timeout Duration timeout,
            @PopularWordCount int popularWordCount,
            @MaxDepth int maxDepth,
            @IgnoredUrls List<Pattern> ignoredUrls,
            @TargetParallelism int threadCount) {
        this.clock = clock;
        this.parserFactory = parserFactory;
        this.timeout = timeout;
        this.popularWordCount = popularWordCount;
        this.maxDepth = maxDepth;
        this.ignoredUrls = ignoredUrls;
        this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
    }

    @Override
    public CrawlResult crawl(List<String> startingUrls) {
        Instant deadline = clock.instant().plus(timeout);
        ConcurrentMap<String, Integer> counts = new ConcurrentHashMap<>();
        ConcurrentSkipListSet<String> visitedUrls = new ConcurrentSkipListSet<>();

        for (String url : startingUrls) {
            pool.invoke(
                    new CrawlInternalTask(
                            url, deadline, maxDepth, counts, visitedUrls, clock, parserFactory, ignoredUrls));
        }
        if (counts.isEmpty()) {
            return new CrawlResult
                    .Builder()
                    .setWordCounts(counts)
                    .setUrlsVisited(visitedUrls.size())
                    .build();
        }
        return new CrawlResult
                .Builder()
                .setWordCounts(WordCounts.sort(counts, popularWordCount))
                .setUrlsVisited(visitedUrls.size())
                .build();
    }

    @Override
    public int getMaxParallelism() {
        return Runtime.getRuntime().availableProcessors();
    }

    private static class CrawlInternalTask extends RecursiveTask<Boolean> {
        private final String url;
        private final Instant deadline;
        private final int maxDepth;
        private final ConcurrentMap<String, Integer> counts;
        private final ConcurrentSkipListSet<String> visitedUrls;
        private final Clock clock;
        @Inject
        private final PageParserFactory parserFactory;
        private final List<Pattern> ignoredUrls;

        public CrawlInternalTask(
                String url,
                Instant deadline,
                int maxDepth,
                ConcurrentMap<String, Integer> counts,
                ConcurrentSkipListSet<String> visitedUrls,
                Clock clock,
                PageParserFactory parserFactory,
                List<Pattern> ignoredUrls) {
            this.url = url;
            this.deadline = deadline;
            this.maxDepth = maxDepth;
            this.counts = counts;
            this.visitedUrls = visitedUrls;
            this.clock = clock;
            this.parserFactory = parserFactory;
            this.ignoredUrls = ignoredUrls;
        }

        @Override
        protected Boolean compute() {
            if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
                return false;
            }
            for (Pattern pattern : ignoredUrls) {
                if (pattern.matcher(url).matches()) {
                    return false;
                }
            }
//            if (!visitedUrls.contains(url)) {
//                return false;
//            }
//            visitedUrls.add(url);
//             for threadsafe (atomic)
            if(!visitedUrls.add(url)) {
                return false;
            }
            PageParser.Result result = parserFactory.get(url).parse();
            for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
                counts.compute(e.getKey(), (k, v) -> (v == null) ? e.getValue() : e.getValue() + v);
            }
            List<CrawlInternalTask> subTasks = new ArrayList<>();
            for (String link : result.getLinks()) {
                subTasks.add(
                        new CrawlInternalTask(
                                link, deadline, maxDepth - 1, counts, visitedUrls,
                                clock, parserFactory, ignoredUrls));
            }
            invokeAll(subTasks);
            return true;
        }
    }

}
