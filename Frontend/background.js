// background.js
chrome.runtime.onInstalled.addListener(() => {
  chrome.contextMenus.create({
    id: "translateToDarija",
    title: "Translate to Darija",
    contexts: ["selection"]
  });

  chrome.sidePanel.setPanelBehavior({ openPanelOnActionClick: true });
});

chrome.contextMenus.onClicked.addListener(async (info, tab) => {
  if (!info.selectionText) return;

  if (info.menuItemId === "translateToDarija") {
    try {
      const { jwt } = await chrome.storage.local.get("jwt");
      if (!jwt) throw new Error("No JWT found, please log in first");

      const response = await fetch("http://localhost:8082/English-darija-tr/rest/translate", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + jwt
        },
        body: JSON.stringify({ text: info.selectionText })
      });

      if (!response.ok) throw new Error("Server returned " + response.status);

      const data = await response.json();
      const message = data.error
        ? "⚠️ Error: " + data.error
        : "Darija Translation:\n" + data.translation;

      chrome.storage.local.set({ lastInput: info.selectionText, lastTranslation: message });

      await chrome.sidePanel.setOptions({ tabId: tab.id, path: "sidebar.html", enabled: true });
      chrome.runtime.sendMessage({ type: "translationUpdated", input: info.selectionText, text: message });

    } catch (error) {
      const errorMsg = "⚠️ Connection error: " + error.message;
      chrome.storage.local.set({ lastTranslation: errorMsg });
      chrome.runtime.sendMessage({ type: "translationUpdated", input: info.selectionText, text: errorMsg });
    }
  }
});
