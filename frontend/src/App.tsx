import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './contexts/AuthContext'
import MainLayout from './layouts/MainLayout'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import DashboardPage from './pages/DashboardPage'
import AccountsPage from './pages/AccountsPage'
import CardsPage from './pages/CardsPage'
import CategoriesPage from './pages/CategoriesPage'
import TransactionsPage from './pages/TransactionsPage'
import BudgetsPage from './pages/BudgetsPage'
import RecurringPage from './pages/RecurringPage'
import ImportPage from './pages/ImportPage'

function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth()
  if (!isAuthenticated) return <Navigate to="/login" replace />
  return <MainLayout>{children}</MainLayout>
}

function PublicRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth()
  if (isAuthenticated) return <Navigate to="/" replace />
  return <>{children}</>
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />
      <Route path="/register" element={<PublicRoute><RegisterPage /></PublicRoute>} />
      <Route path="/" element={<PrivateRoute><DashboardPage /></PrivateRoute>} />
      <Route path="/accounts" element={<PrivateRoute><AccountsPage /></PrivateRoute>} />
      <Route path="/cards" element={<PrivateRoute><CardsPage /></PrivateRoute>} />
      <Route path="/categories" element={<PrivateRoute><CategoriesPage /></PrivateRoute>} />
      <Route path="/transactions" element={<PrivateRoute><TransactionsPage /></PrivateRoute>} />
      <Route path="/budgets" element={<PrivateRoute><BudgetsPage /></PrivateRoute>} />
      <Route path="/recurring" element={<PrivateRoute><RecurringPage /></PrivateRoute>} />
      <Route path="/import" element={<PrivateRoute><ImportPage /></PrivateRoute>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  )
}

export default App
