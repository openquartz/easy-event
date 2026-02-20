import { createI18n } from 'vue-i18n'
import zh from '../locales/zh'
import en from '../locales/en'

const i18n = createI18n({
  locale: 'zh', // set locale
  fallbackLocale: 'en', // set fallback locale
  legacy: false, // you must set `false`, to use Composition API
  messages: {
    zh,
    en,
  },
})

export default i18n
