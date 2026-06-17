export type NotifChannel = 'email' | 'push' | 'discord'

export type NotifPrefs = Record<NotifChannel, boolean>

const PREFIX = 'escrims_notif_'

const DEFAULT_PREFS: NotifPrefs = {
  email: false,
  push: false,
  discord: false,
}

export function getNotifPrefs(userId: string): NotifPrefs {
  try {
    const raw = localStorage.getItem(`${PREFIX}${userId}`)
    if (!raw) return { ...DEFAULT_PREFS }
    const parsed = JSON.parse(raw) as Partial<NotifPrefs>
    return { ...DEFAULT_PREFS, ...parsed }
  } catch {
    return { ...DEFAULT_PREFS }
  }
}

export function setNotifPref(userId: string, channel: NotifChannel, active: boolean): NotifPrefs {
  const prefs = getNotifPrefs(userId)
  prefs[channel] = active
  localStorage.setItem(`${PREFIX}${userId}`, JSON.stringify(prefs))
  return prefs
}

export function countActivePrefs(prefs: NotifPrefs): number {
  return Object.values(prefs).filter(Boolean).length
}
