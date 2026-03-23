# Mivora Mobile (React Native)

This is a React Native (Expo + TypeScript) mobile version scaffolded from the web app flows.

## Implemented flows

- Login with backend `/users/login`
- Persistent auth (`AsyncStorage`)
- Auto refresh token on `401` (`/users/refresh-token`)
- Events list (`/events`)
- Event detail (`/events/:id`)
- Book ticket (`/tickets`)
- My tickets (`/users/me/tickets`)
- Profile + logout (`/users/logout`)

## Run locally

```bash
npm install
npm start
```

If PowerShell blocks `npm` scripts on Windows, use:

```bash
npm.cmd install
npm.cmd start
```

## API base URL

Default base URL is configured in `app.json`:

- `expo.extra.apiBaseUrl`

Current value:

- `https://khoinguyenpham.name.vn/api/v1`

Update it if your backend host changes.
