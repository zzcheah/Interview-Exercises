package dev.zzcheah.interview.csvduplicatedetector.utils;

import dev.zzcheah.interview.csvduplicatedetector.models.MatcherEntry;
import dev.zzcheah.interview.csvduplicatedetector.models.MatcherResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

class SimpleHashMapMatcherTest {

    @Test
    void simpleHashMapMatcherTest() {

        Matcher spiedMatcher = spy(new SimpleHashMapMatcher());

        String duplicate1 = "abc";
        String duplicate2 = "bcd";

        List<MatcherEntry> entries = List.of(
                new MatcherEntry(duplicate1,1),
                new MatcherEntry("def",2),
                new MatcherEntry("xyz",3),
                new MatcherEntry(duplicate1,4),  // duplicate with 1
                new MatcherEntry(duplicate2,5),
                new MatcherEntry(duplicate2,6)   // duplicate with 5
        );

        List<MatcherResult> results = entries.stream()
                .map(spiedMatcher::match)
                .filter(Objects::nonNull)
                .toList();

        assertEquals(2, results.size());
        assertTrue(results.contains(new MatcherResult(1,4)));
        assertTrue(results.contains(new MatcherResult(5,6)));

    }

}