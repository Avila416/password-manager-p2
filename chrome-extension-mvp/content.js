function pickFirst(selectors) {
  for (const selector of selectors) {
    const node = document.querySelector(selector);
    if (node) return node;
  }
  return null;
}

function setInputValue(input, value) {
  if (!input) return;
  input.focus();
  input.value = value;
  input.dispatchEvent(new Event("input", { bubbles: true }));
  input.dispatchEvent(new Event("change", { bubbles: true }));
}

chrome.runtime.onMessage.addListener((message) => {
  if (!message || message.type !== "PM_FILL") return;
  const usernameSelectors = [
    'input[autocomplete="username"]',
    'input[type="email"]',
    'input[name*="user" i]',
    'input[id*="user" i]',
    'input[name*="email" i]',
    'input[id*="email" i]',
    'input[type="text"]'
  ];
  const passwordSelectors = [
    'input[autocomplete="current-password"]',
    'input[type="password"]',
    'input[name*="pass" i]',
    'input[id*="pass" i]'
  ];

  const userInput = pickFirst(usernameSelectors);
  const passInput = pickFirst(passwordSelectors);

  setInputValue(userInput, message.payload.username || "");
  setInputValue(passInput, message.payload.password || "");
});
