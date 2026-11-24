document.addEventListener("DOMContentLoaded", () => {
  const form = document.querySelector("#login-preview");
  if (form) {
    form.addEventListener("submit", (event) => event.preventDefault());
  }
});
