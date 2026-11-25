document.addEventListener("DOMContentLoaded", () => {
  const dateInput = document.querySelector("[data-habit-date]");
  if (dateInput && !dateInput.value) {
    const today = new Date();
    const localDate = new Date(today.getTime() - today.getTimezoneOffset() * 60000)
      .toISOString()
      .slice(0, 10);
    dateInput.value = localDate;
  }
});
