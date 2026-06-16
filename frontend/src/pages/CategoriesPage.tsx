import { useState, useEffect } from 'react'
import TextField from '@mui/material/TextField'
import MenuItem from '@mui/material/MenuItem'
import { getCategoriesApi, createCategoryApi, updateCategoryApi, deleteCategoryApi } from '../api/categories'
import { useAuth } from '../contexts/AuthContext'
import CrudPage, { FormDialog } from '../components/CrudPage'
import type { Category } from '../types'

const defaultForm = { name: '', type: 'EXPENSE' as const }

export default function CategoriesPage() {
  const { token } = useAuth()
  const [rows, setRows] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState<Category | null>(null)
  const [form, setForm] = useState(defaultForm)
  const [saving, setSaving] = useState(false)

  const load = async () => {
    try {
      const res = await getCategoriesApi()
      setRows(res.data)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [token])

  const handleSave = async () => {
    setSaving(true)
    try {
      if (editing) {
        await updateCategoryApi(editing.id, form)
      } else {
        await createCategoryApi(form)
      }
      setDialogOpen(false)
      setEditing(null)
      setForm(defaultForm)
      await load()
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (row: Category) => {
    if (!confirm(`Delete category "${row.name}"?`)) return
    try {
      await deleteCategoryApi(row.id)
      await load()
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete')
    }
  }

  const openEdit = (row: Category) => {
    setEditing(row)
    setForm({ name: row.name, type: row.type as any })
    setDialogOpen(true)
  }

  const openAdd = () => {
    setEditing(null)
    setForm(defaultForm)
    setDialogOpen(true)
  }

  return (
    <>
      <CrudPage
        title="Categories"
        columns={[
          { id: 'name', label: 'Name' },
          { id: 'type', label: 'Type' },
          {
            id: 'isSystem', label: 'System',
            render: (r: Category) => r.isSystem ? 'Yes' : 'No',
          },
        ]}
        rows={rows} loading={loading} error={error}
        onAdd={openAdd} onEdit={openEdit} onDelete={handleDelete}
        getRowId={(r) => r.id}
      />

      <FormDialog
        open={dialogOpen} loading={saving}
        title={editing ? 'Edit Category' : 'New Category'}
        onClose={() => setDialogOpen(false)}
        onSubmit={handleSave}
      >
        <TextField
          fullWidth label="Name" margin="normal" required
          value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
        />
        <TextField
          fullWidth label="Type" margin="normal" select required
          value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value as any })}
        >
          <MenuItem value="INCOME">Income</MenuItem>
          <MenuItem value="EXPENSE">Expense</MenuItem>
        </TextField>
      </FormDialog>
    </>
  )
}
