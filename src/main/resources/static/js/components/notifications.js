document.addEventListener("DOMContentLoaded", () => {
  const toggle = document.querySelector("[data-notification-toggle]");
  const panel = document.querySelector("[data-notification-panel]");
  const badge = document.querySelector("[data-notification-badge]");
  const list = document.querySelector("[data-notification-list]");
  const empty = document.querySelector("[data-notification-empty]");
  const summary = document.querySelector("[data-notification-summary]");
  const error = document.querySelector("[data-notification-error]");
  const feedback = document.querySelector("[data-notification-feedback]");
  const markAll = document.querySelector("[data-notification-read-all]");
  const csrf = document.querySelector("[data-notification-csrf]");
  const component = toggle ? toggle.closest(".notification-center") : null;

  if (!toggle || !panel || !badge || !list || !csrf || !component) return;
  const apiBase = component.dataset.apiBase;
  const contextPath = apiBase.substring(0, apiBase.indexOf("/api/v1/notificacoes"));

  const closePanel = () => {
    panel.hidden = true;
    toggle.setAttribute("aria-expanded", "false");
  };

  const showError = (message) => {
    error.textContent = message;
    error.hidden = false;
  };

  const clearError = () => {
    error.textContent = "";
    error.hidden = true;
  };

  const request = async (path, method) => {
    const headers = { Accept: "application/json" };
    if (method !== "GET") headers[csrf.dataset.header] = csrf.value;
    const response = await fetch(path, {
      method,
      headers,
      credentials: "same-origin"
    });
    if (!response.ok) throw new Error("Não foi possível atualizar as notificações.");
    if (response.status === 204) return null;
    return response.json();
  };

  const render = (data) => {
    const naoLidas = Number(data.naoLidas) || 0;
    badge.hidden = naoLidas === 0;
    badge.textContent = naoLidas > 99 ? "99+" : String(naoLidas);
    badge.setAttribute("aria-label", naoLidas + (naoLidas === 1 ? " notificação não lida" : " notificações não lidas"));
    summary.textContent = naoLidas === 0
      ? "Você está em dia."
      : naoLidas + (naoLidas === 1 ? " não lida" : " não lidas");
    markAll.hidden = naoLidas === 0;

    list.replaceChildren();
    const notifications = Array.isArray(data.notificacoes) ? data.notificacoes : [];
    empty.hidden = notifications.length !== 0;
    notifications.forEach((notification) => {
      const item = document.createElement("li");
      item.className = "notification-item" + (notification.lidaEm ? " is-read" : " is-unread");

      const titleRow = document.createElement("div");
      titleRow.className = "notification-title-row";
      const title = document.createElement("a");
      title.className = "notification-title";
      title.textContent = notification.titulo;
      const destinationPath = contextPath + (notification.urlDestino || "/");
      const destination = new URL(destinationPath, window.location.origin);
      title.href = destination.origin === window.location.origin ? destination.pathname + destination.search : "/";
      title.dataset.notificationLink = String(notification.id);
      titleRow.append(title);

      if (!notification.lidaEm) {
        const readButton = document.createElement("button");
        readButton.type = "button";
        readButton.className = "notification-mark-read";
        readButton.textContent = "Marcar como lida";
        readButton.setAttribute("aria-label", "Marcar como lida: " + notification.titulo);
        readButton.dataset.notificationRead = String(notification.id);
        titleRow.append(readButton);
      }

      const description = document.createElement("p");
      description.className = "notification-description";
      description.textContent = notification.descricao;
      const timestamp = document.createElement("time");
      timestamp.className = "notification-time";
      timestamp.dateTime = notification.criadaEm;
      const created = new Date(notification.criadaEm);
      timestamp.textContent = Number.isNaN(created.getTime())
        ? notification.criadaEm
        : new Intl.DateTimeFormat("pt-BR", { dateStyle: "medium", timeStyle: "short" }).format(created);

      item.append(titleRow, description, timestamp);
      list.append(item);
    });
  };

  const refresh = async (sync) => {
    clearError();
    const path = sync ? apiBase + "/sincronizar" : apiBase;
    const data = await request(path, sync ? "POST" : "GET");
    render(data);
  };

  const setFeedback = (message) => {
    feedback.textContent = message;
    feedback.hidden = message === "";
  };

  toggle.addEventListener("click", async (event) => {
    event.stopPropagation();
    const opening = panel.hidden;
    if (!opening) {
      closePanel();
      return;
    }

    panel.hidden = false;
    toggle.setAttribute("aria-expanded", "true");
    setFeedback("");
    try {
      await refresh(true);
    } catch (requestError) {
      showError(requestError.message);
    }
  });

  panel.addEventListener("click", async (event) => {
    const target = event.target;
    const readButton = target.closest("[data-notification-read]");
    const notificationLink = target.closest("[data-notification-link]");
    if (!readButton && !notificationLink) return;

    const id = readButton
      ? readButton.dataset.notificationRead
      : notificationLink.dataset.notificationLink;
    if (notificationLink && !readButton) {
      event.preventDefault();
    }

    try {
      clearError();
      await request(apiBase + "/" + encodeURIComponent(id) + "/lida", "PATCH");
      await refresh(false);
      setFeedback("Notificação marcada como lida.");
      if (notificationLink && !readButton) {
        window.location.assign(notificationLink.href);
      }
    } catch (requestError) {
      showError(requestError.message);
    }
  });

  markAll.addEventListener("click", async () => {
    try {
      clearError();
      await request(apiBase + "/lidas", "PATCH");
      await refresh(false);
      setFeedback("Todas as notificações foram marcadas como lidas.");
    } catch (requestError) {
      showError(requestError.message);
    }
  });

  document.addEventListener("click", (event) => {
    if (!panel.hidden && !panel.contains(event.target) && !toggle.contains(event.target)) closePanel();
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && !panel.hidden) {
      closePanel();
      toggle.focus();
    }
  });

  refresh(false).catch((requestError) => showError(requestError.message));
});
