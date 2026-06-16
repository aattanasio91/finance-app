import { useState, useEffect } from 'react'
import TextField from '@mui/material/TextField'
import MenuItem from '@mui/material/MenuItem'
import { getAccountsApi, createAccountApi, updateAccountApi, deleteAccountApi } from '../api/accounts'
import CrudPage, { FormDialog } from '../components/CrudPage'
import type { Account } from '../types'

const defaultForm = { name: '', type: 'CHECKING' as const, balance: 0, currency: 'ARS' }

export default function AccountsPage() {
  const [rows, setRows] = useState<Account[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState<Account | null>(null)
  const [form, setForm] = useState(defaultForm)
  const [saving, setSaving] = useState(false)

  const load = async () => {
    try {
      const res = await getAccountsApi()
      setRows(res.data)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load')
    } finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const handleSave = async () => {
    setSaving(true)
    try {
      if (editing) {
        await updateAccountApi(editing.id, form)
      } else {
        await createAccountApi(form)
      }
      setDialogOpen(false)
      setEditing(null)
      setForm(defaultForm)
      await load()
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save')
    } finally { setSaving(false) }
  }

  const handleDelete = async (row: Account) => {
    if (!confirm(`Deactivate account "${row.name}"?`)) return
    try {
      await deleteAccountApi(row.id)
      await load()
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete')
    }
  }

  const formatCurrency = (n: number) =>
    new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', minimumFractionDigits: 0 }).format(n)

  return (
    <>
      <CrudPage
        title="Accounts"
        columns={[
          { id: 'name', label: 'Name' },
          { id: 'type', label: 'Type' },
          { id: 'balance', label: 'Balance', render: (r: Account) => formatCurrency(r.balance) },
          { id: 'currency', label: 'Currency' },
          { id: 'isActive', label: 'Active', render: (r: Account) => r.isActive ? 'Yes' : 'No' },
        ]}
        rows={rows} loading={loading} error={error}
        onAdd={() => { setEditing(null); setForm(defaultForm); setDialogOpen(true) }}
        onEdit={(r) => { setEditing(r); setForm({ name: r.name, type: r.type as any, balance: r.balance, currency: r.currency }); setDialogOpen(true) }}
        onDelete={handleDelete}
        getRowId={(r) => r.id}
      />

      <FormDialog
        open={dialogOpen} loading={saving}
        title={editing ? 'Edit Account' : 'New Account'}
        onClose={() => setDialogOpen(false)}
        onSubmit={handleSave}
      >
        <TextField fullWidth label="Name" margin="normal" required value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })} />
        <TextField fullWidth label="Type" margin="normal" select required value={form.type}
          onChange={(e) => setForm({ ...form, type: e.target.value as any })}>
          <MenuItem value="CHECKING">Checking</MenuItem>
          <MenuItem value="SAVINGS">Savings</MenuItem>
          <MenuItem value="CASH">Cash</MenuItem>
          <MenuItem value="CREDIT_CARD">Credit Card</MenuItem>
          <MenuItem value="INVESTMENT">Investment</MenuItem>
        </TextField>
        <TextField fullWidth label="Balance" type="number" margin="normal" value={form.balance}
          onChange={(e) => setForm({ ...form, balance: Number(e.target.value) })} />
        <TextField fullWidth label="Currency" margin="normal" value={form.currency}
          onChange={(e) => setForm({ ...form, currency: e.target.value })} />
      </FormDialog>
    </>
  )
}
