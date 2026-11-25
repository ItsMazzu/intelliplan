/**
 * IntelliPlan - Dashboard JavaScript
 * Interatividade e funcionalidades do frontend
 */

document.addEventListener('DOMContentLoaded', function () {
    const favoriteUser = document.querySelector('[data-favorite-user]')?.textContent.trim().toLowerCase();
    const favoriteList = document.querySelector('[data-favorite-list]');
    const favoriteFeedback = document.querySelector('[data-favorite-feedback]');
    const favoriteButtonsSelector = '.star-button[data-task-id]';
    const favorites = new Set();
    const favoriteStorageKey = favoriteUser
        ? `intelliplan:dashboard:favorites:${favoriteUser}`
        : null;

    function showFavoriteFeedback(message, isError) {
        if (!favoriteFeedback) return;

        favoriteFeedback.textContent = message;
        favoriteFeedback.classList.remove(
            'hidden',
            'bg-emerald-50',
            'text-emerald-800',
            'bg-rose-50',
            'text-rose-800'
        );
        favoriteFeedback.classList.add(
            isError ? 'bg-rose-50' : 'bg-emerald-50',
            isError ? 'text-rose-800' : 'text-emerald-800'
        );
    }

    function updateFavoriteButton(button, isFavorite) {
        const label = isFavorite ? 'Remover dos favoritos' : 'Favoritar';
        button.classList.toggle('active', isFavorite);
        button.setAttribute('aria-pressed', String(isFavorite));
        button.setAttribute('aria-label', label);
        button.setAttribute('title', label);

        const icon = button.querySelector('svg') || button.querySelector('[data-lucide="star"]');
        if (icon) icon.setAttribute('fill', isFavorite ? 'currentColor' : 'none');
    }

    function syncFavoriteButtons(container) {
        if (!container) return;

        const buttons = [];
        if (container instanceof Element && container.matches(favoriteButtonsSelector)) {
            buttons.push(container);
        }
        if (container.querySelectorAll) {
            buttons.push(...container.querySelectorAll(favoriteButtonsSelector));
        }
        buttons.forEach((button) => {
            updateFavoriteButton(button, favorites.has(button.dataset.taskId));
        });
    }

    if (favoriteStorageKey) {
        try {
            const storedFavorites = window.localStorage.getItem(favoriteStorageKey);
            if (storedFavorites !== null) {
                const favoriteIds = JSON.parse(storedFavorites);
                if (!Array.isArray(favoriteIds) || favoriteIds.some((id) => typeof id !== 'string')) {
                    throw new TypeError('Lista de favoritos inválida.');
                }
                favoriteIds.forEach((id) => favorites.add(id));
            }
        } catch (error) {
            console.error('Não foi possível carregar os favoritos locais.', error);
            showFavoriteFeedback('Não foi possível carregar os favoritos salvos neste navegador.', true);
        }

        syncFavoriteButtons(document);

        document.addEventListener('click', function (event) {
            const target = event.target instanceof Element ? event.target : null;
            const button = target ? target.closest(favoriteButtonsSelector) : null;
            if (!button) return;

            event.preventDefault();
            const taskId = button.dataset.taskId;
            const wasFavorite = favorites.has(taskId);
            if (wasFavorite) {
                favorites.delete(taskId);
            } else {
                favorites.add(taskId);
            }
            updateFavoriteButton(button, !wasFavorite);

            try {
                window.localStorage.setItem(favoriteStorageKey, JSON.stringify([...favorites]));
                showFavoriteFeedback(
                    !wasFavorite ? 'Tarefa adicionada aos favoritos.' : 'Tarefa removida dos favoritos.',
                    false
                );
            } catch (error) {
                if (wasFavorite) {
                    favorites.add(taskId);
                } else {
                    favorites.delete(taskId);
                }
                updateFavoriteButton(button, wasFavorite);
                console.error('Não foi possível salvar os favoritos locais.', error);
                showFavoriteFeedback('Não foi possível salvar o favorito neste navegador.', true);
            }
        });

        if (favoriteList && window.MutationObserver) {
            const observer = new MutationObserver((mutations) => {
                mutations.forEach((mutation) => {
                    mutation.addedNodes.forEach(syncFavoriteButtons);
                });
            });
            observer.observe(favoriteList, { childList: true, subtree: true });
        }
    } else {
        document.querySelectorAll(favoriteButtonsSelector).forEach((button) => {
            button.disabled = true;
        });
        showFavoriteFeedback('Não foi possível identificar a conta autenticada.', true);
    }

    // ===== Busca (Ctrl+K) =====
    document.addEventListener('keydown', function (e) {
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            const searchBox = document.querySelector('.search-box');
            if (searchBox) {
                searchBox.classList.toggle('active');
                const input = searchBox.querySelector('input');
                if (input) input.focus();
            }
        }
    });

    // ===== Checkboxes das Tarefas =====
    document.querySelectorAll('.tarefa-checkbox').forEach(function (checkbox) {
        checkbox.addEventListener('click', function () {
            this.classList.toggle('checked');
            const tarefa = this.closest('.tarefa-item');
            if (tarefa) {
                const titulo = tarefa.querySelector('.tarefa-titulo');
                if (titulo) {
                    titulo.style.textDecoration = this.classList.contains('checked')
                        ? 'line-through'
                        : 'none';
                    titulo.style.opacity = this.classList.contains('checked')
                        ? '0.5'
                        : '1';
                }
            }
            // Adicionar checkmark SVG
            if (this.classList.contains('checked')) {
                this.innerHTML = '<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>';
            } else {
                this.innerHTML = '';
            }
        });
    });

    // ===== Dias do Calendário =====
    document.querySelectorAll('.calendario-dia').forEach(function (dia) {
        if (!dia.classList.contains('vazio')) {
            dia.addEventListener('click', function () {
                // Remover seleção anterior (mantém hoje)
                document.querySelectorAll('.calendario-dia.selected').forEach(function (d) {
                    d.classList.remove('selected');
                });
                if (!this.classList.contains('hoje')) {
                    this.classList.add('selected');
                    this.style.background = '#f3e8ff';
                    this.style.color = '#7c3aed';
                    this.style.fontWeight = '600';
                }
            });
        }
    });

    // ===== Animação das barras de progresso =====
    function animateProgressBars() {
        document.querySelectorAll('.progress-bar .fill').forEach(function (bar) {
            const width = bar.getAttribute('data-width');
            if (width) {
                bar.style.width = '0%';
                setTimeout(function () {
                    bar.style.width = width + '%';
                }, 100);
            }
        });
    }

    // Observer para animar ao entrar na viewport
    const observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                animateProgressBars();
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1 });

    const projetosSection = document.querySelector('.projetos-section');
    if (projetosSection) {
        observer.observe(projetosSection);
    }

    // Iniciar animação das barras
    animateProgressBars();

    // ===== Donut Chart Animation =====
    function animateDonutChart() {
        const circles = document.querySelectorAll('.donut-segment');
        circles.forEach(function (circle) {
            const finalDash = circle.getAttribute('data-dash');
            if (finalDash) {
                const values = finalDash.split(' ');
                const targetFill = parseFloat(values[0]);
                circle.style.transition = 'stroke-dasharray 1s ease';
                setTimeout(function () {
                    circle.setAttribute('stroke-dasharray', finalDash);
                }, 200);
            }
        });
    }

    animateDonutChart();

    // ===== Tooltip nos atalhos =====
    document.querySelectorAll('.atalho-item').forEach(function (item) {
        item.addEventListener('mouseenter', function () {
            this.style.transform = 'translateY(-3px)';
        });
        item.addEventListener('mouseleave', function () {
            this.style.transform = 'translateY(0)';
        });
    });

    // ===== Sidebar toggle para mobile =====
    const menuToggle = document.getElementById('menu-toggle');
    const sidebar = document.querySelector('.sidebar');

    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', function () {
            sidebar.classList.toggle('sidebar-open');
        });
    }

    // ===== Cards hover effect =====
    document.querySelectorAll('.card').forEach(function (card) {
        card.addEventListener('mouseenter', function () {
            this.style.transform = 'translateY(-2px)';
        });
        card.addEventListener('mouseleave', function () {
            this.style.transform = 'translateY(0)';
        });
    });

    // ===== Smooth scroll =====
    document.querySelectorAll('a[href^="#"]').forEach(function (anchor) {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({ behavior: 'smooth' });
            }
        });
    });

    console.log('🚀 IntelliPlan Dashboard carregado com sucesso!');
});
