(() => {
  const allowedProtocols = new Set(["http:", "https:", "mailto:"]);

  const safeHref = (value) => {
    const href = value.trim();
    if (!href || /[\u0000-\u001f\u007f]/.test(href)) return null;

    try {
      const url = new URL(href, document.baseURI);
      return allowedProtocols.has(url.protocol) ? href : null;
    } catch {
      return null;
    }
  };

  const appendInline = (parent, source, depth = 0) => {
    if (depth > 8) {
      parent.append(document.createTextNode(source));
      return;
    }

    const tokenPattern = /(`[^`\n]+`|\*\*[\s\S]+?\*\*|__[\s\S]+?__|~~[\s\S]+?~~|\[[^\]\n]+\]\([^) \n]+\)|\*[^*\n]+\*|_[^_\n]+_)/g;
    let position = 0;
    let match;

    while ((match = tokenPattern.exec(source)) !== null) {
      if (match.index > position) {
        parent.append(document.createTextNode(source.slice(position, match.index)));
      }

      const token = match[0];
      let element;

      if (token.startsWith("`")) {
        element = document.createElement("code");
        element.textContent = token.slice(1, -1);
      } else if (token.startsWith("[") && token.includes("](")) {
        const linkMatch = token.match(/^\[([^\]]+)\]\(([^)]+)\)$/);
        const href = linkMatch ? safeHref(linkMatch[2]) : null;
        if (linkMatch && href) {
          element = document.createElement("a");
          element.setAttribute("href", href);
          element.setAttribute("rel", "noopener noreferrer");
          element.setAttribute("target", "_blank");
          appendInline(element, linkMatch[1], depth + 1);
        } else {
          parent.append(document.createTextNode(token));
          position = tokenPattern.lastIndex;
          continue;
        }
      } else {
        let tagName;
        let content;
        if (token.startsWith("**") || token.startsWith("__")) {
          tagName = "strong";
          content = token.slice(2, -2);
        } else if (token.startsWith("~~")) {
          tagName = "del";
          content = token.slice(2, -2);
        } else {
          tagName = "em";
          content = token.slice(1, -1);
        }
        element = document.createElement(tagName);
        appendInline(element, content, depth + 1);
      }

      parent.append(element);
      position = tokenPattern.lastIndex;
    }

    if (position < source.length) {
      parent.append(document.createTextNode(source.slice(position)));
    }
  };

  const isUnorderedItem = (line) => /^\s*[-*+]\s+/.test(line);
  const isOrderedItem = (line) => /^\s*\d+[.)]\s+/.test(line);
  const isBlockStart = (line) => /^\s*```/.test(line)
    || /^#{1,6}\s+/.test(line)
    || isUnorderedItem(line)
    || isOrderedItem(line);

  const render = (source) => {
    const fragment = document.createDocumentFragment();
    const lines = String(source ?? "").replace(/\r\n?/g, "\n").split("\n");
    let index = 0;

    while (index < lines.length) {
      const line = lines[index];
      if (!line.trim()) {
        index += 1;
        continue;
      }

      if (/^\s*```/.test(line)) {
        const codeLines = [];
        index += 1;
        while (index < lines.length && !/^\s*```\s*$/.test(lines[index])) {
          codeLines.push(lines[index]);
          index += 1;
        }
        if (index < lines.length) index += 1;
        const pre = document.createElement("pre");
        const code = document.createElement("code");
        code.textContent = codeLines.join("\n");
        pre.append(code);
        fragment.append(pre);
        continue;
      }

      const heading = line.match(/^(#{1,6})\s+(.+?)\s*#*\s*$/);
      if (heading) {
        const element = document.createElement(`h${heading[1].length}`);
        appendInline(element, heading[2]);
        fragment.append(element);
        index += 1;
        continue;
      }

      if (isUnorderedItem(line) || isOrderedItem(line)) {
        const ordered = isOrderedItem(line);
        const list = document.createElement(ordered ? "ol" : "ul");
        while (index < lines.length && (ordered ? isOrderedItem(lines[index]) : isUnorderedItem(lines[index]))) {
          const item = document.createElement("li");
          const content = lines[index].replace(ordered ? /^\s*\d+[.)]\s+/ : /^\s*[-*+]\s+/, "");
          appendInline(item, content);
          list.append(item);
          index += 1;
        }
        fragment.append(list);
        continue;
      }

      const paragraphLines = [];
      while (index < lines.length && lines[index].trim() && !isBlockStart(lines[index])) {
        paragraphLines.push(lines[index]);
        index += 1;
      }
      const paragraph = document.createElement("p");
      paragraphLines.forEach((paragraphLine, lineIndex) => {
        if (lineIndex > 0) paragraph.append(document.createElement("br"));
        appendInline(paragraph, paragraphLine);
      });
      fragment.append(paragraph);
    }

    return fragment;
  };

  const renderInto = (element, source) => {
    element.replaceChildren(render(source));
  };

  const initialize = () => {
    document.querySelectorAll("[data-markdown]").forEach((element) => {
      renderInto(element, element.textContent);
    });

    document.querySelectorAll("[data-markdown-input]").forEach((input) => {
      const preview = document.createElement("div");
      preview.className = "markdown-preview-panel";

      const label = document.createElement("p");
      label.className = "markdown-preview-label";
      label.textContent = "Pré-visualização";

      const content = document.createElement("div");
      content.className = "markdown-content markdown-preview";
      content.setAttribute("aria-live", "polite");
      preview.append(label, content);
      input.insertAdjacentElement("afterend", preview);

      const updatePreview = () => renderInto(content, input.value);
      input.addEventListener("input", updatePreview);
      updatePreview();
    });
  };

  window.IntelliPlanMarkdown = Object.freeze({ render, renderInto });
  document.addEventListener("DOMContentLoaded", initialize);
})();
