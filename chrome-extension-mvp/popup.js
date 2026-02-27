const API_BASES = [
  "http://localhost:8084/api/vault",
  "http://localhost:8083/api/vault"
];

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

async function sendFillMessage(tabId, payload) {
  try {
    return await chrome.tabs.sendMessage(tabId, { type: "PM_FILL", payload });
  } catch (err) {
    const msg = String(err && err.message ? err.message : err);
    if (!msg.includes("Receiving end does not exist")) {
      throw err;
    }

    // Content script may not be attached yet; inject and retry once.
    await chrome.scripting.executeScript({
      target: { tabId },
      files: ["content.js"]
    });

    return await chrome.tabs.sendMessage(tabId, { type: "PM_FILL", payload });
  }
}

function isEncryptedValue(value) {
  return typeof value === "string" && value.startsWith("v1:");
}

async function verifyAndGetPassword(entryId, masterPassword) {
  for (const base of API_BASES) {
    try {
      const response = await fetch(`${base}/${encodeURIComponent(entryId)}/verify`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ masterPassword })
      });
      if (!response.ok) {
        continue;
      }
      const data = await response.json();
      return data && typeof data.password === "string" ? data.password : "";
    } catch (_) {
      // Try next base.
    }
  }
  return "";
}

async function resolvePassword(entry) {
  const stored = entry?.password || entry?.encryptedPassword || "";
  if (stored && !isEncryptedValue(stored)) {
    return stored;
  }

  const masterInput = document.getElementById("masterPassword");
  const masterPassword = masterInput && typeof masterInput.value === "string"
    ? masterInput.value.trim()
    : "";

  if (!masterPassword) {
    setStatus("Enter master password to fill encrypted entries.", true);
    return "";
  }

  const revealed = await verifyAndGetPassword(entry.id, masterPassword);
  if (!revealed) {
    setStatus("Master password verification failed.", true);
  }
  return revealed;
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
      try {
        const password = await resolvePassword(entry);
        if (!password) {
          return;
        }

        const response = await sendFillMessage(tabId, {
          username: entry.username || "",
          password
        });

        if (response && response.ok) {
          setStatus("Credentials filled.");
          window.close();
          return;
        }

        setStatus("Could not find login fields on this page.", true);
      } catch (_) {
        setStatus("Fill failed. Reload page and try again.", true);
      }
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
    let res = null;
    for (const base of API_BASES) {
      try {
        const candidate = await fetch(`${base}/by-domain?domain=${encodeURIComponent(domain)}`);
        if (candidate.ok) {
          res = candidate;
          break;
        }
      } catch (_) {
        // Try next base URL.
      }
    }

    if (!res) {
      setStatus("Cannot reach backend. Start module backend first.", true);
      return;
    }

    const entries = await res.json();
    if (!Array.isArray(entries) || entries.length === 0) {
      setStatus("No matching credentials found.");
      return;
    }

    setStatus(`Found ${entries.length} match(es).`);
    renderEntries(entries, tab.id);
  } catch (_) {
    setStatus("Cannot reach backend. Start module backend first.", true);
  }
}

load();
