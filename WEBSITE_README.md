# EduSpace

Responsive educational lesson viewer for videos and PDFs stored in Google Drive.

## Run locally

```bash
cd education-platform
python3 -m http.server 4173
```

Open `http://localhost:4173`.

## Connect Google Drive

1. Make the course folder and its files viewable to anyone with the link.
2. Create an API key in Google Cloud Console and enable Google Drive API v3.
3. Edit `config.js` and set `folderId` and `apiKey`.
4. Reload the site.

The app recursively reads folders, extracts natural lesson numbers from names, and pairs videos with PDFs.
Supported video types are MP4, MOV, AVI, WebM, MKV and Google videos; PDFs are matched by lesson number or nearest name.

For a keyless or static deployment, edit `data/manifest.json`:

```json
{
  "courseTitle": "My Course",
  "lessons": [
    {
      "id": "lesson-1",
      "module": "Module 1",
      "title": "Introduction",
      "videoId": "GOOGLE_DRIVE_FILE_ID",
      "pdfId": "GOOGLE_DRIVE_FILE_ID"
    }
  ]
}
```

Set `manifestUrl` in `config.js` and `folderId`/`apiKey` can remain blank.

## Deploy

Deploy the directory to Netlify, GitHub Pages, Cloudflare Pages, or any static host. No build step is required.
