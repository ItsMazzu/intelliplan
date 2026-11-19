/**
 * IntelliPlan - Dashboard JavaScript
 * Interatividade e funcionalidades do frontend
 */

document.addEventListener('DOMContentLoaded', function () {

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

    // ===== Estrelas Favorito =====
    document.querySelectorAll('.star-button').forEach(function (star) {
    star.addEventListener('click', function () {
        const active = this.classList.toggle('active');
        const icon = this.querySelector('[data-lucide]');

        this.setAttribute(
            'aria-label',
            active ? 'Remover dos favoritos' : 'Favoritar'
        );

        if (icon) {
            icon.setAttribute('fill', active ? 'currentColor' : 'none');
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
