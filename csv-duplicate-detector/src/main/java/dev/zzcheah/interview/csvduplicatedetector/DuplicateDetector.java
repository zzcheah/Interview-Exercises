package dev.zzcheah.interview.csvduplicatedetector;

import java.io.IOException;

public interface DuplicateDetector {
    void run(String filename) throws IOException;
    void detected(String row);
}
