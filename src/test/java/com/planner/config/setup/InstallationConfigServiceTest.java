package com.planner.config.setup;

import com.planner.config.localidade.LocalizacaoInstalacaoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstallationConfigServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void persisteLocalizacaoNoArquivoLocalDaInstalacao() {
        InstallationConfigService service = new InstallationConfigService(
                tempDir.resolve("application.properties"));
        LocalizacaoInstalacaoDTO localizacao = new LocalizacaoInstalacaoDTO("SP", "São Paulo", "3550308");

        service.saveLocalizacao(localizacao);

        assertEquals(localizacao, service.getLocalizacao());
    }
}
