package com.planner.habitos.repository;

import java.time.LocalDate;

public record RegistroHabitoDashboardProjection(Long habitoId, LocalDate dataRegistro) {
}
