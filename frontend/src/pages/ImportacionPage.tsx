import { useState, useEffect, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import Box from '@mui/material/Box'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import Typography from '@mui/material/Typography'
import Button from '@mui/material/Button'
import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'
import Tabs from '@mui/material/Tabs'
import Tab from '@mui/material/Tab'
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
  BANK_PDF: 'Resumen de cuenta (PDF)',
  CARD_PDF: 'Resumen de tarjeta (PDF)',
  CARD_EXCEL: 'Últimos consumos (Excel)',
  INSTALLMENTS_XLS: 'Cuotas pendientes (Excel)',
}

const DOC_ACCEPTS: Record<DocType, string> = {
  BANK_PDF: '.pdf',
  CARD_PDF: '.pdf',
  CARD_EXCEL: '.xlsx',
  INSTALLMENTS_XLS: '.xls',
}

export default function ImportacionPage() {
  const { t } = useTranslation()
  const [tab, setTab] = useState(0)
  const [jobs, setJobs] = useState<ImportJob[]>([])
  const [accounts, setAccounts] = useState<Account[]>([])
  const [cards, setCards] = useState<CreditCard[]>([])
  const [loading, setLoading] = useState(true)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const [csvSourceType, setCsvSourceType] = useState('CSV_BANK')
  const [docType, setDocType] = useState<DocType>('BANK_PDF')
  const [accountId, setAccountId] = useState('')
  const [cardId, setCardId] = useState('')
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
      if (accsRes.data.length > 0 && !accountId) setAccountId(accsRes.data[0].id)
    } catch { } finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const handleUpload = async () => {
    const file = fileRef.current?.files?.[0]
    if (!file) return
    if (!accountId) { setError('Seleccioná una cuenta antes de importar'); return }
    setUploading(true)
    setError('')
    setSuccess('')
    try {
      let res
      if (tab === 0) {
        res = await uploadFileApi(file, csvSourceType, accountId)
      } else {
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
      }
      setSuccess(`Importados ${res!.data.successRows} de ${res!.data.totalRows} registros`)
      await load()
      if (fileRef.current) fileRef.current.value = ''
    } catch (err: any) {
      const msg = err.response?.data?.message || err.response?.data?.error || err.message || 'Error al subir archivo'
      setError(msg)
    } finally { setUploading(false) }
  }

  const statusColor: Record<string, 'success' | 'error' | 'warning' | 'info' | 'default'> = {
    COMPLETED: 'success', FAILED: 'error', PARTIAL: 'warning',
    PROCESSING: 'info', PENDING: 'default',
  }

  if (loading) return <Skeleton variant="rectangular" height={400} />

  return (
    <Box>
      <Typography variant="h4" gutterBottom>{t('page.import.title')}</Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Tabs value={tab} onChange={(_, v) => { setTab(v); setError(''); setSuccess('') }} sx={{ mb: 2 }}>
            <Tab label={t('page.import.tabCsv')} />
            <Tab label={t('page.import.tabSantander')} />
          </Tabs>

          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

          {tab === 0 && (
            <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
              <TextField select value={csvSourceType}
                onChange={(e) => setCsvSourceType(e.target.value)}
                label={t('page.import.source')} sx={{ minWidth: 150 }}>
                <MenuItem value="CSV_BANK">CSV Bancario</MenuItem>
                <MenuItem value="CSV_CARD">CSV Tarjeta</MenuItem>
              </TextField>
              <TextField select value={accountId}
                onChange={(e) => setAccountId(e.target.value)}
                label={t('page.import.account')} sx={{ minWidth: 180 }}>
                {accounts.map(a => (
                  <MenuItem key={a.id} value={a.id}>{a.name}</MenuItem>
                ))}
              </TextField>
              <Button variant="outlined" component="label">
                {t('page.import.chooseFile')}
                <input type="file" hidden ref={fileRef} accept=".csv" />
              </Button>
              <Button
                variant="contained" startIcon={<UploadFileIcon />}
                onClick={handleUpload} disabled={uploading}
              >
                {uploading ? t('page.import.uploading') : t('page.import.upload')}
              </Button>
            </Box>
          )}

          {tab === 1 && (
            <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
              <TextField select value={docType}
                onChange={(e) => setDocType(e.target.value as DocType)}
                label={t('page.import.docType')} sx={{ minWidth: 200 }}>
                {(Object.entries(DOC_LABELS) as [DocType, string][]).map(([k, v]) => (
                  <MenuItem key={k} value={k}>{v}</MenuItem>
                ))}
              </TextField>
              <TextField select value={accountId}
                onChange={(e) => setAccountId(e.target.value)}
                label={t('page.import.account')} sx={{ minWidth: 180 }}>
                {accounts.map(a => (
                  <MenuItem key={a.id} value={a.id}>{a.name}</MenuItem>
                ))}
              </TextField>
              {docType === 'INSTALLMENTS_XLS' && (
                <TextField select value={cardId}
                  onChange={(e) => setCardId(e.target.value)}
                  label={t('page.import.card')} sx={{ minWidth: 180 }}>
                  {cards.map(c => (
                    <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>
                  ))}
                </TextField>
              )}
              <Button variant="outlined" component="label">
                {t('page.import.chooseFile')}
                <input type="file" hidden ref={fileRef} accept={DOC_ACCEPTS[docType]} />
              </Button>
              <Button
                variant="contained" startIcon={<UploadFileIcon />}
                onClick={handleUpload} disabled={uploading}
              >
                {uploading ? t('page.import.uploading') : t('page.import.upload')}
              </Button>
            </Box>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>{t('page.import.history')}</Typography>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell sx={{ fontWeight: 600 }}>{t('page.import.file')}</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>{t('page.import.source')}</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>{t('page.import.status')}</TableCell>
                  <TableCell sx={{ fontWeight: 600 }} align="right">{t('page.import.total')}</TableCell>
                  <TableCell sx={{ fontWeight: 600 }} align="right">{t('page.import.success')}</TableCell>
                  <TableCell sx={{ fontWeight: 600 }} align="right">{t('page.import.errors')}</TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>{t('page.import.date')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {jobs.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={7} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                      {t('page.import.noData')}
                    </TableCell>
                  </TableRow>
                )}
                {jobs.map(j => (
                  <TableRow key={j.id}>
                    <TableCell>{j.fileName}</TableCell>
                    <TableCell>{j.sourceType.replace('SANTANDER_', '').replace('CSV_', '')}</TableCell>
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
