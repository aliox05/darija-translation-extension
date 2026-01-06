// sidebar.js
document.addEventListener("DOMContentLoaded", () => {
  // Load last input and translation when sidebar opens
  chrome.storage.local.get(["lastInput", "lastTranslation"], (data) => {
    if (data.lastInput) {
      document.getElementById("inputText").value = data.lastInput;
    }
    if (data.lastTranslation) {
      document.getElementById("outputText").innerText = data.lastTranslation;
    }
  });

  // ✅ Update translate button state on load
  updateTranslateButtonState();
});

// Utility: enable/disable translate button based on JWT presence
async function updateTranslateButtonState() {
  const { jwt } = await chrome.storage.local.get("jwt");
  const translateBtn = document.getElementById("translateBtn");
  if (jwt) {
    translateBtn.disabled = false;
    translateBtn.style.opacity = "1";
  } else {
    translateBtn.disabled = true;
    translateBtn.style.opacity = "0.5";
  }
}

// Listen for updates from background.js
chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.type === "translationUpdated") {
    document.getElementById("inputText").value = message.input;
    document.getElementById("outputText").innerText = message.text;
  }
});

// Handle signup
document.getElementById("signupBtn").addEventListener("click", async () => {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;

  try {
    const response = await fetch("http://localhost:8082/English-darija-tr/rest/signup", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password, role: "user" })
    });

    const text = await response.text(); 
    let data; 
    try { 
      data = JSON.parse(text); 
    } catch (e) { 
      throw new Error("Server did not return valid JSON: " + text);
    }
    if (!response.ok) {
      document.getElementById("outputText").innerText = "⚠️ Signup error: " + (data.error || response.status);
      return;
    }

    document.getElementById("outputText").innerText = "✅ User registered successfully!";
  } catch (error) {
    document.getElementById("outputText").innerText = "⚠️ Connection error: " + error.message;
  }
});

// Handle login
document.getElementById("loginBtn").addEventListener("click", async () => {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;

  try {
    const response = await fetch("http://localhost:8082/English-darija-tr/rest/auth", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password })
    });

    if (!response.ok) throw new Error("Login failed");

    const data = await response.json();
    await chrome.storage.local.set({ jwt: data.token });

    document.getElementById("outputText").innerText = "✅ Logged in successfully!";
    updateTranslateButtonState(); // ✅ enable translate
  } catch (error) {
    document.getElementById("outputText").innerText = "⚠️ Login error: " + error.message;
  }
});

// Handle translation
document.getElementById("translateBtn").addEventListener("click", async () => {
  const inputText = document.getElementById("inputText").value;

  try {
    const { jwt } = await chrome.storage.local.get("jwt");
    if (!jwt) throw new Error("No JWT found, please log in first");

    const response = await fetch("http://localhost:8082/English-darija-tr/rest/translate", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + jwt
      },
      body: JSON.stringify({ text: inputText })
    });

    if (!response.ok) throw new Error("Server returned " + response.status);

    const data = await response.json();
    const message = data.error
      ? "⚠️ Error: " + data.error
      : "Darija Translation:\n" + data.translation;

    document.getElementById("outputText").innerText = message;
    chrome.storage.local.set({ lastInput: inputText, lastTranslation: message });

  } catch (error) {
    const errorMsg = "⚠️ Connection error: " + error.message;
    document.getElementById("outputText").innerText = errorMsg;
    chrome.storage.local.set({ lastInput: inputText, lastTranslation: errorMsg });
  }
});

// Handle logout
document.getElementById("logoutBtn").addEventListener("click", async () => {
  try {
    await chrome.storage.local.remove("jwt");

    document.getElementById("outputText").innerText = "✅ Logged out successfully!";
    document.getElementById("inputText").value = "";

    updateTranslateButtonState(); // ✅ disable translate
  } catch (error) {
    document.getElementById("outputText").innerText = "⚠️ Logout error: " + error.message;
  }
});
