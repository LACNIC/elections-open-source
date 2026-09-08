(() => {
  const viewer = document.getElementById("pdf-viewer");
  if (!viewer) {
    return;
  }

  if (typeof pdfjsLib === "undefined") {
    console.error("PDF.js is not available.");
    return;
  }

  const url = viewer.getAttribute("data-pdf-url");
  const workerSrc = viewer.getAttribute("data-pdf-worker");

  if (!url) {
    console.error("PDF URL missing on viewer.");
    return;
  }

  if (workerSrc) {
    pdfjsLib.GlobalWorkerOptions.workerSrc = workerSrc;
  }

  const canvas = document.getElementById("pdf-canvas");
  if (!canvas) {
    console.error("PDF canvas element not found.");
    return;
  }

  const ctx = canvas.getContext("2d");
  const pageCountEl = document.getElementById("pdf-page-count");
  const pageNumInput = document.getElementById("pdf-page-num");

  let pdfDoc = null;
  let pageNum = 1;
  let pageRendering = false;
  let pageNumPending = null;
  let scale = 1.2;
  let fitNext = true;

  const canvasWrapper = viewer.querySelector(".pdf-canvas-wrapper");

  const getFitScale = (page) => {
    if (!canvasWrapper) {
      return scale;
    }
    const viewport = page.getViewport({ scale: 1 });
    const padding = 32;
    const targetWidth = Math.max(320, canvasWrapper.clientWidth - padding);
    return Math.max(0.5, Math.min(2, targetWidth / viewport.width));
  };

  const renderPage = (num) => {
    pageRendering = true;
    pdfDoc
      .getPage(num)
      .then((page) => {
        if (fitNext) {
          scale = getFitScale(page);
          fitNext = false;
        }
        const viewport = page.getViewport({ scale });
        canvas.width = viewport.width;
        canvas.height = viewport.height;
        return page.render({ canvasContext: ctx, viewport }).promise;
      })
      .then(() => {
        pageRendering = false;
        if (pageNumPending !== null) {
          const next = pageNumPending;
          pageNumPending = null;
          renderPage(next);
        }
        if (pageNumInput) {
          pageNumInput.value = pageNum;
        }
      })
      .catch((error) => {
        console.error(`Error rendering page ${num}:`, error);
      });
  };

  const queueRenderPage = (num) => {
    if (pageRendering) {
      pageNumPending = num;
    } else {
      renderPage(num);
    }
  };

  const prevButton = document.getElementById("pdf-prev");
  if (prevButton) {
    prevButton.addEventListener("click", () => {
      if (pageNum <= 1) {
        return;
      }
      pageNum -= 1;
      queueRenderPage(pageNum);
    });
  }

  const nextButton = document.getElementById("pdf-next");
  if (nextButton) {
    nextButton.addEventListener("click", () => {
      if (!pdfDoc || pageNum >= pdfDoc.numPages) {
        return;
      }
      pageNum += 1;
      queueRenderPage(pageNum);
    });
  }

  const zoomInButton = document.getElementById("pdf-zoomin");
  if (zoomInButton) {
    zoomInButton.addEventListener("click", () => {
      scale += 0.2;
      queueRenderPage(pageNum);
    });
  }

  const zoomOutButton = document.getElementById("pdf-zoomout");
  if (zoomOutButton) {
    zoomOutButton.addEventListener("click", () => {
      if (scale <= 0.4) {
        return;
      }
      scale -= 0.2;
      queueRenderPage(pageNum);
    });
  }

  const zoomFitButton = document.getElementById("pdf-zoomfit");
  if (zoomFitButton) {
    zoomFitButton.addEventListener("click", () => {
      fitNext = true;
      queueRenderPage(pageNum);
    });
  }

  if (pageNumInput) {
    pageNumInput.addEventListener("change", (event) => {
      const value = Number(event.target.value);
      if (!pdfDoc) {
        return;
      }
      if (value >= 1 && value <= pdfDoc.numPages) {
        pageNum = value;
        queueRenderPage(pageNum);
      } else {
        event.target.value = pageNum;
      }
    });
  }

  let resizeTimer;
  window.addEventListener("resize", () => {
    if (!pdfDoc) {
      return;
    }
    window.clearTimeout(resizeTimer);
    resizeTimer = window.setTimeout(() => {
      fitNext = true;
      queueRenderPage(pageNum);
    }, 150);
  });

  pdfjsLib
    .getDocument(url)
    .promise.then((doc) => {
      pdfDoc = doc;
      if (pageCountEl) {
        pageCountEl.textContent = `/ ${pdfDoc.numPages}`;
      }
      renderPage(pageNum);
    })
    .catch((error) => {
      console.error("Failed to load PDF:", error);
      alert("Error loading PDF document.");
    });
})();
