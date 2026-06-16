const DEBUG = import.meta.env.DEV

const logger = {
  debug: (...args: unknown[]) => {
    if (DEBUG) console.debug('[DEBUG]', ...args)
  },
  info: (...args: unknown[]) => {
    console.info('[INFO]', ...args)
  },
  warn: (...args: unknown[]) => {
    console.warn('[WARN]', ...args)
  },
  error: (...args: unknown[]) => {
    console.error('[ERROR]', ...args)
  },
}

export default logger
