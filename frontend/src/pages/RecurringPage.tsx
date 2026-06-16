import { useState, useEffect } from 'react'
import Box from '@mui/material/Box'
import Card from '@mui/material/Card'
import Typography from '@mui/material/Typography'
import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import DialogTitle from '@mui/material/DialogTitle'
import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
import TextField from '@mui/material/TextField'
import MenuItem from '@mui/material/MenuItem'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import IconButton from '@mui/material/IconButton'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import AddIcon from '@mui/icons-material/Add'
import Chip from '@mui/material/Chip'
import Skeleton from '@mui/material/Skeleton'
import { getRecurringApi, createRecurringApi, updateRecurringApi, deleteRecurringApi } from '../api/recurring'
import { getCategoriesApi } from '../api/categories'
import type { RecurringExpense, Category } from '../types'

interface ReForm {
  name: string
  amount: number
  dayOfMonth: number
  frequency: string
  categoryId: string
  notes: string
}

const defaultForm: ReForm = {
  name: '', amount: 0, dayOfMonth: 1, frequency: 'MONTHLY',
  categoryId: '', notes: '',
}

export default function RecurringPage() {
  const [rows, setRows] = useState<RecurringExpense[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState<RecurringExpense | null>(null)
  const [form, setForm] = useState(defaultForm)
  const [saving, setSaving] = useState(false)

  const load = async () => {
    try {
      const [r, c] = await Promise.all([getRecurringApi(), getCategoriesApi()])
      setRows(r.data)
      setCategories(c.data)
    } finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const handleSave = async () => {
    setSaving(true)
    try {
      const payload = { ...form, categoryId: form.categoryId || null }
      if (editing) {
        await updateRecurringApi(editing.id, payload as any)
      } else {
        await createRecurringApi(payload as any)
      }
      setDialogOpen(false)
      setEditing(null)
      setForm(defaultForm)
      await load()
    } finally { setSaving(false) }
  }

  const handleDelete = async (row: RecurringExpense) => {
    if (!confirm(`Delete "${row.name}"?`)) return
    await deleteRecurringApi(row.id)
    await load()
  }

  const formatCurrency = (n: number) =>
    new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', minimumFractionDigits: 0 }).format(n)

  if (loading) return <Skeleton variant="rectangular" height={400} />

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4">Recurring Expenses</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => {
          setEditing(null); setForm(defaultForm); setDialogOpen(true)
        }}>
          Add Recurring
        </Button>
      </Box>

      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 600 }}>Name</TableCell>
                <TableCell sx={{ fontWeight: 600 }} align="right">Amount</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Day</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Frequency</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Next Date</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                <TableCell sx={{ fontWeight: 600 }} width={100}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.map(row => (
                <TableRow key={row.id}>
                  <TableCell>{row.name}</TableCell>
                  <TableCell align="right">{formatCurrency(row.amount)}</TableCell>
                  <TableCell>{row.dayOfMonth}</TableCell>
                  <TableCell>{row.frequency}</TableCell>
                  <TableCell>{row.nextDate || '-'}</TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      label={row.isActive ? 'Active' : 'Inactive'}
                      color={row.isActive ? 'success' : 'default'}
                    />
                  </TableCell>
                  <TableCell>
                    <IconButton size="small" onClick={() => {
                      setEditing(row)
                      setForm({
                        name: row.name, amount: row.amount,
                        dayOfMonth: row.dayOfMonth, frequency: row.frequency,
                        categoryId: row.categoryId || '', notes: row.notes || '',
                      })
                      setDialogOpen(true)
                    }} color="primary">
                      <EditIcon fontSize="small" />
                    </IconButton>
                    <IconButton size="small" onClick={() => handleDelete(row)} color="error">
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? 'Edit Recurring Expense' : 'New Recurring Expense'}</DialogTitle>
        <DialogContent>
          <TextField fullWidth label="Name" margin="normal" required value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <TextField fullWidth label="Amount" type="number" margin="normal" required value={form.amount}
            onChange={(e) => setForm({ ...form, amount: Number(e.target.value) })} />
          <TextField fullWidth label="Day of Month" type="number" margin="normal" required
            slotProps={{ htmlInput: { min: 1, max: 31 } }} value={form.dayOfMonth}
            onChange={(e) => setForm({ ...form, dayOfMonth: Number(e.target.value) })} />
          <TextField fullWidth label="Frequency" margin="normal" select required value={form.frequency}
            onChange={(e) => setForm({ ...form, frequency: e.target.value })}>
            <MenuItem value="MONTHLY">Monthly</MenuItem>
            <MenuItem value="BIMONTHLY">Bi-monthly</MenuItem>
            <MenuItem value="QUARTERLY">Quarterly</MenuItem>
            <MenuItem value="YEARLY">Yearly</MenuItem>
          </TextField>
          <TextField fullWidth label="Category" margin="normal" select value={form.categoryId}
            onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
            <MenuItem value="">None</MenuItem>
            {categories.filter(c => c.type === 'EXPENSE').map(c => (
              <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>
            ))}
          </TextField>
          <TextField fullWidth label="Notes" margin="normal" multiline rows={2} value={form.notes}
            onChange={(e) => setForm({ ...form, notes: e.target.value })} />
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
