package dev.zzcheah.interview.csvduplicatedetector;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

class HashSetDuplicateDetectorTest {

    @Test
    void simpleHashSetDuplicateDetectorTest() throws IOException {
        String filename = "test-01.csv";
        DuplicateDetector spiedDetector = spy(new HashSetDuplicateDetector());
        spiedDetector.run(filename);
        verify(spiedDetector, times(1)).detected("002");
        verify(spiedDetector, times(1)).detected("006");
        verify(spiedDetector, times(2)).detected(any());
    }

}