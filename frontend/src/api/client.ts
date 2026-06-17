const TOKEN_KEY = 'escrims_token'

export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

function networkErrorMessage(): string {
  return 'No se pudo conectar con el servidor. Verificá que el backend esté corriendo en el puerto 8080.'
}

export async function api<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const token = getToken()
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  }

  let res: Response
  try {
    res = await fetch(`/api${path}`, { ...options, headers })
  } catch {
    throw new ApiError(networkErrorMessage(), 0)
  }

  if (res.status === 204) {
    return undefined as T
  }

  const body = await res.json().catch(() => ({}))

  if (!res.ok) {
    const message =
      typeof body.error === 'string' && body.error
        ? body.error
        : res.status === 502 || res.status === 503
          ? networkErrorMessage()
          : res.statusText || 'Error en la solicitud'
    throw new ApiError(message, res.status)
  }

  return body as T
}
