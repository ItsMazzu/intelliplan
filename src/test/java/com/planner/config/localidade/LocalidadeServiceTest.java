package com.planner.config.localidade;

import com.planner.common.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalidadeServiceTest {

    @Test
    void validaCorrespondenciaDeUfMunicipioECodigoIbge() {
        IbgeLocalidadeClient client = mock(IbgeLocalidadeClient.class);
        when(client.listarEstados()).thenReturn(List.of(new EstadoDTO("SP", "São Paulo")));
        when(client.listarMunicipios("SP"))
                .thenReturn(List.of(new MunicipioDTO("3548500", "Santos", "SP")));
        LocalidadeService service = new LocalidadeService(client);

        assertEquals(
                new LocalizacaoInstalacaoDTO("SP", "Santos", "3548500"),
                service.validar(new LocalizacaoRequestDTO("sp", "Santos", "3548500"))
        );
        assertThrows(BusinessRuleException.class,
                () -> service.validar(new LocalizacaoRequestDTO("RJ", "Santos", "3548500")));
        assertThrows(BusinessRuleException.class,
                () -> service.validar(new LocalizacaoRequestDTO("SP", "São Paulo", "3548500")));
        verify(client, times(1)).listarEstados();
        verify(client, times(1)).listarMunicipios("SP");
    }
}
