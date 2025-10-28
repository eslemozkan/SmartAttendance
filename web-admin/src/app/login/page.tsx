'use client'

import { useState } from 'react'
import { supabase } from '@/lib/supabase'
import { GraduationCap, Eye, EyeOff } from 'lucide-react'

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    try {
      // Basit email/şifre kontrolü (demo için)
      if (email === 'admin@university.edu' && password === 'admin123') {
        // Admin bilgilerini localStorage'a kaydet
        const adminData = {
          id: '1',
          email: 'admin@university.edu',
          full_name: 'Sistem Yöneticisi',
          role: 'super_admin'
        }
        
        localStorage.setItem('admin', JSON.stringify(adminData))
        localStorage.setItem('isAuthenticated', 'true')
        
        // Dashboard'a yönlendir
        window.location.href = '/'
      } else {
        setError('Geçersiz email veya şifre')
      }
      
    } catch (error) {
      console.error('Login hatası:', error)
      setError('Giriş yapılırken bir hata oluştu')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-academic-background flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 bg-academic-primary rounded-lg flex items-center justify-center">
            <GraduationCap className="h-8 w-8 text-white" />
          </div>
          <h2 className="mt-6 text-3xl font-bold text-academic-text-primary">
            Admin Girişi
          </h2>
          <p className="mt-2 text-sm text-academic-text-secondary">
            Smart Attendance Yönetim Paneli
          </p>
        </div>

        <form className="mt-8 space-y-6" onSubmit={handleLogin}>
          <div className="space-y-4">
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-academic-text-primary">
                Email Adresi
              </label>
              <input
                id="email"
                name="email"
                type="email"
                autoComplete="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="input-field mt-1"
                placeholder="admin@university.edu"
              />
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-academic-text-primary">
                Şifre
              </label>
              <div className="mt-1 relative">
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="input-field pr-10"
                  placeholder="••••••••"
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 pr-3 flex items-center"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? (
                    <EyeOff className="h-5 w-5 text-academic-text-secondary" />
                  ) : (
                    <Eye className="h-5 w-5 text-academic-text-secondary" />
                  )}
                </button>
              </div>
            </div>
          </div>

          {error && (
            <div className="bg-academic-error bg-opacity-10 border border-academic-error text-academic-error px-4 py-3 rounded-lg">
              {error}
            </div>
          )}

          <div>
            <button
              type="submit"
              disabled={loading}
              className="btn-primary w-full flex justify-center py-3 px-4 text-sm font-medium disabled:opacity-50"
            >
              {loading ? 'Giriş yapılıyor...' : 'Giriş Yap'}
            </button>
          </div>

          <div className="text-center">
            <p className="text-sm text-academic-text-secondary">
              Demo hesap: admin@university.edu / admin123
            </p>
          </div>
        </form>
      </div>
    </div>
  )
}


