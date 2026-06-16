import { useState, useEffect } from 'react'
import Grid from '@mui/material/Grid'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import Typography from '@mui/material/Typography'
import Skeleton from '@mui/material/Skeleton'
import Box from '@mui/material/Box'
import TrendingUpIcon from '@mui/icons-material/TrendingUp'
import TrendingDownIcon from '@mui/icons-material/TrendingDown'
import AccountBalanceIcon from '@mui/icons-material/AccountBalance'
import SavingsIcon from '@mui/icons-material/Savings'
import { getDashboardApi, getExpensesByCategoryApi } from '../api/dashboard'
import type { DashboardData, CategoryExpense } from '../types'

export default function DashboardPage() {
  const [data, setData] = useState<DashboardData | null>(null)
  const [categoryExpenses, setCategoryExpenses] = useState<CategoryExpense[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      getDashboardApi(),
      getExpensesByCategoryApi(),
    ]).then(([d, c]) => {
      setData(d.data)
      setCategoryExpenses(c.data)
    }).finally(() => setLoading(false))
  }, [])

  const formatCurrency = (n: number) =>
    new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', minimumFractionDigits: 0 }).format(n)

  if (loading) return <Skeleton variant="rectangular" height={300} />

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Dashboard</Typography>

      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <AccountBalanceIcon color="primary" />
                <Typography variant="body2" color="text.secondary">Total Balance</Typography>
              </Box>
              <Typography variant="h5">{formatCurrency(data?.totalBalance ?? 0)}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <TrendingUpIcon sx={{ color: 'success.main' }} />
                <Typography variant="body2" color="text.secondary">Monthly Income</Typography>
              </Box>
              <Typography variant="h5" color="success.main">{formatCurrency(data?.monthlyIncome ?? 0)}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <TrendingDownIcon sx={{ color: 'error.main' }} />
                <Typography variant="body2" color="text.secondary">Monthly Expenses</Typography>
              </Box>
              <Typography variant="h5" color="error.main">{formatCurrency(data?.monthlyExpenses ?? 0)}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <SavingsIcon sx={{ color: 'info.main' }} />
                <Typography variant="body2" color="text.secondary">Net Savings</Typography>
              </Box>
              <Typography variant="h5" color="info.main">{formatCurrency(data?.netSavings ?? 0)}</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {categoryExpenses.length > 0 && (
        <Box>
          <Typography variant="h5" gutterBottom>Expenses by Category</Typography>
          <Card>
            <CardContent>
              {categoryExpenses.map((ce) => (
                <Box key={ce.categoryId} sx={{ mb: 1 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="body2">{ce.categoryName}</Typography>
                    <Typography variant="body2">{formatCurrency(ce.amount)} ({ce.percentage.toFixed(1)}%)</Typography>
                  </Box>
                  <Box sx={{ bgcolor: 'grey.200', borderRadius: 1, height: 8, mt: 0.5 }}>
                    <Box
                      sx={{
                        bgcolor: 'primary.main',
                        borderRadius: 1,
                        height: 8,
                        width: `${Math.min(ce.percentage, 100)}%`,
                      }}
                    />
                  </Box>
                </Box>
              ))}
            </CardContent>
          </Card>
        </Box>
      )}
    </Box>
  )
}
