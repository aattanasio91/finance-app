import { useState, useEffect } from 'react'
import TextField from '@mui/material/TextField'
import MenuItem from '@mui/material/MenuItem'
import { getCardsApi, createCardApi, updateCardApi, deleteCardApi } from '../api/cards'
import { getAccountsApi } from '../api/accounts'
import CrudPage, { FormDialog } from '../components/CrudPage'
import type { CreditCard, Account } from '../types'

interface CardForm {
  accountId: string
  name: string
  brand: string
  closingDay: number
  dueDay: number
  creditLimit: number | null
  colorHex: string
}

const defaultForm: CardForm = {
  accountId: '', name: '', brand: 'VISA', closingDay: 20, dueDay: 5,
  creditLimit: null, colorHex: '',
}

export default function CardsPage() {
  const [rows, setRows] = useState<CreditCard[]>([])
  const [accounts, setAccounts] = useState<Account[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState<CreditCard | null>(null)
  const [form, setForm] = useState(defaultForm)
  const [saving, setSaving] = useState(false)

  const load = async () => {
    try {
      const [cardsRes, accountsRes] = await Promise.all([getCardsApi(), getAccountsApi()])
      setRows(cardsRes.data)
      setAccounts(accountsRes.data)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load')
    } finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const handleSave = async () => {
    setSaving(true)
    try {
      if (editing) {
        await updateCardApi(editing.id, form as any)
      } else {
        await createCardApi(form as any)
      }
      setDialogOpen(false)
      setEditing(null)
      setForm(defaultForm)
      await load()
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save')
    } finally { setSaving(false) }
  }

  const handleDelete = async (row: CreditCard) => {
    if (!confirm(`Deactivate card "${row.name}"?`)) return
    try {
      await deleteCardApi(row.id)
      await load()
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete')
    }
  }

  return (
    <>
      <CrudPage
        title="Credit Cards"
        columns={[
          { id: 'name', label: 'Name' },
          { id: 'brand', label: 'Brand' },
          { id: 'closingDay', label: 'Closing Day' },
          { id: 'dueDay', label: 'Due Day' },
          {
            id: 'creditLimit', label: 'Limit',
            render: (r: CreditCard) => r.creditLimit
              ? `$${r.creditLimit.toLocaleString()}`
              : '-',
          },
          { id: 'isActive', label: 'Active', render: (r: CreditCard) => r.isActive ? 'Yes' : 'No' },
        ]}
        rows={rows} loading={loading} error={error}
        onAdd={() => {
          setEditing(null)
          setForm({ ...defaultForm, accountId: accounts[0]?.id || '' })
          setDialogOpen(true)
        }}
        onEdit={(r) => {
          setEditing(r)
          setForm({
            accountId: r.accountId, name: r.name, brand: r.brand,
            closingDay: r.closingDay, dueDay: r.dueDay,
            creditLimit: r.creditLimit, colorHex: r.colorHex || '',
          })
          setDialogOpen(true)
        }}
        onDelete={handleDelete}
        getRowId={(r) => r.id}
      />

      <FormDialog
        open={dialogOpen} loading={saving}
        title={editing ? 'Edit Card' : 'New Card'}
        onClose={() => setDialogOpen(false)}
        onSubmit={handleSave}
      >
        <TextField fullWidth label="Name" margin="normal" required value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })} />
        <TextField fullWidth label="Account" margin="normal" select required value={form.accountId}
          onChange={(e) => setForm({ ...form, accountId: e.target.value })}>
          {accounts.filter(a => a.isActive).map(a => (
            <MenuItem key={a.id} value={a.id}>{a.name}</MenuItem>
          ))}
        </TextField>
        <TextField fullWidth label="Brand" margin="normal" select required value={form.brand}
          onChange={(e) => setForm({ ...form, brand: e.target.value })}>
          {['VISA', 'MASTERCARD', 'AMEX', 'NARANJA', 'CABAL'].map(b => (
            <MenuItem key={b} value={b}>{b}</MenuItem>
          ))}
        </TextField>
        <TextField fullWidth label="Closing Day" type="number" margin="normal" required
          slotProps={{ htmlInput: { min: 1, max: 31 } }} value={form.closingDay}
          onChange={(e) => setForm({ ...form, closingDay: Number(e.target.value) })} />
        <TextField fullWidth label="Due Day" type="number" margin="normal" required
          slotProps={{ htmlInput: { min: 1, max: 31 } }} value={form.dueDay}
          onChange={(e) => setForm({ ...form, dueDay: Number(e.target.value) })} />
        <TextField fullWidth label="Credit Limit" type="number" margin="normal" value={form.creditLimit ?? ''}
          onChange={(e) => setForm({ ...form, creditLimit: e.target.value ? Number(e.target.value) : null })} />
        <TextField fullWidth label="Color" margin="normal" value={form.colorHex}
          placeholder="#1976d2" onChange={(e) => setForm({ ...form, colorHex: e.target.value })} />
      </FormDialog>
    </>
  )
}
