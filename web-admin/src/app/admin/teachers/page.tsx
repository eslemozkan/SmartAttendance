'use client'

import { useState, useEffect } from 'react'
import { supabase } from '@/lib/supabase'
import { Plus, Edit, Trash2, Search, ArrowLeft, Upload } from 'lucide-react'

export default function TeacherManagement() {
  const [teachers, setTeachers] = useState<any[]>([])
  const [departments, setDepartments] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [showCSVForm, setShowCSVForm] = useState(false)
  const [editingTeacher, setEditingTeacher] = useState<any>(null)
  const [searchTerm, setSearchTerm] = useState('')
  const [csvFile, setCsvFile] = useState<File | null>(null)
  const [uploading, setUploading] = useState(false)

  const [formData, setFormData] = useState({
    full_name: '',
    email: '',
    department_id: ''
  })

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      // Öğretmenleri yükle (profiles tablosundan, teacher role ile)
      const { data: teachersData } = await supabase
        .from('profiles')
        .select(`id, full_name, email, department_id,
          departments(name, code)
        `)
        .eq('role', 'teacher')
        .order('full_name', { ascending: true })

      // Bölümleri yükle
      const { data: departmentsData } = await supabase
        .from('departments')
        .select('*')
        .order('name')

      console.log('Teachers data:', teachersData)
      console.log('Departments data:', departmentsData)
      
      setTeachers(teachersData || [])
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
      // Teacher kaydı profiles tablosuna eklenir
      const { error } = await supabase
        .from('profiles')
        .insert([{
          ...formData,
          role: 'teacher'
        }])

      if (error) throw error

      // Formu sıfırla
      setFormData({
        full_name: '',
        email: '',
        department_id: ''
      })
      setShowForm(false)
      setEditingTeacher(null)
      loadData()
    } catch (error) {
      console.error('Öğretmen kaydedilirken hata:', error)
      alert('Hata: ' + error.message)
    }
  }

  const handleEdit = (teacher: any) => {
    setEditingTeacher(teacher)
    setFormData({
      full_name: teacher.full_name,
      email: teacher.email,
      department_id: teacher.department_id
    })
    setShowForm(true)
  }

  const handleDelete = async (id: string) => {
    if (!confirm('Bu öğretmeni silmek istediğinizden emin misiniz?')) return

    try {
      const { error } = await supabase
        .from('profiles')
        .delete()
        .eq('id', id)

      if (error) throw error
      loadData()
    } catch (error) {
      console.error('Öğretmen silinirken hata:', error)
      alert('Hata: ' + error.message)
    }
  }

  const handleCSVUpload = async () => {
    if (!csvFile) {
      alert('Lütfen bir CSV dosyası seçin')
      return
    }

    setUploading(true)
    try {
      const text = await csvFile.text()
      const lines = text.split('\n').filter(line => line.trim())
      
      const dataLines = lines.slice(1)
      
      const teachersToAdd = []
      let successCount = 0
      let errorCount = 0

      for (const line of dataLines) {
        if (!line.trim()) continue
        
        const [email, full_name, department_code] = line.split(',').map(s => s.trim().replace(/"/g, ''))
        
        if (!email || !full_name) continue

        // Bölümü bul
        const dept = departments.find(d => d.code === department_code)
        if (!dept) {
          console.error(`Bölüm bulunamadı: ${department_code}`)
          errorCount++
          continue
        }

        teachersToAdd.push({
          email,
          full_name,
          department_id: dept.id,
          role: 'teacher'
        })
      }

      // Toplu ekleme
      if (teachersToAdd.length > 0) {
        const { error } = await supabase
          .from('profiles')
          .insert(teachersToAdd)

        if (error) throw error
        successCount = teachersToAdd.length
      }

      alert(`${successCount} öğretmen eklendi${errorCount > 0 ? `, ${errorCount} hata oluştu` : ''}`)
      setShowCSVForm(false)
      setCsvFile(null)
      loadData()
    } catch (error) {
      console.error('CSV yüklenirken hata:', error)
      alert('Hata: ' + error.message)
    } finally {
      setUploading(false)
    }
  }

  const filteredTeachers = teachers.filter(teacher =>
    teacher.full_name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    teacher.email.toLowerCase().includes(searchTerm.toLowerCase())
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
                <h1 className="text-2xl font-bold text-academic-primary">Öğretmen Yönetimi</h1>
                <p className="text-academic-text-secondary">Öğretmen ekleme, düzenleme ve silme işlemleri</p>
              </div>
            </div>
            <div className="flex space-x-2">
              <button 
                onClick={() => setShowCSVForm(true)}
                className="btn-outline flex items-center space-x-2"
              >
                <Upload className="w-4 h-4" />
                <span>CSV Yükle</span>
              </button>
              <button 
                onClick={() => setShowForm(true)}
                className="btn-primary flex items-center space-x-2"
              >
                <Plus className="w-4 h-4" />
                <span>Yeni Öğretmen</span>
              </button>
            </div>
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
              placeholder="Öğretmen ara (ad, email)..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="input-field pl-10"
            />
          </div>
        </div>

        {/* Teachers Table */}
        <div className="card">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="table-header">
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">Ad Soyad</th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">Email</th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">Bölüm</th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider">İşlemler</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-academic-divider">
                {filteredTeachers.map((teacher) => (
                  <tr key={teacher.id} className="table-row">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-academic-text-primary">
                      {teacher.full_name}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-academic-text-primary">
                      {teacher.email}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-academic-text-primary">
                      {teacher.departments?.name || '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex space-x-2">
                        <button
                          onClick={() => handleEdit(teacher)}
                          className="text-academic-primary hover:text-academic-primary-dark"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(teacher.id)}
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

        {/* CSV Upload Modal */}
        {showCSVForm && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-academic-surface rounded-lg p-6 w-full max-w-md mx-4">
              <h3 className="text-lg font-semibold text-academic-text-primary mb-4">
                CSV ile Öğretmen Yükle
              </h3>
              
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-academic-text-primary mb-2">
                    CSV Dosyası Seç
                  </label>
                  <input
                    type="file"
                    accept=".csv"
                    onChange={(e) => setCsvFile(e.target.files?.[0] || null)}
                    className="input-field"
                  />
                  <p className="text-sm text-academic-text-secondary mt-2">
                    CSV formatı: email,full_name,department_code
                  </p>
                  <p className="text-xs text-academic-text-secondary mt-1">
                    Örnek: ahmet@example.com,Ahmet Yılmaz,BM
                  </p>
                </div>

                <div className="flex space-x-3 pt-4">
                  <button
                    type="button"
                    onClick={handleCSVUpload}
                    disabled={!csvFile || uploading}
                    className="btn-primary flex-1 disabled:opacity-50"
                  >
                    {uploading ? 'Yükleniyor...' : 'Yükle'}
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setShowCSVForm(false)
                      setCsvFile(null)
                    }}
                    className="btn-secondary flex-1"
                  >
                    İptal
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Add/Edit Form Modal */}
        {showForm && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-academic-surface rounded-lg p-6 w-full max-w-md mx-4">
              <h3 className="text-lg font-semibold text-academic-text-primary mb-4">
                {editingTeacher ? 'Öğretmen Düzenle' : 'Yeni Öğretmen Ekle'}
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
                    placeholder="Prof. Dr. Ahmet Yılmaz"
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
                    onChange={(e) => setFormData({...formData, department_id: e.target.value})}
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

                <div className="flex space-x-3 pt-4">
                  <button
                    type="submit"
                    className="btn-primary flex-1"
                  >
                    {editingTeacher ? 'Güncelle' : 'Ekle'}
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setShowForm(false)
                      setEditingTeacher(null)
                      setFormData({
                        full_name: '',
                        email: '',
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

