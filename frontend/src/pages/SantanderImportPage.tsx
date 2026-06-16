import { useState, useEffect, useRef } from 'react'
import Box from '@mui/material/Box'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import Typography from '@mui/material/Typography'
import Button from '@mui/material/Button'
import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'
import Alert from '@mui/material/Alert'
import UploadFileIcon from '@mui/icons-material/UploadFile'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import Chip from '@mui/material/Chip'
import Skeleton from '@mui/material/Skeleton'
import { getImportJobsApi } from '../api/imports'
import { getAccountsApi } from '../api/accounts'
import { getCardsApi } from '../api/cards'
import {
  uploadSantanderBankPdfApi,
  uploadSantanderCardPdfApi,
  uploadSantanderCardExcelApi,
  uploadSantanderInstallmentsXlsApi,
} from '../api/santander'
import type { ImportJob, Account, CreditCard } from '../types'

type DocType = 'BANK_PDF' | 'CARD_PDF' | 'CARD_EXCEL' | 'INSTALLMENTS_XLS'

const DOC_LABELS: Record<DocType, string> = {
  BANK_PDF: 'Bank Account PDF',
  CARD_PDF: 'Card Statement PDF',
  CARD_EXCEL: 'Card Consumption Excel',
  INSTALLMENTS_XLS: 'Installments XLS',
}

const DOC_ACCEPTS: Record<DocType, string> = {
  BANK_PDF: '.pdf',
  CARD_PDF: '.pdf',
  CARD_EXCEL: '.xlsx',
  INSTALLMENTS_XLS: '.xls',
}

export default function SantanderImportPage() {
  const [jobs, setJobs] = useState<ImportJob[]>([])
  const [accounts, setAccounts] = useState<Account[]>([])
  const [cards, setCards] = useState<CreditCard[]>([])
  const [loading, setLoading] = useState(true)
  const [uploading, setUploading] = useState(false)
  const [docType, setDocType] = useState<DocType>('BANK_PDF')
  const [accountId, setAccountId] = useState('')
  const [cardId, setCardId] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const fileRef = useRef<HTMLInputElement>(null)

  const load = async () => {
    try {
      const [jobsRes, accsRes, cardsRes] = await Promise.all([
        getImportJobsApi(),
        getAccountsApi(),
        getCardsApi(),
      ])
      setJobs(jobsRes.data)
      setAccounts(accsRes.data.filter((a: Account) => a.isActive))
      setCards(cardsRes.data)
      if (accsRes.data.length > 0) setAccountId(accsRes.data[0].id)
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
      let res
      switch (docType) {
        case 'BANK_PDF':
          res = await uploadSantanderBankPdfApi(accountId, file)
          break
        case 'CARD_PDF':
          res = await uploadSantanderCardPdfApi(accountId, file)
          break
        case 'CARD_EXCEL':
          res = await uploadSantanderCardExcelApi(accountId, file)
          break
        case 'INSTALLMENTS_XLS':
          res = await uploadSantanderInstallmentsXlsApi(accountId, cardId, file)
          break
      }
      setSuccess(`Imported ${res!.data.successRows} of ${res!.data.totalRows} rows`)
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

  const santanderJobs = jobs.filter(j =>
    j.sourceType.startsWith('SANTANDER_')
  )

  if (loading) return <Skeleton variant="rectangular" height={400} />

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Santander Import</Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>Upload Santander Document</Typography>
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
            <TextField select value={docType}
              onChange={(e) => setDocType(e.target.value as DocType)}
              label="Document Type" sx={{ minWidth: 200 }}>
              {(Object.entries(DOC_LABELS) as [DocType, string][]).map(([k, v]) => (
                <MenuItem key={k} value={k}>{v}</MenuItem>
              ))}
            </TextField>
            <TextField select value={accountId}
              onChange={(e) => setAccountId(e.target.value)}
              label="Account" sx={{ minWidth: 180 }}>
              {accounts.map(a => (
                <MenuItem key={a.id} value={a.id}>{a.name}</MenuItem>
              ))}
            </TextField>
            {docType === 'INSTALLMENTS_XLS' && (
              <TextField select value={cardId}
                onChange={(e) => setCardId(e.target.value)}
                label="Card" sx={{ minWidth: 180 }}>
                {cards.map(c => (
                  <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>
                ))}
              </TextField>
            )}
            <Button variant="outlined" component="label">
              Choose File
              <input type="file" hidden ref={fileRef} accept={DOC_ACCEPTS[docType]} />
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
          <Typography variant="h6" gutterBottom>Santander Import History</Typography>
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
                {santanderJobs.map(j => (
                  <TableRow key={j.id}>
                    <TableCell>{j.fileName}</TableCell>
                    <TableCell>{j.sourceType.replace('SANTANDER_', '')}</TableCell>
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
