const config = window.EDUSPACE_CONFIG || {};
const state = {
  lessons: [],
  activeIndex: 0,
  completed: JSON.parse(localStorage.getItem("eduspace.completed") || "[]"),
  layout: "split"
};

const $ = (selector) => document.querySelector(selector);
const els = {
  moduleList: $("#moduleList"), lessonCount: $("#lessonCount"), search: $("#lessonSearch"),
  title: $("#lessonTitle"), breadcrumb: $("#breadcrumb"), complete: $("#completeLesson"),
  videoName: $("#videoName"), pdfName: $("#pdfName"), videoWrap: $("#videoFrameWrap"),
  pdfWrap: $("#pdfFrameWrap"), videoDrive: $("#videoDriveButton"), pdfDownload: $("#pdfDownloadButton"),
  progressText: $("#progressText"), progressBar: $("#progressBar"), previous: $("#previousLesson"),
  next: $("#nextLesson"), previousTitle: $("#previousTitle"), nextTitle: $("#nextTitle"),
  splitView: $("#splitView")
};

function naturalCompare(a, b) {
  return String(a).localeCompare(String(b), undefined, { numeric: true, sensitivity: "base" });
}

function lessonNumber(text) {
  const match = String(text).match(/(?:lesson|lecture|درس|محاضرة|chapter|module|unit)?[\s._-]*(\d+)/i);
  return match ? Number(match[1]) : null;
}

function stripExtension(name) {
  return name.replace(/\.[^.]+$/, "").trim();
}

function isArabic(text) {
  return /[\u0600-\u06FF]/.test(text);
}

async function loadManifest() {
  try {
    const response = await fetch(config.manifestUrl || "data/manifest.json", { cache: "no-store" });
    if (response.ok) {
      const manifest = await response.json();
      if (manifest.lessons && manifest.lessons.length) return manifest;
    }
  } catch (_) {}
  return null;
}

async function driveList(folderId, pageToken = "", items = []) {
  const params = new URLSearchParams({
    q: `'${folderId}' in parents and trashed=false`,
    fields: "nextPageToken,files(id,name,mimeType,webViewLink)",
    pageSize: "1000",
    orderBy: "name_natural",
    supportsAllDrives: "true",
    includeItemsFromAllDrives: "true"
  });
  if (pageToken) params.set("pageToken", pageToken);
  if (config.apiKey) params.set("key", config.apiKey);
  const response = await fetch(`https://www.googleapis.com/drive/v3/files?${params}`);
  if (!response.ok) throw new Error(`Google Drive API error (${response.status})`);
  const data = await response.json();
  items.push(...(data.files || []));
  if (data.nextPageToken) await driveList(folderId, data.nextPageToken, items);
  return items;
}

