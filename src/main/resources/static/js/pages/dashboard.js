document.addEventListener("DOMContentLoaded", () => {
  if (window.lucide) window.lucide.createIcons();

  document.querySelectorAll("[data-profile-toggle]").forEach((toggle) => {
  toggle.addEventListener("click", (event) => {
    event.stopPropagation();
    const wrapper = toggle.closest(".profile-menu-wrapper");
    const menu = wrapper ? wrapper.querySelector("[data-profile-menu]") : null;
    if (!menu) return;
    const opening = !menu.classList.contains("open");
    document.querySelectorAll("[data-profile-menu].open").forEach((openMenu) => openMenu.classList.remove("open"));
    document.querySelectorAll("[data-profile-toggle]").forEach((item) => item.setAttribute("aria-expanded", "false"));
    if (opening) {
      menu.classList.add("open");
      toggle.setAttribute("aria-expanded", "true");
    }
  });
  });

  document.addEventListener("click", () => {
  document.querySelectorAll("[data-profile-menu].open").forEach((menu) => menu.classList.remove("open"));
  document.querySelectorAll("[data-profile-toggle]").forEach((toggle) => toggle.setAttribute("aria-expanded", "false"));
  });
});
