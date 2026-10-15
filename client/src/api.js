// Jedino mjesto za pozive backenda. Spring vraca samo statusni kod, pa poruke gresaka
// daje klijent: zajednicke su ispod, a poziv moze dati svoje (isti kod znaci razlicito po ekranu).

const DEFAULT_ERRORS = {
  400: 'Unos nije ispravan.',
  401: 'Niste prijavljeni.',
  403: 'Zahtjev je odbijen. Osvjezite stranicu.',
  404: 'Trazeno ne postoji.',
}

export class ApiError extends Error {
  constructor(status, message) {
    super(message)
    this.status = status
  }
}

let onUnauthorized = () => {}

// App postavlja sta se desava kad sesija istekne (povratak na prijavu)
export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler
}

function readCookie(name) {
  const entry = document.cookie.split('; ').find((c) => c.startsWith(`${name}=`))
  return entry ? decodeURIComponent(entry.slice(name.length + 1)) : null
}

// CSRF token je u cookie-ju XSRF-TOKEN; ako ga nema (prvi posjet, posle odjave), backend ga postavlja
async function csrfToken() {
  let token = readCookie('XSRF-TOKEN')
  if (!token) {
    await fetch('/api/auth/csrf')
    token = readCookie('XSRF-TOKEN')
  }
  return token
}

export async function api(path, { method = 'GET', body, errors = {} } = {}) {
  const headers = {}
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (method !== 'GET') headers['X-XSRF-TOKEN'] = await csrfToken()

  const response = await fetch(`/api${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  if (!response.ok) {
    if (response.status === 401 && !path.startsWith('/auth/')) onUnauthorized()
    const message = errors[response.status] ?? DEFAULT_ERRORS[response.status]
    throw new ApiError(response.status, message ?? `Greska na serveru (${response.status}).`)
  }
  return response.status === 204 ? null : response.json()
}
