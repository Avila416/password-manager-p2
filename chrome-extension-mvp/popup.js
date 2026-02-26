const API_BASE = "http://localhost:8083/api/vault";

async function getActiveTab() {
  const tabs = await chrome.tabs.query({ active: true, currentWindow: true });
  return tabs && tabs.length ? tabs[0] : null;
}

function setStatus(text, isError = false) {
  const el = document.getElementById("status");
  el.textContent = text;
  el.className = isError ? "error" : "muted";
}

function normalizeHost(url) {
  try {
    const host = new URL(url).hostname || "";
    return host.startsWith("www.") ? host.slice(4) : host;
  } catch (_) {
    return "";
  }
}

function renderEntries(entries, tabId) {
  const wrap = document.getElementById("results");
  wrap.innerHTML = "";

  entries.forEach((entry) => {
    const card = document.createElement("div");
    card.className = "item";

    const row = document.createElement("div");
    row.className = "row";

    const label = document.createElement("div");
    const title = entry.title || entry.website || "Saved Credential";
    label.innerHTML = `<strong>${title}</strong><br><span>${entry.username || ""}</span>`;

    const button = document.createElement("button");
    button.textContent = "Fill";
    button.addEventListener("click", async () => {
      await chrome.tabs.sendMessage(tabId, {
        type: "PM_FILL",
        payload: {
          username: entry.username || "",
          password: entry.password || entry.encryptedPassword || ""
        }
      });
      window.close();
    });

    row.appendChild(label);
    row.appendChild(button);
    card.appendChild(row);
    wrap.appendChild(card);
  });
}

async function load() {
  const tab = await getActiveTab();
  if (!tab || !tab.url || !tab.id) {
    setStatus("No active tab found.", true);
    return;
  }

  const domain = normalizeHost(tab.url);
  document.getElementById("domain").textContent = domain ? `Domain: ${domain}` : "Domain unavailable";
  if (!domain) {
    setStatus("Open a valid website tab.", true);
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/by-domain?domain=${encodeURIComponent(domain)}`);
    if (!res.ok) {
      setStatus(`API error ${res.status}`, true);
      return;
    }
    const entries = await res.json();
    if (!Array.isArray(entries) || entries.length === 0) {
      setStatus("No matching credentials found.");
      return;
    }
    setStatus(`Found ${entries.length} match(es).`);
    renderEntries(entries, tab.id);
  } catch (err) {
    setStatus("Cannot reach backend. Start module backend first.", true);
  }
}

load();
