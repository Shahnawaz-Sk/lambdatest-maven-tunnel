package com.lambdatest;

import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class KillPort {

    private OperatingSystem os;

    public KillPort() {
        SystemInfo si = new SystemInfo();
        this.os = si.getOperatingSystem();
    }

    public void killProcess(int port) {
        int pid = getPid(port);
        if (pid <= 0) {
            System.out.println("No process found on port: " + port);
            return;
        }

        try {
            String cmd = System.getProperty("os.name").startsWith("Windows") ?
                    "taskkill /F /PID " + pid :
                    "kill -9 " + pid;
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            System.out.println("Process " + pid + " has been terminated.");
        } catch (IOException | InterruptedException e) {
            System.err.println("Error terminating process: " + e.getMessage());
        }
    }

    private int getPid(int port) {
        String command = System.getProperty("os.name").startsWith("Windows") ? 
                         "netstat -ano | findstr :" + port :
                         "netstat -tulpn | grep :" + port;
        return executeSystemCommandForPid(command, port);
    }

    private int executeSystemCommandForPid(String command, int port) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(command).getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(Integer.toString(port))) {
                    return parsePidFromLine(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to execute command to find PID: " + e.getMessage());
        }
        return -1;
    }

    private int parsePidFromLine(String line) {
        String[] parts = line.trim().split("\\s+");
        String lastColumn = parts[parts.length - 1];
        try {
            return System.getProperty("os.name").startsWith("Windows") ? 
                   Integer.parseInt(parts[parts.length - 1]) :
                   Integer.parseInt(lastColumn.substring(0, lastColumn.indexOf('/')));
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse PID from line: " + line);
            return -1;
        }
    }
}
