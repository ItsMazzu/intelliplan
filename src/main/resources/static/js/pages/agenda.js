document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".calendar-day[data-date]").forEach((day) => {
    day.addEventListener("click", () => {
      const date = day.dataset.date;
      const link = document.querySelector("[data-new-appointment]");
      if (link && date) {
        window.location.href = link.dataset.newAppointment + "?data=" + date;
      }
    });
  });
});
