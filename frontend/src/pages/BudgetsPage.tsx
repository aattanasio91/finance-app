import { useState, useEffect } from 'react'
import Box from '@mui/material/Box'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import Typography from '@mui/material/Typography'
import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import DialogTitle from '@mui/material/DialogTitle'
import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import TextField from '@mui/material/TextField'
import MenuItem from '@mui/material/MenuItem'
import Grid from '@mui/material/Grid'
import AddIcon from '@mui/icons-material/Add'
import Skeleton from '@mui/material/Skeleton'
import { getBudgetsApi, createBudgetApi, updateBudgetApi, deleteBudgetApi, getBudgetSummaryApi } from '../api/budgets'
import { getCategoriesApi } from '../api/categories'
import type { Budget, BudgetSummary, Category } from '../types'

const defaultForm = { categoryId: '', amount: 0, period: 'MONTHLY' as const }

export default function BudgetsPage() {
  const [budgets, setBudgets] = useState<Budget[]>([])
  const [summary, setSummary] = useState<BudgetSummary[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState<Budget | null>(null)
  const [form, setForm] = useState(defaultForm)
  const [saving, setSaving] = useState(false)

  const load = async () => {
    try {
      const [b, s, c] = await Promise.all([
        getBudgetsApi(), getBudgetSummaryApi(), getCategoriesApi(),
      ])
      setBudgets(b.data)
      setSummary(s.data)
      setCategories(c.data.filter(cat => cat.type === 'EXPENSE'))
    } finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const handleSave = async () => {
    setSaving(true)
    try {
      if (editing) {
        await updateBudgetApi(editing.id, form)
      } else {
        await createBudgetApi(form)
      }
      setDialogOpen(false)
      setEditing(null)
      setForm(defaultForm)
      await load()
    } finally { setSaving(false) }
  }

  const handleDelete = async (b: Budget) => {
    if (!confirm('Delete this budget?')) return
    await deleteBudgetApi(b.id)
    await load()
  }

  const formatCurrency = (n: number) =>
    new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', minimumFractionDigits: 0 }).format(n)

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OK': return 'success.main'
      case 'WARNING': return 'warning.main'
      case 'EXCEEDED': return 'error.main'
      default: return 'text.primary'
    }
  }

  const getCategoryName = (id: string) => categories.find(c => c.id === id)?.name || id

  if (loading) return <Skeleton variant="rectangular" height={400} />

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4">Budgets</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => {
          setEditing(null); setForm(defaultForm); setDialogOpen(true)
        }}>
          Add Budget
        </Button>
      </Box>

      <Grid container spacing={3}>
        {budgets.length === 0 ? (
          <Grid size={{ xs: 12 }}>
            <Card><CardContent><Typography color="text.secondary" align="center">No budgets yet</Typography></CardContent></Card>
          </Grid>
        ) : (
          budgets.map((b) => {
            const s = summary.find(x => x.id === b.id)
            return (
              <Grid key={b.id} size={{ xs: 12, sm: 6, md: 4 }}>
                <Card>
                  <CardContent>
                    <Typography variant="h6">{getCategoryName(b.categoryId)}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      {b.period} budget
                    </Typography>
                    <Box sx={{ mt: 1 }}>
                      <Typography variant="body2">
                        Budget: {formatCurrency(b.amount)}
                      </Typography>
                      {s && (
                        <>
                          <Typography variant="body2">
                            Spent: {formatCurrency(s.spent)}
                          </Typography>
                          <Typography variant="body2" sx={{ color: getStatusColor(s.status), fontWeight: 600 }}>
                            {s.percentage.toFixed(0)}% - {s.status}
                          </Typography>
                          <Box sx={{ bgcolor: 'grey.200', borderRadius: 1, height: 10, mt: 1 }}>
                            <Box sx={{
                              bgcolor: getStatusColor(s.status),
                              borderRadius: 1, height: 10,
                              width: `${Math.min(s.percentage, 100)}%`,
                            }} />
                          </Box>
                        </>
                      )}
                    </Box>
                    <Box sx={{ mt: 2, display: 'flex', gap: 1 }}>
                      <Button size="small" onClick={() => {
                        setEditing(b)
                        setForm({ categoryId: b.categoryId, amount: b.amount, period: b.period as any })
                        setDialogOpen(true)
                      }}>Edit</Button>
                      <Button size="small" color="error" onClick={() => handleDelete(b)}>Delete</Button>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            )
          })
        )}
      </Grid>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? 'Edit Budget' : 'New Budget'}</DialogTitle>
        <DialogContent>
          <TextField fullWidth label="Category" margin="normal" select required value={form.categoryId}
            onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
            {categories.map(c => (
              <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>
            ))}
          </TextField>
          <TextField fullWidth label="Amount" type="number" margin="normal" required value={form.amount}
            onChange={(e) => setForm({ ...form, amount: Number(e.target.value) })} />
          <TextField fullWidth label="Period" margin="normal" select required value={form.period}
            onChange={(e) => setForm({ ...form, period: e.target.value as any })}>
            <MenuItem value="WEEKLY">Weekly</MenuItem>
            <MenuItem value="MONTHLY">Monthly</MenuItem>
            <MenuItem value="YEARLY">Yearly</MenuItem>
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleSave} disabled={saving}>
            {saving ? 'Saving...' : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
