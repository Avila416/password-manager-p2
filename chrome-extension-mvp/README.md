# Chrome Extension MVP

## Demo Flow
1. Start Module 2 backend on `http://localhost:8084` (or legacy `8083`).
2. Save a vault credential with `website` set to a domain or URL (for example `example.com`).
3. In Chrome, open `chrome://extensions`, enable Developer Mode, then `Load unpacked` this folder.
4. Open a login page for the same domain (or `demo-login.html` as a local test).
5. Click the extension and press `Fill`.

## Backend Contract
- Endpoint used by popup: `GET /api/vault/by-domain?domain=...`
- Response expects `username` and `password` fields in each item.
