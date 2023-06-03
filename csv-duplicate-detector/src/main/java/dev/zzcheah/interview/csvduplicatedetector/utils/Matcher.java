package dev.zzcheah.interview.csvduplicatedetector.utils;

import dev.zzcheah.interview.csvduplicatedetector.models.MatcherEntry;
import dev.zzcheah.interview.csvduplicatedetector.models.MatcherResult;

public interface Matcher {

    /**
     * return {@link MatcherResult} if the entry is matched else null <br/>
     * entry is expected to be added to pool of entries for future matching.
     */
    MatcherResult match(MatcherEntry toMatch);

}
