document.addEventListener("DOMContentLoaded", () => {
  if (window.lucide) window.lucide.createIcons();

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
