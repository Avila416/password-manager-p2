function isVisibleInput(node) {
  if (!node || node.disabled) return false;
  if (node.type && node.type.toLowerCase() === "hidden") return false;

  const style = window.getComputedStyle(node);
  if (style.display === "none" || style.visibility === "hidden") return false;

  return node.offsetParent !== null || style.position === "fixed";
}

function pickFirst(selectors) {
  for (const selector of selectors) {
    const matches = Array.from(document.querySelectorAll(selector));
    const node = matches.find(isVisibleInput) || matches[0];
    if (node) return node;
  }
  return null;
}

function setInputValue(input, value) {
  if (!input) return false;

  const descriptor = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value");
  if (descriptor && descriptor.set) {
    descriptor.set.call(input, value);
  } else {
    input.value = value;
  }

  input.focus();
  input.dispatchEvent(new Event("input", { bubbles: true }));
  input.dispatchEvent(new Event("change", { bubbles: true }));
  return true;
}

chrome.runtime.onMessage.addListener((message, _sender, sendResponse) => {
  if (!message || message.type !== "PM_FILL") return;

  const usernameSelectors = [
    '#login_field',
    'input[name="login"]',
    'input[autocomplete="username"]',
    'input[type="email"]',
    'input[name*="user" i]',
    'input[id*="user" i]',
    'input[name*="email" i]',
    'input[id*="email" i]',
    'input[type="text"]'
  ];

  const passwordSelectors = [
    '#password',
    'input[name="password"]',
    'input[autocomplete="current-password"]',
    'input[name*="pass" i]',
    'input[id*="pass" i]',
    'input[type="password"]'
  ];

  const userInput = pickFirst(usernameSelectors);
  const passInput = pickFirst(passwordSelectors);

  const userOk = setInputValue(userInput, message.payload?.username || "");
  const passOk = setInputValue(passInput, message.payload?.password || "");

  sendResponse({ ok: userOk || passOk, userOk, passOk });
});
