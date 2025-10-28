module.exports = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        // Academic Color Palette (Android uygulamasıyla aynı)
        'academic-primary': '#1976D2',
        'academic-primary-dark': '#1565C0',
        'academic-primary-light': '#BBDEFB',
        'academic-secondary': '#424242',
        'academic-secondary-dark': '#212121',
        'academic-secondary-light': '#757575',
        'academic-background': '#FAFAFA',
        'academic-surface': '#FFFFFF',
        'academic-card': '#FFFFFF',
        'academic-text-primary': '#212121',
        'academic-text-secondary': '#757575',
        'academic-text-hint': '#BDBDBD',
        'academic-divider': '#E0E0E0',
        'academic-error': '#F44336',
        'academic-success': '#4CAF50',
      },
    },
  },
  plugins: [],
}