async function loadDriveFolder(folderId) {
  if (!config.apiKey) throw new Error("Google Drive API key is not configured");
  const allFiles = [];
  async function walk(id, moduleName = "") {
    const items = await driveList(id);
    for (const item of items) {
      if (item.mimeType === "application/vnd.google-apps.folder") {
        await walk(item.id, item.name);
      } else {
        allFiles.push({ ...item, moduleName });
      }
    }
  }
  await walk(folderId);

  const videos = allFiles.filter((file) =>
    file.mimeType === "application/pdf" ? false :
    file.mimeType.startsWith("video/") ||
    /\.(mp4|mov|avi|webm|mkv|m4v)$/i.test(file.name) ||
    file.mimeType === "application/vnd.google-apps.video"
  );
  const pdfs = allFiles.filter((file) =>
    file.mimeType === "application/pdf" || /\.pdf$/i.test(file.name)
  );

  const keys = new Map();
  for (const file of [...videos, ...pdfs]) {
    const number = lessonNumber(file.name);
    const key = number !== null ? `number:${number}` : `name:${stripExtension(file.name).toLowerCase()}`;
    if (!keys.has(key)) keys.set(key, { number, name: stripExtension(file.name), module: file.moduleName, videos: [], pdfs: [] });
    const bucket = keys.get(key);
    if (videos.includes(file)) bucket.videos.push(file); else bucket.pdfs.push(file);
  }

  // Unmatched fallback pairing by normalized names.
  const usedVideos = new Set();
  const usedPdfs = new Set();
  const lessons = [];
  for (const [key, bucket] of [...keys.entries()].sort((a, b) => {
    const first = a[1].number ?? 999999;
    const second = b[1].number ?? 999999;
    return first === second ? naturalCompare(a[1].name, b[1].name) : first - second;
  })) {
    const video = bucket.videos.sort((a, b) => naturalCompare(a.name, b.name))[0];
    const pdf = bucket.pdfs.sort((a, b) => naturalCompare(a.name, b.name))[0];
    if (video) usedVideos.add(video.id);
    if (pdf) usedPdfs.add(pdf.id);
    if (video || pdf) lessons.push({
      id: key,
      module: bucket.module || "Course",
      title: bucket.name,
      videoId: video?.id || "",
      pdfId: pdf?.id || ""
    });
  }
  const remainingVideos = videos.filter((file) => !usedVideos.has(file.id));
  const remainingPdfs = pdfs.filter((file) => !usedPdfs.has(file.id));
  let index = lessons.length + 1;
  while (remainingVideos.length || remainingPdfs.length) {
    const video = remainingVideos.shift();
    const pdf = remainingPdfs.shift();
    lessons.push({
      id: `lesson-extra-${index}`,
      module: video?.moduleName || pdf?.moduleName || "Course",
      title: stripExtension((video || pdf).name),
      videoId: video?.id || "",
      pdfId: pdf?.id || ""
    });
    index++;
  }
  return { courseTitle: config.courseTitle || "Education Course", lessons };
}

function previewUrl(id) {
  return `https://drive.google.com/file/d/${id}/preview`;
}

function driveUrl(id) {
  return `https://drive.google.com/file/d/${id}/view`;
}

function renderSidebar(filter = "") {
  const filtered = state.lessons.filter((lesson) =>
    `${lesson.title} ${lesson.module}`.toLowerCase().includes(filter.toLowerCase())
  );
  const grouped = new Map();
  filtered.forEach((lesson) => {
    const module = lesson.module || "Course";
    if (!grouped.has(module)) grouped.set(module, []);
    grouped.get(module).push(lesson);
  });
  els.moduleList.innerHTML = "";
  for (const [module, lessons] of grouped) {
    const section = document.createElement("section");
    section.className = "module";
    section.innerHTML = `<div class="module-name">${escapeHtml(module)}</div>`;
    lessons.forEach((lesson) => {
      const realIndex = state.lessons.indexOf(lesson);
      const complete = state.completed.includes(lesson.id);
      const button = document.createElement("button");
      button.className = `lesson-item${realIndex === state.activeIndex ? " active" : ""}`;
      button.innerHTML = `
        <span class="number">${String(realIndex + 1).padStart(2, "0")}</span>
        <span class="lesson-item-content"><strong>${escapeHtml(lesson.title)}</strong><span>${lesson.videoId ? "Video" : "No video"} · ${lesson.pdfId ? "PDF" : "No PDF"}</span></span>
        <input class="lesson-check" type="checkbox" aria-label="Mark lesson complete" ${complete ? "checked" : ""}>
      `;
      button.addEventListener("click", (event) => {
        if (event.target.classList.contains("lesson-check")) return;
        state.activeIndex = realIndex;
        renderLesson();
        closeMobileSidebar();
      });
      const check = button.querySelector(".lesson-check");
      check.addEventListener("change", () => toggleComplete(lesson.id, check.checked));
      section.appendChild(button);
    });
    els.moduleList.appendChild(section);
  }
  els.lessonCount.textContent = state.lessons.length;
}

function escapeHtml(value) {
  const element = document.createElement("div");
  element.textContent = String(value);
  return element.innerHTML;
}

