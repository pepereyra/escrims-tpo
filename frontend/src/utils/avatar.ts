const AVATAR_PREFIX = 'escrims_avatar_'
const MAX_SIZE = 2 * 1024 * 1024

export function getAvatar(userId: string): string | null {
  return localStorage.getItem(`${AVATAR_PREFIX}${userId}`)
}

export function setAvatar(userId: string, dataUrl: string): void {
  localStorage.setItem(`${AVATAR_PREFIX}${userId}`, dataUrl)
}

export function removeAvatar(userId: string): void {
  localStorage.removeItem(`${AVATAR_PREFIX}${userId}`)
}

export function readImageFile(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    if (!file.type.startsWith('image/')) {
      reject(new Error('El archivo debe ser una imagen'))
      return
    }
    if (file.size > MAX_SIZE) {
      reject(new Error('La imagen no puede superar 2 MB'))
      return
    }
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result as string)
    reader.onerror = () => reject(new Error('No se pudo leer la imagen'))
    reader.readAsDataURL(file)
  })
}

export function getInitials(username: string): string {
  return username.slice(0, 2).toUpperCase()
}
