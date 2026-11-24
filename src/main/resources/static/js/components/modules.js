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
});
