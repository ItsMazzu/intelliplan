document.addEventListener("DOMContentLoaded", () => {
  if (window.lucide) window.lucide.createIcons();

  // Interação visual de demonstração para os checkboxes sem formulário.
  document.querySelectorAll(".task-check").forEach((button) => {
    button.addEventListener("click", () => {
      button.classList.toggle("checked");
      if (button.classList.contains("checked") && !button.querySelector("svg")) {
        button.innerHTML = '<i data-lucide="check" class="h-3 w-3"></i>';
        if (window.lucide) window.lucide.createIcons();
      } else if (!button.classList.contains("checked")) {
        button.innerHTML = "";
      }
    });
  });
});
