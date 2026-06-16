import { type ReactNode } from 'react'
import Box from '@mui/material/Box'
import Card from '@mui/material/Card'
import Typography from '@mui/material/Typography'
import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import DialogTitle from '@mui/material/DialogTitle'
import DialogContent from '@mui/material/DialogContent'
import DialogActions from '@mui/material/DialogActions'
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
import Alert from '@mui/material/Alert'

interface Column<T> {
  id: keyof T | string
  label: string
  render?: (row: T) => ReactNode
  width?: number
}

interface CrudPageProps<T> {
  title: string
  columns: Column<T>[]
  rows: T[]
  loading: boolean
  error?: string
  onAdd: () => void
  onEdit: (row: T) => void
  onDelete: (row: T) => void
  addLabel?: string
  getRowId: (row: T) => string
}

export default function CrudPage<T>({
  title, columns, rows, loading, error,
  onAdd, onEdit, onDelete, addLabel,
  getRowId,
}: CrudPageProps<T>) {
  if (loading) return <Skeleton variant="rectangular" height={400} />

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4">{title}</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={onAdd}>
          {addLabel || `Add ${title}`}
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                {columns.map((col) => (
                  <TableCell key={String(col.id)} sx={{ fontWeight: 600, width: col.width }}>
                    {col.label}
                  </TableCell>
                ))}
                <TableCell sx={{ fontWeight: 600 }} width={100}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={columns.length + 1} align="center">
                    No data found
                  </TableCell>
                </TableRow>
              ) : (
                rows.map((row) => (
                  <TableRow key={getRowId(row)}>
                    {columns.map((col) => (
                      <TableCell key={String(col.id)}>
                        {col.render
                          ? col.render(row)
                          : String(row[col.id as keyof T] ?? '')}
                      </TableCell>
                    ))}
                    <TableCell>
                      <IconButton size="small" onClick={() => onEdit(row)} color="primary">
                        <EditIcon fontSize="small" />
                      </IconButton>
                      <IconButton size="small" onClick={() => onDelete(row)} color="error">
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>
    </Box>
  )
}

export function FormDialog({
  open, title, children, onClose, onSubmit, loading,
}: {
  open: boolean
  title: string
  children: ReactNode
  onClose: () => void
  onSubmit: () => void
  loading?: boolean
}) {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>{children}</DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" onClick={onSubmit} disabled={loading}>
          {loading ? 'Saving...' : 'Save'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
