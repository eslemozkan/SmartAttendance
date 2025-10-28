'use client'

import { useState, useEffect } from 'react'
import { supabase } from '@/lib/supabase'
import { 
  Users, 
  GraduationCap, 
  BookOpen, 
  BarChart3, 
  Settings,
  LogOut,
  Plus,
  Eye
} from 'lucide-react'

export default function AdminDashboard() {
  const [admin, setAdmin] = useState<any>(null)
  const [stats, setStats] = useState({
    totalStudents: 0,
    totalTeachers: 0,
    totalCourses: 0,
    totalDepartments: 0,
    todayAttendance: 0
  })

  useEffect(() => {
    checkAuth()
    loadStats()
  }, [])

  const checkAuth = () => {
    const isAuthenticated = localStorage.getItem('isAuthenticated')
    const adminData = localStorage.getItem('admin')
    
    if (!isAuthenticated || !adminData) {
      window.location.href = '/login'
      return
    }
    
    setAdmin(JSON.parse(adminData))
  }

  const loadStats = async () => {
    try {
      // Öğrenci sayısı
      const { count: studentCount } = await supabase
        .from('students')
        .select('*', { count: 'exact', head: true })

      // Öğretmen sayısı
      const { count: teacherCount } = await supabase
        .from('profiles')
        .select('*', { count: 'exact', head: true })
        .eq('role', 'teacher')

      // Ders sayısı
      const { count: courseCount } = await supabase
        .from('courses')
        .select('*', { count: 'exact', head: true })

      // Bölüm sayısı
      const { count: deptCount } = await supabase
        .from('departments')
        .select('*', { count: 'exact', head: true })

      // Bugünkü yoklama sayısı
      const today = new Date().toISOString().split('T')[0]
      const { count: attendanceCount } = await supabase
        .from('attendances')
        .select('*', { count: 'exact', head: true })
        .gte('marked_at', `${today}T00:00:00`)

      setStats({
        totalStudents: studentCount || 0,
        totalTeachers: teacherCount || 0,
        totalCourses: courseCount || 0,
        totalDepartments: deptCount || 0,
        todayAttendance: attendanceCount || 0
      })
    } catch (error) {
      console.error('Stats yüklenirken hata:', error)
    }
  }

  const handleLogout = () => {
    localStorage.removeItem('admin')
    localStorage.removeItem('isAuthenticated')
    window.location.href = '/login'
  }

  const StatCard = ({ title, value, icon: Icon, color }: any) => (
    <div className="card">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-academic-text-secondary text-sm font-medium">{title}</p>
          <p className="text-2xl font-bold text-academic-text-primary">{value}</p>
        </div>
        <div className={`p-3 rounded-lg ${color}`}>
          <Icon className="w-6 h-6 text-white" />
        </div>
      </div>
    </div>
  )

  const ActionCard = ({ title, description, icon: Icon, onClick, color }: any) => (
    <div className="card cursor-pointer hover:shadow-md transition-shadow" onClick={onClick}>
      <div className="flex items-center space-x-4">
        <div className={`p-3 rounded-lg ${color}`}>
          <Icon className="w-6 h-6 text-white" />
        </div>
        <div>
          <h3 className="font-semibold text-academic-text-primary">{title}</h3>
          <p className="text-academic-text-secondary text-sm">{description}</p>
        </div>
      </div>
    </div>
  )

  return (
    <div className="min-h-screen bg-academic-background">
      {/* Header */}
      <header className="bg-academic-surface border-b border-academic-divider">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div>
              <h1 className="text-2xl font-bold text-academic-primary">Smart Attendance Admin</h1>
              <p className="text-academic-text-secondary">Yönetim Paneli</p>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-academic-text-primary">Hoş geldiniz, {admin?.email}</span>
              <button 
                onClick={handleLogout}
                className="btn-outline flex items-center space-x-2"
              >
                <LogOut className="w-4 h-4" />
                <span>Çıkış</span>
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6 mb-8">
          <StatCard 
            title="Toplam Öğrenci" 
            value={stats.totalStudents} 
            icon={Users} 
            color="bg-academic-primary" 
          />
          <StatCard 
            title="Toplam Öğretmen" 
            value={stats.totalTeachers} 
            icon={GraduationCap} 
            color="bg-academic-secondary" 
          />
          <StatCard 
            title="Toplam Ders" 
            value={stats.totalCourses} 
            icon={BookOpen} 
            color="bg-academic-success" 
          />
          <StatCard 
            title="Toplam Bölüm" 
            value={stats.totalDepartments} 
            icon={BarChart3} 
            color="bg-academic-error" 
          />
          <StatCard 
            title="Bugünkü Yoklama" 
            value={stats.todayAttendance} 
            icon={Eye} 
            color="bg-academic-primary-light text-academic-primary" 
          />
        </div>

        {/* Action Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <ActionCard
            title="Öğrenci Yönetimi"
            description="Öğrenci ekleme, düzenleme ve silme işlemleri"
            icon={Users}
            color="bg-academic-primary"
            onClick={() => window.location.href = '/admin/students'}
          />
          
          <ActionCard
            title="Öğretmen Yönetimi"
            description="Öğretmen ekleme, düzenleme ve silme işlemleri"
            icon={GraduationCap}
            color="bg-academic-secondary"
            onClick={() => window.location.href = '/admin/teachers'}
          />
          
          <ActionCard
            title="Ders Yönetimi"
            description="Ders ekleme, düzenleme ve sınıf atamaları"
            icon={BookOpen}
            color="bg-academic-success"
            onClick={() => window.location.href = '/admin/courses'}
          />
          
          <ActionCard
            title="Sınıf Yönetimi"
            description="Sınıf oluşturma ve öğrenci atamaları"
            icon={BarChart3}
            color="bg-academic-error"
            onClick={() => window.location.href = '/admin/classes'}
          />
          
          <ActionCard
            title="Yoklama Raporları"
            description="Detaylı yoklama raporları ve analizler"
            icon={Eye}
            color="bg-academic-primary-light text-academic-primary"
            onClick={() => window.location.href = '/admin/reports'}
          />
          
          <ActionCard
            title="Sistem Ayarları"
            description="Genel sistem ayarları ve konfigürasyon"
            icon={Settings}
            color="bg-academic-text-secondary"
            onClick={() => window.location.href = '/admin/settings'}
          />
        </div>
      </main>
    </div>
  )
}


