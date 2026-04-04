####


##### User Flow

- Upload file from browser and get redirected to list page with new item visible
- create text item by pasting url, text or snippeets into a form
- open home page and see recent items with metadata
- click an item to preview or download
- delete item from UI
- upload / create via HTTP endpoints

##### Pages

- **Home**-  Recent item list, quick upload form, text form
- **Item Details**- metadata + preview/download action
- **Error Page** - upload failure/ validation failure, default

##### Dynamic Routes

- `GET /` - Home + upload form + note form + list
- `POST /upload` - Handle file upload -> redirect /
- `POST /note` - Handle text/url/snippet -> redirect
- `GET /item/:id` - preview or download or metadata
- `POST /delete/:id` - Delete -> redirect /

[//]: # ( USE SERVER RENDERED)
[//]: # (##### Api Endpoints)

[//]: # (- `POST /api/upload` - handle upload)

[//]: # (- `POST /api/notes` - create text/link/snippet)

[//]: # (- `GET /api/items/:id` - item details)

[//]: # (- `GET /api/items/:id/downoad` - download file)

[//]: # (- `DELETE /api/items/:id/delete` - delete item)

[//]: # (- `GET /health`)

##### Data Type

- tbd

##### Storage

- items (files/notes) persistant
- `/data/metadata`
- `/data/files`
- generate in mem view at startup

##### Upload Req

- Multipart form-data uploads
- images, pdfs, zips, text files, configs
- max upload size - 10-25mb
- sanitize filenames
- UX
  - file picker
  - feedback after upload
  - failure feedback for bad req

##### Text Req

- plain text, snippets, or urls
- Title
- multiline content support
- render safely in html (not executable html)
- clickable urls
- copyable snippets

##### Main Page

- list of recent items
- type badge or label
- name / title
- time created
- size for files
- actions: view/open, download if file, delete


##### Future Scopes

- **Fizzy Search**- ??
- **Login/auth**- ?????
