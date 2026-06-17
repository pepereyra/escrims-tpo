import { useEffect, useRef, useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { VerifiedBadge } from './VerifiedBadge'
import { useAuth } from '../context/AuthContext'
import { getAvatar, getInitials } from '../utils/avatar'

const ACCOUNT_ROUTES = ['/perfil', '/notificaciones', '/favoritos']

export function Layout() {
  const { user, logout, isAdmin } = useAuth()
  const location = useLocation()
  const menuRef = useRef<HTMLDivElement>(null)
  const [menuOpen, setMenuOpen] = useState(false)
  const avatarUrl = user ? getAvatar(user.id) : null
  const accountMenuActive = ACCOUNT_ROUTES.includes(location.pathname)

  useEffect(() => {
    setMenuOpen(false)
  }, [location.pathname])

  useEffect(() => {
    if (!menuOpen) return

    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setMenuOpen(false)
      }
    }

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setMenuOpen(false)
    }

    document.addEventListener('mousedown', handleClickOutside)
    document.addEventListener('keydown', handleEscape)
    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
      document.removeEventListener('keydown', handleEscape)
    }
  }, [menuOpen])

  const handleLogout = () => {
    setMenuOpen(false)
    logout()
  }

  return (
    <div className="app">
      <header className="header">
        <div className="header-inner">
          <NavLink to="/" className="logo">
            <span className="logo-mark">E</span>
            <span className="logo-text">
              SCR<span className="logo-accent">I</span>MS
            </span>
          </NavLink>

          <div className="header-actions">
            {user ? (
              <div className="user-menu-wrap" ref={menuRef}>
                <button
                  type="button"
                  className={`user-menu-trigger${menuOpen ? ' open' : ''}${
                    accountMenuActive ? ' active' : ''
                  }`}
                  onClick={() => setMenuOpen((open) => !open)}
                  aria-expanded={menuOpen}
                  aria-haspopup="menu"
                  aria-label="Menú de cuenta"
                >
                  {avatarUrl ? (
                    <img src={avatarUrl} alt="" className="user-avatar" />
                  ) : (
                    <span className="user-avatar user-avatar-fallback">
                      {getInitials(user.username)}
                    </span>
                  )}
                  <span className="user-chip-text">
                    <span className="user-name">
                      {user.username}
                      {user.verificado && <VerifiedBadge size="sm" />}
                    </span>
                    <span className="user-role">{user.rolSistema}</span>
                  </span>
                  <span className="user-menu-chevron" aria-hidden="true" />
                </button>

                {menuOpen && (
                  <div className="header-menu-dropdown" role="menu">
                    <div className="header-menu-user">
                      {avatarUrl ? (
                        <img src={avatarUrl} alt="" className="header-menu-user-avatar" />
                      ) : (
                        <span className="header-menu-user-avatar header-menu-user-avatar-fallback">
                          {getInitials(user.username)}
                        </span>
                      )}
                      <div className="header-menu-user-info">
                        <span className="header-menu-user-name">
                          {user.username}
                          {user.verificado && <VerifiedBadge size="sm" />}
                        </span>
                        <span className="header-menu-user-email">{user.email}</span>
                      </div>
                    </div>

                    <div className="header-menu-divider" />

                    <NavLink
                      to="/perfil"
                      className={({ isActive }) =>
                        `header-menu-item${isActive ? ' active' : ''}`
                      }
                      role="menuitem"
                      onClick={() => setMenuOpen(false)}
                    >
                      Mi perfil
                    </NavLink>
                    <NavLink
                      to="/notificaciones"
                      className={({ isActive }) =>
                        `header-menu-item${isActive ? ' active' : ''}`
                      }
                      role="menuitem"
                      onClick={() => setMenuOpen(false)}
                    >
                      Notificaciones
                    </NavLink>
                    <NavLink
                      to="/favoritos"
                      className={({ isActive }) =>
                        `header-menu-item${isActive ? ' active' : ''}`
                      }
                      role="menuitem"
                      onClick={() => setMenuOpen(false)}
                    >
                      Favoritos
                    </NavLink>

                    {isAdmin && (
                      <>
                        <div className="header-menu-divider" />
                        <NavLink
                          to="/auditoria"
                          className={({ isActive }) =>
                            `header-menu-item${isActive ? ' active' : ''}`
                          }
                          role="menuitem"
                          onClick={() => setMenuOpen(false)}
                        >
                          Auditoría
                        </NavLink>
                      </>
                    )}

                    <div className="header-menu-divider" />

                    <button
                      type="button"
                      className="header-menu-item header-menu-item-danger"
                      role="menuitem"
                      onClick={handleLogout}
                    >
                      Salir
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <NavLink to="/login" className="btn btn-primary btn-sm">
                Ingresar
              </NavLink>
            )}
          </div>
        </div>
      </header>

      <main className="main">
        <Outlet />
      </main>
    </div>
  )
}
