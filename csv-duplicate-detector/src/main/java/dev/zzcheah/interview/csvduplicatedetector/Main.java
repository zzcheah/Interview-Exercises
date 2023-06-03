package dev.zzcheah.interview.csvduplicatedetector;

import dev.zzcheah.interview.csvduplicatedetector.models.MatcherEntry;
import dev.zzcheah.interview.csvduplicatedetector.models.MatcherResult;
import dev.zzcheah.interview.csvduplicatedetector.utils.Matcher;
import dev.zzcheah.interview.csvduplicatedetector.utils.SimpleHashMapMatcher;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

@Slf4j
public class Main {
    private static final String DUPLICATE_REPORT_FORMAT = "Duplicated entry: row {} and row {} are duplicate";


    public static void main(String[] args) throws IOException {

        String filename = "interviewFindRelationshipNoRepeat.csv";

        Matcher matcher = new SimpleHashMapMatcher();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream(filename))))) {

            String currentLine;
            int rowCount = 1;

            while ((currentLine = reader.readLine()) != null) {

                List<MatcherEntry> entries = createEntries(currentLine, rowCount);

                for (MatcherEntry entry : entries) {
                    MatcherResult result = matcher.match(entry);
                    if (result != null) {
                        log.info(DUPLICATE_REPORT_FORMAT, result.originalRow(), rowCount);
                    }
                }
                rowCount++;
            }

        }

    }

    private static List<MatcherEntry> createEntries(String currentLine, int rowCount) {

        String[] fields = currentLine.split(",");

        String id = fields[2] + "#" + fields[3];
        String name = fields[1];
        String date = fields[4];

        // create entries
        MatcherEntry a = new MatcherEntry(id + "," + name, rowCount);
        MatcherEntry b = new MatcherEntry(name + "," + date, rowCount);
        MatcherEntry c = new MatcherEntry(id + "," + date, rowCount);

        return List.of(a, b, c);
    }
}