function renderLesson() {
  const lesson = state.lessons[state.activeIndex];
  if (!lesson) {
    els.title.textContent = "Course setup required";
    els.breadcrumb.textContent = config.courseTitle || "Education Course";
    els.videoWrap.innerHTML = `<div class="empty-frame">Add a manifest or configure the Google Drive API key.</div>`;
    els.pdfWrap.innerHTML = `<div class="empty-frame">Your paired PDF will appear here.</div>`;
    els.videoName.textContent = "Video";
    els.pdfName.textContent = "PDF notes";
    els.complete.disabled = true;
    els.previous.disabled = els.next.disabled = true;
    updateProgress();
    return;
  }

  els.title.textContent = lesson.title;
  els.breadcrumb.textContent = `${lesson.module || "Course"} / Lesson ${state.activeIndex + 1}`;
  els.videoName.textContent = lesson.videoName || lesson.title;
  els.pdfName.textContent = lesson.pdfName || `${lesson.title} — notes`;
  els.complete.disabled = false;
  els.complete.checked = state.completed.includes(lesson.id);
  els.videoWrap.innerHTML = lesson.videoId
    ? `<iframe src="${previewUrl(lesson.videoId)}" title="${escapeHtml(lesson.title)} video" allow="autoplay; fullscreen" allowfullscreen></iframe>`
    : `<div class="empty-frame">No video paired with this lesson</div>`;
  els.pdfWrap.innerHTML = lesson.pdfId
    ? `<iframe src="${previewUrl(lesson.pdfId)}" title="${escapeHtml(lesson.title)} PDF"></iframe>`
    : `<div class="empty-frame">No PDF paired with this lesson</div>`;
  els.videoDrive.href = lesson.videoId ? driveUrl(lesson.videoId) : "#";
  els.videoDrive.style.pointerEvents = lesson.videoId ? "auto" : "none";
  els.pdfDownload.href = lesson.pdfId ? `${driveUrl(lesson.pdfId)}?usp=sharing` : "#";
  els.pdfDownload.style.pointerEvents = lesson.pdfId ? "auto" : "none";

  const previous = state.lessons[state.activeIndex - 1];
  const next = state.lessons[state.activeIndex + 1];
  els.previous.disabled = !previous;
  els.next.disabled = !next;
  els.previousTitle.textContent = previous?.title || "Start";
  els.nextTitle.textContent = next?.title || "Finish";
  els.previous.style.opacity = previous ? "1" : ".45";
  els.next.style.opacity = next ? "1" : ".45";
  if (isArabic(lesson.title) || config.language === "ar") document.documentElement.dir = "rtl";
  else if (config.language !== "ar") document.documentElement.dir = "ltr";
  renderSidebar(els.search.value);
  updateProgress();
}

function toggleComplete(id, checked) {
  state.completed = checked
    ? [...new Set([...state.completed, id])]
    : state.completed.filter((item) => item !== id);
  localStorage.setItem("eduspace.completed", JSON.stringify(state.completed));
  updateProgress();
  renderSidebar(els.search.value);
}

function updateProgress() {
  const percent = state.lessons.length
    ? Math.round((state.completed.filter((id) => state.lessons.some((lesson) => lesson.id === id)).length / state.lessons.length) * 100)
    : 0;
  els.progressText.textContent = `${percent}%`;
  els.progressBar.style.width = `${percent}%`;
  els.complete.checked = state.lessons[state.activeIndex] ? state.completed.includes(state.lessons[state.activeIndex].id) : false;
}

function setLayout(layout) {
  state.layout = layout;
  els.splitView.classList.remove("stacked", "video-expanded", "pdf-expanded");
  document.querySelectorAll(".layout-button").forEach((button) =>
    button.classList.toggle("active", button.dataset.layout === layout)
  );
  if (layout === "stacked") els.splitView.classList.add("stacked");
  localStorage.setItem("eduspace.layout", layout);
}

function closeMobileSidebar() {
  $("#sidebar").classList.remove("mobile-open");
  $("#sidebarBackdrop").classList.remove("open");
}

