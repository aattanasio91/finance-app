export interface ApiResponse<T> {
  success: boolean
  message?: string
  data: T
}

export interface User {
  id: string
  name: string
  email: string
  defaultCurrency: string
  enabled: boolean
  createdAt: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  name: string
  email: string
  password: string
}

export interface AuthResult {
  token: string
  userId: string
}

export interface Account {
  id: string
  userId: string
  name: string
  type: 'CASH' | 'CHECKING' | 'SAVINGS' | 'CREDIT_CARD' | 'INVESTMENT'
  balance: number
  currency: string
  isActive: boolean
  createdAt: string
}

export interface Category {
  id: string
  name: string
  type: 'INCOME' | 'EXPENSE'
  isSystem: boolean
  createdAt: string
}

export interface CreditCard {
  id: string
  userId: string
  accountId: string
  name: string
  brand: 'VISA' | 'MASTERCARD' | 'AMEX' | 'NARANJA' | 'CABAL'
  closingDay: number
  dueDay: number
  creditLimit: number | null
  colorHex: string | null
  isActive: boolean
}

export interface Merchant {
  id: string
  name: string
  normalizedName: string
  categoryId: string | null
  isVerified: boolean
  createdAt: string
}

export interface Transaction {
  id: string
  userId: string
  accountId: string
  categoryId: string | null
  merchantId: string | null
  parentTransactionId: string | null
  amount: number
  originalAmount: number | null
  description: string
  date: string
  type: 'INCOME' | 'EXPENSE' | 'TRANSFER'
  currency: string
  isManual: boolean
  isRecurring: boolean
  notes: string | null
  createdAt: string
}

export interface Budget {
  id: string
  userId: string
  categoryId: string
  amount: number
  period: 'WEEKLY' | 'MONTHLY' | 'YEARLY' | 'CUSTOM'
  startDate: string
  endDate: string | null
  isActive: boolean
  createdAt: string
}

export interface BudgetSummary {
  id: string
  categoryId: string
  amount: number
  spent: number
  percentage: number
  status: 'OK' | 'WARNING' | 'EXCEEDED'
}

export interface RecurringExpense {
  id: string
  userId: string
  categoryId: string | null
  name: string
  amount: number
  dayOfMonth: number
  frequency: 'MONTHLY' | 'BIMONTHLY' | 'QUARTERLY' | 'YEARLY'
  isActive: boolean
  nextDate: string | null
  notes: string | null
  createdAt: string
}

export interface Installment {
  id: string
  transactionId: string
  currentInstallment: number
  totalInstallments: number
  dueDate: string
  amount: number
  isPaid: boolean
  paidDate: string | null
}

export interface ImportJob {
  id: string
  userId: string
  fileName: string
  fileHash: string | null
  sourceType: string
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'PARTIAL'
  totalRows: number
  successRows: number
  errorRows: number
  errorLog: string | null
  createdAt: string
}

export interface DashboardData {
  totalBalance: number
  monthlyIncome: number
  monthlyExpenses: number
  netSavings: number
}

export interface CategoryExpense {
  categoryId: string
  categoryName: string
  amount: number
  percentage: number
}

export interface MonthlyEvolution {
  year: number
  month: number
  income: number
  expenses: number
}
