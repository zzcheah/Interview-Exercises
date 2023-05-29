package dev.zzcheah.interview.csvduplicatedetector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class HashSetDuplicateDetector implements DuplicateDetector {

    private final Set<String> existingEntries = new HashSet<>();

    @Override
    public void run(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream(filename))))) {

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] fields = currentLine.split(",");
                String id = fields[2] + "#" + fields[3];
                String name = fields[1];
                String date = fields[4];

                String a = id + "," + name;
                String b = name + "," + date;
                String c = id + "," + date;

                for (String entry : List.of(a, b, c)) {
                    if (existingEntries.contains(entry)) {
                        System.out.println("Duplicated entry: " + entry + " at row " + fields[0]);
                        detected(fields[0]);
                    } else {
                        existingEntries.add(entry);
                    }
                }
            }

        }
    }

    @Override
    public void detected(String row) {
        // do nothing
    }
}
