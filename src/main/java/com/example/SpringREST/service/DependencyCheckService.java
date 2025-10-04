package com.example.SpringREST.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Service
public class DependencyCheckService {

    private static final String DEP_CHECK_PATH = "C:\\Users\\udayg\\Downloads\\dependency-check-12.1.3-release\\dependency-check\\bin\\dependency-check.bat";
    private static final String REPORT_DIR = "C:\\Users\\udayg\\dependency-check-report";

    public String runScan(String projectPath) {
        try {
            // Build the command
            ProcessBuilder processBuilder = new ProcessBuilder(
                    DEP_CHECK_PATH,
                    "--scan", projectPath,
                    "--format", "ALL",
                    "--out", REPORT_DIR
            );

            processBuilder.directory(new File(REPORT_DIR));
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // Capture logs
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[Dependency-Check] " + line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return REPORT_DIR + "\\dependency-check-report.html";
            } else {
                return "Dependency-Check scan failed. Exit code: " + exitCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error while running Dependency-Check: " + e.getMessage();
        }
    }
}

