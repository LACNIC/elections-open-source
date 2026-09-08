(() => {
  const initViewer = (viewer) => {
    const url = viewer.getAttribute("data-pdf-url");
    if (!url) {
      return;
    }

    const canvas = viewer.querySelector(".pdf-canvas");
    const ctx = canvas ? canvas.getContext("2d") : null;
    const pageCountEl = viewer.querySelector(".pdf-page-count");
    const pageNumInput = viewer.querySelector(".pdf-page-num");
    const canvasWrapper = viewer.querySelector(".pdf-canvas-wrapper");

    if (!canvas || !ctx) {
      console.error("PDF canvas element not found for viewer.");
      return;
    }

    let pdfDoc = null;
    let pageNum = 1;
    let pageRendering = false;
    let pageNumPending = null;
    let scale = 1;
    let fitNext = true;

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

    const prevButton = viewer.querySelector(".pdf-prev");
    if (prevButton) {
      prevButton.addEventListener("click", () => {
        if (pageNum <= 1) {
          return;
        }
        pageNum -= 1;
        queueRenderPage(pageNum);
      });
    }

    const nextButton = viewer.querySelector(".pdf-next");
    if (nextButton) {
      nextButton.addEventListener("click", () => {
        if (!pdfDoc || pageNum >= pdfDoc.numPages) {
          return;
        }
        pageNum += 1;
        queueRenderPage(pageNum);
      });
    }

    const zoomInButton = viewer.querySelector(".pdf-zoomin");
    if (zoomInButton) {
      zoomInButton.addEventListener("click", () => {
        scale += 0.2;
        queueRenderPage(pageNum);
      });
    }

    const zoomOutButton = viewer.querySelector(".pdf-zoomout");
    if (zoomOutButton) {
      zoomOutButton.addEventListener("click", () => {
        if (scale <= 0.4) {
          return;
        }
        scale -= 0.2;
        queueRenderPage(pageNum);
      });
    }

    const zoomFitButton = viewer.querySelector(".pdf-zoomfit");
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
        if (fitNext) {
          queueRenderPage(pageNum);
        }
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
  };

  const initAllViewers = () => {
    const viewers = document.querySelectorAll(".pdf-viewer[data-pdf-url]");
    if (!viewers.length) {
      return;
    }
    if (typeof pdfjsLib === "undefined") {
      console.error("PDF.js is not available.");
      return;
    }
    const globalWorkerSrc = viewers[0].getAttribute("data-pdf-worker");
    if (globalWorkerSrc) {
      pdfjsLib.GlobalWorkerOptions.workerSrc = globalWorkerSrc;
    }
    viewers.forEach((viewer) => {
      if (viewer.dataset.pdfInitialized) {
        return;
      }
      viewer.dataset.pdfInitialized = "true";
      initViewer(viewer);
    });
  };

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initAllViewers);
  } else {
    initAllViewers();
  }

  if (window.Wicket && window.Wicket.Event && typeof window.Wicket.Event.subscribe === "function") {
    window.Wicket.Event.subscribe("/ajax/call/complete", () => {
      initAllViewers();
    });
  }
})();
