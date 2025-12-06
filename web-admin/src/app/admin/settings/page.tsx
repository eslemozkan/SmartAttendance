'use client'

import { useEffect, useState } from 'react'
import { supabase } from '@/lib/supabase'
import { ArrowLeft, Save, Database, Users, GraduationCap, BookOpen, Calendar, Clock, Info } from 'lucide-react'

export default function SettingsPage() {
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [stats, setStats] = useState({
    totalStudents: 0,
    totalTeachers: 0,
    totalCourses: 0,
    totalClasses: 0,
    totalDepartments: 0,
    totalAttendances: 0
  })
  const [settings, setSettings] = useState({
    currentAcademicYear: '',
    currentSemester: 'Güz'
  })

  useEffect(() => {
    loadData()
  }, [])

  async function loadData() {
    setLoading(true)
    try {
      // Load statistics
      const [
        { count: studentsCount },
        { count: teachersCount },
        { count: coursesCount },
        { count: classesCount },
        { count: departmentsCount },
        { count: attendancesCount }
      ] = await Promise.all([
        supabase.from('students').select('*', { count: 'exact', head: true }),
        supabase.from('profiles').select('*', { count: 'exact', head: true }).eq('role', 'teacher'),
        supabase.from('courses').select('*', { count: 'exact', head: true }),
        supabase.from('classes').select('*', { count: 'exact', head: true }),
        supabase.from('departments').select('*', { count: 'exact', head: true }),
        supabase.from('attendances').select('*', { count: 'exact', head: true })
      ])

      setStats({
        totalStudents: studentsCount || 0,
        totalTeachers: teachersCount || 0,
        totalCourses: coursesCount || 0,
        totalClasses: classesCount || 0,
        totalDepartments: departmentsCount || 0,
        totalAttendances: attendancesCount || 0
      })

      // Load current academic year from classes (most recent)
      const { data: classesData } = await supabase
        .from('classes')
        .select('academic_year')
        .order('academic_year', { ascending: false })
        .limit(1)

      if (classesData && classesData.length > 0) {
        setSettings(prev => ({
          ...prev,
          currentAcademicYear: classesData[0].academic_year || getCurrentAcademicYear()
        }))
      } else {
        setSettings(prev => ({
          ...prev,
          currentAcademicYear: getCurrentAcademicYear()
        }))
      }
    } catch (error: any) {
      console.error('Error loading data:', error)
    } finally {
      setLoading(false)
    }
  }

  function getCurrentAcademicYear(): string {
    const now = new Date()
    const year = now.getFullYear()
    const month = now.getMonth() + 1
    
    if (month >= 9) {
      return `${year}-${year + 1}`
    } else {
      return `${year - 1}-${year}`
    }
  }

  async function handleSave() {
    setSaving(true)
    try {
      // Settings would typically be stored in a settings table
      // For now, we'll just show a success message
      // In a real app, you'd save these to a database
      
      alert('Ayarlar kaydedildi! (Not: Bu ayarlar şu anda veritabanına kaydedilmiyor, sadece gösterim amaçlı)')
      
      // TODO: Implement actual settings save to database
      // const { error } = await supabase
      //   .from('settings')
      //   .upsert({ ...settings, updated_at: new Date().toISOString() })
      
    } catch (error: any) {
      console.error('Error saving settings:', error)
      alert('Ayarlar kaydedilirken hata: ' + error.message)
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-academic-background flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-academic-primary mx-auto"></div>
          <p className="mt-4 text-academic-text-secondary">Yükleniyor...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-academic-background">
      {/* Header */}
      <header className="bg-academic-surface border-b border-academic-divider">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center space-x-4">
              <button onClick={() => (window.location.href = '/')} className="btn-outline flex items-center space-x-2">
                <ArrowLeft className="w-4 h-4" />
                <span>Geri</span>
              </button>
              <div>
                <h1 className="text-2xl font-bold text-academic-primary">Sistem Ayarları</h1>
                <p className="text-academic-text-secondary">Genel sistem ayarları ve konfigürasyon</p>
              </div>
            </div>
            <button onClick={handleSave} disabled={saving} className="btn-primary flex items-center space-x-2">
              <Save className="w-4 h-4" />
              <span>{saving ? 'Kaydediliyor...' : 'Kaydet'}</span>
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Column - Settings */}
          <div className="lg:col-span-2 space-y-6">
            {/* Academic Settings */}
            <div className="card">
              <div className="flex items-center space-x-2 mb-4">
                <Calendar className="w-5 h-5 text-academic-primary" />
                <h2 className="text-lg font-semibold text-academic-text-primary">Akademik Ayarlar</h2>
              </div>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-academic-text-primary mb-1">
                    Mevcut Akademik Yıl
                  </label>
                  <input
                    type="text"
                    value={settings.currentAcademicYear}
                    onChange={e => setSettings({ ...settings, currentAcademicYear: e.target.value })}
                    className="input-field"
                    placeholder="2024-2025"
                  />
                  <p className="text-xs text-academic-text-secondary mt-1">
                    Format: YYYY-YYYY (örn: 2024-2025)
                  </p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-academic-text-primary mb-1">
                    Mevcut Dönem
                  </label>
                  <select
                    value={settings.currentSemester}
                    onChange={e => setSettings({ ...settings, currentSemester: e.target.value })}
                    className="input-field"
                  >
                    <option value="Güz">Güz</option>
                    <option value="Bahar">Bahar</option>
                    <option value="Yaz">Yaz</option>
                  </select>
                </div>
              </div>
            </div>


            {/* System Info */}
            <div className="card">
              <div className="flex items-center space-x-2 mb-4">
                <Info className="w-5 h-5 text-academic-primary" />
                <h2 className="text-lg font-semibold text-academic-text-primary">Sistem Bilgileri</h2>
              </div>
              <div className="space-y-3">
                <div className="flex items-center justify-between p-3 bg-academic-surface rounded-md">
                  <span className="text-sm text-academic-text-secondary">Veritabanı Durumu</span>
                  <span className="px-2 py-1 rounded-full bg-academic-success-light text-academic-success text-xs font-medium">
                    Bağlı
                  </span>
                </div>
                <div className="flex items-center justify-between p-3 bg-academic-surface rounded-md">
                  <span className="text-sm text-academic-text-secondary">Supabase URL</span>
                  <span className="text-xs text-academic-text-primary font-mono">
                    oubvhffqbsxsnbtinzbl.supabase.co
                  </span>
                </div>
                <div className="flex items-center justify-between p-3 bg-academic-surface rounded-md">
                  <span className="text-sm text-academic-text-secondary">Sistem Versiyonu</span>
                  <span className="text-xs text-academic-text-primary">v1.0.0</span>
                </div>
              </div>
            </div>
          </div>

          {/* Right Column - Statistics */}
          <div className="space-y-6">
            <div className="card">
              <h2 className="text-lg font-semibold text-academic-text-primary mb-4">Sistem İstatistikleri</h2>
              <div className="space-y-4">
                <div className="flex items-center space-x-3 p-3 bg-academic-surface rounded-md">
                  <div className="p-2 bg-academic-primary-light rounded-lg">
                    <Users className="w-5 h-5 text-academic-primary" />
                  </div>
                  <div className="flex-1">
                    <p className="text-sm text-academic-text-secondary">Toplam Öğrenci</p>
                    <p className="text-2xl font-bold text-academic-text-primary">{stats.totalStudents}</p>
                  </div>
                </div>

                <div className="flex items-center space-x-3 p-3 bg-academic-surface rounded-md">
                  <div className="p-2 bg-academic-secondary-light rounded-lg">
                    <GraduationCap className="w-5 h-5 text-academic-secondary" />
                  </div>
                  <div className="flex-1">
                    <p className="text-sm text-academic-text-secondary">Toplam Öğretmen</p>
                    <p className="text-2xl font-bold text-academic-text-primary">{stats.totalTeachers}</p>
                  </div>
                </div>

                <div className="flex items-center space-x-3 p-3 bg-academic-surface rounded-md">
                  <div className="p-2 bg-academic-success-light rounded-lg">
                    <BookOpen className="w-5 h-5 text-academic-success" />
                  </div>
                  <div className="flex-1">
                    <p className="text-sm text-academic-text-secondary">Toplam Ders</p>
                    <p className="text-2xl font-bold text-academic-text-primary">{stats.totalCourses}</p>
                  </div>
                </div>

                <div className="flex items-center space-x-3 p-3 bg-academic-surface rounded-md">
                  <div className="p-2 bg-academic-error-light rounded-lg">
                    <Database className="w-5 h-5 text-academic-error" />
                  </div>
                  <div className="flex-1">
                    <p className="text-sm text-academic-text-secondary">Toplam Sınıf</p>
                    <p className="text-2xl font-bold text-academic-text-primary">{stats.totalClasses}</p>
                  </div>
                </div>

                <div className="flex items-center space-x-3 p-3 bg-academic-surface rounded-md">
                  <div className="p-2 bg-academic-primary-light rounded-lg">
                    <Database className="w-5 h-5 text-academic-primary" />
                  </div>
                  <div className="flex-1">
                    <p className="text-sm text-academic-text-secondary">Toplam Bölüm</p>
                    <p className="text-2xl font-bold text-academic-text-primary">{stats.totalDepartments}</p>
                  </div>
                </div>

                <div className="flex items-center space-x-3 p-3 bg-academic-surface rounded-md">
                  <div className="p-2 bg-academic-success-light rounded-lg">
                    <Calendar className="w-5 h-5 text-academic-success" />
                  </div>
                  <div className="flex-1">
                    <p className="text-sm text-academic-text-secondary">Toplam Yoklama</p>
                    <p className="text-2xl font-bold text-academic-text-primary">{stats.totalAttendances}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}

