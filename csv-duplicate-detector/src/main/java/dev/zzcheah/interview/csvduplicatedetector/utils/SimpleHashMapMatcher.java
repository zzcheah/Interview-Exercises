package dev.zzcheah.interview.csvduplicatedetector.utils;

import dev.zzcheah.interview.csvduplicatedetector.models.MatcherEntry;
import dev.zzcheah.interview.csvduplicatedetector.models.MatcherResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple matcher implementation that store all existing records in HashMap <br/>
 * This may be suitable if small set of entries is expected. <br/>
 * For larger set of entry, consider implementing matcher using Bloom Filter. Ref: <br/>
 * <a href="https://github.com/zzcheah/Interview-Exercises/issues/2">BloomFilter Discussion</a>
 */
public class SimpleHashMapMatcher implements Matcher {

    private final Map<String, Integer> existingEntries = new HashMap<>();

    @Override
    public MatcherResult match(MatcherEntry toMatch) {
        if (existingEntries.containsKey(toMatch.item())) {
            return new MatcherResult(
                    existingEntries.get(toMatch.item()),
                    toMatch.row()
            );
        } else {
            registerEntry(toMatch);
            return null;
        }
    }

    private void registerEntry(MatcherEntry entry) {
        existingEntries.put(entry.item(), entry.row());
    }
}
