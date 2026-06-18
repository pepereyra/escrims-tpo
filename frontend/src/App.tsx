import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AdminRoute } from './components/AdminRoute'
import { Layout } from './components/Layout'
import { ProtectedRoute } from './components/ProtectedRoute'
import { AuthProvider } from './context/AuthContext'
import { AuditPage } from './pages/AuditPage'
import { FavoritesPage } from './pages/FavoritesPage'
import { LoginPage } from './pages/LoginPage'
import { NotificationsPage } from './pages/NotificationsPage'
import { ProfilePage } from './pages/ProfilePage'
import { ScrimDetailPage } from './pages/ScrimDetailPage'
import { ScrimsPage } from './pages/ScrimsPage'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route index element={<ScrimsPage />} />
            <Route path="scrims/:id" element={<ScrimDetailPage />} />
            <Route
              path="crear"
              element={<Navigate to="/?crear=1" replace />}
            />
            <Route path="perfil" element={<ProfilePage />} />
            <Route path="favoritos" element={<FavoritesPage />} />
            <Route path="notificaciones" element={<NotificationsPage />} />
            <Route
              path="auditoria"
              element={
                <AdminRoute>
                  <AuditPage />
                </AdminRoute>
              }
            />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
