package com.example.SpringREST.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class SemgrepService {

    public String runSemgrep(String filePath) {
        StringBuilder output = new StringBuilder();
        try {
            // Command: semgrep --config auto --json <filePath>
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "semgrep", "--config", "auto", "--json", filePath);

            processBuilder.redirectErrorStream(true); // Merge error & output
            Process process = processBuilder.start();

            // Capture CLI output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return "Semgrep failed with exit code: " + exitCode + "\nOutput:\n" + output;
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error running Semgrep: " + e.getMessage();
        }
        return output.toString();
    }
}

