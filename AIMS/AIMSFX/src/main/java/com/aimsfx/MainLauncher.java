package com.aimsfx;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class MainLauncher {

    private static ConfigurableApplicationContext springContext;

    private static void killProcessOnPort(int port) {
        try {
            System.out.println("🔄 [System] Checking and releasing port " + port + "...");

            // 1. Use ProcessBuilder instead of Runtime.exec
            // Param 1: Call cmd, Param 2: /c (terminate after execution), Param 3: Command
            // to run
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "netstat -ano | findstr :" + port);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String pid = null;

            // Read returned results from netstat command
            while ((line = reader.readLine()) != null) {
                if (line.contains("LISTENING")) {
                    String[] parts = line.trim().split("\\s+");
                    pid = parts[parts.length - 1];
                    break;
                }
            }
            reader.close();

            // If a running PID is found, proceed to kill it
            if (pid != null && !pid.isEmpty()) {
                System.out.println("⚠️ [Detected] Port " + port + " is occupied by process with PID: " + pid);

                // 2. Convert taskkill command to ProcessBuilder standard (Separate arguments)
                ProcessBuilder killBuilder = new ProcessBuilder("taskkill", "/F", "/PID", pid);
                killBuilder.start().waitFor();

                System.out.println("✅ [Success] Port " + port + " released safely.");
            } else {
                System.out.println("🌱 [Clean] Port " + port + " is currently free. Starting boot process!");
            }

        } catch (Exception e) {
            System.err.println("❌ [Error] Cannot automatically release port " + port + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Start Spring Boot in background
        killProcessOnPort(8080);
        springContext = SpringApplication.run(MainLauncher.class, args);

        // Launch JavaFX application
        Application.launch(HelloApplication.class, args);
    }

    public static ConfigurableApplicationContext getSpringContext() {
        return springContext;
    }
}