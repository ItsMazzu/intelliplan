package com.planner.habitos.service;

import com.planner.common.exception.BusinessRuleException;
import com.planner.common.exception.DuplicateResourceException;
import com.planner.common.exception.ResourceNotFoundException;
import com.planner.habitos.dto.RegistroHabitoRequestDTO;
import com.planner.habitos.dto.RegistroHabitoResponseDTO;
import com.planner.habitos.entity.Habito;
import com.planner.habitos.entity.RegistroHabito;
import com.planner.habitos.repository.RegistroHabitoRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class RegistroHabitoService {

    private final RegistroHabitoRepository registroHabitoRepository;
    private final HabitoService habitoService;

    public RegistroHabitoService(RegistroHabitoRepository registroHabitoRepository,
                                 HabitoService habitoService) {
        this.registroHabitoRepository = registroHabitoRepository;
        this.habitoService = habitoService;
    }

    @Transactional
    public RegistroHabitoResponseDTO registrar(Long usuarioId, RegistroHabitoRequestDTO dto) {
        Habito habito = habitoService.buscarHabitoDoUsuario(usuarioId, dto.habitoId());

        if (!habito.isAtivo()) {
            throw new BusinessRuleException("Não é possível registrar ocorrências para um hábito inativo.");
        }

        if (dto.dataRegistro().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("A data do registro não pode ser futura.");
        }

        if (registroHabitoRepository.existsByHabitoIdAndDataRegistro(dto.habitoId(), dto.dataRegistro())) {
            throw new DuplicateResourceException("Já existe um registro para este hábito na data informada.");
        }

        RegistroHabito registro = new RegistroHabito();
        registro.setHabito(habito);
        registro.setDataRegistro(dto.dataRegistro());
        registro.setObservacao(dto.observacao());

        try {
            return toResponseDTO(registroHabitoRepository.saveAndFlush(registro));
        } catch (DataIntegrityViolationException ex) {
            if (isRegistroHabitoUniqueViolation(ex)) {
                throw new DuplicateResourceException("Já existe um registro para este hábito na data informada.");
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public RegistroHabitoResponseDTO buscarPorId(Long usuarioId, Long id) {
        return toResponseDTO(buscarRegistroDoUsuario(usuarioId, id));
    }

    @Transactional(readOnly = true)
    public List<RegistroHabitoResponseDTO> listarPorHabito(Long usuarioId, Long habitoId) {
        habitoService.buscarHabitoDoUsuario(usuarioId, habitoId);
        return registroHabitoRepository.findByHabitoId(habitoId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RegistroHabitoResponseDTO> listarPorPeriodo(Long usuarioId, Long habitoId, LocalDate inicio, LocalDate fim) {
        habitoService.buscarHabitoDoUsuario(usuarioId, habitoId);
        if (inicio == null || fim == null) {
            throw new BusinessRuleException("As datas inicial e final do período são obrigatórias.");
        }
        if (fim.isBefore(inicio)) {
            throw new BusinessRuleException("A data final do período não pode ser anterior à data inicial.");
        }
        return registroHabitoRepository.findByHabitoIdAndDataRegistroBetween(habitoId, inicio, fim).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public void excluir(Long usuarioId, Long id) {
        RegistroHabito registro = buscarRegistroDoUsuario(usuarioId, id);
        registroHabitoRepository.delete(registro);
    }

    private RegistroHabito buscarRegistroDoUsuario(Long usuarioId, Long id) {
        RegistroHabito registro = registroHabitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de hábito não encontrado."));
        if (!registro.getHabito().getUsuario().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Registro de hábito não encontrado.");
        }
        return registro;
    }

    private boolean isRegistroHabitoUniqueViolation(DataIntegrityViolationException ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof ConstraintViolationException cve) {
                String name = cve.getConstraintName();
                if (name != null && name.toLowerCase(Locale.ROOT).contains("uk_registro_habito_data")) {
                    return true;
                }
            }
            if (current.getMessage() != null && current.getMessage().toLowerCase(Locale.ROOT).contains("uk_registro_habito_data")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private RegistroHabitoResponseDTO toResponseDTO(RegistroHabito registro) {
        return new RegistroHabitoResponseDTO(
                registro.getId(),
                registro.getHabito().getId(),
                registro.getDataRegistro(),
                registro.getObservacao(),
                registro.getRegistradoEm()
        );
    }
}
