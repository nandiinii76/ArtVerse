/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        paper: '#F2EBDD',
        ivory: '#E8E0D0',
        ink: '#1D1B18',
        charcoal: '#292722',
        oxblood: '#642F2F',
        olive: '#555746',
        brass: '#8A7651',
      },
      fontFamily: {
        serif: ['"Cormorant Garamond"', '"EB Garamond"', 'Georgia', 'serif'],
        sans: ['Inter', '"Source Sans Pro"', 'system-ui', 'sans-serif'],
      },
      letterSpacing: {
        wideish: '0.08em',
      },
    },
  },
  plugins: [],
}
