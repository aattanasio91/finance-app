import { useState, useEffect } from 'react'
import Grid from '@mui/material/Grid'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import Typography from '@mui/material/Typography'
import Skeleton from '@mui/material/Skeleton'
import Box from '@mui/material/Box'
import Alert from '@mui/material/Alert'
import AlertTitle from '@mui/material/AlertTitle'
import List from '@mui/material/List'
import ListItem from '@mui/material/ListItem'
import ListItemText from '@mui/material/ListItemText'
import TrendingUpIcon from '@mui/icons-material/TrendingUp'
import TrendingDownIcon from '@mui/icons-material/TrendingDown'
import AccountBalanceIcon from '@mui/icons-material/AccountBalance'
import SavingsIcon from '@mui/icons-material/Savings'
import {
  PieChart, Pie, Cell, Tooltip, ResponsiveContainer,
  BarChart, Bar, XAxis, YAxis, CartesianGrid,
  LineChart, Line, Legend,
} from 'recharts'
import { getDashboardApi } from '../api/dashboard'
import type { DashboardData } from '../types'

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8', '#82ca9d', '#ffc658', '#8dd1e1', '#a4de6c', '#d0ed57']

export default function DashboardPage() {
  const [data, setData] = useState<DashboardData | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getDashboardApi().then((res) => {
      setData(res.data)
    }).finally(() => setLoading(false))
  }, [])

  const f = (n: number) =>
    new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', minimumFractionDigits: 0 }).format(n)

  if (loading) return <Skeleton variant="rectangular" height={400} />
  if (!data) return <Typography color="error">Error loading dashboard</Typography>

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Dashboard</Typography>

      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <AccountBalanceIcon color="primary" />
                <Typography variant="body2" color="text.secondary">Saldo Total</Typography>
              </Box>
              <Typography variant="h5">{f(data.totalBalance)}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <TrendingUpIcon sx={{ color: 'success.main' }} />
                <Typography variant="body2" color="text.secondary">Ingresos del Mes</Typography>
              </Box>
              <Typography variant="h5" color="success.main">{f(data.monthIncome)}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <TrendingDownIcon sx={{ color: 'error.main' }} />
                <Typography variant="body2" color="text.secondary">Gastos del Mes</Typography>
              </Box>
              <Typography variant="h5" color="error.main">{f(data.monthExpenses)}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <SavingsIcon sx={{ color: data.netSavings >= 0 ? 'success.main' : 'error.main' }} />
                <Typography variant="body2" color="text.secondary">Ahorro Neto</Typography>
              </Box>
              <Typography variant="h5" color={data.netSavings >= 0 ? 'success.main' : 'error.main'}>
                {f(data.netSavings)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Ingresos vs Gastos</Typography>
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={[
                      { name: 'Ingresos', value: Math.max(data.monthIncome, 0) },
                      { name: 'Gastos', value: Math.max(data.monthExpenses, 0) },
                    ]}
                    cx="50%" cy="50%" innerRadius={60} outerRadius={100}
                    dataKey="value" label={({ name, value }) => `${name}: $${(value / 1000).toFixed(0)}k`}
                  >
                    {['#00C49F', '#FF8042'].map((c, i) => <Cell key={i} fill={c} />)}
                  </Pie>
                  <Tooltip formatter={(v: number) => f(v)} />
                </PieChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Gastos por Categoría</Typography>
              {data.expensesByCategory.length > 0 ? (
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={data.expensesByCategory} layout="vertical" margin={{ left: 100 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis type="number" tickFormatter={(v) => `$${(v / 1000).toFixed(0)}k`} />
                    <YAxis type="category" dataKey="category" width={90} tick={{ fontSize: 12 }} />
                    <Tooltip formatter={(v: number) => f(v)} />
                    <Bar dataKey="amount" fill="#8884d8" radius={[0, 4, 4, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <Typography color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>
                  Sin gastos este mes
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Evolución Mensual</Typography>
              {data.monthlyEvolution.length > 0 ? (
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={data.monthlyEvolution}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="month" />
                    <YAxis tickFormatter={(v) => `$${(v / 1000).toFixed(0)}k`} />
                    <Tooltip formatter={(v: number) => f(v)} />
                    <Legend />
                    <Line type="monotone" dataKey="income" stroke="#00C49F" name="Ingresos" />
                    <Line type="monotone" dataKey="expenses" stroke="#FF8042" name="Gastos" />
                  </LineChart>
                </ResponsiveContainer>
              ) : (
                <Typography color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>
                  Sin datos mensuales
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>Proyección</Typography>
              <Alert severity={
                data.projection.status === 'POSITIVE' ? 'success' :
                data.projection.status === 'CAUTION' ? 'warning' : 'error'
              }>
                <AlertTitle>
                  {data.projection.status === 'POSITIVE' ? 'Positivo' :
                   data.projection.status === 'CAUTION' ? 'Precaución' : 'Negativo'}
                </AlertTitle>
                Saldo estimado fin de mes: <strong>{f(data.projection.estimatedEndBalance)}</strong>
              </Alert>
              <Box sx={{ mt: 2 }}>
                <Typography variant="subtitle2" gutterBottom>Saldos por Cuenta</Typography>
                <List dense>
                  {data.balanceByAccount.map((a) => (
                    <ListItem key={a.accountId} sx={{ px: 0 }}>
                      <ListItemText primary={a.name} secondary={f(a.balance)} />
                    </ListItem>
                  ))}
                </List>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  )
}
