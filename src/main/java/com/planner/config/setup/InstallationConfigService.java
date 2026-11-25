package com.planner.config.setup;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;

@Service
public class InstallationConfigService {

    private final Object configuracaoLock = new Object();

    public boolean testConnection(DatabaseConfigDTO config) {
        String url = url(config);
        try (Connection connection = DriverManager.getConnection(url, config.usuario(), config.senha())) {
            return connection.isValid(5);
        } catch (Exception ex) {
            return false;
        }
    }

    public void saveDatabaseConfig(DatabaseConfigDTO config) {
        synchronized (configuracaoLock) {
            Path file = configFile();
            try {
                Files.createDirectories(file.getParent());
                Properties properties = Files.exists(file) ? load(file) : new Properties();
                properties.setProperty("spring.datasource.url", url(config));
                properties.setProperty("spring.datasource.username", config.usuario());
                properties.setProperty("spring.datasource.password", config.senha());
                properties.setProperty("installation.initialized", "false");

                Path temporary = Files.createTempFile(file.getParent(), "intelliplan-", ".tmp");
                try (var writer = new OutputStreamWriter(Files.newOutputStream(temporary), StandardCharsets.UTF_8)) {
                    properties.store(writer, "IntelliPlan local configuration");
                }
                protectTemporaryFile(temporary);
                try {
                    Files.move(temporary, file,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException ex) {
                    Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
                }
                protectTemporaryFile(file);
            } catch (IOException ex) {
                throw new IllegalStateException("Não foi possível persistir a configuração local.", ex);
            }
        }
    }

    public void markInitialized() {
        synchronized (configuracaoLock) {
            Path file = configFile();
            try {
                Files.createDirectories(file.getParent());
                Properties properties = Files.exists(file) ? load(file) : new Properties();
                properties.setProperty("installation.initialized", "true");
                Path temporary = Files.createTempFile(file.getParent(), "intelliplan-", ".tmp");
                try (var writer = new OutputStreamWriter(Files.newOutputStream(temporary), StandardCharsets.UTF_8)) {
                    properties.store(writer, "IntelliPlan local configuration");
                }
                protectTemporaryFile(temporary);
                try {
                    Files.move(temporary, file,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException ex) {
                    Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
                }
                protectTemporaryFile(file);
            } catch (IOException ex) {
                throw new IllegalStateException("Não foi possível concluir a persistência da instalação.", ex);
            }
        }
    }

    public boolean isInitialized() {
        synchronized (configuracaoLock) {
            Path file = configFile();
            if (!Files.exists(file)) {
                return false;
            }
            Properties properties = load(file);
            return "true".equalsIgnoreCase(properties.getProperty("installation.initialized", "false"));
        }
    }

    public Path configFile() {
        String configured = System.getenv("INTELLIPLAN_CONFIG_FILE");
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured).toAbsolutePath().normalize();
        }
        return Path.of(System.getProperty("user.home"), ".intelliplan", "application.properties")
                .toAbsolutePath()
                .normalize();
    }

    private Properties load(Path file) {
        Properties properties = new Properties();
        try (var input = Files.newInputStream(file)) {
            properties.load(input);
            return properties;
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível ler a configuração local.", ex);
        }
    }

    private String url(DatabaseConfigDTO config) {
        return "jdbc:postgresql://" + config.host() + ":" + config.porta() + "/" + config.banco();
    }

    private void protectTemporaryFile(Path file) {
        try {
            Set<PosixFilePermission> permissions = EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE
            );
            Files.setPosixFilePermissions(file, permissions);
        } catch (UnsupportedOperationException | IOException ex) {
            // Windows doesn't expose POSIX permissions; the file remains outside the repository.
        }
    }
}
