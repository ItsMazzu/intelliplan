package com.planner.dashboard.dto;

import java.util.List;

public record DashboardDadosDTO(
        String usuarioNome,
        String primeiroNome,
        Long totalTarefas,
        Long totalProjetos,
        Long totalHabitos,
        List<DiaHabitoDTO> diasHabitos,
        String mesAtual,
        List<DiaCalendarioDTO> diasCalendario,
        List<TarefaItemDTO> tarefasHoje,
        Integer progressoGeral,
        String progressoTarefas,
        String progressoEstudos,
        String progressoHabitos,
        String progressoProjetos,
        List<ProjetoItemDTO> projetosAtivos,
        List<MateriaItemDTO> materias
) {
}
