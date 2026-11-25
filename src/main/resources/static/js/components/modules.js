document.addEventListener("DOMContentLoaded", () => {
  if (window.lucide) window.lucide.createIcons();

  document.querySelectorAll("[data-password-toggle]").forEach((button) => {
    button.addEventListener("click", () => {
      const input = document.querySelector(button.dataset.passwordTarget);
      if (!input) return;
      const showing = input.type === "text";
      input.type = showing ? "password" : "text";
      button.setAttribute("aria-pressed", String(!showing));
      button.setAttribute("aria-label", showing ? "Mostrar senha" : "Ocultar senha");
      const icon = button.querySelector("[data-lucide]");
      if (icon) {
        icon.setAttribute("data-lucide", showing ? "eye" : "eye-off");
        if (window.lucide) window.lucide.createIcons();
      }
    });
  });

  document.querySelectorAll("[data-confirm]").forEach((button) => {
    button.addEventListener("click", (event) => {
      const message = button.dataset.confirm || "Deseja realmente continuar?";
      if (!window.confirm(message)) event.preventDefault();
    });
  });

  document.querySelectorAll("form[data-validate]").forEach((form) => {
    form.addEventListener("submit", (event) => {
      const invalid = Array.from(form.querySelectorAll("[required]")).filter((field) => {
        return field.value === null || field.value.trim() === "";
      });
      if (invalid.length > 0) {
        event.preventDefault();
        invalid[0].focus();
      }
    });
  });

  document.querySelectorAll("[data-location-state]").forEach((stateSelect) => {
    const form = stateSelect.closest("form");
    const municipalitySelect = form?.querySelector("[data-location-municipality]");
    const codeInput = form?.querySelector("[data-location-code]");
    const message = form?.querySelector("[data-location-message]");
    if (!form || !municipalitySelect || !codeInput || !message) return;

    const setMessage = (text) => {
      message.textContent = text;
    };

    const loadMunicipalities = async (uf, selectedName = "", selectedCode = "") => {
      municipalitySelect.replaceChildren(new Option("Selecione um município", ""));
      municipalitySelect.disabled = true;
      codeInput.value = "";
      if (!uf) {
        setMessage("Selecione um estado para carregar os municípios.");
        return;
      }

      setMessage("Carregando municípios do IBGE...");
      try {
        const response = await fetch(`/api/v1/localidades/estados/${encodeURIComponent(uf)}/municipios`);
        if (!response.ok) throw new Error("Falha ao carregar municípios.");
        const municipios = await response.json();
        municipios.forEach((municipio) => {
          const option = new Option(municipio.nome, municipio.nome);
          option.dataset.codigoIbge = municipio.codigoIbge;
          municipalitySelect.add(option);
        });
        const selecionado = Array.from(municipalitySelect.options).find((option) => {
          return option.dataset.codigoIbge === selectedCode && option.value === selectedName;
        });
        if (selecionado) {
          selecionado.selected = true;
          codeInput.value = selecionado.dataset.codigoIbge;
        }
        municipalitySelect.disabled = false;
        setMessage(municipios.length > 0
          ? "Municípios carregados pelo serviço oficial do IBGE."
          : "Nenhum município foi retornado para este estado.");
      } catch {
        setMessage("Não foi possível carregar os municípios agora. Tente novamente.");
      }
    };

    const selectedUf = stateSelect.dataset.selected || stateSelect.value;
    const selectedName = municipalitySelect.dataset.selectedName || municipalitySelect.value;
    const selectedCode = municipalitySelect.dataset.selectedCode || codeInput.value;

    stateSelect.addEventListener("change", () => {
      municipalitySelect.dataset.selectedName = "";
      municipalitySelect.dataset.selectedCode = "";
      loadMunicipalities(stateSelect.value);
    });
    municipalitySelect.addEventListener("change", () => {
      const option = municipalitySelect.selectedOptions[0];
      codeInput.value = option?.dataset.codigoIbge || "";
    });

    fetch("/api/v1/localidades/estados")
      .then((response) => {
        if (!response.ok) throw new Error("Falha ao carregar estados.");
        return response.json();
      })
      .then((estados) => {
        estados.forEach((estado) => stateSelect.add(new Option(estado.nome, estado.sigla)));
        stateSelect.value = selectedUf;
        if (stateSelect.value) {
          loadMunicipalities(stateSelect.value, selectedName, selectedCode);
        } else {
          setMessage("Selecione um estado para carregar os municípios.");
        }
      })
      .catch(() => {
        setMessage("Não foi possível carregar os estados agora. Tente novamente.");
      });
  });

  document.querySelectorAll("[data-search-target]").forEach((input) => {
    input.addEventListener("input", () => {
      const target = document.querySelector(input.dataset.searchTarget);
      if (!target) return;
      const term = input.value.trim().toLocaleLowerCase("pt-BR");
      target.querySelectorAll("[data-searchable]").forEach((item) => {
        item.hidden = term !== "" && !item.dataset.searchable.toLocaleLowerCase("pt-BR").includes(term);
      });
    });
  });

  window.setTimeout(() => {
    document.querySelectorAll(".feedback").forEach((feedback) => feedback.remove());
  }, 7000);

  const profileToggles = Array.from(document.querySelectorAll("[data-profile-toggle]"));
  const closeProfileMenus = (restoreFocus) => {
    const openMenus = document.querySelectorAll("[data-profile-menu].open");
    openMenus.forEach((menu) => {
      menu.classList.remove("open");
      menu.setAttribute("aria-hidden", "true");
      if (restoreFocus) {
        const wrapper = menu.closest(".profile-menu-wrapper");
        const toggle = wrapper ? wrapper.querySelector("[data-profile-toggle]") : null;
        if (toggle) toggle.focus();
      }
    });
    profileToggles.forEach((toggle) => toggle.setAttribute("aria-expanded", "false"));
  };

  profileToggles.forEach((toggle) => {
    toggle.addEventListener("click", (event) => {
      event.stopPropagation();
      const wrapper = toggle.closest(".profile-menu-wrapper");
      const menu = wrapper ? wrapper.querySelector("[data-profile-menu]") : null;
      if (!menu) return;
      const opening = !menu.classList.contains("open");
      closeProfileMenus(false);
      if (opening) {
        menu.classList.add("open");
        menu.setAttribute("aria-hidden", "false");
        toggle.setAttribute("aria-expanded", "true");
      }
    });
  });

  document.addEventListener("click", () => {
    closeProfileMenus(false);
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") closeProfileMenus(true);
  });
});
