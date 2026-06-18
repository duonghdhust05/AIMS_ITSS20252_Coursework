package com.aimsfx;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class MainLauncher {

    private static ConfigurableApplicationContext springContext;
    
    // Globally accessible active port for other components like EmailService
    public static int activePort = 8080;

    // Checks if the port is free by trying to bind to it
    private static boolean isPortFree(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void killProcessOnPort(int port) {
        try {
            System.out.println("[System] Checking and releasing port " + port + "...");

            // Use findstr /c:":<port> " to match exact port (avoiding 18080, 80808 etc.)
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "netstat -ano | findstr /c:\":" + port + " \"");
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String pid = null;

            while ((line = reader.readLine()) != null) {
                if (line.contains("LISTENING")) {
                    String[] parts = line.trim().split("\\s+");
                    pid = parts[parts.length - 1];
                    break;
                }
            }
            reader.close();

            if (pid != null && !pid.isEmpty()) {
                System.out.println("[Detected] Port " + port + " is occupied by process with PID: " + pid);

                ProcessBuilder killBuilder = new ProcessBuilder("taskkill", "/F", "/PID", pid);
                Process killProcess = killBuilder.start();
                int exitCode = killProcess.waitFor();
                
                if (exitCode == 0) {
                    System.out.println("[Success] Sent kill command to process " + pid);
                } else {
                    System.err.println("[Warning] taskkill returned non-zero exit code: " + exitCode);
                }
            } else {
                System.out.println("[Clean] Port " + port + " is currently free.");
            }

        } catch (Exception e) {
            System.err.println("[Error] Cannot automatically release port " + port + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int defaultPort = 8080;
        
        // 1. Read default port configuration from application.properties
        try (InputStream input = MainLauncher.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                String portProp = prop.getProperty("server.port");
                if (portProp != null) {
                    defaultPort = Integer.parseInt(portProp.trim());
                }
            }
        } catch (Exception e) {
            System.err.println("[System] Could not load application.properties server.port: " + e.getMessage() + ". Defaulting to 8080.");
        }

        int targetPort = defaultPort;
        boolean portFound = false;

        // 2. Scan up to 10 consecutive ports to find a free one
        for (int offset = 0; offset < 10; offset++) {
            int currentPort = defaultPort + offset;
            System.out.println("[System] Evaluating port " + currentPort + "...");
            
            if (!isPortFree(currentPort)) {
                // If it is occupied, try to kill the occupying process
                killProcessOnPort(currentPort);
                
                // Wait briefly for release
                for (int i = 0; i < 5; i++) {
                    if (isPortFree(currentPort)) {
                        break;
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            // Verify if the port is free now
            if (isPortFree(currentPort)) {
                targetPort = currentPort;
                portFound = true;
                break;
            } else {
                System.out.println("[Occupied] Port " + currentPort + " remains busy. Trying next candidate...");
            }
        }

        if (!portFound) {
            System.err.println("[Critical] Could not find any free port in range " + defaultPort + " - " + (defaultPort + 9) + ". Exiting!");
            System.exit(1);
        }

        activePort = targetPort;
        
        // 3. Override server.port for Spring Boot dynamically
        System.setProperty("server.port", String.valueOf(activePort));
        System.out.println("[Ready] Selected active port: " + activePort + ". Starting Spring Boot...");
        
        // Start Spring Boot in background
        springContext = SpringApplication.run(MainLauncher.class, args);

        // Launch JavaFX application
        Application.launch(HelloApplication.class, args);
    }

    public static ConfigurableApplicationContext getSpringContext() {
        return springContext;
    }
}