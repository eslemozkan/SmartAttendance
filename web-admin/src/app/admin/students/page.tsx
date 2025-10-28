'use client'

import { useState, useEffect } from 'react'
import { supabase } from '@/lib/supabase'
import { Plus, Edit, Trash2, Search, ArrowLeft } from 'lucide-react'

export default function StudentManagement() {
  const [students, setStudents] = useState<any[]>([])
  const [classes, setClasses] = useState<any[]>([])
  const [departments, setDepartments] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editingStudent, setEditingStudent] = useState<any>(null)
  const [searchTerm, setSearchTerm] = useState('')

  const [formData, setFormData] = useState({
    full_name: '',
    email: '',
    class_id: '',
    department_id: ''
  })

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      // Öğrencileri yükle
      const { data: studentsData } = await supabase
        .from('students')
        .select(`id, full_name, email, is_active, class_id, department_id,
          classes(name),
          departments(name, code)
        `)
        .order('created_at', { ascending: false })

      // Sınıfları yükle
      const { data: classesData } = await supabase
        .from('classes')
        .select('*')
        .order('name')

      // Bölümleri yükle
      const { data: departmentsData } = await supabase
        .from('departments')
        .select('*')
        .order('name')

      console.log('Students data:', studentsData)
      console.log('Classes data:', classesData)
      console.log('Departments data:', departmentsData)
      
      setStudents(studentsData || [])
      setClasses(classesData || [])
      setDepartments(departmentsData || [])
    } catch (error) {
      console.error('Veri yüklenirken hata:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    try {
      if (editingStudent) {
        // Güncelleme
        const { error } = await supabase
          .from('students')
          .update(formData)
          .eq('id', editingStudent.id)

        if (error) throw error
      } else {
        // Yeni ekleme: duplicate email kontrolü
        const { data: exists } = await supabase
          .from('students')
          .select('id')
          .eq('email', formData.email)
          .maybeSingle()
        if (exists) throw new Error('Bu email ile öğrenci zaten kayıtlı')

        const { error } = await supabase
          .from('students')
          .insert([formData])

        if (error) throw error
      }

      // Formu sıfırla
      setFormData({
        full_name: '',
        email: '',
        class_id: '',
        department_id: ''
      })
      setShowForm(false)
      setEditingStudent(null)
      loadData()
    } catch (error) {
      console.error('Öğrenci kaydedilirken hata:', error)
      alert('Hata: ' + error.message)
    }
  }

  const handleEdit = (student: any) => {
    setEditingStudent(student)
    setFormData({
      full_name: student.full_name,
      email: student.email,
      class_id: student.class_id,
      department_id: student.department_id
    })
    setShowForm(true)
  }

  const handleDelete = async (id: string) => {
    if (!confirm('Bu öğrenciyi silmek istediğinizden emin misiniz?')) return

    try {
      const { error } = await supabase
        .from('students')
        .delete()
        .eq('id', id)

      if (error) throw error
      loadData()
    } catch (error) {
      console.error('Öğrenci silinirken hata:', error)
      alert('Hata: ' + error.message)
    }
  }

  const filteredStudents = students.filter(student =>
    student.full_name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    student.email.toLowerCase().includes(searchTerm.toLowerCase())
  )

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
              <button 
                onClick={() => window.location.href = '/'}
                className="btn-outline flex items-center space-x-2"
              >
                <ArrowLeft className="w-4 h-4" />
                <span>Geri</span>
              </button>
              <div>
                <h1 className="text-2xl font-bold text-academic-primary">Öğrenci Yönetimi</h1>
                <p className="text-academic-text-secondary">Öğrenci ekleme, düzenleme ve silme işlemleri</p>
              </div>
            </div>
            <button 
              onClick={() => setShowForm(true)}
              className="btn-primary flex items-center space-x-2"
            >
              <Plus className="w-4 h-4" />
              <span>Yeni Öğrenci</span>
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Search */}
        <div className="mb-6">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-academic-text-secondary w-5 h-5" />
            <input
              type="text"
              placeholder="Öğrenci ara (ad, numara, email)..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="input-field pl-10"
            />
          </div>
        </div>

        {/* Students Table */}
        <div className="card">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="table-header">
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">Ad Soyad</th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">Email</th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">Sınıf</th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">Bölüm</th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">Durum</th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">İşlemler</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-academic-divider">
                {filteredStudents.map((student) => (
                  <tr key={student.id} className="table-row">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-academic-text-primary">
                      {student.full_name}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-academic-text-primary">
                      {student.email}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-academic-text-primary">
                      {student.classes?.name || '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-academic-text-primary">
                      {student.departments?.name || '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                        student.is_active 
                          ? 'bg-academic-success bg-opacity-10 text-academic-success' 
                          : 'bg-academic-error bg-opacity-10 text-academic-error'
                      }`}>
                        {student.is_active ? 'Aktif' : 'Pasif'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex space-x-2">
                        <button
                          onClick={() => handleEdit(student)}
                          className="text-academic-primary hover:text-academic-primary-dark"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(student.id)}
                          className="text-academic-error hover:text-academic-error"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Add/Edit Form Modal */}
        {showForm && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-academic-surface rounded-lg p-6 w-full max-w-md mx-4">
              <h3 className="text-lg font-semibold text-academic-text-primary mb-4">
                {editingStudent ? 'Öğrenci Düzenle' : 'Yeni Öğrenci Ekle'}
              </h3>
              
              <form onSubmit={handleSubmit} className="space-y-4">
                

                <div>
                  <label className="block text-sm font-medium text-academic-text-primary mb-1">
                    Ad Soyad
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.full_name}
                    onChange={(e) => setFormData({...formData, full_name: e.target.value})}
                    className="input-field"
                    placeholder="Ahmet Yılmaz"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-academic-text-primary mb-1">
                    Email
                  </label>
                  <input
                    type="email"
                    required
                    value={formData.email}
                    onChange={(e) => setFormData({...formData, email: e.target.value})}
                    className="input-field"
                    placeholder="ahmet@university.edu"
                  />
                </div>

                

                <div>
                  <label className="block text-sm font-medium text-academic-text-primary mb-1">
                    Bölüm
                  </label>
                  <select
                    required
                    value={formData.department_id}
                    onChange={(e) => setFormData({...formData, department_id: e.target.value, class_id: ''})}
                    className="input-field"
                  >
                    <option value="">Bölüm Seçin</option>
                    {departments.map((dept) => (
                      <option key={dept.id} value={dept.id}>
                        {dept.name} ({dept.code})
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-academic-text-primary mb-1">
                    Sınıf
                  </label>
                  <select
                    required
                    value={formData.class_id}
                    onChange={(e) => setFormData({...formData, class_id: e.target.value})}
                    className="input-field"
                    disabled={!formData.department_id}
                  >
                    <option value="">
                      {formData.department_id ? "Sınıf Seçin" : "Önce bölüm seçin"}
                    </option>
                    {classes
                      .filter(c => c.department_id === formData.department_id)
                      .map((cls) => (
                        <option key={cls.id} value={cls.id}>
                          {cls.name}
                        </option>
                      ))}
                  </select>
                  {formData.department_id && classes.filter(c => c.department_id === formData.department_id).length === 0 && (
                    <p className="text-sm text-academic-text-secondary mt-1">
                      Bu bölüm için sınıf bulunamadı
                    </p>
                  )}
                </div>

                <div className="flex space-x-3 pt-4">
                  <button
                    type="submit"
                    className="btn-primary flex-1"
                  >
                    {editingStudent ? 'Güncelle' : 'Ekle'}
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setShowForm(false)
                      setEditingStudent(null)
                      setFormData({
                        full_name: '',
                        email: '',
                        class_id: '',
                        department_id: ''
                      })
                    }}
                    className="btn-secondary flex-1"
                  >
                    İptal
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </main>
    </div>
  )
}


