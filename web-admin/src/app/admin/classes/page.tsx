'use client'

import { useEffect, useMemo, useState } from 'react'
import { supabase } from '@/lib/supabase'
import { ArrowLeft, Plus, Check, X, Edit, Trash2 } from 'lucide-react'

type Department = { id: string; name: string; code: string }
type Class = { id: string; name: string; department_id: string; academic_year: string; created_at: string }

export default function ClassesManagementPage() {
  const [departments, setDepartments] = useState<Department[]>([])
  const [selectedDeptId, setSelectedDeptId] = useState<string>('')
  const [classes, setClasses] = useState<Class[]>([])
  const [loading, setLoading] = useState(true)
  const [creatingClass, setCreatingClass] = useState(false)
  const [editingClass, setEditingClass] = useState<Class | null>(null)
  const [newClass, setNewClass] = useState({ name: '', academic_year: '' })

  useEffect(() => {
    loadDepartments()
  }, [])

  useEffect(() => {
    if (selectedDeptId) {
      loadClasses(selectedDeptId)
    } else {
      setClasses([])
    }
  }, [selectedDeptId])

  async function loadDepartments() {
    setLoading(true)
    const { data } = await supabase.from('departments').select('*').order('name')
    setDepartments(data || [])
    setLoading(false)
  }

  async function loadClasses(departmentId: string) {
    setLoading(true)
    const { data } = await supabase
      .from('classes')
      .select('*')
      .eq('department_id', departmentId)
      .order('name')
    setClasses(data || [])
    setLoading(false)
  }

  async function addClass(departmentId: string) {
    if (!newClass.name.trim()) {
      alert('Lütfen sınıf adı girin (örn: 1-A, 2-B)')
      return
    }
    if (!newClass.academic_year.trim()) {
      alert('Lütfen akademik yıl girin (örn: 2024-2025)')
      return
    }

    const { error } = await supabase.from('classes').insert([
      { 
        name: newClass.name.trim(), 
        academic_year: newClass.academic_year.trim(), 
        department_id: departmentId 
      },
    ])
    if (error) {
      alert('Sınıf eklenemedi: ' + error.message)
      return
    }
    setNewClass({ name: '', academic_year: '' })
    setCreatingClass(false)
    await loadClasses(departmentId)
  }

  async function updateClass(classId: string, departmentId: string) {
    if (!newClass.name.trim()) {
      alert('Lütfen sınıf adı girin (örn: 1-A, 2-B)')
      return
    }
    if (!newClass.academic_year.trim()) {
      alert('Lütfen akademik yıl girin (örn: 2024-2025)')
      return
    }

    const { error } = await supabase
      .from('classes')
      .update({ 
        name: newClass.name.trim(), 
        academic_year: newClass.academic_year.trim() 
      })
      .eq('id', classId)
    if (error) {
      alert('Sınıf güncellenemedi: ' + error.message)
      return
    }
    setNewClass({ name: '', academic_year: '' })
    setEditingClass(null)
    setCreatingClass(false)
    await loadClasses(departmentId)
  }

  function handleEdit(cls: Class) {
    setEditingClass(cls)
    setNewClass({ name: cls.name, academic_year: cls.academic_year })
    setCreatingClass(false)
  }

  function handleCancelEdit() {
    setEditingClass(null)
    setNewClass({ name: '', academic_year: '' })
    setCreatingClass(false)
  }

  async function handleDelete(classId: string) {
    if (!confirm('Bu sınıfı silmek istediğinizden emin misiniz? Bu işlem geri alınamaz ve bu sınıftaki öğrencilerin sınıf bilgisi silinecektir.')) return

    try {
      // Önce öğrencilerin class_id'sini null yap (veya silebiliriz, ama null daha güvenli)
      const { error: studentError } = await supabase
        .from('students')
        .update({ class_id: null })
        .eq('class_id', classId)
      
      if (studentError) {
        console.warn('Öğrenci güncellemesi sırasında hata:', studentError)
      }

      // Sonra sınıfı sil
      const { error } = await supabase
        .from('classes')
        .delete()
        .eq('id', classId)

      if (error) throw error
      
      if (selectedDeptId) await loadClasses(selectedDeptId)
    } catch (error: any) {
      console.error('Sınıf silinirken hata:', error)
      alert('Hata: ' + error.message)
    }
  }

  const selectedDept = useMemo(() => departments.find(d => d.id === selectedDeptId), [departments, selectedDeptId])

  // Get current academic year as default (e.g., 2024-2025)
  const getCurrentAcademicYear = () => {
    const now = new Date()
    const year = now.getFullYear()
    const month = now.getMonth() + 1 // 0-indexed
    
    // If we're in the first half of the year (Jan-Aug), use previous year - current year
    // If we're in the second half (Sep-Dec), use current year - next year
    if (month >= 9) {
      return `${year}-${year + 1}`
    } else {
      return `${year - 1}-${year}`
    }
  }

  if (loading && departments.length === 0) {
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
                <h1 className="text-2xl font-bold text-academic-primary">Sınıf Yönetimi</h1>
                <p className="text-academic-text-secondary">Bölümlere göre sınıf oluşturma ve yönetimi</p>
              </div>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Department Selector */}
        <div className="card mb-6">
          <div className="flex flex-col md:flex-row md:items-end md:space-x-4 gap-3">
            <div className="flex-1">
              <label className="block text-sm font-medium text-academic-text-primary mb-1">Bölüm</label>
              <select
                value={selectedDeptId}
                onChange={e => setSelectedDeptId(e.target.value)}
                className="input-field"
              >
                <option value="">Bölüm seçin</option>
                {departments.map(d => (
                  <option key={d.id} value={d.id}>
                    {d.name} ({d.code})
                  </option>
                ))}
              </select>
            </div>
            {selectedDeptId && (
              <div className="flex-1">
                {!creatingClass && !editingClass ? (
                  <button className="btn-primary" onClick={() => {
                    setCreatingClass(true)
                    setNewClass({ name: '', academic_year: getCurrentAcademicYear() })
                  }}>
                    <Plus className="w-4 h-4 inline mr-2" /> Yeni Sınıf Ekle
                  </button>
                ) : (
                  <div className="flex items-end space-x-2">
                    <div className="flex-1">
                      <label className="block text-sm font-medium text-academic-text-primary mb-1">Sınıf Adı</label>
                      <input
                        className="input-field"
                        value={newClass.name}
                        onChange={e => setNewClass({ ...newClass, name: e.target.value })}
                        placeholder="1-A, 2-B, 3-C"
                      />
                    </div>
                    <div className="flex-1">
                      <label className="block text-sm font-medium text-academic-text-primary mb-1">Akademik Yıl</label>
                      <input
                        className="input-field"
                        value={newClass.academic_year}
                        onChange={e => setNewClass({ ...newClass, academic_year: e.target.value })}
                        placeholder="2024-2025"
                      />
                    </div>
                    <button 
                      className="btn-primary" 
                      onClick={() => editingClass ? updateClass(editingClass.id, selectedDeptId) : addClass(selectedDeptId)}
                    >
                      <Check className="w-4 h-4" />
                    </button>
                    <button className="btn-secondary" onClick={handleCancelEdit}>
                      <X className="w-4 h-4" />
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        {selectedDept && (
          <div className="space-y-6">
            {/* Classes list */}
            <div className="card">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-semibold text-academic-text-primary">{selectedDept.name} Sınıfları</h2>
              </div>
              {classes.length === 0 ? (
                <p className="text-academic-text-secondary">Bu bölümde henüz sınıf yok.</p>
              ) : (
                <div className="space-y-2">
                  {classes.map(cls => (
                    <div 
                      key={cls.id} 
                      className={`flex items-center justify-between p-3 rounded-md border ${
                        editingClass?.id === cls.id 
                          ? 'border-academic-primary bg-academic-primary-light' 
                          : 'border-academic-divider bg-academic-surface'
                      }`}
                    >
                      <div className="flex-1">
                        <span className="text-academic-text-primary font-medium">
                          {cls.name}
                        </span>
                        <span className="text-academic-text-secondary ml-3">
                          ({cls.academic_year})
                        </span>
                      </div>
                      <div className="flex items-center space-x-2">
                        <button
                          onClick={() => handleEdit(cls)}
                          className="p-2 text-academic-primary hover:bg-academic-primary-light rounded-md transition-colors"
                          title="Düzenle"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(cls.id)}
                          className="p-2 text-academic-error hover:bg-academic-error-light rounded-md transition-colors"
                          title="Sil"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  )
}

