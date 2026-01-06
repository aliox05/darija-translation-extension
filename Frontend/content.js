chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.type === "showTranslation") {
    const box = document.createElement("div");
    box.textContent = message.text;
    box.style.position = "fixed";
    box.style.bottom = "20px";
    box.style.right = "20px";
    box.style.background = "#fff8e1";
    box.style.border = "2px solid #d4af37";
    box.style.borderRadius = "8px";
    box.style.padding = "12px";
    box.style.fontFamily = "'Scheherazade New', serif";
    box.style.fontSize = "16px";
    box.style.color = "#4e342e";
    box.style.boxShadow = "0 4px 8px rgba(0,0,0,0.2)";
    box.style.zIndex = "9999";
    box.style.opacity = "0";
    box.style.transition = "opacity 0.5s ease";

    document.body.appendChild(box);
    setTimeout(() => { box.style.opacity = "1"; }, 100);

    setTimeout(() => {
      box.style.opacity = "0";
      setTimeout(() => box.remove(), 500);
    }, 6000);
  }
});
