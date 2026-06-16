import { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import Box from '@mui/material/Box'
import Card from '@mui/material/Card'
import Typography from '@mui/material/Typography'
import Button from '@mui/material/Button'
import FormControl from '@mui/material/FormControl'
import InputLabel from '@mui/material/InputLabel'
import Select from '@mui/material/Select'
import MenuItem from '@mui/material/MenuItem'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import Chip from '@mui/material/Chip'
import Skeleton from '@mui/material/Skeleton'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import RadioButtonUncheckedIcon from '@mui/icons-material/RadioButtonUnchecked'
import { getAllInstallmentsApi, getInstallmentsByCardApi, markInstallmentAsPaidApi } from '../api/installments'
import { getCardsApi } from '../api/cards'
import type { Installment, CreditCard } from '../types'

export default function InstallmentsPage() {
  const { t } = useTranslation()
  const [installments, setInstallments] = useState<Installment[]>([])
  const [cards, setCards] = useState<CreditCard[]>([])
  const [selectedCardId, setSelectedCardId] = useState<string>('')
  const [loading, setLoading] = useState(true)
  const [actionLoading, setActionLoading] = useState<string | null>(null)

  const load = async (cardId: string) => {
    setLoading(true)
    try {
      const [c] = await Promise.all([getCardsApi()])
      setCards(c.data)
      if (cardId) {
        const r = await getInstallmentsByCardApi(cardId)
        setInstallments(r.data)
      } else {
        const r = await getAllInstallmentsApi()
        setInstallments(r.data)
      }
    } finally { setLoading(false) }
  }

  useEffect(() => { load(selectedCardId) }, [])

  const handleCardFilter = async (cardId: string) => {
    setSelectedCardId(cardId)
    setLoading(true)
    try {
      if (cardId) {
        const r = await getInstallmentsByCardApi(cardId)
        setInstallments(r.data)
      } else {
        const r = await getAllInstallmentsApi()
        setInstallments(r.data)
      }
    } finally { setLoading(false) }
  }

  const handleMarkAsPaid = async (id: string) => {
    setActionLoading(id)
    try {
      await markInstallmentAsPaidApi(id)
      await handleCardFilter(selectedCardId)
    } finally { setActionLoading(null) }
  }

  const formatCurrency = (n: number) =>
    new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', minimumFractionDigits: 0 }).format(n)

  const formatDate = (d: string) => {
    const date = new Date(d)
    return date.toLocaleDateString('es-AR')
  }

  if (loading) return <Skeleton variant="rectangular" height={400} />

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4">{t('page.installments.title')}</Typography>
      </Box>

      <Card sx={{ mb: 3, p: 2 }}>
        <FormControl fullWidth>
          <InputLabel>{t('page.installments.filterCard')}</InputLabel>
          <Select
            value={selectedCardId}
            label={t('page.installments.filterCard')}
            onChange={(e) => handleCardFilter(e.target.value)}
          >
            <MenuItem value="">{t('page.installments.allCards')}</MenuItem>
            {cards.filter(c => c.isActive).map(card => (
              <MenuItem key={card.id} value={card.id}>
                {card.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Card>

      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: 600 }}>{t('page.installments.dueDate')}</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>{t('page.installments.installment')}</TableCell>
                <TableCell sx={{ fontWeight: 600 }} align="right">{t('page.installments.amount')}</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>{t('page.installments.status')}</TableCell>
                <TableCell sx={{ fontWeight: 600 }} width={100}>{t('page.installments.action')}</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {installments.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                    {t('page.installments.noData')}
                  </TableCell>
                </TableRow>
              ) : installments.map(row => (
                <TableRow key={row.id} sx={{
                  backgroundColor: !row.isPaid && new Date(row.dueDate) < new Date()
                    ? 'error.lighter'
                    : undefined,
                  '&:hover': { backgroundColor: 'action.hover' },
                }}>
                  <TableCell>{formatDate(row.dueDate)}</TableCell>
                  <TableCell>{row.currentInstallment}/{row.totalInstallments}</TableCell>
                  <TableCell align="right">{formatCurrency(row.amount)}</TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      icon={row.isPaid ? <CheckCircleIcon /> : <RadioButtonUncheckedIcon />}
                      label={row.isPaid ? t('page.installments.paid') : t('page.installments.pending')}
                      color={row.isPaid ? 'success' : 'warning'}
                    />
                  </TableCell>
                  <TableCell>
                    {!row.isPaid && (
                      <Button
                        size="small"
                        variant="outlined"
                        disabled={actionLoading === row.id}
                        onClick={() => handleMarkAsPaid(row.id)}
                      >
                        {actionLoading === row.id ? '...' : t('page.installments.pay')}
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>
    </Box>
  )
}
