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
import Skeleton from '@mui/material/Skeleton'
import Pagination from '@mui/material/Pagination'
import { getTransactionsApi, createTransactionApi, updateTransactionApi, deleteTransactionApi } from '../api/transactions'
import { getAccountsApi } from '../api/accounts'
import { getCategoriesApi } from '../api/categories'
import type { Transaction, Account, Category } from '../types'

interface TxForm {
  accountId: string
  categoryId: string
  amount: number
  description: string
  date: string
  type: 'INCOME' | 'EXPENSE'
  notes: string
}

const defaultForm: TxForm = {
  accountId: '', categoryId: '', amount: 0,
  description: '', date: new Date().toISOString().split('T')[0],
  type: 'EXPENSE', notes: '',
}

export default function TransactionsPage() {
  const [rows, setRows] = useState<Transaction[]>([])
  const [accounts, setAccounts] = useState<Account[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState<Transaction | null>(null)
  const [form, setForm] = useState(defaultForm)
  const [saving, setSaving] = useState(false)
  const [, setError] = useState('')

  const load = async () => {
    try {
      const [txRes, acctRes, catRes] = await Promise.all([
        getTransactionsApi({ page, size: 20, sort: 'date,desc' }),
        getAccountsApi(),
        getCategoriesApi(),
      ])
      setRows(txRes.data.content || txRes.data)
      if (txRes.data.totalPages !== undefined) setTotalPages(txRes.data.totalPages)
      setAccounts(acctRes.data)
      setCategories(catRes.data)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load')
    } finally { setLoading(false) }
  }

  useEffect(() => { load() }, [page])

  const handleSave = async () => {
    setSaving(true)
    try {
      const payload = {
        ...form,
        amount: form.type === 'EXPENSE' ? -Math.abs(form.amount) : Math.abs(form.amount),
        categoryId: form.categoryId || null,
      }
      if (editing) {
        await updateTransactionApi(editing.id, payload)
      } else {
        await createTransactionApi(payload)
      }
      setDialogOpen(false)
      setEditing(null)
      setForm(defaultForm)
      await load()
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save')
    } finally { setSaving(false) }
  }

  const handleDelete = async (row: Transaction) => {
    if (!confirm('Delete this transaction?')) return
    try {
      await deleteTransactionApi(row.id)
      await load()
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete')
    }
  }

  const formatCurrency = (n: number) =>
    new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', minimumFractionDigits: 0 }).format(n)

  if (loading) return <Skeleton variant="rectangular" height={400} />

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4">Transactions</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => {
          setEditing(null)
          setForm({ ...defaultForm, accountId: accounts[0]?.id || '' })
          setDialogOpen(true)
        }}>
          Add Transaction
        </Button>
      </Box>

      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 600 }}>Date</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Description</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Type</TableCell>
                <TableCell sx={{ fontWeight: 600 }} align="right">Amount</TableCell>
                <TableCell sx={{ fontWeight: 600 }} width={100}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.map((row) => (
                <TableRow key={row.id}>
                  <TableCell>{row.date}</TableCell>
                  <TableCell>{row.description}</TableCell>
                  <TableCell>{row.type}</TableCell>
                  <TableCell align="right" sx={{
                    color: row.type === 'INCOME' ? 'success.main' : 'error.main',
                    fontWeight: 600,
                  }}>
                    {formatCurrency(row.amount)}
                  </TableCell>
                  <TableCell>
                    <IconButton size="small" onClick={() => {
                      setEditing(row)
                      setForm({
                        accountId: row.accountId, categoryId: row.categoryId || '',
                        amount: Math.abs(row.amount), description: row.description,
                        date: row.date, type: row.type as 'INCOME' | 'EXPENSE',
                        notes: row.notes || '',
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
        {totalPages > 1 && (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
            <Pagination count={totalPages} page={page + 1}
              onChange={(_, p) => setPage(p - 1)} />
          </Box>
        )}
      </Card>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editing ? 'Edit Transaction' : 'New Transaction'}</DialogTitle>
        <DialogContent>
          <TextField fullWidth label="Description" margin="normal" required value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })} />
          <TextField fullWidth label="Account" margin="normal" select required value={form.accountId}
            onChange={(e) => setForm({ ...form, accountId: e.target.value })}>
            {accounts.filter(a => a.isActive).map(a => (
              <MenuItem key={a.id} value={a.id}>{a.name}</MenuItem>
            ))}
          </TextField>
          <TextField fullWidth label="Type" margin="normal" select required value={form.type}
            onChange={(e) => setForm({ ...form, type: e.target.value as 'INCOME' | 'EXPENSE' })}>
            <MenuItem value="INCOME">Income</MenuItem>
            <MenuItem value="EXPENSE">Expense</MenuItem>
          </TextField>
          <TextField fullWidth label="Amount" type="number" margin="normal" required value={form.amount}
            onChange={(e) => setForm({ ...form, amount: Number(e.target.value) })} />
          <TextField fullWidth label="Date" type="date" margin="normal" required
            slotProps={{ inputLabel: { shrink: true } }} value={form.date}
            onChange={(e) => setForm({ ...form, date: e.target.value })} />
          <TextField fullWidth label="Category" margin="normal" select value={form.categoryId}
            onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
            <MenuItem value="">None</MenuItem>
            {categories.map(c => (
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
