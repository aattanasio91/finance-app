import { useState, useEffect, useRef } from 'react'
import Box from '@mui/material/Box'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import Typography from '@mui/material/Typography'
import Button from '@mui/material/Button'
import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import Chip from '@mui/material/Chip'
import UploadFileIcon from '@mui/icons-material/UploadFile'
import Skeleton from '@mui/material/Skeleton'
import Alert from '@mui/material/Alert'
import { uploadFileApi, getImportJobsApi } from '../api/imports'
import type { ImportJob } from '../types'

export default function ImportPage() {
  const [jobs, setJobs] = useState<ImportJob[]>([])
  const [loading, setLoading] = useState(true)
  const [uploading, setUploading] = useState(false)
  const [sourceType, setSourceType] = useState('CSV_BANK')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const fileRef = useRef<HTMLInputElement>(null)

  const load = async () => {
    try {
      const res = await getImportJobsApi()
      setJobs(res.data)
    } catch { } finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const handleUpload = async () => {
    const file = fileRef.current?.files?.[0]
    if (!file) return
    setUploading(true)
    setError('')
    setSuccess('')
    try {
      const res = await uploadFileApi(file, sourceType)
      setSuccess(`Imported ${res.data.successRows} of ${res.data.totalRows} rows`)
      await load()
      if (fileRef.current) fileRef.current.value = ''
    } catch (err: any) {
      setError(err.response?.data?.message || 'Upload failed')
    } finally { setUploading(false) }
  }

  const statusColor: Record<string, 'success' | 'error' | 'warning' | 'info' | 'default'> = {
    COMPLETED: 'success', FAILED: 'error', PARTIAL: 'warning',
    PROCESSING: 'info', PENDING: 'default',
  }

  if (loading) return <Skeleton variant="rectangular" height={400} />

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Import Transactions</Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>Upload CSV</Typography>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
            <TextField select value={sourceType}
              onChange={(e) => setSourceType(e.target.value)}
              label="Source" sx={{ minWidth: 150 }}>
              <MenuItem value="CSV_BANK">Bank CSV</MenuItem>
              <MenuItem value="CSV_CARD">Card CSV</MenuItem>
            </TextField>
            <Button variant="outlined" component="label">
              Choose File
              <input type="file" hidden ref={fileRef} accept=".csv" />
            </Button>
            <Button
              variant="contained" startIcon={<UploadFileIcon />}
              onClick={handleUpload} disabled={uploading}
            >
              {uploading ? 'Uploading...' : 'Upload'}
            </Button>
          </Box>
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>Import History</Typography>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell sx={{ fontWeight: 600 }}>File</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Source</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Status</TableCell>
                  <TableCell sx={{ fontWeight: 600 }} align="right">Total</TableCell>
                  <TableCell sx={{ fontWeight: 600 }} align="right">Success</TableCell>
                  <TableCell sx={{ fontWeight: 600 }} align="right">Errors</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>Date</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {jobs.map(j => (
                  <TableRow key={j.id}>
                    <TableCell>{j.fileName}</TableCell>
                    <TableCell>{j.sourceType}</TableCell>
                    <TableCell>
                      <Chip size="small" label={j.status} color={statusColor[j.status] || 'default'} />
                    </TableCell>
                    <TableCell align="right">{j.totalRows}</TableCell>
                    <TableCell align="right">{j.successRows}</TableCell>
                    <TableCell align="right">{j.errorRows}</TableCell>
                    <TableCell>{j.createdAt ? new Date(j.createdAt).toLocaleDateString() : '-'}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>
    </Box>
  )
}
