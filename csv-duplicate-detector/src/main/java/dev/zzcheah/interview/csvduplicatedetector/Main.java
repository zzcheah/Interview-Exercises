package dev.zzcheah.interview.csvduplicatedetector;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        String filename = "interviewFindRelationshipNoRepeat.csv";
        DuplicateDetector duplicateDetector = new HashSetDuplicateDetector();
        duplicateDetector.run(filename);

    }
}