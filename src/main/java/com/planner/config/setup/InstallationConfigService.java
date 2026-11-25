package com.planner.config.setup;

import com.planner.config.localidade.LocalizacaoInstalacaoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
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
    private final Path arquivoConfiguracaoExplicito;

    @Autowired
    public InstallationConfigService() {
        this.arquivoConfiguracaoExplicito = null;
    }

    InstallationConfigService(Path arquivoConfiguracaoExplicito) {
        this.arquivoConfiguracaoExplicito = arquivoConfiguracaoExplicito.toAbsolutePath().normalize();
    }

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
                persistir(file, properties);
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
                persistir(file, properties);
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

    public LocalizacaoInstalacaoDTO getLocalizacao() {
        synchronized (configuracaoLock) {
            Path file = configFile();
            if (!Files.exists(file)) {
                return LocalizacaoInstalacaoDTO.vazia();
            }
            Properties properties = load(file);
            return new LocalizacaoInstalacaoDTO(
                    properties.getProperty("installation.location.uf", ""),
                    properties.getProperty("installation.location.municipio", ""),
                    properties.getProperty("installation.location.codigo-ibge", "")
            );
        }
    }

    public void saveLocalizacao(LocalizacaoInstalacaoDTO localizacao) {
        synchronized (configuracaoLock) {
            Path file = configFile();
            try {
                Files.createDirectories(file.getParent());
                Properties properties = Files.exists(file) ? load(file) : new Properties();
                if (localizacao == null || !localizacao.configurada()) {
                    properties.remove("installation.location.uf");
                    properties.remove("installation.location.municipio");
                    properties.remove("installation.location.codigo-ibge");
                } else {
                    properties.setProperty("installation.location.uf", localizacao.uf());
                    properties.setProperty("installation.location.municipio", localizacao.municipio());
                    properties.setProperty("installation.location.codigo-ibge", localizacao.codigoIbge());
                }
                persistir(file, properties);
            } catch (IOException ex) {
                throw new IllegalStateException("Não foi possível persistir a localização da instalação.", ex);
            }
        }
    }

    public Path configFile() {
        if (arquivoConfiguracaoExplicito != null) {
            return arquivoConfiguracaoExplicito;
        }
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
        try (var input = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) {
            properties.load(input);
            return properties;
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível ler a configuração local.", ex);
        }
    }

    private void persistir(Path file, Properties properties) throws IOException {
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