els.search.addEventListener("input", () => renderSidebar(els.search.value));
els.complete.addEventListener("change", () => {
  const lesson = state.lessons[state.activeIndex];
  if (lesson) toggleComplete(lesson.id, els.complete.checked);
});
els.previous.addEventListener("click", () => {
  if (state.activeIndex > 0) { state.activeIndex--; renderLesson(); }
});
els.next.addEventListener("click", () => {
  if (state.activeIndex < state.lessons.length - 1) { state.activeIndex++; renderLesson(); }
});
document.querySelectorAll(".layout-button").forEach((button) =>
  button.addEventListener("click", () => setLayout(button.dataset.layout))
);
document.querySelectorAll("[data-expand]").forEach((button) =>
  button.addEventListener("click", () => {
    const panel = button.dataset.expand;
    const className = `${panel}-expanded`;
    if (els.splitView.classList.contains(className)) els.splitView.classList.remove(className);
    else els.splitView.classList.remove("video-expanded", "pdf-expanded", "stacked"), els.splitView.classList.add(className);
  })
);

$("#themeButton").addEventListener("click", () => {
  const theme = document.documentElement.dataset.theme === "dark" ? "light" : "dark";
  document.documentElement.dataset.theme = theme;
  localStorage.setItem("eduspace.theme", theme);
});
$("#languageButton").addEventListener("click", () => {
  document.documentElement.dir = document.documentElement.dir === "rtl" ? "ltr" : "rtl";
  document.documentElement.lang = document.documentElement.dir === "rtl" ? "ar" : "en";
});
$("#sidebarCollapse").addEventListener("click", () => {
  const collapsed = $("#sidebar").classList.toggle("collapsed");
  $("#main").classList.toggle("sidebar-collapsed", collapsed);
});
$("#menuButton").addEventListener("click", () => {
  $("#sidebar").classList.add("mobile-open");
  $("#sidebarBackdrop").classList.add("open");
});
$("#sidebarBackdrop").addEventListener("click", closeMobileSidebar);

const divider = $("#divider");
divider.addEventListener("pointerdown", (event) => {
  if (window.innerWidth <= 900) return;
  divider.setPointerCapture(event.pointerId);
  divider.classList.add("dragging");
});
divider.addEventListener("pointermove", (event) => {
  if (!divider.hasPointerCapture(event.pointerId)) return;
  const rect = els.splitView.getBoundingRect?.() || els.splitView.getBoundingClientRect();
  const ratio = Math.min(80, Math.max(20, ((event.clientX - rect.left) / rect.width) * 100));
  els.splitView.style.gridTemplateColumns = `minmax(280px,${ratio}fr) 8px minmax(280px,${100 - ratio}fr)`;
});
divider.addEventListener("pointerup", (event) => {
  divider.classList.remove("dragging");
  if (divider.hasPointerCapture(event.pointerId)) divider.releasePointerCapture(event.pointerId);
});

(async function init() {
  const savedTheme = localStorage.getItem("eduspace.theme");
  if (savedTheme) document.documentElement.dataset.theme = savedTheme;
  setLayout(localStorage.getItem("eduspace.layout") || "split");

  let manifest = await loadManifest();
  if (!manifest && config.folderId && config.apiKey) {
    try { manifest = await loadDriveFolder(config.folderId); }
    catch (error) { console.warn(error); manifest = { courseTitle: config.courseTitle, lessons: [] }; }
  }
  if (!manifest) manifest = { courseTitle: config.courseTitle, lessons: [] };
  state.lessons = (manifest.lessons || []).map((lesson, index) => ({
    id: lesson.id || `lesson-${index + 1}`,
    module: lesson.module || "Course",
    title: lesson.title || `Lesson ${index + 1}`,
    videoId: lesson.videoId || "",
    pdfId: lesson.pdfId || "",
    videoName: lesson.videoName,
    pdfName: lesson.pdfName
  }));
  els.breadcrumb.textContent = manifest.courseTitle || config.courseTitle || "Education Course";
  renderLesson();
  renderSidebar();
})();
